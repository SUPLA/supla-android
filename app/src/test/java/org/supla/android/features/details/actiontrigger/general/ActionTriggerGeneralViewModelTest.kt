package org.supla.android.features.details.actiontrigger.general
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

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.remote.hvac.SuplaButtonMode
import org.supla.android.tools.SuplaThreading
import org.supla.core.shared.data.model.channel.SuplaChannelFlag
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

class ActionTriggerGeneralViewModelTest {

  private val viewModel = ActionTriggerGeneralViewModel(
    observeChannelWithChildrenUseCase = mockk(relaxed = true),
    executeActionTriggerActionUseCase = mockk(relaxed = true),
    channelConfigEventsManager = mockk(relaxed = true),
    executeSimpleActionUseCase = mockk(relaxed = true),
    deviceConfigEventsManager = mockk(relaxed = true),
    suplaClientProvider = mockk(relaxed = true),
    dateProvider = mockk<DateProvider>(relaxed = true),
    threading = mockk<SuplaThreading>(relaxed = true)
  )

  @Test
  fun `should show normal state without icon in manual mode`() {
    // when
    val stateData = viewModel.currentStateData(weeklyActive = false)

    // then
    assertThat(stateData).isNotNull
    assertThat(stateData!!.icon).isNull()
    assertThat(stateData.label).isEqualTo(LocalizedString.WithResourceAndArguments(R.string.details_timer_state_label))
    assertThat(stateData.value).isEqualTo(LocalizedString.WithResourceAndArguments(R.string.general_unlocked))
  }

  @Test
  fun `should not show current state outside manual mode`() {
    assertThat(viewModel.currentStateData(weeklyActive = true)).isNull()
  }

  @Test
  fun `should show offline state when device is offline even with weekly schedule active`() {
    val stateData = viewModel.currentStateData(weeklyActive = true, offline = true)

    assertThat(stateData).isNotNull
    assertThat(stateData!!.label).isEqualTo(localizedString(R.string.details_timer_state_label))
    assertThat(stateData.value).isEqualTo(localizedString(R.string.offline))
  }

  @Test
  fun `should expose manual and supported weekly modes without auto`() {
    // given
    val channelFlags = SuplaChannelFlag.WEEKLY_SCHEDULE.rawValue

    // when
    val mode = viewModel.operatingMode(channelFlags, weeklyActive = true)!!

    // then
    assertThat(mode.manualAllowed).isTrue()
    assertThat(mode.manualActive).isFalse()
    assertThat(mode.weeklyAllowed).isTrue()
    assertThat(mode.weeklyActive).isTrue()
    assertThat(mode.autoAllowed).isFalse()
  }

  @Test
  fun `should hide operating mode when weekly schedule is unsupported`() {
    // when
    val mode = viewModel.operatingMode(channelFlags = 0, weeklyActive = false)

    // then
    assertThat(mode).isNull()
  }

  @Test
  fun `should mark lock as active for locked mode`() {
    val buttonState = viewModel.lockButtonState(SuplaButtonMode.LOCKED, temporaryUnlockActive = false)

    assertThat(buttonState.lockActive).isTrue()
    assertThat(buttonState.unlockActive).isFalse()
    assertThat(buttonState.showTemporaryUnlockIcon).isFalse()
  }

  @Test
  fun `should mark lock active and use temporary unlock icon when temporary unlock is allowed`() {
    val buttonState = viewModel.lockButtonState(
      SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED,
      temporaryUnlockActive = false
    )

    assertThat(buttonState.lockActive).isTrue()
    assertThat(buttonState.unlockActive).isFalse()
    assertThat(buttonState.showTemporaryUnlockIcon).isTrue()
  }

  @Test
  fun `should mark unlock as active for other modes`() {
    val buttonState = viewModel.lockButtonState(SuplaButtonMode.NOT_SET, temporaryUnlockActive = false)

    assertThat(buttonState.lockActive).isFalse()
    assertThat(buttonState.unlockActive).isTrue()
    assertThat(buttonState.showTemporaryUnlockIcon).isFalse()
  }

  @Test
  fun `should mark unlock as active while temporary unlock is active`() {
    val buttonState = viewModel.lockButtonState(
      SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED,
      temporaryUnlockActive = true
    )

    assertThat(buttonState.lockActive).isFalse()
    assertThat(buttonState.unlockActive).isTrue()
    assertThat(buttonState.showTemporaryUnlockIcon).isTrue()
  }

  @Test
  fun `should disable lock and unlock buttons when device is offline`() {
    val buttonState = viewModel.lockButtonState(
      SuplaButtonMode.NOT_SET,
      temporaryUnlockActive = false,
      offline = true
    )

    assertThat(buttonState.lockDisabled).isTrue()
    assertThat(buttonState.unlockDisabled).isTrue()
  }

  @Test
  fun `should open unlock dialog with current schedule duration`() {
    viewModel.setPrivateField("mode", SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED)
    viewModel.setPrivateField("currentTemporaryUnlockDurationS", 300)

    viewModel.onUnlock()

    assertThat(viewModel.getViewState().value.unlockDialog)
      .isEqualTo(UnlockDialogState(temporaryUnlockDurationS = 300, unlockTemporaryAllowed = true))
  }

  @Test
  fun `should disable temporary unlock when current schedule duration is zero`() {
    viewModel.setPrivateField("mode", SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED)
    viewModel.setPrivateField("currentTemporaryUnlockDurationS", 0)

    viewModel.onUnlock()

    assertThat(viewModel.getViewState().value.unlockDialog)
      .isEqualTo(UnlockDialogState(temporaryUnlockDurationS = 0, unlockTemporaryAllowed = false))
  }

  @Test
  fun `should clamp changed duration and enable temporary unlock without schedule duration`() {
    viewModel.setPrivateField("mode", SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED)
    viewModel.onUnlock()

    viewModel.onUnlockDialogDurationChange(70_000)

    assertThat(viewModel.getViewState().value.unlockDialog)
      .isEqualTo(UnlockDialogState(temporaryUnlockDurationS = 64_800, unlockTemporaryAllowed = true))
  }

  private fun ActionTriggerGeneralViewModel.setPrivateField(name: String, value: Any?) {
    javaClass.getDeclaredField(name).apply {
      isAccessible = true
      set(this@setPrivateField, value)
    }
  }
}
