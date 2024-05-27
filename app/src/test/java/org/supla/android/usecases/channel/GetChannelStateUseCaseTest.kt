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
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CURTAIN
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_MAILSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_PROJECTOR_SCREEN
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_TERRACE_AWNING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_VERTICAL_BLIND
import org.supla.android.usecases.group.GetGroupActivePercentageUseCase

@RunWith(MockitoJUnitRunner::class)
class GetChannelStateUseCaseTest {

  @Mock
  private lateinit var getGroupActivePercentageUseCase: GetGroupActivePercentageUseCase

  @InjectMocks
  private lateinit var useCase: GetChannelStateUseCase

  @Test
  fun `should get closed state from sub value`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_CONTROLLINGTHEGATE, subValueHi = 1)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.CLOSED)
  }

  @Test
  fun `should get open state from sub value`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_CONTROLLINGTHEGATE, subValueHi = 0)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get partially open state from sub value`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR, subValueHi = 2)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.PARTIALLY_OPENED)
  }

  @Test
  fun `should get closed state for roller shutter`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER, rollerShutterPosition = 100)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.CLOSED)
  }

  @Test
  fun `should get open state for roller shutter`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW, rollerShutterPosition = 0)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get closed state for facade blind`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND, rollerShutterPosition = 100)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.CLOSED)
  }

  @Test
  fun `should get open state for facade blind`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND, rollerShutterPosition = 0)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get closed state for terrace awning`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_TERRACE_AWNING, rollerShutterPosition = 100)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.CLOSED)
  }

  @Test
  fun `should get open state for terrace awning`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_TERRACE_AWNING, rollerShutterPosition = 0)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get closed state for projector screen`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_PROJECTOR_SCREEN, rollerShutterPosition = 100)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get open state for projector screen`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_PROJECTOR_SCREEN, rollerShutterPosition = 0)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.CLOSED)
  }

  @Test
  fun `should get closed state for curtain`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_CURTAIN, rollerShutterPosition = 100)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.CLOSED)
  }

  @Test
  fun `should get open state for curtain`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_CURTAIN, rollerShutterPosition = 0)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get closed state for vertical blind`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_VERTICAL_BLIND, rollerShutterPosition = 100)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.CLOSED)
  }

  @Test
  fun `should get open state for vertical blind`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_VERTICAL_BLIND, rollerShutterPosition = 0)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get closed state from is closed property`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY, isClosed = true)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.CLOSED)
  }

  @Test
  fun `should get open state from is closed property`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY, isClosed = false)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get off state from is closed property`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_POWERSWITCH, isClosed = false)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OFF)
  }

  @Test
  fun `should get on state from is closed property`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_STAIRCASETIMER, isClosed = true)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.ON)
  }

  @Test
  fun `should get off state for dimmer`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_DIMMER, brightness = 0)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OFF)
  }

  @Test
  fun `should get on state for dimmer`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_DIMMER, brightness = 5)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.ON)
  }

  @Test
  fun `should get off state for rgb`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_RGBLIGHTING, colorBrightness = 0)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OFF)
  }

  @Test
  fun `should get on state for rgb`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_RGBLIGHTING, colorBrightness = 5)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.ON)
  }

  @Test
  fun `should get off off state for dimmer and rgb`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING, colorBrightness = 0, brightness = 0)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.COMPLEX)
    assertThat(state.complex).containsExactly(ChannelState.Value.OFF, ChannelState.Value.OFF)
  }

  @Test
  fun `should get off on state for dimmer and rgb`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING, colorBrightness = 3, brightness = 0)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.COMPLEX)
    assertThat(state.complex).containsExactly(ChannelState.Value.OFF, ChannelState.Value.ON)
  }

  @Test
  fun `should get on off state for dimmer and rgb`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING, colorBrightness = 0, brightness = 3)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.COMPLEX)
    assertThat(state.complex).containsExactly(ChannelState.Value.ON, ChannelState.Value.OFF)
  }

  @Test
  fun `should get on on state for dimmer and rgb`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING, colorBrightness = 4, brightness = 3)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.COMPLEX)
    assertThat(state.complex).containsExactly(ChannelState.Value.ON, ChannelState.Value.ON)
  }

  @Test
  fun `should get transparent state for digiglass`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL, transparent = true)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.TRANSPARENT)
  }

  @Test
  fun `should get opaque state for digiglass`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL, transparent = false)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPAQUE)
  }

  @Test
  fun `should get thermostat heat`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_HVAC_THERMOSTAT, thermostatSubfunction = ThermostatSubfunction.HEAT)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.HEAT)
  }

  @Test
  fun `should get thermostat cool`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_HVAC_THERMOSTAT, thermostatSubfunction = ThermostatSubfunction.COOL)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.COOL)
  }

  @Test
  fun `should get offline state for open-close function`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY, online = false)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPEN)
  }

  @Test
  fun `should get offline state for on-off function`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_MAILSENSOR, online = false)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OFF)
  }

  @Test
  fun `should get offline state for complex state`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING, online = false)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.COMPLEX)
    assertThat(state.complex).containsExactly(ChannelState.Value.OFF, ChannelState.Value.OFF)
  }

  @Test
  fun `should get offline state for transparent-opaque function`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL, online = false)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.OPAQUE)
  }

  @Test
  fun `should get offline state for heat thermostat`() {
    // given
    val channelData =
      mockChannelDataEntity(SUPLA_CHANNELFNC_HVAC_THERMOSTAT, online = false, thermostatSubfunction = ThermostatSubfunction.HEAT)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.HEAT)
  }

  @Test
  fun `should get offline state for cool thermostat`() {
    // given
    val channelData =
      mockChannelDataEntity(SUPLA_CHANNELFNC_HVAC_THERMOSTAT, online = false, thermostatSubfunction = ThermostatSubfunction.COOL)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.COOL)
  }

  @Test
  fun `should get not used state`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_RING)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.NOT_USED)
  }

  @Test
  fun `should get not used state for offline`() {
    // given
    val channelData = mockChannelDataEntity(SUPLA_CHANNELFNC_RING, online = false)

    // when
    val state = useCase(channelData)

    // then
    assertThat(state.value).isEqualTo(ChannelState.Value.NOT_USED)
  }

  private fun mockChannelDataEntity(
    function: Int,
    online: Boolean = true,
    subValueHi: Int = 0,
    rollerShutterPosition: Int = 0,
    isClosed: Boolean = false,
    brightness: Int = 0,
    colorBrightness: Int = 0,
    transparent: Boolean = false,
    thermostatSubfunction: ThermostatSubfunction? = null,
  ): ChannelDataEntity =
    mockk {
      every { this@mockk.function } returns function
      every { channelValueEntity } returns mockk<ChannelValueEntity>().also {
        every { it.online } returns online
        every { it.getSubValueHi() } returns subValueHi
        every { it.isClosed() } returns isClosed
        every { it.asBrightness() } returns brightness
        every { it.asBrightnessColor() } returns colorBrightness
        every { it.asDigiglassValue() } returns mockk { every { isAnySectionTransparent } returns transparent }
        if (thermostatSubfunction != null) {
          every { it.asThermostatValue() } returns mockk { every { subfunction } returns thermostatSubfunction }
        }
        every { it.asRollerShutterValue() } returns mockk { every { position } returns rollerShutterPosition }
        every { it.asFacadeBlindValue() } returns mockk { every { position } returns rollerShutterPosition }
      }
    }
}
