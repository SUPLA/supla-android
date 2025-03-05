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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ContainerType {
  DEFAULT,
  WATER,
  SEPTIC;

  @Composable
  fun primary(): Color {
    return when (this) {
      DEFAULT -> MaterialTheme.colorScheme.primaryContainer
      WATER -> MaterialTheme.colorScheme.secondary
      SEPTIC -> MaterialTheme.colorScheme.tertiary
    }
  }

  fun secondary(): Color {
    return when (this) {
      DEFAULT -> Color(0xFF0E8618)
      WATER -> Color(0xFF0067D4)
      SEPTIC -> Color(0xFF8A4F07)
    }
  }
}
