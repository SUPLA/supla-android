package org.supla.android.features.appsettings
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

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.NotificationManager
import android.app.UiModeManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.permissions.PermissionsHelper
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.model.general.LockScreenSettings
import org.supla.android.data.source.runtime.appsettings.ChannelHeight
import org.supla.android.features.lockscreen.UnlockAction
import org.supla.android.tools.SuplaThreading
import org.supla.core.shared.data.model.export.NightModeSetting
import org.supla.core.shared.data.model.thermometer.TemperatureUnit

class SettingsViewModelTest : BaseViewModelTest<SettingsViewState, SettingsViewEvent, SettingsViewModel>(MockSchedulers.MOCKK) {

  @MockK private lateinit var applicationPreferences: ApplicationPreferences

  @MockK private lateinit var notificationManager: NotificationManager

  @MockK private lateinit var permissionsHelper: PermissionsHelper

  @MockK private lateinit var modeManager: UiModeManager

  @MockK private lateinit var encryptedPreferences: EncryptedPreferences

  @MockK override lateinit var threading: SuplaThreading

  override val viewModel: SettingsViewModel by lazy {
    SettingsViewModel(
      applicationPreferences,
      notificationManager,
      permissionsHelper,
      modeManager,
      encryptedPreferences,
      threading
    )
  }

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `loads settings into view state`() {
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true

    viewModel.loadSettings()

    assertThat(states).hasSize(1)
    val state = states.single()
    assertThat(state.channelHeight).isEqualTo(ChannelHeight.HEIGHT_100)
    assertThat(state.temperatureUnit).isEqualTo(TemperatureUnit.FAHRENHEIT)
    assertThat(state.temperaturePrecision).isEqualTo(2)
    assertThat(state.buttonAutoHide).isTrue()
    assertThat(state.showChannelInfo).isFalse()
    assertThat(state.showBottomMenu).isFalse()
    assertThat(state.batteryWarningLevel).isEqualTo("10")
    assertThat(state.lockScreenScope).isEqualTo(LockScreenScope.NONE)
    assertThat(state.locationAllowed).isTrue()
  }

  @Test
  fun `updates channel height preference and state`() {
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true
    every { applicationPreferences.channelHeight = 150 } answers {}

    viewModel.loadSettings()
    viewModel.onChannelHeightChanged(2)

    assertThat(states.last().channelHeight).isEqualTo(ChannelHeight.HEIGHT_150)
    verify { applicationPreferences.channelHeight = 150 }
  }

  @Test
  fun `battery warning input is filtered and limited in the view model`() {
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true
    every { applicationPreferences.batteryWarningLevel = 12 } answers {}

    viewModel.loadSettings()
    viewModel.onBatteryWarningLevelChanged("1a234")

    assertThat(states.last().batteryWarningLevel).isEqualTo("12")
    verify { applicationPreferences.batteryWarningLevel = 12 }
  }

  @Test
  fun `lock screen none selection requests pin verification`() {
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true

    viewModel.loadSettings()
    viewModel.onLockScreenChanged(LockScreenScope.NONE.value)

    assertThat(events).containsExactly(SettingsViewEvent.NavigateToPinVerification(UnlockAction.TurnOffPin))
  }

  @Test
  fun `location order selection emits navigation event`() {
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true

    viewModel.loadSettings()
    viewModel.onLocationOrderClicked()

    assertThat(events).containsExactly(SettingsViewEvent.NavigateToLocalizationsOrdering)
  }

  @Test
  fun `permission selection emits system settings event`() {
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true

    viewModel.loadSettings()
    viewModel.onPermissionClicked()

    assertThat(events).containsExactly(SettingsViewEvent.NavigateToSettings)
  }

  private fun mockPreferences() {
    every { applicationPreferences.channelHeight } returns 100
    every { applicationPreferences.temperatureUnit } returns TemperatureUnit.FAHRENHEIT
    every { applicationPreferences.temperaturePrecision } returns 2
    every { applicationPreferences.isShowBottomMenu } returns false
    every { applicationPreferences.isButtonAutohide } returns true
    every { applicationPreferences.isShowChannelInfo } returns false
    every { applicationPreferences.isShowOpeningPercent } returns true
    every { applicationPreferences.isShowBottomLabel } returns false
    every { applicationPreferences.hideUnavailableChannels } returns false
    every { applicationPreferences.batteryWarningLevel } returns 10
    every { applicationPreferences.nightMode } returns NightModeSetting.NEVER
    every { notificationManager.areNotificationsEnabled() } returns true
    every { encryptedPreferences.lockScreenSettings } returns LockScreenSettings.DEFAULT
  }
}
