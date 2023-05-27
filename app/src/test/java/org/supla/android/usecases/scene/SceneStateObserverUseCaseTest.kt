package org.supla.android.usecases.scene

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
class SceneStateObserverUseCaseTest {
  @Mock
  private lateinit var listsEventsManager: ListsEventsManager

  @Mock
  private lateinit var messageHandler: SuplaClientMessageHandler

  @InjectMocks
  private lateinit var usecase: SceneStateObserverUseCase

  @Test
  fun `should register and emit scene changes`() {
    // given
    val sceneId = 123
    var listener: SuplaClientMessageHandler.OnSuplaClientMessageListener? = null

    doAnswer {
      listener = it.arguments[0] as SuplaClientMessageHandler.OnSuplaClientMessageListener
      return@doAnswer null
    }.whenever(messageHandler).registerMessageListener(any())

    val message: SuplaClientMsg = mockk()
    every { message.type } returns SuplaClientMsg.onSceneChanged
    every { message.sceneId } returns sceneId

    // when
    usecase.register()
    listener!!.onSuplaClientMessageReceived(message)
    usecase.unregister()

    // then
    verify(listsEventsManager).emitSceneChange(sceneId)
    verify(messageHandler).registerMessageListener(listener)
    verify(messageHandler).unregisterMessageListener(listener)
    verifyNoMoreInteractions(listsEventsManager, messageHandler)
  }
}
