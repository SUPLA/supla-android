package org.supla.android.features.details.actiontrigger.schedule
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
import org.supla.android.data.source.remote.hvac.SuplaButtonMode
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.DeviceConfigEventsManager
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.details.actiontrigger.schedule.data.ActionTriggerScheduleProgram
import org.supla.android.features.details.actiontrigger.schedule.data.AtProgramSettingsData
import org.supla.android.features.details.actiontrigger.schedule.data.MAX_TEMPORARY_UNLOCK_DURATION_S
import org.supla.android.features.details.actiontrigger.schedule.extensions.toWeeklyScheduleConfigChange
import org.supla.android.features.details.actiontrigger.schedule.extensions.viewActionTriggerProgramsList
import org.supla.android.features.details.actiontrigger.schedule.ui.dialogs.AtProgramSettingsViewState
import org.supla.android.features.details.schedule.DelayedWeeklyScheduleConfigSubject
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey
import org.supla.android.ui.views.schedule.editor.QuartersSelectionData
import org.supla.android.ui.views.schedule.editor.ScheduleTableBox
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleEditorState
import org.supla.android.ui.views.schedule.editor.quartersSelectionData
import org.supla.android.ui.views.schedule.editor.viewScheduleTableState
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val CONFIG_RELOAD_DELAY_MS = 1000L
private const val INTERACTION_PROTECTION_DELAY_MS = 3000L

@HiltViewModel
class ActionTriggerScheduleViewModel @Inject constructor(
  private val delayedWeeklyScheduleConfigSubject: DelayedWeeklyScheduleConfigSubject,
  private val channelConfigEventsManager: ChannelConfigEventsManager,
  private val deviceConfigEventsManager: DeviceConfigEventsManager,
  private val loadingTimeoutManager: LoadingTimeoutManager,
  private val suplaClientProvider: SuplaClientProvider,
  private val dateProvider: DateProvider,
  threading: SuplaThreading
) : BaseViewModel<ActionTriggerScheduleViewState, ActionTriggerScheduleViewEvent>(
  ActionTriggerScheduleViewState(),
  threading
),
  ActionTriggerScheduleScope {

  private val configReloadSubject = PublishSubject.create<Unit>()
  private var remoteId: Int = 0
  private var changing: Boolean = false
  private var lastInteractionTime: Long? = null

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
      val data = AtProgramSettingsData(
        program = program,
        selectedMode = programConfiguration.buttonMode,
        buttonTemporaryUnlockDurationS = programConfiguration.buttonTemporaryUnlockDurationS ?: 0
      )

      state.copy(programSettings = AtProgramSettingsViewState(data))
    }
  }

  override fun onAtProgramSettingsModeChange(mode: SuplaButtonMode) {
    updateState { state ->
      val settings = state.programSettings ?: return@updateState state
      if (mode !in ALLOWED_MODES) {
        return@updateState state
      }

      val data = settings.data.copy(
        selectedMode = mode,
        buttonTemporaryUnlockDurationS = if (mode == SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED) {
          settings.data.buttonTemporaryUnlockDurationS
        } else {
          0
        }
      )
      state.copy(programSettings = AtProgramSettingsViewState(data))
    }
  }

  override fun onAtProgramSettingsDurationChange(value: Int) {
    val duration = value.coerceIn(0, MAX_TEMPORARY_UNLOCK_DURATION_S)
    updateState { state ->
      val settings = state.programSettings ?: return@updateState state
      val data = settings.data.copy(buttonTemporaryUnlockDurationS = duration)
      state.copy(programSettings = AtProgramSettingsViewState(data))
    }
  }

  override fun onAtProgramSettingsDismiss() {
    updateState { it.copy(programSettings = null) }
  }

  override fun onAtProgramSettingsSave() {
    updateState { state ->
      val settings = state.programSettings ?: return@updateState state
      val data = settings.data
      val updatedProgram = ActionTriggerScheduleProgram(
        SuplaWeeklyScheduleProgram(
          program = data.program,
          buttonMode = data.selectedMode,
          buttonTemporaryUnlockDurationS = if (data.selectedMode == SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED) {
            data.buttonTemporaryUnlockDurationS
          } else {
            0
          }
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

  private fun emitScheduleChange(state: ActionTriggerScheduleViewState) {
    delayedWeeklyScheduleConfigSubject.emit(state.toWeeklyScheduleConfigChange(remoteId))
  }

  private fun getProgramForChange(program: SuplaScheduleProgram, state: ActionTriggerScheduleViewState): SuplaScheduleProgram? {
    if (state.editorState.activeProgram == program) {
      return null
    }

    val programConfiguration = state.editorState.programs.firstOrNull { it.program == program }
    if (programConfiguration != null && programConfiguration.buttonMode !in ALLOWED_MODES) {
      return null
    }

    return program
  }

  private fun reloadConfig(remoteId: Int) {
    suplaClientProvider.provide()?.getChannelConfig(remoteId, ChannelConfigType.WEEKLY_SCHEDULE)
  }

  private fun onConfigLoaded(data: LoadedData) {
    Timber.i("Action trigger schedule got data: $data")

    if (data.weeklyScheduleResult != ConfigResult.RESULT_TRUE) {
      return
    }

    if (changing) {
      Timber.d("Action trigger schedule update skipped because of changing")
      return
    }

    if (lastInteractionTime?.plus(INTERACTION_PROTECTION_DELAY_MS)?.let { it > dateProvider.currentTimestamp() } == true) {
      Timber.d("Action trigger schedule update skipped because of last interaction time")
      configReloadSubject.onNext(Unit)
      return
    }

    updateState {
      it.copy(
        loadingState = it.loadingState.changingLoading(false, dateProvider),
        editorState = it.editorState.copy(
          programs = data.weeklyScheduleConfig.viewActionTriggerProgramsList(),
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

private val ALLOWED_MODES = listOf(
  SuplaButtonMode.NOT_SET,
  SuplaButtonMode.LOCKED,
  SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED
)

sealed class ActionTriggerScheduleViewEvent : ViewEvent

data class ActionTriggerScheduleViewState(
  val loadingState: LoadingTimeoutManager.LoadingState = LoadingTimeoutManager.LoadingState(),
  val editorState: WeeklyScheduleEditorState<ActionTriggerScheduleProgram> = WeeklyScheduleEditorState(),
  val quarterSelection: QuartersSelectionData? = null,
  val programSettings: AtProgramSettingsViewState? = null
) : ViewState()
