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

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.SuplaAppApi
import org.supla.android.core.SuplaAppProvider
import org.supla.android.core.infrastructure.ThreadHandler
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientMessageHandlerWrapper
import org.supla.core.shared.data.model.suplaclient.SuplaResultCode
import org.supla.core.shared.infrastructure.LocalizedStringId
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessageHandler

@RunWith(MockitoJUnitRunner::class)
class LoginUseCaseTest {
  @Mock
  private lateinit var context: Context

  @Mock
  private lateinit var suplaAppProvider: SuplaAppProvider

  @Mock
  private lateinit var suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper

  @Mock
  private lateinit var threadHandler: ThreadHandler

  @InjectMocks
  private lateinit var useCase: LoginUseCase

  @Test
  fun `should login with success`() {
    // given
    val userName = "test@supla.org"
    val password = "password"

    val suplaClient: SuplaClientApi = mockk {
      every { superUserAuthorizationRequest(userName, password) } answers {}
    }
    val suplaApp: SuplaAppApi = mockk {
      every { SuplaClientInitIfNeed(context, password) } returns suplaClient
    }
    whenever(suplaAppProvider.provide()).thenReturn(suplaApp)

    val message = SuplaClientMessage.ClientRegistered
    var listener: SuplaClientMessageHandler.Listener? = null
    doAnswer {
      listener = it.arguments[0] as SuplaClientMessageHandler.Listener
      listener.onReceived(message)
    }.whenever(suplaClientMessageHandlerWrapper).registerMessageListener(any())

    // when
    val observer = useCase.invoke(userName, password).test()

    // then
    observer.assertComplete()
    verify(suplaAppProvider).provide()
    verify(suplaClientMessageHandlerWrapper).registerMessageListener(listener!!)
    verify(suplaClientMessageHandlerWrapper, times(2)).unregisterMessageListener(listener)
    verifyNoMoreInteractions(suplaAppProvider, suplaClientMessageHandlerWrapper)
  }

  @Test
  fun `should login with error`() {
    // given
    val userName = "test@supla.org"
    val password = "password"

    val suplaClient: SuplaClientApi = mockk {
      every { superUserAuthorizationRequest(userName, password) } answers {}
    }
    val suplaApp: SuplaAppApi = mockk {
      every { SuplaClientInitIfNeed(context, password) } returns suplaClient
    }
    whenever(suplaAppProvider.provide()).thenReturn(suplaApp)

    val message = SuplaClientMessage.ClientRegistrationError(SuplaResultCode.CLIENT_LIMIT_EXCEEDED)
    var listener: SuplaClientMessageHandler.Listener? = null
    doAnswer {
      listener = it.arguments[0] as SuplaClientMessageHandler.Listener
      listener.onReceived(message)
    }.whenever(suplaClientMessageHandlerWrapper).registerMessageListener(any())

    // when
    val observer = useCase.invoke(userName, password).test()

    // then
    observer.assertError(AuthorizationException.WithLocalizedString(localizedString(LocalizedStringId.RESULT_CODE_CLIENT_LIMIT_EXCEEDED)))

    verify(suplaAppProvider).provide()
    verify(suplaClientMessageHandlerWrapper).registerMessageListener(listener!!)
    verify(suplaClientMessageHandlerWrapper, times(2)).unregisterMessageListener(listener)
    verifyNoMoreInteractions(suplaAppProvider, suplaClientMessageHandlerWrapper)
  }
}
