package org.supla.android.usecases.channel
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_MAILSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_TERRACE_AWNING

@RunWith(MockitoJUnitRunner::class)
class GetChannelStateUseCaseTest {

  @InjectMocks
  private lateinit var useCase: GetChannelStateUseCase

  @Test
  fun `should get closed state from sub value`() {
    // given
    val function = SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK
    val stateWrapper = mockState(subValueHi = 1)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.CLOSED)
  }

  @Test
  fun `should get open state from sub value`() {
    // given
    val function = SUPLA_CHANNELFNC_CONTROLLINGTHEGATE
    val stateWrapper = mockState(subValueHi = 0)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get partially open state from sub value`() {
    // given
    val function = SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR
    val stateWrapper = mockState(subValueHi = 2)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.PARTIALLY_OPENED)
  }

  @Test
  fun `should get closed state for roller shutter`() {
    // given
    val function = SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
    val stateWrapper = mockState(rollerShutterClosed = true)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.CLOSED)
  }

  @Test
  fun `should get open state for roller shutter`() {
    // given
    val function = SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
    val stateWrapper = mockState(rollerShutterClosed = false)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get closed state for facade blind`() {
    // given
    val function = SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
    val stateWrapper = mockState(rollerShutterClosed = true)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.CLOSED)
  }

  @Test
  fun `should get open state for facade blind`() {
    // given
    val function = SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
    val stateWrapper = mockState(rollerShutterClosed = false)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get closed state for terrace awning`() {
    // given
    val function = SUPLA_CHANNELFNC_TERRACE_AWNING
    val stateWrapper = mockState(rollerShutterClosed = true)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.CLOSED)
  }

  @Test
  fun `should get open state for terrace awning`() {
    // given
    val function = SUPLA_CHANNELFNC_TERRACE_AWNING
    val stateWrapper = mockState(rollerShutterClosed = false)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get closed state from is closed property`() {
    // given
    val function = SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY
    val stateWrapper = mockState(isClosed = true)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.CLOSED)
  }

  @Test
  fun `should get open state from is closed property`() {
    // given
    val function = SUPLA_CHANNELFNC_OPENSENSOR_GATE
    val stateWrapper = mockState(isClosed = false)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get off state from is closed property`() {
    // given
    val function = SUPLA_CHANNELFNC_POWERSWITCH
    val stateWrapper = mockState(isClosed = false)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OFF)
  }

  @Test
  fun `should get on state from is closed property`() {
    // given
    val function = SUPLA_CHANNELFNC_STAIRCASETIMER
    val stateWrapper = mockState(isClosed = true)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.ON)
  }

  @Test
  fun `should get off state for dimmer`() {
    // given
    val function = SUPLA_CHANNELFNC_DIMMER
    val stateWrapper = mockState(brightness = 0)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OFF)
  }

  @Test
  fun `should get on state for dimmer`() {
    // given
    val function = SUPLA_CHANNELFNC_DIMMER
    val stateWrapper = mockState(brightness = 5)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.ON)
  }

  @Test
  fun `should get off state for rgb`() {
    // given
    val function = SUPLA_CHANNELFNC_RGBLIGHTING
    val stateWrapper = mockState(colorBrightness = 0)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OFF)
  }

  @Test
  fun `should get on state for rgb`() {
    // given
    val function = SUPLA_CHANNELFNC_RGBLIGHTING
    val stateWrapper = mockState(colorBrightness = 5)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.ON)
  }

  @Test
  fun `should get off off state for dimmer and rgb`() {
    // given
    val function = SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
    val stateWrapper = mockState(colorBrightness = 0, brightness = 0)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.COMPLEX)
    assertThat(state.complex).containsExactly(ChannelState.Value.OFF, ChannelState.Value.OFF)
  }

  @Test
  fun `should get off on state for dimmer and rgb`() {
    // given
    val function = SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
    val stateWrapper = mockState(colorBrightness = 3, brightness = 0)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.COMPLEX)
    assertThat(state.complex).containsExactly(ChannelState.Value.OFF, ChannelState.Value.ON)
  }

  @Test
  fun `should get on off state for dimmer and rgb`() {
    // given
    val function = SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
    val stateWrapper = mockState(colorBrightness = 0, brightness = 3)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.COMPLEX)
    assertThat(state.complex).containsExactly(ChannelState.Value.ON, ChannelState.Value.OFF)
  }

  @Test
  fun `should get on on state for dimmer and rgb`() {
    // given
    val function = SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
    val stateWrapper = mockState(colorBrightness = 4, brightness = 3)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.COMPLEX)
    assertThat(state.complex).containsExactly(ChannelState.Value.ON, ChannelState.Value.ON)
  }

  @Test
  fun `should get transparent state for digiglass`() {
    // given
    val function = SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL
    val stateWrapper = mockState(transparent = true)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.TRANSPARENT)
  }

  @Test
  fun `should get opaque state for digiglass`() {
    // given
    val function = SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL
    val stateWrapper = mockState(transparent = false)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPAQUE)
  }

  @Test
  fun `should get thermostat heat`() {
    // given
    val function = SUPLA_CHANNELFNC_HVAC_THERMOSTAT
    val stateWrapper = mockState(thermostatSubfunction = ThermostatSubfunction.HEAT)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.HEAT)
  }

  @Test
  fun `should get thermostat cool`() {
    // given
    val function = SUPLA_CHANNELFNC_HVAC_THERMOSTAT
    val stateWrapper = mockState(thermostatSubfunction = ThermostatSubfunction.COOL)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.COOL)
  }

  @Test
  fun `should get offline state for open-close function`() {
    // given
    val function = SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY
    val stateWrapper = mockState(online = false)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get offline state for on-off function`() {
    // given
    val function = SUPLA_CHANNELFNC_MAILSENSOR
    val stateWrapper = mockState(online = false)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OFF)
  }

  @Test
  fun `should get offline state for complex state`() {
    // given
    val function = SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
    val stateWrapper = mockState(online = false)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.COMPLEX)
    assertThat(state.complex).containsExactly(ChannelState.Value.OFF, ChannelState.Value.OFF)
  }

  @Test
  fun `should get offline state for transparent-opaque function`() {
    // given
    val function = SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL
    val stateWrapper = mockState(online = false)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPAQUE)
  }

  @Test
  fun `should get offline state for heat thermostat`() {
    // given
    val function = SUPLA_CHANNELFNC_HVAC_THERMOSTAT
    val stateWrapper = mockState(online = false, thermostatSubfunction = ThermostatSubfunction.HEAT)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.HEAT)
  }

  @Test
  fun `should get offline state for cool thermostat`() {
    // given
    val function = SUPLA_CHANNELFNC_HVAC_THERMOSTAT
    val stateWrapper = mockState(online = false, thermostatSubfunction = ThermostatSubfunction.COOL)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.COOL)
  }

  @Test
  fun `should get not used state`() {
    // given
    val function = SUPLA_CHANNELFNC_RING
    val stateWrapper = mockState(online = true)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.NOT_USED)
  }

  @Test
  fun `should get not used state for offline`() {
    // given
    val function = SUPLA_CHANNELFNC_RING
    val stateWrapper = mockState(online = false)

    // when
    val state = useCase(function, stateWrapper)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.NOT_USED)
  }

  private fun mockState(
    online: Boolean = true,
    subValueHi: Int = 0,
    rollerShutterClosed: Boolean = false,
    isClosed: Boolean = false,
    brightness: Int = 0,
    colorBrightness: Int = 0,
    transparent: Boolean = false,
    thermostatSubfunction: ThermostatSubfunction? = null
  ): ValueStateWrapper {
    return mockk<ValueStateWrapper>().also {
      every { it.online } returns online
      every { it.subValueHi } returns subValueHi
      every { it.rollerShutterClosed } returns rollerShutterClosed
      every { it.isClosed } returns isClosed
      every { it.brightness } returns brightness
      every { it.colorBrightness } returns colorBrightness
      every { it.transparent } returns transparent
      every { it.thermostatSubfunction } returns thermostatSubfunction
    }
  }
}
