package org.supla.android.data.source.remote

import java.util.EnumSet

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

enum class FieldType(val value: Int) {
  STATUS_LED(1 shl 0),
  SCREEN_BRIGHTNESS(1 shl 1),
  BUTTON_VOLUME(1 shl 2),
  DISABLE_USER_INTERFACE(1 shl 3),
  AUTOMATIC_TIME_SYNC(1 shl 4),
  HOME_SCREEN_OFF_DELAY(1 shl 5),
  HOME_SCREEN_CONTENT(1 shl 7)
}

sealed class Field(open val type: FieldType)

enum class StatusLedType(val value: Int) {
  ON_WHEN_CONNECTED(0),
  OFF_WHEN_CONNECTED(1),
  ALWAYS_OFF(2)
}

data class StatusLedField(
  override val type: FieldType,
  val statusLedType: StatusLedType
) : Field(type)

data class ScreenBrightnessField(
  override val type: FieldType,
  val automatic: Boolean,
  val brightnessLevel: Byte,
  val adjustmentForAutomatic: Byte
) : Field(type)

data class ButtonVolumeField(
  override val type: FieldType,
  val volume: Byte
) : Field(type)

enum class UIDisabled(val value: Int) {
  NO(0),
  YES(1),
  PARTIAL(2)
}

data class DisableUserInterfaceField(
  override val type: FieldType,
  val uiDisabled: UIDisabled,
  val minAllowedTemperatureSetpointFromLocalUI: Short,
  val maxAllowedTemperatureSetpointFromLocalUI: Short
) : Field(type)

data class AutomaticTimeSyncField(
  override val type: FieldType,
  val enabled: Boolean
) : Field(type)

data class HomeScreenOffDelayField(
  override val type: FieldType,
  val enabled: Boolean,
  val seconds: Int
) : Field(type)

enum class HomeScreenContent(val value: Int) {
  NONE(1 shl 0),
  TEMPERATURE(1 shl 0),
  TEMPERATURE_AND_HUMIDITY(1 shl 1),
  TIME(1 shl 2),
  TIME_DATE(1 shl 3),
  TEMPERATURE_TIME(1 shl 4),
  MAIN_AND_AUX_TEMPERATURE(1 shl 5)
}

data class HomeScreenContentField(
  override val type: FieldType,
  val available: EnumSet<HomeScreenContent>,
  val content: HomeScreenContent
) : Field(type)

data class SuplaDeviceConfig(val deviceId: Int, val availableFields: EnumSet<FieldType>, val fields: List<Field>)
