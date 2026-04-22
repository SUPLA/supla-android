package org.supla.android.ui.views.schedule
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

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.ui.ResourceCache

@JvmInline
value class ScheduleDetailEntryBoxKey private constructor(private val packed: Int) {
  constructor(dayOfWeek: DayOfWeek, hour: Short) : this(dayOfWeek.day.times(100).plus(hour))

  val dayOfWeek: DayOfWeek
    get() = DayOfWeek.from(packed.div(100))

  val hour: Short
    get() = packed.mod(100).toShort()

  fun copy(): ScheduleDetailEntryBoxKey = ScheduleDetailEntryBoxKey(packed)
}

interface ScheduleDetailEntryBoxValue {
  val program: SuplaScheduleProgram?

  fun drawBox(
    drawScope: DrawScope,
    topLeft: Offset,
    size: Size,
    cornerRadius: CornerRadius,
    resourceCache: ResourceCache
  )
}
