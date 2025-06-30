package org.supla.android.ui
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

import android.view.MenuItem
import androidx.annotation.ColorRes
import org.supla.android.R

interface ToolbarTitleController {
  fun setToolbarTitle(title: AppBar.Title)
}

interface ToolbarItemsController {
  fun setToolbarItemVisible(itemId: Int, visible: Boolean)
}

interface ToolbarVisibilityController {
  fun setToolbarVisible(visibility: ToolbarVisibility)

  data class ToolbarVisibility(
    val visible: Boolean,
    @ColorRes val toolbarColorRes: Int = if (visible) R.color.primary_container else R.color.background,
    @ColorRes val navigationBarColorRes: Int = R.color.surface,
    val isLight: Boolean = visible.not(),
    val shadowVisible: Boolean = true
  )
}

interface ToolbarItemsClickHandler {
  fun onMenuItemClick(menuItem: MenuItem): Boolean
}
