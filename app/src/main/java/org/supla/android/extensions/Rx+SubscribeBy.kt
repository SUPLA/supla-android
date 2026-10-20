package org.supla.android.extensions
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

import io.reactivex.rxjava3.annotations.BackpressureKind
import io.reactivex.rxjava3.annotations.BackpressureSupport
import io.reactivex.rxjava3.annotations.CheckReturnValue
import io.reactivex.rxjava3.annotations.SchedulerSupport
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.internal.functions.Functions

/**
 * Copied from https://github.com/ReactiveX/RxKotlin/blob/3.x/src/main/kotlin/io/reactivex/rxjava3/kotlin/subscribers.kt
 */

private val onNextStub: (Any) -> Unit = {}
private val onErrorStub: (Throwable) -> Unit = {}
private val onCompleteStub: () -> Unit = {}

private fun <T : Any> ((T) -> Unit).asConsumer(): Consumer<T> {
  return if (this === onNextStub) Functions.emptyConsumer() else Consumer(this)
}

private fun ((Throwable) -> Unit).asOnErrorConsumer(): Consumer<Throwable> {
  return if (this === onErrorStub) Functions.ON_ERROR_MISSING else Consumer(this)
}

private fun (() -> Unit).asOnCompleteAction(): Action {
  return if (this === onCompleteStub) Functions.EMPTY_ACTION else Action(this)
}

/**
 * Overloaded subscribe function that allows passing named parameters
 */
@CheckReturnValue
@SchedulerSupport(SchedulerSupport.NONE)
fun <T : Any> Observable<T>.subscribeBy(
  onError: (Throwable) -> Unit = onErrorStub,
  onComplete: () -> Unit = onCompleteStub,
  onNext: (T) -> Unit = onNextStub
): Disposable = subscribe(onNext.asConsumer(), onError.asOnErrorConsumer(), onComplete.asOnCompleteAction())

/**
 * Overloaded subscribe function that allows passing named parameters
 */
@CheckReturnValue
@BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
@SchedulerSupport(SchedulerSupport.NONE)
fun <T : Any> Flowable<T>.subscribeBy(
  onError: (Throwable) -> Unit = onErrorStub,
  onComplete: () -> Unit = onCompleteStub,
  onNext: (T) -> Unit = onNextStub
): Disposable = subscribe(onNext.asConsumer(), onError.asOnErrorConsumer(), onComplete.asOnCompleteAction())

/**
 * Overloaded subscribe function that allows passing named parameters
 */
@CheckReturnValue
@SchedulerSupport(SchedulerSupport.NONE)
fun <T : Any> Single<T>.subscribeBy(
  onError: (Throwable) -> Unit = onErrorStub,
  onSuccess: (T) -> Unit = onNextStub
): Disposable = subscribe(onSuccess.asConsumer(), onError.asOnErrorConsumer())

/**
 * Overloaded subscribe function that allows passing named parameters
 */
@CheckReturnValue
@SchedulerSupport(SchedulerSupport.NONE)
fun <T : Any> Maybe<T>.subscribeBy(
  onError: (Throwable) -> Unit = onErrorStub,
  onComplete: () -> Unit = onCompleteStub,
  onSuccess: (T) -> Unit = onNextStub
): Disposable = subscribe(onSuccess.asConsumer(), onError.asOnErrorConsumer(), onComplete.asOnCompleteAction())

/**
 * Overloaded subscribe function that allows passing named parameters
 */
@CheckReturnValue
@SchedulerSupport(SchedulerSupport.NONE)
fun Completable.subscribeBy(
  onError: (Throwable) -> Unit = onErrorStub,
  onComplete: () -> Unit = onCompleteStub
): Disposable = when {
  // There are optimized versions of the completable Consumers, so we need to use the subscribe overloads
  // here.
  onError === onErrorStub && onComplete === onCompleteStub -> subscribe()
  onError === onErrorStub -> subscribe(onComplete)
  else -> subscribe(onComplete.asOnCompleteAction(), Consumer(onError))
}

/**
 * Overloaded blockingSubscribe function that allows passing named parameters
 */
@SchedulerSupport(SchedulerSupport.NONE)
fun <T : Any> Observable<T>.blockingSubscribeBy(
  onError: (Throwable) -> Unit = onErrorStub,
  onComplete: () -> Unit = onCompleteStub,
  onNext: (T) -> Unit = onNextStub
): Unit = blockingSubscribe(onNext.asConsumer(), onError.asOnErrorConsumer(), onComplete.asOnCompleteAction())
