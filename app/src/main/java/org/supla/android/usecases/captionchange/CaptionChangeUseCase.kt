package org.supla.android.usecases.captionchange
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

import io.reactivex.rxjava3.core.Completable
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.events.UpdateEventsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaptionChangeUseCase @Inject constructor(
  private val locationRepository: LocationRepository,
  private val roomChannelRepository: RoomChannelRepository,
  private val groupRepository: ChannelGroupRepository,
  private val sceneRepository: RoomSceneRepository,
  private val suplaClientProvider: SuplaClientProvider,
  private val updateEventsManager: UpdateEventsManager
) {

  operator fun invoke(caption: String, type: Type, remoteId: Int, profileId: Long): Completable {
    return getUpdater(type).updateCaption(caption, remoteId, profileId)
      .andThen(
        Completable.fromRunnable {
          suplaClientProvider.provide()?.run {
            when (type) {
              Type.LOCATION -> setLocationCaption(remoteId, caption)
              Type.CHANNEL -> {
                setChannelCaption(remoteId, caption)
                updateEventsManager.emitChannelUpdate(remoteId)
              }
              Type.GROUP -> {
                setChannelGroupCaption(remoteId, caption)
                updateEventsManager.emitGroupUpdate(remoteId)
              }
              Type.SCENE -> {
                setSceneCaption(remoteId, caption)
                updateEventsManager.emitSceneUpdate(remoteId)
              }
            }
          }
        }
      )
  }

  private fun getUpdater(type: Type): Updater =
    when (type) {
      Type.LOCATION -> locationRepository
      Type.CHANNEL -> roomChannelRepository
      Type.GROUP -> groupRepository
      Type.SCENE -> sceneRepository
    }

  enum class Type {
    LOCATION, CHANNEL, GROUP, SCENE
  }

  interface Updater {
    fun updateCaption(caption: String, remoteId: Int, profileId: Long): Completable
  }
}
