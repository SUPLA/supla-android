package org.supla.android.features.details.switchdetail.schedule
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
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.SuplaDeviceConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaRelayMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.DeviceConfigEventsManager
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.details.switchdetail.schedule.data.MAX_PROGRAM_DURATION_S
import org.supla.android.features.details.switchdetail.schedule.data.SwitchProgramDuration
import org.supla.android.features.details.switchdetail.schedule.data.SwitchProgramSettingsData
import org.supla.android.features.details.switchdetail.schedule.data.SwitchScheduleProgram
import org.supla.android.features.details.switchdetail.schedule.extensions.viewProgramsList
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey
import org.supla.android.ui.views.schedule.editor.QuartersSelectionData
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleEditorState
import org.supla.android.ui.views.schedule.editor.quartersSelectionData
import org.supla.android.ui.views.schedule.editor.viewScheduleTableState
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

@HiltViewModel
class SwitchScheduleViewModel @Inject constructor(
  private val channelConfigEventsManager: ChannelConfigEventsManager,
  private val deviceConfigEventsManager: DeviceConfigEventsManager,
  private val loadingTimeoutManager: LoadingTimeoutManager,
  private val suplaClientProvider: SuplaClientProvider,
  private val dateProvider: DateProvider,
  threading: SuplaThreading
) : BaseViewModel<SwitchScheduleViewState, SwitchScheduleViewEvent>(
  SwitchScheduleViewState(),
  threading
),
  SwitchScheduleScope {

  private var remoteId: Int = 0

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

    updateState {
      it.copy(loadingState = it.loadingState.changingLoading(true, dateProvider))
    }

    Observable.combineLatest(
      channelConfigEventsManager.observerConfig(remoteId)
        .filter { it.config is SuplaChannelWeeklyScheduleConfig },
      deviceConfigEventsManager.observerConfig(deviceId)
    ) { weeklyConfig, deviceConfig ->
      LoadedData(
        weeklyScheduleConfig = weeklyConfig.config as SuplaChannelWeeklyScheduleConfig,
        weeklyScheduleResult = weeklyConfig.result,
        deviceConfig = deviceConfig.config
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
      val relayModeDurationS = (programConfiguration.relayModeDurationS ?: 0).coerceIn(0, MAX_PROGRAM_DURATION_S)
      val relayOppositeModeDurationS = (programConfiguration.relayOppositeModeDurationS ?: 0).coerceIn(0, MAX_PROGRAM_DURATION_S)

      state.copy(
        programSettings = SwitchProgramSettingsData(
          program = program,
          modes = PROGRAM_MODES,
          selectedMode = programConfiguration.relayMode.takeIf { programConfiguration.isValid } ?: SuplaRelayMode.START_ON,
          relayModeDurationS = relayModeDurationS,
          relayOppositeModeDurationS = relayOppositeModeDurationS
        )
      )
    }
  }

  override fun onProgramSettingsModeChange(mode: SuplaRelayMode) {
    if (mode !in PROGRAM_MODES) {
      return
    }

    updateState { state ->
      state.copy(programSettings = state.programSettings?.copy(selectedMode = mode))
    }
  }

  override fun onProgramSettingsDurationMinusClick(duration: SwitchProgramDuration) {
    changeProgramDuration(duration) { it.minus(1).coerceAtLeast(0) }
  }

  override fun onProgramSettingsDurationPlusClick(duration: SwitchProgramDuration) {
    changeProgramDuration(duration) { it.plus(1).coerceAtMost(MAX_PROGRAM_DURATION_S) }
  }

  override fun onProgramSettingsDurationManualChange(duration: SwitchProgramDuration, value: String) {
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

  override fun onProgramSettingsSave() {}

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
      state.copy(
        editorState = state.editorState.copy(
          scheduleTableState = state.editorState.scheduleTableState.copy(
            schedule = state.editorState.scheduleTableState.schedule + (selection.entryKey to selection.entryValue)
          ),
          activeProgram = selection.activeProgram
        ),
        quarterSelection = null
      )
    }
  }

  override fun onScheduleTableTouched(key: ScheduleDetailEntryBoxKey) {}

  override fun onScheduleTableReload() {}

  override fun onScheduleTableInvalidate() {}

  private fun changeProgramDuration(duration: SwitchProgramDuration, change: (Int) -> Int) {
    val settings = currentState().programSettings ?: return
    val currentValue = when (duration) {
      SwitchProgramDuration.RELAY_MODE -> settings.relayModeDurationS
      SwitchProgramDuration.OPPOSITE_MODE -> settings.relayOppositeModeDurationS
    }
    val newValue = change(currentValue)
    updateProgramDuration(duration, newValue, newValue.toString())
  }

  private fun updateProgramDuration(duration: SwitchProgramDuration, value: Int, valueString: String) {
    updateState { state ->
      val settings = state.programSettings ?: return@updateState state
      val newSettings = when (duration) {
        SwitchProgramDuration.RELAY_MODE -> settings.copy(
          relayModeDurationS = value,
          relayModeDurationSString = valueString
        )
        SwitchProgramDuration.OPPOSITE_MODE -> settings.copy(
          relayOppositeModeDurationS = value,
          relayOppositeModeDurationSString = valueString
        )
      }
      state.copy(programSettings = newSettings)
    }
  }

  private fun getProgramForChange(program: SuplaScheduleProgram, state: SwitchScheduleViewState): SuplaScheduleProgram? {
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
    Timber.i("Switch schedule got data: $data")

    if (data.weeklyScheduleResult != ConfigResult.RESULT_TRUE) {
      return
    }

    updateState {
      it.copy(
        loadingState = it.loadingState.changingLoading(false, dateProvider),
        editorState = it.editorState.copy(
          programs = data.weeklyScheduleConfig.viewProgramsList(),
          scheduleTableState = data.weeklyScheduleConfig.viewScheduleTableState(data.deviceConfig, dateProvider)
        )
      )
    }
  }

  private data class LoadedData(
    val weeklyScheduleConfig: SuplaChannelWeeklyScheduleConfig,
    val weeklyScheduleResult: ConfigResult,
    val deviceConfig: SuplaDeviceConfig?
  )
}

sealed class SwitchScheduleViewEvent : ViewEvent

data class SwitchScheduleViewState(
  val loadingState: LoadingTimeoutManager.LoadingState = LoadingTimeoutManager.LoadingState(),
  val editorState: WeeklyScheduleEditorState<SwitchScheduleProgram> = WeeklyScheduleEditorState(),
  val quarterSelection: QuartersSelectionData? = null,
  val programSettings: SwitchProgramSettingsData? = null
) : ViewState()
