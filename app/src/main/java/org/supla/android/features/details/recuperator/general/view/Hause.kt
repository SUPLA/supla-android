package org.supla.android.features.details.recuperator.general.view
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

import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

private const val HOUSE_PATH = "M173.5 71.5072V54.0001C173.501 51.6089 172.897 49.2463 171.73 47.0772C170.563 44.9081 168.862 42.9848 " +
  "166.745 41.4412L100.05 5.88338C96.611 3.37579 92.2532 2 87.75 2C83.2468 2 78.889 3.37579 75.4497 5.88338L8.7552 41.4412C6.6381 " +
  "42.9848 4.93686 44.9081 3.77014 47.0772C2.60343 49.2463 1.99934 51.6089 2 54.0001L2 137.562C2 141.921 4.00763 146.103 7.58124 " +
  "149.185C11.1549 152.268 16.0017 154 21.0556 154H154.444C159.498 154 164.345 152.268 167.919 149.185C171.492 146.103 173.5 141.921 " +
  "173.5 137.562V130.081"

fun RecuperatorDrawScope.drawHouse(scaleMatrix: Matrix) {
  val housePath = PathParser().parsePathString(HOUSE_PATH).toPath()
  housePath.transform(scaleMatrix)

  drawPath(
    path = housePath,
    color = colors.outline,
    style = Stroke(
      width = 4.dp.toPx(),
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
  )
}
