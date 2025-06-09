package org.supla.core.shared.data.model.general
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

import kotlinx.serialization.Serializable

@Serializable
enum class SuplaFunction(val value: Int) {
  UNKNOWN(-1),
  NONE(0),
  CONTROLLING_THE_GATEWAY_LOCK(10),
  CONTROLLING_THE_GATE(20),
  CONTROLLING_THE_GARAGE_DOOR(30),
  THERMOMETER(40),
  HUMIDITY(42),
  HUMIDITY_AND_TEMPERATURE(45),
  OPEN_SENSOR_GATEWAY(50),
  OPEN_SENSOR_GATE(60),
  OPEN_SENSOR_GARAGE_DOOR(70),
  NO_LIQUID_SENSOR(80),
  CONTROLLING_THE_DOOR_LOCK(90),
  OPEN_SENSOR_DOOR(100),
  CONTROLLING_THE_ROLLER_SHUTTER(110),
  CONTROLLING_THE_ROOF_WINDOW(115),
  OPEN_SENSOR_ROLLER_SHUTTER(120),
  OPEN_SENSOR_ROOF_WINDOW(125),
  POWER_SWITCH(130),
  LIGHTSWITCH(140),
  RING(150),
  ALARM(160),
  NOTIFICATION(170),
  DIMMER(180),
  RGB_LIGHTING(190),
  DIMMER_AND_RGB_LIGHTING(200),
  DEPTH_SENSOR(210),
  DISTANCE_SENSOR(220),
  OPENING_SENSOR_WINDOW(230),
  HOTEL_CARD_SENSOR(235),
  ALARM_ARMAMENT_SENSOR(236),
  MAIL_SENSOR(240),
  WIND_SENSOR(250),
  PRESSURE_SENSOR(260),
  RAIN_SENSOR(270),
  WEIGHT_SENSOR(280),
  WEATHER_STATION(290),
  STAIRCASE_TIMER(300),
  ELECTRICITY_METER(310),
  IC_ELECTRICITY_METER(315),
  IC_GAS_METER(320),
  IC_WATER_METER(330),
  IC_HEAT_METER(340),
  THERMOSTAT_HEATPOL_HOMEPLUS(410),
  HVAC_THERMOSTAT(420),
  HVAC_THERMOSTAT_HEAT_COOL(422),
  HVAC_DOMESTIC_HOT_WATER(426),
  VALVE_OPEN_CLOSE(500),
  VALVE_PERCENTAGE(510),
  GENERAL_PURPOSE_MEASUREMENT(520),
  GENERAL_PURPOSE_METER(530),
  DIGIGLASS_HORIZONTAL(800),
  DIGIGLASS_VERTICAL(810),
  CONTROLLING_THE_FACADE_BLIND(900),
  TERRACE_AWNING(910),
  PROJECTOR_SCREEN(920),
  CURTAIN(930),
  VERTICAL_BLIND(940),
  ROLLER_GARAGE_DOOR(950),
  PUMP_SWITCH(960),
  HEAT_OR_COLD_SOURCE_SWITCH(970),
  CONTAINER(980),
  SEPTIC_TANK(981),
  WATER_TANK(982),
  CONTAINER_LEVEL_SENSOR(990),
  FLOOD_SENSOR(1000);

  companion object {
    fun from(value: Int): SuplaFunction {
      SuplaFunction.entries.forEach {
        if (it.value == value) {
          return it
        }
      }

      return UNKNOWN
    }
  }

  val hasThermostatSubfunction: Boolean
    get() = when (this) {
      HVAC_DOMESTIC_HOT_WATER, HVAC_THERMOSTAT, HVAC_THERMOSTAT_HEAT_COOL -> true
      else -> false
    }
}

fun Int.suplaFunction() = SuplaFunction.from(this)
