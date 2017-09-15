package org.supla.android;

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

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaClientMsg;

import java.util.ArrayList;
import android.os.Vibrator;

public class SuplaApp {

    private ArrayList<Handler>msgReceivers = new ArrayList<>();

    private static final Object _lck1 = new Object();
    private static final Object _lck2 = new Object();

    private static SuplaClient _SuplaClient = null;
    private static SuplaApp _SuplaApp = null;

    private Handler _sc_msg_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            SuplaClientMsg _msg = (SuplaClientMsg)msg.obj;

            if ( _msg != null ) {

                synchronized (_lck2) {

                    for(int a=0;a<msgReceivers.size();a++) {
                        Handler msgReceiver = msgReceivers.get(a);
                        msgReceiver.sendMessage(msgReceiver.obtainMessage(_msg.getType(), _msg));
                    }

                }


            }
        }
    };

    public void addMsgReceiver(Handler msgReceiver) {
        synchronized (_lck2) {

            if ( msgReceivers.indexOf(msgReceiver) == -1 )
                msgReceivers.add(msgReceiver);

        }
    }

    public void removeMsgReceiver(Handler msgReceiver) {
        synchronized (_lck2) {
            msgReceivers.remove(msgReceiver);
        }
    }

    public SuplaClient SuplaClientInitIfNeed(Context context) {

        SuplaClient result;

        synchronized (_lck1) {

            if (_SuplaClient == null || _SuplaClient.canceled() ) {
                _SuplaClient = new SuplaClient(context);
                _SuplaClient.setMsgHandler(_sc_msg_handler);
                _SuplaClient.start();
            }

            result = _SuplaClient;
        }

        return result;
    }

    public void SuplaClientTerminate() {

        synchronized (_lck1) {

            if (_SuplaClient != null) {
                _SuplaClient.cancel();
            }
        }
    }

    public void OnSuplaClientFinished(SuplaClient sender) {

        synchronized (_lck1) {

            if (_SuplaClient == sender ) {
                _SuplaClient = null;
            }
        }
    }

    public SuplaClient getSuplaClient() {

        SuplaClient result;

        synchronized (_lck1) {
            result = _SuplaClient;
        }

        return result;
    }

    public static SuplaApp getApp() {

        synchronized (_lck1) {

            if (_SuplaApp == null) {
                _SuplaApp = new SuplaApp();
            }

        }

        return _SuplaApp;
    }

    public static void Vibrate(Context context) {

        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if ( v != null )
            v.vibrate(100);
    }


}
