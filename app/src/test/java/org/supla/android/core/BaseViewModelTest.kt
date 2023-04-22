package org.supla.android.core

import androidx.annotation.CallSuper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.tools.SuplaSchedulers

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseViewModelTest<S : ViewState, E : ViewEvent> {

  protected abstract val schedulers: SuplaSchedulers
  protected abstract val viewModel: BaseViewModel<S, E>

  protected val states = mutableListOf<S>()
  protected val events = mutableListOf<E>()

  @CallSuper
  open fun setUp() {
    states.clear()
    events.clear()

    viewModel.getViewState()
      .drop(1) // skip first - default state
      .onEach(states::add)
      .launchIn(CoroutineScope(UnconfinedTestDispatcher(TestCoroutineScheduler())))

    viewModel.getViewEvents()
      .onEach(events::add)
      .launchIn(CoroutineScope(UnconfinedTestDispatcher(TestCoroutineScheduler())))
  }
}