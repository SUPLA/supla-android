package org.supla.android.widget.extended.views
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import org.supla.android.features.widget.shared.GlanceDistance
import org.supla.android.features.widget.shared.GlanceTypography
import org.supla.android.images.ImageId
import org.supla.android.ui.glance.Image
import org.supla.android.widget.extended.isBig
import org.supla.android.widget.extended.isMedium
import org.supla.android.widget.extended.isMin

@Composable
fun WidgetTopBar(icon: ImageId, caption: String, widgetSize: DpSize) =
  when {
    widgetSize.isBig -> WidgetTopBarBig(icon, caption)
    widgetSize.isMedium -> WidgetTopBarMedium(icon, caption)
    widgetSize.isMin -> WidgetTopBarSmall(caption)
    else -> WidgetTopBarMicro(caption)
  }

@Composable
fun WidgetTopBarBig(icon: ImageId, caption: String) =
  Row(
    verticalAlignment = Alignment.Vertical.CenterVertically,
    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    modifier = GlanceModifier.padding(4.dp).fillMaxWidth()
  ) {
    Image(
      imageId = icon,
      modifier = GlanceModifier.size(24.dp)
    )
    Spacer(modifier = GlanceModifier.width(GlanceDistance.tiny))
    Text(
      text = caption,
      style = GlanceTypography.bodyMedium
    )
  }

@Composable
fun WidgetTopBarMedium(icon: ImageId, caption: String) =
  Row(
    verticalAlignment = Alignment.Vertical.CenterVertically,
    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    modifier = GlanceModifier.padding(2.dp).fillMaxWidth()
  ) {
    Image(
      imageId = icon,
      modifier = GlanceModifier.size(16.dp)
    )
    Spacer(modifier = GlanceModifier.width(4.dp))
    Text(
      text = caption,
      style = GlanceTypography.bodyMedium
    )
  }

@Composable
fun WidgetTopBarSmall(caption: String) =
  Text(
    text = caption,
    style = GlanceTypography.bodyMedium.copy(textAlign = TextAlign.Center),
    maxLines = 1,
    modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp)
  )

@Composable
fun WidgetTopBarMicro(caption: String) =
  Text(
    text = caption,
    style = GlanceTypography.bodySmall.copy(textAlign = TextAlign.Center),
    maxLines = 1,
    modifier = GlanceModifier.fillMaxWidth()
  )
