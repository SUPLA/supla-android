package org.supla.android.features.details.containerdetail.general.ui
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

import androidx.compose.ui.graphics.Color

sealed class ControlLevel {
  abstract val level: Float
  abstract val levelString: String
  abstract val type: Type
  abstract val color: Color

  enum class Type {
    UPPER, LOWER
  }
}

data class ErrorLevel(
  override val level: Float,
  override val levelString: String,
  override val type: Type
) : ControlLevel() {
  override val color: Color
    get() = Color(0xFFEB3A28)
}

data class WarningLevel(
  override val level: Float,
  override val levelString: String,
  override val type: Type
) : ControlLevel() {
  override val color: Color
    get() = Color(0xFFE3A400)
}
