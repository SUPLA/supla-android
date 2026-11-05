package org.supla.android.main.topbar
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

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.ui.views.buttons.IconButton
import org.supla.android.ui.views.texts.BodyMedium

private const val MAX_ICONS_COUNT = 2

enum class TopBarIcon(
  @DrawableRes val iconRes: Int,
  @StringRes val descriptionRes: Int,
  @ColorRes val tintRes: Int? = null
) {
  OPEN_OCR(R.drawable.ic_ocr_photo, R.string.toolbar_ocr),
  OPEN_SETTINGS(R.drawable.ic_settings, R.string.settings),
  PROFILES(R.drawable.profile_unselected, R.string.profile_plural)
}

@Composable
fun TopBarActions(actions: List<TopBarAction>, searchIconVisible: Boolean) {
  val layout = layoutTopBarActions(actions, searchIconVisible)

  layout.icons.forEach { action ->
    TopBarActionIcon(action)
  }

  if (layout.overflow.isNotEmpty()) {
    TopBarOverflow(layout.overflow)
  }
}

@Composable
private fun TopBarActionIcon(action: TopBarAction) {
  val icon = requireNotNull(action.icon)
  val controller = LocalTopBarController.current
  val scope = rememberCoroutineScope()

  IconButton(
    icon = icon.iconRes,
    onClick = { scope.launch { controller.emit(action.event) } },
    contentDescription = stringResource(icon.descriptionRes),
    modifier = Modifier.size(40.dp),
    tint = icon.tintRes?.let { colorResource(it) } ?: MaterialTheme.colorScheme.onPrimaryContainer
  )
}

@Composable
private fun TopBarOverflow(actions: List<TopBarAction>) {
  var expanded by remember { mutableStateOf(false) }
  val controller = LocalTopBarController.current
  val scope = rememberCoroutineScope()

  Box {
    IconButton(
      icon = Icons.Default.MoreVert,
      onClick = { expanded = true },
      modifier = Modifier.size(40.dp),
      tint = MaterialTheme.colorScheme.onPrimaryContainer
    )

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      actions.forEach { action ->
        DropdownMenuItem(
          text = { BodyMedium(action.titleRes) },
          onClick = {
            expanded = false
            scope.launch { controller.emit(action.event) }
          }
        )
      }
    }
  }
}

data class TopBarActionLayout(
  val icons: List<TopBarAction>,
  val overflow: List<TopBarAction>
)

fun layoutTopBarActions(actions: List<TopBarAction>, searchIconVisible: Boolean): TopBarActionLayout {
  val allIcons = actions.filter { it.displayMode == TopBarDisplayMode.AUTO }
  val hasOverflowOnlyAction = actions.any { it.displayMode == TopBarDisplayMode.OVERFLOW_ONLY }

  if (searchIconVisible && hasOverflowOnlyAction) {
    return TopBarActionLayout(icons = emptyList(), overflow = actions)
  } else if (searchIconVisible && actions.size > 1) {
    return TopBarActionLayout(icons = emptyList(), overflow = actions)
  } else if (hasOverflowOnlyAction) {
    val icon = allIcons.firstOrNull()

    return TopBarActionLayout(
      icons = listOfNotNull(icon),
      overflow = actions.filterNot { it::class == icon?.let { selected -> selected::class } }
    )
  } else {
    val icons = allIcons.take(MAX_ICONS_COUNT)
    val iconIds = icons.mapTo(mutableSetOf()) { it::class }

    return TopBarActionLayout(
      icons = icons,
      overflow = actions.filterNot { it::class in iconIds }
    )
  }
}
