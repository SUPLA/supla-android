package org.supla.android.usecases.channel

import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMessageHandler.OnSuplaClientMessageListener
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.scenes.ListsEventsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelGroupStateObserverUseCase @Inject constructor(
  private val listsEventsManager: ListsEventsManager,
  private val messageHandler: SuplaClientMessageHandler,
) {

  private val listener: OnSuplaClientMessageListener = OnSuplaClientMessageListener { msg ->
    if (msg?.type == SuplaClientMsg.onDataChanged) {
      if (msg.channelGroupId != 0) {
        listsEventsManager.emitGroupChange(msg.channelGroupId)
      }
    }
  }

  fun register() {
    messageHandler.registerMessageListener(listener)
  }

  fun unregister() {
    messageHandler.unregisterMessageListener(listener)
  }
}