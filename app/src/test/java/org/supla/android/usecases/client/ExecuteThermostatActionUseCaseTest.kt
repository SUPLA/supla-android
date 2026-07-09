package org.supla.android.usecases.client
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

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.HvacActionParameters
import org.supla.android.lib.actions.SubjectType

class ExecuteThermostatActionUseCaseTest {

  @MockK
  lateinit var suplaClientProvider: SuplaClientProvider

  @InjectMockKs
  lateinit var useCase: ExecuteThermostatActionUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should invoke thermostat action`() {
    // given
    val type = SubjectType.CHANNEL
    val remoteId = 123
    val mode = SuplaHvacMode.HEAT
    val setpointTemperatureHeat = 12f
    val setpointTemperatureCool = 21f
    val durationInSec = 231L

    val argumentSlot = slot<HvacActionParameters>()
    val suplaClient = mockk<SuplaClientApi>()
    every { suplaClient.executeAction(capture(argumentSlot)) } returns true

    every { suplaClientProvider.provide() } returns suplaClient

    // when
    val observer = useCase.invoke(type, remoteId, mode, setpointTemperatureHeat, setpointTemperatureCool, durationInSec).test()

    // then
    observer.assertComplete()

    assertThat(argumentSlot.captured.action).isEqualTo(ActionId.SET_HVAC_PARAMETERS)
    assertThat(argumentSlot.captured.subjectType).isEqualTo(type)
    assertThat(argumentSlot.captured.subjectId).isEqualTo(remoteId)
    assertThat(argumentSlot.captured.mode).isEqualTo(mode)
    assertThat(argumentSlot.captured.setpointTemperatureHeat).isEqualTo(1200)
    assertThat(argumentSlot.captured.setpointTemperatureCool).isEqualTo(2100)
    assertThat(argumentSlot.captured.durationSec).isEqualTo(durationInSec)
  }
}
