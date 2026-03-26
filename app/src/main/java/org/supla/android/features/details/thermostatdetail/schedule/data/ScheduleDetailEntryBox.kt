package org.supla.android.features.details.thermostatdetail.schedule.data
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import org.supla.android.R
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.features.details.thermostatdetail.schedule.extensions.colorRes
import org.supla.android.features.details.thermostatdetail.schedule.ui.components.ResourceCache

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

data class ThermostatScheduleDetailEntryBoxValue(
  val firstQuarterProgram: SuplaScheduleProgram,
  val secondQuarterProgram: SuplaScheduleProgram,
  val thirdQuarterProgram: SuplaScheduleProgram,
  val fourthQuarterProgram: SuplaScheduleProgram
) : ScheduleDetailEntryBoxValue {

  constructor(singleProgram: SuplaScheduleProgram) : this(singleProgram, singleProgram, singleProgram, singleProgram)

  override val program: SuplaScheduleProgram?
    get() = if (hasSingleProgram) firstQuarterProgram else null

  private val hasSingleProgram: Boolean
    get() = firstQuarterProgram == secondQuarterProgram &&
      secondQuarterProgram == thirdQuarterProgram &&
      thirdQuarterProgram == fourthQuarterProgram

  override fun drawBox(
    drawScope: DrawScope,
    topLeft: Offset,
    size: Size,
    cornerRadius: CornerRadius,
    resourceCache: ResourceCache
  ) {
    if (hasSingleProgram) {
      drawSingleProgram(drawScope, topLeft, size, cornerRadius, resourceCache)
    } else {
      drawMultiprogram(drawScope, topLeft, size, cornerRadius, resourceCache)
    }
  }

  private fun drawSingleProgram(
    drawScope: DrawScope,
    topLeft: Offset,
    size: Size,
    cornerRadius: CornerRadius,
    resourceCache: ResourceCache
  ) {
    drawScope.drawRoundRect(
      color = resourceCache.color(firstQuarterProgram.colorRes()),
      topLeft = topLeft,
      size = size,
      cornerRadius = cornerRadius
    )
  }

  private fun drawMultiprogram(
    drawScope: DrawScope,
    topLeft: Offset,
    size: Size,
    cornerRadius: CornerRadius,
    resourceCache: ResourceCache
  ) {
    val itemWidth = size.width.div(4)
    val itemHeight = size.height
    val quarterSize = Size(itemWidth, itemHeight)

    for (i in 0..3) {
      path.reset()
      val leftOffset = topLeft.x.plus(itemWidth.times(i))
      val offset = Offset(leftOffset, topLeft.y)

      when (i) {
        0 -> path.addRoundRect(
          RoundRect(
            rect = Rect(offset = offset, size = quarterSize),
            topLeft = cornerRadius,
            bottomLeft = cornerRadius
          )
        )
        3 -> path.addRoundRect(
          RoundRect(
            rect = Rect(offset = offset, size = quarterSize),
            topRight = cornerRadius,
            bottomRight = cornerRadius
          )
        )
        else -> path.addRect(rect = Rect(offset = offset, size = quarterSize))
      }
      drawScope.drawPath(path, color = color(i, resourceCache))
    }
  }

  private fun color(index: Int, resourceCache: ResourceCache): Color =
    when (index) {
      0 -> resourceCache.color(firstQuarterProgram.colorRes())
      1 -> resourceCache.color(secondQuarterProgram.colorRes())
      2 -> resourceCache.color(thirdQuarterProgram.colorRes())
      3 -> resourceCache.color(fourthQuarterProgram.colorRes())
      else -> resourceCache.color(R.color.disabled)
    }

  companion object {
    private val path = Path()
  }
}
