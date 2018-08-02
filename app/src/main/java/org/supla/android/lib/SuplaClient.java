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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.supla.android.BuildConfig;
import org.supla.android.SuplaApp;
import org.supla.android.Trace;
import org.supla.android.db.DbHelper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


@SuppressWarnings("JniMissingFunction")
public class SuplaClient extends Thread {

    private int _client_id;
    private long _supla_client_ptr = 0;
    private long _supla_client_ptr_counter = 0;
    private boolean _canceled = false;
    private static final String log_tag = "SuplaClientThread";
    private Context _context;
    private DbHelper DbH = null;
    private android.os.Handler _msgHandler;
    private final Object msgh_lck = new Object();
    private final Object sc_lck = new Object();
    private static final Object st_lck = new Object();
    private static SuplaRegisterError lastRegisterError = null;
    private int regTryCounter = 0; // supla-server v1.0 for Raspberry Compatibility fix

    public native void CfgInit(SuplaCfg cfg);

    private native long scInit(SuplaCfg cfg);

    private native void scFree(long _supla_client);

    private native int scGetId(long _supla_client);

    private native boolean scConnect(long _supla_client);

    private native boolean scConnected(long _supla_client);

    private native boolean scRegistered(long _supla_client);

    private native void scDisconnect(long _supla_client);

    private native boolean scIterate(long _supla_client, int wait_usec);

    private native boolean scOpen(long _supla_client, int ID, int Group, int Open);

    private native boolean scSetRGBW(long _supla_client, int ID, int Group, int Color, int ColorBrightness, int Brightness);

    private native boolean scGetRegistrationEnabled(long _supla_client);

    private native int scGetProtoVersion(long _supla_client);

    private native int scGetMaxProtoVersion(long _supla_client);

    private native boolean scOAuthTokenRequest(long _supla_client);

    public SuplaClient(Context context) {

        super();
        _context = context;

    }


    public void setMsgHandler(Handler msgHandler) {

        synchronized (msgh_lck) {
            _msgHandler = msgHandler;
        }
    }

    public void sendMessage(SuplaClientMsg msg) {

        if (canceled()) return;

        synchronized (msgh_lck) {
            if (_msgHandler != null)
                _msgHandler.sendMessage(_msgHandler.obtainMessage(msg.getType(), msg));
        }
    }

    private boolean Init(SuplaCfg cfg) {

        boolean result;

        synchronized (sc_lck) {
            if (_supla_client_ptr == 0) {
                _supla_client_ptr_counter = 0;
                _supla_client_ptr = scInit(cfg);
            }

            result = _supla_client_ptr != 0;
        }

        return result;
    }

    private long LockClientPtr() {
        long result = 0;

        synchronized (sc_lck) {
            if (_supla_client_ptr != 0) {
                _supla_client_ptr_counter++;
                result = _supla_client_ptr;
            }
        }

        return result;
    }

    private boolean UnlockClientPtr() {

        boolean result = false;

        synchronized (sc_lck) {
            if (_supla_client_ptr != 0
                    && _supla_client_ptr_counter > 0) {

                _supla_client_ptr_counter--;
                result = true;
            }
        }

        return result;
    }

    private void Free() {

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
                } catch (InterruptedException e) {
                }
            }
        }


    }

    public int GetId() {

        int result;

        LockClientPtr();
        try {
            result = _supla_client_ptr != 0 ? scGetId(_supla_client_ptr) : 0;
        } finally {
            UnlockClientPtr();
        }

        return result;
    }

    private boolean Connect() {

        boolean result = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            LockClientPtr();
            try {
                result = _supla_client_ptr != 0 && scConnect(_supla_client_ptr);
            } finally {
                UnlockClientPtr();
            }
        }

        return result;
    }


    public void Reconnect() {
        if (Connected()) Disconnect();
    }

    public boolean Connected() {

        boolean result;

        LockClientPtr();
        try {
            result = _supla_client_ptr != 0 && scConnected(_supla_client_ptr);
        } finally {
            UnlockClientPtr();
        }

        return result;
    }

    public boolean Registered() {

        boolean result;

        synchronized (sc_lck) {
            result = _supla_client_ptr != 0 && scRegistered(_supla_client_ptr);
        }

        return result;
    }

    public void Disconnect() {

        LockClientPtr();
        try {
            if (_supla_client_ptr != 0) {
                scDisconnect(_supla_client_ptr);
            }
        } finally {
            UnlockClientPtr();
        }

    }

    private boolean Iterate(int wait_usec) {

        return _supla_client_ptr != 0 && scIterate(_supla_client_ptr, wait_usec);

    }

    public boolean Open(int ID, boolean Group, int Open) {

        boolean result;

        LockClientPtr();
        try {
            result = _supla_client_ptr != 0 && scOpen(_supla_client_ptr, ID, Group ? 1 : 0, Open);
        } finally {
            UnlockClientPtr();
        }

        return result;
    }

    public boolean Open(int ChannelID, int Open) {
        return Open(ChannelID, false, Open);
    }

    public boolean setRGBW(int ID, boolean Group, int Color, int ColorBrightness, int Brightness) {

        boolean result;

        LockClientPtr();
        try {
            result = _supla_client_ptr != 0 && scSetRGBW(_supla_client_ptr, ID, Group ? 1 : 0, Color, ColorBrightness, Brightness);
        } finally {
            UnlockClientPtr();
        }

        return result;
    }

    public boolean setRGBW(int ChannelID, int Color, int ColorBrightness, int Brightness) {
        return setRGBW(ChannelID, false, Color, ColorBrightness, Brightness);
    }

    public boolean GetRegistrationEnabled() {

        boolean result;

        LockClientPtr();
        try {
            result = _supla_client_ptr != 0 && scGetRegistrationEnabled(_supla_client_ptr);
        } finally {
            UnlockClientPtr();
        }

        return result;
    }

    public int GetProtoVersion() {

        int result;

        synchronized (sc_lck) {
            result = _supla_client_ptr != 0 ? scGetProtoVersion(_supla_client_ptr) : 0;
        }

        return result;
    }

    public int GetMaxProtoVersion() {

        int result;

        LockClientPtr();
        try {
            result = _supla_client_ptr != 0 ? scGetMaxProtoVersion(_supla_client_ptr) : 0;
        } finally {
            UnlockClientPtr();
        }

        return result;
    }


    public boolean OAuthTokenRequest() {

        boolean result;

        LockClientPtr();
        try {
            result = _supla_client_ptr != 0 ? scOAuthTokenRequest(_supla_client_ptr) : false;
        } finally {
            UnlockClientPtr();
        }

        return result;
    }

    private void onVersionError(SuplaVersionError versionError) {
        Trace.d(log_tag, Integer.valueOf(versionError.Version).toString() + "," + Integer.valueOf(versionError.RemoteVersionMin).toString() + "," + Integer.valueOf(versionError.RemoteVersion).toString());

        regTryCounter = 0;
        Preferences prefs = new Preferences(_context);


        if ((prefs.isAdvancedCfg() || versionError.RemoteVersion >= 7)
                && versionError.RemoteVersion >= 5
                && versionError.Version > versionError.RemoteVersion
                && prefs.getPreferedProtocolVersion() != versionError.RemoteVersion) {

            // set prefered to lower
            prefs.setPreferedProtocolVersion(versionError.RemoteVersion);
            Reconnect();
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
        Trace.d(log_tag, "Connected");

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

        Trace.d(log_tag, "Registered");

        regTryCounter = 0;
        Preferences prefs = new Preferences(_context);

        if (GetMaxProtoVersion() > 0
                && prefs.getPreferedProtocolVersion() < GetMaxProtoVersion()
                && registerResult.Version > prefs.getPreferedProtocolVersion()
                && registerResult.Version <= GetMaxProtoVersion()) {
            prefs.setPreferedProtocolVersion(registerResult.Version);
        }

        _client_id = registerResult.ClientID;

        Trace.d(log_tag, "Protocol Version=" + Integer.toString(registerResult.Version));
        Trace.d(log_tag, "registerResult.ChannelCount=" + Integer.toString(registerResult.ChannelCount));
        Trace.d(log_tag, "registerResult.ChannelGroupCount=" + Integer.toString(registerResult.ChannelGroupCount));

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

        Trace.d(log_tag, "SuplaMinVersionRequired - CallType: " + Long.toString(minVersionRequired.CallType) + " MinVersion: " + Integer.toString(minVersionRequired.MinVersion));

    }

    private void LocationUpdate(SuplaLocation location) {
        if (DbH.updateLocation(location)) {
            Trace.d(log_tag, "Location updated");
            onDataChanged();
        }

    }

    private void ChannelUpdate(SuplaChannel channel) {

        boolean _DataChanged = false;

        Trace.d(log_tag, "Channel Function" + Integer.toString(channel.Func) + "  channel ID: " + Integer.toString(channel.Id) + " channel Location ID: " + Integer.toString(channel.LocationID) + " OnLine: " + Boolean.toString(channel.OnLine) + " AltIcon: " + Integer.toString(channel.AltIcon));

        // Update channel value before update the channel
        if (DbH.updateChannelValue(channel.Value, channel.Id, channel.OnLine)) {
            _DataChanged = true;
        }

        if (DbH.updateChannel(channel)) {
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

    private void OnChannelGroupValueChanged() {
        Integer[] groupIds = DbH.updateChannelGroups();
        for (int a = 0; a < groupIds.length; a++) {

            int groupId = groupIds[a].intValue();
            onDataChanged(0, groupId);
        }
    }

    private void ChannelGroupUpdate(SuplaChannelGroup channel_group) {

        boolean _DataChanged = false;

        Trace.d(log_tag, "Channel Group Function" + Integer.toString(channel_group.Func) + "  group ID: " + Integer.toString(channel_group.Id) + " group Location ID: " + Integer.toString(channel_group.LocationID) + " AltIcon: " + Integer.toString(channel_group.AltIcon));

        if (DbH.updateChannelGroup(channel_group)) {
            _DataChanged = true;
        }

        if (channel_group.EOL
                && DbH.setChannelGroupsVisible(0, 2)) {
            _DataChanged = true;
        }

        if (channel_group.EOL) {
            OnChannelGroupValueChanged();
        }

        if (_DataChanged) {
            Trace.d(log_tag, "Channel Group updated");
            onDataChanged(0, channel_group.Id);
        }

    }

    private void ChannelGroupRelationUpdate(SuplaChannelGroupRelation channelgroup_relation) {

        boolean _DataChanged = false;

        Trace.d(log_tag, "Channel Group Relation group ID: " + Integer.toString(channelgroup_relation.ChannelGroupID) + " channel ID: " + Integer.toString(channelgroup_relation.ChannelID));

        if (DbH.updateChannelGroupRelation(channelgroup_relation)) {
            _DataChanged = true;
        }

        if (channelgroup_relation.EOL
                && DbH.setChannelGroupRelationsVisible(0, 2)) {
            _DataChanged = true;
        }

        if (channelgroup_relation.EOL) {
            OnChannelGroupValueChanged();
        }

        if (_DataChanged) {
            Trace.d(log_tag, "Channel Group Relation updated");
            onDataChanged(0, channelgroup_relation.ChannelGroupID);
        }

    }

    private void ChannelValueUpdate(SuplaChannelValueUpdate channelValueUpdate) {

        if (DbH.updateChannelValue(channelValueUpdate)) {
            Trace.d(log_tag, "Channel id" + Integer.toString(channelValueUpdate.Id) + " value updated" + " OnLine: " + Boolean.toString(channelValueUpdate.OnLine));
            onDataChanged(channelValueUpdate.Id, 0);
        }

        if (channelValueUpdate.EOL) {
            OnChannelGroupValueChanged();
        }
    }

    private void ChannelExtendedValueUpdate(SuplaChannelExtendedValueUpdate channelExtendedValueUpdate) {

        if (DbH.updateChannelExtendedValue(channelExtendedValueUpdate.Value, channelExtendedValueUpdate.Id)) {
            onDataChanged(channelExtendedValueUpdate.Id, 0);
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
        Trace.d(log_tag, "OAuthToken");

        SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onOAuthTokenRequestResult);
        msg.setOAuthToken(token);
        sendMessage(msg);
    }

    private void onDataChanged(int ChannelId, int GroupId) {

        SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onDataChanged);
        msg.setChannelId(ChannelId);
        msg.setChannelGroupId(GroupId);

        sendMessage(msg);

    }

    private void onDataChanged() {
        onDataChanged(0, 0);
    }

    public synchronized boolean canceled() {
        return _canceled;
    }

    public synchronized void cancel() {
        _canceled = true;
    }

    public static SuplaRegisterError getLastRegisterError() {

        SuplaRegisterError result;

        synchronized (st_lck) {
            result = lastRegisterError;
        }

        return result;
    }

    private String autodiscoverGetHost(String email) {

        String result = "";

        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("autodiscover.supla.org")
                    .appendPath("users")
                    .appendPath(email);

            request.setURI(new URI(builder.build().toString()));
            HttpResponse response = client.execute(request);

            if (response != null) {
                String json = EntityUtils.toString(response.getEntity());

                JSONTokener tokener = new JSONTokener(json);
                JSONObject jsonResult = new JSONObject(tokener);

                if (jsonResult.getString("email").equals(email)) {
                    result = jsonResult.getString("server");
                }
            }

        } catch (URISyntaxException | IOException | JSONException e) {
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

        DbH = new DbHelper(_context);

        while (!canceled()) {

            synchronized (st_lck) {
                lastRegisterError = null;
            }

            onConnecting();
            setVisible(0,2); // Cleanup
            setVisible(2,1);


            try {
                {
                    SuplaCfg cfg = new SuplaCfg();
                    CfgInit(cfg);

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
                            prefs.setPreferedProtocolVersion(4); // supla-server v1.0 for Raspberry Compatibility fix
                        }

                    } else {
                        cfg.Email = prefs.getEmail();
                        if (!cfg.Email.isEmpty() && cfg.Host.isEmpty()) {
                            cfg.Host = autodiscoverGetHost(cfg.Email);

                            if (!cfg.Host.isEmpty()) {
                                prefs.setServerAddress(cfg.Host);
                            }
                        }

                    }

                    cfg.protocol_version = prefs.getPreferedProtocolVersion();
                    Init(cfg);


                }

                if (Connect()) {

                    while (!canceled() && Iterate(100000)) {}

                    if (!canceled()) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                        }
                    }

                }

            } finally {
                Free();
            }


            if (!canceled()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        }

        SuplaApp.getApp().OnSuplaClientFinished(this);
        Trace.d(log_tag, "SuplaClient Finished");
    }

    static {
        System.loadLibrary("suplaclient");
    }
}
