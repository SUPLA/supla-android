package org.supla.android.features.thermostatdetail.thermostatgeneral
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

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.core.networking.suplaclient.DelayableState
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.local.entity.ThermostatValue
import org.supla.android.data.source.local.temperature.TemperatureCorrection
import org.supla.android.data.source.remote.ChannelConfigResult
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.hvac.SuplaChannelHvacConfig
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlags
import org.supla.android.db.Channel
import org.supla.android.events.ConfigEventsManager
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.extensions.fromSuplaTemperature
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.ifTrue
import org.supla.android.extensions.mapMerged
import org.supla.android.features.thermostatdetail.thermostatgeneral.data.ThermostatIssueItem
import org.supla.android.features.thermostatdetail.thermostatgeneral.ui.ThermostatGeneralViewProxy
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.data.IssueIconType
import org.supla.android.usecases.channel.ChannelWithChildren
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.thermostat.CreateTemperaturesListUseCase
import javax.inject.Inject
import kotlin.math.roundToInt

private const val REFRESH_DELAY_MS = 3000

@HiltViewModel
class ThermostatGeneralViewModel @Inject constructor(
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val createTemperaturesListUseCase: CreateTemperaturesListUseCase,
  private val valuesFormatter: ValuesFormatter,
  private val delayedThermostatActionSubject: DelayedThermostatActionSubject,
  private val configEventsManager: ConfigEventsManager,
  private val suplaClientProvider: SuplaClientProvider,
  private val loadingTimeoutManager: LoadingTimeoutManager,
  schedulers: SuplaSchedulers
) : BaseViewModel<ThermostatGeneralViewState, ThermostatGeneralViewEvent>(ThermostatGeneralViewState(), schedulers),
  ThermostatGeneralViewProxy {

  override fun onViewCreated() {
    loadingTimeoutManager.watch({ currentState().loadingState }) {
      updateState { state ->
        state.viewModelState?.remoteId?.let {
          loadChannel(it)
        }

        state.copy(loadingState = state.loadingState.copy(false))
      }
    }.disposeBySelf()
  }

  fun loadChannel(remoteId: Int) {
    Maybe.zip(
      readChannelWithChildrenUseCase(remoteId).mapMerged { createTemperaturesListUseCase.invoke(it) },
      configEventsManager.observerConfig(remoteId)
        .filter { it.config is SuplaChannelHvacConfig && it.result == ChannelConfigResult.RESULT_TRUE }.firstElement()
    ) { pair, config ->
      LoadedData(pair.first, pair.second, config.config as SuplaChannelHvacConfig)
    }
      .attachSilent()
      .subscribeBy(
        onSuccess = { handleData(it) }
      )
      .disposeBySelf()

    suplaClientProvider.provide()?.getChannelConfig(remoteId, ChannelConfigType.DEFAULT)
  }

  fun loadTemperature(remoteId: Int) {
    val state = currentState()

    if (state.temperatures.firstOrNull { it.thermometerRemoteId == remoteId } != null) {
      state.viewModelState?.remoteId?.let { loadChannel(it) }
    }
  }

  override fun heatingModeChanged() {
    currentState().viewModelState?.let { viewModelState ->
      val newMode = when (val mode = viewModelState.mode) {
        SuplaHvacMode.AUTO -> SuplaHvacMode.COOL
        SuplaHvacMode.COOL -> SuplaHvacMode.HEAT
        SuplaHvacMode.HEAT -> SuplaHvacMode.COOL
        else -> mode
      }

      val newState = viewModelState.copy(mode = newMode)
      updateState { it.copy(viewModelState = newState) }
      delayedThermostatActionSubject.emit(newState)
    }
  }

  override fun coolingModeChanged() {
    currentState().viewModelState?.let { viewModelState ->
      val newMode = when (val mode = viewModelState.mode) {
        SuplaHvacMode.AUTO -> SuplaHvacMode.HEAT
        SuplaHvacMode.COOL -> SuplaHvacMode.HEAT
        SuplaHvacMode.HEAT -> SuplaHvacMode.COOL
        else -> mode
      }

      val newState = viewModelState.copy(mode = newMode)
      updateState { it.copy(viewModelState = newState) }
      delayedThermostatActionSubject.emit(newState)
    }
  }

  override fun setpointTemperatureChanged(minPercentage: Float?, maxPercentage: Float?) {
    val state = currentState()

    state.viewModelState?.let { viewModelState ->
      updateState { it.copy(changing = false) }

      if (minPercentage != null && minPercentage != state.setpointMinTemperaturePercentage) {
        val temperature = getTemperatureForPosition(minPercentage, viewModelState)
        val newState = state.viewModelState.copy(
          lastChangedMin = true,
          setpointMinTemperature = temperature
        )
        updateState {
          it.copy(
            setpointMinTemperaturePercentage = minPercentage,
            viewModelState = newState,
            canIncreaseTemperature = temperature < it.configMaxTemperature,
            canDecreaseTemperature = temperature > it.configMinTemperature,
            lastInteractionTime = System.currentTimeMillis()
          )
        }
        delayedThermostatActionSubject.emit(newState)
      } else if (maxPercentage != null && maxPercentage != state.setpointMaxTemperaturePercentage) {
        val temperature = getTemperatureForPosition(maxPercentage, viewModelState)
        val newState = state.viewModelState.copy(
          lastChangedMin = false,
          setpointMaxTemperature = temperature
        )
        updateState {
          it.copy(
            setpointMaxTemperaturePercentage = maxPercentage,
            viewModelState = newState,
            canIncreaseTemperature = temperature < it.configMaxTemperature,
            canDecreaseTemperature = temperature > it.configMinTemperature,
            lastInteractionTime = System.currentTimeMillis()
          )
        }
        delayedThermostatActionSubject.emit(newState)
      }
    }
  }

  override fun changeSetpointTemperature(correction: TemperatureCorrection) {
    currentState().viewModelState?.let { viewModelState ->
      if (viewModelState.lastChangedMin) {
        changeMinTemperature(viewModelState, correction.step())
      } else {
        changeMaxTemperature(viewModelState, correction.step())
      }
    }
  }

  override fun turnOnOffClicked() {
    val state = currentState()

    state.viewModelState?.let { viewModelState ->
      updateState { it.copy(loadingState = it.loadingState.copy(true), lastInteractionTime = null) }

      val newMode = when {
        state.programmedModeActive && state.isOff -> SuplaHvacMode.OFF
        state.isOff -> SuplaHvacMode.CMD_TURN_ON
        else -> SuplaHvacMode.OFF
      }

      delayedThermostatActionSubject.sendImmediately(
        viewModelState.copy(
          mode = newMode,
          setpointMaxTemperature = null,
          setpointMinTemperature = null
        )
      )
        .attachSilent()
        .subscribeBy()
        .disposeBySelf()
    }
  }

  override fun manualModeClicked() {
    val state = currentState()

    state.viewModelState?.let { viewModelState ->
      updateState { it.copy(loadingState = it.loadingState.copy(true), lastInteractionTime = null) }

      delayedThermostatActionSubject.sendImmediately(
        viewModelState.copy(
          mode = SuplaHvacMode.CMD_SWITCH_TO_MANUAL,
          setpointMinTemperature = null,
          setpointMaxTemperature = null
        )
      )
        .attachSilent()
        .subscribeBy()
        .disposeBySelf()
    }
  }

  override fun weeklyScheduledModeClicked() {
    val state = currentState()

    state.viewModelState?.let { viewModelState ->
      updateState { it.copy(loadingState = it.loadingState.copy(true), lastInteractionTime = null) }

      delayedThermostatActionSubject.sendImmediately(
        viewModelState.copy(
          mode = SuplaHvacMode.CMD_WEEKLY_SCHEDULE,
          setpointMinTemperature = null,
          setpointMaxTemperature = null
        )
      )
        .attachSilent()
        .subscribeBy()
        .disposeBySelf()
    }
  }

  override fun getTemperatureText(minPercentage: Float?, maxPercentage: Float?, state: ThermostatGeneralViewState): StringProvider {
    val minTemperature = minPercentage?.let {
      getTemperatureForPosition(it, state.viewModelState!!)
    }
    val maxTemperature = maxPercentage?.let {
      getTemperatureForPosition(it, state.viewModelState!!)
    }

    return state.viewModelState?.let {
      calculateTemperatureControlText(state.isOffline, it.mode, minTemperature, maxTemperature)
    } ?: { "" }
  }

  override fun markChanging() {
    updateState { it.copy(changing = true) }
  }

  private fun getTemperatureForPosition(percentagePosition: Float, viewModelState: ThermostatGeneralViewModelState) =
    viewModelState.configMinTemperature.plus(
      viewModelState.configMaxTemperature.minus(viewModelState.configMinTemperature).times(percentagePosition)
    ).times(10).roundToInt().toFloat().div(10)

  private fun handleData(data: LoadedData) {
    val channel = data.channelWithChildren.channel
    val value = channel.value.asThermostatValue()

    val setpointMinTemperature = getSetpointMinTemperature(channel, value)
    val setpointMaxTemperature = getSetpointMaxTemperature(channel, value)

    val (configMinTemperature) = guardLet(data.config.temperatures.roomMin?.fromSuplaTemperature()) { return }
    val (configMaxTemperature) = guardLet(data.config.temperatures.roomMax?.fromSuplaTemperature()) { return }

    val range = configMaxTemperature - configMinTemperature
    val isOff = channel.onLine.not() || value.mode == SuplaHvacMode.OFF || value.mode == SuplaHvacMode.NOT_SET

    val canIncreaseTemperature = when (value.mode) {
      SuplaHvacMode.COOL -> if (setpointMaxTemperature != null) setpointMaxTemperature < configMaxTemperature else true
      else -> if (setpointMinTemperature != null) setpointMinTemperature < configMaxTemperature else true
    }
    val canDecreaseTemperature = when (value.mode) {
      SuplaHvacMode.COOL -> if (setpointMaxTemperature != null) setpointMaxTemperature > configMinTemperature else true
      else -> if (setpointMinTemperature != null) setpointMinTemperature > configMinTemperature else true
    }

    updateState {
      if (it.changing) {
        return@updateState it // Do not change anything, when user makes manual operations
      }
      if (it.lastInteractionTime != null && it.lastInteractionTime + REFRESH_DELAY_MS > System.currentTimeMillis()) {
        it.viewModelState?.remoteId?.let { id -> loadChannel(id) }
        return@updateState it // Do not change anything during 3 secs after last user interaction
      }

      it.copy(
        viewModelState = ThermostatGeneralViewModelState(
          remoteId = channel.remoteId,
          function = channel.func,
          lastChangedMin = it.viewModelState?.lastChangedMin ?: (setpointMinTemperature != null),
          setpointMinTemperature = setpointMinTemperature,
          setpointMaxTemperature = setpointMaxTemperature,
          configMinTemperature = configMinTemperature,
          configMaxTemperature = configMaxTemperature,
          mode = value.mode
        ),

        temperatures = data.temperatures,

        isOffline = !channel.onLine,
        isOff = isOff,
        isAutoFunction = channel.func == SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO,
        heatingModeActive = isHeatingModeActive(channel, value),
        coolingModeActive = isCoolingModeActive(channel, value),
        canIncreaseTemperature = canIncreaseTemperature,
        canDecreaseTemperature = canDecreaseTemperature,

        isCurrentlyHeating = value.state.isOn() && value.flags.contains(SuplaThermostatFlags.HEATING),
        isCurrentlyCooling = value.state.isOn() && value.flags.contains(SuplaThermostatFlags.COOLING),

        setpointTemperatureProvider = calculateTemperatureControlText(
          channel.onLine.not(),
          value.mode,
          setpointMinTemperature,
          setpointMaxTemperature
        ),
        configMaxTemperature = configMaxTemperature,
        configMinTemperature = configMinTemperature,
        configMinTemperatureString = valuesFormatter.getTemperatureString(configMinTemperature.toDouble()),
        configMaxTemperatureString = valuesFormatter.getTemperatureString(configMaxTemperature.toDouble()),

        setpointMinTemperaturePercentage = setpointMinTemperature?.minus(configMinTemperature)?.div(range),
        setpointMaxTemperaturePercentage = setpointMaxTemperature?.minus(configMinTemperature)?.div(range),
        currentTemperaturePercentage = calculateCurrentTemperature(data.channelWithChildren, configMinTemperature, configMaxTemperature),

        manualModeActive = isOff.not() && value.flags.contains(SuplaThermostatFlags.WEEKLY_SCHEDULE).not(),
        programmedModeActive = channel.onLine && value.flags.contains(SuplaThermostatFlags.WEEKLY_SCHEDULE),

        issues = createThermostatIssues(value.flags),

        loadingState = it.loadingState.copy(false)
      )
    }
  }

  private fun isHeatingModeActive(channel: Channel, value: ThermostatValue) =
    channel.func == SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO &&
      (value.mode == SuplaHvacMode.AUTO || value.mode == SuplaHvacMode.HEAT)

  private fun isCoolingModeActive(channel: Channel, value: ThermostatValue) =
    channel.func == SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO &&
      (value.mode == SuplaHvacMode.AUTO || value.mode == SuplaHvacMode.COOL)

  private fun calculateTemperatureControlText(
    isOffline: Boolean,
    mode: SuplaHvacMode,
    setpointMinTemperature: Float?,
    setpointMaxTemperature: Float?
  ): StringProvider {
    return when {
      isOffline -> { resources -> resources.getString(R.string.offline) }
      mode == SuplaHvacMode.NOT_SET || mode == SuplaHvacMode.OFF -> { resources ->
        resources.getString(R.string.thermostat_detail_off).lowercase()
      }

      else -> { _ -> getOnlineTemperatureText(setpointMinTemperature, setpointMaxTemperature) }
    }
  }

  private fun getOnlineTemperatureText(setpointMinTemperature: Float?, setpointMaxTemperature: Float?): String {
    val setPointMinTemperatureString = setpointMinTemperature?.let { valuesFormatter.getTemperatureString(it.toDouble()) }
    val setPointMaxTemperatureString = setpointMaxTemperature?.let { valuesFormatter.getTemperatureString(it.toDouble()) }

    return when {
      setPointMinTemperatureString != null && setPointMaxTemperatureString != null ->
        "$setPointMinTemperatureString - $setPointMaxTemperatureString"

      setPointMinTemperatureString != null -> setPointMinTemperatureString
      setPointMaxTemperatureString != null -> setPointMaxTemperatureString
      else -> ""
    }
  }

  private fun calculateCurrentTemperature(data: ChannelWithChildren, configMinTemperature: Float, configMaxTemperature: Float): Float? {
    return data.children.firstOrNull { it.relationType == ChannelRelationType.MAIN_THERMOMETER }?.let {
      val temperature = it.channel.value.getTemp(it.channel.func)

      if (temperature < -273) {
        null
      } else if (temperature < configMinTemperature) {
        0f
      } else if (temperature > configMaxTemperature) {
        1f
      } else {
        val range = configMaxTemperature - configMinTemperature
        temperature.minus(configMinTemperature).div(range).toFloat()
      }
    }
  }

  private fun Channel.setpointMinTemperatureSupported(): Boolean =
    func == SUPLA_CHANNELFNC_HVAC_THERMOSTAT ||
      func == SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO ||
      func == SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER

  private fun Channel.setpointMaxTemperatureSupported(): Boolean =
    func == SUPLA_CHANNELFNC_HVAC_THERMOSTAT ||
      func == SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO

  private fun getSetpointMinTemperature(channel: Channel, thermostatValue: ThermostatValue): Float? =
    (
      channel.setpointMinTemperatureSupported() &&
        thermostatValue.flags.contains(SuplaThermostatFlags.SETPOINT_TEMP_MIN_SET) &&
        (thermostatValue.mode == SuplaHvacMode.HEAT || thermostatValue.mode == SuplaHvacMode.AUTO)
      )
      .ifTrue(thermostatValue.setpointTemperatureHeat)

  private fun getSetpointMaxTemperature(channel: Channel, thermostatValue: ThermostatValue): Float? =
    (
      channel.setpointMaxTemperatureSupported() &&
        thermostatValue.flags.contains(SuplaThermostatFlags.SETPOINT_TEMP_MAX_SET) &&
        (thermostatValue.mode == SuplaHvacMode.COOL || thermostatValue.mode == SuplaHvacMode.AUTO)
      )
      .ifTrue(thermostatValue.setpointTemperatureCool)

  private fun changeMinTemperature(viewModelState: ThermostatGeneralViewModelState, step: Float) {
    viewModelState.setpointMinTemperature?.let {
      val minTemperature = viewModelState.configMinTemperature
      val maxTemperature = viewModelState.configMaxTemperature
      val temperature = it.plus(step).let { temperature ->
        if (temperature < minTemperature) {
          minTemperature
        } else if (temperature > maxTemperature) {
          maxTemperature
        } else {
          temperature
        }
      }

      val range = maxTemperature - minTemperature

      val newState = viewModelState.copy(setpointMinTemperature = temperature)
      updateState { state ->
        state.copy(
          viewModelState = newState,
          setpointTemperatureProvider = calculateTemperatureControlText(
            state.isOffline,
            viewModelState.mode,
            temperature,
            viewModelState.setpointMaxTemperature
          ),
          setpointMinTemperaturePercentage = temperature.minus(minTemperature).div(range),
          canIncreaseTemperature = temperature < maxTemperature,
          canDecreaseTemperature = temperature > minTemperature,
          lastInteractionTime = System.currentTimeMillis(),
          changing = false
        )
      }
      delayedThermostatActionSubject.emit(newState)
    }
  }

  private fun changeMaxTemperature(viewModelState: ThermostatGeneralViewModelState, step: Float) {
    viewModelState.setpointMaxTemperature?.let {
      val minTemperature = viewModelState.configMinTemperature
      val maxTemperature = viewModelState.configMaxTemperature
      val temperature = it.plus(step).let { temperature ->
        if (temperature < minTemperature) {
          minTemperature
        } else if (temperature > maxTemperature) {
          maxTemperature
        } else {
          temperature
        }
      }

      val range = maxTemperature - minTemperature

      val newState = viewModelState.copy(setpointMaxTemperature = temperature)
      updateState { state ->
        state.copy(
          viewModelState = newState,
          setpointTemperatureProvider = calculateTemperatureControlText(
            state.isOffline,
            viewModelState.mode,
            viewModelState.setpointMinTemperature,
            temperature
          ),
          setpointMaxTemperaturePercentage = temperature.minus(minTemperature).div(range),
          canIncreaseTemperature = temperature < maxTemperature,
          canDecreaseTemperature = temperature > minTemperature,
          lastInteractionTime = System.currentTimeMillis(),
          changing = false
        )
      }
      delayedThermostatActionSubject.emit(newState)
    }
  }

  private fun createThermostatIssues(flags: List<SuplaThermostatFlags>): List<ThermostatIssueItem> =
    mutableListOf<ThermostatIssueItem>().apply {
      if (flags.contains(SuplaThermostatFlags.THERMOMETER_ERROR)) {
        add(ThermostatIssueItem(IssueIconType.ERROR, R.string.thermostat_thermometer_error))
      }
      if (flags.contains(SuplaThermostatFlags.CLOCK_ERROR)) {
        add(ThermostatIssueItem(IssueIconType.WARNING, R.string.thermostat_clock_error))
      }
    }

  private data class LoadedData(
    val channelWithChildren: ChannelWithChildren,
    val temperatures: List<ThermostatTemperature>,
    val config: SuplaChannelHvacConfig
  )
}

sealed class ThermostatGeneralViewEvent : ViewEvent

data class ThermostatGeneralViewState(
  val viewModelState: ThermostatGeneralViewModelState? = null,

  val temperatures: List<ThermostatTemperature> = emptyList(),

  val isOffline: Boolean = false,
  val isOff: Boolean = false,

  val isAutoFunction: Boolean = false,
  val heatingModeActive: Boolean = false,
  val coolingModeActive: Boolean = false,
  val isCurrentlyHeating: Boolean = false,
  val isCurrentlyCooling: Boolean = false,
  val canDecreaseTemperature: Boolean = true,
  val canIncreaseTemperature: Boolean = true,

  val setpointTemperatureProvider: StringProvider = { "" },
  val configMinTemperature: Float = 0f,
  val configMaxTemperature: Float = 0f,
  val configMinTemperatureString: String = "",
  val configMaxTemperatureString: String = "",

  val setpointMinTemperaturePercentage: Float? = null,
  val setpointMaxTemperaturePercentage: Float? = null,
  val currentTemperaturePercentage: Float? = null,

  val manualModeActive: Boolean = false,
  val programmedModeActive: Boolean = false,

  val issues: List<ThermostatIssueItem> = emptyList(),

  val loadingState: LoadingTimeoutManager.LoadingState = LoadingTimeoutManager.LoadingState(),
  val lastInteractionTime: Long? = null,
  val changing: Boolean = false
) : ViewState()

data class ThermostatTemperature(
  val thermometerRemoteId: Int,
  val iconProvider: (context: Context) -> Bitmap,
  val temperature: String
)

data class ThermostatGeneralViewModelState(
  val remoteId: Int,
  val function: Int,
  val lastChangedMin: Boolean,
  val configMinTemperature: Float,
  val configMaxTemperature: Float,
  val mode: SuplaHvacMode,
  val setpointMinTemperature: Float? = null,
  val setpointMaxTemperature: Float? = null,
  override val sent: Boolean = false
) : DelayableState {

  override fun sentState(): DelayableState = copy(sent = true)
  override fun delayableCopy(): DelayableState = copy()
}
