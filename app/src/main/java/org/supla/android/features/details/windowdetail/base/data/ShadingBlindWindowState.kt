package org.supla.android.features.details.windowdetail.base.data
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
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

abstract class ShadingBlindWindowState : WindowState() {

  abstract val slatTilt: WindowGroupedValue?
  abstract val markers: List<ShadingBlindMarker>
  abstract val tilt0Angle: Float?
  abstract val tilt100Angle: Float?

  val slatTiltDegrees: Float?
    get() {
      val (tilt) = guardLet(slatTilt) { return null }

      val tilt0 = tilt0Angle ?: DEFAULT_TILT_0_ANGLE
      val tilt100 = tilt100Angle ?: DEFAULT_TILT_100_ANGLE

      return tilt.asAngle(tilt0, tilt100)
    }

  val sliderSlatTiltDegrees: Float?
    get() {
      val (tilt) = guardLet(slatTilt) { return null }
      val (tilt0, tilt100) = guardLet(tilt0Angle, tilt100Angle) { return null }

      return tilt.asAngle(tilt0, tilt100)
    }

  fun slatTiltText(): LocalizedString {
    val (tilt) = guardLet(slatTilt) {
      return localizedString(R.string.facade_blinds_no_tilt)
    }
    return LocalizedString.Constant(tilt.asString(tiltTextFormat, tilt0Angle, tilt100Angle))
  }

  companion object {
    const val DEFAULT_TILT_0_ANGLE = 0f
    const val DEFAULT_TILT_100_ANGLE = 180f

    val tiltTextFormat: WindowGroupedValueFormat = WindowGroupedValueFormat.DEGREE
  }
}
