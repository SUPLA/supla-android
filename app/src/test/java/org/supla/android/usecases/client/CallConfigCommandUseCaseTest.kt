package org.supla.android.usecases.client

import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.runtime.ItemType

@RunWith(MockitoJUnitRunner::class)
class CallConfigCommandUseCaseTest {

  @Mock
  private lateinit var suplaClientProvider: SuplaClientProvider

  @InjectMocks
  private lateinit var useCase: CallConfigCommandUseCase

  @Test
  fun `should call supla client`() {
    // given
    val remoteId = 123
    val command = SuplaConfigCommand.RECALIBRATE

    val suplaClient: SuplaClientApi = mockk {
      every { deviceCalCfgRequest(remoteId, false, command.value, 0, null) } returns true
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val testObserver = useCase.invoke(remoteId, ItemType.CHANNEL, command).test()

    // then
    testObserver.assertComplete()
    verify(suplaClientProvider).provide()
    io.mockk.verify {
      suplaClient.deviceCalCfgRequest(remoteId, false, command.value, 0, null)
    }
  }
}
