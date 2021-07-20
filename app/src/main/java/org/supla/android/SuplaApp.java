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

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;

import org.supla.android.db.DbHelper;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaClientMessageHandler;
import org.supla.android.lib.SuplaClientMsg;
import org.supla.android.lib.SuplaOAuthToken;
import org.supla.android.restapi.SuplaRestApiClientTask;

import java.util.ArrayList;
import org.supla.android.cfg.CfgRepository;
import org.supla.android.cfg.PrefsCfgRepositoryImpl;
import org.supla.android.data.presenter.TemperaturePresenter;
import org.supla.android.data.presenter.TemperaturePresenterImpl;

public class SuplaApp extends Application implements SuplaClientMessageHandler.OnSuplaClientMessageListener,
    TemperaturePresenterFactory {

    private static final Object _lck1 = new Object();
    private static final Object _lck3 = new Object();
    private static SuplaClient _SuplaClient = null;
    private static SuplaApp _SuplaApp = null;
    private Typeface mTypefaceQuicksandRegular;
    private Typeface mTypefaceQuicksandLight;
    private Typeface mTypefaceOpenSansRegular;
    private Typeface mTypefaceOpenSansBold;
    private SuplaOAuthToken _OAuthToken;
    private ArrayList<SuplaRestApiClientTask> _RestApiClientTasks = new ArrayList<SuplaRestApiClientTask>();
    private static long lastWifiScanTime;

    public SuplaApp() {
        SuplaClientMessageHandler.getGlobalInstance().registerMessageListener(this);
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

        if (v != null)
            v.vibrate(100);
    }

    public SuplaClient SuplaClientInitIfNeed(Context context, String oneTimePassword) {

        SuplaClient result;

        synchronized (_lck1) {

            if (_SuplaClient == null || _SuplaClient.canceled()) {
                _SuplaClient = new SuplaClient(context, oneTimePassword);
                _SuplaClient.start();
            }

            result = _SuplaClient;
        }

        return result;
    }

    public SuplaClient SuplaClientInitIfNeed(Context context) {
        return SuplaClientInitIfNeed(context, null);
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

            if (_SuplaClient == sender) {
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

    public SuplaOAuthToken RegisterRestApiClientTask(SuplaRestApiClientTask task) {
        SuplaOAuthToken result = null;
        synchronized (_lck3) {
            if (_OAuthToken != null && _OAuthToken.isAlive()) {
                result = new SuplaOAuthToken(_OAuthToken);
            }

            _RestApiClientTasks.add(task);
            //Trace.d("RegisterRestApiClientTask",
            //        "taskCount: "+Integer.toString(_RestApiClientTasks.size()));
        }

        return result;
    }

    public void UnregisterRestApiClientTask(SuplaRestApiClientTask task) {
        synchronized (_lck3) {
            _RestApiClientTasks.remove(task);

            //Trace.d("UnregisterRestApiClientTask",
            //        "taskCount: "+Integer.toString(_RestApiClientTasks.size()));
        }
    }

    public void CancelAllRestApiClientTasks(boolean mayInterruptIfRunning) {
        synchronized (_lck3) {
            for (int a = 0; a < _RestApiClientTasks.size(); a++) {
                _RestApiClientTasks.get(a).cancel(mayInterruptIfRunning);
            }
        }
    }

    public void initTypefaceCollection(Context context) {
        if (context == null) {
            return;
        }

        if (mTypefaceQuicksandRegular == null) {
            mTypefaceQuicksandRegular = Typeface.createFromAsset(
                    context.getAssets(), "fonts/Quicksand-Regular.ttf");
        }

        if (mTypefaceQuicksandLight == null) {
            mTypefaceQuicksandLight = Typeface.createFromAsset(
                    context.getAssets(), "fonts/Quicksand-Light.ttf");
        }

        if (mTypefaceOpenSansRegular == null) {
            mTypefaceOpenSansRegular = Typeface.createFromAsset(
                    context.getAssets(), "fonts/OpenSans-Regular.ttf");
        }

        if (mTypefaceOpenSansBold == null) {
            mTypefaceOpenSansBold = Typeface.createFromAsset(
                    context.getAssets(), "fonts/OpenSans-Bold.ttf");
        }
    }

    public Typeface getTypefaceQuicksandRegular() {
        return mTypefaceQuicksandRegular;
    }

    public Typeface getTypefaceQuicksandLight() {
        return mTypefaceQuicksandLight;
    }

    public Typeface getTypefaceOpenSansRegular() {
        return mTypefaceOpenSansRegular;
    }

    public Typeface getTypefaceOpenSansBold() {
        return mTypefaceOpenSansBold;
    }

    public static boolean wifiStartScan(WifiManager manager) {
        if (manager.startScan()) {
            lastWifiScanTime = System.currentTimeMillis();
            return true;
        }

        return false;
    }

    public static long getSecondsSinceLastWiFiScan() {
        long result = System.currentTimeMillis() - lastWifiScanTime;
        result/=1000;
        return result;
    }

    @Override
    public void onSuplaClientMessageReceived(SuplaClientMsg msg) {
        if (msg.getType() == SuplaClientMsg.onOAuthTokenRequestResult) {
            synchronized (_lck3) {
                _OAuthToken = msg.getOAuthToken();
                for (int a = 0; a < _RestApiClientTasks.size(); a++) {
                    _RestApiClientTasks.get(a).setToken(_OAuthToken);
                }
            }
        }
    }

    public CfgRepository getCfgRepository() {
        return new PrefsCfgRepositoryImpl(this);
    }

    public TemperaturePresenter getTemperaturePresenter() {
        CfgRepository repo = getCfgRepository();
        return new TemperaturePresenterImpl(getCfgRepository().getCfg());
    }
}
