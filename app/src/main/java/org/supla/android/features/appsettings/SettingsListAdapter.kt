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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import org.supla.android.R
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.model.general.NightModeSetting
import org.supla.android.data.source.runtime.appsettings.ChannelHeight
import org.supla.android.data.source.runtime.appsettings.TemperatureUnit
import org.supla.android.databinding.LiSettingsArrowButtonBinding
import org.supla.android.databinding.LiSettingsChannelHeightBinding
import org.supla.android.databinding.LiSettingsEditTextBinding
import org.supla.android.databinding.LiSettingsHeaderBinding
import org.supla.android.databinding.LiSettingsLockScreenBinding
import org.supla.android.databinding.LiSettingsNightModeBinding
import org.supla.android.databinding.LiSettingsPermissionBinding
import org.supla.android.databinding.LiSettingsRollerShutterBinding
import org.supla.android.databinding.LiSettingsSwitchBinding
import org.supla.android.databinding.LiSettingsTemperaturePrecisionBinding
import org.supla.android.databinding.LiSettingsTemperatureUnitBinding
import java.text.DecimalFormatSymbols
import javax.inject.Inject

class SettingsListAdapter @Inject constructor() : RecyclerView.Adapter<SettingItemViewHolder<*>>() {

  private val items: MutableList<SettingItem> = mutableListOf()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingItemViewHolder<*> {
    val inflater = LayoutInflater.from(parent.context)
    return SettingItemViewHolder.inflate(viewType, parent, inflater)
  }

  override fun getItemCount(): Int {
    return items.count()
  }

  override fun onBindViewHolder(holder: SettingItemViewHolder<*>, position: Int) {
    items[position].bind(holder)
  }

  override fun getItemViewType(position: Int) = items[position].viewResource

  fun setItems(items: List<SettingItem>) {
    this.items.clear()
    this.items.addAll(items)
    notifyDataSetChanged()
  }
}

sealed class SettingItem(val viewResource: Int) {

  abstract fun bind(holder: SettingItemViewHolder<*>)

  data class HeaderItem(
    @StringRes val headerResource: Int
  ) : SettingItem(R.layout.li_settings_header) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      (holder.binding as LiSettingsHeaderBinding).apply {
        settingsHeader.setText(headerResource)
      }
    }
  }

  data class ChannelHeightItem(
    val height: ChannelHeight,
    val callback: (Int) -> Unit = { }
  ) : SettingItem(R.layout.li_settings_channel_height) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      (holder.binding as LiSettingsChannelHeightBinding).apply {
        settingsChannelHeight.position = height.position()
        settingsChannelHeight.setOnPositionChangedListener(callback)
      }
    }
  }

  data class TemperatureUnitItem(
    val unit: TemperatureUnit,
    val callback: (Int) -> Unit = { }
  ) : SettingItem(R.layout.li_settings_temperature_unit) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      (holder.binding as LiSettingsTemperatureUnitBinding).apply {
        settingsTemperatureUnit.position = unit.position()
        settingsTemperatureUnit.setOnPositionChangedListener(callback)
      }
    }
  }

  data class TemperaturePrecisionItem(
    val precision: Int,
    val callback: (Int) -> Unit = { }
  ) : SettingItem(R.layout.li_settings_temperature_precision) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      (holder.binding as LiSettingsTemperaturePrecisionBinding).apply {
        settingsTemperaturePrecisionOneDigit.setText("X${DecimalFormatSymbols.getInstance().decimalSeparator}X°")
        settingsTemperaturePrecisionTwoDigits.setText("X${DecimalFormatSymbols.getInstance().decimalSeparator}XX°")

        settingsTemperaturePrecision.position = precision - 1
        settingsTemperaturePrecision.setOnPositionChangedListener(callback)
      }
    }
  }

  data class ButtonAutoHide(
    val active: Boolean,
    val callback: (Boolean) -> Unit = {}
  ) : SettingItem(R.layout.li_settings_switch) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      (holder.binding as LiSettingsSwitchBinding).apply {
        settingsSwitchLabel.setText(R.string.cfg_button_autohide)
        settingsSwitch.isChecked = active
        settingsSwitch.setOnCheckedChangeListener { _, allowed -> callback(allowed) }
      }
    }
  }

  data class InfoButton(
    val visible: Boolean,
    val callback: (Boolean) -> Unit = {}
  ) : SettingItem(R.layout.li_settings_switch) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      (holder.binding as LiSettingsSwitchBinding).apply {
        settingsSwitchLabel.setText(R.string.show_channel_info)
        settingsSwitch.isChecked = visible
        settingsSwitch.setOnCheckedChangeListener { _, allowed -> callback(allowed) }
      }
    }
  }

  data class BottomLabels(
    val visible: Boolean,
    val enabled: Boolean,
    val callback: (Boolean) -> Unit = {}
  ) : SettingItem(R.layout.li_settings_switch) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      (holder.binding as LiSettingsSwitchBinding).apply {
        settingsSwitchLabel.setText(R.string.settings_show_labels)
        val colorRes = if (enabled) R.color.on_background else R.color.outline
        val color = getColor(settingsSwitchLabel.resources, colorRes, null)
        settingsSwitchLabel.setTextColor(color)
        settingsSwitch.isEnabled = enabled
        settingsSwitch.isChecked = visible
        settingsSwitch.setOnCheckedChangeListener { _, allowed -> callback(allowed) }
      }
    }
  }

  data class BottomMenu(
    val visible: Boolean,
    val callback: (Boolean) -> Unit = {}
  ) : SettingItem(R.layout.li_settings_switch) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      (holder.binding as LiSettingsSwitchBinding).apply {
        settingsSwitchLabel.setText(R.string.settings_show_bottom_menu)
        settingsSwitch.isChecked = visible
        settingsSwitch.setOnCheckedChangeListener { _, allowed -> callback(allowed) }
      }
    }
  }

  data class RollerShutterOpenClose(
    val showOpeningPercentage: Boolean,
    val callback: (Boolean) -> Unit = {}
  ) : SettingItem(R.layout.li_settings_roller_shutter) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      (holder.binding as LiSettingsRollerShutterBinding).apply {
        val position = if (showOpeningPercentage) 0 else 1
        settingsRsText.position = position
        settingsRsText.setOnPositionChangedListener { callback(it == 0) }
      }
    }
  }

  data class NightMode(
    val nightModeSetting: NightModeSetting,
    val callback: (NightModeSetting) -> Unit = {}
  ) : SettingItem(R.layout.li_settings_night_mode) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      (holder.binding as LiSettingsNightModeBinding).apply {
        settingsNightMode.position = nightModeSetting.value
        settingsNightMode.setOnPositionChangedListener { callback(NightModeSetting.from(it)) }
      }
    }
  }

  data class LockScreen(
    val lockScreenScope: LockScreenScope,
    val callback: (LockScreenScope) -> Unit = {}
  ) : SettingItem(R.layout.li_settings_lock_screen) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      (holder.binding as LiSettingsLockScreenBinding).apply {
        settingsLockScreen.position = lockScreenScope.value
        settingsLockScreen.setOnPositionChangedListener { callback(LockScreenScope.from(it)) }
      }
    }
  }

  data class BatteryWarningLevel(
    val level: Int,
    val callback: (Int) -> Unit = {}
  ) : SettingItem(R.layout.li_settings_edit_text) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      (holder.binding as LiSettingsEditTextBinding).apply {
        root.label = root.context.getString(R.string.settings_battery_level_warning)
        root.value = "$level"
        root.suffix = "%"
        root.prefix = "<"
        root.onValueChanged = callback
      }
    }
  }

  data class LocalizationOrdering(
    val callback: () -> Unit = {}
  ) : SettingItem(R.layout.li_settings_arrow_button) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      holder.binding.root.setOnClickListener { callback() }
    }
  }

  data class AndroidAuto(
    val callback: () -> Unit = {}
  ) : SettingItem(R.layout.li_settings_arrow_button) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      holder.binding.root.setOnClickListener { callback() }
      (holder.binding as? LiSettingsArrowButtonBinding)?.apply {
        settingsButtonLabel.setText(R.string.settings_android_auto_label)
      }
    }
  }

  data class NotificationsItem(
    val allowed: Boolean,
    val callback: () -> Unit = {}
  ) : SettingItem(R.layout.li_settings_permission) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      (holder.binding as LiSettingsPermissionBinding).apply {
        settingsPermissionLabel.setText(R.string.settings_notifications)
        if (allowed) {
          settingsPermissionsStatus.setText(R.string.notifications_active)
          settingsPermissionsStatus.setTextColor(getColor(settingsPermissionsStatus.resources, R.color.primary, null))
        } else {
          settingsPermissionsStatus.setText(R.string.notifications_inactive)
          settingsPermissionsStatus.setTextColor(getColor(settingsPermissionsStatus.resources, R.color.error, null))
        }
        root.setOnClickListener { callback() }
      }
    }
  }

  data class LocalizationItem(
    val allowed: Boolean,
    val callback: () -> Unit = {}
  ) : SettingItem(R.layout.li_settings_permission) {
    override fun bind(holder: SettingItemViewHolder<*>) {
      (holder.binding as LiSettingsPermissionBinding).apply {
        settingsPermissionLabel.setText(R.string.settings_localization)
        if (allowed) {
          settingsPermissionsStatus.setText(R.string.notifications_active)
          settingsPermissionsStatus.setTextColor(getColor(settingsPermissionsStatus.resources, R.color.primary, null))
        } else {
          settingsPermissionsStatus.setText(R.string.notifications_inactive)
          settingsPermissionsStatus.setTextColor(getColor(settingsPermissionsStatus.resources, R.color.error, null))
        }
        root.setOnClickListener { callback() }
      }
    }
  }
}

data class SettingItemViewHolder<T : ViewBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root) {
  companion object {
    fun inflate(viewId: Int, parent: ViewGroup, inflater: LayoutInflater): SettingItemViewHolder<*> {
      return when (viewId) {
        R.layout.li_settings_channel_height -> {
          SettingItemViewHolder(LiSettingsChannelHeightBinding.inflate(inflater, parent, false))
        }

        R.layout.li_settings_temperature_unit -> {
          SettingItemViewHolder(LiSettingsTemperatureUnitBinding.inflate(inflater, parent, false))
        }

        R.layout.li_settings_temperature_precision -> {
          SettingItemViewHolder(LiSettingsTemperaturePrecisionBinding.inflate(inflater, parent, false))
        }

        R.layout.li_settings_roller_shutter -> {
          SettingItemViewHolder(LiSettingsRollerShutterBinding.inflate(inflater, parent, false))
        }

        R.layout.li_settings_switch -> {
          SettingItemViewHolder(LiSettingsSwitchBinding.inflate(inflater, parent, false))
        }

        R.layout.li_settings_arrow_button -> {
          SettingItemViewHolder(LiSettingsArrowButtonBinding.inflate(inflater, parent, false))
        }

        R.layout.li_settings_permission -> {
          SettingItemViewHolder(LiSettingsPermissionBinding.inflate(inflater, parent, false))
        }

        R.layout.li_settings_header -> {
          SettingItemViewHolder(LiSettingsHeaderBinding.inflate(inflater, parent, false))
        }

        R.layout.li_settings_night_mode -> {
          SettingItemViewHolder(LiSettingsNightModeBinding.inflate(inflater, parent, false))
        }

        R.layout.li_settings_lock_screen -> {
          SettingItemViewHolder(LiSettingsLockScreenBinding.inflate(inflater, parent, false))
        }

        R.layout.li_settings_edit_text -> {
          SettingItemViewHolder(LiSettingsEditTextBinding.inflate(inflater, parent, false))
        }

        else -> throw IllegalArgumentException("Unsupported view type $viewId")
      }
    }
  }
}
