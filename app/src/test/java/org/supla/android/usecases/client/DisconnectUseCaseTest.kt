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

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.supla.android.core.SuplaAppApi
import org.supla.android.core.SuplaAppProvider
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.events.UpdateEventsManager

class DisconnectUseCaseTest {
  @MockK
  private lateinit var suplaClientProvider: SuplaClientProvider

  @MockK
  private lateinit var suplaAppProvider: SuplaAppProvider

  @MockK
  private lateinit var updateEventsManager: UpdateEventsManager

  @InjectMockKs
  private lateinit var useCase: DisconnectUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should disconnect when canceled`() {
    // given
    val suplaClient: SuplaClientApi = mockk {
      every { canceled() } returns true
      every { cancel() } answers {}
    }
    every { suplaClientProvider.provide() } returns suplaClient

    val suplaApp: SuplaAppApi = mockk {
      every { CancelAllRestApiClientTasks(true) } answers {}
      every { cleanupToken() } answers {}
    }
    every { suplaAppProvider.provide() } returns suplaApp
    mockUpdateEventsManager()

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    verify {
      suplaClient.canceled()
      suplaClient.cancel()
      suplaApp.CancelAllRestApiClientTasks(true)
      suplaApp.cleanupToken()
      updateEventsManager.cleanup()
      updateEventsManager.emitChannelsUpdate()
      updateEventsManager.emitGroupsUpdate()
      updateEventsManager.emitScenesUpdate()
    }
    confirmVerified(suplaClient, suplaApp, updateEventsManager)
  }

  @Test
  fun `should disconnect when not canceled`() {
    // given
    val suplaClient: SuplaClientApi = mockk {
      every { canceled() } returns false
      every { cancel() } answers {}
      every { join() } answers {}
    }
    every { suplaClientProvider.provide() } returns suplaClient

    val suplaApp: SuplaAppApi = mockk {
      every { CancelAllRestApiClientTasks(true) } answers {}
      every { cleanupToken() } answers {}
    }
    every { suplaAppProvider.provide() } returns suplaApp
    mockUpdateEventsManager()

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    verify {
      suplaClient.canceled()
      suplaClient.cancel()
      suplaClient.join()
      suplaApp.CancelAllRestApiClientTasks(true)
      suplaApp.cleanupToken()
      updateEventsManager.cleanup()
      updateEventsManager.emitChannelsUpdate()
      updateEventsManager.emitGroupsUpdate()
      updateEventsManager.emitScenesUpdate()
    }
  }

  private fun mockUpdateEventsManager() {
    every { updateEventsManager.cleanup() } answers {}
    every { updateEventsManager.emitChannelsUpdate() } answers {}
    every { updateEventsManager.emitGroupsUpdate() } answers {}
    every { updateEventsManager.emitScenesUpdate() } answers {}
  }
}
