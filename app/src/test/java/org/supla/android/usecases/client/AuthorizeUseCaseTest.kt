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
import org.supla.android.R
import org.supla.android.core.infrastructure.ThreadHandler
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientMessageHandlerWrapper
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.lib.SuplaConst

@RunWith(MockitoJUnitRunner::class)
class AuthorizeUseCaseTest {

  @Mock
  private lateinit var suplaClientProvider: SuplaClientProvider

  @Mock
  private lateinit var suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper

  @Mock
  private lateinit var threadHandler: ThreadHandler

  @InjectMocks
  private lateinit var useCase: AuthorizeUseCase

  @Test
  fun `should authorize with timeout`() {
    // given
    val userName = "test@supla.org"
    val password = "password"

    val suplaClient: SuplaClientApi = mockk {
      every { superUserAuthorizationRequest(userName, password) } answers {}
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    var listener: SuplaClientMessageHandler.OnSuplaClientMessageListener? = null
    doAnswer {
      listener = it.arguments[0] as SuplaClientMessageHandler.OnSuplaClientMessageListener
    }.whenever(suplaClientMessageHandlerWrapper).registerMessageListener(any())

    // when
    val observer = useCase.invoke(userName, password).test()

    // then
    observer.assertError(AuthorizationException(R.string.time_exceeded))

    verify(suplaClientProvider).provide()
    verify(suplaClientMessageHandlerWrapper).registerMessageListener(listener!!)
    verify(suplaClientMessageHandlerWrapper).unregisterMessageListener(listener!!)
    verifyNoMoreInteractions(suplaClientProvider, suplaClientMessageHandlerWrapper)
  }

  @Test
  fun `should authorize with success`() {
    // given
    val userName = "test@supla.org"
    val password = "password"

    val suplaClient: SuplaClientApi = mockk {
      every { superUserAuthorizationRequest(userName, password) } answers {}
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    val message: SuplaClientMsg = mockk {
      every { type } returns SuplaClientMsg.onSuperuserAuthorizationResult
      every { isSuccess } returns true
    }
    var listener: SuplaClientMessageHandler.OnSuplaClientMessageListener? = null
    doAnswer {
      listener = it.arguments[0] as SuplaClientMessageHandler.OnSuplaClientMessageListener
      listener?.onSuplaClientMessageReceived(message)
    }.whenever(suplaClientMessageHandlerWrapper).registerMessageListener(any())

    // when
    val observer = useCase.invoke(userName, password).test()

    // then
    observer.assertComplete()
    verify(suplaClientProvider).provide()
    verify(suplaClientMessageHandlerWrapper).registerMessageListener(listener!!)
    verify(suplaClientMessageHandlerWrapper, times(2)).unregisterMessageListener(listener!!)
    verifyNoMoreInteractions(suplaClientProvider, suplaClientMessageHandlerWrapper)
  }

  @Test
  fun `should authorize with unauthorized error`() {
    val message: SuplaClientMsg = mockk {
      every { type } returns SuplaClientMsg.onSuperuserAuthorizationResult
      every { isSuccess } returns false
      every { code } returns SuplaConst.SUPLA_RESULTCODE_UNAUTHORIZED
    }

    doAuthorizationTestWithError(message, R.string.incorrect_email_or_password)
  }

  @Test
  fun `should authorize with temporarily unavailable error`() {
    val message: SuplaClientMsg = mockk {
      every { type } returns SuplaClientMsg.onSuperuserAuthorizationResult
      every { isSuccess } returns false
      every { code } returns SuplaConst.SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE
    }

    doAuthorizationTestWithError(message, R.string.status_temporarily_unavailable)
  }

  @Test
  fun `should authorize with unknown error`() {
    val message: SuplaClientMsg = mockk {
      every { type } returns SuplaClientMsg.onSuperuserAuthorizationResult
      every { isSuccess } returns false
      every { code } returns 0
    }

    doAuthorizationTestWithError(message, R.string.status_unknown_err)
  }

  private fun doAuthorizationTestWithError(message: SuplaClientMsg, errorMessage: Int) {
    // given
    val userName = "test@supla.org"
    val password = "password"

    val suplaClient: SuplaClientApi = mockk {
      every { superUserAuthorizationRequest(userName, password) } answers {}
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    var listener: SuplaClientMessageHandler.OnSuplaClientMessageListener? = null
    doAnswer {
      listener = it.arguments[0] as SuplaClientMessageHandler.OnSuplaClientMessageListener
      listener?.onSuplaClientMessageReceived(message)
    }.whenever(suplaClientMessageHandlerWrapper).registerMessageListener(any())

    // when
    val observer = useCase.invoke(userName, password).test()

    // then
    observer.assertError(AuthorizationException(errorMessage))

    verify(suplaClientProvider).provide()
    verify(suplaClientMessageHandlerWrapper).registerMessageListener(listener!!)
    verify(suplaClientMessageHandlerWrapper, times(2)).unregisterMessageListener(listener!!)
    verifyNoMoreInteractions(suplaClientProvider, suplaClientMessageHandlerWrapper)
  }
}
