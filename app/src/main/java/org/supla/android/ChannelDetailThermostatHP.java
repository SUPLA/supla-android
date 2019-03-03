package org.supla.android;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;

import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;
import org.supla.android.restapi.DownloadThermostatMeasurements;
import org.supla.android.restapi.SuplaRestApiClientTask;


public class ChannelDetailThermostatHP extends DetailLayout implements View.OnClickListener,
        SuplaRestApiClientTask.IAsyncResults {

    public final static int STATUS_POWERON = 0x01;
    public final static int STATUS_PROGRAMMODE = 0x04;
    public final static int STATUS_HEATERANDWATERTEST = 0x10;
    public final static int STATUS_HEATING = 0x20;

    public final static int BTN_SET_OFF = 0;
    public final static int BTN_SET_ON = 1;
    public final static int BTN_SET_TOGGLE = 2;

    private Button btnOnOff;
    private Button btnNormal;
    private Button btnEco;
    private Button btnAuto;
    private Button btnTurbo;
    private Button btnPlus;
    private Button btnMinus;
    private TextView tvTemperature;
    private long refreshLock = 0;
    private int presetTemperature = 0;
    private Double measuredTemperature = 0.00;
    private DownloadThermostatMeasurements dtm;
    private ThermostatChartHelper chartHelper;

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

        tvTemperature = findViewById(R.id.hpTvTemperature);

        Typeface type = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/OpenSans-Regular.ttf");
        tvTemperature.setTypeface(type);

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

        btnPlus = findViewById(R.id.hpBtnPlus);
        btnPlus.setOnClickListener(this);

        btnMinus = findViewById(R.id.hpBtnMinus);
        btnMinus.setOnClickListener(this);

        chartHelper = new ThermostatChartHelper(getContext());
        chartHelper.setBarChart((BarChart) findViewById(R.id.emBarChart));
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

    private byte setBtnAppearance(Button btn, int setOn, int textOn, int textOff) {

        if (setOn == BTN_SET_TOGGLE) {
            setOn = btnIsOn(btn) ? BTN_SET_OFF : BTN_SET_ON;
        }

        if (setOn == BTN_SET_ON) {
            btn.setTag(1);
            btn.setBackgroundResource(R.drawable.hp_button_on);
            if (textOn != 0) {
                btn.setText(textOn);
            }
            return 1;
        } else {
            btn.setTag(0);
            btn.setBackgroundResource(R.drawable.hp_button_off);
            if (textOff != 0) {
                btn.setText(textOn);
            }
        }
        return 0;
    }

    private byte setBtnAppearance(Button btn, int setOn) {
        return setBtnAppearance(btn, setOn, 0, 0);
    }

    private byte setBtnAppearance(Button btn, boolean setOn) {
        return setBtnAppearance(btn, setOn ? BTN_SET_ON : BTN_SET_OFF, 0, 0);
    }

    private boolean btnIsOn(Button btn) {
        return ((Integer)btn.getTag()).intValue() == 1;
    }

    private void displayTemperature() {
        CharSequence t = ChannelBase.getHumanReadableThermostatTemperature(
                measuredTemperature,
                new Double(presetTemperature));

        tvTemperature.setText(t);
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

        Double temp = cev.getExtendedValue().ThermostatValue.getPresetTemperature(0);
        presetTemperature = temp != null ? temp.intValue() : 0;
        measuredTemperature = cev.getExtendedValue().ThermostatValue.getMeasuredTemperature(0);

        displayTemperature();

        Double ecoReduction = cev.getExtendedValue().ThermostatValue.getPresetTemperature(3);
        if (ecoReduction != null) {
            setBtnAppearance(btnEco, ecoReduction > 0.0);
        }

        Integer flags = cev.getExtendedValue().ThermostatValue.getFlags(4);

        if (flags != null) {
            setBtnAppearance(btnOnOff,(flags & STATUS_POWERON), R.string.hp_on, R.string.hp_off);
            setBtnAppearance(btnAuto, (flags & STATUS_PROGRAMMODE) > 0);
        }

        Integer turbo = cev.getExtendedValue().ThermostatValue.getValues(4);
        if (turbo != null) {
            setBtnAppearance(btnTurbo, turbo > 0);
        }

        if (!btnIsOn(btnOnOff) || btnIsOn(btnEco)
                || btnIsOn(btnTurbo) || btnIsOn(btnAuto)) {
            setBtnAppearance(btnNormal, false);
        } else {
            setBtnAppearance(btnNormal, true);
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
        chartHelper.loadThermostatMeasurements(getRemoteId());
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

    private void setAllButtonsOff(Button skip) {
        if (skip != btnNormal) {
            setBtnAppearance(btnNormal, BTN_SET_OFF);
        }

        if (skip != btnEco) {
            setBtnAppearance(btnEco, BTN_SET_OFF);
        }

        if (skip != btnTurbo) {
            setBtnAppearance(btnTurbo, BTN_SET_OFF);
        }

        if (skip != btnAuto) {
            setBtnAppearance(btnAuto, BTN_SET_OFF);
        }
    }

    private void setAllButtonsOff() {
        setAllButtonsOff(null);
    }

    @Override
    public void onClick(View view) {
        refreshLock = System.currentTimeMillis()+2000;

        byte on = 0;

        if (view == btnPlus) {
            presetTemperature++;
            if (presetTemperature > 30) {
                presetTemperature = 30;
            }

            setTemperature(0, (double) presetTemperature);
            displayTemperature();
        } else if (view == btnMinus) {
            presetTemperature--;
            if (presetTemperature < 10) {
                presetTemperature = 10;
            }

            setTemperature(0, (double) presetTemperature);
            displayTemperature();
        } else if (view == btnOnOff) {
            on = setBtnAppearance(btnOnOff, BTN_SET_TOGGLE,  R.string.hp_on,  R.string.hp_off);
            if (on == 0) {
                setAllButtonsOff();
            }
            deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_TURNON, on);
        } else if (view == btnNormal) {
            setBtnAppearance(btnNormal, BTN_SET_ON);
            deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_SET_MODE_NORMAL, null);
            setAllButtonsOff(btnNormal);
        } else if (view == btnEco) {
            on = setBtnAppearance(btnEco, BTN_SET_TOGGLE);
            deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_SET_MODE_ECO,
                    (byte)(on > 0 ? 5 : 0));
        } else if (view == btnAuto) {
            on = setBtnAppearance(btnAuto, BTN_SET_TOGGLE);
            deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_SET_MODE_AUTO, on);
        } else if (view == btnTurbo) {
            on = setBtnAppearance(btnTurbo, BTN_SET_TOGGLE);
            deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_SET_MODE_TURBO,
                    (byte)(on > 0 ? 3 : 0));
        }

        if (on == 1 && (view == btnNormal
                || view == btnEco
                || view == btnAuto
                || view == btnTurbo)) {
            setAllButtonsOff((Button) view);
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
    public void onRestApiTaskStarted(SuplaRestApiClientTask task) {

    }

    @Override
    public void onRestApiTaskFinished(SuplaRestApiClientTask task) {
        dtm = null;
        chartHelper.loadThermostatMeasurements(getRemoteId());
    }
}

