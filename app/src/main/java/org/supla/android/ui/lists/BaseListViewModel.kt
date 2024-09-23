package org.supla.android.ui.lists

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelFunction
import org.supla.android.lib.SuplaChannelValue
import org.supla.android.lib.SuplaClientMessageHandler.OnSuplaClientMessageListener
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.profile.CloudUrl
import org.supla.android.usecases.profile.LoadActiveProfileUrlUseCase

abstract class BaseListViewModel<S : ViewState, E : ViewEvent>(
  private val preferences: Preferences,
  defaultState: S,
  schedulers: SuplaSchedulers,
  private val loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase? = null,
) : BaseViewModel<S, E>(defaultState, schedulers) {

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
      SuplaChannelFunction.THERMOMETER,
      SuplaChannelFunction.HUMIDITY_AND_TEMPERATURE,
      SuplaChannelFunction.ELECTRICITY_METER,
      SuplaChannelFunction.IC_ELECTRICITY_METER,
      SuplaChannelFunction.IC_GAS_METER,
      SuplaChannelFunction.IC_WATER_METER,
      SuplaChannelFunction.HVAC_THERMOSTAT,
      SuplaChannelFunction.HVAC_DOMESTIC_HOT_WATER,
      SuplaChannelFunction.IC_HEAT_METER,
      SuplaChannelFunction.GENERAL_PURPOSE_MEASUREMENT,
      SuplaChannelFunction.GENERAL_PURPOSE_METER,
      SuplaChannelFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaChannelFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaChannelFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaChannelFunction.TERRACE_AWNING,
      SuplaChannelFunction.CURTAIN,
      SuplaChannelFunction.PROJECTOR_SCREEN,
      SuplaChannelFunction.VERTICAL_BLIND,
      SuplaChannelFunction.ROLLER_GARAGE_DOOR -> true

      SuplaChannelFunction.LIGHTSWITCH,
      SuplaChannelFunction.POWER_SWITCH,
      SuplaChannelFunction.STAIRCASE_TIMER -> {
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

      SuplaChannelFunction.UNKNOWN,
      SuplaChannelFunction.NONE,
      SuplaChannelFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaChannelFunction.CONTROLLING_THE_GATE,
      SuplaChannelFunction.CONTROLLING_THE_GARAGE_DOOR,
      SuplaChannelFunction.HUMIDITY,
      SuplaChannelFunction.OPEN_SENSOR_GATEWAY,
      SuplaChannelFunction.OPEN_SENSOR_GATE,
      SuplaChannelFunction.OPEN_SENSOR_GARAGE_DOOR,
      SuplaChannelFunction.NO_LIQUID_SENSOR,
      SuplaChannelFunction.CONTROLLING_THE_DOOR_LOCK,
      SuplaChannelFunction.OPEN_SENSOR_DOOR,
      SuplaChannelFunction.OPEN_SENSOR_ROLLER_SHUTTER,
      SuplaChannelFunction.OPEN_SENSOR_ROOF_WINDOW,
      SuplaChannelFunction.RING,
      SuplaChannelFunction.ALARM,
      SuplaChannelFunction.NOTIFICATION,
      SuplaChannelFunction.DIMMER,
      SuplaChannelFunction.RGB_LIGHTING,
      SuplaChannelFunction.DIMMER_AND_RGB_LIGHTING,
      SuplaChannelFunction.DEPTH_SENSOR,
      SuplaChannelFunction.DISTANCE_SENSOR,
      SuplaChannelFunction.OPENING_SENSOR_WINDOW,
      SuplaChannelFunction.HOTEL_CARD_SENSOR,
      SuplaChannelFunction.ALARM_ARMAMENT_SENSOR,
      SuplaChannelFunction.MAIL_SENSOR,
      SuplaChannelFunction.WIND_SENSOR,
      SuplaChannelFunction.PRESSURE_SENSOR,
      SuplaChannelFunction.RAIN_SENSOR,
      SuplaChannelFunction.WEIGHT_SENSOR,
      SuplaChannelFunction.WEATHER_STATION,
      SuplaChannelFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
      SuplaChannelFunction.HVAC_THERMOSTAT_HEAT_COOL,
      SuplaChannelFunction.VALVE_OPEN_CLOSE,
      SuplaChannelFunction.VALVE_PERCENTAGE,
      SuplaChannelFunction.DIGIGLASS_HORIZONTAL,
      SuplaChannelFunction.DIGIGLASS_VERTICAL,
      SuplaChannelFunction.PUMP_SWITCH,
      SuplaChannelFunction.HEAT_OR_COLD_SOURCE_SWITCH -> false
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
