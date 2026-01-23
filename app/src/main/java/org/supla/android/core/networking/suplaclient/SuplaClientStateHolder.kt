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
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.supla.android.core.SuplaAppProvider
import org.supla.android.extensions.subscribeBy
import org.supla.android.tools.SuplaSchedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@SuppressLint("CheckResult")
class SuplaClientStateHolder @Inject constructor(
  @ApplicationContext private val applicationContext: Context,
  private val suplaAppProvider: SuplaAppProvider,
  suplaSchedulers: SuplaSchedulers
) {

  private var stateSubject: BehaviorSubject<SuplaClientState> = BehaviorSubject.createDefault(SuplaClientState.Initialization)

  init {
    stateSubject
      .subscribeOn(suplaSchedulers.io)
      .observeOn(suplaSchedulers.io)
      .subscribeBy(
        onNext = {
          Timber.i("Supla client state: $it")

          when (it) {
            is SuplaClientState.Connecting -> {
              // The connecting state may result as a change from many different states
              // and we want that in this state always SuplaClient tries to connect
              // that's why this initialization is added here.
              suplaAppProvider.provide().SuplaClientInitIfNeed(applicationContext)
            }

            else -> {}
          }
        }
      )
  }

  fun state(): Observable<SuplaClientState> = stateSubject.hide()

  fun stateOrNull(): SuplaClientState? = stateSubject.value

  fun handleEvent(event: SuplaClientEvent) {
    synchronized(this) {
      handleEventInternally(event)
    }
  }

  private fun handleEventInternally(event: SuplaClientEvent) {
    var message = "Got event: $event"
    stateSubject.value?.nextState(event)?.let {
      message += " -> state: $it"
      stateSubject.onNext(it)
    }

    Timber.i(message)
  }
}
