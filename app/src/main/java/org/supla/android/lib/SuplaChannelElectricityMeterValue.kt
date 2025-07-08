package org.supla.android.lib
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
import org.supla.android.data.source.remote.electricitymeter.ElectricityMeterPhaseSequence
import org.supla.android.data.source.remote.electricitymeter.ElectricityMeterPhaseSequence.Companion.from
import org.supla.android.tools.UsedFromNativeCode
import kotlin.Double

@Serializable
class SuplaChannelElectricityMeterValue @UsedFromNativeCode internal constructor(
  val measuredValues: Int,
  val voltagePhaseAngle12: Int,
  val voltagePhaseAngle13: Int,
  val voltagePhaseSequence: ElectricityMeterPhaseSequence,
  val currentPhaseSequence: ElectricityMeterPhaseSequence,
  val period: Int,
  val totalCost: Double,
  val pricePerUnit: Double,
  val currency: String,
  val totalForwardActiveEnergyBalanced: Double,
  val totalReverseActiveEnergyBalanced: Double
) {

  private val sumList: MutableList<SummaryDto> = mutableListOf()
  private val mp1List: MutableList<MeasurementDto> = mutableListOf()
  private val mp2List: MutableList<MeasurementDto> = mutableListOf()
  private val mp3List: MutableList<MeasurementDto> = mutableListOf()

  @UsedFromNativeCode
  constructor(
    measuredValues: Int,
    voltagePhaseAngle12: Int,
    voltagePhaseAngle13: Int,
    phaseSequence: Int,
    period: Int,
    totalCost: Int,
    pricePerUnit: Int,
    currency: String,
    totalForwardActiveEnergyBalanced: Long,
    totalReverseActiveEnergyBalanced: Long
  ) : this(
    measuredValues = measuredValues,
    voltagePhaseAngle12 = voltagePhaseAngle12,
    voltagePhaseAngle13 = voltagePhaseAngle13,
    voltagePhaseSequence = from((phaseSequence and 0x1) == 0),
    currentPhaseSequence = from((phaseSequence and 0x2) == 0),
    period = period,
    totalCost = totalCost / 100.0,
    pricePerUnit = pricePerUnit / 10000.0,
    currency = currency,
    totalForwardActiveEnergyBalanced = totalForwardActiveEnergyBalanced / 100000.0,
    totalReverseActiveEnergyBalanced = totalReverseActiveEnergyBalanced / 100000.0
  )

  @UsedFromNativeCode
  fun addSummary(phase: Int, sum: Summary) {
    if (phase >= 1 && phase <= 3) {
      if (sumList.size >= phase) {
        sumList[phase - 1] = SummaryDto.from(sum)
      }
      sumList.add(phase - 1, SummaryDto.from(sum))
    }
  }

  @UsedFromNativeCode
  fun addMeasurement(phase: Int, measurement: Measurement) {
    when (phase) {
      1 -> mp1List.add(MeasurementDto.from(measurement))
      2 -> mp2List.add(MeasurementDto.from(measurement))
      3 -> mp3List.add(MeasurementDto.from(measurement))
    }
  }

  fun getMeasurement(phase: Int, index: Int): Measurement? {
    return toMeasurement(
      measurement = when (phase) {
        1 -> if (mp1List.size > index) mp1List[index] else null
        2 -> if (mp2List.size > index) mp2List[index] else null
        3 -> if (mp3List.size > index) mp3List[index] else null
        else -> null
      }
    )
  }

  val summary: Summary
    get() {
      val summaryP1 = getSummary(1)
      val summaryP2 = getSummary(2)
      val summaryP3 = getSummary(3)

      return Summary(
        (summaryP1.totalForwardActiveEnergy + summaryP2.totalForwardActiveEnergy + summaryP3.totalForwardActiveEnergy),
        (summaryP1.totalReverseActiveEnergy + summaryP2.totalReverseActiveEnergy + summaryP3.totalReverseActiveEnergy),
        (summaryP1.totalForwardReactiveEnergy + summaryP2.totalForwardReactiveEnergy + summaryP3.totalForwardReactiveEnergy),
        (summaryP1.totalReverseReactiveEnergy + summaryP2.totalReverseReactiveEnergy + summaryP3.totalReverseReactiveEnergy)
      )
    }

  fun getSummary(phase: Int): Summary {
    if (phase >= 1 && phase <= 3) {
      if (sumList.size >= phase) {
        return toSummary(sumList[phase - 1])
      }
    }
    return Summary(0.00, 0.00, 0.00, 0.00)
  }

  inner class Measurement(
    val frequency: Double,
    val voltage: Double,
    val current: Double,
    val powerActive: Double,
    val powerReactive: Double,
    val powerApparent: Double,
    val powerFactor: Double,
    val phaseAngle: Double
  ) {
    @UsedFromNativeCode
    constructor(
      frequency: Int,
      voltage: Int,
      current: Int,
      powerActive: Int,
      powerReactive: Int,
      powerApparent: Int,
      powerFactor: Int,
      phaseAngle: Int
    ) : this(
      frequency = frequency / 100.0,
      voltage = voltage / 100.0,
      current = current / 1000.0,
      powerActive = powerActive / 100000.0,
      powerReactive = powerReactive / 100000.0,
      powerApparent = powerApparent / 100000.0,
      powerFactor = powerFactor / 1000.0,
      phaseAngle = phaseAngle / 10.0
    )
  }

  inner class Summary(
    val totalForwardActiveEnergy: Double,
    val totalReverseActiveEnergy: Double,
    val totalForwardReactiveEnergy: Double,
    val totalReverseReactiveEnergy: Double
  ) {
    @UsedFromNativeCode
    internal constructor(
      totalForwardActiveEnergy: Long,
      totalReverseActiveEnergy: Long,
      totalForwardReactiveEnergy: Long,
      totalReverseReactiveEnergy: Long
    ) : this(
      totalForwardActiveEnergy = totalForwardActiveEnergy / 100000.00,
      totalReverseActiveEnergy = totalReverseActiveEnergy / 100000.00,
      totalForwardReactiveEnergy = totalForwardReactiveEnergy / 100000.00,
      totalReverseReactiveEnergy = totalReverseReactiveEnergy / 100000.00
    )
  }

  private fun toSummary(dto: SummaryDto): Summary =
    Summary(
      totalForwardActiveEnergy = dto.totalForwardActiveEnergy,
      totalReverseActiveEnergy = dto.totalReverseActiveEnergy,
      totalForwardReactiveEnergy = dto.totalForwardReactiveEnergy,
      totalReverseReactiveEnergy = dto.totalReverseReactiveEnergy
    )

  private fun toMeasurement(measurement: MeasurementDto?): Measurement? =
    measurement?.let { dto ->
      Measurement(
        frequency = dto.frequency,
        voltage = dto.voltage,
        current = dto.current,
        powerActive = dto.powerActive,
        powerReactive = dto.powerReactive,
        powerApparent = dto.powerApparent,
        powerFactor = dto.powerFactor,
        phaseAngle = dto.phaseAngle
      )
    }
}

@Serializable
private data class SummaryDto(
  val totalForwardActiveEnergy: Double,
  val totalReverseActiveEnergy: Double,
  val totalForwardReactiveEnergy: Double,
  val totalReverseReactiveEnergy: Double
) {
  companion object {
    fun from(summary: SuplaChannelElectricityMeterValue.Summary): SummaryDto =
      SummaryDto(
        totalForwardActiveEnergy = summary.totalForwardActiveEnergy,
        totalReverseActiveEnergy = summary.totalReverseActiveEnergy,
        totalForwardReactiveEnergy = summary.totalForwardReactiveEnergy,
        totalReverseReactiveEnergy = summary.totalReverseReactiveEnergy
      )
  }
}

@Serializable
private data class MeasurementDto(
  val frequency: Double,
  val voltage: Double,
  val current: Double,
  val powerActive: Double,
  val powerReactive: Double,
  val powerApparent: Double,
  val powerFactor: Double,
  val phaseAngle: Double
) {
  companion object {
    fun from(measurement: SuplaChannelElectricityMeterValue.Measurement): MeasurementDto =
      MeasurementDto(
        frequency = measurement.frequency,
        voltage = measurement.voltage,
        current = measurement.current,
        powerActive = measurement.powerActive,
        powerReactive = measurement.powerReactive,
        powerApparent = measurement.powerApparent,
        powerFactor = measurement.powerFactor,
        phaseAngle = measurement.phaseAngle
      )
  }
}
