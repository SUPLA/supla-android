package org.supla.android.widget.extended.views.electricitymeter
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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.work.WorkManager
import org.supla.android.R
import org.supla.android.features.widget.shared.GlanceDistance
import org.supla.android.features.widget.shared.GlanceTypography
import org.supla.android.widget.extended.ExtendedValueWidgetState
import org.supla.android.widget.extended.ExtendedValueWidgetWorker
import org.supla.android.widget.extended.WidgetValue
import org.supla.android.widget.extended.isBig
import org.supla.android.widget.extended.isMedium
import org.supla.android.widget.extended.isMicroLong
import org.supla.android.widget.extended.isMin
import org.supla.android.widget.extended.views.WidgetTopBar
import java.util.Date

@Composable
fun ElectricityMeterContent(state: ExtendedValueWidgetState, widgetSize: DpSize) {
  val context = LocalContext.current
  val glanceId = LocalGlanceId.current
  Scaffold(
    titleBar = {
      WidgetTopBar(
        icon = state.icon,
        caption = state.caption,
        widgetSize = widgetSize
      )
    },
    backgroundColor = GlanceTheme.colors.background,
    modifier = GlanceModifier.fillMaxSize()
      .clickable { ExtendedValueWidgetWorker.singleRun(WorkManager.getInstance(context), glanceId) }
  ) {
    when (state.value) {
      is WidgetValue.ElectricityMeter ->
        when {
          widgetSize.isBig() -> ElectricityBigView(state.value, Date(state.updateTime))
          widgetSize.isMedium() -> ElectricityMediumView(state.value, Date(state.updateTime))
          widgetSize.isMin() -> ElectricitySmallView(state.value, Date(state.updateTime))
          widgetSize.isMicroLong() -> ElectricityMicroLongView(state.icon, state.value)
          else -> ElectricityMicroView(state.value)
        }

      is WidgetValue.Offline -> Offline()
      else -> NoValue()
    }
  }
}

@Composable
private fun NoValue() =
  Text(
    text = LocalContext.current.getString(R.string.widget_no_value),
    style = GlanceTypography.bodyMedium.copy(textAlign = TextAlign.Center),
    modifier = GlanceModifier.fillMaxWidth().padding(GlanceDistance.tiny)
  )

@Composable
private fun Offline() =
  Text(
    text = LocalContext.current.getString(R.string.channel_offline),
    style = GlanceTypography.bodyMedium.copy(textAlign = TextAlign.Center),
    modifier = GlanceModifier.fillMaxWidth().padding(GlanceDistance.tiny)
  )
