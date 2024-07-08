package org.supla.android.features.details.thermostatdetail.general.data

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlag
import org.supla.android.data.source.remote.thermostat.ThermostatState
import org.supla.android.data.source.remote.thermostat.ThermostatValue
import org.supla.android.usecases.icon.GetChannelIconUseCase

@RunWith(MockitoJUnitRunner::class)
class SensorIssueTest {

  @Test
  fun `should not create issue when no flag set`() {
    // given
    val value: ThermostatValue = mockk {
      every { online } returns true
      every { state } returns ThermostatState(0)
      every { mode } returns SuplaHvacMode.OFF
      every { setpointTemperatureHeat } returns 10f
      every { setpointTemperatureCool } returns 10f
      every { flags } returns emptyList()
    }
    val children = emptyList<ChannelChildEntity>()
    val getChannelIconUseCase: GetChannelIconUseCase = mockk()

    // when
    val sensorIssue = SensorIssue.build(value, children, getChannelIconUseCase)

    // then
    assertThat(sensorIssue).isNull()
  }

  @Test
  fun `should create issue`() {
    // given
    val value: ThermostatValue = mockk {
      every { online } returns true
      every { state } returns ThermostatState(0)
      every { mode } returns SuplaHvacMode.OFF
      every { setpointTemperatureHeat } returns 10f
      every { setpointTemperatureCool } returns 10f
      every { flags } returns listOf(SuplaThermostatFlag.FORCED_OFF_BY_SENSOR)
    }
    val children = listOf<ChannelChildEntity>()
    val getChannelIconUseCase: GetChannelIconUseCase = mockk()

    // when
    val sensorIssue = SensorIssue.build(value, children, getChannelIconUseCase)

    // then
    assertThat(sensorIssue).isNotNull
  }
}
