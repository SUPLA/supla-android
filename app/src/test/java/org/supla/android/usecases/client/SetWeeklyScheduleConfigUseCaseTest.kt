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

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleEntry
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram

@RunWith(MockitoJUnitRunner::class)
class SetWeeklyScheduleConfigUseCaseTest {

  @Mock
  lateinit var suplaClientProvider: SuplaClientProvider

  @InjectMocks
  lateinit var useCase: SetWeeklyScheduleConfigUseCase

  @Test
  fun `should set channel config`() {
    // given
    val remoteId = 123
    val configurations = listOf(mockk<SuplaWeeklyScheduleProgram>())
    val schedule = listOf(mockk<SuplaWeeklyScheduleEntry>())

    val argumentSlot = slot<SuplaChannelWeeklyScheduleConfig>()
    val suplaClient = mockk<SuplaClientApi>()
    every { suplaClient.setChannelConfig(capture(argumentSlot)) } returns true

    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val observer = useCase(remoteId, configurations, schedule).test()

    // then
    observer.assertComplete()

    assertThat(argumentSlot.captured.remoteId).isEqualTo(remoteId)
    assertThat(argumentSlot.captured.programConfigurations).isEqualTo(configurations)
    assertThat(argumentSlot.captured.schedule).isEqualTo(schedule)
    assertThat(argumentSlot.captured.func).isNull()
  }
}
