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
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.usecases.channel.stringvalueprovider.ContainerValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.DepthSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.DistanceSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ElectricityMeterValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.GpmValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.HumidityAndTemperatureValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.HumidityValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ImpulseCounterValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.PressureSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.RainSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.SwitchWithMeterValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ThermometerValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.WeightSensorValueStringProvider
import org.supla.core.shared.data.model.general.SuplaFunction

@RunWith(MockitoJUnitRunner::class)
class GetChannelValueStringUseCaseTest {

  @Mock
  private lateinit var thermometerValueProvider: ThermometerValueStringProvider

  @Mock
  private lateinit var humidityAndTemperatureValueProvider: HumidityAndTemperatureValueStringProvider

  @Mock
  private lateinit var depthSensorValueProvider: DepthSensorValueStringProvider

  @Mock
  private lateinit var generalPurposeMeasurementValueProvider: GpmValueStringProvider

  @Mock
  private lateinit var distanceSensorValueStringProvider: DistanceSensorValueStringProvider

  @Mock
  private lateinit var electricityMeterValueStringProvider: ElectricityMeterValueStringProvider

  @Mock
  private lateinit var switchWithMeterValueStringProvider: SwitchWithMeterValueStringProvider

  @Mock
  private lateinit var impulseCounterValueStringProvider: ImpulseCounterValueStringProvider

  @Mock
  private lateinit var pressureSensorValueStringProvider: PressureSensorValueStringProvider

  @Mock
  private lateinit var rainSensorValueStringProvider: RainSensorValueStringProvider

  @Mock
  private lateinit var humidityValueStringProvider: HumidityValueStringProvider

  @Mock
  private lateinit var containerValueStringProvider: ContainerValueStringProvider

  @Mock
  private lateinit var weightSensorValueStringProvider: WeightSensorValueStringProvider

  @InjectMocks
  private lateinit var useCase: GetChannelValueStringUseCase

  @Test
  fun `should get no value text when channel offline`() {
    // given
    val channel: ChannelDataEntity = mockk {
      every { channelValueEntity } returns mockk {
        every { function } returns SuplaFunction.HUMIDITY_AND_TEMPERATURE
        every { online } returns false
      }
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    // when
    val valueText = useCase(channelWithChildren)

    // then
    assertThat(valueText).isEqualTo(ValuesFormatter.NO_VALUE_TEXT)
  }

  @Test
  fun `should check all handlers if can handle channel and return no value when no one can handle`() {
    // given
    val function = SuplaFunction.HUMIDITY_AND_TEMPERATURE
    val channel: ChannelDataEntity = mockk {
      every { this@mockk.function } returns function
      every { channelValueEntity } returns mockk {
        every { online } returns true
      }
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    // when
    val valueText = useCase(channelWithChildren)

    // then
    assertThat(valueText).isEqualTo(ValuesFormatter.NO_VALUE_TEXT)
    verify(thermometerValueProvider).handle(channelWithChildren)
    verify(humidityAndTemperatureValueProvider).handle(channelWithChildren)
    verify(depthSensorValueProvider).handle(channelWithChildren)
    verify(generalPurposeMeasurementValueProvider).handle(channelWithChildren)
    verifyNoMoreInteractions(
      thermometerValueProvider,
      humidityAndTemperatureValueProvider,
      depthSensorValueProvider,
      generalPurposeMeasurementValueProvider
    )
  }

  @Test
  fun `should return value of first provider which can handle channel`() {
    // given
    val function = SuplaFunction.HUMIDITY_AND_TEMPERATURE
    val value = "some value"
    val channel: ChannelDataEntity = mockk {
      every { this@mockk.function } returns function
      every { channelValueEntity } returns mockk {
        every { online } returns true
      }
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    whenever(humidityAndTemperatureValueProvider.handle(channelWithChildren)).thenReturn(true)
    whenever(humidityAndTemperatureValueProvider.value(channelWithChildren, ValueType.FIRST)).thenReturn(value)

    // when
    val valueText = useCase(channelWithChildren)

    // then
    assertThat(valueText).isEqualTo(value)
    verify(thermometerValueProvider).handle(channelWithChildren)
    verify(humidityAndTemperatureValueProvider).handle(channelWithChildren)
    verify(humidityAndTemperatureValueProvider).value(channelWithChildren, ValueType.FIRST)
    verifyNoMoreInteractions(thermometerValueProvider, humidityAndTemperatureValueProvider)
    verifyNoInteractions(depthSensorValueProvider, generalPurposeMeasurementValueProvider)
  }
}
