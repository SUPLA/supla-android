package org.supla.android.usecases.channel

import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.supla.android.events.ListsEventsManager
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMsg

@RunWith(MockitoJUnitRunner::class)
class ChannelStateObserverUseCaseTest {
  @Mock
  private lateinit var listsEventsManager: ListsEventsManager

  @Mock
  private lateinit var messageHandler: SuplaClientMessageHandler

  @InjectMocks
  private lateinit var usecase: ChannelStateObserverUseCase

  @Test
  fun `should register and emit group changes`() {
    // given
    val channelId = 123
    var listener: SuplaClientMessageHandler.OnSuplaClientMessageListener? = null

    doAnswer {
      listener = it.arguments[0] as SuplaClientMessageHandler.OnSuplaClientMessageListener
      return@doAnswer null
    }.whenever(messageHandler).registerMessageListener(any())

    val message: SuplaClientMsg = mockk()
    every { message.type } returns SuplaClientMsg.onDataChanged
    every { message.channelId } returns channelId

    // when
    usecase.register()
    listener!!.onSuplaClientMessageReceived(message)
    usecase.unregister()

    // then
    verify(listsEventsManager).emitChannelChange(channelId)
    verify(messageHandler).registerMessageListener(listener)
    verify(messageHandler).unregisterMessageListener(listener)
    verifyNoMoreInteractions(listsEventsManager, messageHandler)
  }
}
