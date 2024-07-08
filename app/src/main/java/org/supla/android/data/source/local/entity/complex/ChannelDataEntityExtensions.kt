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

import org.supla.android.Trace
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.data.source.local.entity.hasMeasurements
import org.supla.android.data.source.local.entity.isElectricityMeter
import org.supla.android.data.source.local.entity.isFacadeBlind
import org.supla.android.data.source.local.entity.isGpMeasurement
import org.supla.android.data.source.local.entity.isGpMeter
import org.supla.android.data.source.local.entity.isGpm
import org.supla.android.data.source.local.entity.isHvacThermostat
import org.supla.android.data.source.local.entity.isIconValueItem
import org.supla.android.data.source.local.entity.isShadingSystem
import org.supla.android.data.source.local.entity.isThermometer
import org.supla.android.data.source.local.entity.isVerticalBlind
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.data.source.remote.channel.suplaElectricityMeterMeasuredTypes
import org.supla.android.extensions.TAG
import org.supla.android.lib.SuplaChannelElectricityMeterValue
import org.supla.android.lib.SuplaChannelImpulseCounterValue

fun ChannelDataEntity.isIconValueItem() = channelEntity.isIconValueItem()

fun ChannelDataEntity.isGpm() = channelEntity.isGpm()

fun ChannelDataEntity.isGpMeasurement() = channelEntity.isGpMeasurement()

fun ChannelDataEntity.isGpMeter() = channelEntity.isGpMeter()

fun ChannelDataEntity.isElectricityMeter() = channelEntity.isElectricityMeter()

fun ChannelDataEntity.isHvacThermostat() = channelEntity.isHvacThermostat()

fun ChannelDataEntity.hasMeasurements() = channelEntity.hasMeasurements()

fun ChannelDataEntity.isThermometer() = channelEntity.isThermometer()

fun ChannelDataEntity.isShadingSystem() = channelEntity.isShadingSystem()

fun ChannelDataEntity.isFacadeBlind() = channelEntity.isFacadeBlind()

fun ChannelDataEntity.isVerticalBlind() = channelEntity.isVerticalBlind()

val ChannelDataEntity.Electricity: ChannelDataElectricityExtension
  get() = ChannelDataElectricityExtension(this)

val ChannelDataEntity.ImpulseCounter: ChannelDataImpulseCounterExtension
  get() = ChannelDataImpulseCounterExtension(this)

@JvmInline
value class ChannelDataElectricityExtension(private val channelData: ChannelDataEntity) {
  val phases: List<Phase>
    get() = Phase.entries
      .filter { channelData.flags and it.disabledFlag.rawValue == 0L }

  val value: SuplaChannelElectricityMeterValue?
    get() = try {
      channelData.channelExtendedValueEntity?.getSuplaValue()?.ElectricityMeterValue
    } catch (ex: Exception) {
      Trace.w(TAG, "Could not get electricity meter value", ex)
      null
    }

  val measuredTypes: List<SuplaElectricityMeasurementType>
    get() = value?.measuredValues?.suplaElectricityMeterMeasuredTypes ?: emptyList()

  val hasBalance: Boolean
    get() = measuredTypes.contains(SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY_BALANCED) &&
      measuredTypes.contains(SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY_BALANCED)
}

@JvmInline
value class ChannelDataImpulseCounterExtension(private val channelData: ChannelDataEntity) {
  val value: SuplaChannelImpulseCounterValue?
    get() = try {
      channelData.channelExtendedValueEntity?.getSuplaValue()?.ImpulseCounterValue
    } catch (ex: Exception) {
      Trace.w(TAG, "Could not get impulse counter value", ex)
      null
    }
}
