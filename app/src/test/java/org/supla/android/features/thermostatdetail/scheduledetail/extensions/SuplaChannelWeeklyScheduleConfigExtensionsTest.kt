package org.supla.android.features.thermostatdetail.scheduledetail.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.R
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleEntry
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.features.thermostatdetail.scheduledetail.data.ScheduleDetailEntryBoxKey
import org.supla.android.features.thermostatdetail.scheduledetail.data.ScheduleDetailEntryBoxValue
import org.supla.android.features.thermostatdetail.scheduledetail.data.ScheduleDetailProgramBox
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_COOL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT

@RunWith(MockitoJUnitRunner::class)
class SuplaChannelWeeklyScheduleConfigExtensionsTest {

  @Test
  fun `should create schedule map for schedule table`() {
    // given
    val config = SuplaChannelWeeklyScheduleConfig(
      remoteId = 123,
      func = 234,
      programConfigurations = emptyList(),
      schedule = listOf(
        SuplaWeeklyScheduleEntry(DayOfWeek.MONDAY, 8, QuarterOfHour.FIRST, SuplaScheduleProgram.PROGRAM_1),
        SuplaWeeklyScheduleEntry(DayOfWeek.MONDAY, 8, QuarterOfHour.SECOND, SuplaScheduleProgram.PROGRAM_1),
        SuplaWeeklyScheduleEntry(DayOfWeek.MONDAY, 8, QuarterOfHour.THIRD, SuplaScheduleProgram.PROGRAM_1),
        SuplaWeeklyScheduleEntry(DayOfWeek.MONDAY, 8, QuarterOfHour.FOURTH, SuplaScheduleProgram.PROGRAM_1),
        SuplaWeeklyScheduleEntry(DayOfWeek.MONDAY, 9, QuarterOfHour.FIRST, SuplaScheduleProgram.PROGRAM_1),
        SuplaWeeklyScheduleEntry(DayOfWeek.MONDAY, 9, QuarterOfHour.SECOND, SuplaScheduleProgram.PROGRAM_2),
        SuplaWeeklyScheduleEntry(DayOfWeek.MONDAY, 9, QuarterOfHour.THIRD, SuplaScheduleProgram.PROGRAM_3),
        SuplaWeeklyScheduleEntry(DayOfWeek.MONDAY, 9, QuarterOfHour.FOURTH, SuplaScheduleProgram.PROGRAM_4)
      )
    )

    // when
    val map = config.viewScheduleBoxesMap()

    // then
    assertThat(map.keys).containsExactly(
      ScheduleDetailEntryBoxKey(DayOfWeek.MONDAY, 8),
      ScheduleDetailEntryBoxKey(DayOfWeek.MONDAY, 9)
    )
    assertThat(map.values).containsExactly(
      ScheduleDetailEntryBoxValue(SuplaScheduleProgram.PROGRAM_1),
      ScheduleDetailEntryBoxValue(
        SuplaScheduleProgram.PROGRAM_1,
        SuplaScheduleProgram.PROGRAM_2,
        SuplaScheduleProgram.PROGRAM_3,
        SuplaScheduleProgram.PROGRAM_4
      )
    )
  }

  @Test
  fun `should create program list for schedule detail`() {
    // given
    val function = SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO
    val config = SuplaChannelWeeklyScheduleConfig(
      remoteId = 123,
      func = function,
      programConfigurations = listOf(
        SuplaWeeklyScheduleProgram(
          program = SuplaScheduleProgram.PROGRAM_1,
          mode = SuplaHvacMode.HEAT,
          setpointTemperatureHeat = 2300,
          setpointTemperatureCool = null
        ),
        SuplaWeeklyScheduleProgram(
          program = SuplaScheduleProgram.PROGRAM_2,
          mode = SuplaHvacMode.OFF,
          setpointTemperatureHeat = null,
          setpointTemperatureCool = null
        ),
        SuplaWeeklyScheduleProgram(
          program = SuplaScheduleProgram.PROGRAM_3,
          mode = SuplaHvacMode.COOL,
          setpointTemperatureHeat = null,
          setpointTemperatureCool = 2100
        ),
        SuplaWeeklyScheduleProgram(
          program = SuplaScheduleProgram.PROGRAM_4,
          mode = SuplaHvacMode.AUTO,
          setpointTemperatureHeat = 2100,
          setpointTemperatureCool = 2300
        )
      ),
      schedule = emptyList()
    )

    // when
    val programs = config.viewProgramBoxesList()

    // then
    assertThat(programs).containsExactly(
      ScheduleDetailProgramBox(function, SuplaScheduleProgram.PROGRAM_1, SuplaHvacMode.HEAT, 23f, null, R.drawable.ic_heat),
      ScheduleDetailProgramBox(function, SuplaScheduleProgram.PROGRAM_2, SuplaHvacMode.OFF, null, null, null),
      ScheduleDetailProgramBox(function, SuplaScheduleProgram.PROGRAM_3, SuplaHvacMode.COOL, null, 21f, R.drawable.ic_cool),
      ScheduleDetailProgramBox(function, SuplaScheduleProgram.PROGRAM_4, SuplaHvacMode.AUTO, 21f, 23f, null),
      ScheduleDetailProgramBox(function, SuplaScheduleProgram.OFF, SuplaHvacMode.OFF, null, null, R.drawable.ic_power_button)
    )
  }

  @Test
  fun `should not add icon for program when function is heat`() {
    // given
    val function = SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT
    val config = SuplaChannelWeeklyScheduleConfig(
      remoteId = 123,
      func = function,
      programConfigurations = listOf(
        SuplaWeeklyScheduleProgram(
          program = SuplaScheduleProgram.PROGRAM_1,
          mode = SuplaHvacMode.HEAT,
          setpointTemperatureHeat = 2300,
          setpointTemperatureCool = null
        ),
        SuplaWeeklyScheduleProgram(
          program = SuplaScheduleProgram.PROGRAM_2,
          mode = SuplaHvacMode.OFF,
          setpointTemperatureHeat = null,
          setpointTemperatureCool = null
        ),
        SuplaWeeklyScheduleProgram(
          program = SuplaScheduleProgram.PROGRAM_3,
          mode = SuplaHvacMode.NOT_SET,
          setpointTemperatureHeat = null,
          setpointTemperatureCool = null
        )
      ),
      schedule = emptyList()
    )

    // when
    val programs = config.viewProgramBoxesList()

    // then
    assertThat(programs).containsExactly(
      ScheduleDetailProgramBox(function, SuplaScheduleProgram.PROGRAM_1, SuplaHvacMode.HEAT, 23f, null, null),
      ScheduleDetailProgramBox(function, SuplaScheduleProgram.PROGRAM_2, SuplaHvacMode.OFF, null, null, null),
      ScheduleDetailProgramBox(function, SuplaScheduleProgram.PROGRAM_3, SuplaHvacMode.NOT_SET, null, null, null),
      ScheduleDetailProgramBox(function, SuplaScheduleProgram.OFF, SuplaHvacMode.OFF, null, null, R.drawable.ic_power_button)
    )
  }

  @Test
  fun `should not add icon for program when function is cool`() {
    // given
    val function = SUPLA_CHANNELFNC_HVAC_THERMOSTAT_COOL
    val config = SuplaChannelWeeklyScheduleConfig(
      remoteId = 123,
      func = function,
      programConfigurations = listOf(
        SuplaWeeklyScheduleProgram(
          program = SuplaScheduleProgram.PROGRAM_1,
          mode = SuplaHvacMode.COOL,
          setpointTemperatureHeat = null,
          setpointTemperatureCool = 2300
        ),
        SuplaWeeklyScheduleProgram(
          program = SuplaScheduleProgram.PROGRAM_2,
          mode = SuplaHvacMode.OFF,
          setpointTemperatureHeat = null,
          setpointTemperatureCool = null
        ),
        SuplaWeeklyScheduleProgram(
          program = SuplaScheduleProgram.PROGRAM_3,
          mode = SuplaHvacMode.NOT_SET,
          setpointTemperatureHeat = null,
          setpointTemperatureCool = null
        )
      ),
      schedule = emptyList()
    )

    // when
    val programs = config.viewProgramBoxesList()

    // then
    assertThat(programs).containsExactly(
      ScheduleDetailProgramBox(function, SuplaScheduleProgram.PROGRAM_1, SuplaHvacMode.COOL, null, 23f, null),
      ScheduleDetailProgramBox(function, SuplaScheduleProgram.PROGRAM_2, SuplaHvacMode.OFF, null, null, null),
      ScheduleDetailProgramBox(function, SuplaScheduleProgram.PROGRAM_3, SuplaHvacMode.NOT_SET, null, null, null),
      ScheduleDetailProgramBox(function, SuplaScheduleProgram.OFF, SuplaHvacMode.OFF, null, null, R.drawable.ic_power_button)
    )
  }
}
