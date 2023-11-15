package org.supla.android.features.thermostatdetail.thermostatgeneral.data
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
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.StringProvider
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.AutomaticTimeSyncField
import org.supla.android.data.source.remote.FieldType
import org.supla.android.data.source.remote.SuplaDeviceConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleEntry
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlags
import java.util.EnumSet

@RunWith(MockitoJUnitRunner::class)
class ThermostatProgramInfoTest {

  @Mock
  private lateinit var dateProvider: DateProvider

  @Test
  fun `should fail when date provider not set`() {
    // when
    Assertions.assertThatThrownBy {
      ThermostatProgramInfo.Builder().build()
    }.hasMessageContaining("Date provider cannot be null")
  }

  @Test
  fun `should fail when config not set`() {
    // given
    val builder = ThermostatProgramInfo.Builder()
    builder.dateProvider = dateProvider

    // when
    Assertions.assertThatThrownBy {
      builder.build()
    }.hasMessageContaining("Config cannot be null")
  }

  @Test
  fun `should fail when flags not set`() {
    // given
    val builder = ThermostatProgramInfo.Builder()
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = SuplaChannelWeeklyScheduleConfig(123, null, emptyList(), emptyList())

    // when
    Assertions.assertThatThrownBy {
      builder.build()
    }.hasMessageContaining("Thermostat flags cannot be null")
  }

  @Test
  fun `should fail when mode not set`() {
    // given
    val builder = ThermostatProgramInfo.Builder()
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = SuplaChannelWeeklyScheduleConfig(123, null, emptyList(), emptyList())
    builder.thermostatFlags = emptyList()

    // when
    Assertions.assertThatThrownBy {
      builder.build()
    }.hasMessageContaining("Current mode cannot be null")
  }

  @Test
  fun `should fail when temperature not set`() {
    // given
    val builder = ThermostatProgramInfo.Builder()
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = SuplaChannelWeeklyScheduleConfig(123, null, emptyList(), emptyList())
    builder.thermostatFlags = emptyList()
    builder.currentMode = SuplaHvacMode.HEAT

    // when
    Assertions.assertThatThrownBy {
      builder.build()
    }.hasMessageContaining("Current temperature cannot be null")
  }

  @Test
  fun `should fail when online not set`() {
    // given
    val builder = ThermostatProgramInfo.Builder()
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = SuplaChannelWeeklyScheduleConfig(123, null, emptyList(), emptyList())
    builder.thermostatFlags = emptyList()
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f

    // when
    Assertions.assertThatThrownBy {
      builder.build()
    }.hasMessageContaining("Channel online cannot be null")
  }

  @Test
  fun `should get empty list when channel is offline`() {
    // given
    val builder = ThermostatProgramInfo.Builder()
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = mockWeeklyScheduleConfig()
    builder.thermostatFlags = emptyList()
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f
    builder.channelOnline = false

    // when
    val list = builder.build()

    // then
    assertThat(list).isEmpty()
  }

  @Test
  fun `should get empty list when weekly schedule is not active`() {
    // given
    val builder = ThermostatProgramInfo.Builder()
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = mockWeeklyScheduleConfig()
    builder.thermostatFlags = emptyList()
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f
    builder.channelOnline = true

    // when
    val list = builder.build()

    // then
    assertThat(list).isEmpty()
  }

  @Test
  fun `should get error list when clock error set`() {
    // given
    val builder = ThermostatProgramInfo.Builder()
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = mockWeeklyScheduleConfig()
    builder.thermostatFlags = listOf(SuplaThermostatFlags.WEEKLY_SCHEDULE, SuplaThermostatFlags.CLOCK_ERROR)
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f
    builder.channelOnline = true

    // when
    val list = builder.build()

    // then
    assertThat(list).hasSize(1)
    assertThat(list).extracting({ it.type }, { it.icon }, { it.iconColor }, { it.manualActive })
      .containsExactly(tuple(ThermostatProgramInfo.Type.CURRENT, R.drawable.ic_heat, R.color.red, false))

    assertStringProvider(list[0].time!!, R.string.thermostat_clock_error)
  }

  @Test
  fun `should get empty list when could not find needed data`() {
    // given
    val builder = ThermostatProgramInfo.Builder()
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = mockWeeklyScheduleConfig()
    builder.thermostatFlags = listOf(SuplaThermostatFlags.WEEKLY_SCHEDULE)
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f
    builder.channelOnline = true

    whenever(dateProvider.currentDayOfWeek()).thenReturn(DayOfWeek.THURSDAY)
    whenever(dateProvider.currentHour()).thenReturn(0)
    whenever(dateProvider.currentMinute()).thenReturn(0)

    // when
    val list = builder.build()

    // then
    assertThat(list).isEmpty()
  }

  @Test
  fun `should get filled list`() {
    // given
    val builder = ThermostatProgramInfo.Builder()
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = mockWeeklyScheduleConfig()
    builder.thermostatFlags = listOf(SuplaThermostatFlags.WEEKLY_SCHEDULE)
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f
    builder.channelOnline = true

    whenever(dateProvider.currentDayOfWeek()).thenReturn(DayOfWeek.MONDAY)
    whenever(dateProvider.currentHour()).thenReturn(0)
    whenever(dateProvider.currentMinute()).thenReturn(35)

    // when
    val list = builder.build()

    // then
    assertThat(list).extracting({ it.type }, { it.icon }, { it.iconColor }, { it.manualActive })
      .containsExactly(
        tuple(ThermostatProgramInfo.Type.CURRENT, R.drawable.ic_heat, R.color.red, false),
        tuple(ThermostatProgramInfo.Type.NEXT, R.drawable.ic_cool, R.color.blue, false)
      )
  }

  @Test
  fun `should empty list when time synchronization disabled`() {
    // given
    val builder = ThermostatProgramInfo.Builder()
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = mockWeeklyScheduleConfig()
    builder.deviceConfig = mockDeviceConfigWithDisabledTimeSync()
    builder.thermostatFlags = listOf(SuplaThermostatFlags.WEEKLY_SCHEDULE)
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f
    builder.channelOnline = true

    // when
    val list = builder.build()

    // then
    assertThat(list).isEmpty()
  }

  @Test
  fun `should get filled list with active temporary schedule change`() {
    // given
    val builder = ThermostatProgramInfo.Builder()
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = mockWeeklyScheduleConfig()
    builder.thermostatFlags = listOf(SuplaThermostatFlags.WEEKLY_SCHEDULE, SuplaThermostatFlags.WEEKLY_SCHEDULE_TEMPORAL_OVERRIDE)
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f
    builder.channelOnline = true

    whenever(dateProvider.currentDayOfWeek()).thenReturn(DayOfWeek.MONDAY)
    whenever(dateProvider.currentHour()).thenReturn(0)
    whenever(dateProvider.currentMinute()).thenReturn(35)

    // when
    val list = builder.build()

    // then
    assertThat(list).extracting({ it.type }, { it.icon }, { it.iconColor }, { it.manualActive })
      .containsExactly(
        tuple(ThermostatProgramInfo.Type.CURRENT, R.drawable.ic_heat, R.color.red, true),
        tuple(ThermostatProgramInfo.Type.NEXT, R.drawable.ic_cool, R.color.blue, false)
      )
  }

  @Test
  fun `should get empty list when only one program`() {
    // given
    val builder = ThermostatProgramInfo.Builder()
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = mockWeeklyScheduleConfig(SuplaScheduleProgram.PROGRAM_1)
    builder.thermostatFlags = listOf(SuplaThermostatFlags.WEEKLY_SCHEDULE, SuplaThermostatFlags.WEEKLY_SCHEDULE_TEMPORAL_OVERRIDE)
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f
    builder.channelOnline = true

    whenever(dateProvider.currentDayOfWeek()).thenReturn(DayOfWeek.MONDAY)
    whenever(dateProvider.currentHour()).thenReturn(0)
    whenever(dateProvider.currentMinute()).thenReturn(35)

    // when
    val list = builder.build()

    // then
    assertThat(list).isEmpty()
  }

  private fun mockWeeklyScheduleConfig(otherProgram: SuplaScheduleProgram = SuplaScheduleProgram.PROGRAM_2) =
    SuplaChannelWeeklyScheduleConfig(
      remoteId = 123,
      func = null,
      programConfigurations = listOf(
        SuplaWeeklyScheduleProgram(
          program = SuplaScheduleProgram.PROGRAM_1,
          mode = SuplaHvacMode.HEAT,
          setpointTemperatureCool = 1200,
          setpointTemperatureHeat = 0
        ),
        SuplaWeeklyScheduleProgram(
          program = SuplaScheduleProgram.PROGRAM_2,
          mode = SuplaHvacMode.COOL,
          setpointTemperatureCool = 2200,
          setpointTemperatureHeat = 0
        )
      ),
      schedule = listOf(
        SuplaWeeklyScheduleEntry(
          dayOfWeek = DayOfWeek.MONDAY,
          hour = 0,
          quarterOfHour = QuarterOfHour.SECOND,
          program = SuplaScheduleProgram.PROGRAM_1
        ),
        SuplaWeeklyScheduleEntry(
          dayOfWeek = DayOfWeek.MONDAY,
          hour = 0,
          quarterOfHour = QuarterOfHour.FIRST,
          program = SuplaScheduleProgram.PROGRAM_1
        ),
        SuplaWeeklyScheduleEntry(
          dayOfWeek = DayOfWeek.MONDAY,
          hour = 0,
          quarterOfHour = QuarterOfHour.THIRD,
          program = SuplaScheduleProgram.PROGRAM_1
        ),
        SuplaWeeklyScheduleEntry(
          dayOfWeek = DayOfWeek.MONDAY,
          hour = 0,
          quarterOfHour = QuarterOfHour.FOURTH,
          program = SuplaScheduleProgram.PROGRAM_1
        ),
        SuplaWeeklyScheduleEntry(
          dayOfWeek = DayOfWeek.MONDAY,
          hour = 1,
          quarterOfHour = QuarterOfHour.SECOND,
          program = SuplaScheduleProgram.PROGRAM_1
        ),
        SuplaWeeklyScheduleEntry(
          dayOfWeek = DayOfWeek.MONDAY,
          hour = 1,
          quarterOfHour = QuarterOfHour.FIRST,
          program = SuplaScheduleProgram.PROGRAM_1
        ),
        SuplaWeeklyScheduleEntry(
          dayOfWeek = DayOfWeek.MONDAY,
          hour = 1,
          quarterOfHour = QuarterOfHour.THIRD,
          program = otherProgram
        ),
        SuplaWeeklyScheduleEntry(
          dayOfWeek = DayOfWeek.MONDAY,
          hour = 1,
          quarterOfHour = QuarterOfHour.FOURTH,
          program = otherProgram
        )
      )
    )

  private fun mockDeviceConfigWithDisabledTimeSync() = SuplaDeviceConfig(
    deviceId = 123,
    availableFields = EnumSet.of(FieldType.AUTOMATIC_TIME_SYNC),
    fields = listOf(AutomaticTimeSyncField(FieldType.AUTOMATIC_TIME_SYNC, false))
  )

  private fun assertStringProvider(stringProvider: StringProvider, expectedStringRes: Int) {
    val context = mock(Context::class.java)
    whenever(context.getString(anyInt())).thenReturn("")
    stringProvider(context)
    verify(context).getString(expectedStringRes)
  }
}
