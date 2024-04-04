package org.supla.android.features.details.windowdetail.base.data.facadeblinds
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
import org.supla.android.core.ui.StringProvider
import org.supla.android.extensions.guardLet
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValueFormat
import org.supla.android.features.details.windowdetail.base.data.WindowState

data class FacadeBlindWindowState(
  /**
   * The whole blind roller position in percentage
   * 0 - open
   * 100 - closed
   */
  override val position: WindowGroupedValue,

  /**
   * Slat tilt as percentage - 0 up to 100
   */
  val slatTilt: WindowGroupedValue? = null,

  val tilt0Angle: Float? = null,

  val tilt100Angle: Float? = null,

  /**
   * Used for groups - shows positions of single roller shutter
   */
  val markers: List<FacadeBlindMarker> = emptyList(),

  override val positionTextFormat: WindowGroupedValueFormat = WindowGroupedValueFormat.PERCENTAGE,
  val tiltTextFormat: WindowGroupedValueFormat = WindowGroupedValueFormat.DEGREE
) : WindowState() {

  override fun updating(position: WindowGroupedValue): WindowState = copy(position = position, markers = emptyList())

  val slatTiltDegrees: Float?
    get() {
      val (tilt) = guardLet(slatTilt) { return null }

      val tilt0 = tilt0Angle ?: DEFAULT_TILT_0_ANGLE
      val tilt100 = tilt100Angle ?: DEFAULT_TILT_100_ANGLE

      return tilt.asAngle(tilt0, tilt100)
    }

  fun slatTiltText(): StringProvider {
    val (tilt) = guardLet(slatTilt) {
      return { it.getString(R.string.facade_blinds_no_tilt) }
    }
    return { tilt.asString(tiltTextFormat, tilt0Angle, tilt100Angle) }
  }

  companion object {
    const val DEFAULT_TILT_0_ANGLE = 0f
    const val DEFAULT_TILT_100_ANGLE = 180f
  }
}
