package org.supla.android.features.details.relayschedule
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.SuplaDeviceConfig
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaRelayMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.DeviceConfigEventsManager
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.details.relayschedule.data.MAX_PROGRAM_DURATION_S
import org.supla.android.features.details.relayschedule.data.RelayProgramDuration
import org.supla.android.features.details.relayschedule.data.RelayProgramSettingsData
import org.supla.android.features.details.relayschedule.data.RelayScheduleProgram
import org.supla.android.features.details.relayschedule.extensions.toWeeklyScheduleConfigChange
import org.supla.android.features.details.relayschedule.extensions.viewRelayProgramsList
import org.supla.android.features.details.relayschedule.ui.dialogs.RelayProgramSettingsViewState
import org.supla.android.features.details.schedule.DelayedWeeklyScheduleConfigSubject
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey
import org.supla.android.ui.views.schedule.editor.QuartersSelectionData
import org.supla.android.ui.views.schedule.editor.ScheduleTableBox
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleEditorState
import org.supla.android.ui.views.schedule.editor.quartersSelectionData
import org.supla.android.ui.views.schedule.editor.viewScheduleTableState
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val PROGRAM_MODES = listOf(
  SuplaRelayMode.START_ON,
  SuplaRelayMode.START_OFF,
  SuplaRelayMode.FORCED_ON,
  SuplaRelayMode.FORCED_OFF,
  SuplaRelayMode.AUTOMATIC
)

private const val CONFIG_RELOAD_DELAY_MS = 1000L
private const val INTERACTION_PROTECTION_DELAY_MS = 3000L

@HiltViewModel
class RelayScheduleViewModel @Inject constructor(
  private val delayedWeeklyScheduleConfigSubject: DelayedWeeklyScheduleConfigSubject,
  private val channelConfigEventsManager: ChannelConfigEventsManager,
  private val deviceConfigEventsManager: DeviceConfigEventsManager,
  private val loadingTimeoutManager: LoadingTimeoutManager,
  private val suplaClientProvider: SuplaClientProvider,
  private val dateProvider: DateProvider,
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  threading: SuplaThreading
) : BaseViewModel<RelayScheduleViewState, RelayScheduleViewEvent>(
  RelayScheduleViewState(),
  threading
),
  RelayScheduleScope {

  private val configReloadSubject = PublishSubject.create<Unit>()
  private var remoteId: Int = 0
  private var changing: Boolean = false
  private var lastInteractionTime: Long? = null
  private var automaticModeSupported: Boolean = false

  override fun onViewCreated() {
    loadingTimeoutManager.watch({ currentState().loadingState }) {
      updateState { state ->
        reloadConfig(remoteId)
        state.copy(loadingState = state.loadingState.changingLoading(false, dateProvider))
      }
    }.disposeBySelf()
  }

  fun observeConfig(remoteId: Int, deviceId: Int) {
    this.remoteId = remoteId
    changing = false
    lastInteractionTime = null
    automaticModeSupported = false

    configReloadSubject.attachSilent()
      .debounce(CONFIG_RELOAD_DELAY_MS, TimeUnit.MILLISECONDS, threading.schedulers.computation)
      .subscribeBy(
        onNext = { reloadConfig(remoteId) },
        onError = defaultErrorHandler("observeConfig($remoteId)")
      )
      .disposeBySelf()

    updateState {
      it.copy(loadingState = it.loadingState.changingLoading(true, dateProvider))
    }

    Observable.combineLatest(
      channelConfigEventsManager.observerConfig(remoteId)
        .filter { it.config is SuplaChannelWeeklyScheduleConfig },
      deviceConfigEventsManager.observerConfig(deviceId),
      readChannelByRemoteIdUseCase(remoteId).toObservable()
    ) { weeklyConfig, deviceConfig, channel ->
      LoadedData(
        weeklyScheduleConfig = weeklyConfig.config as SuplaChannelWeeklyScheduleConfig,
        weeklyScheduleResult = weeklyConfig.result,
        deviceConfig = deviceConfig.config,
        channelFlags = channel.flags
      )
    }
      .debounce(50, TimeUnit.MILLISECONDS, threading.schedulers.computation)
      .subscribeBy(
        onNext = { onConfigLoaded(it) },
        onError = defaultErrorHandler("observeConfig($remoteId)")
      )
      .disposeBySelf()

    reloadConfig(remoteId)
    suplaClientProvider.provide()?.getDeviceConfig(deviceId)
  }

  override fun onScheduleProgramClick(program: SuplaScheduleProgram) {
    updateState { state ->
      state.copy(
        editorState = state.editorState.copy(
          activeProgram = getProgramForChange(program, state)
        )
      )
    }
  }

  override fun onScheduleProgramLongClick(program: SuplaScheduleProgram) {
    if (program == SuplaScheduleProgram.OFF) {
      return
    }

    updateState { state ->
      val programConfiguration = state.editorState.programs.firstOrNull { it.program == program }
        ?: return@updateState state
      val relayDurationS = (programConfiguration.relayModeDurationS ?: 0).coerceIn(0, MAX_PROGRAM_DURATION_S)
      val relayOppositeDurationS = if (relayDurationS > 0) {
        (programConfiguration.relayOppositeModeDurationS ?: 0).coerceIn(0, MAX_PROGRAM_DURATION_S)
      } else {
        0
      }
      val availableModes = availableProgramModes()
      val data = RelayProgramSettingsData(
        program = program,
        selectedMode = programConfiguration.relayMode.takeIf { programConfiguration.isValid && it in availableModes }
          ?: SuplaRelayMode.START_ON,
        relayDurationS = relayDurationS,
        relayOppositeDurationS = relayOppositeDurationS
      )

      state.copy(
        programSettings = RelayProgramSettingsViewState(data = data, modes = availableModes)
      )
    }
  }

  override fun onProgramSettingsModeChange(mode: SuplaRelayMode) {
    updateState { state ->
      val settings = state.programSettings ?: return@updateState state
      if (mode !in settings.modes) {
        return@updateState state
      }

      state.copy(programSettings = settings.copy(data = settings.data.copy(selectedMode = mode)))
    }
  }

  override fun onProgramSettingsDurationMinusClick(duration: RelayProgramDuration) {
    changeProgramDuration(duration) { it.minus(1).coerceAtLeast(0) }
  }

  override fun onProgramSettingsDurationPlusClick(duration: RelayProgramDuration) {
    changeProgramDuration(duration) { it.plus(1).coerceAtMost(MAX_PROGRAM_DURATION_S) }
  }

  override fun onProgramSettingsDurationManualChange(duration: RelayProgramDuration, value: String) {
    if (durationChangeAllowed(duration).not()) {
      return
    }

    val normalizedValue = value.ifEmpty { "0" }
    if (normalizedValue.any { it.isDigit().not() }) {
      return
    }

    val durationS = normalizedValue.toIntOrNull() ?: return
    if (durationS !in 0..MAX_PROGRAM_DURATION_S) {
      return
    }

    updateProgramDuration(duration, durationS, normalizedValue)
  }

  override fun onProgramSettingsDismiss() {
    updateState { it.copy(programSettings = null) }
  }

  override fun onProgramSettingsSave() {
    updateState { state ->
      val settings = state.programSettings ?: return@updateState state
      val data = settings.data
      val updatedProgram = RelayScheduleProgram(
        SuplaWeeklyScheduleProgram(
          program = data.program,
          relayMode = data.selectedMode,
          relayModeDurationS = data.relayDurationS,
          relayOppositeModeDurationS = data.relayOppositeDurationS.takeIf { data.relayDurationS > 0 } ?: 0
        )
      )
      val newState = state.copy(
        editorState = state.editorState.copy(
          programs = state.editorState.programs.map { if (it.program == data.program) updatedProgram else it },
          activeProgram = data.program
        ),
        programSettings = null
      )

      lastInteractionTime = dateProvider.currentTimestamp()
      emitScheduleChange(newState)
      newState
    }
  }

  override fun onScheduleTableLongPress(key: ScheduleDetailEntryBoxKey?) {
    updateState { it.copy(quarterSelection = it.editorState.quartersSelectionData(key)) }
  }

  override fun onQuartersSelectionDismiss() {
    updateState { it.copy(quarterSelection = null) }
  }

  override fun onQuartersSelectionProgramChange(program: SuplaScheduleProgram) {
    updateState { state ->
      val newProgram = getProgramForChange(program, state)
      state.copy(
        editorState = state.editorState.copy(activeProgram = newProgram),
        quarterSelection = state.quarterSelection?.copy(activeProgram = newProgram)
      )
    }
  }

  override fun onQuartersSelectionQuarterChange(quarterOfHour: QuarterOfHour) {
    updateState { state ->
      val selection = state.quarterSelection ?: return@updateState state
      val activeProgram = selection.activeProgram ?: return@updateState state
      val entryValue = when (quarterOfHour) {
        QuarterOfHour.FIRST -> selection.entryValue.copy(firstQuarterProgram = activeProgram)
        QuarterOfHour.SECOND -> selection.entryValue.copy(secondQuarterProgram = activeProgram)
        QuarterOfHour.THIRD -> selection.entryValue.copy(thirdQuarterProgram = activeProgram)
        QuarterOfHour.FOURTH -> selection.entryValue.copy(fourthQuarterProgram = activeProgram)
      }

      state.copy(quarterSelection = selection.copy(entryValue = entryValue))
    }
  }

  override fun onQuartersSelectionFinish() {
    updateState { state ->
      val selection = state.quarterSelection ?: return@updateState state
      val newState = state.copy(
        editorState = state.editorState.copy(
          scheduleTableState = state.editorState.scheduleTableState.copy(
            schedule = state.editorState.scheduleTableState.schedule + (selection.entryKey to selection.entryValue)
          ),
          activeProgram = selection.activeProgram
        ),
        quarterSelection = null
      )

      lastInteractionTime = dateProvider.currentTimestamp()
      emitScheduleChange(newState)
      newState
    }
  }

  override fun onScheduleTableTouched(key: ScheduleDetailEntryBoxKey) {
    currentState().let { state ->
      val activeProgram = state.editorState.activeProgram ?: return

      if (state.editorState.scheduleTableState.schedule[key]?.singleProgram != activeProgram) {
        changing = true
        updateState {
          state.copy(
            editorState = state.editorState.copy(
              scheduleTableState = state.editorState.scheduleTableState.copy(
                schedule = state.editorState.scheduleTableState.schedule + (key to ScheduleTableBox(activeProgram))
              )
            )
          )
        }
      }

      emitScheduleChange(currentState())
    }
  }

  override fun onScheduleTableReload() {
    emitScheduleChange(currentState())
    changing = false
    lastInteractionTime = dateProvider.currentTimestamp()
  }

  override fun onScheduleTableInvalidate() {
    reloadConfig(remoteId)
  }

  private fun emitScheduleChange(state: RelayScheduleViewState) {
    delayedWeeklyScheduleConfigSubject.emit(state.toWeeklyScheduleConfigChange(remoteId))
  }

  private fun availableProgramModes(): List<SuplaRelayMode> = PROGRAM_MODES.filter {
    it != SuplaRelayMode.AUTOMATIC || automaticModeSupported
  }

  private fun changeProgramDuration(duration: RelayProgramDuration, change: (Int) -> Int) {
    val settings = currentState().programSettings ?: return
    if (durationChangeAllowed(duration, settings).not()) {
      return
    }

    val currentValue = when (duration) {
      RelayProgramDuration.RELAY_MODE -> settings.data.relayDurationS
      RelayProgramDuration.OPPOSITE_MODE -> settings.data.relayOppositeDurationS
    }
    val newValue = change(currentValue)
    updateProgramDuration(duration, newValue, newValue.toString())
  }

  private fun durationChangeAllowed(
    duration: RelayProgramDuration,
    settings: RelayProgramSettingsViewState? = currentState().programSettings
  ): Boolean = settings != null &&
    (duration == RelayProgramDuration.RELAY_MODE || settings.data.relayDurationS > 0)

  private fun updateProgramDuration(duration: RelayProgramDuration, value: Int, valueString: String) {
    updateState { state ->
      val settings = state.programSettings ?: return@updateState state
      val data = when (duration) {
        RelayProgramDuration.RELAY_MODE -> settings.data.copy(
          relayDurationS = value,
          relayOppositeDurationS = if (value == 0) 0 else settings.data.relayOppositeDurationS
        )
        RelayProgramDuration.OPPOSITE_MODE -> settings.data.copy(relayOppositeDurationS = value)
      }
      val newSettings = RelayProgramSettingsViewState(
        data = data,
        modes = settings.modes,
        relayDurationSString = if (duration == RelayProgramDuration.RELAY_MODE) valueString else settings.relayDurationSString,
        relayOppositeDurationSString = if (duration == RelayProgramDuration.RELAY_MODE && value == 0) {
          "0"
        } else if (duration == RelayProgramDuration.OPPOSITE_MODE) {
          valueString
        } else {
          settings.relayOppositeDurationSString
        }
      )
      state.copy(programSettings = newSettings)
    }
  }

  private fun getProgramForChange(program: SuplaScheduleProgram, state: RelayScheduleViewState): SuplaScheduleProgram? {
    if (state.editorState.activeProgram == program) {
      return null
    }

    val programConfiguration = state.editorState.programs.firstOrNull { it.program == program }
    if (programConfiguration?.isValid == false) {
      return null
    }

    return program
  }

  private fun reloadConfig(remoteId: Int) {
    suplaClientProvider.provide()?.getChannelConfig(remoteId, ChannelConfigType.WEEKLY_SCHEDULE)
  }

  private fun onConfigLoaded(data: LoadedData) {
    Timber.i("Relay schedule got data: $data")

    if (data.weeklyScheduleResult != ConfigResult.RESULT_TRUE) {
      return
    }

    automaticModeSupported = SuplaChannelFlag.RELAY_MODE_AUTOMATIC_SUPPORTED inside data.channelFlags

    if (changing) {
      Timber.d("Relay schedule update skipped because of changing")
      return
    }

    if (lastInteractionTime?.plus(INTERACTION_PROTECTION_DELAY_MS)?.let { it > dateProvider.currentTimestamp() } == true) {
      Timber.d("Relay schedule update skipped because of last interaction time")
      configReloadSubject.onNext(Unit)
      return
    }

    updateState {
      it.copy(
        loadingState = it.loadingState.changingLoading(false, dateProvider),
        editorState = it.editorState.copy(
          programs = data.weeklyScheduleConfig.viewRelayProgramsList(),
          scheduleTableState = data.weeklyScheduleConfig.viewScheduleTableState(data.deviceConfig, dateProvider)
        )
      )
    }
  }

  private data class LoadedData(
    val weeklyScheduleConfig: SuplaChannelWeeklyScheduleConfig,
    val weeklyScheduleResult: ConfigResult,
    val deviceConfig: SuplaDeviceConfig?,
    val channelFlags: Long
  )
}

sealed class RelayScheduleViewEvent : ViewEvent

data class RelayScheduleViewState(
  val loadingState: LoadingTimeoutManager.LoadingState = LoadingTimeoutManager.LoadingState(),
  val editorState: WeeklyScheduleEditorState<RelayScheduleProgram> = WeeklyScheduleEditorState(),
  val quarterSelection: QuartersSelectionData? = null,
  val programSettings: RelayProgramSettingsViewState? = null
) : ViewState()
