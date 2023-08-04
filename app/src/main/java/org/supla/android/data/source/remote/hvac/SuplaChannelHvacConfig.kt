package org.supla.android.data.source.remote.hvac

import org.supla.android.data.source.remote.SuplaChannelConfig

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

data class SuplaHvacTemperatures(
  val freezeProtection: Double?,
  val eco: Double?,
  val comfort: Double?,
  val boost: Double?,
  val heatProtection: Double?,
  val histeresis: Double?,
  val belowAlarm: Double?,
  val aboveAlarm: Double?,
  val auxMinSetpoint: Double?,
  val auxMaxSetpoint: Double?,
  val roomMin: Double?,
  val roomMax: Double?,
  val auxMin: Double?,
  val auxMax: Double?,
  val histeresisMin: Double?,
  val histeresisMax: Double?,
  val autoOffsetMin: Double?,
  val autoOffsetMax: Double?,
)

enum class SuplaHvacThermometerType(value: Int) {
  NOT_SET(0),
  DISABLED(1),
  FLOOR(2),
  WATER(3),
  GENERIC_HEATER(4),
  GENERIC_COOLER(5)
}

enum class SuplaHvacAlgorithm(value: Int) {
  NOT_SET(0),
  ON_OFF(1)
}

data class SuplaChannelHvacConfig(
  override val remoteId: Int,
  override val func: Int,
  val mainThermometerRemoteId: Int,
  val auxThermometerRemoteId: Int,
  val auxThermometerType: SuplaHvacThermometerType,
  val antiFreezeAndOverheatProtectionEnabled: Boolean,
  val availableAlgorithms: List<SuplaHvacAlgorithm>,
  val usedAlgorithm: SuplaHvacAlgorithm,
  val minOnTimeSec: Int,
  val minOffTimeSec: Int,
  val outputValueOnError: Int,
  val temperatures: SuplaHvacTemperatures
) : SuplaChannelConfig(remoteId, func)