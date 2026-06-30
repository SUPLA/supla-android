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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

abstract class EventBasedViewModel<E : ViewEvent> : ViewModel() {
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

  open fun onViewCreated() {}
  open fun onStart() {}
  open fun onStop() {}

  protected fun sendEvent(event: E) {
    viewEvents.tryEmit(Event(event))
  }

  data class Event<T>(
    val item: T,
    var processed: Boolean = false
  )
}
