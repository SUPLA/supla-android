package org.supla.android.usecases.channel

import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.supla.android.events.UpdateEventsManager
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMessageHandler.OnSuplaClientMessageListener
import org.supla.android.lib.SuplaClientMsg

@RunWith(MockitoJUnitRunner::class)
class ChannelGroupStateObserverUseCaseTest {
  @Mock
  private lateinit var updateEventsManager: UpdateEventsManager

  @Mock
  private lateinit var messageHandler: SuplaClientMessageHandler

  @InjectMocks
  private lateinit var usecase: ChannelGroupStateObserverUseCase

  @Test
  fun `should register and emit group changes`() {
    // given
    val channelGroupId = 123
    var listener: OnSuplaClientMessageListener? = null

    doAnswer {
      listener = it.arguments[0] as OnSuplaClientMessageListener
      return@doAnswer null
    }.whenever(messageHandler).registerMessageListener(any())

    val message: SuplaClientMsg = mockk()
    every { message.type } returns SuplaClientMsg.onDataChanged
    every { message.channelGroupId } returns channelGroupId

    // when
    usecase.register()
    listener!!.onSuplaClientMessageReceived(message)
    usecase.unregister()

    // then
    verify(updateEventsManager).emitGroupUpdate(channelGroupId)
    verify(messageHandler).registerMessageListener(listener)
    verify(messageHandler).unregisterMessageListener(listener)
    verifyNoMoreInteractions(updateEventsManager, messageHandler)
  }
}
