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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaClientMsg;
import org.supla.android.lib.SuplaConnError;
import org.supla.android.lib.SuplaEvent;
import org.supla.android.lib.SuplaRegisterError;
import org.supla.android.lib.SuplaRegistrationEnabled;
import org.supla.android.lib.SuplaVersionError;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@SuppressLint("Registered")
public class BaseActivity extends Activity {

    private Handler _sc_msg_handler = null;
    private static Date BackgroundTime = null;
    private static Timer bgTimer = null;
    protected static Activity CurrentActivity = null;

    @Override
    protected void onResume() {
        super.onResume();

        if ( bgTimer != null ) {
            bgTimer.cancel();
            bgTimer = null;
        }
/*
//CHECK IT AT BW (FAST DSL)

        {
            SuplaClient client = SuplaApp.getApp().getSuplaClient();

            if ( getBackgroundTime() >= getResources().getInteger(R.integer.background_timeout)
                    &&  client != null ) {

                Trace.d("A", "A");
                client.Reconnect();
            }
        }

*/
        BackgroundTime = null;

        SuplaApp.getApp().SuplaClientInitIfNeed(getApplicationContext());

    }

    @Override
    protected void onPause() {
        super.onPause();
        BackgroundTime = new Date();

        if ( bgTimer == null ) {
            bgTimer = new Timer();
            bgTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    SuplaClient client = SuplaApp.getApp().getSuplaClient();

                    if ( client == null
                            || getBackgroundTime() >= getResources().getInteger(R.integer.background_timeout) ) {

                        if ( client != null ) {
                            client.cancel();
                        }

                        bgTimer.cancel();
                        bgTimer = null;
                    }

                }
            }, 1000, 1000);
        }
    }

    public static long getBackgroundTime() {

        if ( BackgroundTime != null ) {
            long diffInMs = (new Date()).getTime() - BackgroundTime.getTime();
            return TimeUnit.MILLISECONDS.toSeconds(diffInMs);
        }

        return 0;
    }

    protected void RegisterMessageHandler() {

        if ( _sc_msg_handler != null )
            return;

        _sc_msg_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                SuplaClientMsg _msg = (SuplaClientMsg)msg.obj;

                switch(_msg.getType()) {
                    case SuplaClientMsg.onConnecting:
                    case SuplaClientMsg.onRegistering:
                    case SuplaClientMsg.onRegistered:
                    case SuplaClientMsg.onRegisterError:
                    case SuplaClientMsg.onDisconnected:
                    case SuplaClientMsg.onConnected:
                    case SuplaClientMsg.onVersionError:
                        BeforeStatusMsg();
                        break;
                }

                switch(_msg.getType()) {
                    case SuplaClientMsg.onDataChanged:
                        OnDataChangedMsg(_msg.getChannelId(), _msg.getChannelGroupId());
                        break;
                    case SuplaClientMsg.onConnecting:
                        OnConnectingMsg();
                        break;
                    case SuplaClientMsg.onRegistering:
                        OnRegisteringMsg();
                        break;
                    case SuplaClientMsg.onRegistered:
                        OnRegisteredMsg();
                        break;
                    case SuplaClientMsg.onRegisterError:
                        OnRegisterErrorMsg(_msg.getRegisterError());
                        break;
                    case SuplaClientMsg.onDisconnected:
                        OnDisconnectedMsg();
                        break;
                    case SuplaClientMsg.onConnected:
                        OnConnectedMsg();
                        break;
                    case SuplaClientMsg.onVersionError:
                        OnVersionErrorMsg(_msg.getVersionError());
                        break;
                    case SuplaClientMsg.onEvent:
                        OnEventMsg(_msg.getEvent());
                        break;
                    case SuplaClientMsg.onConnError:
                        OnConnErrorMsg(_msg.getConnError());
                        break;
                    case SuplaClientMsg.onRegistrationEnabled:
                        OnRegistrationEnabled(_msg.getRegistrationEnabled());
                        break;
                }

            }
        };

        SuplaApp.getApp().addMsgReceiver(_sc_msg_handler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ( _sc_msg_handler != null ) {
            SuplaApp.getApp().removeMsgReceiver(_sc_msg_handler);
            _sc_msg_handler = null;
        }

    }

    protected void BeforeStatusMsg() {}

    protected void OnDataChangedMsg(int ChannelId, int GroupId) {}

    protected void OnConnectingMsg() {}

    protected void OnRegisteringMsg() {}

    protected void OnRegisteredMsg() {}

    protected void OnRegisterErrorMsg(SuplaRegisterError error) {}

    protected void OnDisconnectedMsg() {}

    protected void OnConnectedMsg() {}

    protected void OnVersionErrorMsg(SuplaVersionError error) {}

    protected void OnEventMsg(SuplaEvent event) {}

    protected void OnConnErrorMsg(SuplaConnError error) {}

    protected void OnRegistrationEnabled(SuplaRegistrationEnabled registrationEnabled) {}
}
