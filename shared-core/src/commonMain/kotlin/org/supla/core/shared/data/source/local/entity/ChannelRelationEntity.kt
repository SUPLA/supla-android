package org.supla.core.shared.data.source.local.entity
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

interface ChannelRelationEntity {
  val channelId: Int
  val parentId: Int
  val relationType: ChannelRelationType
  val profileId: Long
  val deleteFlag: Boolean
}

enum class ChannelRelationType(val value: Short) {
  UNKNOWN(-1),
  DEFAULT(0),
  OPENING_SENSOR(1),
  PARTIAL_OPENING_SENSOR(2),
  METER(3),
  MAIN_THERMOMETER(4),
  AUX_THERMOMETER_FLOOR(5),
  AUX_THERMOMETER_WATER(6),
  AUX_THERMOMETER_GENERIC_HEATER(7),
  AUX_THERMOMETER_GENERIC_COOLER(8),
  MASTER_THERMOSTAT(20),
  HEAT_OR_COLD_SOURCE_SWITCH(21),
  PUMP_SWITCH(22);

  fun isThermometer(): Boolean {
    return isMainThermometer() || isAuxThermometer()
  }

  fun isAuxThermometer(): Boolean {
    return when (this) {
      AUX_THERMOMETER_FLOOR,
      AUX_THERMOMETER_WATER,
      AUX_THERMOMETER_GENERIC_HEATER,
      AUX_THERMOMETER_GENERIC_COOLER -> true
      else -> false
    }
  }

  fun isMainThermometer(): Boolean {
    return when (this) {
      MAIN_THERMOMETER -> true
      else -> false
    }
  }

  companion object {
    fun from(value: Short): ChannelRelationType {
      for (type in entries) {
        if (type.value == value) {
          return type
        }
      }

      return UNKNOWN
    }
  }
}
