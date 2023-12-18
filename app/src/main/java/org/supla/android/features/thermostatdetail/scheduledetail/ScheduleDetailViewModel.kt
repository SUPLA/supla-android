package org.supla.android.features.thermostatdetail.scheduledetail
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
import io.reactivex.rxjava3.subjects.PublishSubject
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.Trace
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.DelayableState
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.local.temperature.TemperatureCorrection
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.SuplaDeviceConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelHvacConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleEntry
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.data.source.remote.isAutomaticTimeSyncDisabled
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.DeviceConfigEventsManager
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.extensions.TAG
import org.supla.android.extensions.fromSuplaTemperature
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.ifFalse
import org.supla.android.extensions.toSuplaTemperature
import org.supla.android.features.thermostatdetail.scheduledetail.data.ProgramSettingsData
import org.supla.android.features.thermostatdetail.scheduledetail.data.QuartersSelectionData
import org.supla.android.features.thermostatdetail.scheduledetail.data.ScheduleDetailEntryBoxKey
import org.supla.android.features.thermostatdetail.scheduledetail.data.ScheduleDetailEntryBoxValue
import org.supla.android.features.thermostatdetail.scheduledetail.data.ScheduleDetailProgramBox
import org.supla.android.features.thermostatdetail.scheduledetail.extensions.viewProgramBoxesList
import org.supla.android.features.thermostatdetail.scheduledetail.extensions.viewScheduleBoxesMap
import org.supla.android.features.thermostatdetail.scheduledetail.ui.ScheduleDetailViewProxy
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL
import org.supla.android.tools.SuplaSchedulers
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val REFRESH_DELAY_MS = 3000L

@HiltViewModel
class ScheduleDetailViewModel @Inject constructor(
  private val valuesFormatter: ValuesFormatter,
  private val suplaClientProvider: SuplaClientProvider,
  private val channelConfigEventsManager: ChannelConfigEventsManager,
  private val deviceConfigEventsManager: DeviceConfigEventsManager,
  private val delayedWeeklyScheduleConfigSubject: DelayedWeeklyScheduleConfigSubject,
  private val loadingTimeoutManager: LoadingTimeoutManager,
  private val preferences: Preferences,
  private val dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseViewModel<ScheduleDetailViewState, ScheduleDetailViewEvent>(ScheduleDetailViewState(), schedulers), ScheduleDetailViewProxy {

  private val updateSubject = PublishSubject.create<Int>()

  override fun onViewCreated() {
    loadingTimeoutManager.watch({ currentState().loadingState }) {
      updateState { state ->
        reloadConfig(state.remoteId)
        state.copy(loadingState = state.loadingState.changingLoading(false, dateProvider))
      }
    }

    updateState { it.copy(showHelp = preferences.showThermostatScheduleInfo) }
  }

  fun observeConfig(remoteId: Int, deviceId: Int) {
    updateSubject.attachSilent()
      .debounce(1, TimeUnit.SECONDS)
      .subscribeBy(
        onNext = { reloadConfig(remoteId) },
        onError = defaultErrorHandler("observeConfig($remoteId)")
      )
      .disposeBySelf()

    Observable.combineLatest(
      channelConfigEventsManager.observerConfig(remoteId).filter { it.config is SuplaChannelWeeklyScheduleConfig },
      channelConfigEventsManager.observerConfig(remoteId).filter { it.config is SuplaChannelHvacConfig },
      deviceConfigEventsManager.observerConfig(deviceId)
    ) { weeklyConfig, defaultConfig, deviceConfig ->
      LoadedData(
        weeklyConfig.config as SuplaChannelWeeklyScheduleConfig,
        weeklyConfig.result,
        defaultConfig.config as SuplaChannelHvacConfig,
        defaultConfig.result,
        deviceConfig.config
      )
    }
      .debounce(50, TimeUnit.MILLISECONDS)
      .subscribeBy(
        onNext = { onConfigLoaded(it) },
        onError = defaultErrorHandler("observeConfig($remoteId)")
      ).disposeBySelf()

    updateState { it.copy(loadingState = it.loadingState.changingLoading(true, dateProvider), remoteId = remoteId) }
    reloadConfig(remoteId)
    suplaClientProvider.provide()?.getDeviceConfig(deviceId)
  }

  override fun updateSchedule() {
    delayedWeeklyScheduleConfigSubject.emit(currentState())
    updateState { it.copy(changing = false, lastInteractionTime = System.currentTimeMillis()) }
  }

  override fun changeScheduleEntry(key: ScheduleDetailEntryBoxKey) {
    currentState().let { state ->
      if (state.activeProgram == null) {
        return // No active program, so nothing to change.
      }

      if (state.schedule[key] == null || state.schedule[key]?.singleProgram() != state.activeProgram) {
        updateState { _ ->
          state.copy(
            schedule = mutableMapOf<ScheduleDetailEntryBoxKey, ScheduleDetailEntryBoxValue>().also {
              it.putAll(state.schedule)
              it[key] = ScheduleDetailEntryBoxValue(singleProgram = state.activeProgram)
            },
            changing = true,
            lastInteractionTime = System.currentTimeMillis()
          )
        }
      }

      delayedWeeklyScheduleConfigSubject.emit(currentState())
    }
  }

  override fun invalidateSchedule() {
    reloadConfig(currentState().remoteId)
  }

  override fun changeProgram(program: SuplaScheduleProgram) {
    updateState {
      it.copy(activeProgram = getProgramForChange(program, it))
    }
  }

  override fun startQuartersDialog(key: ScheduleDetailEntryBoxKey?) {
    updateState { it.copy(quarterSelection = it.quarterSelectionData(key)) }
  }

  override fun cancelQuartersDialog() {
    updateState { it.copy(quarterSelection = null) }
  }

  override fun onQuartersDialogProgramChange(program: SuplaScheduleProgram) {
    updateState {
      val newProgram = getProgramForChange(program, it)
      it.copy(
        activeProgram = newProgram,
        quarterSelection = it.quarterSelection?.copy(activeProgram = newProgram)
      )
    }
  }

  override fun onQueartersDialogQuarterChange(quarterOfHour: QuarterOfHour) {
    updateState { state ->
      if (state.quarterSelection?.activeProgram == null) {
        return@updateState state
      }

      val entry = when (quarterOfHour) {
        QuarterOfHour.FIRST -> state.quarterSelection.entryValue.copy(firstQuarterProgram = state.quarterSelection.activeProgram)
        QuarterOfHour.SECOND -> state.quarterSelection.entryValue.copy(secondQuarterProgram = state.quarterSelection.activeProgram)
        QuarterOfHour.THIRD -> state.quarterSelection.entryValue.copy(thirdQuarterProgram = state.quarterSelection.activeProgram)
        QuarterOfHour.FOURTH -> state.quarterSelection.entryValue.copy(fourthQuarterProgram = state.quarterSelection.activeProgram)
      }

      return@updateState entry.let {
        state.copy(quarterSelection = state.quarterSelection.copy(entryValue = it))
      }
    }
  }

  override fun saveQuartersDialogChanges() {
    updateState { state ->
      state.quarterSelection?.let { selection ->
        val newState = state.copy(
          schedule = mutableMapOf<ScheduleDetailEntryBoxKey, ScheduleDetailEntryBoxValue>().also {
            it.putAll(state.schedule)
            it[selection.entryKey] = selection.entryValue
          },
          activeProgram = state.quarterSelection.activeProgram,
          quarterSelection = null,
          lastInteractionTime = System.currentTimeMillis()
        )

        delayedWeeklyScheduleConfigSubject.emit(newState) // Sending changes to the server

        newState // Updating view state
      } ?: state.copy(quarterSelection = null)
    }
  }

  override fun startProgramDialog(program: SuplaScheduleProgram) {
    if (program != SuplaScheduleProgram.OFF) {
      updateState { it.copy(programSettings = createProgramSettingData(it, program)) }
    }
  }

  override fun cancelProgramDialog() {
    updateState { it.copy(programSettings = null) }
  }

  override fun saveProgramDialogChanges() {
    updateState { state ->
      state.copy(
        programs = state.updatedPrograms(state.channelFunction),
        activeProgram = state.programSettings?.program,
        programSettings = null,
        lastInteractionTime = System.currentTimeMillis()
      ).also {
        delayedWeeklyScheduleConfigSubject.emit(it.copy())
      }
    }
  }

  override fun onProgramDialogTemperatureClickChange(
    programMode: SuplaHvacMode,
    modeForTemperature: SuplaHvacMode,
    correction: TemperatureCorrection
  ) {
    changeProgramTemperature(
      programMode,
      modeForTemperature,
      { valuesFormatter.getTemperatureString(it.toDouble(), withDegree = false) },
      { it.plus(correction.step()) },
      true
    )
  }

  override fun onProgramDialogTemperatureManualChange(programMode: SuplaHvacMode, modeForTemperature: SuplaHvacMode, value: String) {
    try {
      if (value.length > 6) {
        throw IllegalArgumentException("Provided value is to long")
      }

      val temperature = value.replace(',', '.').toFloat()
      changeProgramTemperature(programMode, modeForTemperature, { value }, { temperature }, false)
    } catch (ex: Exception) {
      if ((ex is NumberFormatException || ex is IllegalArgumentException).not()) {
        throw ex
      }

      when {
        value.isEmpty() && modeForTemperature == SuplaHvacMode.HEAT ->
          updateState { it.copy(programSettings = it.programSettings?.cleanSetpointMin()) }

        value.isEmpty() && modeForTemperature == SuplaHvacMode.COOL ->
          updateState { it.copy(programSettings = it.programSettings?.cleanSetpointMax()) }

        else -> currentState().programSettings?.let { settings ->
          val textProvider: (Float) -> String = when {
            value == "-" -> { _ -> "-" }
            modeForTemperature == SuplaHvacMode.HEAT -> { _ -> settings.setpointTemperatureHeatString ?: "" }
            modeForTemperature == SuplaHvacMode.COOL -> { _ -> settings.setpointTemperatureCoolString ?: "" }
            else -> { _ -> "" }
          }

          val temperatureModifier: (Float) -> Float = when (modeForTemperature) {
            SuplaHvacMode.HEAT -> { _ -> settings.setpointTemperatureHeat ?: 0f }
            SuplaHvacMode.COOL -> { _ -> settings.setpointTemperatureCool ?: 0f }
            else -> { _ -> 0f }
          }

          changeProgramTemperature(
            programMode = programMode,
            modeForTemperature = modeForTemperature,
            textProvider = textProvider,
            temperatureModifier = temperatureModifier,
            withCorrection = false
          )
        }
      }
    }
  }

  override fun onHelpClosed() {
    preferences.showThermostatScheduleInfo = false
    updateState { it.copy(showHelp = false) }
  }

  private fun getProgramForChange(program: SuplaScheduleProgram, state: ScheduleDetailViewState): SuplaScheduleProgram? {
    if (state.activeProgram == program) {
      return null // Deselect active program
    }

    for (programConfiguration in state.programs) {
      if (programConfiguration.scheduleProgram.program == program && programConfiguration.scheduleProgram.mode == SuplaHvacMode.NOT_SET) {
        return null // Don't allow to set program with NOT_SET mode
      }
    }

    return program
  }

  private fun changeProgramTemperature(
    programMode: SuplaHvacMode,
    modeForTemperature: SuplaHvacMode,
    textProvider: (Float) -> String,
    temperatureModifier: (Float) -> Float,
    withCorrection: Boolean
  ) {
    val state = currentState()
    when {
      programMode == SuplaHvacMode.HEAT -> changeProgramTemperatureMin(temperatureModifier, textProvider, withCorrection)
      programMode == SuplaHvacMode.COOL -> changeProgramTemperatureMax(temperatureModifier, textProvider, withCorrection)
      programMode == SuplaHvacMode.HEAT_COOL && modeForTemperature == SuplaHvacMode.HEAT ->
        changeProgramTemperatureMin(
          temperatureModifier,
          textProvider,
          withCorrection,
          state.programSettings?.setpointTemperatureCool
        )

      programMode == SuplaHvacMode.HEAT_COOL && modeForTemperature == SuplaHvacMode.COOL ->
        changeProgramTemperatureMax(
          temperatureModifier,
          textProvider,
          withCorrection,
          state.programSettings?.setpointTemperatureHeat
        )

      else -> throw IllegalStateException("Trying to change temperature for illegal mode: $modeForTemperature")
    }
  }

  private fun changeProgramTemperatureMin(
    temperatureModifier: (Float) -> Float,
    textProvider: (Float) -> String,
    withCorrection: Boolean = false,
    setpointTemperatureCool: Float? = null
  ) {
    val state = currentState()
    state.programSettings?.setpointTemperatureHeat?.let { oldTemperature ->
      val newTemperature = temperatureModifier(oldTemperature).let {
        when {
          withCorrection && it > state.configTemperatureMax -> state.configTemperatureMax
          withCorrection && it < state.configTemperatureMin -> state.configTemperatureMin
          else -> it
        }
      }
      val maxTemperature = setpointTemperatureCool ?: state.configTemperatureMax
      val temperatureCorrect = newTemperature >= state.configTemperatureMin && newTemperature <= maxTemperature

      updateState {
        it.copy(
          programSettings = it.programSettings?.copy(
            setpointTemperatureHeat = newTemperature,
            setpointTemperatureHeatString = textProvider(newTemperature),
            setpointTemperatureHeatMinusAllowed = newTemperature > it.configTemperatureMin,
            setpointTemperatureHeatPlusAllowed = newTemperature < it.configTemperatureMax,
            temperatureHeatCorrect = temperatureCorrect
          )
        )
      }
    }
  }

  private fun changeProgramTemperatureMax(
    temperatureModifier: (Float) -> Float,
    textProvider: (Float) -> String,
    withCorrection: Boolean = false,
    setpointTemperatureHeat: Float? = null
  ) {
    val state = currentState()
    state.programSettings?.setpointTemperatureCool?.let { oldTemperature ->
      val newTemperature = temperatureModifier(oldTemperature).let {
        when {
          withCorrection && it > state.configTemperatureMax -> state.configTemperatureMax
          withCorrection && it < state.configTemperatureMin -> state.configTemperatureMin
          else -> it
        }
      }
      val minTemperature = setpointTemperatureHeat ?: state.configTemperatureMin
      val temperatureCorrect = newTemperature >= minTemperature && newTemperature <= state.configTemperatureMax

      updateState {
        it.copy(
          programSettings = it.programSettings?.copy(
            setpointTemperatureCool = newTemperature,
            setpointTemperatureCoolString = textProvider(newTemperature),
            setpointTemperatureCoolMinusAllowed = newTemperature > it.configTemperatureMin,
            setpointTemperatureCoolPlusAllowed = newTemperature < it.configTemperatureMax,
            temperatureCoolCorrect = temperatureCorrect
          )
        )
      }
    }
  }

  private fun createProgramSettingData(state: ScheduleDetailViewState, program: SuplaScheduleProgram): ProgramSettingsData? {
    for (programBox in state.programs) {
      if (programBox.scheduleProgram.program == program) {
        return ProgramSettingsData(
          program = program,
          modes = programAvailableModes(state),
          selectedMode = programBox.modeForModify,
          setpointTemperatureHeat = programBox.temperatureMinForModify,
          setpointTemperatureCool = programBox.temperatureMaxForModify,
          setpointTemperatureHeatString = valuesFormatter.getTemperatureString(programBox.temperatureMinForModify, withDegree = false),
          setpointTemperatureCoolString = valuesFormatter.getTemperatureString(programBox.temperatureMaxForModify, withDegree = false),
          temperatureUnit = preferences.temperatureUnit
        )
      }
    }

    return null
  }

  private fun programAvailableModes(state: ScheduleDetailViewState) = when {
    state.channelFunction == SUPLA_CHANNELFNC_HVAC_THERMOSTAT && state.thermostatFunction == ThermostatSubfunction.HEAT ->
      listOf(SuplaHvacMode.HEAT)

    state.channelFunction == SUPLA_CHANNELFNC_HVAC_THERMOSTAT && state.thermostatFunction == ThermostatSubfunction.COOL ->
      listOf(SuplaHvacMode.COOL)

    state.channelFunction == SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL ->
      listOf(SuplaHvacMode.HEAT_COOL, SuplaHvacMode.HEAT, SuplaHvacMode.COOL)

    state.channelFunction == SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER ->
      listOf(SuplaHvacMode.HEAT)

    else -> listOf()
  }

  private fun reloadConfig(remoteId: Int) {
    suplaClientProvider.provide()?.getChannelConfig(remoteId, ChannelConfigType.WEEKLY_SCHEDULE)
    suplaClientProvider.provide()?.getChannelConfig(remoteId, ChannelConfigType.DEFAULT)
  }

  private fun onConfigLoaded(data: LoadedData) {
    Trace.i(TAG, "Schedule detail got data: $data")

    if (data.weeklyScheduleResult != ConfigResult.RESULT_TRUE || data.defaultResult != ConfigResult.RESULT_TRUE) {
      return
    }
    val (channelFunction) = guardLet(data.weeklyScheduleConfig.func) {
      return
    }
    val (configTemperatureMin, configTemperatureMax) = guardLet(
      data.defaultConfig.temperatures.roomMin?.fromSuplaTemperature(),
      data.defaultConfig.temperatures.roomMax?.fromSuplaTemperature()
    ) {
      return
    }

    val calendar = Calendar.getInstance()

    updateState {
      if (it.changing) {
        Trace.d(TAG, "update skipped because of changing")
        return@updateState it // Do not change anything, when user makes manual operations
      }
      if (it.lastInteractionTime != null && it.lastInteractionTime + REFRESH_DELAY_MS > System.currentTimeMillis()) {
        Trace.d(TAG, "update skipped because of last interaction time")
        updateSubject.onNext(0)
        return@updateState it // Do not change anything during 3 secs after last user interaction
      }
      val thermostatFunction = data.defaultConfig.subfunction

      Trace.d(TAG, "updating state with data")
      it.copy(
        loadingState = it.loadingState.changingLoading(false, dateProvider),
        channelFunction = channelFunction,
        schedule = data.weeklyScheduleConfig.viewScheduleBoxesMap(),
        programs = data.weeklyScheduleConfig.viewProgramBoxesList(thermostatFunction),
        configTemperatureMin = configTemperatureMin,
        configTemperatureMax = configTemperatureMax,
        currentDayOfWeek = data.deviceConfig.isAutomaticTimeSyncDisabled().ifFalse(DayOfWeek.from(calendar.get(Calendar.DAY_OF_WEEK) - 1)),
        currentHour = data.deviceConfig.isAutomaticTimeSyncDisabled().ifFalse(calendar.get(Calendar.HOUR_OF_DAY)),
        thermostatFunction = thermostatFunction
      )
    }
  }

  private data class LoadedData(
    val weeklyScheduleConfig: SuplaChannelWeeklyScheduleConfig,
    val weeklyScheduleResult: ConfigResult,
    val defaultConfig: SuplaChannelHvacConfig,
    val defaultResult: ConfigResult,
    val deviceConfig: SuplaDeviceConfig?
  )
}

sealed class ScheduleDetailViewEvent : ViewEvent

data class ScheduleDetailViewState(
  val loadingState: LoadingTimeoutManager.LoadingState = LoadingTimeoutManager.LoadingState(),
  val lastInteractionTime: Long? = null,
  val changing: Boolean = false,

  val remoteId: Int = 0,
  val channelFunction: Int = 0,
  val thermostatFunction: ThermostatSubfunction? = null,
  val configTemperatureMin: Float = 0f,
  val configTemperatureMax: Float = 0f,

  val activeProgram: SuplaScheduleProgram? = null,
  val programs: List<ScheduleDetailProgramBox> = emptyList(),
  val schedule: Map<ScheduleDetailEntryBoxKey, ScheduleDetailEntryBoxValue> = emptyMap(),
  val quarterSelection: QuartersSelectionData? = null,
  val programSettings: ProgramSettingsData? = null,
  val currentDayOfWeek: DayOfWeek? = null,
  val currentHour: Int? = null,
  val showHelp: Boolean = false,
  override val sent: Boolean = false
) : ViewState(), DelayableState {

  fun quarterSelectionData(forKey: ScheduleDetailEntryBoxKey?): QuartersSelectionData? {
    val (key) = guardLet(forKey) { return null }
    val (value) = guardLet(schedule[forKey]) { return null }

    return QuartersSelectionData(
      entryKey = key.copy(),
      entryValue = value.copy(),
      activeProgram = activeProgram
    )
  }

  fun updatedPrograms(function: Int): List<ScheduleDetailProgramBox> =
    programSettings?.let { programToUpdate ->
      mutableListOf<ScheduleDetailProgramBox>().apply {
        for (program in programs) {
          if (program.scheduleProgram.program == programToUpdate.program) {
            val icon = when {
              function == SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL && program.scheduleProgram.mode == SuplaHvacMode.HEAT -> R.drawable.ic_heat
              function == SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL && program.scheduleProgram.mode == SuplaHvacMode.COOL -> R.drawable.ic_cool
              else -> null
            }

            ScheduleDetailProgramBox(
              channelFunction = function,
              thermostatFunction = thermostatFunction!!,
              SuplaWeeklyScheduleProgram(
                program = programToUpdate.program,
                mode = programToUpdate.selectedMode,
                setpointTemperatureHeat = programToUpdate.setpointTemperatureHeat?.toSuplaTemperature(),
                setpointTemperatureCool = programToUpdate.setpointTemperatureCool?.toSuplaTemperature()
              ),
              iconRes = icon
            ).also {
              add(it)
            }
          } else {
            add(program)
          }
        }
      }
    } ?: programs

  fun suplaPrograms(): List<SuplaWeeklyScheduleProgram> = mutableListOf<SuplaWeeklyScheduleProgram>().apply {
    for (program in programs) {
      if (program.scheduleProgram.program == SuplaScheduleProgram.OFF) {
        continue
      }
      add(program.scheduleProgram.copy())
    }
  }

  fun suplaSchedule(): List<SuplaWeeklyScheduleEntry> = mutableListOf<SuplaWeeklyScheduleEntry>().apply {
    for (entry in schedule) {
      add(SuplaWeeklyScheduleEntry(entry.key.dayOfWeek, entry.key.hour.toInt(), QuarterOfHour.FIRST, entry.value.firstQuarterProgram))
      add(SuplaWeeklyScheduleEntry(entry.key.dayOfWeek, entry.key.hour.toInt(), QuarterOfHour.SECOND, entry.value.secondQuarterProgram))
      add(SuplaWeeklyScheduleEntry(entry.key.dayOfWeek, entry.key.hour.toInt(), QuarterOfHour.THIRD, entry.value.thirdQuarterProgram))
      add(SuplaWeeklyScheduleEntry(entry.key.dayOfWeek, entry.key.hour.toInt(), QuarterOfHour.FOURTH, entry.value.fourthQuarterProgram))
    }
  }

  override fun sentState(): DelayableState = copy(sent = true)
  override fun delayableCopy(): DelayableState = copy()
}
