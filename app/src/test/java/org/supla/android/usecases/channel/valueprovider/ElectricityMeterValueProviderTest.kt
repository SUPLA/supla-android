package org.supla.android.usecases.channel.valueprovider
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
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.model.settings.ListValueAggregation
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterBalanceType
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterMeasurementType
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterSettings
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.lib.SuplaChannelElectricityMeterValue
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.usecases.channel.ValueType
import org.supla.core.shared.data.model.general.SuplaFunction

class ElectricityMeterValueProviderTest {

  @MockK
  private lateinit var userStateHolder: UserStateHolder

  @InjectMockKs
  private lateinit var provider: ElectricityMeterValueProvider

  private val profileId = 1L
  private val remoteId = 100

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should handle electricity meter channel`() {
    // given
    val channelWithChildren = mockk<ChannelWithChildren> {
      every { function } returns SuplaFunction.ELECTRICITY_METER
    }

    // when
    val result = provider.handle(channelWithChildren)

    // then
    assertThat(result).isTrue()
  }

  @Test
  fun `should provide reverse active energy from summary for list value`() {
    // given
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.REVERSE_ACTIVE_ENERGY
    )
    val channelWithChildren = channelWithChildren(
      electricityValue = electricityValue(
        measuredValues = SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY.rawValue,
        summaries = listOf(
          summary(1.0, 10.0, 0.0, 0.0),
          summary(2.0, 20.0, 0.0, 0.0),
          summary(3.0, 30.0, 0.0, 0.0)
        )
      )
    )

    // when
    val result = provider.value(channelWithChildren, ValueType.List()) as Double

    // then
    assertThat(result).isEqualTo(60.0)
  }

  @Test
  fun `should provide forward reactive energy from summary for list value`() {
    // given
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.FORWARD_REACTIVE_ENERGY
    )
    val channelWithChildren = channelWithChildren(
      electricityValue = electricityValue(
        measuredValues = SuplaElectricityMeasurementType.FORWARD_REACTIVE_ENERGY.rawValue,
        summaries = listOf(
          summary(0.0, 0.0, 1.5, 0.0),
          summary(0.0, 0.0, 2.0, 0.0),
          summary(0.0, 0.0, 3.5, 0.0)
        )
      )
    )

    // when
    val result = provider.value(channelWithChildren, ValueType.List()) as Double

    // then
    assertThat(result).isEqualTo(7.0)
  }

  @Test
  fun `should provide reverse reactive energy from summary for list value`() {
    // given
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.REVERSE_REACTIVE_ENERGY
    )
    val channelWithChildren = channelWithChildren(
      electricityValue = electricityValue(
        measuredValues = SuplaElectricityMeasurementType.REVERSE_REACTIVE_ENERGY.rawValue,
        summaries = listOf(
          summary(0.0, 0.0, 0.0, 1.2),
          summary(0.0, 0.0, 0.0, 2.3),
          summary(0.0, 0.0, 0.0, 3.8)
        )
      )
    )

    // when
    val result = provider.value(channelWithChildren, ValueType.List()) as Double

    // then
    assertThat(result).isEqualTo(7.3)
  }

  @Test
  fun `should sum active power from enabled phases`() {
    // given
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.POWER_ACTIVE
    )
    val channelWithChildren = channelWithChildren(
      flags = SuplaChannelFlag.PHASE2_UNSUPPORTED.rawValue,
      electricityValue = electricityValue(
        measuredValues = SuplaElectricityMeasurementType.POWER_ACTIVE.rawValue,
        measurements = listOf(
          measurement(voltage = 230.0, current = 1.0, powerActive = 1.5),
          measurement(voltage = 240.0, current = 2.0, powerActive = 2.5),
          measurement(voltage = 250.0, current = 3.0, powerActive = 3.0)
        )
      )
    )

    // when
    val result = provider.value(channelWithChildren, ValueType.List()) as Double

    // then
    assertThat(result).isEqualTo(4.5)
  }

  @Test
  fun `should sum active power from enabled phases and convert kw to w`() {
    // given
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.POWER_ACTIVE
    )
    val channelWithChildren = channelWithChildren(
      flags = SuplaChannelFlag.PHASE2_UNSUPPORTED.rawValue,
      electricityValue = electricityValue(
        measuredValues = SuplaElectricityMeasurementType.POWER_ACTIVE_KW.rawValue,
        measurements = listOf(
          measurement(voltage = 230.0, current = 1.0, powerActive = 1.5),
          measurement(voltage = 240.0, current = 2.0, powerActive = 2.5),
          measurement(voltage = 250.0, current = 3.0, powerActive = 3.0)
        )
      )
    )

    // when
    val result = provider.value(channelWithChildren, ValueType.List()) as Double

    // then
    assertThat(result).isEqualTo(4500.0)
  }

  @Test
  fun `should average voltage and sum current only from enabled phases`() {
    // given
    val voltageSettings = settings(metricOnList = ElectricityMeterMeasurementType.VOLTAGE)
    val currentSettings = settings(metricOnList = ElectricityMeterMeasurementType.CURRENT)
    val channelWithChildren = channelWithChildren(
      flags = SuplaChannelFlag.PHASE2_UNSUPPORTED.rawValue,
      electricityValue = electricityValue(
        measuredValues = SuplaElectricityMeasurementType.VOLTAGE.rawValue or SuplaElectricityMeasurementType.CURRENT.rawValue,
        measurements = listOf(
          measurement(voltage = 230.0, current = 1.25, powerActive = 1.0),
          measurement(voltage = 240.0, current = 2.25, powerActive = 2.0),
          measurement(voltage = 250.0, current = 3.5, powerActive = 3.0)
        )
      )
    )

    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns voltageSettings

    // when
    val voltage = provider.value(channelWithChildren, ValueType.List()) as Double

    // then
    assertThat(voltage).isEqualTo(240.0)

    // given
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns currentSettings

    // when
    val current = provider.value(channelWithChildren, ValueType.List()) as Double

    // then
    assertThat(current).isEqualTo(4.75)
  }

  @Test
  fun `should fall back to default parsed value for forward active energy list metric`() {
    // given
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.FORWARD_ACTIVE_ENERGY
    )
    val channelWithChildren = channelWithChildren(
      electricityValue = electricityValue(
        measuredValues = SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY.rawValue,
        summaries = listOf(
          summary(1.0, 0.0, 0.0, 0.0),
          summary(0.5, 0.0, 0.0, 0.0),
          summary(2.0, 0.0, 0.0, 0.0)
        )
      ),
    )

    // when
    val result = provider.value(channelWithChildren, ValueType.List()) as Double

    // then
    assertThat(result).isEqualTo(3.5)
  }

  @Test
  fun `should calculate forward active energy balanced`() {
    // given
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.FORWARD_ACTIVE_ENERGY,
      metricOnListBalanceType = ElectricityMeterBalanceType.VECTOR
    )
    val channelWithChildren = channelWithChildren(
      electricityValue = electricityValue(
        measuredValues = SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY.rawValue,
        totalForwardActiveEnergyBalanced = 1500000
      ),
    )

    // when
    val result = provider.value(channelWithChildren, ValueType.List()) as Double

    // then
    assertThat(result).isEqualTo(15.0)
  }

  @Test
  fun `should calculate reverse active energy balanced`() {
    // given
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.REVERSE_ACTIVE_ENERGY,
      metricOnListBalanceType = ElectricityMeterBalanceType.VECTOR
    )
    val channelWithChildren = channelWithChildren(
      electricityValue = electricityValue(
        measuredValues = SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY.rawValue,
        totalReverseActiveEnergyBalanced = 1600000
      ),
    )

    // when
    val result = provider.value(channelWithChildren, ValueType.List()) as Double

    // then
    assertThat(result).isEqualTo(16.0)
  }

  private fun settings(
    metricOnList: ElectricityMeterMeasurementType,
    metricOnListBalanceType: ElectricityMeterBalanceType = ElectricityMeterBalanceType.ARITHMETIC
  ) =
    ElectricityMeterSettings(
      currentMonthBalancing = ElectricityMeterBalanceType.DEFAULT,
      metricOnList = metricOnList,
      metricOnListBalancing = metricOnListBalanceType,
      metricOnListAggregation = ListValueAggregation.NO_AGGREGATION
    )

  private fun channelWithChildren(
    flags: Long = 0L,
    electricityValue: SuplaChannelElectricityMeterValue,
    channelValueEntity: ChannelValueEntity = channelValueEntity()
  ): ChannelWithChildren {
    val extendedValueEntity = mockk<ChannelExtendedValueEntity> {
      every { getSuplaValue() } returns SuplaChannelExtendedValue().apply {
        ElectricityMeterValue = electricityValue
      }
    }
    val channelData = mockk<ChannelDataEntity> {
      every { this@mockk.channelValueEntity } returns channelValueEntity
      every { this@mockk.channelExtendedValueEntity } returns extendedValueEntity
      every { this@mockk.flags } returns flags
    }

    return mockk {
      every { this@mockk.profileId } returns this@ElectricityMeterValueProviderTest.profileId
      every { this@mockk.remoteId } returns this@ElectricityMeterValueProviderTest.remoteId
      every { this@mockk.channel } returns channelData
      every { this@mockk.flags } returns flags
    }
  }

  private fun channelValueEntity(valueBytes: ByteArray = byteArrayOf()): ChannelValueEntity =
    mockk {
      every { getValueAsByteArray() } returns valueBytes
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
    }

  private fun electricityValue(
    measuredValues: Int,
    summaries: List<SummarySeed> = emptyList(),
    measurements: List<MeasurementSeed> = emptyList(),
    totalForwardActiveEnergyBalanced: Long = 0,
    totalReverseActiveEnergyBalanced: Long = 0
  ): SuplaChannelElectricityMeterValue =
    SuplaChannelElectricityMeterValue(
      measuredValues = measuredValues,
      voltagePhaseAngle12 = 0,
      voltagePhaseAngle13 = 0,
      phaseSequence = 0,
      period = 0,
      totalCost = 0,
      pricePerUnit = 0,
      currency = "",
      totalForwardActiveEnergyBalanced = totalForwardActiveEnergyBalanced,
      totalReverseActiveEnergyBalanced = totalReverseActiveEnergyBalanced
    ).apply {
      summaries.forEachIndexed { index, summary ->
        addSummary(index + 1, Summary(summary.fae, summary.rae, summary.fre, summary.rre))
      }
      measurements.forEachIndexed { index, measurement ->
        addMeasurement(
          index + 1,
          Measurement(
            frequency = 50.0,
            voltage = measurement.voltage,
            current = measurement.current,
            powerActive = measurement.powerActive,
            powerReactive = 0.0,
            powerApparent = 0.0,
            powerFactor = 0.0,
            phaseAngle = 0.0
          )
        )
      }
    }

  private fun summary(fae: Double, rae: Double, fre: Double, rre: Double) = SummarySeed(fae, rae, fre, rre)

  private fun measurement(voltage: Double, current: Double, powerActive: Double) =
    MeasurementSeed(voltage, current, powerActive)

  private data class SummarySeed(
    val fae: Double,
    val rae: Double,
    val fre: Double,
    val rre: Double
  )

  private data class MeasurementSeed(
    val voltage: Double,
    val current: Double,
    val powerActive: Double
  )
}
