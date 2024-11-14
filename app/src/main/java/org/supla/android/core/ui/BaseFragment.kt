package org.supla.android.core.ui
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

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.FlowPreview
import org.supla.android.MainActivity
import org.supla.android.extensions.visibleIf
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMessageHandler.OnSuplaClientMessageListener
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.ui.AppBar
import org.supla.android.ui.LoadableContent
import org.supla.android.ui.ToolbarItemsClickHandler
import org.supla.android.ui.ToolbarItemsController
import org.supla.android.ui.ToolbarTitleController
import org.supla.android.ui.ToolbarVisibilityController
import java.io.Serializable

abstract class BaseFragment<S : ViewState, E : ViewEvent>(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

  private val suplaMessageListener: OnSuplaClientMessageListener = OnSuplaClientMessageListener { onSuplaMessage(it) }

  constructor() : this(0)

  protected abstract val viewModel: BaseViewModel<S, E>

  protected val viewState: S
    get() = viewModel.getViewState().value

  @OptIn(FlowPreview::class)
  @CallSuper
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    lifecycleScope.launchWhenStarted {
      viewModel.isLoadingEvent().collect {
        (requireActivity() as? LoadableContent)?.getLoadingIndicator()?.visibleIf(it)
      }
    }

    lifecycleScope.launchWhenStarted { viewModel.getViewEvents().collect { event -> handleEvents(event) } }
    lifecycleScope.launchWhenStarted { viewModel.getViewState().collect { state -> handleViewState(state) } }

    viewModel.onViewCreated()
    (activity as? ToolbarVisibilityController)?.setToolbarVisible(getToolbarVisible())
  }

  @CallSuper
  override fun onResume() {
    super.onResume()
    SuplaClientMessageHandler.getGlobalInstance().registerMessageListener(suplaMessageListener)
    if (this is ToolbarItemsClickHandler) {
      (requireActivity() as? MainActivity)?.registerMenuItemClickHandler(this)
    }
  }

  @CallSuper
  override fun onPause() {
    if (this is ToolbarItemsClickHandler) {
      (requireActivity() as? MainActivity)?.unregisterMenuItemClickHandler(this)
    }
    SuplaClientMessageHandler.getGlobalInstance().unregisterMessageListener(suplaMessageListener)
    super.onPause()
  }

  protected abstract fun handleViewState(state: S)

  protected abstract fun handleEvents(event: E)

  protected open fun onSuplaMessage(message: SuplaClientMsg) {
  }

  protected fun setToolbarTitle(title: String) {
    (activity as? ToolbarTitleController)?.setToolbarTitle(AppBar.Title.Text(title))
  }

  protected fun setToolbarTitle(title: AppBar.Title) {
    (activity as? ToolbarTitleController)?.setToolbarTitle(title)
  }

  protected fun setToolbarItemVisible(itemId: Int, visible: Boolean) {
    (activity as? ToolbarItemsController)?.setToolbarItemVisible(itemId, visible)
  }

  protected open fun getToolbarVisible(): Boolean = true

  protected fun <T : Serializable> requireSerializable(key: String, clazz: Class<T>): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      requireArguments().getSerializable(key, clazz)!!
    } else {
      @Suppress("DEPRECATION", "UNCHECKED_CAST")
      requireArguments().getSerializable(key) as T
    }
  }
}
