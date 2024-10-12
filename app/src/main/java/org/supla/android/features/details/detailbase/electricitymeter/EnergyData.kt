package org.supla.android.features.details.detailbase.electricitymeter
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

import org.supla.android.extensions.ifNotZero
import org.supla.android.usecases.channel.valueformatter.ChannelValueFormatter
import java.text.DecimalFormat

data class EnergyData(
  val energy: String,
  val price: String? = null
) {
  companion object {
    operator fun invoke(formatter: ChannelValueFormatter, energy: Double, pricePerUnit: Double, currency: String): EnergyData =
      EnergyData(
        energy = formatter.format(energy),
        price = pricePerUnit.ifNotZero {
          val decimalFormatter = DecimalFormat()
          decimalFormatter.minimumFractionDigits = 2
          decimalFormatter.maximumFractionDigits = 2

          "${decimalFormatter.format(it.times(energy))} $currency"
        }
      )
  }
}
