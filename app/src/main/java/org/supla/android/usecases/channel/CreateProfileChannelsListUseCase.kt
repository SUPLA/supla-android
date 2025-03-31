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
import org.supla.android.core.shared.shareable
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.model.general.IconType
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.indicatorIcon
import org.supla.android.data.source.local.entity.complex.isGpMeasurement
import org.supla.android.data.source.local.entity.complex.isGpMeter
import org.supla.android.data.source.local.entity.complex.isHvacThermostat
import org.supla.android.data.source.local.entity.complex.isIconValueItem
import org.supla.android.data.source.local.entity.complex.isShadingSystem
import org.supla.android.data.source.local.entity.complex.onlineState
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.isGarageDoorRoller
import org.supla.android.data.source.local.entity.isIconWithAction
import org.supla.android.data.source.local.entity.isProjectorScreen
import org.supla.android.data.source.local.entity.isRgbw
import org.supla.android.data.source.local.entity.isSwitch
import org.supla.android.data.source.remote.thermostat.getIndicatorIcon
import org.supla.android.data.source.remote.thermostat.getSetpointText
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.lists.onlineState
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelIssuesForListUseCase
import java.util.Collections
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateProfileChannelsListUseCase @Inject constructor(
  private val channelRelationRepository: ChannelRelationRepository,
  private val channelRepository: RoomChannelRepository,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val valuesFormatter: ValuesFormatter,
  private val getChannelIssuesForListUseCase: GetChannelIssuesForListUseCase,
  private val getChannelChildrenTreeUseCase: GetChannelChildrenTreeUseCase
) {

  operator fun invoke(): Observable<List<ListItem>> =
    Single.zip(
      channelRelationRepository.findChildrenToParentsRelations().firstOrError(),
      channelRepository.findList()
    ) { relationMap, entities -> Pair(relationMap, entities) }
      .map { (relationMap, entities) ->
        val channels = mutableListOf<ListItem>()

        val channelsMap = mutableMapOf<Int, ChannelDataEntity>().also { map -> entities.forEach { map[it.remoteId] = it } }
        val allChildrenIds = relationMap.flatMap { it.value }.map { it.channelId }
        val childrenMap = mutableMapOf<Int, List<ChannelChildEntity?>>().also { map ->
          relationMap.forEach { relation ->
            val childrenList = LinkedList<Int>()
            map[relation.key] = getChannelChildrenTreeUseCase.invoke(relation.key, relationMap, channelsMap, childrenList)
          }
        }

        var location: LocationEntity? = null
        entities.forEach {
          if (allChildrenIds.contains(it.remoteId)) {
            // Skip channels which have parent ID.
            return@forEach
          }

          val currentLocation = location
          if (currentLocation == null || currentLocation.remoteId != it.locationEntity.remoteId) {
            val newLocation = it.locationEntity

            if (currentLocation == null || newLocation.caption != currentLocation.caption) {
              location = newLocation
              channels.add(ListItem.LocationItem(newLocation))
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

  private fun createChannelListItem(channelData: ChannelDataEntity, childrenMap: MutableMap<Int, List<ChannelChildEntity?>>) =
    when {
      channelData.isGpMeasurement() || channelData.isGpMeter() || channelData.isIconValueItem() ->
        toIconValueItem(channelData, childrenMap)

      channelData.isShadingSystem() ||
        channelData.isProjectorScreen() ||
        channelData.isGarageDoorRoller() ||
        channelData.isSwitch() ||
        channelData.isRgbw() ||
        channelData.function == SuplaFunction.VALVE_OPEN_CLOSE -> toIconWithButtonsItem(channelData, childrenMap)

      channelData.isIconWithAction() -> toIconWithRightButtonItem(channelData, childrenMap)
      channelData.isHvacThermostat() -> toThermostatItem(channelData, childrenMap)
      channelData.function == SuplaFunction.HUMIDITY_AND_TEMPERATURE -> toDoubleValueItem(channelData, childrenMap)
      else -> toChannelItem(channelData, childrenMap)
    }

  private fun toIconValueItem(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ListItem.IconValueItem =
    channelWithChildren(channelData, childrenMap).let {
      ListItem.IconValueItem(
        channelData,
        channelData.locationEntity.caption,
        channelData.channelValueEntity.status.onlineState,
        getCaptionUseCase(channelData.shareable),
        getChannelIconUseCase(channelData),
        getChannelValueStringUseCase.valueOrNull(it),
        getChannelIssuesForListUseCase(it.shareable)
      )
    }

  private fun toThermostatItem(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ListItem.HvacThermostatItem {
    val thermostatValue = channelData.channelValueEntity.asThermostatValue()
    val children = childrenMap[channelData.remoteId]
    val mainThermometerChild = children?.firstOrNull { it?.relationType == ChannelRelationType.MAIN_THERMOMETER }
    val indicatorIcon = thermostatValue.getIndicatorIcon() mergeWith children?.filterNotNull()?.indicatorIcon
    val onlineState = channelData.channelValueEntity.status.onlineState mergeWith children?.filterNotNull()?.onlineState

    return ListItem.HvacThermostatItem(
      channelData,
      channelData.locationEntity.caption,
      onlineState,
      getCaptionUseCase(channelData.shareable),
      getChannelIconUseCase(channelData),
      mainThermometerChild?.let { getChannelValueStringUseCase(it.withChildren) } ?: ValuesFormatter.NO_VALUE_TEXT,
      getChannelIssuesForListUseCase(channelWithChildren(channelData, childrenMap).shareable),
      channelData.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue?.countdownEndsAt,
      thermostatValue.getSetpointText(valuesFormatter),
      indicatorIcon.resource,
    )
  }

  private fun toIconWithButtonsItem(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ListItem.IconWithButtonsItem {
    return channelWithChildren(channelData, childrenMap).let {
      ListItem.IconWithButtonsItem(
        channelData,
        channelData.locationEntity.caption,
        channelData.channelValueEntity.status.onlineState,
        getCaptionUseCase(channelData.shareable),
        getChannelIconUseCase(channelData),
        value = getChannelValueStringUseCase.valueOrNull(it),
        channelData.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue?.countdownEndsAt,
        getChannelIssuesForListUseCase(it.shareable)
      )
    }
  }

  private fun toIconWithRightButtonItem(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ListItem.IconWithRightButtonItem {
    return channelWithChildren(channelData, childrenMap).let {
      ListItem.IconWithRightButtonItem(
        channelData,
        channelData.locationEntity.caption,
        channelData.channelValueEntity.status.onlineState,
        getCaptionUseCase(channelData.shareable),
        getChannelIconUseCase(channelData),
        value = getChannelValueStringUseCase.valueOrNull(it),
        channelData.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue?.countdownEndsAt,
        getChannelIssuesForListUseCase(it.shareable)
      )
    }
  }

  private fun toDoubleValueItem(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ListItem.DoubleValueItem =
    channelWithChildren(channelData, childrenMap).let {
      ListItem.DoubleValueItem(
        channelData,
        channelData.locationEntity.caption,
        channelData.channelValueEntity.status.onlineState,
        getCaptionUseCase(channelData.shareable),
        getChannelIconUseCase(channelData),
        value = getChannelValueStringUseCase.valueOrNull(it),
        getChannelIssuesForListUseCase(it.shareable),
        secondIcon = getChannelIconUseCase(channelData, IconType.SECOND),
        secondValue = getChannelValueStringUseCase.valueOrNull(it, ValueType.SECOND, withUnit = false)
      )
    }

  private fun toChannelItem(channelData: ChannelDataEntity, childrenMap: MutableMap<Int, List<ChannelChildEntity?>>): ListItem.ChannelItem =
    ListItem.ChannelItem(
      channelData,
      childrenMap[channelData.remoteId]?.filterNotNull(),
      channelData.getLegacyChannel()
    )

  private fun channelWithChildren(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ChannelWithChildren {
    val children = mutableListOf<ChannelChildEntity>().apply {
      childrenMap[channelData.remoteId]?.forEach { it?.let { add(it) } }
    }
    return ChannelWithChildren(channelData, children)
  }
}
