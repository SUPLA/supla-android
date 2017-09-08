package org.supla.android;

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

 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.supla.android.lib.Preferences;
import org.supla.android.lib.SuplaRegistrationEnabled;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class AddWizardActivity extends NavigationActivity implements ESPConfigureTask.AsyncResponse {

    private final int STEP_NONE                          = 0;
    private final int STEP_CHECK_WIFI                    = 1;
    private final int STEP_CHECK_REGISTRATION_ENABLED    = 2;
    private final int STEP_SCAN                          = 3;
    private final int STEP_CONNECT                       = 4;
    private final int STEP_CONFIGURE                     = 5;
    private final int STEP_RECONNECT                     = 6;
    private final int STEP_DONE                          = 7;

    private Timer watchDog;

    private int step;
    private Date step_time;

    private BroadcastReceiver scanResultReceiver;
    private BroadcastReceiver stateChangedReceiver;
    private Button btnStart;
    private EditText edList;
    private EditText edPassword;

    private ESPConfigureTask espConfigTask = null;
    private WifiManager manager = null;
    private String SSID;
    private int NetworkID;

    private int iodev_NetworkID = -1;
    private String iodev_SSID = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        watchDog = new Timer();

        manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        setContentView(R.layout.activity_wizard);

        btnStart = (Button)findViewById(R.id.wizard_start);
        btnStart.setOnClickListener(this);

        edList = (EditText)findViewById(R.id.wizard_list);
        edPassword = (EditText)findViewById(R.id.wizard_pwd);

        showMenuBar();
        RegisterMessageHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        edList.setText("", EditText.BufferType.NORMAL);

        watchDog.scheduleAtFixedRate( new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable()
                {
                    public void run()
                    {

                        int timeout = 0;

                        switch(step) {
                            case STEP_CHECK_WIFI:
                                timeout = 2;
                                break;
                            case STEP_CHECK_REGISTRATION_ENABLED:
                                timeout = 5;
                                break;
                            case STEP_SCAN:
                            case STEP_CONNECT:
                            case STEP_CONFIGURE:
                            case STEP_RECONNECT:
                                timeout = 15;
                                break;
                        }

                        if ( timeout > 0
                                && step_time != null
                                &&  (System.currentTimeMillis() - step_time.getTime())/1000 >= timeout  ) {

                            onWatchDogTimeout();
                        }

                    }
                });
            }
        }, 0, 1000 );


    };

    @Override
    protected void onPause() {
        super.onPause();

        watchDog.cancel();

        removeConfigTask();
        unregisterReceivers();
        removeIODeviceNetwork();
    };

    private void addLogLine(String line) {
        edList.setText(edList.getText()+"\n"+line, EditText.BufferType.NORMAL);
    }

    private void setStep(int step) {

        if ( step == STEP_DONE || step == STEP_NONE ) {
            step_time = null;
        } else {
            step_time = new Date();
        }

        this.step = step;
    }

    private void onWatchDogTimeout() {

        addLogLine("Wathdog timeout: " + Integer.toString(step));

        switch(step) {
            case STEP_CHECK_WIFI:
                break;
            case STEP_CHECK_REGISTRATION_ENABLED:
                break;
            case STEP_SCAN:
                break;
            case STEP_CONNECT:
                break;
            case STEP_CONFIGURE:
                break;
            case STEP_RECONNECT:
                break;
        }

        setStep(STEP_NONE);

        removeConfigTask();
        unregisterReceivers();
        removeIODeviceNetwork();


    }

    private void unregisterReceivers() {

        if ( stateChangedReceiver != null ) {
            unregisterReceiver(stateChangedReceiver);
            stateChangedReceiver = null;
        }

        if ( scanResultReceiver != null ) {
            unregisterReceiver(scanResultReceiver);
            scanResultReceiver = null;
        }

    }

    private void removeConfigTask() {

        if ( espConfigTask != null ) {
            espConfigTask.cancel(true);
            espConfigTask = null;
        }

    }

    private boolean checkWiFi() {

        SSID = "";
        NetworkID = -1;

        if (manager.isWifiEnabled()) {

            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                SSID = wifiInfo.getSSID();
                NetworkID = wifiInfo.getNetworkId();

                if (SSID.startsWith("\"") && SSID.endsWith("\"")){
                    SSID = SSID.substring(1, SSID.length()-1);
                }
            }
        }

        if ( !SSID.isEmpty() )
            return manager.setWifiEnabled(true);

        return false;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if ( v != btnStart ) return;

        addLogLine("Start");

        setStep(STEP_CHECK_WIFI);

        if ( checkWiFi() ) {

            setStep(STEP_CHECK_REGISTRATION_ENABLED);
            SuplaApp.getApp().getSuplaClient().GetRegistrationEnabled();

        } else {
            setStep(STEP_NONE);
        }

    }

    protected void OnRegistrationEnabled(SuplaRegistrationEnabled registrationEnabled) {

        super.OnRegistrationEnabled(registrationEnabled);

        if ( registrationEnabled.IsIODeviceRegistrationEnabled() ) {

            setStep(STEP_SCAN);
            startScan();

        } else {

            setStep(STEP_NONE);
            addLogLine("Rejestracja wyłączona");
        }


    };



    private void startScan() {

        unregisterReceivers();

        scanResultReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context c, Intent i){

                unregisterReceiver(scanResultReceiver);
                scanResultReceiver = null;

                Pattern mPattern = Pattern.compile("\\-[A-Fa-f0-9]{12}$");
                List<ScanResult> scanned  =  manager.getScanResults();
                boolean match = false;

                for(int a=0;a<scanned.size();a++) {

                    iodev_SSID = scanned.get(a).SSID;
                    if ( ( iodev_SSID.startsWith("SUPLA-")
                            || iodev_SSID.startsWith("ZAMEL-")
                            || iodev_SSID.startsWith("NICE-") )
                            && mPattern.matcher(iodev_SSID).find() ) {

                        match = true;
                        connect();
                        break;
                    }

                }

                if ( !match ) {
                    addLogLine("Nic nie znaleziono!");
                    setStep(STEP_NONE);
                }

            }
        };

        IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        registerReceiver(scanResultReceiver, i);

        manager.startScan();

    }

    private void connect() {

        addLogLine("Łączenie z: "+iodev_SSID);
        setStep(STEP_CONNECT);

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + iodev_SSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        iodev_NetworkID = manager.addNetwork(conf);

        if ( iodev_NetworkID == -1 ) {
            addLogLine("Nie można dodać sieci");
            return;
        }

        unregisterReceivers();
        removeConfigTask();

        espConfigTask = new ESPConfigureTask();
        espConfigTask.setDelegate(this);

        manager.disconnect();

        final Preferences prefs = new Preferences(this);

        stateChangedReceiver = new BroadcastReceiver(){
                @Override
                public void onReceive(Context c, Intent i){

                    NetworkInfo info = i.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                    if ( info != null
                            && info.isConnected() ) {

                        WifiInfo wifiInfo = manager.getConnectionInfo();
                        if (wifiInfo != null && wifiInfo.getNetworkId() == iodev_NetworkID ) {

                            unregisterReceiver(stateChangedReceiver);
                            stateChangedReceiver = null;

                            setStep(STEP_CONFIGURE);
                            espConfigTask.execute(SSID, edPassword.getText().toString(), prefs.getServerAddress(), prefs.getEmail());

                        }

                    }


                }
        };

        IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        registerReceiver(stateChangedReceiver, i);

        manager.enableNetwork(iodev_NetworkID, true);
        manager.reconnect();


    }


    public void removeIODeviceNetwork() {

        if ( iodev_NetworkID != -1 ) {

            manager.disconnect();
            manager.removeNetwork(iodev_NetworkID);
            iodev_NetworkID = -1;

            manager.enableNetwork(NetworkID, true);
            manager.reconnect();
        }

    }

    @Override
    public void onBackPressed() {
        showMain(this);
        finish();
    }

    @Override
    public void espConfigFinished(final ESPConfigureTask.ConfigResult result) {

        unregisterReceivers();

        setStep(STEP_RECONNECT);

        stateChangedReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context c, Intent i){

                NetworkInfo info = i.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                if ( info != null
                        && info.isConnected() ) {

                    WifiInfo wifiInfo = manager.getConnectionInfo();
                    if (wifiInfo != null && wifiInfo.getNetworkId() == NetworkID ) {

                        unregisterReceiver(stateChangedReceiver);
                        stateChangedReceiver = null;

                        setStep(STEP_DONE);

                        addLogLine("FINISH");
                        addLogLine("Last state: "+result.deviceLastState);
                    }

                }

            }
        };

        IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(stateChangedReceiver, i);
        removeIODeviceNetwork();

    }
}
