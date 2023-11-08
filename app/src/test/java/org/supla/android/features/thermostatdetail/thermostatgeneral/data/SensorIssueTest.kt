package org.supla.android.features.thermostatdetail.thermostatgeneral.data

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.local.entity.ThermostatState
import org.supla.android.data.source.local.entity.ThermostatValue
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlags
import org.supla.android.db.Channel
import org.supla.android.usecases.channel.ChannelChild

@RunWith(MockitoJUnitRunner::class)
class SensorIssueTest {

  @Test
  fun `should not create issue when no flag set`() {
    // given
    val value = ThermostatValue(ThermostatState(0), SuplaHvacMode.OFF, 10f, 10f, emptyList())
    val children = emptyList<ChannelChild>()

    // when
    val sensorIssue = SensorIssue.build(value, children)

    // then
    assertThat(sensorIssue).isNull()
  }

  @Test
  fun `should create issue`() {
    // given
    val channel: Channel = mockk()
    val value = ThermostatValue(ThermostatState(0), SuplaHvacMode.OFF, 10f, 10f, listOf(SuplaThermostatFlags.FORCED_OFF_BY_SENSOR))
    val children = listOf(ChannelChild(relationType = ChannelRelationType.DEFAULT, channel = channel))

    // when
    val sensorIssue = SensorIssue.build(value, children)

    // then
    assertThat(sensorIssue).isNotNull
  }
}
