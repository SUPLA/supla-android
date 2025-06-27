package org.supla.android.ui

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
    val isLight: Boolean = visible.not()
  )
}

interface ToolbarItemsClickHandler {
  fun onMenuItemClick(menuItem: MenuItem): Boolean
}
