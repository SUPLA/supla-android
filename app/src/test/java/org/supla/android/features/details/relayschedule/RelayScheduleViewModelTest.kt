package org.supla.android.features.details.relayschedule

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

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaRelayMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleEntry
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.DeviceConfigEventsManager
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.features.details.relayschedule.data.RelayProgramDuration
import org.supla.android.features.details.relayschedule.data.RelayScheduleProgram
import org.supla.android.features.details.schedule.DelayedWeeklyScheduleConfigSubject
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey
import org.supla.android.ui.views.schedule.editor.QuartersSelectionData
import org.supla.android.ui.views.schedule.editor.ScheduleTableBox
import org.supla.android.ui.views.schedule.editor.ScheduleTableState
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleEditorState
import java.util.concurrent.TimeUnit

class RelayScheduleViewModelTest :
  BaseViewModelTest<RelayScheduleViewState, RelayScheduleViewEvent, RelayScheduleViewModel>(MockSchedulers.MOCKK) {

  @RelaxedMockK
  private lateinit var channelConfigEventsManager: ChannelConfigEventsManager

  @RelaxedMockK
  private lateinit var deviceConfigEventsManager: DeviceConfigEventsManager

  @RelaxedMockK
  private lateinit var loadingTimeoutManager: LoadingTimeoutManager

  @RelaxedMockK
  private lateinit var suplaClientProvider: SuplaClientProvider

  @RelaxedMockK
  private lateinit var suplaClient: SuplaClientApi

  @RelaxedMockK
  private lateinit var dateProvider: DateProvider

  @RelaxedMockK
  private lateinit var delayedWeeklyScheduleConfigSubject: DelayedWeeklyScheduleConfigSubject

  @RelaxedMockK
  override lateinit var threading: SuplaThreading

  @InjectMockKs
  override lateinit var viewModel: RelayScheduleViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()

    every { threading.schedulers.computation } returns testScheduler
    every { dateProvider.currentTimestamp() } returns 100
    every { dateProvider.currentDayOfWeek() } returns DayOfWeek.MONDAY
    every { dateProvider.currentHour() } returns 12
    every { suplaClientProvider.provide() } returns suplaClient
  }

  @Test
  fun `should observe weekly schedule and device config`() {
    // given
    val remoteId = 123
    val deviceId = 321
    val channelConfigSubject = PublishSubject.create<ChannelConfigEventsManager.ConfigEvent>()
    val deviceConfigSubject = PublishSubject.create<DeviceConfigEventsManager.ConfigEvent>()
    val weeklyProgram = SuplaWeeklyScheduleProgram(
      program = SuplaScheduleProgram.PROGRAM_1,
      relayMode = SuplaRelayMode.START_ON,
      relayModeDurationS = 20,
      relayOppositeModeDurationS = 0
    )
    every { channelConfigEventsManager.observerConfig(remoteId) } returns channelConfigSubject
    every { deviceConfigEventsManager.observerConfig(deviceId) } returns deviceConfigSubject

    // when
    viewModel.observeConfig(remoteId, deviceId)
    deviceConfigSubject.onNext(DeviceConfigEventsManager.ConfigEvent(ConfigResult.RESULT_FALSE, null))
    channelConfigSubject.onNext(ChannelConfigEventsManager.ConfigEvent(ConfigResult.RESULT_TRUE, SuplaChannelConfig(remoteId, null, 0)))
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    assertThat(states.last()).isEqualTo(
      RelayScheduleViewState(
        loadingState = LoadingTimeoutManager.LoadingState(initialLoading = false, loading = true, lastLoadingStartTimestamp = 100)
      )
    )

    // when
    channelConfigSubject.onNext(
      ChannelConfigEventsManager.ConfigEvent(
        ConfigResult.RESULT_TRUE,
        SuplaChannelWeeklyScheduleConfig(
          remoteId,
          null,
          0,
          listOf(weeklyProgram),
          listOf(
            SuplaWeeklyScheduleEntry(
              DayOfWeek.MONDAY,
              8,
              QuarterOfHour.FIRST,
              SuplaScheduleProgram.PROGRAM_1
            ),
            SuplaWeeklyScheduleEntry(
              DayOfWeek.MONDAY,
              8,
              QuarterOfHour.THIRD,
              SuplaScheduleProgram.PROGRAM_2
            )
          )
        )
      )
    )
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    assertThat(states.last()).isEqualTo(
      RelayScheduleViewState(
        loadingState = LoadingTimeoutManager.LoadingState(initialLoading = false, loading = false),
        editorState = WeeklyScheduleEditorState(
          programs = listOf(RelayScheduleProgram(weeklyProgram), RelayScheduleProgram.DEFAULT),
          scheduleTableState = ScheduleTableState(
            schedule = mapOf(
              ScheduleDetailEntryBoxKey(DayOfWeek.MONDAY, 8) to ScheduleTableBox(
                firstQuarterProgram = SuplaScheduleProgram.PROGRAM_1,
                secondQuarterProgram = SuplaScheduleProgram.OFF,
                thirdQuarterProgram = SuplaScheduleProgram.PROGRAM_2,
                fourthQuarterProgram = SuplaScheduleProgram.OFF
              )
            ),
            currentDayOfWeek = DayOfWeek.MONDAY,
            currentHour = 12
          )
        )
      )
    )
    verify { suplaClient.getChannelConfig(remoteId, ChannelConfigType.WEEKLY_SCHEDULE) }
    verify { suplaClient.getDeviceConfig(deviceId) }
  }

  @Test
  fun `should reload weekly schedule and stop loading on timeout`() {
    // given
    val remoteId = 123
    lateinit var onTimeout: () -> Unit
    every { loadingTimeoutManager.watch(any(), any()) } answers {
      onTimeout = secondArg()
      mockk<Disposable>(relaxed = true)
    }
    every { channelConfigEventsManager.observerConfig(remoteId) } returns
      PublishSubject.create<ChannelConfigEventsManager.ConfigEvent>()
    every { deviceConfigEventsManager.observerConfig(any()) } returns
      PublishSubject.create<DeviceConfigEventsManager.ConfigEvent>()
    viewModel.observeConfig(remoteId, 321)

    // when
    viewModel.onViewCreated()
    onTimeout()

    // then
    assertThat(states.last()).isEqualTo(
      RelayScheduleViewState(
        loadingState = LoadingTimeoutManager.LoadingState(initialLoading = false, loading = false)
      )
    )
    verify(exactly = 2) { suplaClient.getChannelConfig(remoteId, ChannelConfigType.WEEKLY_SCHEDULE) }
  }

  @Test
  fun `should protect local table changes from incoming configuration`() {
    // given
    val remoteId = 123
    val deviceId = 321
    val key = ScheduleDetailEntryBoxKey(DayOfWeek.MONDAY, 8)
    val channelConfigSubject = PublishSubject.create<ChannelConfigEventsManager.ConfigEvent>()
    val deviceConfigSubject = PublishSubject.create<DeviceConfigEventsManager.ConfigEvent>()
    val serverConfig = SuplaChannelWeeklyScheduleConfig(
      remoteId = remoteId,
      func = null,
      crc32 = 0,
      programConfigurations = listOf(
        weeklyProgram(SuplaScheduleProgram.PROGRAM_1, SuplaRelayMode.START_ON),
        weeklyProgram(SuplaScheduleProgram.PROGRAM_2, SuplaRelayMode.FORCED_ON)
      ),
      schedule = listOf(
        SuplaWeeklyScheduleEntry(
          DayOfWeek.MONDAY,
          8,
          QuarterOfHour.FIRST,
          SuplaScheduleProgram.PROGRAM_1
        )
      )
    )
    every { channelConfigEventsManager.observerConfig(remoteId) } returns channelConfigSubject
    every { deviceConfigEventsManager.observerConfig(deviceId) } returns deviceConfigSubject
    viewModel.observeConfig(remoteId, deviceId)
    deviceConfigSubject.onNext(DeviceConfigEventsManager.ConfigEvent(ConfigResult.RESULT_TRUE, null))
    channelConfigSubject.onNext(ChannelConfigEventsManager.ConfigEvent(ConfigResult.RESULT_TRUE, serverConfig))
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
    viewModel.onScheduleProgramClick(SuplaScheduleProgram.PROGRAM_2)
    viewModel.onScheduleTableTouched(key)

    // when - stale config arrives during table interaction
    channelConfigSubject.onNext(ChannelConfigEventsManager.ConfigEvent(ConfigResult.RESULT_TRUE, serverConfig))
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then
    assertThat(states.last().editorState.scheduleTableState.schedule[key]).isEqualTo(
      ScheduleTableBox(SuplaScheduleProgram.PROGRAM_2)
    )

    // when - interaction ends and stale config arrives immediately afterwards
    viewModel.onScheduleTableReload()
    channelConfigSubject.onNext(ChannelConfigEventsManager.ConfigEvent(ConfigResult.RESULT_TRUE, serverConfig))
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    // then - local state is still protected and a fresh config is requested with a delay
    assertThat(states.last().editorState.scheduleTableState.schedule[key]).isEqualTo(
      ScheduleTableBox(SuplaScheduleProgram.PROGRAM_2)
    )
    testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
    verify(exactly = 2) { suplaClient.getChannelConfig(remoteId, ChannelConfigType.WEEKLY_SCHEDULE) }
  }

  @Test
  fun `should emit current schedule on table reload and reload config on invalidation`() {
    // given
    loadPrograms(weeklyProgram(SuplaScheduleProgram.PROGRAM_1, SuplaRelayMode.START_ON))

    // when
    viewModel.onScheduleTableReload()
    viewModel.onScheduleTableInvalidate()

    // then
    verify {
      delayedWeeklyScheduleConfigSubject.emit(
        match {
          it.remoteId == 123 &&
            it.programConfigurations.single().program == SuplaScheduleProgram.PROGRAM_1
        }
      )
    }
    verify(exactly = 2) { suplaClient.getChannelConfig(123, ChannelConfigType.WEEKLY_SCHEDULE) }
  }

  @Test
  fun `should select only valid schedule programs`() {
    // given
    val remoteId = 123
    val deviceId = 321
    val channelConfigSubject = PublishSubject.create<ChannelConfigEventsManager.ConfigEvent>()
    val deviceConfigSubject = PublishSubject.create<DeviceConfigEventsManager.ConfigEvent>()
    every { channelConfigEventsManager.observerConfig(remoteId) } returns channelConfigSubject
    every { deviceConfigEventsManager.observerConfig(deviceId) } returns deviceConfigSubject

    viewModel.observeConfig(remoteId, deviceId)
    deviceConfigSubject.onNext(DeviceConfigEventsManager.ConfigEvent(ConfigResult.RESULT_TRUE, null))
    channelConfigSubject.onNext(
      ChannelConfigEventsManager.ConfigEvent(
        ConfigResult.RESULT_TRUE,
        SuplaChannelWeeklyScheduleConfig(
          remoteId = remoteId,
          func = null,
          crc32 = 0,
          programConfigurations = listOf(
            weeklyProgram(SuplaScheduleProgram.PROGRAM_1, SuplaRelayMode.START_ON),
            weeklyProgram(SuplaScheduleProgram.PROGRAM_2, SuplaRelayMode.NOT_SET),
            weeklyProgram(SuplaScheduleProgram.PROGRAM_3, SuplaRelayMode.FORCED_OFF),
            weeklyProgram(SuplaScheduleProgram.PROGRAM_4, SuplaRelayMode.AUTOMATIC)
          ),
          schedule = emptyList()
        )
      )
    )
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
    val loadedState = states.last()

    // when - select valid program
    viewModel.onScheduleProgramClick(SuplaScheduleProgram.PROGRAM_1)

    // then
    assertThat(states.last()).isEqualTo(
      loadedState.copy(editorState = loadedState.editorState.copy(activeProgram = SuplaScheduleProgram.PROGRAM_1))
    )

    // when - deselect active program
    viewModel.onScheduleProgramClick(SuplaScheduleProgram.PROGRAM_1)

    // then
    assertThat(states.last()).isEqualTo(loadedState)

    // when - try to select invalid program
    viewModel.onScheduleProgramClick(SuplaScheduleProgram.PROGRAM_1)
    viewModel.onScheduleProgramClick(SuplaScheduleProgram.PROGRAM_2)

    // then
    assertThat(states.last()).isEqualTo(loadedState)

    // when - select default program
    viewModel.onScheduleProgramClick(SuplaScheduleProgram.OFF)

    // then
    assertThat(states.last()).isEqualTo(
      loadedState.copy(editorState = loadedState.editorState.copy(activeProgram = SuplaScheduleProgram.OFF))
    )
  }

  @Test
  fun `should apply active program to touched schedule table entry`() {
    // given
    val key = ScheduleDetailEntryBoxKey(DayOfWeek.MONDAY, 8)
    loadConfiguration(
      programs = listOf(
        weeklyProgram(SuplaScheduleProgram.PROGRAM_1, SuplaRelayMode.START_ON),
        weeklyProgram(SuplaScheduleProgram.PROGRAM_2, SuplaRelayMode.FORCED_ON)
      ),
      schedule = listOf(
        SuplaWeeklyScheduleEntry(
          DayOfWeek.MONDAY,
          8,
          QuarterOfHour.FIRST,
          SuplaScheduleProgram.PROGRAM_1
        )
      )
    )
    viewModel.onScheduleProgramClick(SuplaScheduleProgram.PROGRAM_2)

    // when
    viewModel.onScheduleTableTouched(key)

    // then
    assertThat(states.last().editorState.scheduleTableState.schedule[key]).isEqualTo(
      ScheduleTableBox(SuplaScheduleProgram.PROGRAM_2)
    )
    verify {
      delayedWeeklyScheduleConfigSubject.emit(
        match { change ->
          change.remoteId == 123 &&
            change.schedule
              .filter { it.dayOfWeek == DayOfWeek.MONDAY && it.hour == 8 }
              .all { it.program == SuplaScheduleProgram.PROGRAM_2 }
        }
      )
    }
  }

  @Test
  fun `should edit schedule quarters and emit updated configuration`() {
    // given
    val key = ScheduleDetailEntryBoxKey(DayOfWeek.MONDAY, 8)
    val initialValue = ScheduleTableBox(
      firstQuarterProgram = SuplaScheduleProgram.PROGRAM_1,
      secondQuarterProgram = SuplaScheduleProgram.OFF,
      thirdQuarterProgram = SuplaScheduleProgram.OFF,
      fourthQuarterProgram = SuplaScheduleProgram.OFF
    )
    val loadedState = loadConfiguration(
      programs = listOf(
        weeklyProgram(SuplaScheduleProgram.PROGRAM_1, SuplaRelayMode.START_ON),
        weeklyProgram(SuplaScheduleProgram.PROGRAM_2, SuplaRelayMode.FORCED_ON)
      ),
      schedule = listOf(
        SuplaWeeklyScheduleEntry(
          DayOfWeek.MONDAY,
          8,
          QuarterOfHour.FIRST,
          SuplaScheduleProgram.PROGRAM_1
        )
      )
    )

    // when - open quarter selection
    viewModel.onScheduleTableLongPress(key)

    // then
    assertThat(states.last().quarterSelection).isEqualTo(
      QuartersSelectionData(
        entryKey = key,
        entryValue = initialValue,
        activeProgram = null
      )
    )

    // when - select a program and change one quarter
    viewModel.onQuartersSelectionProgramChange(SuplaScheduleProgram.PROGRAM_2)
    viewModel.onQuartersSelectionQuarterChange(QuarterOfHour.SECOND)

    // then
    assertThat(states.last().quarterSelection?.entryValue).isEqualTo(
      initialValue.copy(secondQuarterProgram = SuplaScheduleProgram.PROGRAM_2)
    )

    // when - confirm changes
    viewModel.onQuartersSelectionFinish()

    // then
    with(states.last()) {
      assertThat(quarterSelection).isNull()
      assertThat(editorState.activeProgram).isEqualTo(SuplaScheduleProgram.PROGRAM_2)
      assertThat(editorState.scheduleTableState.schedule[key]).isEqualTo(
        initialValue.copy(secondQuarterProgram = SuplaScheduleProgram.PROGRAM_2)
      )
    }
    verify {
      delayedWeeklyScheduleConfigSubject.emit(
        match {
          it.remoteId == 123 &&
            it.schedule.first { entry ->
              entry.dayOfWeek == DayOfWeek.MONDAY &&
                entry.hour == 8 &&
                entry.quarterOfHour == QuarterOfHour.SECOND
            }.program == SuplaScheduleProgram.PROGRAM_2
        }
      )
    }
  }

  @Test
  fun `should open and update program settings within duration range`() {
    // given
    val loadedState = loadPrograms(
      SuplaWeeklyScheduleProgram(
        program = SuplaScheduleProgram.PROGRAM_1,
        relayMode = SuplaRelayMode.NOT_SET,
        relayModeDurationS = null,
        relayOppositeModeDurationS = null
      )
    )

    // when - long press default program
    viewModel.onScheduleProgramLongClick(SuplaScheduleProgram.OFF)

    // then
    assertThat(states.last()).isEqualTo(loadedState)

    // when - long press configurable program
    viewModel.onScheduleProgramLongClick(SuplaScheduleProgram.PROGRAM_1)

    // then
    with(states.last().programSettings!!) {
      assertThat(program).isEqualTo(SuplaScheduleProgram.PROGRAM_1)
      assertThat(selectedMode).isEqualTo(SuplaRelayMode.START_ON)
      assertThat(relayModeDurationS).isZero()
      assertThat(relayOppositeModeDurationS).isZero()
      assertThat(relayModeDurationSString).isEqualTo("0")
      assertThat(relayOppositeModeDurationSString).isEqualTo("0")
    }

    // when - try to decrement zero
    viewModel.onProgramSettingsDurationMinusClick(RelayProgramDuration.RELAY_MODE)

    // then
    assertThat(states.last().programSettings!!.relayModeDurationS).isZero()

    // when - increment and provide invalid manual values
    viewModel.onProgramSettingsDurationPlusClick(RelayProgramDuration.RELAY_MODE)
    viewModel.onProgramSettingsDurationManualChange(RelayProgramDuration.RELAY_MODE, "12a")
    viewModel.onProgramSettingsDurationManualChange(RelayProgramDuration.RELAY_MODE, "65536")

    // then
    assertThat(states.last().programSettings!!.relayModeDurationS).isEqualTo(1)
    assertThat(states.last().programSettings!!.relayModeDurationSString).isEqualTo("1")

    // when - provide valid values
    viewModel.onProgramSettingsDurationManualChange(RelayProgramDuration.RELAY_MODE, "00123")
    viewModel.onProgramSettingsDurationManualChange(RelayProgramDuration.OPPOSITE_MODE, "")

    // then
    assertThat(states.last().programSettings!!.relayModeDurationS).isEqualTo(123)
    assertThat(states.last().programSettings!!.relayModeDurationSString).isEqualTo("00123")
    assertThat(states.last().programSettings!!.relayOppositeModeDurationS).isZero()
    assertThat(states.last().programSettings!!.relayOppositeModeDurationSString).isEqualTo("0")

    // when - reach the upper limit and try to increment it
    viewModel.onProgramSettingsDurationManualChange(RelayProgramDuration.OPPOSITE_MODE, "65535")
    viewModel.onProgramSettingsDurationPlusClick(RelayProgramDuration.OPPOSITE_MODE)

    // then
    assertThat(states.last().programSettings!!.relayOppositeModeDurationS).isEqualTo(65_535)
    assertThat(states.last().programSettings!!.relayOppositeModeDurationSString).isEqualTo("65535")
  }

  @Test
  fun `should update and save program settings`() {
    // given
    loadPrograms(weeklyProgram(SuplaScheduleProgram.PROGRAM_1, SuplaRelayMode.START_ON))
    viewModel.onScheduleProgramLongClick(SuplaScheduleProgram.PROGRAM_1)

    // when
    viewModel.onProgramSettingsModeChange(SuplaRelayMode.FORCED_ON)

    // then
    assertThat(states.last().programSettings!!.selectedMode).isEqualTo(SuplaRelayMode.FORCED_ON)

    // when - unsupported mode
    viewModel.onProgramSettingsModeChange(SuplaRelayMode.NOT_SET)

    // then
    assertThat(states.last().programSettings!!.selectedMode).isEqualTo(SuplaRelayMode.FORCED_ON)

    // when
    viewModel.onProgramSettingsSave()

    // then
    with(states.last()) {
      assertThat(programSettings).isNull()
      assertThat(editorState.activeProgram).isEqualTo(SuplaScheduleProgram.PROGRAM_1)
      assertThat(editorState.programs.first().relayMode).isEqualTo(SuplaRelayMode.FORCED_ON)
    }
    verify {
      delayedWeeklyScheduleConfigSubject.emit(
        match {
          it.remoteId == 123 &&
            it.programConfigurations.single().relayMode == SuplaRelayMode.FORCED_ON
        }
      )
    }
  }

  @Test
  fun `should dismiss program settings without saving`() {
    // given
    loadPrograms(weeklyProgram(SuplaScheduleProgram.PROGRAM_1, SuplaRelayMode.START_ON))
    viewModel.onScheduleProgramLongClick(SuplaScheduleProgram.PROGRAM_1)

    // when
    viewModel.onProgramSettingsDismiss()

    // then
    assertThat(states.last().programSettings).isNull()
    verify(exactly = 0) { delayedWeeklyScheduleConfigSubject.emit(any()) }
  }

  private fun loadPrograms(vararg programs: SuplaWeeklyScheduleProgram): RelayScheduleViewState {
    return loadConfiguration(programs.toList(), emptyList())
  }

  private fun loadConfiguration(
    programs: List<SuplaWeeklyScheduleProgram>,
    schedule: List<SuplaWeeklyScheduleEntry>
  ): RelayScheduleViewState {
    val remoteId = 123
    val deviceId = 321
    val channelConfigSubject = PublishSubject.create<ChannelConfigEventsManager.ConfigEvent>()
    val deviceConfigSubject = PublishSubject.create<DeviceConfigEventsManager.ConfigEvent>()
    every { channelConfigEventsManager.observerConfig(remoteId) } returns channelConfigSubject
    every { deviceConfigEventsManager.observerConfig(deviceId) } returns deviceConfigSubject

    viewModel.observeConfig(remoteId, deviceId)
    deviceConfigSubject.onNext(DeviceConfigEventsManager.ConfigEvent(ConfigResult.RESULT_TRUE, null))
    channelConfigSubject.onNext(
      ChannelConfigEventsManager.ConfigEvent(
        ConfigResult.RESULT_TRUE,
        SuplaChannelWeeklyScheduleConfig(remoteId, null, 0, programs, schedule)
      )
    )
    testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

    return states.last()
  }

  private fun weeklyProgram(program: SuplaScheduleProgram, mode: SuplaRelayMode) = SuplaWeeklyScheduleProgram(
    program = program,
    relayMode = mode,
    relayModeDurationS = 0,
    relayOppositeModeDurationS = 0
  )
}
