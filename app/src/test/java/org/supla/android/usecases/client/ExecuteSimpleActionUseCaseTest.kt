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

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.VibrationHelper

@RunWith(MockitoJUnitRunner::class)
class ExecuteSimpleActionUseCaseTest {

  @Mock
  private lateinit var suplaClientProvider: SuplaClientProvider

  @Mock
  private lateinit var vibrationHelper: VibrationHelper

  @InjectMocks
  private lateinit var useCase: ExecuteSimpleActionUseCase

  @Test
  fun `should execute action and vibrate`() {
    // given
    val actionId = ActionId.TURN_ON
    val type = SubjectType.CHANNEL
    val remoteId = 123

    val suplaClient: SuplaClientApi = mock()
    whenever(suplaClient.executeAction(any())).thenReturn(true)

    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val observer = useCase.invoke(actionId, type, remoteId).test()

    // then
    observer.assertComplete()

    verify(suplaClient).executeAction(
      argThat { parameters ->
        parameters.action == actionId && parameters.subjectType == type && parameters.subjectId == remoteId
      }
    )
    verify(suplaClientProvider).provide()
    verify(vibrationHelper).vibrate()
    verifyNoMoreInteractions(suplaClient, suplaClientProvider, vibrationHelper)
  }

  @Test
  fun `should not vibrate when action is not executed successfully`() {
    // given
    val actionId = ActionId.TURN_ON
    val type = SubjectType.CHANNEL
    val remoteId = 123

    val suplaClient: SuplaClientApi = mock()
    whenever(suplaClient.executeAction(any())).thenReturn(false)

    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val observer = useCase.invoke(actionId, type, remoteId).test()

    // then
    observer.assertComplete()

    verify(suplaClient).executeAction(
      argThat { parameters ->
        parameters.action == actionId && parameters.subjectType == type && parameters.subjectId == remoteId
      }
    )
    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(suplaClient, suplaClientProvider)
    verifyNoInteractions(vibrationHelper)
  }

  @Test
  fun `should not fail when no supla client is provided`() {
    // given
    val actionId = ActionId.TURN_ON
    val type = SubjectType.CHANNEL
    val remoteId = 123

    whenever(suplaClientProvider.provide()).thenReturn(null)

    // when
    val observer = useCase.invoke(actionId, type, remoteId).test()

    // then
    observer.assertComplete()

    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(suplaClientProvider)
    verifyNoInteractions(vibrationHelper)
  }
}
