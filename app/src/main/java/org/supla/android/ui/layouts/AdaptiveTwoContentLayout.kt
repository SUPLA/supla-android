package org.supla.android.ui.layouts

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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import org.supla.android.core.ui.theme.Distance
import kotlin.math.roundToInt

/** Each content slot must emit exactly one layout element. */
@Composable
fun AdaptiveTwoContentLayout(
  first: @Composable () -> Unit,
  second: @Composable () -> Unit,
  modifier: Modifier = Modifier
) {
  val horizontalSpacing = Distance.tiny
  val verticalSpacing = 4.dp
  Layout(
    modifier = modifier,
    content = {
      first()
      second()
    }
  ) { measurables, constraints ->
    require(measurables.size == 2) { "AdaptiveTwoContentLayout requires exactly two content slots" }

    val childConstraints = Constraints(
      maxWidth = constraints.maxWidth,
      maxHeight = constraints.maxHeight
    )
    val firstPlaceable = measurables[0].measure(childConstraints)
    val secondPlaceable = measurables[1].measure(childConstraints)
    val rowSpacing = horizontalSpacing.roundToPx()

    val rowWidth = firstPlaceable.width.toLong() + rowSpacing + secondPlaceable.width
    val showInRow = rowWidth <= constraints.maxWidth

    if (showInRow) {
      val width = constraints.constrainWidth(rowWidth.toInt())
      val height = constraints.constrainHeight(maxOf(firstPlaceable.height, secondPlaceable.height))

      layout(width, height) {
        firstPlaceable.placeRelative(0, (height - firstPlaceable.height).div(2f).roundToInt())
        secondPlaceable.placeRelative(firstPlaceable.width + rowSpacing, (height - secondPlaceable.height).div(2f).roundToInt())
      }
    } else {
      val columnSpacing = verticalSpacing.roundToPx()
      val contentWidth = maxOf(firstPlaceable.width, secondPlaceable.width)
      val contentHeight = firstPlaceable.height.toLong() + columnSpacing + secondPlaceable.height
      val width = constraints.constrainWidth(contentWidth)
      val height = constraints.constrainHeight(contentHeight.coerceAtMost(Constraints.Infinity.toLong()).toInt())

      layout(width, height) {
        firstPlaceable.placeRelative(0, 0)
        secondPlaceable.placeRelative(0, firstPlaceable.height + columnSpacing)
      }
    }
  }
}
