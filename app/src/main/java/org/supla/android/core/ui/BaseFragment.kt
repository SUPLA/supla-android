package org.supla.android.core.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope

abstract class BaseFragment<S : ViewState, E : ViewEvent>(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

  protected abstract fun getViewModel(): BaseViewModel<S, E>

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    lifecycleScope.launchWhenStarted {
      getViewModel().isLoadingEvent().collect { isLoading ->
        (requireActivity() as BaseActivity).showLoading(isLoading)
      }
    }

    lifecycleScope.launchWhenStarted { getViewModel().getViewEvents().collect { event -> handleEvents(event) } }
    lifecycleScope.launchWhenStarted { getViewModel().getViewState().collect { state -> handleViewState(state) } }
  }

  protected abstract fun handleViewState(state: S)

  protected abstract fun handleEvents(event: E)
}
