package org.supla.android.features.details.thermostatdetail.schedule.extensions

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.R
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleEntry
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.features.details.thermostatdetail.schedule.data.ScheduleDetailProgramBox
import org.supla.android.features.details.thermostatdetail.schedule.data.ThermostatScheduleDetailEntryBoxValue
import org.supla.android.features.details.thermostatdetail.ui.OFF
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat

class SuplaChannelWeeklyScheduleConfigExtensionsTest {

  @MockK
  private lateinit var valueFormatter: ValueFormatter

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should create schedule map for schedule table`() {
    // given
    val config = SuplaChannelWeeklyScheduleConfig(
      remoteId = 123,
      func = 234,
      crc32 = 1L,
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
      ThermostatScheduleDetailEntryBoxValue(SuplaScheduleProgram.PROGRAM_1),
      ThermostatScheduleDetailEntryBoxValue(
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
    val function = SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL
    val thermostatFunction = ThermostatSubfunction.HEAT
    val config = SuplaChannelWeeklyScheduleConfig(
      remoteId = 123,
      crc32 = 1L,
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
          mode = SuplaHvacMode.HEAT_COOL,
          setpointTemperatureHeat = 2100,
          setpointTemperatureCool = 2300
        )
      ),
      schedule = emptyList()
    )
    every { valueFormatter.format(23f, ValueFormat.TemperatureWithDegree) } returns "23.0"
    every { valueFormatter.format(21f, ValueFormat.TemperatureWithDegree) } returns "21.0"

    // when
    val programs = config.viewProgramBoxesList(thermostatFunction, valueFormatter)

    // then
    assertThat(programs).containsExactly(
      ScheduleDetailProgramBox(
        function,
        thermostatFunction,
        SuplaScheduleProgram.PROGRAM_1,
        SuplaHvacMode.HEAT,
        23f,
        null,
        LocalizedString.Constant("23.0"),
        R.drawable.ic_heat
      ),
      ScheduleDetailProgramBox(
        function,
        thermostatFunction,
        SuplaScheduleProgram.PROGRAM_2,
        SuplaHvacMode.OFF,
        null,
        null,
        LocalizedString.Constant("---"),
        null
      ),
      ScheduleDetailProgramBox(
        function,
        thermostatFunction,
        SuplaScheduleProgram.PROGRAM_3,
        SuplaHvacMode.COOL,
        null,
        21f,
        LocalizedString.Constant("21.0"),
        R.drawable.ic_cool
      ),
      ScheduleDetailProgramBox(
        function,
        thermostatFunction,
        SuplaScheduleProgram.PROGRAM_4,
        SuplaHvacMode.HEAT_COOL,
        21f,
        23f,
        LocalizedString.Constant("21.0 - 23.0"),
        null
      ),
      ScheduleDetailProgramBox(
        function,
        thermostatFunction,
        SuplaScheduleProgram.OFF,
        SuplaHvacMode.OFF,
        null,
        null,
        localizedString(R.string.turn_off),
        R.drawable.ic_power_button
      )
    )
  }

  @Test
  fun `should not add icon for program when function is heat`() {
    // given
    val function = SUPLA_CHANNELFNC_HVAC_THERMOSTAT
    val thermostatFunction = ThermostatSubfunction.HEAT
    val config = SuplaChannelWeeklyScheduleConfig(
      remoteId = 123,
      func = function,
      crc32 = 1L,
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
    every { valueFormatter.format(23f, ValueFormat.TemperatureWithDegree) } returns "23.0"

    // when
    val programs = config.viewProgramBoxesList(thermostatFunction, valueFormatter)

    // then
    assertThat(programs).containsExactly(
      ScheduleDetailProgramBox(
        function,
        thermostatFunction,
        SuplaScheduleProgram.PROGRAM_1,
        SuplaHvacMode.HEAT,
        23f,
        null,
        LocalizedString.Constant("23.0"),
        null
      ),
      ScheduleDetailProgramBox(
        function,
        thermostatFunction,
        SuplaScheduleProgram.PROGRAM_2,
        SuplaHvacMode.OFF,
        null,
        null,
        LocalizedString.Constant(NO_VALUE_TEXT),
        null
      ),
      ScheduleDetailProgramBox(
        function,
        thermostatFunction,
        SuplaScheduleProgram.PROGRAM_3,
        SuplaHvacMode.NOT_SET,
        null,
        null,
        LocalizedString.Constant(NO_VALUE_TEXT),
        null
      ),
      ScheduleDetailProgramBox(
        function,
        thermostatFunction,
        SuplaScheduleProgram.OFF,
        SuplaHvacMode.OFF,
        null,
        null,
        localizedString(R.string.turn_off),
        R.drawable.ic_power_button
      )
    )
  }

  @Test
  fun `should not add icon for program when function is cool`() {
    // given
    val function = SUPLA_CHANNELFNC_HVAC_THERMOSTAT
    val thermostatFunction = ThermostatSubfunction.COOL
    val config = SuplaChannelWeeklyScheduleConfig(
      remoteId = 123,
      func = function,
      crc32 = 1L,
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
    every { valueFormatter.format(23f, ValueFormat.TemperatureWithDegree) } returns "23.0"

    // when
    val programs = config.viewProgramBoxesList(thermostatFunction, valueFormatter)

    // then
    assertThat(programs).containsExactly(
      ScheduleDetailProgramBox(
        function,
        thermostatFunction,
        SuplaScheduleProgram.PROGRAM_1,
        SuplaHvacMode.COOL,
        null,
        23f,
        LocalizedString.Constant("23.0"),
        null
      ),
      ScheduleDetailProgramBox(
        function,
        thermostatFunction,
        SuplaScheduleProgram.PROGRAM_2,
        SuplaHvacMode.OFF,
        null,
        null,
        LocalizedString.Constant(NO_VALUE_TEXT),
        null
      ),
      ScheduleDetailProgramBox(
        function,
        thermostatFunction,
        SuplaScheduleProgram.PROGRAM_3,
        SuplaHvacMode.NOT_SET,
        null,
        null,
        LocalizedString.Constant(NO_VALUE_TEXT),
        null
      ),
      ScheduleDetailProgramBox(
        function,
        thermostatFunction,
        SuplaScheduleProgram.OFF,
        SuplaHvacMode.OFF,
        null,
        null,
        localizedString(R.string.turn_off),
        R.drawable.ic_power_button
      )
    )
  }
}
