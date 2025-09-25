package org.supla.android.data.model.spinner
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

import org.supla.android.R
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.complex.SceneDataEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.features.widget.shared.subjectdetail.ActionDetail
import org.supla.android.features.widget.shared.subjectdetail.SubjectDetail
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.ui.views.spinner.SpinnerItem
import org.supla.android.ui.views.spinner.SubjectSpinnerItem
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.extensions.invoke
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT

data class SubjectItem(
  val id: Int,
  val caption: LocalizedString,
  val actions: List<ActionId>,
  val function: SuplaFunction?,
  val userIcon: Int?,
  val altIcon: Int?,
  val value: String?,
  override val icon: ImageId?,
  override val isLocation: Boolean,
) : SubjectSpinnerItem {

  override val label: LocalizedString
    get() = caption

  fun actionsList(selectedAction: ActionId? = null): SingleSelectionList<ActionId>? =
    if (actions.isEmpty()) {
      null
    } else {
      SingleSelectionList(
        selected = actions.firstOrNull { it == selectedAction } ?: actions.first(),
        label = R.string.widget_configure_action_label,
        items = actions
      )
    }

  fun details(selected: SubjectDetail? = null): SingleSelectionList<SubjectDetail>? =
    actions.isNotEmpty().ifTrue {
      with(actions.map { ActionDetail(it) }) {
        SingleSelectionList(
          selected = this.firstOrNull { it == selected } ?: ActionDetail(actions.first()),
          label = R.string.widget_configure_action_label,
          items = this
        )
      }
    }

  companion object {
    fun create(
      id: Int = 0,
      caption: LocalizedString = LocalizedString.Constant(""),
      actions: List<ActionId> = emptyList(),
      function: SuplaFunction? = null,
      userIcon: Int? = null,
      altIcon: Int? = null,
      icon: ImageId? = null,
      isLocation: Boolean = false,
      value: String? = null
    ): SubjectItem =
      SubjectItem(
        id = id,
        caption = caption,
        actions = actions,
        function = function,
        userIcon = userIcon,
        altIcon = altIcon,
        icon = icon,
        isLocation = isLocation,
        value = value
      )
  }
}

interface SubjectItemConversionScope {
  val getCaptionUseCase: GetCaptionUseCase
  val getChannelIconUseCase: GetChannelIconUseCase
  val getSceneIconUseCase: GetSceneIconUseCase

  fun channelsSubjectItems(items: List<ChannelDataEntity>, getChannelValueStringUseCase: GetChannelValueStringUseCase): List<SubjectItem> =
    toSubjectItems(items, { it.locationEntity }, { it.subjectItem(getChannelValueStringUseCase) })

  fun groupsSubjectItems(items: List<ChannelGroupDataEntity>): List<SubjectItem> =
    toSubjectItems(items, { it.locationEntity }, { it.subjectItem })

  fun scenesSubjectItems(items: List<SceneDataEntity>): List<SubjectItem> =
    toSubjectItems(items, { it.locationEntity }, { it.subjectItem })

  fun <T> toSubjectItems(
    items: List<T>,
    locationExporter: (T) -> LocationEntity,
    converter: (T) -> SubjectItem
  ): List<SubjectItem> {
    if (items.isEmpty()) {
      return emptyList()
    }

    return mutableListOf<SubjectItem>().apply {
      var location = locationExporter(items.first())
      add(location.subjectItem)

      items.forEach {
        val currentLocation = locationExporter(it)
        if (location.caption != currentLocation.caption) {
          location = currentLocation
          add(location.subjectItem)
        }
        add(converter(it))
      }
    }
  }

  fun List<SubjectItem>.asSingleSelectionList(type: SubjectType, selectedId: Int? = null): SingleSelectionList<SubjectItem>? =
    if (isEmpty()) {
      null
    } else {
      SingleSelectionList(
        selected = firstOrNull { it.id == selectedId } ?: first { !it.isLocation },
        label = type.nameRes,
        items = this
      )
    }

  fun List<ProfileEntity>.asSingleSelectionList(selectedId: Long? = null): SingleSelectionList<ProfileItem>? =
    map { ProfileItem(it.id!!, LocalizedString.Constant(it.name)) }
      .asSingleSelectionList(R.string.widget_configure_profile_label)
      ?.let { list ->
        list.copy(selected = list.items.firstOrNull { it.id == selectedId } ?: list.items.first())
      }

  fun <T : SpinnerItem> List<T>.asSingleSelectionList(label: Int): SingleSelectionList<T>? =
    if (isEmpty()) {
      null
    } else {
      SingleSelectionList(selected = first(), label = label, items = this)
    }

  val LocationEntity.subjectItem: SubjectItem
    get() = SubjectItem(
      id = remoteId,
      caption = LocalizedString.Constant(caption),
      actions = emptyList(),
      function = null,
      userIcon = null,
      altIcon = null,
      icon = null,
      isLocation = true,
      value = null
    )

  fun ChannelDataEntity.subjectItem(getChannelValueStringUseCase: GetChannelValueStringUseCase): SubjectItem =
    SubjectItem(
      id = remoteId,
      caption = getCaptionUseCase(shareable),
      actions = function.actions,
      function = function,
      userIcon = userIcon,
      altIcon = altIcon,
      icon = getChannelIconUseCase.forState(channelEntity, offlineState),
      isLocation = false,
      value = when (function) {
        SuplaFunction.DIMMER -> "0"
        SuplaFunction.RGB_LIGHTING,
        SuplaFunction.DIMMER_AND_RGB_LIGHTING -> "${channelValueEntity.asColor()}"

        SuplaFunction.THERMOMETER,
        SuplaFunction.GENERAL_PURPOSE_METER,
        SuplaFunction.GENERAL_PURPOSE_MEASUREMENT -> getChannelValueStringUseCase.invoke(ChannelWithChildren(this))

        SuplaFunction.HUMIDITY_AND_TEMPERATURE -> ChannelWithChildren(this).let {
          val temperature = getChannelValueStringUseCase(it)
          val humidity = getChannelValueStringUseCase(it, valueType = ValueType.SECOND)
          "$temperature\n$humidity"
        }

        else -> NO_VALUE_TEXT
      }
    )

  val ChannelGroupDataEntity.subjectItem: SubjectItem
    get() = SubjectItem(
      id = remoteId,
      caption = getCaptionUseCase(shareable),
      actions = function.actions,
      function = channelGroupEntity.function,
      userIcon = userIcon,
      altIcon = altIcon,
      icon = getChannelIconUseCase.forState(channelGroupEntity, offlineState),
      isLocation = false,
      value = when (function) {
        SuplaFunction.RGB_LIGHTING,
        SuplaFunction.DIMMER_AND_RGB_LIGHTING,
        SuplaFunction.DIMMER -> "0"

        else -> NO_VALUE_TEXT
      }
    )

  val SceneDataEntity.subjectItem: SubjectItem
    get() = SubjectItem(
      id = remoteId,
      caption = getCaptionUseCase(sceneEntity),
      actions = listOf(ActionId.EXECUTE, ActionId.INTERRUPT, ActionId.INTERRUPT_AND_EXECUTE),
      function = null,
      userIcon = sceneEntity.userIcon,
      altIcon = sceneEntity.altIcon,
      icon = getSceneIconUseCase(sceneEntity),
      isLocation = false,
      value = NO_VALUE_TEXT
    )

  val SuplaFunction.actions: List<ActionId>
    get() = when (this) {
      SuplaFunction.OPEN_SENSOR_GATEWAY,
      SuplaFunction.OPEN_SENSOR_GATE,
      SuplaFunction.OPEN_SENSOR_GARAGE_DOOR,
      SuplaFunction.OPEN_SENSOR_DOOR,
      SuplaFunction.NO_LIQUID_SENSOR,
      SuplaFunction.DEPTH_SENSOR,
      SuplaFunction.DISTANCE_SENSOR,
      SuplaFunction.OPENING_SENSOR_WINDOW,
      SuplaFunction.HOTEL_CARD_SENSOR,
      SuplaFunction.ALARM_ARMAMENT_SENSOR,
      SuplaFunction.MAIL_SENSOR,
      SuplaFunction.WIND_SENSOR,
      SuplaFunction.PRESSURE_SENSOR,
      SuplaFunction.RAIN_SENSOR,
      SuplaFunction.WEIGHT_SENSOR,
      SuplaFunction.WEATHER_STATION,
      SuplaFunction.THERMOMETER,
      SuplaFunction.HUMIDITY,
      SuplaFunction.HUMIDITY_AND_TEMPERATURE,
      SuplaFunction.UNKNOWN,
      SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER,
      SuplaFunction.OPEN_SENSOR_ROOF_WINDOW,
      SuplaFunction.RING,
      SuplaFunction.ALARM,
      SuplaFunction.NOTIFICATION,
      SuplaFunction.ELECTRICITY_METER,
      SuplaFunction.IC_ELECTRICITY_METER,
      SuplaFunction.IC_GAS_METER,
      SuplaFunction.IC_WATER_METER,
      SuplaFunction.IC_HEAT_METER,
      SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
      SuplaFunction.GENERAL_PURPOSE_METER,
      SuplaFunction.DIGIGLASS_HORIZONTAL,
      SuplaFunction.DIGIGLASS_VERTICAL,
      SuplaFunction.CONTAINER,
      SuplaFunction.SEPTIC_TANK,
      SuplaFunction.WATER_TANK,
      SuplaFunction.CONTAINER_LEVEL_SENSOR,
      SuplaFunction.FLOOD_SENSOR,
      SuplaFunction.PUMP_SWITCH,
      SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH,
      SuplaFunction.NONE -> emptyList()

      SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK -> listOf(ActionId.OPEN)

      SuplaFunction.CONTROLLING_THE_GATE,
      SuplaFunction.CONTROLLING_THE_GARAGE_DOOR -> listOf(ActionId.OPEN_CLOSE, ActionId.OPEN, ActionId.CLOSE)

      SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaFunction.VERTICAL_BLIND,
      SuplaFunction.ROLLER_GARAGE_DOOR -> listOf(ActionId.SHUT, ActionId.REVEAL)

      SuplaFunction.POWER_SWITCH,
      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.STAIRCASE_TIMER,
      SuplaFunction.DIMMER,
      SuplaFunction.RGB_LIGHTING,
      SuplaFunction.DIMMER_AND_RGB_LIGHTING,
      SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
      SuplaFunction.HVAC_THERMOSTAT,
      SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
      SuplaFunction.HVAC_DOMESTIC_HOT_WATER -> listOf(ActionId.TURN_ON, ActionId.TURN_OFF, ActionId.TOGGLE)

      SuplaFunction.VALVE_OPEN_CLOSE,
      SuplaFunction.VALVE_PERCENTAGE -> listOf(ActionId.OPEN, ActionId.CLOSE)

      SuplaFunction.TERRACE_AWNING,
      SuplaFunction.PROJECTOR_SCREEN,
      SuplaFunction.CURTAIN -> listOf(ActionId.EXPAND, ActionId.COLLAPSE)
    }
}
