package org.supla.android.core
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

import androidx.annotation.CallSuper
import io.mockk.every
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.mockito.kotlin.whenever
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.testhelpers.StdoutTree
import org.supla.android.tools.SuplaSchedulers
import timber.log.Timber
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseViewModelTest<S : ViewState, E : ViewEvent, VM : BaseViewModel<S, E>>(
  private val mockSchedulers: MockSchedulers = MockSchedulers.MOCK
) {

  protected abstract val schedulers: SuplaSchedulers
  protected abstract val viewModel: VM

  protected val states = mutableListOf<S>()
  protected val events = mutableListOf<E>()

  protected val testScheduler = TestScheduler()

  @CallSuper
  open fun setUp() {
    Timber.uprootAll()
    Timber.plant(StdoutTree())
    states.clear()
    events.clear()

    when (mockSchedulers) {
      MockSchedulers.MOCK -> {
        whenever(schedulers.io).thenReturn(Schedulers.trampoline())
        whenever(schedulers.ui).thenReturn(Schedulers.trampoline())
      }

      MockSchedulers.MOCKK -> {
        every { schedulers.io } returns Schedulers.trampoline()
        every { schedulers.ui } returns Schedulers.trampoline()
      }

      MockSchedulers.NONE -> {} // No mocks
    }

    viewModel.getViewState()
      .drop(1) // skip first - default state
      .onEach(states::add)
      .launchIn(CoroutineScope(UnconfinedTestDispatcher(TestCoroutineScheduler())))

    viewModel.getViewEvents()
      .onEach(events::add)
      .launchIn(CoroutineScope(UnconfinedTestDispatcher(TestCoroutineScheduler())))
  }

  enum class MockSchedulers {
    MOCK, MOCKK, NONE
  }

  @Suppress("UNCHECKED_CAST")
  fun BaseViewModel<S, E>.setState(state: S) {
    BaseViewModel::class.memberProperties
      .find { it.name == "viewState" }
      ?.let {
        it.isAccessible = true
        val viewState = it.getter.call(this) as MutableStateFlow<S>
        viewState.tryEmit(state)
      }
  }
}
