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
import io.reactivex.rxjava3.subjects.PublishSubject
import org.supla.android.Trace
import org.supla.android.data.source.remote.ChannelConfigResult
import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.extensions.TAG
import org.supla.android.extensions.guardLet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigEventsManager @Inject constructor() {

  private val subjects: MutableMap<Int, PublishSubject<ConfigEvent>> = mutableMapOf()

  fun emitConfig(result: ChannelConfigResult, config: SuplaChannelConfig?) {
    val (remoteId) = guardLet(config?.remoteId) {
      Trace.e(TAG, "Got result `$result` without config `$config`")
      return
    }

    getSubject(remoteId).run {
      onNext(ConfigEvent(result, config))
    }
  }

  fun observerConfig(remoteId: Int): Observable<ConfigEvent> = getSubject(remoteId).hide()

  @Synchronized
  private fun getSubject(remoteId: Int): PublishSubject<ConfigEvent> {
    val subject = subjects[remoteId]
    if (subject != null) {
      return subject
    }

    return PublishSubject.create<ConfigEvent>().also {
      subjects[remoteId] = it
    }
  }

  data class ConfigEvent(
    val result: ChannelConfigResult,
    val config: SuplaChannelConfig?
  )
}
