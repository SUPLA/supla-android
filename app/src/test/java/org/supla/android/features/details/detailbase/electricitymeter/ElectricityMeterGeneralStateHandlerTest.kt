package org.supla.android.features.details.detailbase.electricitymeter
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
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.data.source.remote.electricitymeter.ElectricityMeterPhaseSequence
import org.supla.android.lib.SuplaChannelElectricityMeterValue
import org.supla.android.lib.SuplaChannelElectricityMeterValue.Measurement
import org.supla.android.lib.SuplaChannelElectricityMeterValue.Summary
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.ui.views.card.SummaryCardData
import org.supla.android.usecases.channel.measurements.ElectricityMeasurements

class ElectricityMeterGeneralStateHandlerTest {
  @MockK
  private lateinit var noExtendedValueStateHandler: NoExtendedValueStateHandler

  @MockK
  private lateinit var preferences: Preferences

  @InjectMockKs
  private lateinit var handler: ElectricityMeterGeneralStateHandler

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should try create state when no extended value available`() {
    // given
    val channel: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { channelExtendedValueEntity } returns null
      }
    }
    val state: ElectricityMeterState = mockk()

    every { noExtendedValueStateHandler.updateState(state, channel) } returns state

    // when
    val result = handler.updateState(state, channel)

    // then
    assertThat(result).isSameAs(state)
  }

  @Test
  fun `should update state - total forward energy`() {
    // given
    val electricityMeterValue: SuplaChannelElectricityMeterValue = mockk {
      every { measuredValues } returns (0x100 or 7)
      every { summary } returns mockk {
        every { totalForwardActiveEnergy } returns 100.0
      }
      every { pricePerUnit } returns 2.0
      every { currency } returns "PLN"
      every { getMeasurement(1, 0) } returns mockMeasurement(1)
      every { getMeasurement(2, 0) } returns mockMeasurement(2)
      every { getMeasurement(3, 0) } returns mockMeasurement(3)
      every { getSummary(1) } returns mockSummary(1)
      every { getSummary(2) } returns mockSummary(2)
      every { getSummary(3) } returns mockSummary(3)
    }
    val channel: ChannelDataEntity = mockk {
      every { channelExtendedValueEntity } returns mockk {
        every { getSuplaValue() } returns mockSuplaChannelExtendedValue(electricityMeterValue)
      }
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
      every { flags } returns 0
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
    }
    val state = ElectricityMeterState()

    every { preferences.shouldShowEmGeneralIntroduction() } returns true

    // when
    val result = handler.updateState(state, channelWithChildren)

    // then
    assertThat(result).isEqualTo(
      state.copy(
        online = true,
        totalForwardActiveEnergy = SummaryCardData("100.0 kWh", "200.00 PLN"),
        phaseMeasurementTypes = listOf(
          SuplaElectricityMeasurementType.VOLTAGE,
          SuplaElectricityMeasurementType.CURRENT,
          SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY
        ),
        phaseMeasurementValues = listOf(
          phaseWithMeasurements(R.string.em_chart_all_phases, "11 - 31 ", forwardActiveEnergy = "600.00000 "),
          phaseWithMeasurements(R.string.details_em_phase1, "11.00 ", "12.00 ", "100.00000 "),
          phaseWithMeasurements(R.string.details_em_phase2, "21.00 ", "22.00 ", "200.00000 "),
          phaseWithMeasurements(R.string.details_em_phase3, "31.00 ", "32.00 ", "300.00000 ")
        ),
        electricGridParameters = mapOf(
          SuplaElectricityMeasurementType.FREQUENCY to "10.00 "
        ),
        showIntroduction = true
      )
    )
  }

  @Test
  fun `should update state - total reverse energy`() {
    // given
    val electricityMeterValue: SuplaChannelElectricityMeterValue = mockk {
      every { measuredValues } returns (0x200 or 0x100 or 0x10000 or 0x20000)
      every { summary } returns mockk {
        every { totalForwardActiveEnergy } returns 98.0
        every { totalReverseActiveEnergy } returns 100.0
      }
      every { pricePerUnit } returns 10.0
      every { currency } returns "PLN"
      every { getMeasurement(1, 0) } returns mockMeasurement(1)
      every { getSummary(1) } returns mockSummary(1)
      every { voltagePhaseAngle12 } returns 123
      every { voltagePhaseAngle13 } returns 234
    }
    val channel: ChannelDataEntity = mockk {
      every { channelExtendedValueEntity } returns mockk {
        every { getSuplaValue() } returns mockSuplaChannelExtendedValue(electricityMeterValue)
      }
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
      every { flags } returns (SuplaChannelFlag.PHASE2_UNSUPPORTED.rawValue or SuplaChannelFlag.PHASE3_UNSUPPORTED.rawValue)
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
    }
    val measurements = ElectricityMeasurements(21f, 22f)
    val state = ElectricityMeterState()

    every { preferences.shouldShowEmGeneralIntroduction() } returns false

    // when
    val result = handler.updateState(state, channelWithChildren, measurements)

    // then
    assertThat(result).isEqualTo(
      state.copy(
        online = true,
        totalForwardActiveEnergy = SummaryCardData("98.00 kWh", "980.00 PLN"),
        totalReversedActiveEnergy = SummaryCardData("100.0 kWh"),
        currentMonthForwardActiveEnergy = SummaryCardData("21.00 kWh", "210.00 PLN"),
        currentMonthReversedActiveEnergy = SummaryCardData("22.00 kWh"),
        phaseMeasurementTypes = listOf(
          SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY,
          SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY
        ),
        phaseMeasurementValues = listOf(
          phaseWithMeasurements(R.string.details_em_phase1, forwardActiveEnergy = "100.00000 ", reverseActiveEnergy = "101.00000 "),
        ),
        electricGridParameters = mapOf(
          SuplaElectricityMeasurementType.VOLTAGE_PHASE_ANGLE_12 to "12.3 ",
          SuplaElectricityMeasurementType.VOLTAGE_PHASE_ANGLE_13 to "23.4 "
        )
      )
    )
  }

  @Test
  fun `should update state - total reverse energy without price`() {
    // given
    val electricityMeterValue: SuplaChannelElectricityMeterValue = mockk {
      every { measuredValues } returns (0x4000 or 0x2000 or 0x200 or 0x100 or 0x40000 or 0x80000)
      every { summary } returns mockk {
        every { totalForwardActiveEnergy } returns 98.0
        every { totalReverseActiveEnergy } returns 100.0
      }
      every { pricePerUnit } returns 0.0
      every { currency } returns "PLN"
      every { getMeasurement(1, 0) } returns mockMeasurement(1)
      every { getMeasurement(2, 0) } returns mockMeasurement(2)
      every { getSummary(1) } returns mockSummary(1)
      every { getSummary(2) } returns mockSummary(2)
      every { totalForwardActiveEnergyBalanced } returns 111.1
      every { totalReverseActiveEnergyBalanced } returns 222.2
      every { voltagePhaseSequence } returns ElectricityMeterPhaseSequence.CLOCKWISE
      every { currentPhaseSequence } returns ElectricityMeterPhaseSequence.COUNTER_CLOCKWISE
    }
    val channel: ChannelDataEntity = mockk {
      every { channelExtendedValueEntity } returns mockk {
        every { getSuplaValue() } returns mockSuplaChannelExtendedValue(electricityMeterValue)
      }
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
      every { flags } returns SuplaChannelFlag.PHASE3_UNSUPPORTED.rawValue
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
    }
    val measurements = ElectricityMeasurements(21f, 22f)
    val state = ElectricityMeterState()

    every { preferences.shouldShowEmGeneralIntroduction() } returns false

    // when
    val result = handler.updateState(state, channelWithChildren, measurements)

    // then
    assertThat(result).isEqualTo(
      state.copy(
        online = true,
        totalForwardActiveEnergy = SummaryCardData("98.00 kWh"),
        totalReversedActiveEnergy = SummaryCardData("100.0 kWh"),
        currentMonthForwardActiveEnergy = SummaryCardData("21.00 kWh"),
        currentMonthReversedActiveEnergy = SummaryCardData("22.00 kWh"),
        phaseMeasurementTypes = listOf(
          SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY,
          SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY
        ),
        phaseMeasurementValues = listOf(
          phaseWithMeasurements(R.string.em_chart_all_phases, forwardActiveEnergy = "300.00000 ", reverseActiveEnergy = "302.00000 "),
          phaseWithMeasurements(R.string.details_em_phase1, forwardActiveEnergy = "100.00000 ", reverseActiveEnergy = "101.00000 "),
          phaseWithMeasurements(R.string.details_em_phase2, forwardActiveEnergy = "200.00000 ", reverseActiveEnergy = "201.00000 "),
        ),
        vectorBalancedValues = mapOf(
          SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY_BALANCED to "111.1 ",
          SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY_BALANCED to "222.2 "
        ),
        electricGridParameters = mapOf(
          SuplaElectricityMeasurementType.VOLTAGE_PHASE_SEQUENCE to "1-2-3",
          SuplaElectricityMeasurementType.CURRENT_PHASE_SEQUENCE to "1-3-2"
        )
      )
    )
  }

  private fun mockMeasurement(phase: Int): Measurement =
    mockk {
      every { frequency } returns phase * 10.0
      every { voltage } returns phase * 10.0 + 1
      every { current } returns phase * 10.0 + 2
    }

  private fun mockSummary(phase: Int): Summary =
    mockk {
      every { totalForwardActiveEnergy } returns phase * 100.0
      every { totalReverseActiveEnergy } returns phase * 100.0 + 1
      every { totalForwardReactiveEnergy } returns phase * 100.0 + 2
      every { totalReverseReactiveEnergy } returns phase * 100.0 + 3
    }

  private fun mockSuplaChannelExtendedValue(
    electricityMeterValue: SuplaChannelElectricityMeterValue
  ): SuplaChannelExtendedValue =
    SuplaChannelExtendedValue().apply {
      ElectricityMeterValue = electricityMeterValue
    }

  private fun phaseWithMeasurements(
    phaseLabel: Int,
    voltage: String? = null,
    current: String? = null,
    forwardActiveEnergy: String? = null,
    reverseActiveEnergy: String? = null
  ) =
    PhaseWithMeasurements(
      phase = phaseLabel,
      values = mutableMapOf<SuplaElectricityMeasurementType, String>().apply {
        voltage?.let { put(SuplaElectricityMeasurementType.VOLTAGE, it) }
        current?.let { put(SuplaElectricityMeasurementType.CURRENT, it) }
        forwardActiveEnergy?.let { put(SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY, it) }
        reverseActiveEnergy?.let { put(SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY, it) }
      }
    )
}
