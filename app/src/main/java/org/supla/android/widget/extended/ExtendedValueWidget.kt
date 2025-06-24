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

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.LocalContext
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
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.di.entrypoints.WidgetConfigurationDaoEntryPoint
import org.supla.android.images.ImageId
import org.supla.android.widget.extended.views.InvalidWidgetContent
import org.supla.android.widget.extended.views.electricitymeter.ElectricityMeterContent
import org.supla.core.shared.data.model.general.SuplaFunction
import java.util.Date

class ExtendedValueWidget : GlanceAppWidget() {

  override val sizeMode: SizeMode = SizeMode.Exact

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
    val DESIRED_SMALL = DpSize(140.dp, 110.dp)
    val DESIRED_MEDIUM = DpSize(270.dp, 110.dp)
    val DESIRED_BIG = DpSize(270.dp, 190.dp)

    val TAG = ExtendedValueWidget::class.simpleName
  }
}

@Composable
@SuppressLint("LocalContextConfigurationRead")
fun DpSize.isMicroLong(): Boolean =
  width / LocalContext.current.widthScaleFactor > 180.dp

@Composable
@SuppressLint("LocalContextConfigurationRead")
fun DpSize.isMin(): Boolean =
  height / LocalContext.current.heightScaleFactor > 65.dp

@Composable
@SuppressLint("LocalContextConfigurationRead")
fun DpSize.isMedium(): Boolean =
  height / LocalContext.current.heightScaleFactor > 65.dp && width / LocalContext.current.widthScaleFactor > 180.dp

@Composable
@SuppressLint("LocalContextConfigurationRead")
fun DpSize.isBig(): Boolean =
  width / LocalContext.current.widthScaleFactor > 250.dp && height / LocalContext.current.heightScaleFactor > 180.dp

private val Context.widthScaleFactor: Float
  get() =
    resources.configuration.fontScale.let { fontScale ->
      if (fontScale > 1) {
        1f + (fontScale - 1) / 2
      } else {
        fontScale
      }
    }

private val Context.heightScaleFactor: Float
  get() =
    resources.configuration.fontScale

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
  value = WidgetValue.ElectricityMeter(
    totalEnergy = WidgetValue.ElectricityMeter.Energy("120 kWh", "130 kWh"),
    mapOf(
      Phase.PHASE_1 to WidgetValue.ElectricityMeter.Energy("100.0 kWh", "40 kWh"),
      Phase.PHASE_2 to WidgetValue.ElectricityMeter.Energy("120.0 kWh", "30 kWh"),
      Phase.PHASE_3 to WidgetValue.ElectricityMeter.Energy("60.0 kWh", "50 kWh")
    )
  ),
  updateTime = Date().time
)

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 180)
@Composable
private fun Preview() {
  SuplaGlanceTheme {
    View(PREVIEW_STATE, ExtendedValueWidget.DESIRED_BIG)
  }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 180, heightDp = 110)
@Composable
private fun Preview_Medium() {
  SuplaGlanceTheme {
    View(PREVIEW_STATE, ExtendedValueWidget.DESIRED_MEDIUM)
  }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 110, heightDp = 110)
@Composable
private fun Preview_Small() {
  SuplaGlanceTheme {
    View(PREVIEW_STATE, ExtendedValueWidget.DESIRED_SMALL)
  }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 110, heightDp = 40)
@Composable
private fun Preview_Micro() {
  SuplaGlanceTheme {
    View(PREVIEW_STATE, DpSize(110.dp, 25.dp))
  }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 220, heightDp = 40)
@Composable
private fun Preview_MicroLong() {
  SuplaGlanceTheme {
    View(PREVIEW_STATE, DpSize(220.dp, 25.dp))
  }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 130, heightDp = 98)
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
      widgetSize = ExtendedValueWidget.DESIRED_SMALL
    )
  }
}
