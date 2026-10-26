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
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.R
import org.supla.android.core.branding.Configuration
import org.supla.android.core.permissions.PermissionsHelper
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.source.runtime.appsettings.ChannelHeight
import org.supla.android.features.lockscreen.UnlockAction
import org.supla.android.model.general.appCompatDelegateValue
import org.supla.android.model.general.modeManagerValue
import org.supla.android.tools.SuplaThreading
import org.supla.core.shared.data.model.export.NightModeSetting
import org.supla.core.shared.data.model.thermometer.TemperatureUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val preferences: ApplicationPreferences,
  private val notificationManager: NotificationManager,
  private val permissionsHelper: PermissionsHelper,
  private val modeManager: UiModeManager,
  private val encryptedPreferences: EncryptedPreferences,
  threading: SuplaThreading
) : BaseViewModel<SettingsViewState, SettingsViewEvent>(
  defaultState = SettingsViewState(),
  threading = threading,
  titleRes = R.string.settings
),
  SettingsViewScope {

  fun loadSettings() {
    updateState {
      SettingsViewState(
        channelHeight = getChannelHeight(),
        temperatureUnit = preferences.temperatureUnit,
        temperaturePrecision = preferences.temperaturePrecision,
        buttonAutoHide = preferences.isButtonAutohide,
        showChannelInfo = preferences.isShowChannelInfo,
        showBottomMenu = preferences.isShowBottomMenu,
        showBottomLabels = preferences.isShowBottomLabel,
        hideUnavailableChannels = preferences.hideUnavailableChannels,
        showOpeningPercentage = preferences.isShowOpeningPercent,
        nightMode = preferences.nightMode,
        lockScreenScope = encryptedPreferences.lockScreenSettings.scope,
        batteryWarningLevel = preferences.batteryWarningLevel.toString(),
        androidAutoSupported = Configuration.ANDROID_AUTO_SUPPORT,
        notificationsAllowed = areNotificationsEnabled(),
        locationAllowed = isLocationPermissionGranted()
      )
    }
  }

  override fun onChannelHeightChanged(position: Int) {
    val height = ChannelHeight.forPosition(position)
    preferences.channelHeight = height.percent
    updateState { it.copy(channelHeight = height) }
  }

  override fun onTemperatureUnitChanged(position: Int) {
    val unit = TemperatureUnit.forPosition(position)
    preferences.temperatureUnit = unit
    updateState { it.copy(temperatureUnit = unit) }
  }

  override fun onTemperaturePrecisionChanged(position: Int) {
    val precision = position + 1
    preferences.temperaturePrecision = precision
    updateState { it.copy(temperaturePrecision = precision) }
  }

  override fun onButtonAutoHideChanged(value: Boolean) {
    preferences.isButtonAutohide = value
    updateState { it.copy(buttonAutoHide = value) }
  }

  override fun onChannelInfoChanged(value: Boolean) {
    preferences.isShowChannelInfo = value
    updateState { it.copy(showChannelInfo = value) }
  }

  override fun onBottomMenuChanged(value: Boolean) {
    preferences.isShowBottomMenu = value
    updateState { it.copy(showBottomMenu = value) }
  }

  override fun onBottomLabelsChanged(value: Boolean) {
    preferences.isShowBottomLabel = value
    updateState { it.copy(showBottomLabels = value) }
  }

  override fun onUnavailableChannelsChanged(value: Boolean) {
    preferences.hideUnavailableChannels = value
    updateState { it.copy(hideUnavailableChannels = value) }
  }

  override fun onOpeningPercentageChanged(position: Int) {
    val value = position == 0
    preferences.isShowOpeningPercent = value
    updateState { it.copy(showOpeningPercentage = value) }
  }

  override fun onNightModeChanged(position: Int) {
    val setting = when (position) {
      0 -> NightModeSetting.ALWAYS
      1 -> NightModeSetting.NEVER
      else -> NightModeSetting.AUTO
    }
    preferences.nightMode = setting
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      modeManager.setApplicationNightMode(setting.modeManagerValue())
    } else {
      AppCompatDelegate.setDefaultNightMode(setting.appCompatDelegateValue())
    }
    updateState { it.copy(nightMode = setting) }
  }

  override fun onLockScreenChanged(position: Int) {
    val setting = LockScreenScope.from(position)
    val pinSum = encryptedPreferences.lockScreenSettings.pinSum

    if (setting == LockScreenScope.NONE) {
      sendEvent(SettingsViewEvent.NavigateToPinVerification(UnlockAction.TurnOffPin))
    } else if (pinSum != null && setting == LockScreenScope.ACCOUNTS) {
      sendEvent(SettingsViewEvent.NavigateToPinVerification(UnlockAction.ConfirmAuthorizeAccounts))
    } else if (pinSum != null && setting == LockScreenScope.APPLICATION) {
      sendEvent(SettingsViewEvent.NavigateToPinVerification(UnlockAction.ConfirmAuthorizeApplication))
    } else {
      sendEvent(SettingsViewEvent.NavigateToPinSetup(setting))
    }
  }

  override fun onBatteryWarningLevelChanged(value: String) {
    val acceptedValue = value.filter(Char::isDigit).take(2)
    updateState { it.copy(batteryWarningLevel = acceptedValue) }
    acceptedValue.toIntOrNull()?.let { preferences.batteryWarningLevel = it }
  }

  override fun onLocationOrderClicked() {
    sendEvent(SettingsViewEvent.NavigateToLocalizationsOrdering)
  }

  override fun onAndroidAutoClicked() {
    sendEvent(SettingsViewEvent.NavigateToAndroidAuto)
  }

  override fun onNfcClicked() {
    sendEvent(SettingsViewEvent.NavigateToNfc)
  }

  override fun onPermissionClicked() {
    sendEvent(SettingsViewEvent.NavigateToSettings)
  }

  private fun getChannelHeight() = ChannelHeight.entries
    .firstOrNull { it.percent == preferences.channelHeight } ?: ChannelHeight.HEIGHT_100

  private fun areNotificationsEnabled() = if (VERSION.SDK_INT > VERSION_CODES.N) {
    notificationManager.areNotificationsEnabled()
  } else {
    true
  }

  private fun isLocationPermissionGranted() =
    permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION)
}

interface SettingsViewScope {
  fun onChannelHeightChanged(position: Int)
  fun onTemperatureUnitChanged(position: Int)
  fun onTemperaturePrecisionChanged(position: Int)
  fun onButtonAutoHideChanged(value: Boolean)
  fun onChannelInfoChanged(value: Boolean)
  fun onBottomMenuChanged(value: Boolean)
  fun onBottomLabelsChanged(value: Boolean)
  fun onUnavailableChannelsChanged(value: Boolean)
  fun onOpeningPercentageChanged(position: Int)
  fun onNightModeChanged(position: Int)
  fun onLockScreenChanged(position: Int)
  fun onBatteryWarningLevelChanged(value: String)
  fun onLocationOrderClicked()
  fun onAndroidAutoClicked()
  fun onNfcClicked()
  fun onPermissionClicked()
}

sealed class SettingsViewEvent : ViewEvent {
  data object NavigateToLocalizationsOrdering : SettingsViewEvent()
  data object NavigateToAndroidAuto : SettingsViewEvent()
  data object NavigateToNfc : SettingsViewEvent()
  data object NavigateToSettings : SettingsViewEvent()
  data class NavigateToPinVerification(val verificationAction: UnlockAction) : SettingsViewEvent()
  data class NavigateToPinSetup(val lockScreenScope: LockScreenScope) : SettingsViewEvent()
}

data class SettingsViewState(
  val channelHeight: ChannelHeight = ChannelHeight.HEIGHT_100,
  val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
  val temperaturePrecision: Int = 1,
  val buttonAutoHide: Boolean = false,
  val showChannelInfo: Boolean = false,
  val showBottomMenu: Boolean = false,
  val showBottomLabels: Boolean = false,
  val hideUnavailableChannels: Boolean = false,
  val showOpeningPercentage: Boolean = true,
  val nightMode: NightModeSetting = NightModeSetting.NEVER,
  val lockScreenScope: LockScreenScope = LockScreenScope.NONE,
  val batteryWarningLevel: String = "10",
  val androidAutoSupported: Boolean = false,
  val notificationsAllowed: Boolean = true,
  val locationAllowed: Boolean = false
) : ViewState()
