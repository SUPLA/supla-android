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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.settings.ChannelHeightOption
import org.supla.android.ui.views.settings.SettingChannelHeightItem
import org.supla.android.ui.views.settings.SettingCheckboxItem
import org.supla.android.ui.views.settings.SettingNavigationItem
import org.supla.android.ui.views.settings.SettingSegmentedItem
import org.supla.android.ui.views.settings.SettingTextFieldItem
import org.supla.android.ui.views.settings.SettingsBlock
import org.supla.android.ui.views.settings.SettingsHeader
import org.supla.core.shared.data.model.export.NightModeSetting
import java.text.DecimalFormatSymbols

@Composable
fun SettingsViewScope.View(state: SettingsViewState) {
  Column(
    verticalArrangement = Arrangement.spacedBy(1.dp),
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(vertical = Distance.default)
  ) {
    SettingsHeader(stringResource(R.string.settings_appearance))
    SettingsBlock {
      item {
        SettingChannelHeightItem(
          label = stringResource(R.string.cfg_channel_height),
          options = listOf(
            ChannelHeightOption(stringResource(R.string.settings_channel_height_compact), R.drawable.channel_height_small),
            ChannelHeightOption(stringResource(R.string.settings_channel_height_standard), R.drawable.channel_height_normal),
            ChannelHeightOption(stringResource(R.string.settings_channel_height_large), R.drawable.channel_height_big)
          ),
          selectedIndex = state.channelHeight.position(),
          onOptionSelected = ::onChannelHeightChanged
        )
      }
      item {
        SettingSegmentedItem(
          label = stringResource(R.string.cfg_temperature_unit),
          options = listOf("°C", "°F"),
          selectedIndex = state.temperatureUnit.position(),
          onOptionSelected = ::onTemperatureUnitChanged
        )
      }
      item {
        val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
        SettingSegmentedItem(
          label = stringResource(R.string.cfg_temperature_precision),
          options = listOf("0${decimalSeparator}0°", "0${decimalSeparator}00°"),
          selectedIndex = state.temperaturePrecision - 1,
          onOptionSelected = ::onTemperaturePrecisionChanged
        )
      }
      item {
        SettingSegmentedItem(
          label = stringResource(R.string.rs_display_mode),
          options = listOf(
            stringResource(R.string.rs_display_mode_opening),
            stringResource(R.string.rs_display_mode_closing)
          ),
          selectedIndex = if (state.showOpeningPercentage) 0 else 1,
          onOptionSelected = ::onOpeningPercentageChanged
        )
      }
      item {
        SettingSegmentedItem(
          label = stringResource(R.string.cfg_night_mode),
          options = listOf(stringResource(R.string.yes), stringResource(R.string.no), stringResource(R.string.auto)),
          selectedIndex = when (state.nightMode) {
            NightModeSetting.ALWAYS -> 0
            NightModeSetting.AUTO -> 2
            NightModeSetting.NEVER, NightModeSetting.UNSET -> 1
          },
          onOptionSelected = ::onNightModeChanged
        )
      }
      item {
        SettingTextFieldItem(
          label = stringResource(R.string.settings_battery_level_warning),
          value = state.batteryWarningLevel,
          prefix = "<",
          unit = "%",
          onValueChanged = ::onBatteryWarningLevelChanged
        )
      }
      item {
        SettingNavigationItem(
          label = stringResource(R.string.location_ordering),
          onClick = ::onLocationOrderClicked
        )
      }
    }

    SettingsHeader(stringResource(R.string.settings_channel_list), modifier = Modifier.padding(top = Distance.default))
    SettingsBlock {
      item {
        SettingCheckboxItem(
          label = stringResource(R.string.cfg_button_autohide),
          checked = state.buttonAutoHide,
          onCheckedChanged = ::onButtonAutoHideChanged
        )
      }
      item {
        SettingCheckboxItem(
          label = stringResource(R.string.show_channel_info),
          checked = state.showChannelInfo,
          onCheckedChanged = ::onChannelInfoChanged
        )
      }
      item {
        SettingCheckboxItem(
          label = stringResource(R.string.settings_show_bottom_menu),
          checked = state.showBottomMenu,
          onCheckedChanged = ::onBottomMenuChanged
        )
      }
      item {
        SettingCheckboxItem(
          label = stringResource(R.string.settings_show_labels),
          checked = state.showBottomLabels,
          onCheckedChanged = ::onBottomLabelsChanged
        )
      }
      if (state.androidAutoSupported) {
        item {
          SettingCheckboxItem(
            label = stringResource(R.string.settings_show_unavailable_channels),
            checked = state.hideUnavailableChannels,
            onCheckedChanged = ::onUnavailableChannelsChanged
          )
        }
      }
    }

    SettingsHeader(stringResource(R.string.settings_security), modifier = Modifier.padding(top = Distance.default))
    SettingsBlock {
      item {
        SettingSegmentedItem(
          label = stringResource(R.string.cfg_lock_screen),
          options = listOf(
            stringResource(R.string.cfg_lock_screen_none),
            stringResource(R.string.cfg_lock_screen_app),
            stringResource(R.string.cfg_lock_screen_accounts)
          ),
          selectedIndex = state.lockScreenScope.value,
          onOptionSelected = ::onLockScreenChanged
        )
      }
    }

    SettingsHeader(stringResource(R.string.settings_integrations), modifier = Modifier.padding(top = Distance.default))
    SettingsBlock {
      if (state.androidAutoSupported) {
        item {
          SettingNavigationItem(
            label = stringResource(R.string.settings_android_auto_label),
            onClick = ::onAndroidAutoClicked
          )
        }
      }
      item {
        SettingNavigationItem(
          label = stringResource(R.string.settings_nfc_label),
          onClick = ::onNfcClicked
        )
      }
    }

    SettingsHeader(stringResource(R.string.settings_permissions), modifier = Modifier.padding(top = Distance.default))
    SettingsBlock {
      item {
        SettingNavigationItem(
          label = stringResource(R.string.settings_notifications),
          value = stringResource(if (state.notificationsAllowed) R.string.notifications_active else R.string.notifications_inactive),
          valueColor = if (state.notificationsAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
          onClick = ::onPermissionClicked
        )
      }
      item {
        SettingNavigationItem(
          label = stringResource(R.string.settings_localization),
          value = stringResource(if (state.locationAllowed) R.string.notifications_active else R.string.notifications_inactive),
          valueColor = if (state.locationAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
          onClick = ::onPermissionClicked
        )
      }
    }
  }
}

@Composable
@org.supla.android.tools.SuplaPreview
private fun SettingsPreview() {
  SuplaTheme {
    previewScope.View(SettingsViewState(androidAutoSupported = true))
  }
}

private val previewScope = object : SettingsViewScope {
  override fun onChannelHeightChanged(position: Int) {}
  override fun onTemperatureUnitChanged(position: Int) {}
  override fun onTemperaturePrecisionChanged(position: Int) {}
  override fun onButtonAutoHideChanged(value: Boolean) {}
  override fun onChannelInfoChanged(value: Boolean) {}
  override fun onBottomMenuChanged(value: Boolean) {}
  override fun onBottomLabelsChanged(value: Boolean) {}
  override fun onUnavailableChannelsChanged(value: Boolean) {}
  override fun onOpeningPercentageChanged(position: Int) {}
  override fun onNightModeChanged(position: Int) {}
  override fun onLockScreenChanged(position: Int) {}
  override fun onBatteryWarningLevelChanged(value: String) {}
  override fun onLocationOrderClicked() {}
  override fun onAndroidAutoClicked() {}
  override fun onNfcClicked() {}
  override fun onPermissionClicked() {}
}
