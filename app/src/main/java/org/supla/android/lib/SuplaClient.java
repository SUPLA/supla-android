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

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.supla.android.BuildConfig;
import org.supla.android.Preferences;
import org.supla.android.R;
import org.supla.android.SuplaApp;
import org.supla.android.Trace;
import org.supla.android.db.Channel;
import org.supla.android.db.DbHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

@SuppressWarnings("unused")
public class SuplaClient extends Thread {

    private static final String log_tag = "SuplaClientThread";
    private static final Object st_lck = new Object();
    private static SuplaRegisterError lastRegisterError = null;

    static {
        System.loadLibrary("suplaclient");
    }

    private final Object sc_lck = new Object();
    private int _client_id;
    private long _supla_client_ptr = 0;
    private long _supla_client_ptr_counter = 0;
    private boolean _canceled = false;
    private Context _context;
    private DbHelper DbH = null;
    private int regTryCounter = 0; // supla-server v1.0 for Raspberry Compatibility fix
    private long lastTokenRequest = 0;
    private boolean superUserAuthorized = false;
    private String oneTimePassword;

    public SuplaClient(Context context, String oneTimePassword) {

        super();
        _context = context;
        this.oneTimePassword = oneTimePassword;

    }

    public static SuplaRegisterError getLastRegisterError() {

        SuplaRegisterError result;

        synchronized (st_lck) {
            result = lastRegisterError;
        }

        return result;
    }

    public native void cfgInit(SuplaCfg cfg);

    private native long scInit(SuplaCfg cfg);

    private native void scFree(long _supla_client);

    private native int scGetId(long _supla_client);

    private native boolean scConnect(long _supla_client);

    private native boolean scConnected(long _supla_client);

    private native boolean scRegistered(long _supla_client);

    private native void scDisconnect(long _supla_client);

    private native boolean scIterate(long _supla_client, int wait_usec);

    private native boolean scOpen(long _supla_client, int ID, int Group, int Open);

    private native boolean scTimerArm(long _supla_client, int ChannelID, int On, int DurationMS);

    private native boolean scSetRGBW(long _supla_client, int ID, int Group,
                                     int Color, int ColorBrightness, int Brightness,
                                     int TurnOnOff);

    private native boolean scGetRegistrationEnabled(long _supla_client);

    private native int scGetProtoVersion(long _supla_client);

    private native int scGetMaxProtoVersion(long _supla_client);

    private native boolean scOAuthTokenRequest(long _supla_client);

    private native boolean scDeviceCalCfgRequest(long _supla_client, int ID, int Group,
                                                 int Command, int DataType, byte[] Data);

    private native boolean scDeviceCalCfgCancelAllCommands(long _supla_client,
                                                           int DeviceID);

    private native boolean scThermostatScheduleCfgRequest(long _supla_client, int ID, int Group,
                                                          SuplaThermostatScheduleCfg cfg);

    private native boolean scSuperUserAuthorizationRequest(long _supla_client,
                                                           String email, String password);

    private native boolean scGetSuperUserAuthorizationResult(long _supla_client);

    private native boolean scGetChannelState(long _supla_client, int ChannelID);

    private native boolean scGetChannelBasicCfg(long _supla_client, int ChannelID);

    private native boolean scSetChannelFunction(long _supla_client, int ChannelID, int Function);

    private native boolean scSetChannelCaption(long _supla_client, int ChannelID, String Caption);

    private native boolean scSetLocationCaption(long _supla_client, int LocationID, String Caption);

    private native boolean scReconnectAllClients(long _supla_client);

    private native boolean scSetRegistrationEnabled(long _supla_client,
                                                    int ioDeviceRegTimeSec, int clientRegTimeSec);

    private native boolean scReconnectDevice(long _supla_client, int DeviceID);

    private native boolean scZWaveConfigModeActive(long _supla_client, int DeviceID);

    private native boolean scZWaveResetAndClear(long _supla_client, int DeviceID);

    private native boolean scZWaveAddNode(long _supla_client, int DeviceID);

    private native boolean scZWaveRemoveNode(long _supla_client, int DeviceID);

    private native boolean scZWaveGetNodeList(long _supla_client, int DeviceID);

    private native boolean scZWaveGetAssignedNodeId(long _supla_client, int ChannelID);

    private native boolean scZWaveAssignNodeId(long _supla_client, int ChannelID, short NodeId);

    private native boolean scZWaveGetWakeUpSettings(long _supla_client, int ChannelID);

    private native boolean scZWaveSetWakeUpTime(long _supla_client, int ChannelID, int Time);

    private native boolean scSetLightsourceLifespan(long _supla_client, int ChannelID, boolean
            resetCounter, boolean setTime, int lifeSpan);

    private native boolean scSetDfgTransparency(long _supla_client, int ChannelID, short mask,
                                                short active_bits);

    private void sendMessage(SuplaClientMsg msg) {
        if (canceled()) return;
        SuplaClientMessageHandler.getGlobalInstance().sendMessage(msg);
    }

    private void init(SuplaCfg cfg) {
        synchronized (sc_lck) {
            if (_supla_client_ptr == 0) {
                _supla_client_ptr_counter = 0;
                _supla_client_ptr = scInit(cfg);
            }
        }
    }

    private void lockClientPtr() {
        synchronized (sc_lck) {
            if (_supla_client_ptr != 0) {
                _supla_client_ptr_counter++;
            }
        }
    }

    private void unlockClientPtr() {
        synchronized (sc_lck) {
            if (_supla_client_ptr != 0
                    && _supla_client_ptr_counter > 0) {

                _supla_client_ptr_counter--;
            }
        }
    }

    private void free() {
        boolean freed = false;

        while (!freed) {

            synchronized (sc_lck) {
                if (_supla_client_ptr != 0
                        && _supla_client_ptr_counter == 0) {
                    scFree(_supla_client_ptr);
                    _supla_client_ptr = 0;
                }


                freed = _supla_client_ptr == 0;
            }

            if (!freed) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public int getClientId() {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0 ? scGetId(_supla_client_ptr) : 0;
        } finally {
            unlockClientPtr();
        }
    }

    private boolean connect() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo =
                connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            lockClientPtr();
            try {
                return _supla_client_ptr != 0 && scConnect(_supla_client_ptr);
            } finally {
                unlockClientPtr();
            }
        }

        return false;
    }

    public void reconnect() {
        if (connected()) disconnect();
    }

    private boolean connected() {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0 && scConnected(_supla_client_ptr);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean registered() {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0 && scRegistered(_supla_client_ptr);
        } finally {
            unlockClientPtr();
        }
    }

    private void disconnect() {
        lockClientPtr();
        try {
            if (_supla_client_ptr != 0) {
                scDisconnect(_supla_client_ptr);
            }
        } finally {
            unlockClientPtr();
        }
    }

    private boolean iterate() {
        return _supla_client_ptr != 0 && scIterate(_supla_client_ptr, 100000);
    }

    public boolean open(int ID, boolean Group, int Open) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0 && scOpen(_supla_client_ptr, ID, Group ? 1 : 0, Open);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean timerArm(int ChannelID, boolean On, int DurationMS) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0 && scTimerArm(_supla_client_ptr,
                    ChannelID, On ? 1 : 0, DurationMS);
        } finally {
            unlockClientPtr();
        }
    }
    
    public boolean open(int ChannelID, int Open) {
        return open(ChannelID, false, Open);
    }

    public boolean setRGBW(int ID, boolean Group, int Color, int ColorBrightness, int Brightness,
                           boolean TurnOnOff) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scSetRGBW(_supla_client_ptr, ID, Group ? 1 : 0, Color,
                    ColorBrightness, Brightness, TurnOnOff ? 1 : 0);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean setRGBW(int ChannelID, int Color, int ColorBrightness, int Brightness,
                           boolean TurnOnOff) {
        return setRGBW(ChannelID, false, Color, ColorBrightness, Brightness, TurnOnOff);
    }

    public void getRegistrationEnabled() {
        lockClientPtr();
        try {
            if (_supla_client_ptr != 0) {
                scGetRegistrationEnabled(_supla_client_ptr);
            }
        } finally {
            unlockClientPtr();
        }
    }

    public int getProtoVersion() {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0 ? scGetProtoVersion(_supla_client_ptr) : 0;
        } finally {
            unlockClientPtr();
        }
    }

    public int getMaxProtoVersion() {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0 ? scGetMaxProtoVersion(_supla_client_ptr) : 0;
        } finally {
            unlockClientPtr();
        }
    }

    public void oAuthTokenRequest() {
        long now = System.currentTimeMillis();
        if (now - lastTokenRequest <= 5000) {
            Trace.d(log_tag, "Token already requested: "
                    + (now - lastTokenRequest));
            return;
        }

        lockClientPtr();
        try {
            if (_supla_client_ptr != 0 && scOAuthTokenRequest(_supla_client_ptr)) {
                lastTokenRequest = now;
            }
        } finally {
            unlockClientPtr();
        }
    }

    public boolean turnOnOff(Context context, boolean turnOn,
                             int remoteId, boolean group, int channelFunc, boolean vibrate) {
        if ((channelFunc == SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
              || channelFunc == SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
              || channelFunc == SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER)) {
            if (turnOn) {
                DbHelper helper = DbHelper.getInstance(context);
                if (helper == null) {
                    return false;
                }
                Channel channel = helper.getChannel(remoteId);
                if (channel == null) {
                    return false;
                }
                if (!channel.getValue().hiValue()
                        && channel.getValue().overcurrentRelayOff()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(android.R.string.dialog_alert_title);
                    builder.setMessage(R.string.overcurrent_question);

                    builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                        dialog.dismiss();

                        if (vibrate) {
                            SuplaApp.Vibrate(context);
                        }
                        open(remoteId, group, 1);
                    });

                    builder.setNeutralButton(R.string.no,
                            (dialog, id) -> dialog.cancel());

                    AlertDialog alert = builder.create();
                    alert.show();
                    return true;
                }
            }

            if (vibrate) {
                SuplaApp.Vibrate(context);
            }
            open(remoteId, group, turnOn ? 1 : 0);
            return true;
        }

        return false;
    }

    public void superUserAuthorizationRequest(String email, String password) {
        lockClientPtr();
        try {
            if (_supla_client_ptr != 0) {
                scSuperUserAuthorizationRequest(_supla_client_ptr, email, password);
            }
        } finally {
            unlockClientPtr();
        }
    }

    public void getSuperUserAuthorizationResult() {
        lockClientPtr();
        try {
            if (_supla_client_ptr != 0) {
                scGetSuperUserAuthorizationResult(_supla_client_ptr);
            }
        } finally {
            unlockClientPtr();
        }
    }

    public boolean isSuperUserAuthorized() {
        boolean result = false;
        synchronized (sc_lck) {
            result = superUserAuthorized;
        }
        return result;
    }

    public boolean deviceCalCfgRequest(int ID, boolean Group, int Command,
                                       int DataType, byte[] Data) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0 && scDeviceCalCfgRequest(_supla_client_ptr,
                    ID, Group ? 1 : 0, Command, DataType, Data);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean deviceCalCfgRequest(int ChannelID, int Command, int DataType, byte[] Data) {
        return deviceCalCfgRequest(ChannelID, false, Command, DataType, Data);
    }

    public boolean thermostatScheduleCfgRequest(int ID, boolean Group,
                                                SuplaThermostatScheduleCfg cfg) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0 && scThermostatScheduleCfgRequest(_supla_client_ptr,
                    ID, Group ? 1 : 0, cfg);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean deviceCalCfgCancelAllCommands(int DeviceID) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0 && scDeviceCalCfgCancelAllCommands(_supla_client_ptr,
                    DeviceID);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean thermostatScheduleCfgRequest(int ChannelID, SuplaThermostatScheduleCfg cfg) {
        return thermostatScheduleCfgRequest(ChannelID, false, cfg);
    }

    public boolean getChannelState(int ChannelID) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0 && scGetChannelState(_supla_client_ptr, ChannelID);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean getChannelBasicCfg(int ChannelID) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0 && scGetChannelBasicCfg(_supla_client_ptr, ChannelID);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean setChannelFunction(int ChannelID, int Function) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scSetChannelFunction(_supla_client_ptr, ChannelID, Function);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean setChannelCaption(int ChannelID, String Caption) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scSetChannelCaption(_supla_client_ptr, ChannelID, Caption);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean setLocationCaption(int LocationID, String Caption) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scSetLocationCaption(_supla_client_ptr, LocationID, Caption);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean setDfgTransparency(int ChannelID, short mask, short active_bits) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scSetDfgTransparency(_supla_client_ptr, ChannelID, mask, active_bits);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean reconnectAllClients() {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scReconnectAllClients(_supla_client_ptr);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean setRegistrationEnabled(int ioDeviceRegTimeSec, int clientRegTimeSec) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scSetRegistrationEnabled(_supla_client_ptr,
                    ioDeviceRegTimeSec, clientRegTimeSec);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean reconnectDevice(int DeviceId) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scReconnectDevice(_supla_client_ptr, DeviceId);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean zwaveConfigModeActive(Integer DeviceID) {
        if (DeviceID == null) {
            return false;
        }

        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scZWaveConfigModeActive(_supla_client_ptr, DeviceID.intValue());
        } finally {
            unlockClientPtr();
        }
    }

    public boolean zwaveResetAndClear(Integer DeviceID) {
        if (DeviceID == null) {
            return false;
        }

        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scZWaveResetAndClear(_supla_client_ptr, DeviceID.intValue());
        } finally {
            unlockClientPtr();
        }
    }

    public boolean zwaveAddNode(Integer DeviceID) {
        if (DeviceID == null) {
            return false;
        }

        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scZWaveAddNode(_supla_client_ptr, DeviceID.intValue());
        } finally {
            unlockClientPtr();
        }
    }

    public boolean zwaveRemoveNode(Integer DeviceID) {
        if (DeviceID == null) {
            return false;
        }

        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scZWaveRemoveNode(_supla_client_ptr, DeviceID.intValue());
        } finally {
            unlockClientPtr();
        }
    }

    public boolean zwaveGetNodeList(Integer DeviceID) {
        if (DeviceID == null) {
            return false;
        }

        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scZWaveGetNodeList(_supla_client_ptr, DeviceID.intValue());
        } finally {
            unlockClientPtr();
        }
    }

    public boolean zwaveGetAssignedNodeId(int ChannelID) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scZWaveGetAssignedNodeId(_supla_client_ptr, ChannelID);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean zwaveAssignNodeId(int ChannelID, short NodeId) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scZWaveAssignNodeId(_supla_client_ptr, ChannelID, NodeId);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean zwaveGetWakeUpSettings(int ChannelID) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scZWaveGetWakeUpSettings(_supla_client_ptr, ChannelID);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean zwaveSetWakeUpTime(int ChannelID, int Time) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scZWaveSetWakeUpTime(_supla_client_ptr, ChannelID, Time);
        } finally {
            unlockClientPtr();
        }
    }

    public boolean setLightsourceLifespan(int ChannelID, boolean resetCounter,
                                          boolean setTime, int lifeSpan) {
        lockClientPtr();
        try {
            return _supla_client_ptr != 0
                    && scSetLightsourceLifespan(_supla_client_ptr, ChannelID,
                    resetCounter, setTime, lifeSpan);
        } finally {
            unlockClientPtr();
        }
    }

    private void onVersionError(SuplaVersionError versionError) {
        Trace.d(log_tag, Integer.valueOf(versionError.Version).toString() + ","
                + Integer.valueOf(versionError.RemoteVersionMin).toString() + ","
                + Integer.valueOf(versionError.RemoteVersion).toString());

        regTryCounter = 0;
        Preferences prefs = new Preferences(_context);


        if ((prefs.isAdvancedCfg() || versionError.RemoteVersion >= 7)
                && versionError.RemoteVersion >= 5
                && versionError.Version > versionError.RemoteVersion
                && prefs.getPreferedProtocolVersion() != versionError.RemoteVersion) {

            // set prefered to lower
            prefs.setPreferedProtocolVersion(versionError.RemoteVersion);
            reconnect();
            return;
        }

        SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onVersionError);
        msg.setVersionError(versionError);
        sendMessage(msg);

        cancel();
    }

    private void onConnecting() {
        Trace.d(log_tag, "Connecting");

        sendMessage(new SuplaClientMsg(this, SuplaClientMsg.onConnecting));
    }

    private void onConnError(SuplaConnError connError) {

        Trace.d(log_tag, connError.codeToString(_context));

        SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onConnError);
        msg.setConnError(connError);
        sendMessage(msg);

        if (connError.Code == SuplaConst.SUPLA_RESULTCODE_HOSTNOTFOUND) {
            cancel();
        }
    }

    private void onConnected() {
        Trace.d(log_tag, "connected");

        sendMessage(new SuplaClientMsg(this, SuplaClientMsg.onConnected));
    }

    private void onDisconnected() {

        sendMessage(new SuplaClientMsg(this, SuplaClientMsg.onDisconnected));
    }

    private void onRegistering() {
        Trace.d(log_tag, "Registering");

        regTryCounter++;
        sendMessage(new SuplaClientMsg(this, SuplaClientMsg.onRegistering));
    }

    private void onRegistered(SuplaRegisterResult registerResult) {

        Trace.d(log_tag, "registered");

        regTryCounter = 0;
        Preferences prefs = new Preferences(_context);

        if (getMaxProtoVersion() > 0
                && prefs.getPreferedProtocolVersion() < getMaxProtoVersion()
                && registerResult.Version > prefs.getPreferedProtocolVersion()
                && registerResult.Version <= getMaxProtoVersion()) {
            prefs.setPreferedProtocolVersion(registerResult.Version);
        }

        _client_id = registerResult.ClientID;

        Trace.d(log_tag, "Protocol Version="
                + registerResult.Version);
        Trace.d(log_tag, "registerResult.ChannelCount="
                + registerResult.ChannelCount);
        Trace.d(log_tag, "registerResult.ChannelGroupCount="
                + registerResult.ChannelGroupCount);

        if (registerResult.ChannelCount == 0
                && DbH.setChannelsVisible(0, 2)) {

            onDataChanged();
        }

        SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onRegistered);
        msg.setRegisterResult(registerResult);
        sendMessage(msg);
    }

    private void onRegisterError(SuplaRegisterError registerError) {
        Trace.d(log_tag, registerError.codeToString(_context));

        regTryCounter = 0;
        synchronized (st_lck) {
            lastRegisterError = registerError;
        }

        SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onRegisterError);
        msg.setRegisterError(registerError);
        sendMessage(msg);

        cancel();
    }

    private void onRegistrationEnabled(SuplaRegistrationEnabled registrationEnabled) {

        Trace.d(log_tag, "onRegistrationEnabled");

        SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onRegistrationEnabled);
        msg.setRegistrationEnabled(registrationEnabled);
        sendMessage(msg);

    }

    private void onMinVersionRequired(SuplaMinVersionRequired minVersionRequired) {

        Trace.d(log_tag, "SuplaMinVersionRequired - CallType: "
                + minVersionRequired.CallType + " MinVersion: "
                + minVersionRequired.MinVersion);

    }

    private void locationUpdate(SuplaLocation location) {
        Trace.d(log_tag, "Location "+Integer.toString(location.Id)+" "+location.Caption);

        if (DbH.updateLocation(location)) {
            Trace.d(log_tag, "Location updated");
            onDataChanged();
        }

    }

    private boolean isChannelExcluded(SuplaChannel channel) {
        // For partner applications
        return false;
    }

    private void channelUpdate(SuplaChannel channel) {

        boolean _DataChanged = false;

        Trace.d(log_tag, "Channel Function" + channel.Func
                + "  channel ID: " + channel.Id
                + " channel Location ID: " + channel.LocationID
                + " OnLine: " + channel.OnLine
                + " AltIcon: " + channel.AltIcon
                + " UserIcon: " + channel.UserIcon
                + " Flags: " + channel.Flags);

        // Update channel value before update the channel
        if (DbH.updateChannelValue(channel.Value, channel.Id, channel.OnLine)) {
            _DataChanged = true;
        }

        if (!isChannelExcluded(channel)
                && DbH.updateChannel(channel)) {
            _DataChanged = true;
        }

        if (channel.EOL
                && DbH.setChannelsVisible(0, 2)) {
            _DataChanged = true;
        }

        if (_DataChanged) {
            Trace.d(log_tag, "Channel updated");
            onDataChanged(channel.Id, 0);
        }

    }

    private void onChannelGroupValueChanged() {
        List<Integer> groupIds = DbH.updateChannelGroups();
        for (int groupId : groupIds) {
            onDataChanged(0, groupId);
        }
    }

    private void channelGroupUpdate(SuplaChannelGroup channel_group) {

        boolean _DataChanged = false;

        Trace.d(log_tag, "Channel Group Function "
                + channel_group.Func + "  group ID: "
                + channel_group.Id + " group Location ID: "
                + channel_group.LocationID + " AltIcon: "
                + channel_group.AltIcon + " UserIcon: "
                + channel_group.UserIcon);

        if (DbH.updateChannelGroup(channel_group)) {
            _DataChanged = true;
        }

        if (channel_group.EOL
                && DbH.setChannelGroupsVisible(0, 2)) {
            _DataChanged = true;
        }

        if (channel_group.EOL) {
            onChannelGroupValueChanged();
        }

        if (_DataChanged) {
            Trace.d(log_tag, "Channel Group updated");
            onDataChanged(0, channel_group.Id);
        }

    }

    private void channelGroupRelationUpdate(SuplaChannelGroupRelation channelgroup_relation) {

        boolean _DataChanged = false;

        Trace.d(log_tag, "Channel Group Relation group ID: "
                + channelgroup_relation.ChannelGroupID
                + " channel ID: " + channelgroup_relation.ChannelID);

        if (DbH.updateChannelGroupRelation(channelgroup_relation)) {
            _DataChanged = true;
        }

        if (channelgroup_relation.EOL
                && DbH.setChannelGroupRelationsVisible(0, 2)) {
            _DataChanged = true;
        }

        if (channelgroup_relation.EOL) {
            onChannelGroupValueChanged();
        }

        if (_DataChanged) {
            Trace.d(log_tag, "Channel Group Relation updated");
            onDataChanged(0, channelgroup_relation.ChannelGroupID);
        }

    }

    private void channelValueUpdate(SuplaChannelValueUpdate channelValueUpdate) {

        if (DbH.updateChannelValue(channelValueUpdate)) {

            Trace.d(log_tag, "Channel id" + channelValueUpdate.Id
                    + " sub_value type: " +  channelValueUpdate.Value.SubValueType
                    + " value updated" + " OnLine: " + channelValueUpdate.OnLine
                    + " value[0]: " + channelValueUpdate.Value.Value[0]);
            onDataChanged(channelValueUpdate.Id, 0);
        }

        if (channelValueUpdate.EOL) {
            onChannelGroupValueChanged();
        }
    }

    private void channelExtendedValueUpdate(SuplaChannelExtendedValueUpdate channelExtendedValueUpdate) {
        if (DbH.updateChannelExtendedValue(channelExtendedValueUpdate.Value,
                channelExtendedValueUpdate.Id)) {
            onDataChanged(channelExtendedValueUpdate.Id, 0, true);
        }
    }

    private void onEvent(SuplaEvent event) {
        Trace.d(log_tag, "Event");

        SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onEvent);
        event.Owner = event.SenderID == _client_id;
        msg.setEvent(event);
        sendMessage(msg);
    }

    private void onOAuthTokenRequestResult(SuplaOAuthToken token) {
        Trace.d(log_tag, "OAuthToken" + (token == null ? " is null" : ""));

        if (token != null && token.getUrl() == null) {
            Preferences prefs = new Preferences(_context);
            try {
                token.setUrl(new URL("https://" + prefs.getServerAddress()));
            } catch (MalformedURLException ignored) {
            }
        }

        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onOAuthTokenRequestResult);
        msg.setOAuthToken(token);
        sendMessage(msg);
    }

    private void onDeviceCalCfgResult(int ChannelId, int Command, int Result, byte[] Data) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onCalCfgResult);
        msg.setChannelId(ChannelId);
        msg.setCommand(Command);
        msg.setResult(Result);
        msg.setData(Data);
        sendMessage(msg);
    }

    private void onDeviceCalCfgProgressReport(int ChannelId, int Command, short Progress) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onCalCfgProgressReport);
        msg.setChannelId(ChannelId);
        msg.setCommand(Command);
        msg.setProgress(Progress);
        sendMessage(msg);
    }

    private void onDeviceCalCfgDebugString(String str) {
        Trace.d("CalCfgDebugString", str);
    }

    private void onChannelState(SuplaChannelState state) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onChannelState);
        msg.setChannelState(state);
        sendMessage(msg);
    }

    private void onChannelBasicCfg(SuplaChannelBasicCfg cfg) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onChannelBasicCfg);
        msg.setChannelBasicCfg(cfg);
        sendMessage(msg);
    }

    private void onChannelFunctionSetResult(int ChannelID, int Func, int ResultCode) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onChannelFunctionSetResult);
        msg.setCode(ResultCode);
        msg.setFunc(Func);
        msg.setChannelId(ChannelID);
        sendMessage(msg);
    }

    private void onChannelCaptionSetResult(int ChannelID, String Caption, int ResultCode) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onChannelCaptionSetResult);
        msg.setCode(ResultCode);
        msg.setText(Caption);
        msg.setChannelId(ChannelID);
        sendMessage(msg);
    }

    private void onLocationCaptionSetResult(int LocationID, String Caption, int ResultCode) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onLocationCaptionSetResult);
        msg.setCode(ResultCode);
        msg.setText(Caption);
        msg.setLocationId(LocationID);
        sendMessage(msg);
    }

    private void onClientsReconnectResult(int ResultCode) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onClientsReconnectResult);
        msg.setCode(ResultCode);
        sendMessage(msg);
    }

    private void onSetRegistrationEnabledResult(int ResultCode) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onSetRegistrationEnabledResult);
        msg.setCode(ResultCode);
        sendMessage(msg);
    }

    private void onSuperUserAuthorizationResult(boolean authorized, int code) {
        synchronized (sc_lck) {
            superUserAuthorized = authorized && code == SuplaConst.SUPLA_RESULTCODE_AUTHORIZED;
        }

        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onSuperuserAuthorizationResult);
        msg.setSuccess(authorized);
        msg.setCode(code);
        sendMessage(msg);
    }

    private void onDataChanged(int ChannelId, int GroupId, boolean extendedValue) {

        SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onDataChanged);
        msg.setChannelId(ChannelId);
        msg.setChannelGroupId(GroupId);
        msg.setExtendedValue(extendedValue);

        sendMessage(msg);

    }

    private void onDataChanged(int ChannelId, int GroupId) {
        onDataChanged(ChannelId, GroupId, false);
    }

    private void onDataChanged() {
        onDataChanged(0, 0, false);
    }

    private void onZWaveResetAndClearResult(int result) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onZWaveResetAndClearResult);
        msg.setResult(result);
        sendMessage(msg);
    }

    private void onZWaveAddNodeResult(int result, ZWaveNode node) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onZWaveAddNodeResult);
        msg.setResult(result);
        msg.setNode(node);
        sendMessage(msg);
    }

    private void onZWaveRemoveNodeResult(int result, short nodeId) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onZWaveRemoveNodeResult);
        msg.setResult(result);
        msg.setNodeId(nodeId);
        sendMessage(msg);
    }

    private void onZWaveGetNodeListResult(int result, ZWaveNode node) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onZWaveGetNodeListResult);
        msg.setResult(result);
        msg.setNode(node);
        sendMessage(msg);
    }

    private void onZWaveGetAssignedNodeIdResult(int result, short nodeId) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onZWaveGetAssignedNodeIdResult);
        msg.setResult(result);
        msg.setNodeId(nodeId);
        sendMessage(msg);
    }

    private void onZWaveAssignNodeIdResult(int result, short nodeId) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onZWaveAssignNodeIdResult);
        msg.setResult(result);
        msg.setNodeId(nodeId);
        sendMessage(msg);
    }

    private void onZWaveWakeUpSettingsReport(int result, ZWaveWakeUpSettings settings) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onZWaveWakeUpSettingsReport);
        msg.setResult(result);
        msg.setWakeUpSettings(settings);
        sendMessage(msg);
    }

    private void onZWaveSetWakeUpTimeResult(int result) {
        SuplaClientMsg msg = new SuplaClientMsg(this,
                SuplaClientMsg.onZWaveSetWakeUpTimeResult);
        msg.setResult(result);
        sendMessage(msg);
    }

    public synchronized boolean canceled() {
        return _canceled;
    }

    public synchronized void cancel() {
        _canceled = true;
    }

    private String autodiscoverGetHost(String email) {

        String result = "";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("autodiscover.supla.org")
                .appendPath("users")
                .appendPath(email);

        URL url;
        try {
            url = new URL(builder.build().toString());

            StringBuilder json = new StringBuilder();
            String line;

            HttpsURLConnection https;
            try {
                https = (HttpsURLConnection) url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(https.getInputStream()));

                while ((line = br.readLine()) != null) {
                    json.append(line);
                }

                JSONTokener tokener = new JSONTokener(json.toString());
                try {
                    JSONObject jsonResult = new JSONObject(tokener);
                    if (jsonResult.getString("email").equals(email)) {
                        result = jsonResult.getString("server");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void setVisible(int Visible, int WhereVisible) {

        boolean _DataChanged = false;

        if (DbH.setChannelsVisible(Visible, WhereVisible)) {
            _DataChanged = true;
        }

        if (DbH.setChannelGroupsVisible(Visible, WhereVisible)) {
            _DataChanged = true;
        }

        if (DbH.setChannelGroupRelationsVisible(Visible, WhereVisible)) {
            _DataChanged = true;
        }

        if (DbH.setChannelsOffline()) {
            _DataChanged = true;
        }

        if (_DataChanged) {
            onDataChanged();
        }

    }

    public void run() {

        DbH = DbHelper.getInstance(_context);
        DbH.loadUserIconsIntoCache();

        while (!canceled()) {

            synchronized (st_lck) {
                lastRegisterError = null;
                superUserAuthorized = false;
            }

            onConnecting();
            setVisible(0, 2); // Cleanup
            setVisible(2, 1);


            try {
                {
                    SuplaCfg cfg = new SuplaCfg();
                    cfgInit(cfg);

                    Preferences prefs = new Preferences(_context);

                    cfg.Host = prefs.getServerAddress();
                    cfg.clientGUID = prefs.getClientGUID();
                    cfg.AuthKey = prefs.getAuthKey();
                    cfg.Name = Build.MANUFACTURER + " " + Build.MODEL;
                    cfg.SoftVer = "Android" + Build.VERSION.RELEASE + "/" + BuildConfig.VERSION_NAME;

                    if (prefs.isAdvancedCfg()) {
                        cfg.AccessID = prefs.getAccessID();
                        cfg.AccessIDpwd = prefs.getAccessIDpwd();

                        if (regTryCounter >= 2) {
                            // supla-server v1.0 for Raspberry Compatibility fix
                            prefs.setPreferedProtocolVersion(4);
                        }

                    } else {
                        cfg.Email = prefs.getEmail();
                        if (!cfg.Email.isEmpty() && cfg.Host.isEmpty()) {
                            cfg.Host = autodiscoverGetHost(cfg.Email);

                            if (cfg.Host.isEmpty()) {
                                onConnError(new SuplaConnError(
                                        SuplaConst.SUPLA_RESULTCODE_HOSTNOTFOUND));
                            } else {
                                prefs.setServerAddress(cfg.Host);
                            }
                        }

                        cfg.setPassword(oneTimePassword);
                    }

                    oneTimePassword = "";
                    cfg.protocol_version = prefs.getPreferedProtocolVersion();
                    init(cfg);
                }

                if (connect()) {

                    //noinspection StatementWithEmptyBody
                    while (!canceled() && iterate()) {
                    }

                    if (!canceled()) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ignored) {
                        }
                    }

                }

            } finally {
                free();
            }


            if (!canceled()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            }
        }

        SuplaApp.getApp().OnSuplaClientFinished(this);
        Trace.d(log_tag, "SuplaClient Finished");
    }
}
