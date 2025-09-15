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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.usecases.channel.stringvalueprovider.ContainerValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.DepthSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.DistanceSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ElectricityMeterValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.GpmValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.HeatpolThermostatValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.HumidityAndTemperatureValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.HumidityValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ImpulseCounterValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.PressureSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.RainSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.SwitchWithMeterValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ThermometerValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.WeightSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.WindSensorValueStringProvider
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT

class GetChannelValueStringUseCaseTest {

  @MockK(relaxed = true)
  private lateinit var thermometerValueProvider: ThermometerValueStringProvider

  @MockK(relaxed = true)
  private lateinit var humidityAndTemperatureValueProvider: HumidityAndTemperatureValueStringProvider

  @MockK(relaxed = true)
  private lateinit var depthSensorValueProvider: DepthSensorValueStringProvider

  @MockK(relaxed = true)
  private lateinit var generalPurposeMeasurementValueProvider: GpmValueStringProvider

  @MockK(relaxed = true)
  private lateinit var distanceSensorValueStringProvider: DistanceSensorValueStringProvider

  @MockK(relaxed = true)
  private lateinit var electricityMeterValueStringProvider: ElectricityMeterValueStringProvider

  @MockK(relaxed = true)
  private lateinit var switchWithMeterValueStringProvider: SwitchWithMeterValueStringProvider

  @MockK(relaxed = true)
  private lateinit var impulseCounterValueStringProvider: ImpulseCounterValueStringProvider

  @MockK(relaxed = true)
  private lateinit var pressureSensorValueStringProvider: PressureSensorValueStringProvider

  @MockK(relaxed = true)
  private lateinit var rainSensorValueStringProvider: RainSensorValueStringProvider

  @MockK(relaxed = true)
  private lateinit var humidityValueStringProvider: HumidityValueStringProvider

  @MockK(relaxed = true)
  private lateinit var containerValueStringProvider: ContainerValueStringProvider

  @MockK(relaxed = true)
  private lateinit var weightSensorValueStringProvider: WeightSensorValueStringProvider

  @MockK(relaxed = true)
  private lateinit var windSensorValueStringProvider: WindSensorValueStringProvider

  @MockK(relaxed = true)
  private lateinit var heatpolThermostatValueStringProvider: HeatpolThermostatValueStringProvider

  @InjectMockKs
  private lateinit var useCase: GetChannelValueStringUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should get no value text when channel offline`() {
    // given
    val channel: ChannelDataEntity = mockk {
      every { channelValueEntity } returns mockk {
        every { function } returns SuplaFunction.HUMIDITY_AND_TEMPERATURE
        every { status } returns SuplaChannelAvailabilityStatus.OFFLINE
      }
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    // when
    val valueText = useCase(channelWithChildren)

    // then
    assertThat(valueText).isEqualTo(NO_VALUE_TEXT)
  }

  @Test
  fun `should check all handlers if can handle channel and return no value when no one can handle`() {
    // given
    val function = SuplaFunction.HUMIDITY_AND_TEMPERATURE
    val channel: ChannelDataEntity = mockk {
      every { this@mockk.function } returns function
      every { channelValueEntity } returns mockk {
        every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      }
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    // when
    val valueText = useCase(channelWithChildren)

    // then
    assertThat(valueText).isEqualTo(NO_VALUE_TEXT)
    verify {
      thermometerValueProvider.handle(channelWithChildren)
      humidityAndTemperatureValueProvider.handle(channelWithChildren)
      depthSensorValueProvider.handle(channelWithChildren)
      generalPurposeMeasurementValueProvider.handle(channelWithChildren)
      distanceSensorValueStringProvider.handle(channelWithChildren)
      electricityMeterValueStringProvider.handle(channelWithChildren)
      switchWithMeterValueStringProvider.handle(channelWithChildren)
      impulseCounterValueStringProvider.handle(channelWithChildren)
      pressureSensorValueStringProvider.handle(channelWithChildren)
      rainSensorValueStringProvider.handle(channelWithChildren)
      humidityValueStringProvider.handle(channelWithChildren)
      containerValueStringProvider.handle(channelWithChildren)
      weightSensorValueStringProvider.handle(channelWithChildren)
      windSensorValueStringProvider.handle(channelWithChildren)
      heatpolThermostatValueStringProvider.handle(channelWithChildren)
    }
    confirmVerified(
      thermometerValueProvider,
      humidityAndTemperatureValueProvider,
      depthSensorValueProvider,
      generalPurposeMeasurementValueProvider,
      distanceSensorValueStringProvider,
      electricityMeterValueStringProvider,
      switchWithMeterValueStringProvider,
      impulseCounterValueStringProvider,
      pressureSensorValueStringProvider,
      rainSensorValueStringProvider,
      humidityValueStringProvider,
      containerValueStringProvider,
      weightSensorValueStringProvider,
      windSensorValueStringProvider,
      heatpolThermostatValueStringProvider
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
        every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      }
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    every { humidityAndTemperatureValueProvider.handle(channelWithChildren) } returns true
    every { humidityAndTemperatureValueProvider.value(channelWithChildren, ValueType.FIRST) } returns value

    // when
    val valueText = useCase(channelWithChildren)

    // then
    assertThat(valueText).isEqualTo(value)
    verify {
      thermometerValueProvider.handle(channelWithChildren)
      humidityAndTemperatureValueProvider.handle(channelWithChildren)
      humidityAndTemperatureValueProvider.value(channelWithChildren, ValueType.FIRST)
    }
    confirmVerified(
      thermometerValueProvider,
      humidityAndTemperatureValueProvider,
      depthSensorValueProvider,
      generalPurposeMeasurementValueProvider,
      distanceSensorValueStringProvider,
      electricityMeterValueStringProvider,
      switchWithMeterValueStringProvider,
      impulseCounterValueStringProvider,
      pressureSensorValueStringProvider,
      rainSensorValueStringProvider,
      humidityValueStringProvider,
      containerValueStringProvider,
      weightSensorValueStringProvider,
      windSensorValueStringProvider,
      heatpolThermostatValueStringProvider
    )
  }
}
