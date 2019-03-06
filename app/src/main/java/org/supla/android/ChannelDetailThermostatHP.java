package org.supla.android;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;

import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.lib.Preferences;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;
import org.supla.android.restapi.DownloadThermostatMeasurements;
import org.supla.android.restapi.SuplaRestApiClientTask;

import java.util.Arrays;
import java.util.List;


public class ChannelDetailThermostatHP extends DetailLayout implements View.OnClickListener,
        SuplaRestApiClientTask.IAsyncResults {

    private class CfgItem {

        public final static int ID_TURBO_TIME = 1;
        public final static int ID_WATER_MAX = 2;
        public final static int ID_ECO_REDUCTION = 3;
        public final static int ID_TEMP_COMFORT = 4;
        public final static int ID_TEMP_ECO = 5;

        private int Id;
        private Button BtnPlus;
        private Button BtnMinus;
        private TextView TvValue;
        int value;
        private int Min;
        private int Max;

        CfgItem(int id, int btnMinus, int btnPlus, int tvValue, int min, int max
                , int defaultValue) {
            BtnMinus = findViewById(btnMinus);
            BtnPlus = findViewById(btnPlus);
            TvValue = findViewById(tvValue);

            Id = id;
            Min = min;
            Max = max;
            value = defaultValue;

            if (BtnMinus!=null) {
                BtnMinus.setOnClickListener(ChannelDetailThermostatHP.this);
            }

            if (BtnPlus!=null) {
                BtnPlus.setOnClickListener(ChannelDetailThermostatHP.this);
            }

            displayValue();
        }

        public void displayValue() {
            if (TvValue != null) {
                if (Id == ID_TURBO_TIME) {
                    TvValue.setText(Integer.toString(value)+" godz.");
                } else {
                    TvValue.setText(Integer.toString(value)+(char) 0x00B0);
                }

            }
        }

        public boolean onClick(View view) {

            if (view == null) {
                return false;
            }

            if (view == BtnMinus) {
                value--;
                setValue(value);
            } else if (view == BtnPlus) {
                value++;
                setValue(value);
            } else {
                return false;
            }

            displayValue();
            return true;
        }

        public int getId() {
            return Id;
        }

        public boolean idEqualsTo(int id) {
            return id == Id;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            if (value < Min) {
                value = Min;
            } else if (value > Max) {
                value = Max;
            }

            this.value = value;
            displayValue();
        }
    }

    public final static int STATUS_POWERON = 0x01;
    public final static int STATUS_PROGRAMMODE = 0x04;
    public final static int STATUS_HEATERANDWATERTEST = 0x10;
    public final static int STATUS_HEATING = 0x20;

    public final static int BTN_SET_OFF = 0;
    public final static int BTN_SET_ON = 1;
    public final static int BTN_SET_TOGGLE = 2;

    private int cfgTurboTime = 1;

    private Button btnSettings;
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
    private RelativeLayout rlContent;
    private RelativeLayout rlSettings;
    private ProgressBar progressBar;
    private List<CfgItem> cfgItems;

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

        rlContent = findViewById(R.id.hpContent);
        rlContent.setVisibility(VISIBLE);

        rlSettings = findViewById(R.id.hpSettings);
        rlSettings.setVisibility(GONE);

        progressBar = findViewById(R.id.hpProgressBar);
        progressBar.setVisibility(INVISIBLE);

        tvTemperature = findViewById(R.id.hpTvTemperature);

        Typeface tfOpenSansRegular = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/OpenSans-Regular.ttf");

        Typeface tfQuicksandRegular = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/Quicksand-Regular.ttf");

        tvTemperature.setTypeface(tfOpenSansRegular);

        btnSettings = findViewById(R.id.hpBtnSettings);
        btnSettings.setOnClickListener(this);

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


        ((TextView)findViewById(R.id.hpTvCaptionTurbo)).setTypeface(tfOpenSansRegular);
        ((TextView)findViewById(R.id.hpTvTurbo)).setTypeface(tfOpenSansRegular);
        ((TextView)findViewById(R.id.hpTvCaptionWaterMax)).setTypeface(tfOpenSansRegular);
        ((TextView)findViewById(R.id.hpTvWaterMax)).setTypeface(tfOpenSansRegular);
        ((TextView)findViewById(R.id.hpTvEcoReductionCaption)).setTypeface(tfOpenSansRegular);
        ((TextView)findViewById(R.id.hpTvEcoReduction)).setTypeface(tfOpenSansRegular);
        ((TextView)findViewById(R.id.hpTvAutoCaption)).setTypeface(tfOpenSansRegular);
        ((TextView)findViewById(R.id.hpTvAuto)).setTypeface(tfOpenSansRegular);
        ((TextView)findViewById(R.id.hpTvEco)).setTypeface(tfOpenSansRegular);
        ((TextView)findViewById(R.id.hpTvEcoCaption)).setTypeface(tfOpenSansRegular);

        cfgItems = Arrays.asList(
                new CfgItem(CfgItem.ID_TURBO_TIME,
                        R.id.hpBtnTurboMinus,
                        R.id.hpBtnTurboPlus, R.id.hpTvTurbo, 1, 3, 1),

                new CfgItem(CfgItem.ID_WATER_MAX,
                        R.id.hpBtnWaterMaxMinus,
                        R.id.hpBtnWaterMaxPlus, R.id.hpTvWaterMax, 30, 70,
                        70),

                new CfgItem(CfgItem.ID_ECO_REDUCTION,
                        R.id.hpBtnEcoReductionMinus,
                        R.id.hpBtnEcoReductionPlus, R.id.hpTvEcoReduction, 1, 5,
                        3),

                new CfgItem(CfgItem.ID_TEMP_COMFORT,
                        R.id.hpBtnAutoMinus,
                        R.id.hpBtnAutoPlus, R.id.hpTvAuto, 10, 30, 22),

                new CfgItem(CfgItem.ID_TEMP_ECO,
                        R.id.hpBtnEcoMinus,
                        R.id.hpBtnEcoPlus, R.id.hpTvEco, 10, 30, 19)
        );

        Preferences prefs = new Preferences(getContext());
        setCfgValue(CfgItem.ID_TURBO_TIME, prefs.getHeatpolTurboTime());
        setCfgValue(CfgItem.ID_ECO_REDUCTION, prefs.getHeatpolEcoReduction());
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
                btn.setText(textOff);
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

    public void displaySettings() {
        rlContent.setVisibility(GONE);
        rlSettings.setVisibility(VISIBLE);
    }

    public Integer getCfgValue(int id) {
        for (CfgItem item : cfgItems) {
            if (item.idEqualsTo(id)) {
                return new Integer(item.getValue());
            }
        }
        return null;
    }

    public void setCfgValue(int id, int value) {
        for (CfgItem item : cfgItems) {
            if (item.idEqualsTo(id)) {
                item.setValue(value);
            }
        }
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

        Double waterMax = cev.getExtendedValue().ThermostatValue.getPresetTemperature(2);
        if (waterMax != null) {
            setCfgValue(CfgItem.ID_WATER_MAX, waterMax.intValue());
        }

        Double ecoReduction = cev.getExtendedValue().ThermostatValue.getPresetTemperature(3);
        if (ecoReduction != null) {
            setBtnAppearance(btnEco, ecoReduction > 0.0);
        }

        Double comfortTemp = cev.getExtendedValue().ThermostatValue.getPresetTemperature(4);
        if (comfortTemp != null) {
            setCfgValue(CfgItem.ID_TEMP_COMFORT, comfortTemp.intValue());
        }

        Double ecoTemp = cev.getExtendedValue().ThermostatValue.getPresetTemperature(5);
        if (ecoTemp != null) {
            setCfgValue(CfgItem.ID_TEMP_ECO, ecoTemp.intValue());
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

        rlContent.setVisibility(VISIBLE);
        rlSettings.setVisibility(GONE);

        OnChannelDataChanged();
        chartHelper.loadThermostatMeasurements(getRemoteId());
        chartHelper.moveToEnd(20, 1000);

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

        for (CfgItem item : cfgItems) {
            if (item.onClick(view)) {

                int idx = 0;
                Preferences prefs = new Preferences(getContext());

                switch(item.getId()) {
                    case CfgItem.ID_WATER_MAX:
                        idx = 2;
                        break;
                    case CfgItem.ID_TEMP_COMFORT:
                        idx = 3;
                        break;
                    case CfgItem.ID_TEMP_ECO:
                        idx = 4;
                        break;
                    case CfgItem.ID_TURBO_TIME:
                        prefs.setHeatpolTurboTime(item.getValue());

                        if (btnIsOn(btnTurbo)) {
                            deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_SET_MODE_TURBO,
                                    (byte)item.getValue());
                        }
                        break;
                    case CfgItem.ID_ECO_REDUCTION:
                        prefs.setHeatpolEcoReduction(item.getValue());

                        if (btnIsOn(btnEco)) {
                            deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_SET_MODE_ECO,
                                    (byte)(item.getValue() * 10));
                        }
                        break;
                }

                if (idx > 0) {
                    setTemperature(idx, item.getValue() * 1.0);
                }

                return;
            }
        }

        byte on = 0;

        if (view == btnSettings) {

            if (rlSettings.getVisibility() == GONE) {
                displaySettings();
            } else {
                rlContent.setVisibility(VISIBLE);
                rlSettings.setVisibility(GONE);
            }

        } else if (view == btnPlus) {
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
                    (byte)(on > 0 ? getCfgValue(CfgItem.ID_ECO_REDUCTION) : 0));
        } else if (view == btnAuto) {
            on = setBtnAppearance(btnAuto, BTN_SET_TOGGLE);
            deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_SET_MODE_AUTO, on);
        } else if (view == btnTurbo) {
            on = setBtnAppearance(btnTurbo, BTN_SET_TOGGLE);
            deviceCalCfgRequest(SuplaConst.SUPLA_THERMOSTAT_CMD_SET_MODE_TURBO,
                    (byte)(on > 0 ? getCfgValue(CfgItem.ID_TURBO_TIME) : 0));
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
        progressBar.setVisibility(VISIBLE);
    }

    @Override
    public void onRestApiTaskFinished(SuplaRestApiClientTask task) {
        dtm = null;
        progressBar.setVisibility(INVISIBLE);
        chartHelper.loadThermostatMeasurements(getRemoteId());
    }
}

