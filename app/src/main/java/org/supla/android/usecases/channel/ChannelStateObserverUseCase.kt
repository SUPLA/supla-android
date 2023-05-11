package org.supla.android.usecases.channel

import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMessageHandler.OnSuplaClientMessageListener
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.events.ListsEventsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelStateObserverUseCase @Inject constructor(
  private val listsEventsManager: ListsEventsManager,
  private val messageHandler: SuplaClientMessageHandler,
) {

  private val listener: OnSuplaClientMessageListener = OnSuplaClientMessageListener { msg ->
    if (msg?.type == SuplaClientMsg.onDataChanged) {
      if (msg.channelId != 0) {
        listsEventsManager.emitChannelChange(msg.channelId)
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