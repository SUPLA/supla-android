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

import org.supla.android.ui.views.card.SummaryCardData

data class ImpulseCounterState(
  val online: Boolean? = null,
  val totalData: SummaryCardData? = null,
  val currentMonthData: SummaryCardData? = null,
  val currentMonthDownloading: Boolean = false
)

fun ImpulseCounterState?.copyOrCreate(
  online: Boolean? = null,
  totalData: SummaryCardData? = null,
  currentMonthData: SummaryCardData? = null,
  currentMonthDownloading: Boolean = false
): ImpulseCounterState =
  this?.copy(
    online = online,
    totalData = totalData,
    currentMonthData = currentMonthData,
    currentMonthDownloading = currentMonthDownloading,
  ) ?: ImpulseCounterState(
    online = online,
    totalData = totalData,
    currentMonthData = currentMonthData,
    currentMonthDownloading = currentMonthDownloading
  )
