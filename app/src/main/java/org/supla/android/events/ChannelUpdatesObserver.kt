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

import io.reactivex.rxjava3.disposables.Disposable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.extensions.subscribeBy
import org.supla.android.tools.SuplaSchedulers

interface ChannelUpdatesObserver {

  val updateEventsManager: UpdateEventsManager

  val schedulers: SuplaSchedulers

  fun onChannelUpdate(channelWithChildren: ChannelWithChildren)

  fun handle(disposable: Disposable)

  fun observe(remoteId: Int) {
    handle(
      updateEventsManager.observeChannelWithChildren(remoteId)
        .subscribeOn(schedulers.io)
        .observeOn(schedulers.ui)
        .subscribeBy(onNext = this::onChannelUpdate)
    )
  }
}
