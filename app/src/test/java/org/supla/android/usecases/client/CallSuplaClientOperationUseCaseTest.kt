package org.supla.android.usecases.client

import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.tools.VibrationHelper

@RunWith(MockitoJUnitRunner::class)
class CallSuplaClientOperationUseCaseTest {

  @Mock
  private lateinit var suplaClientProvider: SuplaClientProvider

  @Mock
  private lateinit var vibrationHelper: VibrationHelper

  @InjectMocks
  private lateinit var useCase: CallSuplaClientOperationUseCase

  @Test
  fun `should invoke recalibrate command and vibrate`() {
    // given
    val remoteId = 123
    val operation = SuplaClientOperation.Command.Recalibrate

    val suplaClient: SuplaClientApi = mockk {
      every { deviceCalCfgRequest(remoteId, false, operation.command, 0, null) } returns true
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val testObserver = useCase.invoke(remoteId, ItemType.CHANNEL, operation).test()

    // then
    testObserver.assertComplete()
    verify(suplaClientProvider).provide()
    verify(vibrationHelper).vibrate()
    io.mockk.verify {
      suplaClient.deviceCalCfgRequest(remoteId, false, operation.command, 0, null)
    }
    verifyNoMoreInteractions(suplaClientProvider, vibrationHelper)
  }

  @Test
  fun `should invoke move up and don't vibrate`() {
    // given
    val remoteId = 123
    val operation = SuplaClientOperation.MoveUp

    val suplaClient: SuplaClientApi = mockk {
      every { open(remoteId, false, 2) } returns false
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val testObserver = useCase.invoke(remoteId, ItemType.CHANNEL, operation).test()

    // then
    testObserver.assertComplete()
    verify(suplaClientProvider).provide()
    io.mockk.verify {
      suplaClient.open(remoteId, false, 2)
    }
    verifyNoMoreInteractions(suplaClientProvider)
    verifyZeroInteractions(vibrationHelper)
  }

  @Test
  fun `should invoke move down and vibrate`() {
    // given
    val remoteId = 123
    val operation = SuplaClientOperation.MoveDown

    val suplaClient: SuplaClientApi = mockk {
      every { open(remoteId, true, 1) } returns true
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val testObserver = useCase.invoke(remoteId, ItemType.GROUP, operation).test()

    // then
    testObserver.assertComplete()
    verify(suplaClientProvider).provide()
    verify(vibrationHelper).vibrate()
    io.mockk.verify {
      suplaClient.open(remoteId, true, 1)
    }
    verifyNoMoreInteractions(suplaClientProvider, vibrationHelper)
  }
}
