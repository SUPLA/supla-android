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

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.hasValue
import org.supla.android.data.source.local.entity.complex.isGpMeasurement
import org.supla.android.data.source.local.entity.complex.isGpMeter
import org.supla.android.data.source.local.entity.complex.isHvacThermostat
import org.supla.android.data.source.local.entity.complex.isMeasurement
import org.supla.android.images.ImageCache
import org.supla.android.lib.SuplaConst.SUPLA_CHANNEL_FLAG_HAS_PARENT
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.location.CollapsedFlag
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateProfileChannelsListUseCase @Inject constructor(
  private val channelRelationRepository: ChannelRelationRepository,
  private val channelRepository: RoomChannelRepository,
  private val getChannelCaptionUseCase: GetChannelCaptionUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val valuesFormatter: ValuesFormatter
) {

  operator fun invoke(): Observable<List<ListItem>> =
    Single.zip(
      channelRelationRepository.findChildrenToParentsRelations(),
      channelRepository.findList()
    ) { relationMap, entities -> Pair(relationMap, entities) }
      .map { (relationMap, entities) ->
        val channels = mutableListOf<ListItem>()

        val channelsMap = mutableMapOf<Int, ChannelDataEntity>().also { map -> entities.forEach { map[it.remoteId] = it } }
        val childrenMap = mutableMapOf<Int, List<ChannelChildEntity?>>().also { map ->
          relationMap.forEach { relation ->
            map[relation.key] = relation.value.map { entry ->
              channelsMap[entry.channelId]?.let {
                ChannelChildEntity(
                  entry,
                  it
                )
              }
            }
          }
        }

        var location: LocationEntity? = null
        entities.forEach {
          if (it.channelEntity.flags and SUPLA_CHANNEL_FLAG_HAS_PARENT > 0) {
            // Skip channels which have parent ID.
            return@forEach
          }

          val currentLocation = location
          if (currentLocation == null || currentLocation.remoteId != it.locationEntity.remoteId) {
            val newLocation = it.locationEntity

            if (currentLocation == null || newLocation.caption != currentLocation.caption) {
              location = newLocation
              channels.add(ListItem.LocationItem(newLocation.toLegacyLocation()))
            }
          }

          location?.let { locationEntity ->
            if (!locationEntity.isCollapsed(CollapsedFlag.CHANNEL)) {
              channels.add(createChannelListItem(it, childrenMap))
            }
          }
        }

        Collections.unmodifiableList(channels)
      }.toObservable()

  private fun createChannelListItem(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ) =
    when {
      channelData.isGpMeasurement() -> toGpMeasurement(channelData)
      channelData.isGpMeter() -> toGpMeterItem(channelData)
      channelData.isMeasurement() -> toMeasurementItem(channelData)
      channelData.isHvacThermostat() -> toThermostatItem(channelData, childrenMap)
      else -> toChannelItem(channelData, childrenMap)
    }

  private fun toGpMeasurement(channelData: ChannelDataEntity): ListItem.GeneralPurposeMeasurementItem =
    ListItem.GeneralPurposeMeasurementItem(
      channelData.getLegacyChannel(),
      channelData.locationEntity.caption,
      channelData.channelValueEntity.online,
      getChannelCaptionUseCase(channelData),
      { ImageCache.getBitmap(it, getChannelIconUseCase(channelData)) },
      getChannelValueStringUseCase(channelData)
    )

  private fun toGpMeterItem(channelData: ChannelDataEntity): ListItem.GeneralPurposeMeterItem =
    ListItem.GeneralPurposeMeterItem(
      channelData.getLegacyChannel(),
      channelData.locationEntity.caption,
      channelData.channelValueEntity.online,
      getChannelCaptionUseCase(channelData),
      { ImageCache.getBitmap(it, getChannelIconUseCase(channelData)) },
      getChannelValueStringUseCase(channelData)
    )

  private fun toMeasurementItem(channelData: ChannelDataEntity): ListItem.MeasurementItem {
    val value: String? = when {
      channelData.hasValue() -> getChannelValueStringUseCase(channelData)
      else -> null
    }

    return ListItem.MeasurementItem(
      channelData.getLegacyChannel(),
      channelData.locationEntity.caption,
      channelData.channelValueEntity.online,
      getChannelCaptionUseCase(channelData),
      { ImageCache.getBitmap(it, getChannelIconUseCase(channelData)) },
      value
    )
  }

  private fun toThermostatItem(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ListItem.HvacThermostatItem {
    val thermostatValue = channelData.channelValueEntity.asThermostatValue()
    val mainThermometerChild = childrenMap[channelData.remoteId]
      ?.firstOrNull { it?.relationType == ChannelRelationType.MAIN_THERMOMETER }

    return ListItem.HvacThermostatItem(
      channelData.getLegacyChannel(),
      channelData.locationEntity.caption,
      channelData.channelValueEntity.online,
      getChannelCaptionUseCase(channelData),
      { ImageCache.getBitmap(it, getChannelIconUseCase(channelData)) },
      mainThermometerChild?.channelDataEntity?.let { getChannelValueStringUseCase(it) } ?: ValuesFormatter.NO_VALUE_TEXT,
      thermostatValue.getIssueIconType(),
      channelData.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue?.countdownEndsAt,
      thermostatValue.getSetpointText(valuesFormatter),
      thermostatValue.getIndicatorIcon(),
      thermostatValue.getIssueMessage()
    )
  }

  private fun toChannelItem(channelData: ChannelDataEntity, childrenMap: MutableMap<Int, List<ChannelChildEntity?>>): ListItem.ChannelItem =
    ListItem.ChannelItem(
      channelData.getLegacyChannel(),
      channelData.locationEntity.toLegacyLocation(),
      childrenMap[channelData.remoteId]?.filterNotNull()
    )
}
