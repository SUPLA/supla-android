package org.supla.core.shared.usecase.channel.valueformatter.types
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

enum class ValueUnit(val text: String, val leadingSpace: Boolean) {
  EMPTY("", false),
  VOLTAGE("V", true),
  TEMPERATURE_CELSIUS("°C", true),
  TEMPERATURE_FAHRENHEIT("°F", true),
  TEMPERATURE_DEGREE("°", false),
  HUMIDITY("%", false),
  ELECTRICITY_METER("kWh", true),
  POWER_ACTIVE("W", true),
  CURRENT("A", true),
  PRESSURE("hPa", true),
  RAIN("mm", true),
  WIND("m/s", true),
  WEIGHT_DEFAULT("g", true),
  WEIGHT_KILO("kg", true),
  DISTANCE_MILLI("mm", true),
  DISTANCE_CENTI("cm", true),
  DISTANCE_KILO("km", true),
  DISTANCE_DEFAULT("m", true);

  override fun toString(): String =
    if (leadingSpace) " $text" else text
}
