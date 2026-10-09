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

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.gray
import org.supla.android.features.details.programinfo.ProgramInfo
import org.supla.android.tools.SuplaPreview
import org.supla.core.shared.infrastructure.LocalizedString

@Composable
fun ProgramInfoRow(infos: List<ProgramInfo>, modifier: Modifier = Modifier) {
  val labels = infos.map { stringResource(id = it.type.stringRes).uppercase() }
  val context = LocalContext.current
  val descriptions = infos.map { it.description?.invoke(context) }
  val times = infos.map { it.time?.invoke(context) }
  val tinySpacing = dimensionResource(id = R.dimen.distance_tiny)
  val verticalSpacing = 10.dp

  Layout(
    modifier = modifier
      .height(80.dp)
      .padding(
        start = dimensionResource(id = R.dimen.distance_default),
        top = dimensionResource(id = R.dimen.distance_default),
        end = dimensionResource(id = R.dimen.distance_default)
      ),
    content = {
      infos.forEachIndexed { index, info ->
        ProgramInfoLabel(labels[index])
        ProgramInfoValue(info, descriptions[index], times[index], tinySpacing)
      }
    },
    measurePolicy = MeasurePolicy { measurables, constraints ->
      val pairs = measurables.chunked(2)
      val maxLabelWidth = pairs.maxOfOrNull { (label, _) -> label.minIntrinsicWidth(constraints.maxHeight) } ?: 0
      val maxValueWidth = pairs.maxOfOrNull { (_, value) -> value.minIntrinsicWidth(constraints.maxHeight) } ?: 0
      val rowSpacingPx = tinySpacing.roundToPx()
      val labelWidth = if (constraints.hasBoundedWidth) {
        maxLabelWidth.coerceAtMost((constraints.maxWidth - maxValueWidth - rowSpacingPx).coerceAtLeast(0))
      } else {
        maxLabelWidth
      }
      val valueMaxWidth = if (constraints.hasBoundedWidth) {
        (constraints.maxWidth - labelWidth).coerceAtLeast(0)
      } else {
        Constraints.Infinity
      }
      val labelConstraints = Constraints(minWidth = labelWidth, maxWidth = labelWidth, maxHeight = constraints.maxHeight)
      val valueConstraints = Constraints(maxWidth = valueMaxWidth, maxHeight = constraints.maxHeight)
      val rowSizes = pairs.map { (label, value) ->
        label.measure(labelConstraints) to value.measure(valueConstraints)
      }
      val spacingPx = verticalSpacing.roundToPx()
      val contentHeight = rowSizes.sumOf { maxOf(it.first.height, it.second.height) } +
        (rowSizes.size - 1).coerceAtLeast(0) * spacingPx
      val layoutWidth = if (constraints.hasBoundedWidth) {
        constraints.maxWidth
      } else {
        (rowSizes.maxOfOrNull { it.first.width + it.second.width } ?: 0).coerceAtLeast(constraints.minWidth)
      }
      val layoutHeight = contentHeight.coerceAtMost(constraints.maxHeight).coerceAtLeast(constraints.minHeight)

      layout(layoutWidth, layoutHeight) {
        var y = 0
        rowSizes.forEach { (label, value) ->
          label.placeRelative(0, y)
          value.placeRelative(labelWidth + rowSpacingPx, y)
          y += maxOf(label.height, value.height) + spacingPx
        }
      }
    }
  )
}

@Composable
private fun ProgramInfoLabel(label: String) =
  Text(
    text = label,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.gray,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis
  )

@Composable
private fun ProgramInfoValue(info: ProgramInfo, description: String?, time: String?, spacing: Dp) {
  Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
    if (info.icon != null && info.iconColor != null) {
      ProgramInfoIcon(info.icon, info.iconColor)
    }
    description?.let { ProgramInfoDescription(it) }
    time?.let {
      Text(
        text = it,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
    if (info.indicatorIcon != null && info.indicatorIconColor != null) {
      ProgramInfoIcon(info.indicatorIcon, info.indicatorIconColor)
    }
  }
}

@Composable
private fun ProgramInfoIcon(@DrawableRes icon: Int, @ColorRes color: Int) =
  Image(
    painter = painterResource(id = icon),
    contentDescription = null,
    colorFilter = ColorFilter.tint(color = colorResource(id = color)),
    modifier = Modifier.size(19.dp),
    contentScale = ContentScale.Fit
  )

@Composable
private fun ProgramInfoDescription(description: String) =
  Text(
    text = description,
    style = MaterialTheme.typography.bodyMedium,
    fontWeight = FontWeight.SemiBold,
    color = MaterialTheme.colorScheme.onBackground
  )

@SuplaPreview
@Composable
private fun ProgramInfoRowPreview() {
  SuplaTheme {
    Box(modifier = Modifier.fillMaxWidth()) {
      ProgramInfoRow(
        infos = listOf(
          ProgramInfo(
            type = ProgramInfo.Type.CURRENT,
            description = LocalizedString.Constant("On"),
            time = LocalizedString.Constant("until 14:30")
          ),
          ProgramInfo(
            type = ProgramInfo.Type.NEXT,
            description = LocalizedString.Constant("Off"),
            time = LocalizedString.Constant("at 14:30")
          )
        )
      )
    }
  }
}
