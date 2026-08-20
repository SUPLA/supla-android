package org.supla.android.ui.lists
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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.reactivex.rxjava3.core.Observable
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.extensions.subscribeBy
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
import org.supla.android.usecases.profile.CloudUrl
import org.supla.android.usecases.profile.LoadActiveProfileUrlUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import timber.log.Timber

private const val CLICK_EVENT_DELAY_MS = 250

abstract class BaseListViewModel<S : ViewState, E : ViewEvent>(
  private val vibrationHelper: VibrationHelper,
  private val dateProvider: DateProvider,
  schedulers: SuplaSchedulers,
  defaultState: S,
  private val loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase? = null,
) : BaseViewModel<S, E>(defaultState, schedulers) {

  protected var lastItemOpenTime: Long = 0

  var listLoaded by mutableStateOf(false)
    private set

  protected val listState = mutableStateListOf<ListItem>()
  val list: List<ListItem> = listState

  open fun onDragStarted(remoteId: Int) {
    vibrationHelper.vibrate()
  }

  protected abstract fun reloadList()

  protected fun updateDefaultItem(item: ListItem) {
    updateItem(item) { it is ListItem.DefaultItem }
  }

  protected fun updateSceneItem(item: ListItem) {
    updateItem(item) { it is ListItem.SceneItem }
  }

  private fun updateItem(item: ListItem, matcher: (ListItem) -> Boolean) {
    try {
      val index = listState.indexOfFirst { matcher(it) && it.remoteId == item.remoteId }
      if (index >= 0 && listState[index] != item) {
        listState[index] = item
      }
    } catch (ex: Exception) {
      Timber.e(ex, "Item update failed! (id: `${item.remoteId}`)")
    }
  }

  protected fun updateItems(items: List<ListItem>) {
    if (!listLoaded) {
      listLoaded = true
    }

    listState.clear()
    listState.addAll(items)
  }

  protected fun observeUpdates(updatesObservable: Observable<Any>) {
    updatesObservable
      .attachSilent()
      .subscribeBy(
        onNext = { reloadList() }
      )
      .disposeBySelf()
  }

  protected fun isEventAllowed(): Boolean {
    val currentTimestamp = dateProvider.currentTimestamp()
    if (currentTimestamp - lastItemOpenTime < CLICK_EVENT_DELAY_MS) {
      // Skip opening details when fast clicking...
      return false
    }
    lastItemOpenTime = currentTimestamp
    return true
  }

  protected fun isAvailableInOffline(channel: ChannelDataBase) =
    when (channel.function) {
      SuplaFunction.THERMOMETER,
      SuplaFunction.HUMIDITY_AND_TEMPERATURE,
      SuplaFunction.ELECTRICITY_METER,
      SuplaFunction.IC_ELECTRICITY_METER,
      SuplaFunction.IC_GAS_METER,
      SuplaFunction.IC_WATER_METER,
      SuplaFunction.HVAC_THERMOSTAT,
      SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
      SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
      SuplaFunction.HVAC_HRV,
      SuplaFunction.IC_HEAT_METER,
      SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
      SuplaFunction.GENERAL_PURPOSE_METER,
      SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaFunction.TERRACE_AWNING,
      SuplaFunction.CURTAIN,
      SuplaFunction.PROJECTOR_SCREEN,
      SuplaFunction.VERTICAL_BLIND,
      SuplaFunction.ROLLER_GARAGE_DOOR,
      SuplaFunction.CONTAINER,
      SuplaFunction.SEPTIC_TANK,
      SuplaFunction.WATER_TANK,
      SuplaFunction.VALVE_OPEN_CLOSE,
      SuplaFunction.VALVE_PERCENTAGE,
      SuplaFunction.HUMIDITY,
      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.POWER_SWITCH,
      SuplaFunction.STAIRCASE_TIMER,
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaFunction.CONTROLLING_THE_GATE,
      SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
      SuplaFunction.DIMMER,
      SuplaFunction.DIMMER_CCT,
      SuplaFunction.RGB_LIGHTING,
      SuplaFunction.DIMMER_AND_RGB_LIGHTING,
      SuplaFunction.DIMMER_CCT_AND_RGB -> true
      SuplaFunction.UNKNOWN,
      SuplaFunction.NONE,
      SuplaFunction.OPEN_SENSOR_GATEWAY,
      SuplaFunction.OPEN_SENSOR_GATE,
      SuplaFunction.OPEN_SENSOR_GARAGE_DOOR,
      SuplaFunction.NO_LIQUID_SENSOR,
      SuplaFunction.OPEN_SENSOR_DOOR,
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
      SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
      SuplaFunction.DIGIGLASS_HORIZONTAL,
      SuplaFunction.DIGIGLASS_VERTICAL,
      SuplaFunction.PUMP_SWITCH,
      SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH,
      SuplaFunction.CONTAINER_LEVEL_SENSOR,
      SuplaFunction.FLOOD_SENSOR,
      SuplaFunction.MOTION_SENSOR,
      SuplaFunction.BINARY_SENSOR,
      SuplaFunction.SMOKE_SENSOR,
      SuplaFunction.CARBON_MONOXIDE_SENSOR,
      SuplaFunction.GAS_SENSOR -> false
    }

  protected fun loadServerUrl(handler: (CloudUrl) -> Unit) {
    loadActiveProfileUrlUseCase?.invoke()
      ?.attach()
      ?.subscribeBy(
        onSuccess = handler,
        onError = defaultErrorHandler("loadServerUrl")
      )
      ?.disposeBySelf()
  }
}
