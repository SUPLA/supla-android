package org.supla.android.lib;

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

import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import java.util.ArrayList;

public class SuplaClientMessageHandler {

  private static SuplaClientMessageHandler globalInstance;
  private Handler _sc_msg_handler = null;
  private ArrayList<OnSuplaClientMessageListener> listeners;

  private static class ScMsgHandler extends Handler {
    @Override
    public void handleMessage(@NonNull Message msg) {
      super.handleMessage(msg);
      if (msg != null && msg.obj != null && msg.obj instanceof SuplaClientMsg) {
        SuplaClientMessageHandler.getGlobalInstance()
            .onSuplaClientMessage((SuplaClientMsg) msg.obj);
      }
    }
  }

  public interface OnSuplaClientMessageListener {
    void onSuplaClientMessageReceived(SuplaClientMsg msg);
  }

  private void onSuplaClientMessage(SuplaClientMsg msg) {
    ArrayList<OnSuplaClientMessageListener> listeners;
    synchronized (this) {
      listeners = new ArrayList<>(this.listeners);
    }
    for (OnSuplaClientMessageListener listener : listeners) {
      listener.onSuplaClientMessageReceived(msg);
    }
  }

  public SuplaClientMessageHandler() {
    if (_sc_msg_handler == null) {
      _sc_msg_handler = new ScMsgHandler();
    }

    listeners = new ArrayList<>();
  }

  public void registerMessageListener(OnSuplaClientMessageListener listener) {
    synchronized (this) {
      if (listeners.indexOf(listener) == -1) {
        listeners.add(listener);
      }
    }
  }

  public void unregisterMessageListener(OnSuplaClientMessageListener listener) {
    synchronized (this) {
      listeners.remove(listener);
    }
  }

  public void sendMessage(SuplaClientMsg msg) {
    _sc_msg_handler.sendMessage(_sc_msg_handler.obtainMessage(msg.getType(), msg));
  }

  public static SuplaClientMessageHandler getGlobalInstance() {
    if (globalInstance == null) {
      globalInstance = new SuplaClientMessageHandler();
    }

    return globalInstance;
  }
}
