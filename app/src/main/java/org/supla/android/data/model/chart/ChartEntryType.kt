package org.supla.android.data.model.chart

import com.github.mikephil.charting.components.YAxis.AxisDependency

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

enum class ChartEntryType(private val axisDependency: AxisDependency) {
  TEMPERATURE(AxisDependency.LEFT),
  HUMIDITY(AxisDependency.RIGHT),
  HUMIDITY_ONLY(AxisDependency.LEFT),
  GENERAL_PURPOSE_MEASUREMENT(AxisDependency.LEFT),
  GENERAL_PURPOSE_METER(AxisDependency.LEFT),
  IMPULSE_COUNTER(AxisDependency.LEFT),
  ELECTRICITY(AxisDependency.LEFT),
  VOLTAGE(AxisDependency.LEFT),
  CURRENT(AxisDependency.LEFT),
  POWER_ACTIVE(AxisDependency.LEFT);

  fun leftAxis() = axisDependency == AxisDependency.LEFT
  fun rightAxis() = axisDependency == AxisDependency.RIGHT
}
