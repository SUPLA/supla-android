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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.supla.android.lib.SuplaChannelBasicCfg;
import org.supla.android.lib.SuplaChannelState;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaClientMsg;
import org.supla.android.lib.SuplaConnError;
import org.supla.android.lib.SuplaEvent;
import org.supla.android.lib.SuplaOAuthToken;
import org.supla.android.lib.SuplaRegisterError;
import org.supla.android.lib.SuplaRegistrationEnabled;
import org.supla.android.lib.SuplaVersionError;
import org.supla.android.lib.ZWaveNode;
import org.supla.android.lib.ZWaveWakeUpSettings;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressLint("registered")
public class BaseActivity extends Activity {

    private final CompositeDisposable disposables = new CompositeDisposable();

    protected static Activity CurrentActivity = null;
    private static Date BackgroundTime = null;
    private static Timer bgTimer = null;
    private Handler _sc_msg_handler = null;

    public static long getBackgroundTime() {

        if (BackgroundTime != null) {
            long diffInMs = (new Date()).getTime() - BackgroundTime.getTime();
            return TimeUnit.MILLISECONDS.toSeconds(diffInMs);
        }

        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SuplaApp.getApp().initTypefaceCollection(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bgTimer != null) {
            bgTimer.cancel();
            bgTimer = null;
        }

        BackgroundTime = null;

        SuplaApp.getApp().SuplaClientInitIfNeed(getApplicationContext());

    }

    @Override
    protected void onPause() {
        super.onPause();
        BackgroundTime = new Date();

        if (bgTimer == null) {
            bgTimer = new Timer();
            bgTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    if (bgTimer != null) {
                        SuplaClient client = SuplaApp.getApp().getSuplaClient();

                        if (client == null
                                || getBackgroundTime() >= getResources().getInteger(R.integer.background_timeout)) {

                            if (client != null) {
                                client.cancel();
                            }

                            bgTimer.cancel();
                            bgTimer = null;
                        }
                    }

                }
            }, 1000, 1000);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposables.clear();
    }

    protected void RegisterMessageHandler() {

        if (_sc_msg_handler != null)
            return;

        _sc_msg_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                SuplaClientMsg _msg = (SuplaClientMsg) msg.obj;

                switch (_msg.getType()) {
                    case SuplaClientMsg.onConnecting:
                    case SuplaClientMsg.onRegistering:
                    case SuplaClientMsg.onRegistered:
                    case SuplaClientMsg.onRegisterError:
                    case SuplaClientMsg.onDisconnected:
                    case SuplaClientMsg.onConnected:
                    case SuplaClientMsg.onVersionError:
                        beforeStatusMsg();
                        break;
                }

                switch (_msg.getType()) {
                    case SuplaClientMsg.onDataChanged:
                        onDataChangedMsg(_msg.getChannelId(),
                                _msg.getChannelGroupId(), _msg.isExtendedValue());
                        break;
                    case SuplaClientMsg.onConnecting:
                        onConnectingMsg();
                        break;
                    case SuplaClientMsg.onRegistering:
                        onRegisteringMsg();
                        break;
                    case SuplaClientMsg.onRegistered:
                        onRegisteredMsg();
                        break;
                    case SuplaClientMsg.onRegisterError:
                        onRegisterErrorMsg(_msg.getRegisterError());
                        break;
                    case SuplaClientMsg.onDisconnected:
                        onDisconnectedMsg();
                        break;
                    case SuplaClientMsg.onConnected:
                        onConnectedMsg();
                        break;
                    case SuplaClientMsg.onVersionError:
                        onVersionErrorMsg(_msg.getVersionError());
                        break;
                    case SuplaClientMsg.onEvent:
                        onEventMsg(_msg.getEvent());
                        break;
                    case SuplaClientMsg.onConnError:
                        onConnErrorMsg(_msg.getConnError());
                        break;
                    case SuplaClientMsg.onRegistrationEnabled:
                        onRegistrationEnabled(_msg.getRegistrationEnabled());
                        break;
                    case SuplaClientMsg.onOAuthTokenRequestResult:
                        onOAuthTokenRequestResult(_msg.getOAuthToken());
                        break;
                    case SuplaClientMsg.onCalCfgResult:
                        onCalCfgResult(_msg.getChannelId(),
                                _msg.getCommand(),
                                _msg.getResult(),
                                _msg.getData());
                        break;
                    case SuplaClientMsg.onSuperuserAuthorizationResult:
                        onSuperuserAuthorizationResult(_msg.isSuccess(), _msg.getResult());
                        break;
                    case SuplaClientMsg.onChannelState:
                        onChannelState(_msg.getChannelState());
                        break;
                    case SuplaClientMsg.onChannelBasicCfg:
                        onChannelBasicCfg(_msg.getChannelBasicCfg());
                        break;
                    case SuplaClientMsg.onChannelFunctionSetResult:
                        onChannelFunctionSetResult(_msg.getChannelId(), _msg.getFunc(), _msg.getCode());
                        break;
                    case SuplaClientMsg.onChannelCaptionSetResult:
                        onChannelCaptionSetResult(_msg.getChannelId(), _msg.getText(), _msg.getCode());
                        break;
                    case SuplaClientMsg.onClientsReconnectResult:
                        onClientsReconnectResult(_msg.getCode());
                        break;
                    case SuplaClientMsg.onSetRegistrationEnabledResult:
                        onSetRegistrationEnabledResult(_msg.getCode());
                        break;
                    case SuplaClientMsg.onZWaveResetAndClearResult:
                        onZWaveResetAndClearResult(_msg.getResult());
                        break;
                    case SuplaClientMsg.onZWaveAddNodeResult:
                        onZWaveAddNodeResult(_msg.getResult(), _msg.getNode());
                        break;
                    case SuplaClientMsg.onZWaveRemoveNodeResult:
                        onZWaveRemoveNodeResult(_msg.getResult(), _msg.getNodeId());
                        break;
                    case SuplaClientMsg.onZWaveGetNodeListResult:
                        onZWaveGetNodeListResult(_msg.getResult(), _msg.getNode());
                        break;
                    case SuplaClientMsg.onZWaveGetAssignedNodeIdResult:
                        onZWaveGetAssignedNodeIdResult(_msg.getResult(), _msg.getNodeId());
                        break;
                    case SuplaClientMsg.onZWaveAssignNodeIdResult:
                        onZWaveAssignNodeIdResult(_msg.getResult(), _msg.getNodeId());
                        break;
                    case SuplaClientMsg.onZWaveWakeUpSettingsReport:
                        onZWaveWakeUpSettingsReport(_msg.getResult(), _msg.getWakeUpSettings());
                        break;
                    case SuplaClientMsg.onZWaveSetWakeUpTimeResult:
                        onZwaveSetWakeUpTimeResult(_msg.getResult());
                        break;
                    case SuplaClientMsg.onCalCfgProgressReport:
                        onCalCfgProgressReport(_msg.getChannelId(),
                                _msg.getCommand(), _msg.getProgress());
                        break;
                }

            }
        };

        SuplaApp.getApp().addMsgReceiver(_sc_msg_handler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (_sc_msg_handler != null) {
            SuplaApp.getApp().removeMsgReceiver(_sc_msg_handler);
            _sc_msg_handler = null;
        }

    }

    protected void beforeStatusMsg() {
    }

    protected void onDataChangedMsg(int ChannelId, int GroupId, boolean extendedValue) {
    }

    protected void onConnectingMsg() {
    }

    protected void onRegisteringMsg() {
    }

    protected void onRegisteredMsg() {
    }

    protected void onRegisterErrorMsg(SuplaRegisterError error) {
    }

    protected void onDisconnectedMsg() {
    }

    protected void onConnectedMsg() {
    }

    protected void onVersionErrorMsg(SuplaVersionError error) {
    }

    protected void onEventMsg(SuplaEvent event) {
    }


    protected void onConnErrorMsg(SuplaConnError error) {
    }


    protected void onRegistrationEnabled(SuplaRegistrationEnabled registrationEnabled) {
    }


    protected void onOAuthTokenRequestResult(SuplaOAuthToken token) {
    }


    protected void onCalCfgResult(int channelId, int command, int result, byte[] data) {
    }


    protected void onCalCfgProgressReport(int channelId, int command, short progress) {
    }


    protected void onSuperuserAuthorizationResult(boolean success, int code) {
    }


    protected void onChannelState(SuplaChannelState state) {
    }


    protected void onChannelBasicCfg(SuplaChannelBasicCfg basicCfg) {
    }


    protected void onChannelFunctionSetResult(int channelId, int func, int code) {
    }


    protected void onChannelCaptionSetResult(int channelId, String caption, int code) {
    }


    protected void onClientsReconnectResult(int code) {
    }


    protected void onSetRegistrationEnabledResult(int code) {
    }


    protected void onZWaveResetAndClearResult(int result) {
    }


    protected void onZWaveAddNodeResult(int result, ZWaveNode node) {
    }

    protected void onZWaveRemoveNodeResult(int result, short nodeId) {
    }

    protected void onZWaveGetNodeListResult(int result, ZWaveNode node) {
    }

    protected void onZWaveGetAssignedNodeIdResult(int result, short nodeId) {
    }

    protected void onZWaveAssignNodeIdResult(int result, short nodeId) {
    }

    protected void onZWaveWakeUpSettingsReport(int result, ZWaveWakeUpSettings settings) {
    }

    protected void onZwaveSetWakeUpTimeResult(int result) {
    }

    protected void subscribe(Completable completable, Action onComplete, Consumer<? super Throwable> onError) {
        disposables.add(completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onComplete, onError));
    }
}
