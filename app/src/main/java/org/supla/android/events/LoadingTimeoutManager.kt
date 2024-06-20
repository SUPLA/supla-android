package org.supla.android.events
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

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.infrastructure.DateProvider
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TIMEOUT_MS = 5000

class LoadingTimeoutManager @Inject constructor() {

  private val timeoutWatcher = Observable.interval(100, TimeUnit.MILLISECONDS)
  private var timeout = TIMEOUT_MS

  fun watch(stateProvider: () -> LoadingState, onTimeout: () -> Unit): Disposable {
    return timeoutWatcher.subscribeBy(
      onNext = {
        val state = stateProvider()

        if (state.initialLoading) {
          return@subscribeBy
        }
        if (state.loading.not()) {
          return@subscribeBy
        }
        state.lastLoadingStartTimestamp?.let { startTime ->
          if (System.currentTimeMillis() > startTime.plus(timeout)) {
            onTimeout()
          }
        }
      }
    )
  }

  data class LoadingState(
    val initialLoading: Boolean = true,
    val loading: Boolean = true,
    val lastLoadingStartTimestamp: Long? = null
  ) {
    fun changingLoading(loading: Boolean, dateProvider: DateProvider): LoadingState =
      copy(initialLoading = false, loading = loading, lastLoadingStartTimestamp = if (loading) dateProvider.currentTimestamp() else null)
  }
}
