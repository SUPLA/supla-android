package org.supla.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;
import org.supla.android.restapi.DownloadThermostatMeasurements;
import org.supla.android.restapi.SuplaRestApiClientTask;

import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.Handler;

public class ChannelDetailThermostatHP extends DetailLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, SuplaRestApiClientTask.IAsyncResults {

    public final static int STATUS_POWERON = 0x01;
    public final static int STATUS_PROGRAMMODE = 0x04;
    public final static int STATUS_HEATERANDWATERTEST = 0x10;
    public final static int STATUS_HEATING = 0x20;

    public final static int BTN_SET_OFF = 0;
    public final static int BTN_SET_ON = 1;
    public final static int BTN_SET_TOGGLE = 2;

    private Timer delayTimer1 = null;

    private Button btnOnOff;
    private Button btnNormal;
    private Button btnEco;
    private Button btnAuto;
    private Button btnTurbo;
    private TextView tvMeasuredTemp;
    private TextView tvPresetTemp;
    private TextView tvWaterTemp;
    private SeekBar sbTemperature;
    private long refreshLock = 0;
    private DownloadThermostatMeasurements dtm;

    public ChannelDetailThermostatHP(Context context, ChannelListView cLV) {
        super(context, cLV);
    }

    public ChannelDetailThermostatHP(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelDetailThermostatHP(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChannelDetailThermostatHP(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        super.init();

        btnOnOff = findViewById(R.id.hpBtnOnOff);
        btnOnOff.setOnClickListener(this);
        btnOnOff.setTag(Integer.valueOf(0));

        btnNormal = findViewById(R.id.hpBtnNormal);
        btnNormal.setOnClickListener(this);
        btnNormal.setTag(Integer.valueOf(0));

        btnEco = findViewById(R.id.hpBtnEco);
        btnEco.setOnClickListener(this);
        btnEco.setTag(Integer.valueOf(0));

        btnAuto = findViewById(R.id.hpBtnAuto);
        btnAuto.setOnClickListener(this);
        btnAuto.setTag(Integer.valueOf(0));

        btnTurbo = findViewById(R.id.hpBtnTurbo);
        btnTurbo.setOnClickListener(this);
        btnTurbo.setTag(Integer.valueOf(0));

        tvMeasuredTemp = findViewById(R.id.hpTvMeasuredTemp);
        tvPresetTemp = findViewById(R.id.hpTvPresetTemp);
        tvWaterTemp = findViewById(R.id.hpTvWaterTemp);

        sbTemperature = findViewById(R.id.hpSbTemperature);
        sbTemperature.setOnSeekBarChangeListener(this);
        sbTemperature.setMax(100);
        sbTemperature.setProgress(50);
    }

    @Override
    public View getContentView() {
        return inflateLayout(R.layout.detail_homeplus);
    }

    private void setTemperatureTextView(TextView tv, Double temp) {
        if (temp != null && temp > -273) {
            tv.setText(String.format("%.1f", temp) + (char) 0x00B0);
        } else {
            tv.setText("--");
        }
        tv.setTag(temp);
    }

    private byte setBtnOnOff(Button btn, int setOn, String textOn, String textOff) {

        if (setOn == BTN_SET_TOGGLE) {
            setOn = ((Integer)btn.getTag()).intValue() == 1 ? BTN_SET_OFF : BTN_SET_ON;
        }

        if (setOn == BTN_SET_ON) {
            btn.setTag(1);
            btn.setBackgroundColor(Color.GREEN);
            if (textOn != null) {
                btn.setText(textOn);
            }
            return 1;
        } else {
            btn.setTag(0);
            btn.setBackgroundColor(Color.RED);
            if (textOff != null) {
                btn.setText(textOn);
            }
        }
        return 0;
    }

    private byte setBtnOnOff(Button btn, int setOn) {
        return setBtnOnOff(btn, setOn, null, null);
    }

    @Override
    public void OnChannelDataChanged() {
        if (refreshLock > System.currentTimeMillis()) {
            return;
        }

        ChannelExtendedValue cev = DBH.getChannelExtendedValue(getRemoteId());
        if (cev.getType() != SuplaConst.EV_TYPE_THERMOSTAT_DETAILS_V1
                || cev.getExtendedValue().ThermostatValue == null) {
            return;
        }

        setTemperatureTextView(tvMeasuredTemp,
                cev.getExtendedValue().ThermostatValue.getMeasuredTemperature(0));
        setTemperatureTextView(tvPresetTemp,
                cev.getExtendedValue().ThermostatValue.getPresetTemperature(0));
        setTemperatureTextView(tvWaterTemp,
                cev.getExtendedValue().ThermostatValue.getPresetTemperature(1));

        Double ecoReduction = cev.getExtendedValue().ThermostatValue.getPresetTemperature(3);


            if (ecoReduction != null) {
                if (ecoReduction > 0) {
                    btnEco.setTag(1);
                    btnEco.setBackgroundColor(Color.GREEN);
                } else {
                    btnEco.setTag(0);
                    btnEco.setBackgroundColor(Color.RED);
                }
            }

            Integer flags = cev.getExtendedValue().ThermostatValue.getFlags(4);

            if (flags != null) {
                setBtnOnOff(btnOnOff,(flags & STATUS_POWERON) > 0 ? BTN_SET_ON : BTN_SET_OFF,
                        "Włączony", "Wyłączony");

                setBtnOnOff(btnAuto, (flags & STATUS_PROGRAMMODE) > 0 ? BTN_SET_ON : BTN_SET_OFF);
            }

            Integer turbo = cev.getExtendedValue().ThermostatValue.getValues(4);
            if (turbo != null) {
                setBtnOnOff(btnTurbo, turbo > 0 ? BTN_SET_ON : BTN_SET_OFF);
            }

    }

    private void runDownloadTask() {
        if (dtm != null && !dtm.isAlive(90)) {
            dtm.cancel(true);
            dtm = null;
        }

        if (dtm == null) {
            dtm = new DownloadThermostatMeasurements(this.getContext());
            dtm.setChannelId(getRemoteId());
            dtm.setDelegate(this);
            dtm.execute();
        };
    }

    @Override
    public void onDetailShow() {
        super.onDetailShow();
        OnChannelDataChanged();
        runDownloadTask();
    }

    private void deviceCalCfgRequest(int cmd, int dataType, byte[] data) {
        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if (client == null || !isDetailVisible()) {
            return;
        }

        client.DeviceCalCfgRequest(getRemoteId(), cmd, dataType, data);
    }

    private void deviceCalCfgRequest(int cmd, Byte data) {

        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if (client == null || !isDetailVisible()) {
            return;
        }

        byte[] arr = new byte[1];
        arr[0] = data == null ? 0 : data.byteValue();

        deviceCalCfgRequest(cmd, 0, data == null ? null : arr);
    }

    @Override
    public void onClick(View view) {

        refreshLock = System.currentTimeMillis()+1500;
        // ... timer


        byte on;

        if (view == btnOnOff) {
            on = setBtnOnOff(btnOnOff, BTN_SET_TOGGLE, "Włączony", "Wyłączony");
            deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_TURNON, on);
        } else if (view == btnNormal) {
            setBtnOnOff(btnNormal, BTN_SET_ON);
            deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_SET_MODE_NORMAL, null);
        } else if (view == btnEco) {
            on = setBtnOnOff(btnEco, BTN_SET_TOGGLE);
            deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_SET_MODE_ECO,
                    (byte)(on > 0 ? 5 : 0));
        } else if (view == btnAuto) {
            on = setBtnOnOff(btnAuto, BTN_SET_TOGGLE);
            deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_SET_MODE_AUTO, on);
        } else if (view == btnTurbo) {
            on = setBtnOnOff(btnTurbo, BTN_SET_TOGGLE);
            deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_SET_MODE_TURBO,
                    (byte)(on > 0 ? 3 : 0));
        }
    }

    public void setTemperature(int idx, Double temperature) {

        if (idx < 0 || idx > 10) {
            return;
        }

        short index = 1;
        index<<=idx;

        byte[] data = new byte[22];
        data[0] = (byte)(index & 0x00FF);
        data[1] = (byte)((index & 0xFF00) >> 8);

        short t = (short)(temperature * 100);

        data[2+idx*2] = (byte)(t & 0x00FF);
        data[3+idx*2] = (byte)((t & 0xFF00) >> 8);

        deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_SET_TEMPERATURE, 0, data);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        refreshLock = System.currentTimeMillis()+1500;
        Double temp = (double)(10 + i * 20 / 100);
        setTemperatureTextView(tvPresetTemp, temp);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (tvPresetTemp.getTag() instanceof Double
                && (Double)tvPresetTemp.getTag() >= 10
                && (Double)tvPresetTemp.getTag() <= 30) {
            setTemperature(0,
                    (Double)tvPresetTemp.getTag());
        }

    }

    @Override
    public void onRestApiTaskStarted(SuplaRestApiClientTask task) {

    }

    @Override
    public void onRestApiTaskFinished(SuplaRestApiClientTask task) {
        dtm = null;
    }
}

