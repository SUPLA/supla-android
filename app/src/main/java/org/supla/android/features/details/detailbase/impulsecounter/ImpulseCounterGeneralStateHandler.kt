package org.supla.android.features.details.detailbase.impulsecounter
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

import org.supla.android.data.source.local.entity.complex.ImpulseCounter
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.extensions.guardLet
import org.supla.android.ui.views.card.SummaryCardData
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.measurements.ImpulseCounterMeasurements
import org.supla.android.usecases.channel.valueformatter.ImpulseCounterValueFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImpulseCounterGeneralStateHandler @Inject constructor(
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase
) {

  val formatter = ImpulseCounterValueFormatter()

  fun updateState(
    state: ImpulseCounterState?,
    channelWithChildren: ChannelWithChildren,
    measurements: ImpulseCounterMeasurements?
  ): ImpulseCounterState? {
    if (!channelWithChildren.isOrHasImpulseCounter) {
      return state
    }

    val unit = channelWithChildren.channel.ImpulseCounter.value?.unit?.let { ImpulseCounterValueFormatter.Data(it) }
    val (value) = guardLet(channelWithChildren.channel.ImpulseCounter.value) {
      return state.copyOrCreate(
        online = channelWithChildren.status.online,
        totalData = SummaryCardData(value = getChannelValueStringUseCase(channelWithChildren)),
        currentMonthData = measurements?.toSummaryCardData(formatter)
      )
    }

    val formatterWithUnknownValue = ImpulseCounterValueFormatter(showUnknownValue = true)
    return state.copyOrCreate(
      online = channelWithChildren.status.online,
      totalData = SummaryCardData(formatterWithUnknownValue, value.calculatedValue, value.pricePerUnit, value.currency, unit),
      currentMonthData = measurements?.toSummaryCardData(formatter, value)
    )
  }
}
