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
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.usecases.channel.valueprovider.ContainerValueProvider
import org.supla.android.usecases.channel.valueprovider.DepthSensorValueProvider
import org.supla.android.usecases.channel.valueprovider.DistanceSensorValueProvider
import org.supla.android.usecases.channel.valueprovider.ElectricityMeterValueProvider
import org.supla.android.usecases.channel.valueprovider.GpmValueProvider
import org.supla.android.usecases.channel.valueprovider.HumidityAndTemperatureValueProvider
import org.supla.android.usecases.channel.valueprovider.ImpulseCounterValueProvider
import org.supla.android.usecases.channel.valueprovider.PressureSensorValueProvider
import org.supla.android.usecases.channel.valueprovider.RainSensorValueProvider
import org.supla.android.usecases.channel.valueprovider.SwitchWithElectricityMeterValueProvider
import org.supla.android.usecases.channel.valueprovider.SwitchWithImpulseCounterValueProvider
import org.supla.android.usecases.channel.valueprovider.ThermometerValueProvider
import org.supla.android.usecases.channel.valueprovider.WeightSensorValueProvider
import org.supla.core.shared.data.model.general.SuplaFunction

@RunWith(MockitoJUnitRunner::class)
class GetChannelValueUseCaseTest {

  @Mock
  private lateinit var depthSensorValueProvider: DepthSensorValueProvider

  @Mock
  private lateinit var gpmValueProvider: GpmValueProvider

  @Mock
  private lateinit var humidityAndTemperatureValueProvider: HumidityAndTemperatureValueProvider

  @Mock
  private lateinit var thermometerValueProvider: ThermometerValueProvider

  @Mock
  private lateinit var distanceSensorValueProvider: DistanceSensorValueProvider

  @Mock
  private lateinit var electricityMeterValueProvider: ElectricityMeterValueProvider

  @Mock
  private lateinit var impulseCounterValueProvider: ImpulseCounterValueProvider

  @Mock
  private lateinit var switchWithElectricityMeterValueProvider: SwitchWithElectricityMeterValueProvider

  @Mock
  private lateinit var switchWithImpulseCounterValueProvider: SwitchWithImpulseCounterValueProvider

  @Mock
  private lateinit var pressureSensorValueProvider: PressureSensorValueProvider

  @Mock
  private lateinit var rainSensorValueProvider: RainSensorValueProvider

  @Mock
  private lateinit var containerValueProvider: ContainerValueProvider

  @Mock
  private lateinit var weightSensorValueProvider: WeightSensorValueProvider

  @InjectMocks
  private lateinit var useCase: GetChannelValueUseCase

  @Test
  fun `should check all handlers if can handle channel and throw exception that could not provide channel value`() {
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
    verify(thermometerValueProvider).handle(channel)
    verify(humidityAndTemperatureValueProvider).handle(channel)
    verify(depthSensorValueProvider).handle(channel)
    verify(gpmValueProvider).handle(channel)
    verifyNoMoreInteractions(
      thermometerValueProvider,
      humidityAndTemperatureValueProvider,
      depthSensorValueProvider,
      gpmValueProvider
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

    whenever(humidityAndTemperatureValueProvider.handle(channel)).thenReturn(true)
    whenever(humidityAndTemperatureValueProvider.value(channel, ValueType.FIRST)).thenReturn(value)

    // when
    val valueText: Double = useCase(channel)

    // then
    Assertions.assertThat(valueText).isEqualTo(value)
    verify(depthSensorValueProvider).handle(channel)
    verify(gpmValueProvider).handle(channel)
    verify(humidityAndTemperatureValueProvider).handle(channel)
    verify(humidityAndTemperatureValueProvider).value(channel, ValueType.FIRST)
    verifyNoMoreInteractions(depthSensorValueProvider, gpmValueProvider, humidityAndTemperatureValueProvider)
    verifyNoInteractions(thermometerValueProvider)
  }
}
