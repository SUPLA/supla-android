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
import io.mockk.Called
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.ShadingSystemActionParameters
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.VibrationHelper

class ExecuteShadingSystemActionUseCaseTest {

  @MockK
  private lateinit var suplaClientProvider: SuplaClientProvider

  @MockK
  private lateinit var vibrationHelper: VibrationHelper

  @InjectMockKs
  private lateinit var useCase: ExecuteShadingSystemActionUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should execute action and vibrate`() {
    // given
    val actionId = ActionId.TURN_ON
    val type = SubjectType.CHANNEL
    val remoteId = 123
    val percentage = 23

    val suplaClient: SuplaClientApi = mockk()
    every { suplaClient.executeAction(any()) } returns true
    every { suplaClientProvider.provide() } returns suplaClient
    every { vibrationHelper.vibrate() } just Runs

    // when
    val observer = useCase.invoke(actionId, type, remoteId, percentage).test()

    // then
    observer.assertComplete()

    verify {
      suplaClient.executeAction(
        match { parameters ->
          parameters.action == actionId &&
            parameters.subjectType == type &&
            parameters.subjectId == remoteId &&
            (parameters as ShadingSystemActionParameters).percentage.compareTo(23) == 0
        }
      )
      suplaClientProvider.provide()
      vibrationHelper.vibrate()
    }
    confirmVerified(suplaClient, suplaClientProvider, vibrationHelper)
  }

  @Test
  fun `should not vibrate when action is not executed successfully`() {
    // given
    val actionId = ActionId.TURN_ON
    val type = SubjectType.CHANNEL
    val remoteId = 123
    val percentage = 23

    val suplaClient: SuplaClientApi = mockk()
    every { suplaClient.executeAction(any()) } returns false

    every { suplaClientProvider.provide() } returns suplaClient

    // when
    val observer = useCase.invoke(actionId, type, remoteId, percentage).test()

    // then
    observer.assertComplete()

    verify {
      suplaClient.executeAction(
        match { parameters ->
          parameters.action == actionId &&
            parameters.subjectType == type &&
            parameters.subjectId == remoteId &&
            (parameters as ShadingSystemActionParameters).percentage.compareTo(23) == 0
        }
      )
    }
    verify { suplaClientProvider.provide() }
    confirmVerified(suplaClient, suplaClientProvider)
    verify {
      vibrationHelper wasNot Called
    }
  }

  @Test
  fun `should not fail when no supla client is provided`() {
    // given
    val actionId = ActionId.TURN_ON
    val type = SubjectType.CHANNEL
    val remoteId = 123
    val percentage = 23

    every { suplaClientProvider.provide() } returns null

    // when
    val observer = useCase.invoke(actionId, type, remoteId, percentage).test()

    // then
    observer.assertComplete()

    verify { suplaClientProvider.provide() }
    confirmVerified(suplaClientProvider)
    verify {
      vibrationHelper wasNot Called
    }
  }
}
