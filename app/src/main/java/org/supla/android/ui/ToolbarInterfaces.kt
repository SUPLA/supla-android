package org.supla.android.ui

import android.view.MenuItem

interface ToolbarTitleController {
  fun setToolbarTitle(title: String)
}

interface ToolbarItemsController {
  fun setToolbarItemVisible(itemId: Int, visible: Boolean)
}

interface ToolbarVisibilityController {
  fun setToolbarVisible(visible: Boolean)
}

interface ToolbarItemsClickHandler {
  fun onMenuItemClick(menuItem: MenuItem): Boolean
}
