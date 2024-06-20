package org.supla.android.usecases.channel
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
import io.reactivex.rxjava3.core.Single
import org.supla.android.Trace
import org.supla.android.data.model.general.EntityUpdateResult
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.db.Location
import org.supla.android.extensions.TAG
import org.supla.android.lib.SuplaChannel
import org.supla.android.usecases.channelconfig.RequestChannelConfigUseCase
import org.supla.android.widget.WidgetManager
import org.supla.android.widget.WidgetPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChannelUseCase @Inject constructor(
  private val requestChannelConfigUseCase: RequestChannelConfigUseCase,
  private val profileRepository: RoomProfileRepository,
  private val channelRepository: RoomChannelRepository,
  private val locationRepository: LocationRepository,
  private val widgetPreferences: WidgetPreferences,
  private val widgetManager: WidgetManager
) {

  operator fun invoke(suplaChannel: SuplaChannel): Single<EntityUpdateResult> =
    checkLocation(suplaChannel.LocationID) { locationEntity ->
      channelRepository.findByRemoteId(suplaChannel.Id)
        .toSingle()
        .flatMap { channelEntity ->

          if (channelEntity.differsFrom(suplaChannel) || channelEntity.visible != 1) {
            updateChannel(locationEntity, channelEntity, suplaChannel)
          } else {
            Single.just(EntityUpdateResult.NOP)
          }
        }.onErrorResumeNext { throwable ->
          throwable.printStackTrace()
          if (throwable is NoSuchElementException) {
            insertChannel(locationEntity, suplaChannel)
          } else {
            Trace.e(TAG, "Channel update failed!", throwable)
            Single.just(EntityUpdateResult.ERROR)
          }
        }
    }

  private fun updateChannel(locationEntity: LocationEntity, channelEntity: ChannelEntity, suplaChannel: SuplaChannel) =
    updatePosition(locationEntity, channelEntity.updatedBy(suplaChannel), channelEntity.locationChanged(suplaChannel))
      .flatMapCompletable(channelRepository::update)
      .andThen(requestChannelConfigUseCase(suplaChannel))
      .andThen(checkWidgetUpdateNeeded(channelEntity.profileId, suplaChannel))
      .andThen(Single.just(EntityUpdateResult.UPDATED))

  private fun insertChannel(locationEntity: LocationEntity, suplaChannel: SuplaChannel) =
    profileRepository.findActiveProfile()
      .flatMapCompletable {
        updatePosition(locationEntity, ChannelEntity.from(suplaChannel, it.id!!), false)
          .flatMapCompletable(channelRepository::insert)
      }
      .andThen(requestChannelConfigUseCase(suplaChannel))
      .andThen(Single.just(EntityUpdateResult.UPDATED))

  private fun updatePosition(locationEntity: LocationEntity, channelEntity: ChannelEntity, locationChanged: Boolean) =
    if (locationEntity.sorting == Location.SortingType.USER_DEFINED && (channelEntity.id == null || locationChanged)) {
      channelRepository.findChannelCountInLocation(locationEntity.remoteId)
        .map { count ->
          Trace.i(TAG, "Updating channel position to `$count`")
          return@map channelEntity.copy(position = count + 1)
        }
    } else if (locationEntity.sorting == Location.SortingType.DEFAULT && channelEntity.position != 0) {
      Single.just(channelEntity.copy(position = 0))
    } else {
      Single.just(channelEntity)
    }

  private fun checkWidgetUpdateNeeded(profileId: Long, suplaChannel: SuplaChannel) =
    Completable.fromRunnable {
      widgetManager.findWidgetConfig(profileId, suplaChannel.Id)?.let { (widgetId, configuration) ->
        widgetPreferences.setWidgetConfiguration(
          widgetId,
          configuration.copy(userIcon = suplaChannel.UserIcon, altIcon = suplaChannel.AltIcon, itemFunction = suplaChannel.Func)
        )
        widgetManager.updateWidget(widgetId)
      }
    }

  private fun checkLocation(locationId: Int, updater: (LocationEntity) -> Single<EntityUpdateResult>) =
    locationRepository.findByRemoteId(locationId)
      .toSingle()
      .flatMap { updater(it) }
      .onErrorResumeNext {
        if (it !is NoSuchElementException) {
          Trace.e(TAG, "Channel update - location check failed!", it)
        }
        Single.just(EntityUpdateResult.ERROR)
      }

  private fun ChannelEntity.locationChanged(suplaChannel: SuplaChannel) =
    locationId != suplaChannel.LocationID
}
