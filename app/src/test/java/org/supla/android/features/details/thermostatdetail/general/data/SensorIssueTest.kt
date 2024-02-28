package org.supla.android.features.details.thermostatdetail.general.data

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.local.entity.ThermostatState
import org.supla.android.data.source.local.entity.ThermostatValue
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlags
import org.supla.android.db.Channel

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

    // when
    val sensorIssue = SensorIssue.build(value, children)

    // then
    assertThat(sensorIssue).isNull()
  }

  @Test
  fun `should create issue`() {
    // given
    val channel: Channel = mockk()
    val value: ThermostatValue = mockk {
      every { online } returns true
      every { state } returns ThermostatState(0)
      every { mode } returns SuplaHvacMode.OFF
      every { setpointTemperatureHeat } returns 10f
      every { setpointTemperatureCool } returns 10f
      every { flags } returns listOf(SuplaThermostatFlags.FORCED_OFF_BY_SENSOR)
    }
    val children = listOf<ChannelChildEntity>()
//    val children = listOf(ChannelChild(relationType = ChannelRelationType.DEFAULT, channel = channel))

    // when
    val sensorIssue = SensorIssue.build(value, children)

    // then
    assertThat(sensorIssue).isNotNull
  }
}
