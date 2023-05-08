package org.supla.android.core.ui

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import org.supla.android.Trace
import org.supla.android.extensions.TAG
import org.supla.android.tools.SuplaSchedulers

abstract class BaseViewModel<S : ViewState, E : ViewEvent> constructor(
  defaultState: S,
  private val schedulers: SuplaSchedulers
) : ViewModel() {

  private val viewState: MutableStateFlow<S> = MutableStateFlow(defaultState)
  fun getViewState(): StateFlow<S> = viewState

  private val viewEvents: MutableSharedFlow<Event<E?>> =
    MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  fun getViewEvents(): Flow<E> = viewEvents
    .filter { it.item != null }
    .filter { it.processed.not() }
    .map {
      it.run {
        it.processed = true
        it.item!!
      }
    }

  @FlowPreview
  fun isLoadingEvent(): Flow<Boolean> = viewState
    .map { it.loading }
    .distinctUntilChanged()
    .debounce(timeoutMillis = 100)

  private val compositeDisposable = CompositeDisposable()

  override fun onCleared() {
    compositeDisposable.clear()
  }

  protected fun sendEvent(event: E) {
    viewEvents.tryEmit(Event(event))
  }

  protected fun updateState(updater: (S) -> S) {
    viewState.tryEmit(updater(viewState.value))
  }

  protected fun currentState(): S {
    return viewState.value
  }

  protected abstract fun loadingState(isLoading: Boolean): S

  fun Disposable.disposeBySelf() {
    compositeDisposable.add(this)
  }

  fun <T> Maybe<T>.attach(): Maybe<T> {
    val calledAt = findStackEntryString(Thread.currentThread().stackTrace)

    return subscribeOn(schedulers.io)
      .doOnError { Trace.e(TAG, "Maybe called at '$calledAt' failed with ${it.message}", it) }
      .doOnSubscribe { updateState { loadingState(true) } }
      .doOnTerminate { updateState { loadingState(false) } }
  }

  fun Completable.attach(): Completable {
    val calledAt = findStackEntryString(Thread.currentThread().stackTrace)

    return subscribeOn(schedulers.io)
      .doOnError { Trace.e(TAG, "Completable called at '$calledAt' failed with ${it.message}", it) }
      .doOnSubscribe { updateState { loadingState(true) } }
      .doOnTerminate { updateState { loadingState(false) } }
  }

  fun <T : Any> Single<T>.attach(): Single<T> {
    val calledAt = findStackEntryString(Thread.currentThread().stackTrace)

    return subscribeOn(schedulers.io)
      .doOnError { Trace.e(TAG, "Single called at '$calledAt' failed with ${it.message}", it) }
      .doOnSubscribe { updateState { loadingState(true) } }
      .doOnTerminate { updateState { loadingState(false) } }
  }

  fun <T : Any> Observable<T>.attach(): Observable<T> {
    val calledAt = findStackEntryString(Thread.currentThread().stackTrace)

    return subscribeOn(schedulers.io)
      .doOnError { Trace.e(TAG, "Single called at '$calledAt' failed with ${it.message}", it) }
      .doOnSubscribe { updateState { loadingState(true) } }
      .doOnTerminate { updateState { loadingState(false) } }
  }

  private fun findStackEntryString(stack: Array<StackTraceElement>): String? {
    for (entry in stack) {
      entry.toString().run {
        if (startsWith("org.supla") && contains(".BaseViewModel").not()) {
          return this
        }
      }
    }

    return null
  }

  data class Event<T>(
    val item: T,
    var processed: Boolean = false
  )
}
