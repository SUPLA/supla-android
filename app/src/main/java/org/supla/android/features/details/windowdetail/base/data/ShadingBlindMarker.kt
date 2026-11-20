package org.supla.android.features.details.windowdetail.base.data

import org.supla.core.shared.extensions.guardLet

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

data class ShadingBlindMarker(
  val position: Float,
  val tiltValue: Float?,
  val tilt0Angle: Float? = null,
  val tilt100Angle: Float? = null
) {
  val tilt: Float?
    get() {
      val (tiltValue, tilt0, tilt100) = guardLet(tiltValue, tilt0Angle, tilt100Angle) { return null }
      return tiltValue.angleBetween(tilt0, tilt100)
    }
}
