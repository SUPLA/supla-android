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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.supla.android.lib.SuplaRegisterError;
import org.supla.android.lib.SuplaRegistrationEnabled;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class AddWizardActivity extends NavigationActivity implements ESPConfigureTask.AsyncResponse, AdapterView.OnItemSelectedListener, View.OnTouchListener {

    private final int WIZARD_PERMISSIONS_REQUEST = 1;

    private final int STEP_NONE = 0;
    private final int STEP_CHECK_WIFI = 1;
    private final int STEP_CHECK_REGISTRATION_ENABLED_TRY1 = 2;
    private final int STEP_CHECK_REGISTRATION_ENABLED_TRY2 = 3;
    private final int STEP_SCAN = 4;
    private final int STEP_CONNECT = 5;
    private final int STEP_CONFIGURE = 6;
    private final int STEP_RECONNECT = 7;
    private final int STEP_DONE = 8;

    private final int PAGE_STEP_1 = 1;
    private final int PAGE_STEP_2 = 2;
    private final int PAGE_STEP_3 = 3;
    private final int PAGE_ERROR = 4;
    private final int PAGE_DONE = 5;

    private final int SCAN_RETRY = 4;

    private int scanRetry;

    private int pageId;
    private Timer watchDog;
    private Timer blinkTimer;
    private Timer preloaderTimer;
    private short preloaderPos;

    private int step;
    private Date step_time;

    private BroadcastReceiver scanResultReceiver;
    private BroadcastReceiver stateChangedReceiver;

    private ESPConfigureTask espConfigTask = null;
    private WifiManager manager = null;
    private int NetworkID;

    private int iodev_NetworkID = -1;
    private String iodev_SSID = "";

    private View vStep1;
    private View vStep2;
    private View vStep3;
    private View vError;
    private View vDone;

    private Spinner spWifiList;
    private EditText edPassword;
    private CheckBox cbSavePassword;
    private Button btnViewPassword;

    private TextView tvStep2Info;

    private ImageView ivDot;

    private TextView tvErrorMsg;

    private TextView tvIODevName;
    private TextView tvIODevFirmware;
    private TextView tvIODevMAC;
    private TextView tvIODevLastState;

    private Button btnNext1;
    private Button btnNext2;
    private Button btnNext3;

    private RelativeLayout rlStepContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        setContentView(R.layout.activity_wizard);

        rlStepContent = findViewById(R.id.wizard_step_content);

        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Regular.ttf");

        vStep1 = Inflate(R.layout.add_wizard_step1, null);
        vStep1.setVisibility(View.GONE);
        rlStepContent.addView(vStep1);

        TextView tv = findViewById(R.id.wizard_step1_txt1);
        tv.setTypeface(type);

        tv = findViewById(R.id.wizard_step1_txt2);
        tv.setTypeface(type);

        vStep2 = Inflate(R.layout.add_wizard_step2, null);
        vStep2.setVisibility(View.GONE);
        rlStepContent.addView(vStep2);

        tv = findViewById(R.id.wizard_step2_txt1);
        tv.setTypeface(type);

        spWifiList = findViewById(R.id.wizard_wifi_list);
        spWifiList.setOnItemSelectedListener(this);

        tvStep2Info = findViewById(R.id.wizard_step2_txt3);
        tvStep2Info.setTypeface(type);

        edPassword = findViewById(R.id.wizard_password);
        cbSavePassword = findViewById(R.id.wizard_cb_save_pwd);
        btnViewPassword = findViewById(R.id.wizard_look_button);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            edPassword.setBackground(getResources().getDrawable(R.drawable.rounded_edittext_yellow));
        } else {
            edPassword.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext_yellow));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            spWifiList.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
        } else {
            spWifiList.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext));
        }

        cbSavePassword.setTypeface(type);
        cbSavePassword.setOnClickListener(this);

        btnViewPassword.setOnTouchListener(this);

        View.OnFocusChangeListener fcl = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        };

        edPassword.setOnFocusChangeListener(fcl);

        vStep3 = Inflate(R.layout.add_wizard_step3, null);
        vStep3.setVisibility(View.GONE);
        rlStepContent.addView(vStep3);

        tv = findViewById(R.id.wizard_step3_txt1);
        tv.setTypeface(type);

        tv = findViewById(R.id.wizard_step3_txt2);
        tv.setTypeface(type);

        tv = findViewById(R.id.wizard_step3_txt3);
        tv.setTypeface(type);

        ivDot = findViewById(R.id.wizard_dot);

        vError = Inflate(R.layout.add_wizard_error, null);
        vError.setVisibility(View.GONE);
        rlStepContent.addView(vError);

        tvErrorMsg = findViewById(R.id.wizard_error_txt);
        tvErrorMsg.setTypeface(type);

        vDone = Inflate(R.layout.add_wizard_done, null);
        vDone.setVisibility(View.GONE);
        rlStepContent.addView(vDone);

        tv = findViewById(R.id.wizard_done_txt1);
        tv.setTypeface(type);

        type = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");

        tv = findViewById(R.id.wizard_done_txt2);
        tv.setTypeface(type);

        tv = findViewById(R.id.wizard_done_txt3);
        tv.setTypeface(type);

        tv = findViewById(R.id.wizard_done_txt4);
        tv.setTypeface(type);

        tv = findViewById(R.id.wizard_done_txt5);
        tv.setTypeface(type);

        tv = findViewById(R.id.wizard_done_txt6);
        tv.setTypeface(type);

        tvIODevName = findViewById(R.id.wizard_done_iodev_name);
        tvIODevName.setTypeface(type);

        tvIODevFirmware = findViewById(R.id.wizard_done_iodev_firmware);
        tvIODevFirmware.setTypeface(type);

        tvIODevMAC = findViewById(R.id.wizard_done_iodev_mac);
        tvIODevMAC.setTypeface(type);

        tvIODevLastState = findViewById(R.id.wizard_done_iodev_laststate);
        tvIODevLastState.setTypeface(type);


        btnNext1 = findViewById(R.id.wizard_next1);
        btnNext1.setOnClickListener(this);

        btnNext2 = findViewById(R.id.wizard_next2);
        btnNext2.setOnClickListener(this);
        btnNext2.setTypeface(type);

        btnNext3 = findViewById(R.id.wizard_next3);
        btnNext3.setOnClickListener(this);

        showMenuBar();
        RegisterMessageHandler();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (pageId == PAGE_STEP_2
                && event.getAction() == MotionEvent.ACTION_DOWN
                && edPassword.hasFocus()) {

            edPassword.clearFocus();

        }

        return super.onTouchEvent(event);
    }

    private void setBtnEnabled(boolean enabled) {
        btnNext1.setEnabled(enabled);
        btnNext2.setEnabled(enabled);
        btnNext3.setEnabled(enabled);
    }

    @SuppressLint("SetTextI18n")
    private void showPage(int id) {

        setStep(STEP_NONE);
        vStep1.setVisibility(View.GONE);
        vStep2.setVisibility(View.GONE);
        vStep3.setVisibility(View.GONE);
        vError.setVisibility(View.GONE);
        vDone.setVisibility(View.GONE);
        setPreloaderVisible(false);
        setBtnEnabled(true);
        btnNext2.setText(R.string.next, TextView.BufferType.NORMAL);

        switch (id) {
            case PAGE_STEP_1:
                vStep1.setVisibility(View.VISIBLE);
                break;

            case PAGE_STEP_2:
                edPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                vStep2.setVisibility(View.VISIBLE);
                break;

            case PAGE_STEP_3:
                vStep3.setVisibility(View.VISIBLE);
                btnNext2.setText(R.string.start, TextView.BufferType.NORMAL);
                break;

            case PAGE_ERROR:
                vError.setVisibility(View.VISIBLE);
                btnNext2.setText("OK", TextView.BufferType.NORMAL);
                break;

            case PAGE_DONE:
                vDone.setVisibility(View.VISIBLE);
                btnNext2.setText("OK", TextView.BufferType.NORMAL);
                break;
        }

        pageId = id;
    }

    private void showError(String error_msg) {
        tvErrorMsg.setText(error_msg, TextView.BufferType.NORMAL);
        showPage(PAGE_ERROR);
    }

    private void showError(int msg_id) {
        showError(getResources().getString(msg_id));
    }

    private void showDone(ESPConfigureTask.ConfigResult result) {

        tvIODevName.setText(result.deviceName, TextView.BufferType.NORMAL);
        tvIODevFirmware.setText(result.deviceFirmwareVersion, TextView.BufferType.NORMAL);
        tvIODevMAC.setText(result.deviceMAC, TextView.BufferType.NORMAL);
        tvIODevLastState.setText(result.deviceLastState, TextView.BufferType.NORMAL);

        showPage(PAGE_DONE);
    }

    private void cleanUp() {

        if (watchDog != null) {
            watchDog.cancel();
            watchDog = null;
        }

        if (blinkTimer != null) {
            blinkTimer.cancel();
            blinkTimer = null;
        }


        removeConfigTask();
        unregisterReceivers();
        removeIODeviceNetwork();

    }

    private void setPreloaderVisible(boolean visible) {

        if (preloaderTimer != null) {
            preloaderPos = -1;
            preloaderTimer.cancel();
            preloaderTimer = null;
        }


        if (visible) {

            btnNext1.setBackgroundResource(R.drawable.btnnextr2);

            ViewGroup.LayoutParams params = btnNext1.getLayoutParams();
            params.width = getResources().getDimensionPixelSize(R.dimen.wizard_btnnextl_width);

            btnNext1.setLayoutParams(params);

            preloaderPos = 0;

            preloaderTimer = new Timer();
            preloaderTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {

                    runOnUiThread(new Runnable() {
                        public void run() {

                            if (preloaderPos == -1) {
                                return;
                            }

                            String txt = "";

                            for (int a = 0; a < 10; a++) {
                                txt += preloaderPos == a ? "|" : ".";
                            }


                            preloaderPos++;
                            if (preloaderPos > 9) {
                                preloaderPos = 0;
                            }


                            btnNext2.setText(txt, TextView.BufferType.NORMAL);


                        }
                    });
                }
            }, 0, 100);

        } else {

            btnNext1.setBackgroundResource(R.drawable.btnnextr);
            ViewGroup.LayoutParams params = btnNext1.getLayoutParams();
            params.width = getResources().getDimensionPixelSize(R.dimen.wizard_btnnextr_width);
            btnNext1.setLayoutParams(params);

        }

    }

    private boolean internetWiFi() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected() && ni.getTypeName().equalsIgnoreCase("WIFI");
        }

        return false;
    }

    public String getApplicationName() {
        ApplicationInfo applicationInfo = getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : getString(stringId);
    }

    @Override
    protected void onResume() {
        super.onResume();

        cleanUp();

        Preferences prefs = new Preferences(this);

        if (prefs.isAdvancedCfg()) {

            showError(R.string.add_wizard_is_not_available);
            return;

        } else if (!internetWiFi()) {

            showError(R.string.wizard_no_internetwifi);
            return;

        } else if (SuplaApp.getApp().getSuplaClient() != null) {
            int version = SuplaApp.getApp().getSuplaClient().GetProtoVersion();

            if (version > 0 && version < 7) {
                showError(R.string.wizard_server_compat_error);
                return;
            }
        }

        watchDog = new Timer();
        watchDog.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    public void run() {

                        int timeout = 0;

                        switch (step) {
                            case STEP_CHECK_WIFI:
                                timeout = 10;
                                break;
                            case STEP_CHECK_REGISTRATION_ENABLED_TRY1:
                            case STEP_CHECK_REGISTRATION_ENABLED_TRY2:
                                timeout = 10;
                                break;
                            case STEP_SCAN:
                                timeout = 50;
                                break;
                            case STEP_CONNECT:
                            case STEP_CONFIGURE:
                                timeout = 40;
                                break;
                            case STEP_RECONNECT:
                                timeout = 25;
                                break;
                        }

                        if (timeout > 0
                                && step_time != null
                                && (System.currentTimeMillis() - step_time.getTime()) / 1000 >= timeout) {

                            onWatchDogTimeout();
                        }

                    }
                });
            }
        }, 0, 1000);


        blinkTimer = new Timer();
        blinkTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    public void run() {

                        if (pageId == PAGE_STEP_3) {
                            ivDot.setVisibility(ivDot.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                        }

                    }
                });
            }
        }, 0, 100);


        showPage(PAGE_STEP_1);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_NETWORK_STATE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_WIFI_STATE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CHANGE_WIFI_STATE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.


                showError(getResources().getString(R.string.wizard_not_enought_permissions, getApplicationName()));


            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_NETWORK_STATE,
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.CHANGE_WIFI_STATE,
                                Manifest.permission.ACCESS_FINE_LOCATION}, WIZARD_PERMISSIONS_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }


    @Override
    protected void onPause() {
        super.onPause();

        if (pageId >= PAGE_STEP_2) {

            Preferences prefs = new Preferences(this);

            if (cbSavePassword.isChecked()) {
                prefs.wizardSetPassword(getSelectedSSID(), cbSavePassword.isChecked() ? edPassword.getText().toString() : "");
            }

            prefs.wizardSetSavePasswordEnabled(getSelectedSSID(), cbSavePassword.isChecked());
        }


        setPreloaderVisible(false);
        cleanUp();
    }

    private void setStep(int step) {

        if (step == STEP_DONE || step == STEP_NONE) {
            step_time = null;
        } else {
            step_time = new Date();
        }

        this.step = step;
    }

    private void onWatchDogTimeout() {

        switch (step) {
            case STEP_CHECK_WIFI:
                showError(R.string.wizard_wifi_timeout);
                break;
            case STEP_CHECK_REGISTRATION_ENABLED_TRY1:
                setStep(STEP_CHECK_REGISTRATION_ENABLED_TRY2);
                SuplaApp.getApp().SuplaClientInitIfNeed(getApplicationContext()).GetRegistrationEnabled();
                return;
            case STEP_CHECK_REGISTRATION_ENABLED_TRY2:
                showError(R.string.device_reg_request_timeout);
                break;
            case STEP_SCAN:
                showError(R.string.wizard_scan_timeout);
                break;
            case STEP_CONNECT:
                showError(R.string.wizard_connect_timeout);
                break;
            case STEP_CONFIGURE:
                showError(R.string.wizard_configure_timeout);
                break;
            case STEP_RECONNECT:
                showError(R.string.wizard_reconnect_timeout);
                break;
        }

        removeConfigTask();
        unregisterReceivers();
        removeIODeviceNetwork();


    }

    private void unregisterReceivers() {

        if (stateChangedReceiver != null) {
            unregisterReceiver(stateChangedReceiver);
            stateChangedReceiver = null;
        }

        if (scanResultReceiver != null) {
            unregisterReceiver(scanResultReceiver);
            scanResultReceiver = null;
        }

    }

    private void removeConfigTask() {

        if (espConfigTask != null) {
            espConfigTask.cancel(true);
            espConfigTask = null;
        }

    }

    private boolean checkWiFi() {

        String SSID = "";
        NetworkID = -1;

        if (manager.isWifiEnabled()) {

            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                SSID = wifiInfo.getSSID();
                NetworkID = wifiInfo.getNetworkId();

                if (SSID.startsWith("\"") && SSID.endsWith("\"")) {
                    SSID = SSID.substring(1, SSID.length() - 1);
                }

                spinnerLoad(SSID);

            }
        }

        return !SSID.isEmpty() && manager.setWifiEnabled(true);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v == btnNext1 || v == btnNext2 || v == btnNext3) {

            setPreloaderVisible(true);
            setBtnEnabled(false);

            switch (pageId) {
                case PAGE_STEP_1:

                    setStep(STEP_CHECK_WIFI);

                    if (!checkWiFi()) {
                        showError(R.string.wizard_wifi_error);
                    }

                    break;
                case PAGE_STEP_2:

                    if (edPassword.getText().toString().isEmpty()) {

                        setPreloaderVisible(false);
                        setBtnEnabled(true);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            edPassword.setBackground(getResources().getDrawable(R.drawable.rounded_edittext_err));
                        } else {
                            edPassword.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext_err));
                        }

                    } else {
                        showPage(PAGE_STEP_3);

                        // Autostart after 20 sec.
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if (pageId == PAGE_STEP_3 && preloaderTimer == null) {
                                    onClick(btnNext1);
                                }

                            }
                        }, 20000);
                    }

                    break;
                case PAGE_STEP_3:

                    btnNext2.setText("....", TextView.BufferType.NORMAL);
                    setStep(STEP_CHECK_REGISTRATION_ENABLED_TRY1);
                    SuplaApp.getApp().SuplaClientInitIfNeed(getApplicationContext()).GetRegistrationEnabled();

                    break;
                case PAGE_ERROR:
                case PAGE_DONE:
                    showMain(this);
                    finish();
                    break;

            }

            return;
        }

        if (v == cbSavePassword) {
            Preferences prefs = new Preferences(this);
            prefs.wizardSetSavePasswordEnabled(getSelectedSSID(), cbSavePassword.isChecked());
        }

    }

    protected void OnRegistrationEnabled(SuplaRegistrationEnabled registrationEnabled) {

        super.OnRegistrationEnabled(registrationEnabled);

        if (registrationEnabled.IsIODeviceRegistrationEnabled()) {

            setStep(STEP_SCAN);
            scanRetry = SCAN_RETRY;
            startScan();

        } else {
            showError(getResources().getString(R.string.device_registration_disabled, SuplaRegisterError.getHostname(this)));
        }


    }

    private boolean espNetworkName(String SSID) {

        if (SSID.startsWith("\"") && SSID.endsWith("\"")) {
            SSID = SSID.substring(1, SSID.length() - 1);
        }

        Pattern mPattern = Pattern.compile("\\-[A-Fa-f0-9]{12}$");

        return (SSID.startsWith("SUPLA-")
                || SSID.startsWith("ZAMEL-")
                || SSID.startsWith("NICE-")
                || SSID.startsWith("HEATPOL-"))
                && mPattern.matcher(SSID).find();
    }

    private void startScan() {

        unregisterReceivers();

        scanResultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {

                unregisterReceiver(scanResultReceiver);
                scanResultReceiver = null;


                List<ScanResult> scanned = manager.getScanResults();
                boolean match = false;

                for (int a = 0; a < scanned.size(); a++) {

                    iodev_SSID = scanned.get(a).SSID;

                    if (espNetworkName(iodev_SSID)) {
                        match = true;
                        connect();
                        break;
                    }

                }

                if (!match) {

                    if (scanRetry > 0) {
                        scanRetry--;
                        startScan();
                    } else {
                        showError(R.string.wizard_iodevice_notfound);
                    }


                }

            }
        };

        IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        registerReceiver(scanResultReceiver, i);

        manager.startScan();

    }

    private void spinnerLoad(final String SSID) {

        unregisterReceivers();

        final Context context = this;

        scanResultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {

                unregisterReceiver(scanResultReceiver);
                scanResultReceiver = null;

                Pattern mPattern = Pattern.compile("\\-[A-Fa-f0-9]{12}$");
                List<ScanResult> scanned = manager.getScanResults();

                ArrayList<String> spinnerArray = new ArrayList<String>();

                Preferences prefs = new Preferences(context);
                String prefSelected = prefs.wizardGetSelectedWifi();

                String Selected = SSID;

                if (!prefSelected.isEmpty()) {
                    for (ScanResult sr : scanned) {
                        if (prefSelected.equals(sr.SSID)) {
                            Selected = sr.SSID;
                            break;
                        }
                    }
                }

                spinnerArray.add(Selected);

                if (!Selected.equals(SSID)) {
                    spinnerArray.add(SSID);
                }

                for (ScanResult sr : scanned) {

                    Set<String> set = new HashSet<String>(spinnerArray);

                    if (!sr.SSID.equals(SSID) && !espNetworkName(sr.SSID) && !set.contains(sr.SSID))
                        spinnerArray.add(sr.SSID);
                }

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinnerArray);
                spWifiList.setAdapter(spinnerArrayAdapter);
                onWifiSelectionChange();

                showPage(PAGE_STEP_2);
            }
        };

        IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        registerReceiver(scanResultReceiver, i);

        manager.startScan();

    }

    private void unbindNetwork() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                cm.bindProcessToNetwork(null);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager.setProcessDefaultNetwork(null);
        }

    }

    private void bindNetwork() {

        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] ns = cm.getAllNetworks();
            for (Network n : ns) {

                NetworkCapabilities c = cm.getNetworkCapabilities(n);

                if (c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        cm.bindProcessToNetwork(n);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ConnectivityManager.setProcessDefaultNetwork(n);
                    }

                }

            }
        }


    }

    private void connect() {

        setStep(STEP_CONNECT);

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + iodev_SSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        iodev_NetworkID = -1;

        List<WifiConfiguration> list = manager.getConfiguredNetworks();

        for (WifiConfiguration item : list) {
            if (item.SSID != null
                    && (item.SSID.equals(iodev_SSID)
                    || item.SSID.equals(conf.SSID))) {
                iodev_NetworkID = item.networkId;
                break;
            }
        }

        if (iodev_NetworkID == -1) {
            iodev_NetworkID = manager.addNetwork(conf);
        }

        if (iodev_NetworkID == -1) {
            showError(R.string.wizard_addnetwork_error);
            return;
        }

        unregisterReceivers();
        removeConfigTask();

        espConfigTask = new ESPConfigureTask();
        espConfigTask.setDelegate(this);

        manager.disconnect();

        final Preferences prefs = new Preferences(this);

        stateChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {

                NetworkInfo info = i.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                //Trace.d("Status", info.getState().toString());

                if (info != null
                        && info.isConnected()) {

                    WifiInfo wifiInfo = manager.getConnectionInfo();
                    if (wifiInfo != null && wifiInfo.getNetworkId() == iodev_NetworkID) {

                        bindNetwork();

                        unregisterReceiver(stateChangedReceiver);
                        stateChangedReceiver = null;

                        setStep(STEP_CONFIGURE);
                        espConfigTask.execute(getSelectedSSID(), edPassword.getText().toString(), prefs.getServerAddress(), prefs.getEmail());

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

        if (iodev_NetworkID != -1) {

            unbindNetwork();

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

        if (result.resultCode == ESPConfigureTask.RESULT_SUCCESS) {

            setStep(STEP_RECONNECT);

            if (blinkTimer != null) {
                blinkTimer.cancel();
                blinkTimer = null;

                ivDot.setVisibility(View.VISIBLE);
            }

            stateChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent i) {

                    NetworkInfo info = i.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                    if (info != null
                            && info.isConnected()) {

                        WifiInfo wifiInfo = manager.getConnectionInfo();
                        if (wifiInfo != null && wifiInfo.getNetworkId() == NetworkID) {

                            unregisterReceiver(stateChangedReceiver);
                            stateChangedReceiver = null;

                            if (SuplaApp.getApp().getSuplaClient() != null) {
                                SuplaApp.getApp().getSuplaClient().Reconnect();
                            }

                            setStep(STEP_DONE);
                            showDone(result);
                        }

                    }

                }
            };

        } else {

            switch (result.resultCode) {
                case ESPConfigureTask.RESULT_PARAM_ERROR:
                    showError(R.string.wizard_result_param_error);
                    break;
                case ESPConfigureTask.RESULT_COMPAT_ERROR:
                    showError(R.string.wizard_result_compat_error);
                    break;
                case ESPConfigureTask.RESULT_CONN_ERROR:
                    showError(R.string.wizard_result_conn_error);
                    break;
                case ESPConfigureTask.RESULT_FAILED:
                    showError(R.string.wizard_result_failed);
                    break;
            }
        }


        IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(stateChangedReceiver, i);
        removeIODeviceNetwork();

    }

    private String getSelectedSSID() {

        if (spWifiList.getSelectedItem() != null) {
            return spWifiList.getSelectedItem().toString();
        }

        return "";
    }

    private void onWifiSelectionChange() {

        Preferences prefs = new Preferences(this);

        tvStep2Info.setText(getResources().getString(R.string.wizard_step2_txt3, getSelectedSSID()));

        cbSavePassword.setChecked(prefs.wizardSavePasswordEnabled(getSelectedSSID()));
        edPassword.setText(cbSavePassword.isChecked() ? prefs.wizardGetPassword(getSelectedSSID()) : "");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        onWifiSelectionChange();

        Preferences prefs = new Preferences(this);
        prefs.wizardSetSelectedWifi(getSelectedSSID());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {


    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (v == btnViewPassword) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    edPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    edPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    break;
            }
        }

        return false;
    }
}
