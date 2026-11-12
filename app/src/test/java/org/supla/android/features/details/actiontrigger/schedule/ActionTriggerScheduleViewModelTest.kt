package org.supla.android.features.details.actiontrigger.schedule

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.remote.hvac.SuplaButtonMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.DeviceConfigEventsManager
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.features.details.actiontrigger.schedule.data.ActionTriggerScheduleProgram
import org.supla.android.features.details.actiontrigger.schedule.data.AtProgramDurationOption
import org.supla.android.features.details.actiontrigger.schedule.data.AtProgramSettingsDialogState
import org.supla.android.features.details.actiontrigger.schedule.data.MAX_TEMPORARY_UNLOCK_DURATION_S
import org.supla.android.features.details.schedule.DelayedWeeklyScheduleConfigSubject
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleEditorState
import org.supla.core.shared.infrastructure.LocalizedString

class ActionTriggerScheduleViewModelTest :
  BaseViewModelTest<ActionTriggerScheduleViewState, ActionTriggerScheduleViewEvent, ActionTriggerScheduleViewModel>() {

  @RelaxedMockK
  private lateinit var delayedWeeklyScheduleConfigSubject: DelayedWeeklyScheduleConfigSubject

  @RelaxedMockK
  private lateinit var channelConfigEventsManager: ChannelConfigEventsManager

  @RelaxedMockK
  private lateinit var deviceConfigEventsManager: DeviceConfigEventsManager

  @RelaxedMockK
  private lateinit var loadingTimeoutManager: LoadingTimeoutManager

  @RelaxedMockK
  private lateinit var suplaClientProvider: SuplaClientProvider

  @RelaxedMockK
  private lateinit var dateProvider: DateProvider

  @RelaxedMockK
  override lateinit var threading: SuplaThreading

  @InjectMockKs
  override lateinit var viewModel: ActionTriggerScheduleViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `should change selected mode in program settings and clear duration when temporary unlock is disabled`() {
    val program = SuplaScheduleProgram.PROGRAM_1
    setSettingsState(program, SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED, 600)

    viewModel.onAtProgramSettingsModeChange(SuplaButtonMode.LOCKED)

    assertThat(viewModel.getViewState().value.programSettings)
      .isEqualTo(
        AtProgramSettingsDialogState(
          program = program,
          selectedMode = SuplaButtonMode.LOCKED,
          durationOptions = AtProgramDurationOption.entries,
          activeDuration = AtProgramDurationOption.CUSTOM,
          currentTemporaryUnlockDurationS = null
        )
      )
  }

  @Test
  fun `should clamp temporary unlock duration to the supported maximum`() {
    val program = SuplaScheduleProgram.PROGRAM_1
    setSettingsState(program, SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED, 300)

    viewModel.onAtProgramSettingsDurationChange(Int.MAX_VALUE)

    assertThat(viewModel.getViewState().value.programSettings?.currentTemporaryUnlockDurationS)
      .isEqualTo(MAX_TEMPORARY_UNLOCK_DURATION_S)
  }

  @Test
  fun `should dismiss program settings`() {
    setSettingsState(SuplaScheduleProgram.PROGRAM_1, SuplaButtonMode.LOCKED, null)

    viewModel.onAtProgramSettingsDismiss()

    assertThat(viewModel.getViewState().value.programSettings).isNull()
  }

  private fun setSettingsState(
    program: SuplaScheduleProgram,
    mode: SuplaButtonMode,
    durationS: Int?
  ) {
    val programData = ActionTriggerScheduleProgram(
      program = program,
      buttonMode = mode,
      buttonTemporaryUnlockDurationS = durationS,
      label = LocalizedString.Constant("Program")
    )
    viewModel.setState(
      ActionTriggerScheduleViewState(
        editorState = WeeklyScheduleEditorState(programs = listOf(programData)),
        programSettings = AtProgramSettingsDialogState(
          program = program,
          selectedMode = mode,
          durationOptions = AtProgramDurationOption.entries,
          activeDuration = AtProgramDurationOption.CUSTOM,
          currentTemporaryUnlockDurationS = durationS
        )
      )
    )
  }
}
