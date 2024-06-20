package org.supla.android.features.details.windowdetail.base.ui
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

sealed interface ShadingSystemAction {
  object Open : ShadingSystemAction
  object Close : ShadingSystemAction
  object Stop : ShadingSystemAction
  object MoveUp : ShadingSystemAction
  object MoveDown : ShadingSystemAction
  object Calibrate : ShadingSystemAction

  data class OpenAt(val position: Float) : ShadingSystemAction
  data class MoveTo(val position: Float) : ShadingSystemAction
  data class TiltTo(val tilt: Float) : ShadingSystemAction
  data class TiltSetTo(val tilt: Float) : ShadingSystemAction
  data class MoveAndTiltTo(val position: Float, val tilt: Float) : ShadingSystemAction
  data class MoveAndTiltSetTo(val position: Float, val tilt: Float) : ShadingSystemAction
}
