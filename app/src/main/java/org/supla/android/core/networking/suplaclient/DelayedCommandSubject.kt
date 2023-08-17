package org.supla.android.core.networking.suplaclient
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

import android.annotation.SuppressLint
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.PublishSubject
import org.supla.android.Trace
import org.supla.android.extensions.TAG
import org.supla.android.tools.SuplaSchedulers
import java.util.concurrent.TimeUnit

const val DELAYED_COMMAND_DELAY_S = 2L

@SuppressLint("CheckResult")
abstract class DelayedCommandSubject<T : DelayableState>(schedulers: SuplaSchedulers) {

  private val delayedRequestsSubject = PublishSubject.create<T>()

  init {
    delayedRequestsSubject
      .debounce(DELAYED_COMMAND_DELAY_S, TimeUnit.SECONDS)
      .flatMapCompletable { if (it.sent.not()) { execute(it) } else Completable.complete() }
      .subscribeOn(schedulers.io)
      .subscribeBy(
        onError = { Trace.e(TAG, "Could not execute delayed request", it) }
      )
  }

  @Suppress("UNCHECKED_CAST")
  fun emit(state: T) {
    delayedRequestsSubject.onNext(state.delayableCopy() as T)
  }

  @Suppress("UNCHECKED_CAST")
  fun sendImmediately(state: T): Completable {
    val sentState = state.sentState() as T
    delayedRequestsSubject.onNext(sentState)

    return execute(sentState)
  }

  abstract fun execute(state: T): Completable
}

interface DelayableState {
  val sent: Boolean
  fun sentState(): DelayableState
  fun delayableCopy(): DelayableState // to avoid overriding the data class copy(..) method
}
