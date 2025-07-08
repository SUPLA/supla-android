package org.supla.android.lib
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

import android.os.Handler
import android.os.Message
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessageHandler

class AndroidSuplaClientMessageHandler : SuplaClientMessageHandler {

  private val scMsgHandler: Handler = ScMsgHandler()
  private val listeners: MutableList<SuplaClientMessageHandler.Listener> = mutableListOf()

  private class ScMsgHandler : Handler() {
    override fun handleMessage(msg: Message) {
      super.handleMessage(msg)
      if (msg.obj != null && msg.obj is SuplaClientMessage) {
        globalInstance.onSuplaClientMessage((msg.obj as SuplaClientMessage?)!!)
      }
    }
  }

  private fun onSuplaClientMessage(msg: SuplaClientMessage) {
    val listeners: List<SuplaClientMessageHandler.Listener>?
    synchronized(this) {
      listeners = mutableListOf<SuplaClientMessageHandler.Listener>().also { it.addAll(this.listeners.toList()) }
    }
    for (listener in listeners!!) {
      listener.onReceived(msg)
    }
  }

  override fun register(listener: SuplaClientMessageHandler.Listener) {
    synchronized(this) {
      if (!listeners.contains(listener)) {
        listeners.add(listener)
      }
    }
  }

  override fun unregister(listener: SuplaClientMessageHandler.Listener) {
    synchronized(this) {
      listeners.remove(listener)
    }
  }

  fun sendMessage(msg: SuplaClientMessage?) {
    scMsgHandler.sendMessage(scMsgHandler.obtainMessage(0, msg))
  }

  companion object {
    @JvmStatic
    private lateinit var globalInstance: AndroidSuplaClientMessageHandler

    fun getGlobalInstance(): AndroidSuplaClientMessageHandler {
      if (!AndroidSuplaClientMessageHandler::globalInstance.isInitialized) {
        globalInstance = AndroidSuplaClientMessageHandler()
      }

      return globalInstance
    }
  }
}

fun AndroidSuplaClientMessageHandler.sendChannelCaptionSetResult(channelId: Int, caption: String?, resultCode: Int) {
  sendMessage(SuplaClientMessage.ChannelCaptionSetResult(channelId, caption, resultCode))
}
