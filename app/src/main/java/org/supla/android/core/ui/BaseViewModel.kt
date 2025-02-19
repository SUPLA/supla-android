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

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.supla.android.Trace
import org.supla.android.extensions.TAG
import org.supla.android.tools.SuplaSchedulers

interface BaseViewProxy<S : ViewState> {
  fun getViewState(): StateFlow<S>
}

abstract class BaseViewModel<S : ViewState, E : ViewEvent>(
  defaultState: S,
  private val schedulers: SuplaSchedulers
) : ViewModel() {

  private val loadingState: MutableStateFlow<Boolean> = MutableStateFlow(false)
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
  fun isLoadingEvent(): Flow<Boolean> = loadingState
    .map { it }
    .distinctUntilChanged()
    .debounce(timeoutMillis = 350)

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

  protected open fun setLoading(loading: Boolean) {
    throw IllegalStateException("Using `attachLoadable()` needs to override this method!")
  }

  open fun onViewCreated() {}

  fun Disposable.disposeBySelf() {
    compositeDisposable.add(this)
  }

  fun handle(disposable: Disposable) {
    compositeDisposable.add(disposable)
  }

  fun <T : Any> Maybe<T>.attach(): Maybe<T> {
    return attachSilent()
      .doOnSubscribe { loadingState.tryEmit(true) }
      .doOnTerminate { loadingState.tryEmit(false) }
  }

  fun <T : Any> Maybe<T>.attachSilent(): Maybe<T> {
    val calledAt = findStackEntryString(Thread.currentThread().stackTrace)

    return subscribeOn(schedulers.io)
      .observeOn(schedulers.ui)
      .doOnError { Trace.e(TAG, errorMessage("Maybe", calledAt, it.message), it) }
  }

  fun Completable.attach(): Completable {
    val calledAt = findStackEntryString(Thread.currentThread().stackTrace)

    return subscribeOn(schedulers.io)
      .observeOn(schedulers.ui)
      .doOnError { Trace.e(TAG, errorMessage("Completable", calledAt, it.message), it) }
      .doOnSubscribe { loadingState.tryEmit(true) }
      .doOnTerminate { loadingState.tryEmit(false) }
  }

  fun Completable.attachSilent(): Completable {
    val calledAt = findStackEntryString(Thread.currentThread().stackTrace)

    return subscribeOn(schedulers.io)
      .observeOn(schedulers.ui)
      .doOnError { Trace.e(TAG, errorMessage("Completable", calledAt, it.message), it) }
  }

  fun <T : Any> Single<T>.attach(): Single<T> {
    val calledAt = findStackEntryString(Thread.currentThread().stackTrace)

    return subscribeOn(schedulers.io)
      .observeOn(schedulers.ui)
      .doOnError { Trace.e(TAG, errorMessage("Single", calledAt, it.message), it) }
      .doOnSubscribe { loadingState.tryEmit(true) }
      .doOnTerminate { loadingState.tryEmit(false) }
  }

  fun <T : Any> Single<T>.attachSilent(): Single<T> {
    val calledAt = findStackEntryString(Thread.currentThread().stackTrace)

    return subscribeOn(schedulers.io)
      .observeOn(schedulers.ui)
      .doOnError { Trace.e(TAG, errorMessage("Maybe", calledAt, it.message), it) }
  }

  fun <T : Any> Observable<T>.attach(): Observable<T> {
    return attachSilent()
      .doOnSubscribe { loadingState.tryEmit(true) }
      .doOnTerminate { loadingState.tryEmit(false) }
      .doOnNext { loadingState.tryEmit(false) }
  }

  fun <T : Any> Observable<T>.attachSilent(): Observable<T> {
    val calledAt = findStackEntryString(Thread.currentThread().stackTrace)

    return subscribeOn(schedulers.io)
      .observeOn(schedulers.ui)
      .doOnError { Trace.e(TAG, errorMessage("Observable", calledAt, it.message), it) }
  }

  fun <T : Any> Maybe<T>.attachLoadable(): Maybe<T> {
    return attachSilent()
      .doOnSubscribe { setLoading(true) }
      .doOnTerminate { setLoading(false) }
  }

  fun <T : Any> Observable<T>.attachLoadable(): Observable<T> {
    return attachSilent()
      .doOnSubscribe { setLoading(true) }
      .doOnTerminate { setLoading(false) }
      .doOnNext { setLoading(false) }
  }

  fun <T : Any> Single<T>.attachLoadable(): Single<T> {
    return attachSilent()
      .doOnSubscribe { setLoading(true) }
      .doOnTerminate { setLoading(false) }
  }

  protected fun defaultErrorHandler(method: String): (Throwable) -> Unit = { throwable ->
    Trace.e(TAG, "Subscription failed! (${this::class.java.name}:$method)", throwable)
  }

  private fun errorMessage(type: String, calledAt: String?, throwableMessage: String?) =
    "$type called at '$calledAt' failed with message: '$throwableMessage'"

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
