package org.supla.android.core.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import org.supla.android.MainActivity
import org.supla.android.extensions.visibleIf
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMessageHandler.OnSuplaClientMessageListener
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.ui.LoadableContent
import org.supla.android.ui.ToolbarItemsClickHandler
import org.supla.android.ui.ToolbarItemsController
import org.supla.android.ui.ToolbarTitleController

abstract class BaseFragment<S : ViewState, E : ViewEvent>(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

  private val suplaMessageListener: OnSuplaClientMessageListener = OnSuplaClientMessageListener { onSuplaMessage(it) }

  constructor() : this(0)

  protected abstract fun getViewModel(): BaseViewModel<S, E>

  @CallSuper
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    lifecycleScope.launchWhenStarted {
      getViewModel().isLoadingEvent().collect {
        (requireActivity() as? LoadableContent)?.getLoadingIndicator()?.visibleIf(it)
      }
    }

    lifecycleScope.launchWhenStarted { getViewModel().getViewEvents().collect { event -> handleEvents(event) } }
    lifecycleScope.launchWhenStarted { getViewModel().getViewState().collect { state -> handleViewState(state) } }

    getViewModel().onViewCreated()
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
    (requireActivity() as? ToolbarTitleController)?.setToolbarTitle(title)
  }

  protected fun setToolbarItemVisible(itemId: Int, visible: Boolean) {
    (requireActivity() as? ToolbarItemsController)?.setToolbarItemVisible(itemId, visible)
  }
}
