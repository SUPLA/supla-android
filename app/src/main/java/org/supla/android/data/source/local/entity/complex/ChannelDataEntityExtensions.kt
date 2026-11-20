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

import com.google.gson.Gson
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.data.source.local.entity.hasMeasurements
import org.supla.android.data.source.local.entity.isElectricityMeter
import org.supla.android.data.source.local.entity.isHvacThermostat
import org.supla.android.data.source.local.entity.isIconValueItem
import org.supla.android.data.source.local.entity.isImpulseCounter
import org.supla.android.data.source.local.entity.isShadingSystem
import org.supla.android.data.source.local.entity.isThermometer
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.data.source.remote.channel.suplaElectricityMeterMeasuredTypes
import org.supla.android.data.source.remote.rollershutter.SuplaChannelFacadeBlindConfig
import org.supla.android.features.details.windowdetail.base.data.ShadingBlindMarker
import org.supla.android.lib.SuplaChannelElectricityMeterValue
import org.supla.android.lib.SuplaChannelImpulseCounterValue
import org.supla.core.shared.data.model.shadingsystem.SuplaShadingSystemFlag
import timber.log.Timber

fun ChannelDataEntity.isIconValueItem() = channelEntity.isIconValueItem()

fun ChannelDataEntity.isElectricityMeter() = channelEntity.isElectricityMeter()

fun ChannelDataEntity.isImpulseCounter() = channelEntity.isImpulseCounter()

fun ChannelDataEntity.isHvacThermostat() = channelEntity.isHvacThermostat()

fun ChannelDataEntity.hasMeasurements() = channelEntity.hasMeasurements()

fun ChannelDataEntity.isThermometer() = channelEntity.isThermometer()

fun ChannelDataEntity.isShadingSystem() = channelEntity.isShadingSystem()

val ChannelDataEntity.Electricity: ChannelDataElectricityExtension
  get() = ChannelDataElectricityExtension(this)

val ChannelDataEntity.ImpulseCounter: ChannelDataImpulseCounterExtension
  get() = ChannelDataImpulseCounterExtension(this)

fun List<ChannelDataEntity>.toShadingBlindMarkers(gson: Gson): List<ShadingBlindMarker> =
  mapNotNull { channel ->
    val value = channel.channelValueEntity.asFacadeBlindValue()
    if (!value.hasValidPosition()) return@mapNotNull null

    val tiltValue = value.tilt
      .takeIf { value.hasValidTilt() && value.flags.contains(SuplaShadingSystemFlag.TILT_IS_SET) }
      ?.toFloat()

    val config = (channel.configEntity?.toSuplaConfig(gson) as? SuplaChannelFacadeBlindConfig)
    ShadingBlindMarker(
      position = value.position.toFloat(),
      tiltValue = tiltValue,
      tilt0Angle = config?.tilt0Angle?.toFloat(),
      tilt100Angle = config?.tilt100Angle?.toFloat()
    )
  }.sortedBy { it.position }

@JvmInline
value class ChannelDataElectricityExtension(private val channelData: ChannelDataEntity) {
  val phases: List<Phase>
    get() = Phase.entries
      .filter { channelData.flags and it.disabledFlag.rawValue == 0L }

  val value: SuplaChannelElectricityMeterValue?
    get() = try {
      channelData.channelExtendedValueEntity?.getSuplaValue()?.ElectricityMeterValue
    } catch (ex: Exception) {
      Timber.w(ex, "Could not get electricity meter value")
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
      Timber.w(ex, "Could not get impulse counter value")
      null
    }
}
