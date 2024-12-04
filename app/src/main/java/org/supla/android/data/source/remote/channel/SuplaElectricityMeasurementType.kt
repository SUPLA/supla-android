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
import org.supla.android.ui.views.SpinnerItem

enum class SuplaElectricityMeasurementType(val rawValue: Int, val ordering: Int, @StringRes override val labelRes: Int) : SpinnerItem {
  FREQUENCY(0x1, 1, R.string.details_em_frequency),
  VOLTAGE(0x2, 3, R.string.details_em_voltage),
  CURRENT(0x4, 4, R.string.details_em_current),
  POWER_ACTIVE(0x8, 5, R.string.details_em_power_active),
  POWER_REACTIVE(0x10, 6, R.string.details_em_power_reactive),
  POWER_APPARENT(0x20, 7, R.string.details_em_power_apparent),
  POWER_FACTOR(0x40, 8, R.string.details_em_power_factor),
  PHASE_ANGLE(0x80, 9, R.string.details_em_phase_angle),
  FORWARD_ACTIVE_ENERGY(0x100, 10, R.string.details_em_forward_active_energy),
  REVERSE_ACTIVE_ENERGY(0x200, 11, R.string.details_em_reverse_active_energy),
  FORWARD_REACTIVE_ENERGY(0x400, 12, R.string.details_em_forward_reactive_energy),
  REVERSE_REACTIVE_ENERGY(0x800, 13, R.string.details_em_reverse_reactive_energy),
  CURRENT_OVER_65A(0x1000, 4, R.string.details_em_current),
  FORWARD_ACTIVE_ENERGY_BALANCED(0x2000, 14, R.string.details_em_forward_active_energy),
  REVERSE_ACTIVE_ENERGY_BALANCED(0x4000, 15, R.string.details_em_reverse_active_energy),
  POWER_ACTIVE_KW(0x100000, 5, R.string.details_em_power_active),
  POWER_REACTIVE_KVAR(0x200000, 6, R.string.details_em_power_reactive),
  POWER_APPARENT_KVA(0x400000, 7, R.string.details_em_power_apparent);

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
      REVERSE_REACTIVE_ENERGY,
      CURRENT_OVER_65A,
      POWER_ACTIVE_KW,
      POWER_REACTIVE_KVAR,
      POWER_APPARENT_KVA -> true

      else -> false
    }

  val provider: ((SuplaChannelElectricityMeterValue.Measurement?, SuplaChannelElectricityMeterValue.Summary) -> Float?)?
    get() = when (this) {
      FREQUENCY -> { measurement, _ -> measurement?.freq?.toFloat() }
      VOLTAGE -> { measurement, _ -> measurement?.voltage?.toFloat() }
      CURRENT -> { measurement, _ -> measurement?.current?.toFloat() }
      CURRENT_OVER_65A -> { measurement, _ -> measurement?.current?.toFloat()?.times(10) }
      POWER_ACTIVE -> { measurement, _ -> measurement?.powerActive?.toFloat() }
      POWER_ACTIVE_KW -> { measurement, _ -> measurement?.powerActive?.toFloat()?.times(1000) }
      POWER_REACTIVE -> { measurement, _ -> measurement?.powerReactive?.toFloat() }
      POWER_REACTIVE_KVAR -> { measurement, _ -> measurement?.powerReactive?.toFloat()?.times(1000) }
      POWER_APPARENT -> { measurement, _ -> measurement?.powerApparent?.toFloat() }
      POWER_APPARENT_KVA -> { measurement, _ -> measurement?.powerApparent?.toFloat()?.times(1000) }
      POWER_FACTOR -> { measurement, _ -> measurement?.powerFactor?.toFloat() }
      PHASE_ANGLE -> { measurement, _ -> measurement?.phaseAngle?.toFloat() }
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
      CURRENT, CURRENT_OVER_65A -> "A"
      POWER_ACTIVE, POWER_ACTIVE_KW -> "W"
      POWER_REACTIVE, POWER_REACTIVE_KVAR -> "var"
      POWER_APPARENT, POWER_APPARENT_KVA -> "VA"
      POWER_FACTOR -> ""
      PHASE_ANGLE -> "°"
      FORWARD_ACTIVE_ENERGY,
      REVERSE_ACTIVE_ENERGY,
      FORWARD_ACTIVE_ENERGY_BALANCED,
      REVERSE_ACTIVE_ENERGY_BALANCED -> "kWh"

      FORWARD_REACTIVE_ENERGY,
      REVERSE_REACTIVE_ENERGY -> "kvarh"
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

  val showEnergyLabel: Boolean
    get() = when (this) {
      FORWARD_ACTIVE_ENERGY,
      REVERSE_ACTIVE_ENERGY,
      FORWARD_REACTIVE_ENERGY,
      REVERSE_REACTIVE_ENERGY -> true

      else -> false
    }

  val shortLabel: Int
    get() = when (this) {
      FREQUENCY -> R.string.details_em_frequency
      VOLTAGE -> R.string.details_em_voltage
      CURRENT, CURRENT_OVER_65A -> R.string.details_em_current
      POWER_ACTIVE, POWER_ACTIVE_KW -> R.string.details_em_power_active
      POWER_REACTIVE, POWER_REACTIVE_KVAR -> R.string.details_em_power_reactive
      POWER_APPARENT, POWER_APPARENT_KVA -> R.string.details_em_power_apparent
      POWER_FACTOR -> R.string.details_em_power_factor
      PHASE_ANGLE -> R.string.details_em_phase_angle
      FORWARD_ACTIVE_ENERGY -> R.string.details_em_forward_active_energy_short
      REVERSE_ACTIVE_ENERGY -> R.string.details_em_reverse_active_energy_short
      FORWARD_ACTIVE_ENERGY_BALANCED -> R.string.details_em_forward_active_energy_short
      REVERSE_ACTIVE_ENERGY_BALANCED -> R.string.details_em_reverse_active_energy_short
      FORWARD_REACTIVE_ENERGY -> R.string.details_em_forward_reactive_energy_short
      REVERSE_REACTIVE_ENERGY -> R.string.details_em_reverse_reactive_energy_short
    }

  fun merge(values: List<Float>): Value? =
    when (this) {
      FREQUENCY -> Value.Single(values.first())
      VOLTAGE -> Value.Double(values.min(), values.max())
      POWER_ACTIVE,
      POWER_ACTIVE_KW,
      POWER_REACTIVE,
      POWER_REACTIVE_KVAR,
      POWER_APPARENT,
      POWER_APPARENT_KVA,
      FORWARD_ACTIVE_ENERGY,
      REVERSE_ACTIVE_ENERGY,
      FORWARD_REACTIVE_ENERGY,
      REVERSE_REACTIVE_ENERGY -> Value.Single(values.sum())

      else -> null
    }

  companion object {
    fun from(value: Int): List<SuplaElectricityMeasurementType> =
      mutableListOf<SuplaElectricityMeasurementType>().apply {
        SuplaElectricityMeasurementType.entries.forEach {
          if (it.rawValue.and(value) > 0) {
            add(it)
          }
        }
        if (contains(CURRENT) && contains(CURRENT_OVER_65A)) {
          remove(CURRENT_OVER_65A)
        }
        if (contains(POWER_ACTIVE) && contains(POWER_ACTIVE_KW)) {
          remove(POWER_ACTIVE)
        }
        if (contains(POWER_REACTIVE) && contains(POWER_REACTIVE_KVAR)) {
          remove(POWER_REACTIVE)
        }
        if (contains(POWER_APPARENT) && contains(POWER_APPARENT_KVA)) {
          remove(POWER_APPARENT)
        }
      }
  }

  sealed interface Value {
    data class Single(val value: Float) : Value
    data class Double(val first: Float, val second: Float) : Value
  }
}

val Int.suplaElectricityMeterMeasuredTypes: List<SuplaElectricityMeasurementType>
  get() = SuplaElectricityMeasurementType.from(this)

val List<SuplaElectricityMeasurementType>.hasForwardAndReverseEnergy: Boolean
  get() =
    contains(SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY_BALANCED) &&
      contains(SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY_BALANCED)
