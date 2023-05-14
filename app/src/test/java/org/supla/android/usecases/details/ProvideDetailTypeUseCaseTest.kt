package org.supla.android.usecases.details

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.db.Channel
import org.supla.android.db.ChannelValue
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS
import org.supla.android.lib.SuplaConst.*

@RunWith(MockitoJUnitRunner::class)
class ProvideDetailTypeUseCaseTest {

  @InjectMocks
  private lateinit var useCase: ProvideDetailTypeUseCase

  @Test
  fun `should provide detail for dimmer`() {
    testDetailType(SUPLA_CHANNELFNC_DIMMER, DetailType.RGBW)
  }

  @Test
  fun `should provide detail for dimmer and RGB`() {
    testDetailType(SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING, DetailType.RGBW)
  }

  @Test
  fun `should provide detail for RGB`() {
    testDetailType(SUPLA_CHANNELFNC_RGBLIGHTING, DetailType.RGBW)
  }

  @Test
  fun `should provide detail for roller shutter`() {
    testDetailType(SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER, DetailType.RS)
  }

  @Test
  fun `should provide detail for roof window`() {
    testDetailType(SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW, DetailType.RS)
  }

  @Test
  fun `should provide detail for light switch with impulse counter`() {
    testDetailType(SUPLA_CHANNELFNC_LIGHTSWITCH, DetailType.IC) { channel ->
      val channelValue: ChannelValue = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_IC_MEASUREMENTS.toShort()
      every { channel.value } returns channelValue
    }
  }

  @Test
  fun `should provide detail for power switch with impulse counter`() {
    testDetailType(SUPLA_CHANNELFNC_POWERSWITCH, DetailType.IC) { channel ->
      val channelValue: ChannelValue = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_IC_MEASUREMENTS.toShort()
      every { channel.value } returns channelValue
    }
  }

  @Test
  fun `should provide detail for stair case timer with impulse counter`() {
    testDetailType(SUPLA_CHANNELFNC_STAIRCASETIMER, DetailType.IC) { channel ->
      val channelValue: ChannelValue = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_IC_MEASUREMENTS.toShort()
      every { channel.value } returns channelValue
    }
  }

  @Test
  fun `should provide detail for light switch with measurement`() {
    testDetailType(SUPLA_CHANNELFNC_LIGHTSWITCH, DetailType.EM) { channel ->
      val channelValue: ChannelValue = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort()
      every { channel.value } returns channelValue
    }
  }

  @Test
  fun `should provide detail for power switch with measurement`() {
    testDetailType(SUPLA_CHANNELFNC_POWERSWITCH, DetailType.EM) { channel ->
      val channelValue: ChannelValue = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort()
      every { channel.value } returns channelValue
    }
  }

  @Test
  fun `should provide detail for stair case timer with measurement`() {
    testDetailType(SUPLA_CHANNELFNC_STAIRCASETIMER, DetailType.EM) { channel ->
      val channelValue: ChannelValue = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort()
      every { channel.value } returns channelValue
    }
  }

  @Test
  fun `should not provide detail for light switch without sub value type`() {
    testDetailType(SUPLA_CHANNELFNC_LIGHTSWITCH, null) { channel ->
      every { channel.value } returns null
    }
  }

  @Test
  fun `should not provide detail for power switch without sub value type`() {
    testDetailType(SUPLA_CHANNELFNC_POWERSWITCH, null) { channel ->
      every { channel.value } returns null
    }
  }

  @Test
  fun `should not provide detail for stair case timer without sub value type`() {
    testDetailType(SUPLA_CHANNELFNC_STAIRCASETIMER, null) { channel ->
      every { channel.value } returns null
    }
  }

  @Test
  fun `should provide detail for electricity meter`() {
    testDetailType(SUPLA_CHANNELFNC_ELECTRICITY_METER, DetailType.EM)
  }

  @Test
  fun `should provide detail for electricity IC`() {
    testDetailType(SUPLA_CHANNELFNC_IC_ELECTRICITY_METER, DetailType.IC)
  }

  @Test
  fun `should provide detail for gas IC`() {
    testDetailType(SUPLA_CHANNELFNC_IC_GAS_METER, DetailType.IC)
  }

  @Test
  fun `should provide detail for water IC`() {
    testDetailType(SUPLA_CHANNELFNC_IC_WATER_METER, DetailType.IC)
  }

  @Test
  fun `should provide detail for heat IC`() {
    testDetailType(SUPLA_CHANNELFNC_IC_HEAT_METER, DetailType.IC)
  }

  @Test
  fun `should provide detail for thermometer`() {
    testDetailType(SUPLA_CHANNELFNC_THERMOMETER, DetailType.TEMPERATURE)
  }

  @Test
  fun `should provide detail for thermometer with humidity`() {
    testDetailType(SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE, DetailType.TEMPERATURE_HUMIDITY)
  }

  @Test
  fun `should provide detail for HP thermostat`() {
    testDetailType(SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS, DetailType.THERMOSTAT_HP)
  }

  @Test
  fun `should provide detail for digiglass`() {
    testDetailType(SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL, DetailType.DIGIGLASS)
    testDetailType(SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL, DetailType.DIGIGLASS)
  }

  @Test
  fun `should not provide detail for unsupported channel function`() {
    testDetailType(SUPLA_BIT_FUNC_CONTROLLINGTHEGATE, null)
  }

  private fun testDetailType(function: Int, result: DetailType?, extraMocks: ((Channel) -> Unit) = { }) {
    // given
    val channel: Channel = mockk()
    every { channel.func } returns function
    extraMocks(channel)

    // when
    val detailType = useCase(channel)

    // then
    if (result == null) {
      assertThat(detailType).isNull()
    } else {
      assertThat(detailType).isEqualTo(result)
    }
  }
}
