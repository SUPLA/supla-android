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

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.lib.SuplaChannelValue
import org.supla.android.lib.SuplaClientMessageHandler.OnSuplaClientMessageListener
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.authorize.AuthorizationModelState
import org.supla.android.ui.dialogs.authorize.BaseAuthorizationViewModel
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.profile.CloudUrl
import org.supla.android.usecases.profile.LoadActiveProfileUrlUseCase
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.general.SuplaFunction

abstract class BaseListViewModel<S : AuthorizationModelState, E : ViewEvent>(
  private val preferences: Preferences,
  roomProfileRepository: RoomProfileRepository,
  suplaClientProvider: SuplaClientProvider,
  authorizeUseCase: AuthorizeUseCase,
  schedulers: SuplaSchedulers,
  loginUseCase: LoginUseCase,
  defaultState: S,
  private val loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase? = null,
) : BaseAuthorizationViewModel<S, E>(suplaClientProvider, roomProfileRepository, loginUseCase, authorizeUseCase, defaultState, schedulers) {

  private val preferencesChangeListener = OnSharedPreferenceChangeListener { _, key ->
    if (key.equals(Preferences.pref_channel_height)) {
      sendReassignEvent()
    }
  }
  private val suplaMessageListener: OnSuplaClientMessageListener = OnSuplaClientMessageListener { onSuplaMessage(it) }

  init {
    preferences.registerChangeListener(preferencesChangeListener)
  }

  @CallSuper
  override fun onCleared() {
    preferences.unregisterChangeListener(preferencesChangeListener)
    super.onCleared()
  }

  protected abstract fun sendReassignEvent()

  protected abstract fun reloadList()

  @VisibleForTesting
  open fun onSuplaMessage(message: SuplaClientMsg) {
  }

  protected fun observeUpdates(updatesObservable: Observable<Any>) {
    updatesObservable
      .attachSilent()
      .subscribeBy(
        onNext = { reloadList() }
      )
      .disposeBySelf()
  }

  protected fun isAvailableInOffline(channel: ChannelDataBase, children: List<ChannelChildEntity>?) =
    when (channel.function) {
      SuplaFunction.THERMOMETER,
      SuplaFunction.HUMIDITY_AND_TEMPERATURE,
      SuplaFunction.ELECTRICITY_METER,
      SuplaFunction.IC_ELECTRICITY_METER,
      SuplaFunction.IC_GAS_METER,
      SuplaFunction.IC_WATER_METER,
      SuplaFunction.HVAC_THERMOSTAT,
      SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
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
      SuplaFunction.HUMIDITY -> true

      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.POWER_SWITCH,
      SuplaFunction.STAIRCASE_TIMER -> {
        if (children?.firstOrNull { it.relationType == ChannelRelationType.METER } != null) {
          true
        } else {
          when ((channel as? ChannelDataEntity)?.channelValueEntity?.subValueType) {
            SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS.toShort(),
            SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort() -> true

            else -> false
          }
        }
      }

      SuplaFunction.UNKNOWN,
      SuplaFunction.NONE,
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaFunction.CONTROLLING_THE_GATE,
      SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
      SuplaFunction.OPEN_SENSOR_GATEWAY,
      SuplaFunction.OPEN_SENSOR_GATE,
      SuplaFunction.OPEN_SENSOR_GARAGE_DOOR,
      SuplaFunction.NO_LIQUID_SENSOR,
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
      SuplaFunction.OPEN_SENSOR_DOOR,
      SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER,
      SuplaFunction.OPEN_SENSOR_ROOF_WINDOW,
      SuplaFunction.RING,
      SuplaFunction.ALARM,
      SuplaFunction.NOTIFICATION,
      SuplaFunction.DIMMER,
      SuplaFunction.RGB_LIGHTING,
      SuplaFunction.DIMMER_AND_RGB_LIGHTING,
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
      SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
      SuplaFunction.DIGIGLASS_HORIZONTAL,
      SuplaFunction.DIGIGLASS_VERTICAL,
      SuplaFunction.PUMP_SWITCH,
      SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH,
      SuplaFunction.CONTAINER_LEVEL_SENSOR,
      SuplaFunction.FLOOD_SENSOR -> false
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
