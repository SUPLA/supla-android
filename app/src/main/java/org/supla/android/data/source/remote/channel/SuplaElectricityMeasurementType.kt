package org.supla.android.data.source.remote.channel
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

import androidx.annotation.StringRes
import org.supla.android.R
import org.supla.android.lib.SuplaChannelElectricityMeterValue

enum class SuplaElectricityMeasurementType(val rawValue: Int, @StringRes val labelRes: Int) {
  FREQUENCY(0x1, R.string.details_em_frequency),
  VOLTAGE(0x2, R.string.details_em_voltage),
  CURRENT(0x4, R.string.details_em_current),
  POWER_ACTIVE(0x8, R.string.details_em_power_active),
  POWER_REACTIVE(0x10, R.string.details_em_power_reactive),
  POWER_APPARENT(0x20, R.string.details_em_power_apparent),
  POWER_FACTOR(0x40, R.string.details_em_power_factor),
  PHASE_ANGLE(0x80, R.string.details_em_phase_angle),
  FORWARD_ACTIVE_ENERGY(0x100, R.string.details_em_total_forward_active_energy),
  REVERSE_ACTIVE_ENERGY(0x200, R.string.details_em_reverse_active_energy),
  FORWARD_REACTIVE_ENERGY(0x400, R.string.details_em_total_forward_reactive_energy),
  REVERSE_REACTIVE_ENERGY(0x800, R.string.details_em_total_reverse_reactive_energy),
  CURRENT_OVER_65A(0x1000, 0),
  FORWARD_ACTIVE_ENERGY_BALANCED(0x2000, R.string.details_em_total_forward_active_energy),
  REVERSE_ACTIVE_ENERGY_BALANCED(0x4000, R.string.details_em_reverse_active_energy),
  POWER_ACTIVE_KW(0x100000, 0),
  POWER_REACTIVE_KVAR(0x200000, 0),
  POWER_APPARENT_KVA(0x400000, 0);

  val isFlag: Boolean
    get() = when (this) {
      CURRENT_OVER_65A,
      POWER_ACTIVE_KW,
      POWER_REACTIVE_KVAR,
      POWER_APPARENT_KVA -> true

      else -> false
    }

  val hasFlag: Boolean
    get() = when (this) {
      POWER_ACTIVE,
      POWER_REACTIVE,
      POWER_APPARENT -> true

      else -> false
    }

  val flag: SuplaElectricityMeasurementType?
    get() = when (this) {
      POWER_ACTIVE -> POWER_ACTIVE_KW
      POWER_REACTIVE -> POWER_REACTIVE_KVAR
      POWER_APPARENT -> POWER_APPARENT_KVA

      else -> null
    }

  val phaseType: Boolean
    get() = when (this) {
      FREQUENCY,
      VOLTAGE,
      CURRENT,
      POWER_ACTIVE,
      POWER_REACTIVE,
      POWER_APPARENT,
      POWER_FACTOR,
      PHASE_ANGLE,
      FORWARD_ACTIVE_ENERGY,
      REVERSE_ACTIVE_ENERGY,
      FORWARD_REACTIVE_ENERGY,
      REVERSE_REACTIVE_ENERGY -> true

      else -> false
    }

  val provider: ((SuplaChannelElectricityMeterValue.Measurement, SuplaChannelElectricityMeterValue.Summary) -> Float)?
    get() = when (this) {
      FREQUENCY -> { measurement, _ -> measurement.freq.toFloat() }
      VOLTAGE -> { measurement, _ -> measurement.voltage.toFloat() }
      CURRENT -> { measurement, _ -> measurement.current.toFloat() }
      POWER_ACTIVE -> { measurement, _ -> measurement.powerActive.toFloat() }
      POWER_REACTIVE -> { measurement, _ -> measurement.powerReactive.toFloat() }
      POWER_APPARENT -> { measurement, _ -> measurement.powerApparent.toFloat() }
      POWER_FACTOR -> { measurement, _ -> measurement.powerFactor.toFloat() }
      PHASE_ANGLE -> { measurement, _ -> measurement.phaseAngle.toFloat() }
      FORWARD_ACTIVE_ENERGY -> { _, sum -> sum.totalForwardActiveEnergy.toFloat() }
      REVERSE_ACTIVE_ENERGY -> { _, sum -> sum.totalReverseActiveEnergy.toFloat() }
      FORWARD_REACTIVE_ENERGY -> { _, sum -> sum.totalForwardReactiveEnergy.toFloat() }
      REVERSE_REACTIVE_ENERGY -> { _, sum -> sum.totalReverseReactiveEnergy.toFloat() }

      else -> null
    }

  val unit: String
    get() = when (this) {
      FREQUENCY -> "Hz"
      VOLTAGE -> "V"
      CURRENT -> "A"
      POWER_ACTIVE -> "W"
      POWER_REACTIVE -> "var"
      POWER_APPARENT -> "VA"
      POWER_FACTOR -> ""
      PHASE_ANGLE -> "°"
      FORWARD_ACTIVE_ENERGY,
      REVERSE_ACTIVE_ENERGY -> "kWh"

      FORWARD_REACTIVE_ENERGY,
      REVERSE_REACTIVE_ENERGY -> "kvarh"

      else -> ""
    }

  val precision: Int
    get() = when (this) {
      FREQUENCY -> 2
      VOLTAGE -> 2
      CURRENT -> 2
      POWER_ACTIVE -> 2
      POWER_REACTIVE -> 2
      POWER_APPARENT -> 2
      POWER_FACTOR -> 3
      PHASE_ANGLE -> 2
      FORWARD_ACTIVE_ENERGY,
      REVERSE_ACTIVE_ENERGY -> 5

      FORWARD_REACTIVE_ENERGY,
      REVERSE_REACTIVE_ENERGY -> 5

      else -> 0
    }

  fun merge(values: List<Float>): Float? =
    when (this) {
      FREQUENCY -> values.first()
      VOLTAGE -> values.average().toFloat()
      CURRENT -> null
      POWER_ACTIVE,
      POWER_REACTIVE,
      POWER_APPARENT,
      POWER_FACTOR,
      FORWARD_ACTIVE_ENERGY,
      REVERSE_ACTIVE_ENERGY,
      FORWARD_REACTIVE_ENERGY,
      REVERSE_REACTIVE_ENERGY -> values.sum()

      else -> 0f
    }

  companion object {
    fun from(value: Int): List<SuplaElectricityMeasurementType> =
      mutableListOf<SuplaElectricityMeasurementType>().apply {
        SuplaElectricityMeasurementType.entries.forEach {
          if (it.rawValue.and(value) > 0) {
            add(it)
          }
        }
      }
  }
}

val Int.suplaElectricityMeterMeasuredTypes: List<SuplaElectricityMeasurementType>
  get() = SuplaElectricityMeasurementType.from(this)
