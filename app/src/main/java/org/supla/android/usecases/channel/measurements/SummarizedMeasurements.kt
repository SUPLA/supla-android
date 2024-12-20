package org.supla.android.usecases.channel.measurements
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

import org.supla.android.data.source.remote.electricitymeter.hasForwardEnergy
import org.supla.android.data.source.remote.electricitymeter.hasReverseEnergy
import org.supla.android.lib.SuplaChannelElectricityMeterValue
import org.supla.android.lib.SuplaChannelImpulseCounterValue
import org.supla.android.ui.views.card.SummaryCardData
import org.supla.android.usecases.channel.valueformatter.ImpulseCounterValueFormatter
import org.supla.android.usecases.channel.valueformatter.ListElectricityMeterValueFormatter
import org.supla.core.shared.extensions.ifTrue

sealed interface SummarizedMeasurements

data class ElectricityMeasurements(
  val forwardActiveEnergy: Float,
  val reversedActiveEnergy: Float
) : SummarizedMeasurements {
  fun toForwardEnergy(
    formatter: ListElectricityMeterValueFormatter,
    electricityMeterValue: SuplaChannelElectricityMeterValue? = null
  ): SummaryCardData? =
    if (electricityMeterValue != null) {
      with(electricityMeterValue) {
        hasForwardEnergy.ifTrue { SummaryCardData(formatter, forwardActiveEnergy.toDouble(), pricePerUnit, currency) }
      }
    } else {
      SummaryCardData(value = formatter.format(forwardActiveEnergy))
    }

  fun toReverseEnergy(
    formatter: ListElectricityMeterValueFormatter,
    electricityMeterValue: SuplaChannelElectricityMeterValue? = null
  ): SummaryCardData? =
    if (electricityMeterValue != null) {
      electricityMeterValue.hasReverseEnergy.ifTrue { SummaryCardData(value = formatter.format(reversedActiveEnergy)) }
    } else {
      SummaryCardData(value = formatter.format(reversedActiveEnergy))
    }
}

data class ImpulseCounterMeasurements(
  val counter: Float
) : SummarizedMeasurements {
  fun toSummaryCardData(
    formatter: ImpulseCounterValueFormatter,
    impulseCounterValue: SuplaChannelImpulseCounterValue? = null
  ): SummaryCardData =
    impulseCounterValue?.let {
      val unit = impulseCounterValue.unit?.let { ImpulseCounterValueFormatter.Data(it) }
      SummaryCardData(formatter, counter.toDouble(), impulseCounterValue.pricePerUnit, impulseCounterValue.currency, unit)
    } ?: SummaryCardData(formatter.format(counter.toDouble()))
}
