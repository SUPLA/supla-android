package org.supla.android.usecases.group

import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.di.FORMATTER_THERMOMETER
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.list.GroupToListItemMapper
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CreateProfileGroupsListUseCase @Inject constructor(
  override val getGroupActivePercentageUseCase: GetGroupActivePercentageUseCase,
  private val channelGroupRepository: ChannelGroupRepository,
  override val getChannelIconUseCase: GetChannelIconUseCase,
  override val getCaptionUseCase: GetCaptionUseCase,
  @param:Named(FORMATTER_THERMOMETER) override val thermometerValueFormatter: ValueFormatter
) : GroupToListItemMapper {
  operator fun invoke(): Observable<List<ListItem>> =
    channelGroupRepository.findList().map { entities ->
      val groups = mutableListOf<ListItem>()

      var location: LocationEntity? = null
      entities.forEach {
        val currentLocation = location
        if (currentLocation == null || currentLocation.remoteId != it.locationId) {
          val newLocation = it.locationEntity

          if (currentLocation == null || newLocation.caption != currentLocation.caption) {
            location = newLocation
            groups.add(ListItem.LocationItem(newLocation))
          }
        }

        location.let { locationEntity ->
          if (!locationEntity.isCollapsed(CollapsedFlag.GROUP)) {
            groups.add(createListItem(it))
          }
        }
      }

      groups.toList()
    }.toObservable()

  private fun createListItem(groupDataEntity: ChannelGroupDataEntity) =
    when (groupDataEntity.function) {
      SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS -> toHeatpolThermostatItem(groupDataEntity)

      SuplaFunction.CONTROLLING_THE_GATE,
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK -> toIconWithRightButtonItem(groupDataEntity)

      SuplaFunction.POWER_SWITCH,
      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.DIMMER,
      SuplaFunction.DIMMER_CCT,
      SuplaFunction.RGB_LIGHTING,
      SuplaFunction.DIMMER_AND_RGB_LIGHTING,
      SuplaFunction.DIMMER_CCT_AND_RGB,
      SuplaFunction.STAIRCASE_TIMER,
      SuplaFunction.HVAC_THERMOSTAT,
      SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
      SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
      SuplaFunction.HVAC_HRV,
      SuplaFunction.VALVE_OPEN_CLOSE,
      SuplaFunction.VALVE_PERCENTAGE,
      SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaFunction.TERRACE_AWNING,
      SuplaFunction.PROJECTOR_SCREEN,
      SuplaFunction.CURTAIN,
      SuplaFunction.VERTICAL_BLIND,
      SuplaFunction.ROLLER_GARAGE_DOOR -> toIconWithButtonsItem(groupDataEntity)

      SuplaFunction.UNKNOWN,
      SuplaFunction.NONE,
      SuplaFunction.THERMOMETER,
      SuplaFunction.HUMIDITY,
      SuplaFunction.HUMIDITY_AND_TEMPERATURE,
      SuplaFunction.OPEN_SENSOR_GATEWAY,
      SuplaFunction.OPEN_SENSOR_GATE,
      SuplaFunction.OPEN_SENSOR_GARAGE_DOOR,
      SuplaFunction.NO_LIQUID_SENSOR,
      SuplaFunction.OPEN_SENSOR_DOOR,
      SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER,
      SuplaFunction.OPEN_SENSOR_ROOF_WINDOW,
      SuplaFunction.RING,
      SuplaFunction.ALARM,
      SuplaFunction.NOTIFICATION,
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
      SuplaFunction.ELECTRICITY_METER,
      SuplaFunction.IC_ELECTRICITY_METER,
      SuplaFunction.IC_GAS_METER,
      SuplaFunction.IC_WATER_METER,
      SuplaFunction.IC_HEAT_METER,
      SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
      SuplaFunction.GENERAL_PURPOSE_METER,
      SuplaFunction.DIGIGLASS_HORIZONTAL,
      SuplaFunction.DIGIGLASS_VERTICAL,
      SuplaFunction.PUMP_SWITCH,
      SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH,
      SuplaFunction.CONTAINER,
      SuplaFunction.SEPTIC_TANK,
      SuplaFunction.WATER_TANK,
      SuplaFunction.CONTAINER_LEVEL_SENSOR,
      SuplaFunction.FLOOD_SENSOR,
      SuplaFunction.MOTION_SENSOR,
      SuplaFunction.BINARY_SENSOR -> toIconValueItem(groupDataEntity)
    }
}