package org.supla.android.features.details.thermostatdetail.general.data
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
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
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
import org.supla.core.shared.data.model.function.thermostat.SuplaThermostatFlag
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import java.util.EnumSet

class ThermostatProgramInfoTest {

  @MockK
  private lateinit var valueFormatter: ValueFormatter

  @MockK
  private lateinit var dateProvider: DateProvider

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should fail when date provider not set`() {
    // when
    Assertions.assertThatThrownBy {
      ThermostatProgramInfo.Builder(valueFormatter).build()
    }.hasMessageContaining("Date provider cannot be null")
  }

  @Test
  fun `should fail when config not set`() {
    // given
    val builder = ThermostatProgramInfo.Builder(valueFormatter)
    builder.dateProvider = dateProvider

    // when
    Assertions.assertThatThrownBy {
      builder.build()
    }.hasMessageContaining("Config cannot be null")
  }

  @Test
  fun `should fail when flags not set`() {
    // given
    val builder = ThermostatProgramInfo.Builder(valueFormatter)
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = SuplaChannelWeeklyScheduleConfig(123, null, 1L, emptyList(), emptyList())

    // when
    Assertions.assertThatThrownBy {
      builder.build()
    }.hasMessageContaining("Thermostat flags cannot be null")
  }

  @Test
  fun `should fail when mode not set`() {
    // given
    val builder = ThermostatProgramInfo.Builder(valueFormatter)
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = SuplaChannelWeeklyScheduleConfig(123, null, 1L, emptyList(), emptyList())
    builder.thermostatFlags = emptyList()

    // when
    Assertions.assertThatThrownBy {
      builder.build()
    }.hasMessageContaining("Current mode cannot be null")
  }

  @Test
  fun `should fail when temperature not set`() {
    // given
    val builder = ThermostatProgramInfo.Builder(valueFormatter)
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = SuplaChannelWeeklyScheduleConfig(123, null, 1L, emptyList(), emptyList())
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
    val builder = ThermostatProgramInfo.Builder(valueFormatter)
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = SuplaChannelWeeklyScheduleConfig(123, null, 1L, emptyList(), emptyList())
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
    val builder = ThermostatProgramInfo.Builder(valueFormatter)
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
    val builder = ThermostatProgramInfo.Builder(valueFormatter)
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
    val temperatureString = "18.4"
    val builder = ThermostatProgramInfo.Builder(valueFormatter)
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = mockWeeklyScheduleConfig()
    builder.thermostatFlags = listOf(SuplaThermostatFlag.WEEKLY_SCHEDULE, SuplaThermostatFlag.CLOCK_ERROR)
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f
    builder.channelOnline = true

    every { valueFormatter.format(builder.currentTemperature, ValueFormat.TemperatureWithDegree) } returns temperatureString

    // when
    val list = builder.build()

    // then
    assertThat(list).containsExactly(
      ThermostatProgramInfo(
        type = ThermostatProgramInfo.Type.CURRENT,
        time = localizedString(R.string.thermostat_clock_error),
        icon = R.drawable.ic_heat,
        iconColor = R.color.red,
        manualActive = false,
        descriptionProvider = LocalizedString.Constant("18.4")
      )
    )
  }

  @Test
  fun `should get empty list when could not find needed data`() {
    // given
    val builder = ThermostatProgramInfo.Builder(valueFormatter)
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = mockWeeklyScheduleConfig()
    builder.thermostatFlags = listOf(SuplaThermostatFlag.WEEKLY_SCHEDULE)
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f
    builder.channelOnline = true

    every { dateProvider.currentDayOfWeek() } returns DayOfWeek.THURSDAY
    every { dateProvider.currentHour() } returns 0
    every { dateProvider.currentMinute() } returns 0

    // when
    val list = builder.build()

    // then
    assertThat(list).isEmpty()
  }

  @Test
  fun `should get filled list`() {
    // given
    val temperatureString = "18.4"
    val builder = ThermostatProgramInfo.Builder(valueFormatter)
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = mockWeeklyScheduleConfig()
    builder.thermostatFlags = listOf(SuplaThermostatFlag.WEEKLY_SCHEDULE)
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f
    builder.channelOnline = true

    every { dateProvider.currentDayOfWeek() } returns DayOfWeek.MONDAY
    every { dateProvider.currentHour() } returns 0
    every { dateProvider.currentMinute() } returns 35

    every { valueFormatter.format(builder.currentTemperature, ValueFormat.TemperatureWithDegree) } returns temperatureString
    every { valueFormatter.format(22.0, ValueFormat.TemperatureWithDegree) } returns "22.0"

    // when
    val list = builder.build()

    // then
    assertThat(list).containsExactly(
      ThermostatProgramInfo(
        type = ThermostatProgramInfo.Type.CURRENT,
        time = LocalizedString.WithResourceAndArguments(
          id = R.string.thermostat_detail_program_time,
          arguments = listOf(LocalizedString.WithResourceAndArguments(R.string.time_just_minutes, listOf(55)))
        ),
        icon = R.drawable.ic_heat,
        iconColor = R.color.red,
        manualActive = false,
        descriptionProvider = LocalizedString.Constant("18.4")
      ),
      ThermostatProgramInfo(
        type = ThermostatProgramInfo.Type.NEXT,
        time = null,
        icon = R.drawable.ic_cool,
        iconColor = R.color.blue,
        manualActive = false,
        descriptionProvider = LocalizedString.Constant("22.0")
      )
    )
  }

  @Test
  fun `should get only current program when time sync disabled`() {
    // given
    val temperatureString = "18.4"
    val builder = ThermostatProgramInfo.Builder(valueFormatter)
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = mockWeeklyScheduleConfig()
    builder.deviceConfig = mockDeviceConfigWithDisabledTimeSync()
    builder.thermostatFlags = listOf(SuplaThermostatFlag.WEEKLY_SCHEDULE)
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f
    builder.channelOnline = true

    every { dateProvider.currentDayOfWeek() } returns DayOfWeek.MONDAY
    every { dateProvider.currentHour() } returns 0
    every { dateProvider.currentMinute() } returns 35

    every { valueFormatter.format(builder.currentTemperature, ValueFormat.TemperatureWithDegree) } returns temperatureString

    // when
    val list = builder.build()

    // then
    assertThat(list).containsExactly(
      ThermostatProgramInfo(
        type = ThermostatProgramInfo.Type.CURRENT,
        time = null,
        icon = R.drawable.ic_heat,
        iconColor = R.color.red,
        manualActive = false,
        descriptionProvider = LocalizedString.Constant("18.4")
      )
    )
  }

  @Test
  fun `should get filled list with active temporary schedule change`() {
    // given
    val temperatureString = "18.4"
    val builder = ThermostatProgramInfo.Builder(valueFormatter)
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = mockWeeklyScheduleConfig()
    builder.thermostatFlags = listOf(SuplaThermostatFlag.WEEKLY_SCHEDULE, SuplaThermostatFlag.WEEKLY_SCHEDULE_TEMPORAL_OVERRIDE)
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f
    builder.channelOnline = true

    every { dateProvider.currentDayOfWeek() } returns DayOfWeek.MONDAY
    every { dateProvider.currentHour() } returns 0
    every { dateProvider.currentMinute() } returns 35

    every { valueFormatter.format(builder.currentTemperature, ValueFormat.TemperatureWithDegree) } returns temperatureString
    every { valueFormatter.format(22.0, ValueFormat.TemperatureWithDegree) } returns "22.0"

    // when
    val list = builder.build()

    // then
    assertThat(list).containsExactly(
      ThermostatProgramInfo(
        type = ThermostatProgramInfo.Type.CURRENT,
        time = LocalizedString.WithResourceAndArguments(
          id = R.string.thermostat_detail_program_time,
          arguments = listOf(LocalizedString.WithResourceAndArguments(R.string.time_just_minutes, listOf(55)))
        ),
        icon = R.drawable.ic_heat,
        iconColor = R.color.red,
        manualActive = true,
        descriptionProvider = LocalizedString.Constant(temperatureString)
      ),
      ThermostatProgramInfo(
        type = ThermostatProgramInfo.Type.NEXT,
        time = null,
        icon = R.drawable.ic_cool,
        iconColor = R.color.blue,
        manualActive = false,
        descriptionProvider = LocalizedString.Constant("22.0")
      )
    )
  }

  @Test
  fun `should get empty list when only one program`() {
    // given
    val builder = ThermostatProgramInfo.Builder(valueFormatter)
    builder.dateProvider = dateProvider
    builder.weeklyScheduleConfig = mockWeeklyScheduleConfig(SuplaScheduleProgram.PROGRAM_1)
    builder.thermostatFlags = listOf(SuplaThermostatFlag.WEEKLY_SCHEDULE, SuplaThermostatFlag.WEEKLY_SCHEDULE_TEMPORAL_OVERRIDE)
    builder.currentMode = SuplaHvacMode.HEAT
    builder.currentTemperature = 18.4f
    builder.channelOnline = true

    every { dateProvider.currentDayOfWeek() } returns DayOfWeek.MONDAY
    every { dateProvider.currentHour() } returns 0
    every { dateProvider.currentMinute() } returns 35

    // when
    val list = builder.build()

    // then
    assertThat(list).isEmpty()
  }

  private fun mockWeeklyScheduleConfig(otherProgram: SuplaScheduleProgram = SuplaScheduleProgram.PROGRAM_2) =
    SuplaChannelWeeklyScheduleConfig(
      remoteId = 123,
      func = null,
      crc32 = 1L,
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
}
