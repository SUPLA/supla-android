package org.supla.android.ui.views.buttons
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults
import org.supla.android.ui.views.icons.LockIcon
import org.supla.android.ui.views.icons.LockOpenIcon
import org.supla.android.ui.views.icons.LockTemporaryIcon
import org.supla.core.shared.extensions.forTrue

enum class LockIconType {
  CLOSED, OPENED, TEMPORARY_CLOSED;

  @Composable
  fun Icon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
  ) {
    when (this) {
      CLOSED -> LockIcon(modifier = modifier, color = color)
      OPENED -> LockOpenIcon(modifier = modifier, color = color)
      TEMPORARY_CLOSED -> LockTemporaryIcon(modifier = modifier, color = color)
    }
  }
}

@Composable
fun LockSuplaButton(
  modifier: Modifier = Modifier,
  disabled: Boolean = false,
  pressed: Boolean = false,
  type: LockIconType = LockIconType.CLOSED,
  onClick: () -> Unit
) {
  val colorDisabled = MaterialTheme.colorScheme.outline
  SuplaButton(
    onClick = onClick,
    modifier = modifier,
    disabled = disabled,
    active = pressed,
    colors = SuplaButtonDefaults.primaryColors(borderDisabledAndPressed = MaterialTheme.colorScheme.primary),
    shape = SuplaButtonDefaults.allRoundedShape()
  ) { color ->
    type.Icon(
      color = disabled.forTrue { colorDisabled } ?: color,
      modifier = Modifier.align(Alignment.Center)
    )
  }
}
