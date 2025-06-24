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
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalGlanceId
import androidx.glance.action.clickable
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.work.WorkManager
import org.supla.android.R
import org.supla.android.features.widget.shared.GlanceTypography
import org.supla.android.images.ImageId
import org.supla.android.widget.extended.ExtendedValueWidgetWorker

@Composable
fun InvalidWidgetContent(widgetSize: DpSize, stringRes: Int) {
  val context = LocalContext.current
  val glanceId = LocalGlanceId.current

  Scaffold(
    titleBar = {
      WidgetTopBar(
        icon = ImageId(R.drawable.splash_logo),
        caption = LocalContext.current.getString(R.string.app_name),
        widgetSize = widgetSize
      )
    },
    backgroundColor = GlanceTheme.colors.background,
    modifier = GlanceModifier
      .clickable { ExtendedValueWidgetWorker.singleRun(WorkManager.getInstance(context), glanceId) },
  ) {
    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(
        text = LocalContext.current.getString(stringRes),
        style = GlanceTypography.bodyMedium.copy(textAlign = TextAlign.Center),
      )
    }
  }
}
