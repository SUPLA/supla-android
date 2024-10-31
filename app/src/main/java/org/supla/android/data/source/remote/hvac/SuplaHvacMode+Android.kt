package org.supla.android.data.source.remote.hvac
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

import org.supla.android.R

val SuplaHvacMode.icon: Int?
  get() {
    return when (this) {
      SuplaHvacMode.OFF -> R.drawable.ic_power_button
      SuplaHvacMode.HEAT -> R.drawable.ic_heat
      SuplaHvacMode.COOL -> R.drawable.ic_cool
      else -> null
    }
  }

val SuplaHvacMode.iconColor: Int?
  get() {
    return when (this) {
      SuplaHvacMode.OFF -> R.color.on_surface_variant
      SuplaHvacMode.HEAT -> R.color.red
      SuplaHvacMode.COOL -> R.color.blue
      else -> null
    }
  }
