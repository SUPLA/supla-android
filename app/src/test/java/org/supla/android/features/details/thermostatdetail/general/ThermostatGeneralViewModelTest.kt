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

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.model.temperature.TemperatureCorrection
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.channel.SuplaChannelFunction
import org.supla.android.data.source.remote.hvac.SuplaChannelHvacConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaHvacAlgorithm
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaHvacTemperatures
import org.supla.android.data.source.remote.hvac.SuplaHvacThermometerType
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlag
import org.supla.android.data.source.remote.thermostat.ThermostatState
import org.supla.android.data.source.remote.thermostat.ThermostatValue
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.DeviceConfigEventsManager
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.events.UpdateEventsManager
import org.supla.android.extensions.date
import org.supla.android.extensions.shift
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.SuplaTimerState
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.GetChannelValueUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenTreeUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.thermostat.CreateTemperaturesListUseCase
import java.util.Date
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class ThermostatGeneralViewModelTest :
  BaseViewModelTest<ThermostatGeneralViewState, ThermostatGeneralViewEvent, ThermostatGeneralViewModel>() {

  @Mock
  lateinit var readChannelWithChildrenTreeUseCase: ReadChannelWithChildrenTreeUseCase

  @Mock
  lateinit var createTemperaturesListUseCase: CreateTemperaturesListUseCase

  @Mock
  lateinit var getChannelValueUseCase: GetChannelValueUseCase

  @Mock
  lateinit var valuesFormatter: ValuesFormatter

  @Mock
  lateinit var delayedThermostatActionSubject: DelayedThermostatActionSubject

  @Mock
  lateinit var channelConfigEventsManager: ChannelConfigEventsManager

  @Mock
  lateinit var suplaClientProvider: SuplaClientProvider

  @Mock
  lateinit var loadingTimeoutManager: LoadingTimeoutManager

  @Mock
  lateinit var dateProvider: DateProvider

  @Mock
  lateinit var updateEventsManager: UpdateEventsManager

  @Mock
  lateinit var deviceConfigEventsManager: DeviceConfigEventsManager

  @Mock
  lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @InjectMocks
  override lateinit var viewModel: ThermostatGeneralViewModel

  @Before
  override fun setUp() {
    super.setUp()
    whenever(schedulers.computation).thenReturn(testScheduler)
    whenever(updateEventsManager.observeChannelsUpdate()).thenReturn(Observable.empty())
  }

  @Test
  fun shouldLoadHeatThermostatInStandbyState() {
    // given
    val remoteId = 123
    val deviceId = 321
    mockHeatThermostat(remoteId, deviceId, 23.4f)

    // when
    viewModel.observeData(remoteId, deviceId)
    viewModel.triggerDataLoad(remoteId)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      ThermostatGeneralViewState(
        viewModelState = ThermostatGeneralViewModelState(
          remoteId = remoteId,
          function = SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
          lastChangedHeat = true,
          configMinTemperature = 10f,
          configMaxTemperature = 40f,
          mode = SuplaHvacMode.HEAT,
          setpointHeatTemperature = 23.4f,
          setpointCoolTemperature = null,
          subfunction = ThermostatSubfunction.HEAT,
          relatedRemoteIds = listOf(999, 998)
        ),
        currentTemperaturePercentage = 0.17666666f,
        configMinTemperatureString = "10,0",
        configMaxTemperatureString = "40,0",
        manualModeActive = true,
        loadingState = LoadingTimeoutManager.LoadingState(initialLoading = false, loading = false)
      )
    )
  }

  @Test
  fun shouldLoadCoolThermostatInCoolingState() {
    // given
    val remoteId = 123
    val deviceId = 321
    val channelWithChildren = mockChannelWithChildren(
      remoteId = remoteId,
      mode = SuplaHvacMode.COOL,
      setpointTemperatureCool = 20.8f,
      flags = listOf(SuplaThermostatFlag.SETPOINT_TEMP_MAX_SET, SuplaThermostatFlag.COOLING)
    )

    whenever(channelConfigEventsManager.observerConfig(remoteId)).thenReturn(
      Observable.just(
        ChannelConfigEventsManager.ConfigEvent(
          ConfigResult.RESULT_TRUE,
          mockSuplaChannelHvacConfig(remoteId, ThermostatSubfunction.COOL)
        )
      ),
      Observable.just(
        ChannelConfigEventsManager.ConfigEvent(
          ConfigResult.RESULT_TRUE,
          mockSuplaChannelWeeklyScheduleConfig(remoteId)
        )
      )
    )
    whenever(deviceConfigEventsManager.observerConfig(deviceId)).thenReturn(
      Observable.just(DeviceConfigEventsManager.ConfigEvent(ConfigResult.RESULT_FALSE, null))
    )
    whenever(readChannelWithChildrenTreeUseCase.invoke(remoteId)).thenReturn(
      Observable.just(channelWithChildren)
    )
    whenever(createTemperaturesListUseCase.invoke(channelWithChildren)).thenReturn(emptyList())
    whenever(valuesFormatter.getTemperatureString(10f)).thenReturn("10,0")
    whenever(valuesFormatter.getTemperatureString(40f)).thenReturn("40,0")

    // when
    viewModel.observeData(remoteId, deviceId)
    viewModel.triggerDataLoad(remoteId)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      ThermostatGeneralViewState(
        viewModelState = ThermostatGeneralViewModelState(
          remoteId = remoteId,
          function = SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
          lastChangedHeat = false,
          configMinTemperature = 10f,
          configMaxTemperature = 40f,
          mode = SuplaHvacMode.COOL,
          setpointHeatTemperature = null,
          setpointCoolTemperature = 20.8f,
          subfunction = ThermostatSubfunction.COOL,
          relatedRemoteIds = listOf(999, 998)
        ),
        showCoolingIndicator = true,
        currentTemperaturePercentage = 0.17666666f,
        configMinTemperatureString = "10,0",
        configMaxTemperatureString = "40,0",
        manualModeActive = true,
        loadingState = LoadingTimeoutManager.LoadingState(initialLoading = false, loading = false)
      )
    )
  }

  @Test
  fun `should change heat temperature on setpoint position change`() {
    // given
    val remoteId = 321
    val deviceId = 321
    val currentDate = date(2022, 10, 21)
    val timerEndDate = date(2022, 10, 22)
    mockHeatThermostat(remoteId, deviceId, setpointTemperatureHeat = 22.4f, timerEndDate = timerEndDate)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)
    whenever(dateProvider.currentDate()).thenReturn(currentDate)

    // when
    viewModel.observeData(remoteId, deviceId)
    viewModel.triggerDataLoad(remoteId)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
    viewModel.setpointTemperatureChanged(0.5f, null)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    assertThat(events).isEmpty()
    val state = thermostatDefaultState(remoteId, 22.4f, relatedRemoteIds = listOf(999, 998))
      .let { it.copy(viewModelState = it.viewModelState?.copy(timerEndDate = timerEndDate)) }
    val emittedState = state.viewModelState!!.copy(setpointHeatTemperature = 25f)
    assertThat(states).containsExactly(
      state,
      state.copy(
        viewModelState = emittedState,
        lastInteractionTime = currentTimestamp
      )
    )

    verify(delayedThermostatActionSubject).emit(emittedState)
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should change heat temperature on setpoint position change in weekly schedule`() {
    // given
    val remoteId = 321
    val deviceId = 321
    mockHeatThermostat(remoteId, deviceId, 22.4f, weeklyScheduleActive = true)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    // when
    viewModel.observeData(remoteId, deviceId)
    viewModel.triggerDataLoad(remoteId)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
    viewModel.setpointTemperatureChanged(0.5f, null)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    val state = thermostatDefaultState(remoteId, setpointTemperatureHeat = 22.4f, manualActive = false, relatedRemoteIds = listOf(999, 998))
    val emittedState = state.viewModelState!!.copy(setpointHeatTemperature = 25f)
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      state,
      state.copy(
        viewModelState = emittedState,
        lastInteractionTime = currentTimestamp
      )
    )

    verify(delayedThermostatActionSubject).emit(emittedState.copy(mode = SuplaHvacMode.NOT_SET))
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should change cool temperature on setpoint position change`() {
    // given
    val remoteId = 321
    val deviceId = 321
    mockCoolThermostat(remoteId, deviceId, setpointTemperature = 22.4f)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    // when
    viewModel.observeData(remoteId, deviceId)
    viewModel.triggerDataLoad(remoteId)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
    viewModel.setpointTemperatureChanged(null, 0.5f)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    assertThat(events).isEmpty()
    val state = thermostatDefaultState(
      remoteId,
      setpointTemperatureCool = 22.4f,
      mode = SuplaHvacMode.COOL,
      currentlyCooling = true,
      subfunction = ThermostatSubfunction.COOL,
      relatedRemoteIds = listOf(999, 998)
    )
    val emittedState = state.viewModelState!!.copy(setpointCoolTemperature = 25f)
    assertThat(states).containsExactly(
      state,
      state.copy(
        viewModelState = emittedState,
        lastInteractionTime = currentTimestamp
      )
    )

    verify(delayedThermostatActionSubject).emit(emittedState)
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should change cool temperature on setpoint position change in weekly schedule`() {
    // given
    val remoteId = 321
    val deviceId = 321
    mockCoolThermostat(remoteId, deviceId, 22.4f, weeklyScheduleActive = true)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    // when
    viewModel.observeData(remoteId, deviceId)
    viewModel.triggerDataLoad(remoteId)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
    viewModel.setpointTemperatureChanged(null, 0.5f)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    val state = thermostatDefaultState(
      remoteId,
      setpointTemperatureCool = 22.4f,
      manualActive = false,
      mode = SuplaHvacMode.COOL,
      currentlyCooling = true,
      subfunction = ThermostatSubfunction.COOL,
      relatedRemoteIds = listOf(999, 998)
    )
    val emittedState = state.viewModelState!!.copy(setpointCoolTemperature = 25f)
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      state,
      state.copy(
        viewModelState = emittedState,
        lastInteractionTime = currentTimestamp
      )
    )

    verify(delayedThermostatActionSubject).emit(emittedState.copy(mode = SuplaHvacMode.NOT_SET))
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should change heat temperature by step`() {
    // given
    val remoteId = 321
    val deviceId = 321
    mockHeatThermostat(remoteId, deviceId, 22.4f)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    // when
    viewModel.observeData(remoteId, deviceId)
    viewModel.triggerDataLoad(remoteId)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
    viewModel.changeSetpointTemperature(TemperatureCorrection.UP)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    assertThat(events).isEmpty()
    val state = thermostatDefaultState(remoteId, setpointTemperatureHeat = 22.4f, relatedRemoteIds = listOf(999, 998))
    val emittedState = state.viewModelState!!.copy(setpointHeatTemperature = 22.5f)
    assertThat(states).containsExactly(
      state,
      state.copy(
        viewModelState = emittedState,
        lastInteractionTime = currentTimestamp
      )
    )

    verify(delayedThermostatActionSubject).emit(emittedState)
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should change cool temperature by step with weekly schedule`() {
    // given
    val remoteId = 321
    val deviceId = 321
    val state = thermostatDefaultState(
      remoteId,
      setpointTemperatureCool = 22.4f,
      manualActive = false,
      mode = SuplaHvacMode.COOL,
      currentlyCooling = true,
      subfunction = ThermostatSubfunction.COOL,
      relatedRemoteIds = listOf(999, 998)
    )
    mockCoolThermostat(remoteId, deviceId, 22.4f, weeklyScheduleActive = true)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    // when
    viewModel.observeData(remoteId, deviceId)
    viewModel.triggerDataLoad(remoteId)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
    viewModel.changeSetpointTemperature(TemperatureCorrection.DOWN)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    val emittedState = state.viewModelState!!.copy(setpointCoolTemperature = 22.3f)

    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      state,
      state.copy(
        viewModelState = emittedState,
        lastInteractionTime = currentTimestamp
      )
    )

    verify(delayedThermostatActionSubject).emit(emittedState.copy(mode = SuplaHvacMode.NOT_SET))
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should turn off`() {
    // given
    val remoteId = 321
    val deviceId = 321
    mockHeatThermostat(remoteId, deviceId, 22.4f)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    val state = thermostatDefaultState(remoteId, setpointTemperatureHeat = 22.4f, relatedRemoteIds = listOf(999, 998))
    val emittedState = state.viewModelState!!.copy(
      mode = SuplaHvacMode.OFF,
      setpointHeatTemperature = null
    )
    whenever(delayedThermostatActionSubject.sendImmediately(emittedState)).thenReturn(Completable.complete())

    // when
    viewModel.observeData(remoteId, deviceId)
    viewModel.triggerDataLoad(remoteId)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
    viewModel.turnOnOffClicked()
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      state,
      state.copy(
        loadingState = state.loadingState.changingLoading(true, dateProvider)
      )
    )

    verify(delayedThermostatActionSubject).sendImmediately(emittedState)
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should turn on`() {
    // given
    val remoteId = 321
    val deviceId = 321
    mockHeatThermostat(remoteId, deviceId, 22.4f, mode = SuplaHvacMode.OFF)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    val state = thermostatDefaultState(
      remoteId = remoteId,
      setpointTemperatureHeat = 22.4f,
      mode = SuplaHvacMode.OFF,
      relatedRemoteIds = listOf(999, 998)
    )
    val emittedState = state.viewModelState!!.copy(
      mode = SuplaHvacMode.CMD_TURN_ON,
      setpointHeatTemperature = null
    )
    whenever(delayedThermostatActionSubject.sendImmediately(emittedState)).thenReturn(Completable.complete())

    // when
    viewModel.observeData(remoteId, deviceId)
    viewModel.triggerDataLoad(remoteId)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
    viewModel.turnOnOffClicked()
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      state,
      state.copy(
        loadingState = state.loadingState.changingLoading(true, dateProvider)
      )
    )

    verify(delayedThermostatActionSubject).sendImmediately(emittedState)
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should turn off when is off but in weekly schedule`() {
    // given
    val remoteId = 321
    val deviceId = 321
    mockHeatThermostat(remoteId, deviceId, 22.4f, mode = SuplaHvacMode.OFF, weeklyScheduleActive = true)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    val state = thermostatDefaultState(
      remoteId = remoteId,
      setpointTemperatureHeat = 22.4f,
      mode = SuplaHvacMode.OFF,
      manualActive = false,
      relatedRemoteIds = listOf(999, 998)
    )
    val emittedState = state.viewModelState!!.copy(
      mode = SuplaHvacMode.OFF,
      setpointHeatTemperature = null
    )
    whenever(delayedThermostatActionSubject.sendImmediately(emittedState)).thenReturn(Completable.complete())

    // when
    viewModel.observeData(remoteId, deviceId)
    viewModel.triggerDataLoad(remoteId)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
    viewModel.turnOnOffClicked()
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      state,
      state.copy(
        loadingState = state.loadingState.changingLoading(true, dateProvider)
      )
    )

    verify(delayedThermostatActionSubject).sendImmediately(emittedState)
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun shouldNotShowTimerWhenEndDateBeforeCurrentDate() {
    // given
    val remoteId = 123
    val deviceId = 321
    val currentDate = date(2023, 11, 10)
    mockHeatThermostat(remoteId, deviceId, 23.4f, timerEndDate = currentDate.shift(-5))
    whenever(dateProvider.currentDate()).thenReturn(currentDate)

    // when
    viewModel.observeData(remoteId, deviceId)
    viewModel.triggerDataLoad(remoteId)
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      ThermostatGeneralViewState(
        viewModelState = ThermostatGeneralViewModelState(
          remoteId = remoteId,
          function = SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
          lastChangedHeat = true,
          configMinTemperature = 10f,
          configMaxTemperature = 40f,
          mode = SuplaHvacMode.HEAT,
          setpointHeatTemperature = 23.4f,
          setpointCoolTemperature = null,
          subfunction = ThermostatSubfunction.HEAT,
          relatedRemoteIds = listOf(999, 998)
        ),
        currentTemperaturePercentage = 0.17666666f,
        configMinTemperatureString = "10,0",
        configMaxTemperatureString = "40,0",
        manualModeActive = true,
        loadingState = LoadingTimeoutManager.LoadingState(initialLoading = false, loading = false)
      )
    )
  }

  private fun thermostatDefaultState(
    remoteId: Int,
    setpointTemperatureHeat: Float? = null,
    setpointTemperatureCool: Float? = null,
    manualActive: Boolean = true,
    mode: SuplaHvacMode = SuplaHvacMode.HEAT,
    currentlyCooling: Boolean = false,
    subfunction: ThermostatSubfunction = ThermostatSubfunction.HEAT,
    relatedRemoteIds: List<Int> = emptyList()
  ) =
    ThermostatGeneralViewState(
      viewModelState = ThermostatGeneralViewModelState(
        remoteId = remoteId,
        function = SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
        lastChangedHeat = setpointTemperatureHeat != null,
        configMinTemperature = 10f,
        configMaxTemperature = 40f,
        mode = mode,
        setpointHeatTemperature = setpointTemperatureHeat,
        setpointCoolTemperature = setpointTemperatureCool,
        subfunction = subfunction,
        relatedRemoteIds = relatedRemoteIds
      ),
      isOff = mode == SuplaHvacMode.OFF,
      manualModeActive = if (mode == SuplaHvacMode.OFF) false else manualActive,
      programmedModeActive = manualActive.not(),
      currentTemperaturePercentage = 0.17666666f,
      configMinTemperatureString = "10,0",
      configMaxTemperatureString = "40,0",
      showCoolingIndicator = currentlyCooling,
      loadingState = LoadingTimeoutManager.LoadingState(initialLoading = false, loading = false)
    )

  private fun mockHeatThermostat(
    remoteId: Int,
    deviceId: Int,
    setpointTemperatureHeat: Float,
    weeklyScheduleActive: Boolean = false,
    mode: SuplaHvacMode = SuplaHvacMode.HEAT,
    timerEndDate: Date? = null
  ) {
    val flags = mutableListOf(SuplaThermostatFlag.SETPOINT_TEMP_MIN_SET)
    if (weeklyScheduleActive) {
      flags.add(SuplaThermostatFlag.WEEKLY_SCHEDULE)
    }
    val channelWithChildren = mockChannelWithChildren(
      remoteId = remoteId,
      mode = mode,
      setpointTemperatureHeat = setpointTemperatureHeat,
      flags = flags,
      timerEndDate = timerEndDate
    )

    whenever(channelConfigEventsManager.observerConfig(remoteId)).thenReturn(
      Observable.just(
        ChannelConfigEventsManager.ConfigEvent(
          ConfigResult.RESULT_TRUE,
          mockSuplaChannelHvacConfig(remoteId)
        )
      ),
      Observable.just(
        ChannelConfigEventsManager.ConfigEvent(
          ConfigResult.RESULT_TRUE,
          mockSuplaChannelWeeklyScheduleConfig(remoteId)
        )
      )
    )
    whenever(deviceConfigEventsManager.observerConfig(deviceId)).thenReturn(
      Observable.just(DeviceConfigEventsManager.ConfigEvent(ConfigResult.RESULT_FALSE, null))
    )
    whenever(readChannelWithChildrenTreeUseCase.invoke(remoteId)).thenReturn(
      Observable.just(channelWithChildren)
    )
    whenever(createTemperaturesListUseCase.invoke(channelWithChildren)).thenReturn(emptyList())
    whenever(valuesFormatter.getTemperatureString(10f)).thenReturn("10,0")
    whenever(valuesFormatter.getTemperatureString(40f)).thenReturn("40,0")
  }

  private fun mockCoolThermostat(remoteId: Int, deviceId: Int, setpointTemperature: Float, weeklyScheduleActive: Boolean = false) {
    val flags = mutableListOf(SuplaThermostatFlag.SETPOINT_TEMP_MAX_SET, SuplaThermostatFlag.COOLING)
    if (weeklyScheduleActive) {
      flags.add(SuplaThermostatFlag.WEEKLY_SCHEDULE)
    }
    val channelWithChildren = mockChannelWithChildren(
      remoteId = remoteId,
      mode = SuplaHvacMode.COOL,
      setpointTemperatureCool = setpointTemperature,
      flags = flags
    )

    whenever(channelConfigEventsManager.observerConfig(remoteId)).thenReturn(
      Observable.just(
        ChannelConfigEventsManager.ConfigEvent(
          ConfigResult.RESULT_TRUE,
          mockSuplaChannelHvacConfig(remoteId, ThermostatSubfunction.COOL)
        )
      ),
      Observable.just(
        ChannelConfigEventsManager.ConfigEvent(
          ConfigResult.RESULT_TRUE,
          mockSuplaChannelWeeklyScheduleConfig(remoteId)
        )
      )
    )
    whenever(deviceConfigEventsManager.observerConfig(deviceId)).thenReturn(
      Observable.just(DeviceConfigEventsManager.ConfigEvent(ConfigResult.RESULT_FALSE, null))
    )
    whenever(readChannelWithChildrenTreeUseCase.invoke(remoteId)).thenReturn(Observable.just(channelWithChildren))
    whenever(createTemperaturesListUseCase.invoke(channelWithChildren)).thenReturn(emptyList())
    whenever(valuesFormatter.getTemperatureString(10f)).thenReturn("10,0")
    whenever(valuesFormatter.getTemperatureString(40f)).thenReturn("40,0")
  }

  private fun mockChannelWithChildren(
    remoteId: Int,
    func: SuplaChannelFunction = SuplaChannelFunction.HVAC_THERMOSTAT,
    mode: SuplaHvacMode = SuplaHvacMode.HEAT,
    setpointTemperatureHeat: Float? = null,
    setpointTemperatureCool: Float? = null,
    flags: List<SuplaThermostatFlag> = listOf(SuplaThermostatFlag.SETPOINT_TEMP_MIN_SET),
    timerEndDate: Date? = null
  ): ChannelWithChildren {
    val thermostatValue: ThermostatValue = mockk {
      every { online } returns true
      every { state } returns ThermostatState(1)
      every { this@mockk.mode } returns mode
      every { this@mockk.setpointTemperatureHeat } returns (setpointTemperatureHeat ?: 0f)
      every { this@mockk.setpointTemperatureCool } returns (setpointTemperatureCool ?: 0f)
      every { this@mockk.flags } returns mutableListOf<SuplaThermostatFlag>().apply {
        addAll(flags)
        setpointTemperatureCool?.let { add(SuplaThermostatFlag.HEAT_OR_COOL) }
      }
      every { subfunction } returns (setpointTemperatureCool?.let { ThermostatSubfunction.COOL } ?: ThermostatSubfunction.HEAT)
    }

    val value: ChannelValueEntity = mockk()
    every { value.online } returns true
    every { value.asThermostatValue() } returns thermostatValue

    val suplaExtendedValue = SuplaChannelExtendedValue()
    timerEndDate?.let { suplaExtendedValue.TimerStateValue = SuplaTimerState(it.time.div(1000), null, 0, null) }

    val extendedValue: ChannelExtendedValueEntity = mockk()
    every { extendedValue.getSuplaValue() } returns suplaExtendedValue

    val channelDataEntity: ChannelDataEntity = mockk {
      every { this@mockk.remoteId } returns remoteId
      every { this@mockk.function } returns func
      every { this@mockk.channelExtendedValueEntity } returns extendedValue
      every { this@mockk.channelValueEntity } returns value
    }

    val thermometerEntity: ChannelDataEntity = mockk {
      every { this@mockk.remoteId } returns 999
    }
    val children = listOf(
      mockChannelChildEntity(ChannelRelationType.MAIN_THERMOMETER, SuplaConst.SUPLA_CHANNELFNC_THERMOMETER, dataEntity = thermometerEntity),
      mockChannelChildEntity(ChannelRelationType.AUX_THERMOMETER_FLOOR, remoteId = 998)
    )

    every { children[0].function } returns SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
    every { children[0].channelDataEntity } returns thermometerEntity
    whenever(getChannelValueUseCase.invoke<Double>(thermometerEntity)).thenReturn(15.3)

    return ChannelWithChildren(channelDataEntity, children)
  }

  private fun mockSuplaChannelHvacConfig(
    remoteId: Int,
    subfunction: ThermostatSubfunction = ThermostatSubfunction.HEAT
  ): SuplaChannelHvacConfig =
    SuplaChannelHvacConfig(
      remoteId = remoteId,
      func = SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
      crc32 = 1L,
      mainThermometerRemoteId = 234,
      auxThermometerRemoteId = 345,
      auxThermometerType = SuplaHvacThermometerType.FLOOR,
      antiFreezeAndOverheatProtectionEnabled = false,
      availableAlgorithms = listOf(SuplaHvacAlgorithm.ON_OFF_SETPOINT_AT_MOST),
      usedAlgorithm = SuplaHvacAlgorithm.ON_OFF_SETPOINT_AT_MOST,
      minOnTimeSec = 10,
      minOffTimeSec = 20,
      outputValueOnError = 0,
      subfunction = subfunction,
      temperatureSetpointChangeSwitchesToManualMode = false,
      temperatures = SuplaHvacTemperatures(
        freezeProtection = null,
        eco = null,
        comfort = null,
        boost = null,
        heatProtection = null,
        histeresis = null,
        belowAlarm = null,
        aboveAlarm = null,
        auxMinSetpoint = null,
        auxMaxSetpoint = null,
        roomMin = 1000,
        roomMax = 4000,
        auxMin = null,
        auxMax = null,
        histeresisMin = null,
        histeresisMax = null,
        heatCoolOffsetMin = null,
        heatCoolOffsetMax = null
      )
    )

  private fun mockSuplaChannelWeeklyScheduleConfig(remoteId: Int): SuplaChannelWeeklyScheduleConfig =
    SuplaChannelWeeklyScheduleConfig(
      remoteId = remoteId,
      func = SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
      crc32 = 1L,
      programConfigurations = listOf(),
      schedule = listOf()
    )

  private fun mockChannelChildEntity(
    relationType: ChannelRelationType,
    function: Int? = null,
    dataEntity: ChannelDataEntity? = null,
    remoteId: Int? = null
  ): ChannelChildEntity {
    val channelDataEntity = dataEntity ?: remoteId?.let { mockk { every { this@mockk.remoteId } returns it } } ?: mockk()

    return mockk<ChannelChildEntity> {
      function?.let { every { this@mockk.function } returns it }
      every { this@mockk.relationType } returns relationType
      every { this@mockk.children } returns emptyList()
      every { this@mockk.channelDataEntity } returns channelDataEntity
    }
  }
}
