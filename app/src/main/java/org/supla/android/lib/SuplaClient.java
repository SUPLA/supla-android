package org.supla.android.lib;

/*
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

 Author: Przemyslaw Zygmunt p.zygmunt@acsoftware.pl [AC SOFTWARE]
 */

import android.content.Context;
import android.os.Build;

import org.supla.android.BuildConfig;
import org.supla.android.Trace;
import org.supla.android.db.DbHelper;

import java.util.Arrays;

public class SuplaClient extends Thread {

    private long _supla_client = 0;
    private boolean _canceled = false;
    private static final String log_tag = "SuplaClientThread";
    private Context _context;
    private DbHelper DbH = null;

    public native void CfgInit(SuplaCfg cfg);
    private native long scInit(SuplaCfg cfg);
    private native void scFree(long _supla_client);
    private native int scGetId(long _supla_client);
    private native boolean scConnect(long _supla_client);
    private native boolean scConnected(long _supla_client);
    private native boolean scRegistered(long _supla_client);
    private native void scDisconnect(long _supla_client);
    private native boolean scIterate(long _supla_client, int wait_usec);
    private native boolean scOpen(long _supla_client, int ChannelID, int Open);

    public SuplaClient(Context context) {

        super();
        _context = context;

    }

    protected void finalize ()  {
        Disconnect();
        Free();
    }


    private boolean Init(SuplaCfg cfg) {

        if ( _supla_client  == 0 ) {
            _supla_client = scInit(cfg);
        }

        return _supla_client != 0;
    }

    private void Free() {
        if ( _supla_client != 0 )
            scFree(_supla_client);
        _supla_client = 0;
    }

    public int GetId() {
        return _supla_client != 0 ? scGetId(_supla_client) : 0;
    }

    private boolean Connect() {
        return _supla_client != 0 ? scConnect(_supla_client) : false;
    }

    public boolean Connected() {
        return _supla_client != 0 ? scConnected(_supla_client) : false;
    }

    public boolean Registered() {
        return _supla_client != 0 ? scRegistered(_supla_client) : false;
    }

    public void Disconnect() {
        if ( _supla_client != 0 )
            scDisconnect(_supla_client);
    }

    private boolean Iterate(int wait_usec) {
        return _supla_client != 0 && scIterate(_supla_client, wait_usec) == true ? true : false;
    }

    public boolean Open(int ChannelID, int Open) {
        return _supla_client != 0 ? scOpen(_supla_client, ChannelID, Open) : false;
    }

    private void onVersionError(SuplaVersionError versionError) {
        Trace.d(log_tag, new Integer(versionError.Version).toString() + "," + new Integer(versionError.RemoteVersionMin).toString() + "," + new Integer(versionError.RemoteVersion).toString());
    }

    private void onConnecting() {
        Trace.d(log_tag, "Connecting");
    }

    private void onConnected() {
        Trace.d(log_tag, "Connected");
    }

    private void onDisconnected() {
        Trace.d(log_tag, "Disconnected");
    }

    private void onRegistering() {
        Trace.d(log_tag, "Registering");
    }

    private void onRegistered(SuplaRegisterResult registerResult) {
        Trace.d(log_tag, "Registered");
    }

    private void onRegisterError(SuplaRegisterError registerError) {
        Trace.d(log_tag, registerError.codeToString(_context));
    }

    private void LocationUpdate(SuplaLocation location) {

        if ( DbH.updateLocation(location) ) {
            Trace.d(log_tag, "Location updated");
        }

    }

    private void ChannelUpdate(SuplaChannel channel) {

        if ( DbH.updateChannel(channel) ) {
            Trace.d(log_tag, "Channel updated");
        }

    }

    private void ChannelValueUpdate(SuplaChannelValueUpdate channelValueUpdate) {



    }

    private void onEvent(SuplaEvent event) {
        Trace.d(log_tag, "Event");
    }

    public synchronized boolean canceled() {
        return _canceled;
    }

    public synchronized void cancel() {
        _canceled = true;
    }

    public void run() {

        DbH = new DbHelper(_context);

        while(!canceled()) {

            onConnecting();


            {
                SuplaCfg cfg = new SuplaCfg();
                CfgInit(cfg);

                Preferences prefs = new Preferences(_context);

                cfg.Host = prefs.getServerAddress();
                cfg.clientGUID = prefs.getClientGUID();
                cfg.AccessID = prefs.getAccessID();
                cfg.AccessIDpwd = prefs.getAccessIDpwd();
                cfg.Name = Build.MANUFACTURER + " " + Build.MODEL;
                cfg.SoftVer = "Android"+Build.VERSION.RELEASE+"/"+ BuildConfig.VERSION_NAME;

                Init(cfg);
            }

            if ( Connect() ) {

                while( !canceled() && Iterate(100000) ) {};

                if ( canceled() == false ) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {}
                }

            }


            if ( canceled() == false ) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {}
            }
        }

    }

    static {
        System.loadLibrary("suplaclient");
    }
}
