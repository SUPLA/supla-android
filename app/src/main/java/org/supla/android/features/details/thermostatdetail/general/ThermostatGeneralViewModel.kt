package org.supla.android.features.details.thermostatdetail.general
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

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.DelayableState
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.shared.shareable
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.temperature.TemperatureCorrection
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.SuplaDeviceConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelHvacConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.data.source.remote.hvac.filterRelationType
import org.supla.android.di.FORMATTER_THERMOMETER
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.DeviceConfigEventsManager
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.extensions.ifNumber
import org.supla.android.features.details.thermostatdetail.general.data.SensorIssue
import org.supla.android.features.details.thermostatdetail.general.data.ThermostatProgramInfo
import org.supla.android.features.details.thermostatdetail.general.data.build
import org.supla.android.features.details.thermostatdetail.general.ui.ThermostatGeneralViewProxy
import org.supla.android.features.details.thermostatdetail.ui.TimerHeaderState
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.GetChannelValueUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenTreeUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.thermostat.CheckIsSlaveThermostatUseCase
import org.supla.android.usecases.thermostat.CreateTemperaturesListUseCase
import org.supla.android.usecases.thermostat.MeasurementValue
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.function.thermostat.SuplaThermostatFlag
import org.supla.core.shared.data.model.function.thermostat.ThermostatValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.extensions.ifLet
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.channel.issues.ThermostatIssuesProvider
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.roundToInt

private const val REFRESH_DELAY_MS = 3000

@HiltViewModel
class ThermostatGeneralViewModel @Inject constructor(
  private val readChannelWithChildrenTreeUseCase: ReadChannelWithChildrenTreeUseCase,
  private val delayedThermostatActionSubject: DelayedThermostatActionSubject,
  private val createTemperaturesListUseCase: CreateTemperaturesListUseCase,
  private val checkIsSlaveThermostatUseCase: CheckIsSlaveThermostatUseCase,
  private val channelConfigEventsManager: ChannelConfigEventsManager,
  private val deviceConfigEventsManager: DeviceConfigEventsManager,
  private val getChannelValueUseCase: GetChannelValueUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val loadingTimeoutManager: LoadingTimeoutManager,
  private val suplaClientProvider: SuplaClientProvider,
  private val schedulers: SuplaSchedulers,
  private val dateProvider: DateProvider,
  @Named(FORMATTER_THERMOMETER) private val thermometerValueFormatter: ValueFormatter
) : BaseViewModel<ThermostatGeneralViewState, ThermostatGeneralViewEvent>(ThermostatGeneralViewState(), schedulers),
  ThermostatGeneralViewProxy {

  private val updateSubject: BehaviorSubject<Int> = BehaviorSubject.createDefault(0)
  private val thermostatIssuesProvider = ThermostatIssuesProvider()

  override fun onViewCreated() {
    loadingTimeoutManager.watch({ currentState().loadingState }) {
      updateState { state ->
        updateViewData()
        state.copy(loadingState = state.loadingState.changingLoading(false, dateProvider))
      }
    }.disposeBySelf()
  }

  fun observeData(remoteId: Int, deviceId: Int) {
    Observable.combineLatest(
      readChannelWithChildrenTreeUseCase(remoteId),
      channelConfigEventsManager.observerConfig(remoteId)
        .filter { it.config is SuplaChannelHvacConfig && it.result == ConfigResult.RESULT_TRUE }
        .map { it.config as SuplaChannelHvacConfig },
      channelConfigEventsManager.observerConfig(remoteId)
        .filter { it.config is SuplaChannelWeeklyScheduleConfig },
      deviceConfigEventsManager.observerConfig(deviceId),
      checkIsSlaveThermostatUseCase(remoteId).toObservable(),
      Observable.merge(
        Observable.just(0),
        updateSubject.debounce(1, TimeUnit.SECONDS)
      )
    ) { channelWithChildren, hvacConfig, weeklySchedule, deviceConfig, isSlave, _ ->
      LoadedData(
        channelWithChildren = channelWithChildren,
        temperatures = createTemperaturesListUseCase(channelWithChildren),
        config = hvacConfig,
        weeklySchedule = weeklySchedule.config as SuplaChannelWeeklyScheduleConfig,
        deviceConfig = deviceConfig.config,
        isSlaveThermostat = isSlave
      )
    }
      .debounce(50, TimeUnit.MILLISECONDS, schedulers.computation)
      .attachSilent()
      .subscribeBy(
        onNext = { handleData(it) },
        onError = defaultErrorHandler("observeData($remoteId)")
      )
      .disposeBySelf()
  }

  fun loadData(remoteId: Int, deviceId: Int) {
    suplaClientProvider.provide()?.getChannelConfig(remoteId, ChannelConfigType.DEFAULT)
    suplaClientProvider.provide()?.getChannelConfig(remoteId, ChannelConfigType.WEEKLY_SCHEDULE)
    suplaClientProvider.provide()?.getDeviceConfig(deviceId)
  }

  override fun heatingModeChanged() {
    currentState().viewModelState?.let { viewModelState ->
      val newMode = when (val mode = viewModelState.mode) {
        SuplaHvacMode.HEAT_COOL -> SuplaHvacMode.COOL
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
        SuplaHvacMode.HEAT_COOL -> SuplaHvacMode.HEAT
        SuplaHvacMode.COOL -> SuplaHvacMode.HEAT
        SuplaHvacMode.HEAT -> SuplaHvacMode.COOL
        else -> mode
      }

      val newState = viewModelState.copy(mode = newMode)
      updateState { it.copy(viewModelState = newState) }
      delayedThermostatActionSubject.emit(newState)
    }
  }

  override fun setpointTemperatureChanged(heatPercentage: Float?, coolPercentage: Float?) {
    val state = currentState()

    state.viewModelState?.let { viewModelState ->
      if (heatPercentage != null && heatPercentage != state.setpointHeatTemperaturePercentage) {
        val temperature = getTemperatureForPosition(heatPercentage, viewModelState)

        updateStateForHeatChange(viewModelState.copy(setpointHeatTemperature = temperature, lastChangedHeat = true))
      } else if (coolPercentage != null && coolPercentage != state.setpointCoolTemperaturePercentage) {
        val temperature = getTemperatureForPosition(coolPercentage, viewModelState)

        updateStateForCoolChange(viewModelState.copy(setpointCoolTemperature = temperature, lastChangedHeat = false))
      } else {
        updateState { it.copy(changing = false) }
      }
    }
  }

  override fun changeSetpointTemperature(correction: TemperatureCorrection) {
    val state = currentState()
    state.viewModelState?.let { viewModelState ->
      if (viewModelState.lastChangedHeat) {
        changeHeatTemperature(state, viewModelState, correction.step())
      } else {
        changeCoolTemperature(state, viewModelState, correction.step())
      }
    }
  }

  override fun turnOnOffClicked() {
    val state = currentState()

    state.viewModelState?.let { viewModelState ->
      updateState { it.copy(loadingState = it.loadingState.changingLoading(true, dateProvider), lastInteractionTime = null) }

      val newMode = when {
        state.programmedModeActive && state.isOff -> SuplaHvacMode.OFF
        state.isOff -> SuplaHvacMode.CMD_TURN_ON
        else -> SuplaHvacMode.OFF
      }

      delayedThermostatActionSubject.sendImmediately(
        viewModelState.copy(
          mode = newMode,
          setpointCoolTemperature = null,
          setpointHeatTemperature = null
        )
      )
        .attachSilent()
        .subscribeBy(onError = defaultErrorHandler("turnOnOffClicked()"))
        .disposeBySelf()
    }
  }

  override fun manualModeClicked() {
    val state = currentState()

    state.viewModelState?.let { viewModelState ->
      updateState { it.copy(loadingState = it.loadingState.changingLoading(true, dateProvider), lastInteractionTime = null) }

      delayedThermostatActionSubject.sendImmediately(
        viewModelState.copy(
          mode = SuplaHvacMode.CMD_SWITCH_TO_MANUAL,
          setpointHeatTemperature = null,
          setpointCoolTemperature = null
        )
      )
        .attachSilent()
        .subscribeBy(onError = defaultErrorHandler("manualModeClicked()"))
        .disposeBySelf()
    }
  }

  override fun weeklyScheduledModeClicked() {
    val state = currentState()

    state.viewModelState?.let { viewModelState ->
      updateState { it.copy(loadingState = it.loadingState.changingLoading(true, dateProvider), lastInteractionTime = null) }

      delayedThermostatActionSubject.sendImmediately(
        viewModelState.copy(
          mode = SuplaHvacMode.CMD_WEEKLY_SCHEDULE,
          setpointHeatTemperature = null,
          setpointCoolTemperature = null
        )
      )
        .attachSilent()
        .subscribeBy(onError = defaultErrorHandler("weeklyScheduledModeClicked()"))
        .disposeBySelf()
    }
  }

  override fun getTemperatureText(minPercentage: Float?, maxPercentage: Float?, state: ThermostatGeneralViewState): LocalizedString {
    val minTemperature = minPercentage.ifNumber {
      getTemperatureForPosition(it, state.viewModelState!!)
    }
    val maxTemperature = maxPercentage.ifNumber {
      getTemperatureForPosition(it, state.viewModelState!!)
    }

    return state.viewModelState?.let {
      calculateTemperatureControlText(state.isOffline, it.mode, minTemperature, maxTemperature)
    } ?: LocalizedString.Empty
  }

  override fun markChanging() {
    updateState {
      it.copy(
        viewModelState = it.viewModelState?.copy(
          mode = getModeForOffChanges(it, it.viewModelState)
        ),
        changing = true
      )
    }
  }

  /**
   * Triggers [handleData] with last database state.
   */
  private fun updateViewData() {
    updateSubject.onNext(0)
  }

  private fun getTemperatureForPosition(percentagePosition: Float, viewModelState: ThermostatGeneralViewModelState) =
    viewModelState.configMinTemperature.plus(
      viewModelState.configMaxTemperature.minus(viewModelState.configMinTemperature).times(percentagePosition)
    ).times(10).roundToInt().toFloat().div(10)

  private fun handleData(data: LoadedData) {
    val channelData = data.channelWithChildren.channel
    val value = channelData.channelValueEntity
    val thermostatValue = channelData.channelValueEntity.asThermostatValue()
    val currentDate = dateProvider.currentDate()
    val timerState = channelData.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue?.let {
      if (it.countdownEndsAt?.before(currentDate) == true) {
        null
      } else {
        it
      }
    }

    val setpointHeatTemperature = getSetpointHeatTemperature(channelData, thermostatValue)
    val setpointCoolTemperature = getSetpointCoolTemperature(channelData, thermostatValue)

    val (configMinTemperature) = guardLet(data.config.minTemperature) { return }
    val (configMaxTemperature) = guardLet(data.config.maxTemperature) { return }

    val online = value.status.online
    val isOff = value.status.offline || thermostatValue.mode == SuplaHvacMode.OFF || thermostatValue.mode == SuplaHvacMode.NOT_SET
    val currentPower = if (online) thermostatValue.state.power else null

    updateState {
      if (it.changing) {
        Timber.d("update skipped because of changing")
        return@updateState it // Do not change anything, when user makes manual operations
      }
      if (it.lastInteractionTime != null && it.lastInteractionTime + REFRESH_DELAY_MS > dateProvider.currentTimestamp()) {
        Timber.d("update skipped because of last interaction time")
        updateSubject.onNext(0)
        return@updateState it // Do not change anything during 3 secs after last user interaction
      }
      Timber.d("updating state with data")

      it.copy(
        viewModelState = ThermostatGeneralViewModelState(
          remoteId = channelData.remoteId,
          function = channelData.function.value,
          lastChangedHeat = lastChangedHeat(it.viewModelState, thermostatValue, setpointHeatTemperature),
          setpointHeatTemperature = setpointHeatTemperature,
          setpointCoolTemperature = setpointCoolTemperature,
          configMinTemperature = configMinTemperature,
          configMaxTemperature = configMaxTemperature,
          mode = thermostatValue.mode,
          subfunction = thermostatValue.subfunction,
          relatedRemoteIds = getRelatedIds(data.channelWithChildren),
          timerEndDate = timerState?.countdownEndsAt
        ),

        temperatures = data.temperatures,

        buttonsDisabled = value.status.offline || data.isSlaveThermostat,
        isOffline = value.status.offline,
        isOff = isOff,
        currentPower = currentPower,
        isAutoFunction = channelData.function == SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
        heatingModeActive = isHeatingModeActive(channelData, thermostatValue),
        coolingModeActive = isCoolingModeActive(channelData, thermostatValue),

        showHeatingIndicator = isFlagActive(data.channelWithChildren, SuplaThermostatFlag.HEATING),
        showCoolingIndicator = isFlagActive(data.channelWithChildren, SuplaThermostatFlag.COOLING),
        pumpSwitchIcon = online.ifTrue { pumpSwitchIcon(data.channelWithChildren) },
        heatOrColdSourceSwitchIcon = online.ifTrue { heatOrColdSourceSwitchIcon(data.channelWithChildren) },

        configMinTemperatureString = thermometerValueFormatter.format(configMinTemperature),
        configMaxTemperatureString = thermometerValueFormatter.format(configMaxTemperature),

        currentTemperaturePercentage = calculateCurrentTemperature(data, configMinTemperature, configMaxTemperature),

        manualModeActive = isOff.not() && thermostatValue.flags.contains(SuplaThermostatFlag.WEEKLY_SCHEDULE).not(),
        programmedModeActive = online && thermostatValue.flags.contains(SuplaThermostatFlag.WEEKLY_SCHEDULE),

        temporaryChangeActive = online && thermostatValue.flags.contains(SuplaThermostatFlag.WEEKLY_SCHEDULE_TEMPORAL_OVERRIDE),
        temporaryProgramInfo = buildProgramInfo(data.weeklySchedule, data.deviceConfig, thermostatValue, online),

        sensorIssue = SensorIssue.build(thermostatValue, data.channelWithChildren.children, getChannelIconUseCase),

        issues = createThermostatIssues(data.channelWithChildren),

        loadingState = it.loadingState.changingLoading(false, dateProvider)
      )
    }
  }

  private fun isFlagActive(channelWithChildren: ChannelWithChildren, flag: SuplaThermostatFlag): Boolean {
    val children = channelWithChildren.allDescendantFlat.filter { it.relationType == ChannelRelationType.MASTER_THERMOSTAT }
    val channelHasFlag = channelWithChildren.channel.isActive(flag)

    return if (children.isEmpty()) {
      channelHasFlag
    } else {
      channelHasFlag || children.fold(false) { result, child ->
        result || child.channelDataEntity.isActive(flag)
      }
    }
  }

  private fun pumpSwitchIcon(channelWithChildren: ChannelWithChildren): ImageId? {
    channelWithChildren.pumpSwitchChild?.let { getChannelIconUseCase(it.channelDataEntity) }
      .let { if (it != null) return it }

    channelWithChildren.allDescendantFlat
      .filter { it.relationType == ChannelRelationType.PUMP_SWITCH }
      .let { if (it.size == 1) return getChannelIconUseCase(it.first().channelDataEntity) }

    return null
  }

  private fun heatOrColdSourceSwitchIcon(channelWithChildren: ChannelWithChildren): ImageId? {
    channelWithChildren.heatOrColdSourceSwitchChild?.let { getChannelIconUseCase(it.channelDataEntity) }
      .let { if (it != null) return it }

    channelWithChildren.allDescendantFlat
      .filter { it.relationType == ChannelRelationType.HEAT_OR_COLD_SOURCE_SWITCH }
      .let { if (it.size == 1) return getChannelIconUseCase(it.first().channelDataEntity) }

    return null
  }

  private fun getRelatedIds(channelWithChildren: ChannelWithChildren): List<Int> =
    channelWithChildren.allDescendantFlat
      .filter {
        it.relationType == ChannelRelationType.PUMP_SWITCH ||
          it.relationType == ChannelRelationType.HEAT_OR_COLD_SOURCE_SWITCH ||
          it.relationType == ChannelRelationType.MASTER_THERMOSTAT ||
          it.relationType.isThermometer()
      }
      .map { it.channelDataEntity.remoteId }

  private fun buildProgramInfo(
    weeklyConfig: SuplaChannelWeeklyScheduleConfig,
    deviceConfig: SuplaDeviceConfig?,
    value: ThermostatValue,
    channelOnline: Boolean
  ) =
    ThermostatProgramInfo.Builder().also {
      it.dateProvider = this@ThermostatGeneralViewModel.dateProvider
      it.weeklyScheduleConfig = weeklyConfig
      it.deviceConfig = deviceConfig
      it.thermostatFlags = value.flags
      it.currentMode = value.mode
      it.currentTemperature = when (value.subfunction) {
        ThermostatSubfunction.HEAT -> value.setpointTemperatureHeat
        ThermostatSubfunction.COOL -> value.setpointTemperatureCool
        else -> null
      }
      it.channelOnline = channelOnline
    }
      .build()

  private fun isHeatingModeActive(channel: ChannelDataEntity, value: ThermostatValue) =
    channel.function == SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL &&
      (value.mode == SuplaHvacMode.HEAT_COOL || value.mode == SuplaHvacMode.HEAT)

  private fun isCoolingModeActive(channel: ChannelDataEntity, value: ThermostatValue) =
    channel.function == SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL &&
      (value.mode == SuplaHvacMode.HEAT_COOL || value.mode == SuplaHvacMode.COOL)

  private fun calculateTemperatureControlText(
    isOffline: Boolean,
    mode: SuplaHvacMode,
    setpointMinTemperature: Float?,
    setpointMaxTemperature: Float?
  ): LocalizedString {
    return when {
      isOffline -> localizedString(R.string.offline)
      mode == SuplaHvacMode.NOT_SET || mode == SuplaHvacMode.OFF -> localizedString(R.string.thermostat_detail_off)
      else -> getOnlineTemperatureText(setpointMinTemperature, setpointMaxTemperature)
    }
  }

  private fun getOnlineTemperatureText(setpointMinTemperature: Float?, setpointMaxTemperature: Float?): LocalizedString {
    val setPointMinTemperatureString = setpointMinTemperature?.let { thermometerValueFormatter.format(it.toDouble()) }
    val setPointMaxTemperatureString = setpointMaxTemperature?.let { thermometerValueFormatter.format(it.toDouble()) }

    return when {
      setPointMinTemperatureString != null && setPointMaxTemperatureString != null ->
        LocalizedString.Constant("$setPointMinTemperatureString - $setPointMaxTemperatureString")

      setPointMinTemperatureString != null -> LocalizedString.Constant(setPointMinTemperatureString)
      setPointMaxTemperatureString != null -> LocalizedString.Constant(setPointMaxTemperatureString)
      else -> LocalizedString.Empty
    }
  }

  private fun calculateCurrentTemperature(
    data: LoadedData,
    configMinTemperature: Float,
    configMaxTemperature: Float
  ): Float? {
    return data.channelWithChildren.children.firstOrNull { data.config.temperatureControlType.filterRelationType(it.relationType) }?.let {
      val temperature: Double = getChannelValueUseCase(it.withChildren)

      if (temperature <= -273) {
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

  private fun getSetpointHeatTemperature(channel: ChannelDataEntity, thermostatValue: ThermostatValue): Float? {
    val setpointSet = thermostatValue.flags.contains(SuplaThermostatFlag.SETPOINT_TEMP_MIN_SET)
    if (channel.function == SuplaFunction.HVAC_DOMESTIC_HOT_WATER && setpointSet) {
      return thermostatValue.setpointTemperatureHeat
    }
    if (channel.function == SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL && setpointSet) {
      return thermostatValue.setpointTemperatureHeat
    }
    val isHeatSubfunction = thermostatValue.subfunction == ThermostatSubfunction.HEAT
    if (channel.function == SuplaFunction.HVAC_THERMOSTAT && isHeatSubfunction && setpointSet) {
      return thermostatValue.setpointTemperatureHeat
    }

    return null
  }

  private fun getSetpointCoolTemperature(channel: ChannelDataEntity, thermostatValue: ThermostatValue): Float? {
    val setpointSet = thermostatValue.flags.contains(SuplaThermostatFlag.SETPOINT_TEMP_MAX_SET)
    if (channel.function == SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL && setpointSet) {
      return thermostatValue.setpointTemperatureCool
    }
    val isCoolSubfunction = thermostatValue.subfunction == ThermostatSubfunction.COOL
    if (channel.function == SuplaFunction.HVAC_THERMOSTAT && isCoolSubfunction && setpointSet) {
      return thermostatValue.setpointTemperatureCool
    }

    return null
  }

  private fun changeHeatTemperature(state: ThermostatGeneralViewState, viewModelState: ThermostatGeneralViewModelState, step: Float) {
    viewModelState.setpointHeatTemperature?.let {
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

      updateStateForHeatChange(
        viewModelState.copy(
          setpointHeatTemperature = temperature,
          mode = getModeForOffChanges(state, viewModelState)
        )
      )
    }
  }

  private fun updateStateForHeatChange(viewModelState: ThermostatGeneralViewModelState) {
    updateState { state ->
      if (state.programmedModeActive) {
        delayedThermostatActionSubject.emit(viewModelState.copy(mode = SuplaHvacMode.NOT_SET))
      } else {
        delayedThermostatActionSubject.emit(viewModelState)
      }

      state.copy(
        viewModelState = viewModelState,
        lastInteractionTime = dateProvider.currentTimestamp(),
        changing = false
      )
    }
  }

  private fun changeCoolTemperature(state: ThermostatGeneralViewState, viewModelState: ThermostatGeneralViewModelState, step: Float) {
    viewModelState.setpointCoolTemperature?.let {
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

      updateStateForCoolChange(
        viewModelState.copy(
          setpointCoolTemperature = temperature,
          mode = getModeForOffChanges(state, viewModelState)
        )
      )
    }
  }

  private fun updateStateForCoolChange(viewModelState: ThermostatGeneralViewModelState) {
    updateState { state ->
      if (state.programmedModeActive) {
        delayedThermostatActionSubject.emit(viewModelState.copy(mode = SuplaHvacMode.NOT_SET))
      } else {
        delayedThermostatActionSubject.emit(viewModelState)
      }

      state.copy(
        viewModelState = viewModelState,
        lastInteractionTime = dateProvider.currentTimestamp(),
        changing = false
      )
    }
  }

  private fun createThermostatIssues(channelWithChildren: ChannelWithChildren): List<ChannelIssueItem> =
    thermostatIssuesProvider.provide(channelWithChildren.shareable)

  private fun getModeForOffChanges(state: ThermostatGeneralViewState, modelState: ThermostatGeneralViewModelState): SuplaHvacMode =
    if (modelState.mode == SuplaHvacMode.OFF && state.isOffline.not() && state.programmedModeActive) {
      if (modelState.lastChangedHeat) {
        SuplaHvacMode.HEAT
      } else {
        SuplaHvacMode.COOL
      }
    } else {
      modelState.mode
    }

  private fun lastChangedHeat(state: ThermostatGeneralViewModelState?, value: ThermostatValue, setpointHeatTemperature: Float?): Boolean {
    return if (state == null) {
      (setpointHeatTemperature != null)
    } else if (state.subfunction != null && state.subfunction != value.subfunction) {
      value.subfunction == ThermostatSubfunction.HEAT
    } else {
      state.lastChangedHeat
    }
  }

  private data class LoadedData(
    val channelWithChildren: ChannelWithChildren,
    val temperatures: List<MeasurementValue>,
    val config: SuplaChannelHvacConfig,
    val weeklySchedule: SuplaChannelWeeklyScheduleConfig,
    val deviceConfig: SuplaDeviceConfig?,
    val isSlaveThermostat: Boolean
  )
}

sealed class ThermostatGeneralViewEvent : ViewEvent

data class ThermostatGeneralViewState(
  val viewModelState: ThermostatGeneralViewModelState? = null,

  val temperatures: List<MeasurementValue> = emptyList(),

  val isOffline: Boolean = false,
  val isOff: Boolean = false,
  val currentPower: Float? = null,
  val buttonsDisabled: Boolean = false,

  val isAutoFunction: Boolean = false,
  val heatingModeActive: Boolean = false,
  val coolingModeActive: Boolean = false,
  val showHeatingIndicator: Boolean = false,
  val showCoolingIndicator: Boolean = false,
  val pumpSwitchIcon: ImageId? = null,
  val heatOrColdSourceSwitchIcon: ImageId? = null,

  val configMinTemperatureString: String = "",
  val configMaxTemperatureString: String = "",

  val currentTemperaturePercentage: Float? = null,

  val manualModeActive: Boolean = false,
  val programmedModeActive: Boolean = false,

  val temporaryChangeActive: Boolean = false,
  val temporaryProgramInfo: List<ThermostatProgramInfo> = emptyList(),

  val sensorIssue: SensorIssue? = null,

  val issues: List<ChannelIssueItem> = emptyList(),

  val loadingState: LoadingTimeoutManager.LoadingState = LoadingTimeoutManager.LoadingState(),
  val lastInteractionTime: Long? = null,
  val changing: Boolean = false
) : ViewState(), TimerHeaderState {

  val setpointHeatTemperaturePercentage: Float?
    get() {
      ifLet(
        viewModelState?.setpointHeatTemperature,
        viewModelState?.configMinTemperature,
        viewModelState?.configMaxTemperature
      ) { (heat, min, max) ->
        return when {
          viewModelState?.mode == SuplaHvacMode.HEAT || viewModelState?.mode == SuplaHvacMode.HEAT_COOL ->
            heat.minus(min).div(max - min)

          viewModelState?.mode == SuplaHvacMode.OFF && programmedModeActive ->
            heat.minus(min).div(max - min)

          else -> null
        }
      }
      return null
    }

  val setpointCoolTemperaturePercentage: Float?
    get() {
      ifLet(
        viewModelState?.setpointCoolTemperature,
        viewModelState?.configMinTemperature,
        viewModelState?.configMaxTemperature
      ) { (cool, min, max) ->
        return when {
          viewModelState?.mode == SuplaHvacMode.COOL || viewModelState?.mode == SuplaHvacMode.HEAT_COOL ->
            cool.minus(min).div(max - min)

          viewModelState?.mode == SuplaHvacMode.OFF && programmedModeActive ->
            cool.minus(min).div(max - min)

          else -> null
        }
      }
      return null
    }

  val canDecreaseTemperature: Boolean
    get() {
      ifLet(viewModelState) { (state) ->
        return if (state.lastChangedHeat) {
          state.setpointHeatTemperature?.let { it > state.configMinTemperature } ?: false
        } else {
          state.setpointCoolTemperature?.let { it > state.configMinTemperature } ?: false
        }
      }

      return false
    }

  val canIncreaseTemperature: Boolean
    get() {
      ifLet(viewModelState) { (state) ->
        return if (state.lastChangedHeat) {
          state.setpointHeatTemperature?.let { it < state.configMaxTemperature } ?: false
        } else {
          state.setpointCoolTemperature?.let { it < state.configMaxTemperature } ?: false
        }
      }

      return false
    }

  override val endDateText: StringProvider
    get() = TimerHeaderState.endDateText(viewModelState?.timerEndDate)

  override val currentStateIcon: Int?
    get() = TimerHeaderState.currentStateIcon(viewModelState?.mode)

  override val currentStateIconColor: Int
    get() = TimerHeaderState.currentStateIconColor(viewModelState?.mode)

  override val currentStateValue: StringProvider
    get() = TimerHeaderState.currentStateValue(
      viewModelState?.mode,
      viewModelState?.setpointHeatTemperature,
      viewModelState?.setpointCoolTemperature
    )
}

data class ThermostatGeneralViewModelState(
  val remoteId: Int,
  val function: Int,
  val lastChangedHeat: Boolean,
  val configMinTemperature: Float,
  val configMaxTemperature: Float,
  val mode: SuplaHvacMode,
  val setpointHeatTemperature: Float? = null,
  val setpointCoolTemperature: Float? = null,
  val subfunction: ThermostatSubfunction? = null,
  val timerEndDate: Date? = null,
  val relatedRemoteIds: List<Int> = emptyList(),
  override val sent: Boolean = false
) : DelayableState {

  override fun sentState(): DelayableState = copy(sent = true)
  override fun delayableCopy(): DelayableState = copy()
}

private fun ChannelDataEntity.isActive(flag: SuplaThermostatFlag): Boolean {
  val value = channelValueEntity.asThermostatValue()
  return channelValueEntity.status.online && value.state.isOn() && value.flags.contains(flag)
}
