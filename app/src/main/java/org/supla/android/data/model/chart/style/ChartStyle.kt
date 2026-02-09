package org.supla.android.data.model.chart.style
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

import android.content.Context
import androidx.annotation.ColorRes
import org.supla.android.R
import org.supla.android.ui.views.charts.marker.BaseMarkerView
import org.supla.android.ui.views.charts.marker.ChartMarkerView
import org.supla.android.ui.views.charts.marker.ElectricityMarkerView
import org.supla.android.ui.views.charts.marker.ImpulseCounterMarkerView

sealed class ChartStyle(
  @ColorRes val leftAxisColor: Int,
  @ColorRes val rightAxisColor: Int,
  val drawBarShadow: Boolean,
  val markerViewProvider: (context: Context) -> BaseMarkerView,
  val setMinValue: Boolean = true,
  val setMaxValue: Boolean = true
)

data object TemperatureChartStyle : ChartStyle(
  leftAxisColor = R.color.dark_red,
  rightAxisColor = R.color.dark_blue,
  drawBarShadow = false,
  markerViewProvider = { ChartMarkerView(it) }
)

data object HumidityChartStyle : ChartStyle(
  leftAxisColor = R.color.dark_blue,
  rightAxisColor = R.color.dark_blue,
  drawBarShadow = false,
  markerViewProvider = { ChartMarkerView(it) }
)

data object GpmChartStyle : ChartStyle(
  leftAxisColor = R.color.on_background,
  rightAxisColor = R.color.on_background,
  drawBarShadow = true,
  markerViewProvider = { ChartMarkerView(it) },
  setMinValue = false,
  setMaxValue = false
)

data object ElectricityChartStyle : ChartStyle(
  leftAxisColor = R.color.on_background,
  rightAxisColor = R.color.on_background,
  drawBarShadow = true,
  markerViewProvider = { ElectricityMarkerView(it) }
)

data object ElectricityHistoryChartStyle : ChartStyle(
  leftAxisColor = R.color.on_background,
  rightAxisColor = R.color.on_background,
  drawBarShadow = true,
  markerViewProvider = { ChartMarkerView(it) },
  setMinValue = false,
  setMaxValue = false
)

data object ImpulseCounterChartStyle : ChartStyle(
  leftAxisColor = R.color.on_background,
  rightAxisColor = R.color.on_background,
  drawBarShadow = true,
  markerViewProvider = { ImpulseCounterMarkerView(it) }
)
