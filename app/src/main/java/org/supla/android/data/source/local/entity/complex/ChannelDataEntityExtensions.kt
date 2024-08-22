package org.supla.android.data.source.local.entity.complex
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

import org.supla.android.data.model.general.hasElectricityMeter
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.data.source.local.entity.hasMeasurements
import org.supla.android.data.source.local.entity.isFacadeBlind
import org.supla.android.data.source.local.entity.isGpMeasurement
import org.supla.android.data.source.local.entity.isGpMeter
import org.supla.android.data.source.local.entity.isGpm
import org.supla.android.data.source.local.entity.isHvacThermostat
import org.supla.android.data.source.local.entity.isMeasurement
import org.supla.android.data.source.local.entity.isShadingSystem
import org.supla.android.data.source.local.entity.isThermometer
import org.supla.android.data.source.local.entity.isVerticalBlind
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.data.source.remote.channel.suplaElectricityMeterMeasuredTypes
import org.supla.android.lib.SuplaChannelElectricityMeterValue
import org.supla.android.lib.SuplaConst

fun ChannelDataEntity.isMeasurement() = channelEntity.isMeasurement()

fun ChannelDataEntity.isGpm() = channelEntity.isGpm()

fun ChannelDataEntity.isGpMeasurement() = channelEntity.isGpMeasurement()

fun ChannelDataEntity.isGpMeter() = channelEntity.isGpMeter()

fun ChannelDataEntity.isHvacThermostat() = channelEntity.isHvacThermostat()

fun ChannelDataEntity.hasMeasurements() = channelEntity.hasMeasurements()

fun ChannelDataEntity.hasValue(): Boolean =
  when (function) {
    SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT,
    SuplaConst.SUPLA_CHANNELFNC_THERMOMETER,
    SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR,
    SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR,
    SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER -> true

    SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
    SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH,
    SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER -> hasElectricityMeter

    else -> false
  }

fun ChannelDataEntity.isThermometer() = channelEntity.isThermometer()

fun ChannelDataEntity.isShadingSystem() = channelEntity.isShadingSystem()

fun ChannelDataEntity.isFacadeBlind() = channelEntity.isFacadeBlind()

fun ChannelDataEntity.isVerticalBlind() = channelEntity.isVerticalBlind()

val ChannelDataEntity.Electricity: ChannelDataElectricityExtension
  get() = ChannelDataElectricityExtension(this)

@JvmInline
value class ChannelDataElectricityExtension(private val channelData: ChannelDataEntity) {
  val phases: List<Phase>
    get() = Phase.entries
      .filter { channelData.flags and it.disabledFlag.rawValue == 0L }

  val value: SuplaChannelElectricityMeterValue?
    get() = channelData.channelExtendedValueEntity?.getSuplaValue()?.ElectricityMeterValue

  val measuredTypes: List<SuplaElectricityMeasurementType>
    get() = value?.measuredValues?.suplaElectricityMeterMeasuredTypes ?: emptyList()

  val hasBalance: Boolean
    get() = measuredTypes.contains(SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY_BALANCED) &&
      measuredTypes.contains(SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY_BALANCED)
}
