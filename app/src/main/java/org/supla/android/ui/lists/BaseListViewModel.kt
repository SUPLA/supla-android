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
import org.supla.android.lib.SuplaChannelValue
import org.supla.android.lib.SuplaClientMessageHandler.OnSuplaClientMessageListener
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.lib.SuplaConst
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

  protected fun isAvailableInOffline(function: Int, subValueType: Short?) = when (function) {
    SuplaConst.SUPLA_CHANNELFNC_THERMOMETER,
    SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE,
    SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER,
    SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
    SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER,
    SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER,
    SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT,
    SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER,
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND,
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW,
    SuplaConst.SUPLA_CHANNELFNC_TERRACE_AWNING,
    SuplaConst.SUPLA_CHANNELFNC_PROJECTOR_SCREEN -> true

    SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH,
    SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
    SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER -> {
      when (subValueType) {
        SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS.toShort(),
        SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort() -> true

        else -> false
      }
    }

    else -> false
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
