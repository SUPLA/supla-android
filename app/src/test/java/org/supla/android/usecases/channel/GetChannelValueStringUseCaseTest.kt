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
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.usecases.channel.stringvalueprovider.DepthSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.DistanceSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ElectricityMeterValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.GpmValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.HumidityAndTemperatureValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ImpulseCounterValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ThermometerValueStringProvider

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
  private lateinit var impulseCounterValueStringProvider: ImpulseCounterValueStringProvider

  @InjectMocks
  private lateinit var useCase: GetChannelValueStringUseCase

  @Test
  fun `should get no value text when channel offline`() {
    // given
    val channel: ChannelDataEntity = mockk {
      every { channelValueEntity } returns mockk {
        every { online } returns false
      }
    }

    // when
    val valueText = useCase(channel)

    // then
    assertThat(valueText).isEqualTo(ValuesFormatter.NO_VALUE_TEXT)
  }

  @Test
  fun `should check all handlers if can handle channel and return no value when no one can handle`() {
    // given
    val function = SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
    val channel: ChannelDataEntity = mockk {
      every { this@mockk.function } returns function
      every { channelValueEntity } returns mockk {
        every { online } returns true
      }
    }

    // when
    val valueText = useCase(channel)

    // then
    assertThat(valueText).isEqualTo(ValuesFormatter.NO_VALUE_TEXT)
    verify(thermometerValueProvider).handle(channel)
    verify(humidityAndTemperatureValueProvider).handle(channel)
    verify(depthSensorValueProvider).handle(channel)
    verify(generalPurposeMeasurementValueProvider).handle(channel)
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
    val function = SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
    val value = "some value"
    val channel: ChannelDataEntity = mockk {
      every { this@mockk.function } returns function
      every { channelValueEntity } returns mockk {
        every { online } returns true
      }
    }

    whenever(humidityAndTemperatureValueProvider.handle(channel)).thenReturn(true)
    whenever(humidityAndTemperatureValueProvider.value(channel, ValueType.FIRST)).thenReturn(value)

    // when
    val valueText = useCase(channel)

    // then
    assertThat(valueText).isEqualTo(value)
    verify(thermometerValueProvider).handle(channel)
    verify(humidityAndTemperatureValueProvider).handle(channel)
    verify(humidityAndTemperatureValueProvider).value(channel, ValueType.FIRST)
    verifyNoMoreInteractions(thermometerValueProvider, humidityAndTemperatureValueProvider)
    verifyNoInteractions(depthSensorValueProvider, generalPurposeMeasurementValueProvider)
  }
}
