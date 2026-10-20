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

import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.tools.VibrationHelper

class CallSuplaClientOperationUseCaseTest {

  @MockK
  private lateinit var suplaClientProvider: SuplaClientProvider

  @MockK
  private lateinit var vibrationHelper: VibrationHelper

  @InjectMockKs
  private lateinit var useCase: CallSuplaClientOperationUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should invoke recalibrate command and vibrate`() {
    // given
    val remoteId = 123
    val operation = SuplaClientOperation.Command.Recalibrate

    val suplaClient: SuplaClientApi = mockk {
      every { deviceCalCfgRequest(remoteId, false, operation.command.value, 0, null) } returns true
    }
    every { suplaClientProvider.provide() } returns suplaClient
    every { vibrationHelper.vibrate() } just Runs

    // when
    val testObserver = useCase.invoke(remoteId, ItemType.CHANNEL, operation).test()

    // then
    testObserver.assertComplete()
    verify {
      suplaClientProvider.provide()
      vibrationHelper.vibrate()
      suplaClient.deviceCalCfgRequest(remoteId, false, operation.command.value, 0, null)
    }
    confirmVerified(suplaClientProvider, vibrationHelper)
  }

  @Test
  fun `should invoke move up and don't vibrate`() {
    // given
    val remoteId = 123
    val operation = SuplaClientOperation.MoveUp

    val suplaClient: SuplaClientApi = mockk {
      every { open(remoteId, false, 2) } returns false
    }
    every { suplaClientProvider.provide() } returns suplaClient

    // when
    val testObserver = useCase.invoke(remoteId, ItemType.CHANNEL, operation).test()

    // then
    testObserver.assertComplete()
    verify { suplaClientProvider.provide() }
    io.mockk.verify {
      suplaClient.open(remoteId, false, 2)
    }
    confirmVerified(suplaClientProvider)
    verify {
      vibrationHelper wasNot Called
    }
  }

  @Test
  fun `should invoke move down and vibrate`() {
    // given
    val remoteId = 123
    val operation = SuplaClientOperation.MoveDown

    val suplaClient: SuplaClientApi = mockk {
      every { open(remoteId, true, 1) } returns true
    }
    every { suplaClientProvider.provide() } returns suplaClient
    every { vibrationHelper.vibrate() } just Runs

    // when
    val testObserver = useCase.invoke(remoteId, ItemType.GROUP, operation).test()

    // then
    testObserver.assertComplete()
    verify {
      suplaClientProvider.provide()
      vibrationHelper.vibrate()
      suplaClient.open(remoteId, true, 1)
    }
    confirmVerified(suplaClientProvider, vibrationHelper)
  }
}
