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

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.usecases.channel.valueprovider.ContainerValueProvider
import org.supla.android.usecases.channel.valueprovider.DepthSensorValueProvider
import org.supla.android.usecases.channel.valueprovider.DistanceSensorValueProvider
import org.supla.android.usecases.channel.valueprovider.ElectricityMeterValueProvider
import org.supla.android.usecases.channel.valueprovider.GpmValueProvider
import org.supla.android.usecases.channel.valueprovider.HeatpolThermostatValueProvider
import org.supla.android.usecases.channel.valueprovider.HumidityAndTemperatureValueProvider
import org.supla.android.usecases.channel.valueprovider.HumidityValueProvider
import org.supla.android.usecases.channel.valueprovider.ImpulseCounterValueProvider
import org.supla.android.usecases.channel.valueprovider.PressureSensorValueProvider
import org.supla.android.usecases.channel.valueprovider.RainSensorValueProvider
import org.supla.android.usecases.channel.valueprovider.SwitchWithElectricityMeterValueProvider
import org.supla.android.usecases.channel.valueprovider.SwitchWithImpulseCounterValueProvider
import org.supla.android.usecases.channel.valueprovider.ThermometerValueProvider
import org.supla.android.usecases.channel.valueprovider.WeightSensorValueProvider
import org.supla.android.usecases.channel.valueprovider.WindSensorValueProvider
import org.supla.core.shared.data.model.general.SuplaFunction

class GetChannelValueUseCaseTest {

  @MockK(relaxed = true)
  private lateinit var depthSensorValueProvider: DepthSensorValueProvider

  @MockK(relaxed = true)
  private lateinit var gpmValueProvider: GpmValueProvider

  @MockK(relaxed = true)
  private lateinit var humidityAndTemperatureValueProvider: HumidityAndTemperatureValueProvider

  @MockK(relaxed = true)
  private lateinit var thermometerValueProvider: ThermometerValueProvider

  @MockK(relaxed = true)
  private lateinit var distanceSensorValueProvider: DistanceSensorValueProvider

  @MockK(relaxed = true)
  private lateinit var electricityMeterValueProvider: ElectricityMeterValueProvider

  @MockK(relaxed = true)
  private lateinit var impulseCounterValueProvider: ImpulseCounterValueProvider

  @MockK(relaxed = true)
  private lateinit var switchWithElectricityMeterValueProvider: SwitchWithElectricityMeterValueProvider

  @MockK(relaxed = true)
  private lateinit var switchWithImpulseCounterValueProvider: SwitchWithImpulseCounterValueProvider

  @MockK(relaxed = true)
  private lateinit var pressureSensorValueProvider: PressureSensorValueProvider

  @MockK(relaxed = true)
  private lateinit var rainSensorValueProvider: RainSensorValueProvider

  @MockK(relaxed = true)
  private lateinit var containerValueProvider: ContainerValueProvider

  @MockK(relaxed = true)
  private lateinit var weightSensorValueProvider: WeightSensorValueProvider

  @MockK(relaxed = true)
  private lateinit var windSensorValueProvider: WindSensorValueProvider

  @MockK(relaxed = true)
  private lateinit var heatpolThermostatValueProvider: HeatpolThermostatValueProvider

  @MockK(relaxed = true)
  private lateinit var humidityValueProvider: HumidityValueProvider

  @InjectMockKs
  private lateinit var useCase: GetChannelValueUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should check all providers if can handle channel and throw exception that could not provide channel value`() {
    // given
    val channel: ChannelWithChildren = mockk {
      every { function } returns SuplaFunction.HUMIDITY
    }

    // when
    Assertions.assertThatThrownBy {
      useCase.invoke(channel)
    }
      .hasMessage("No value provider for channel function `${SuplaFunction.HUMIDITY}`")
      .isInstanceOf(IllegalStateException::class.java)

    // then
    verify {
      thermometerValueProvider.handle(channel)
      humidityAndTemperatureValueProvider.handle(channel)
      depthSensorValueProvider.handle(channel)
      gpmValueProvider.handle(channel)
      distanceSensorValueProvider.handle(channel)
      electricityMeterValueProvider.handle(channel)
      impulseCounterValueProvider.handle(channel)
      switchWithElectricityMeterValueProvider.handle(channel)
      switchWithImpulseCounterValueProvider.handle(channel)
      pressureSensorValueProvider.handle(channel)
      rainSensorValueProvider.handle(channel)
      containerValueProvider.handle(channel)
      weightSensorValueProvider.handle(channel)
      windSensorValueProvider.handle(channel)
      heatpolThermostatValueProvider.handle(channel)
      humidityValueProvider.handle(channel)
    }
    confirmVerified(
      thermometerValueProvider,
      humidityAndTemperatureValueProvider,
      depthSensorValueProvider,
      gpmValueProvider,
      distanceSensorValueProvider,
      electricityMeterValueProvider,
      impulseCounterValueProvider,
      switchWithElectricityMeterValueProvider,
      switchWithImpulseCounterValueProvider,
      pressureSensorValueProvider,
      rainSensorValueProvider,
      containerValueProvider,
      weightSensorValueProvider,
      windSensorValueProvider,
      heatpolThermostatValueProvider,
      humidityValueProvider
    )
  }

  @Test
  fun `should return value of first provider which can handle channel`() {
    // given
    val function = SuplaFunction.HUMIDITY_AND_TEMPERATURE
    val value = 12.4
    val channel: ChannelWithChildren = mockk {
      every { this@mockk.function } returns function
      every { channel } returns mockk {
        every { channelValueEntity } returns mockk {
          every { status } returns SuplaChannelAvailabilityStatus.ONLINE
        }
      }
    }

    every { humidityAndTemperatureValueProvider.handle(channel) } returns true
    every { humidityAndTemperatureValueProvider.value(channel, ValueType.FIRST) } returns value

    // when
    val valueText: Double = useCase(channel)

    // then
    Assertions.assertThat(valueText).isEqualTo(value)
    verify {
      depthSensorValueProvider.handle(channel)
      gpmValueProvider.handle(channel)
      humidityAndTemperatureValueProvider.handle(channel)
      humidityAndTemperatureValueProvider.value(channel, ValueType.FIRST)
    }
    confirmVerified(
      thermometerValueProvider,
      humidityAndTemperatureValueProvider,
      depthSensorValueProvider,
      gpmValueProvider,
      distanceSensorValueProvider,
      electricityMeterValueProvider,
      impulseCounterValueProvider,
      switchWithElectricityMeterValueProvider,
      switchWithImpulseCounterValueProvider,
      pressureSensorValueProvider,
      rainSensorValueProvider,
      containerValueProvider,
      weightSensorValueProvider,
      windSensorValueProvider,
      heatpolThermostatValueProvider,
      humidityValueProvider
    )
  }
}
