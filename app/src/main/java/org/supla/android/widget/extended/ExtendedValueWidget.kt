package org.supla.android.widget.extended
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

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.coroutineScope
import org.supla.android.R
import org.supla.android.Trace
import org.supla.android.core.ui.theme.SuplaGlanceTheme
import org.supla.android.di.entrypoints.WidgetConfigurationDaoEntryPoint
import org.supla.android.images.ImageId
import org.supla.android.widget.extended.views.InvalidWidgetContent
import org.supla.android.widget.extended.views.electricitymeter.ElectricityMeterContent
import org.supla.core.shared.data.model.general.SuplaFunction
import java.util.Date

class ExtendedValueWidget : GlanceAppWidget() {

  override val sizeMode: SizeMode = SizeMode.Responsive(
    setOf(SMALL, MEDIUM, BIG)
  )

  override val stateDefinition = ExtendedValueWidgetStateDefinition

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    coroutineScope {
      provideContent {
        SuplaGlanceTheme {
          View(
            state = currentState(),
            widgetSize = LocalSize.current
          )
        }
      }
    }
  }

  override suspend fun onDelete(context: Context, glanceId: GlanceId) {
    super.onDelete(context, glanceId)

    val widgetConfigurationDao =
      EntryPointAccessors.fromApplication(
        context,
        WidgetConfigurationDaoEntryPoint::class.java
      ).provideWidgetConfigurationDao()

    widgetConfigurationDao.deleteBy(glanceId = glanceId.toString())
  }

  companion object {
    val SMALL = DpSize(160.dp, 90.dp)
    val MEDIUM = DpSize(200.dp, 90.dp)
    val BIG = DpSize(250.dp, 210.dp)

    val TAG = ExtendedValueWidget::class.simpleName
  }
}

@Composable
private fun View(state: ExtendedValueWidgetState, widgetSize: DpSize) {
  when (state.function) {
    SuplaFunction.ELECTRICITY_METER -> ElectricityMeterContent(state, widgetSize)
    else -> {
      InvalidWidgetContent(widgetSize, R.string.widget_not_supported)
    }
  }
}

fun intent(context: Context, intentAction: String, widgetId: Int): Intent =
  intent(context, intentAction, intArrayOf(widgetId))

fun intent(context: Context, intentAction: String, widgetIds: IntArray): Intent {
  Trace.d(ExtendedValueWidget::javaClass.name, "Creating intent with action: $intentAction")
  return Intent(context, ExtendedValueWidget::class.java).apply {
    action = intentAction
    flags = Intent.FLAG_RECEIVER_FOREGROUND
    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
  }
}

private val PREVIEW_STATE = ExtendedValueWidgetState(
  icon = ImageId(R.drawable.fnc_electricity_meter),
  caption = "Electricity Meter",
  function = SuplaFunction.ELECTRICITY_METER,
  value = WidgetValue.Empty,
  updateTime = Date().time
)

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 258, heightDp = 213)
@Composable
private fun Preview() {
  SuplaGlanceTheme {
    View(PREVIEW_STATE, ExtendedValueWidget.BIG)
  }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 258, heightDp = 100)
@Composable
private fun Preview_Medium() {
  SuplaGlanceTheme {
    View(PREVIEW_STATE, ExtendedValueWidget.MEDIUM)
  }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 167, heightDp = 98)
@Composable
private fun Preview_Small() {
  SuplaGlanceTheme {
    View(PREVIEW_STATE, ExtendedValueWidget.SMALL)
  }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 167, heightDp = 98)
@Composable
private fun Preview_Invalid() {
  SuplaGlanceTheme {
    View(
      state = ExtendedValueWidgetState(
        ImageId(R.drawable.logo),
        "",
        SuplaFunction.NONE,
        WidgetValue.Empty,
        Date().time
      ),
      widgetSize = ExtendedValueWidget.MEDIUM
    )
  }
}
