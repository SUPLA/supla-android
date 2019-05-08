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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;

import org.supla.android.charts.ElectricityChartHelper;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.db.DbHelper;
import org.supla.android.images.ImageCache;
import org.supla.android.lib.SuplaChannelElectricityMeterValue;
import org.supla.android.lib.SuplaConst;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;
import org.supla.android.restapi.DownloadElectricityMeterMeasurements;
import org.supla.android.restapi.SuplaRestApiClientTask;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.String.format;

public class ChannelDetailEM extends DetailLayout implements View.OnClickListener, SuplaRestApiClientTask.IAsyncResults, AdapterView.OnItemSelectedListener {

    private Integer phase;

    private TextView tvTotalForwardActiveEnergy;
    private TextView tvCurrentConsumption;
    private TextView tvCurrentCost;
    private TextView tvTotalCost;

    private TextView tvFreq;
    private TextView tvlFreq;
    private TextView tvVoltage;
    private TextView tvlVoltage;
    private TextView tvCurrent;
    private TextView tvlCurrent;
    private TextView tvPowerActive;
    private TextView tvlPowerActive;
    private TextView tvPowerReactive;
    private TextView tvlPowerReactive;
    private TextView tvPowerApparent;
    private TextView tvlPowerApparent;
    private TextView tvPowerFactor;
    private TextView tvlPowerFactor;
    private TextView tvPhaseAngle;
    private TextView tvlPhaseAngle;
    private TextView tvPhaseForwardActiveEnergy;
    private TextView tvlPhaseForwardActiveEnergy;
    private TextView tvPhaseReverseActiveEnergy;
    private TextView tvlPhaseReverseActiveEnergy;
    private TextView tvPhaseForwardReactiveEnergy;
    private TextView tvlPhaseForwardReactiveEnergy;
    private TextView tvPhaseReverseReactiveEnergy;
    private TextView tvlPhaseReverseReactiveEnergy;

    private ImageView emImgIcon;
    private TextView tvChannelTitle;

    private Button btnPhase1;
    private Button btnPhase2;
    private Button btnPhase3;

    final Handler mHandler = new Handler();
    ElectricityChartHelper chartHelper;
    private ImageView ivGraph;
    private LinearLayout llDetails;
    private RelativeLayout rlButtons1;
    private RelativeLayout rlButtons2;
    private Spinner emSpinner;
    private ProgressBar emProgress;
    private Timer timer1;
    private DownloadElectricityMeterMeasurements demm = null;

    public ChannelDetailEM(Context context, ChannelListView cLV) {
        super(context, cLV);
    }

    public ChannelDetailEM(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelDetailEM(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChannelDetailEM(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void init() {
        super.init();

        phase = 1;

        tvTotalForwardActiveEnergy = findViewById(R.id.emtv_TotalForwardActiveEnergy);
        tvCurrentConsumption = findViewById(R.id.emtv_CurrentConsumption);
        tvCurrentCost = findViewById(R.id.emtv_CurrentCost);
        tvTotalCost = findViewById(R.id.emtv_TotalCost);

        tvFreq = findViewById(R.id.emtv_Freq);
        tvlFreq = findViewById(R.id.emtv_lFreq);
        tvVoltage = findViewById(R.id.emtv_Voltage);
        tvlVoltage = findViewById(R.id.emtv_lVoltage);
        tvCurrent = findViewById(R.id.emtv_Current);
        tvlCurrent = findViewById(R.id.emtv_lCurrent);
        tvPowerActive = findViewById(R.id.emtv_PowerActive);
        tvlPowerActive = findViewById(R.id.emtv_lPowerActive);
        tvPowerReactive = findViewById(R.id.emtv_PowerReactive);
        tvlPowerReactive = findViewById(R.id.emtv_lPowerReactive);
        tvPowerApparent = findViewById(R.id.emtv_PowerApparent);
        tvlPowerApparent = findViewById(R.id.emtv_lPowerApparent);
        tvPowerFactor = findViewById(R.id.emtv_PowerFactor);
        tvlPowerFactor = findViewById(R.id.emtv_lPowerFactor);
        tvPhaseAngle = findViewById(R.id.emtv_PhaseAngle);
        tvlPhaseAngle = findViewById(R.id.emtv_lPhaseAngle);
        tvPhaseForwardActiveEnergy = findViewById(R.id.emtv_PhaseForwardActiveEnergy);
        tvlPhaseForwardActiveEnergy = findViewById(R.id.emtv_lPhaseForwardActiveEnergy);
        tvPhaseReverseActiveEnergy = findViewById(R.id.emtv_PhaseReverseActiveEnergy);
        tvlPhaseReverseActiveEnergy = findViewById(R.id.emtv_lPhaseReverseActiveEnergy);
        tvPhaseForwardReactiveEnergy = findViewById(R.id.emtv_PhaseForwardRectiveEnergy);
        tvlPhaseForwardReactiveEnergy = findViewById(R.id.emtv_lPhaseForwardRectiveEnergy);
        tvPhaseReverseReactiveEnergy = findViewById(R.id.emtv_PhaseReverseRectiveEnergy);
        tvlPhaseReverseReactiveEnergy = findViewById(R.id.emtv_lPhaseReverseRectiveEnergy);

        emImgIcon = findViewById(R.id.emimgIcon);
        tvChannelTitle = findViewById(R.id.emtv_ChannelTitle);

        btnPhase1 = findViewById(R.id.embtn_Phase1);
        btnPhase2 = findViewById(R.id.embtn_Phase2);
        btnPhase3 = findViewById(R.id.embtn_Phase3);

        btnPhase1.setOnClickListener(this);
        btnPhase2.setOnClickListener(this);
        btnPhase3.setOnClickListener(this);

        btnPhase1.setTag(1);
        btnPhase2.setTag(2);
        btnPhase3.setTag(3);

        llDetails = findViewById(R.id.emllDetails);
        rlButtons1 = findViewById(R.id.emrlButtons1);
        rlButtons2 = findViewById(R.id.emrlButtons2);

        chartHelper = new ElectricityChartHelper(getContext());
        chartHelper.setCombinedChart((CombinedChart) findViewById(R.id.emCombinedChart));
        chartHelper.setPieChart((PieChart) findViewById(R.id.emPieChart));
        chartHelper.setUnit("kWh");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item, chartHelper.getSpinnerItems(0));

        emSpinner = findViewById(R.id.emSpinner);
        emSpinner.setAdapter(adapter);
        emSpinner.setOnItemSelectedListener(this);

        ivGraph = findViewById(R.id.emGraphImg);
        ivGraph.bringToFront();
        ivGraph.setOnClickListener(this);

        emProgress = findViewById(R.id.emProgressBar);

        showChart(false);
    }

    private void showChart(boolean show) {

        int img;

        if (show) {
            llDetails.setVisibility(GONE);
            chartHelper.setVisibility(VISIBLE);
            rlButtons1.setVisibility(INVISIBLE);
            rlButtons2.setVisibility(VISIBLE);
            img = R.drawable.graphon;
            ivGraph.setTag(1);

            onItemSelected(null, null,
                    emSpinner.getSelectedItemPosition(),
                    emSpinner.getSelectedItemId());

        } else {
            llDetails.setVisibility(VISIBLE);
            chartHelper.setVisibility(GONE);
            rlButtons1.setVisibility(VISIBLE);
            rlButtons2.setVisibility(INVISIBLE);
            img = R.drawable.graphoff;
            ivGraph.setTag(null);
        }

        Drawable d = getResources().getDrawable(img);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ivGraph.setBackground(d);
        } else {
            ivGraph.setBackgroundDrawable(d);
        }
    }

    protected String getActiveEnergyFormattedString(double energy) {
        int precision = 5;
        if (energy >= 1000) {
            precision = 3;
        } else if (energy >= 10000) {
            precision = 2;
        }
        return format("%."+Integer.toString(precision)+"f kWh", energy);
    }

    private void displayMeasurementDetail(long vars, int var, TextView tv1, TextView tv2) {
        tv1.setVisibility((vars & var) > 0 ? VISIBLE : GONE);
        tv2.setVisibility(tv1.getVisibility());
    }

    private void displayMeasurementDetails(long vars) {
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FREQ,
                tvFreq, tvlFreq);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_VOLTAGE,
                tvVoltage, tvlVoltage);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_CURRENT,
                tvCurrent, tvlCurrent);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_ACTIVE,
                tvPowerActive, tvlPowerActive);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_REACTIVE,
                tvPowerReactive, tvlPowerReactive);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_APPARENT,
                tvPowerApparent, tvlPowerApparent);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_FACTOR,
                tvPowerFactor, tvlPowerFactor);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_PHASE_ANGLE,
                tvPhaseAngle, tvlPhaseAngle);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FORWARD_ACTIVE_ENERGY,
                tvPhaseForwardActiveEnergy, tvlPhaseForwardActiveEnergy);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_REVERSE_ACTIVE_ENERGY,
                tvPhaseReverseActiveEnergy, tvlPhaseReverseActiveEnergy);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FORWARD_REACTIVE_ENERGY,
                tvPhaseForwardReactiveEnergy, tvlPhaseForwardReactiveEnergy);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_REVERSE_REACTIVE_ENERGY,
                tvPhaseReverseReactiveEnergy, tvlPhaseReverseReactiveEnergy);
    }

    public void channelExtendedDataToViews(boolean setIcon) {

        Channel channel = (Channel) getChannelFromDatabase();

        if (setIcon) {
            emImgIcon.setBackgroundColor(Color.TRANSPARENT);
            emImgIcon.setImageBitmap(ImageCache.getBitmap(getContext(), channel.getImageIdx()));
        }

        tvChannelTitle.setText(channel.getNotEmptyCaption(getContext()));

        ChannelExtendedValue cev = channel.getExtendedValue();

        setBtnBackground(btnPhase1, R.drawable.em_phase_btn_black);
        setBtnBackground(btnPhase2, R.drawable.em_phase_btn_black);
        setBtnBackground(btnPhase3, R.drawable.em_phase_btn_black);

        String empty = "----";
        tvTotalForwardActiveEnergy.setText(empty);
        tvCurrentConsumption.setText(empty);
        tvCurrentCost.setText(empty);
        tvTotalCost.setText(empty);

        tvFreq.setText(empty);
        tvVoltage.setText(empty);
        tvCurrent.setText(empty);
        tvPowerActive.setText(empty);
        tvPowerReactive.setText(empty);
        tvPowerApparent.setText(empty);
        tvPowerFactor.setText(empty);
        tvPhaseAngle.setText(empty);
        tvPhaseForwardActiveEnergy.setText(empty);
        tvPhaseReverseActiveEnergy.setText(empty);
        tvPhaseForwardReactiveEnergy.setText(empty);
        tvPhaseReverseReactiveEnergy.setText(empty);

        int vars = 0;

        if (cev != null
                && cev.getExtendedValue() != null
                && cev.getExtendedValue().ElectricityMeterValue != null) {

            SuplaChannelElectricityMeterValue em = cev.getExtendedValue().ElectricityMeterValue;
            vars = em.getMeasuredValues();
            chartHelper.setCurrency(em.getCurrency());
            chartHelper.setPricePerUnit(em.getPricePerUnit());
            SuplaChannelElectricityMeterValue.Summary sum = em.getSummary();

            double currentConsumption = 0;

            DbHelper mDBH = new DbHelper(getContext(), true);

            if (mDBH.electricityMeterMeasurementsStartsWithTheCurrentMonth(channel.getChannelId())) {
                currentConsumption = sum.getTotalForwardActiveEnergy();
            } else {
                double v0 = mDBH.getLastElectricityMeterMeasurementValue(0,
                        channel.getChannelId());
                double v1 = mDBH.getLastElectricityMeterMeasurementValue(-1,
                        channel.getChannelId());
                currentConsumption = v0-v1;
            }

            tvTotalForwardActiveEnergy
                    .setText(getActiveEnergyFormattedString(sum.getTotalForwardActiveEnergy()));
            tvCurrentConsumption.setText(String.format("%.2f kWh", currentConsumption));
            tvCurrentCost.setText(String.format("%.2f "+em.getCurrency(),
                    currentConsumption * em.getPricePerUnit()));
            tvTotalCost.setText(String.format("%.2f "+em.getCurrency(), em.getTotalCost()));

            SuplaChannelElectricityMeterValue.Measurement m = em.getMeasurement(phase, 0);
            if (m!= null) {

                Button btn = null;
                switch (phase) {
                    case 1: btn = btnPhase1; break;
                    case 2: btn = btnPhase2; break;
                    case 3: btn = btnPhase3; break;
                }

                setBtnBackground(btn, m.getVoltage() > 0 ? R.drawable.em_phase_btn_green : R.drawable.em_phase_btn_red);

                tvFreq.setText(format("%.2f Hz", m.getFreq()));
                tvVoltage.setText(format("%.2f V", m.getVoltage()));
                tvCurrent.setText(format("%.3f A", m.getCurrent()));
                tvPowerActive.setText(format("%.5f W", m.getPowerActive()));
                tvPowerReactive.setText(format("%.5f var", m.getPowerReactive()));
                tvPowerApparent.setText(format("%.5f VA", m.getPowerApparent()));
                tvPowerFactor.setText(format("%.3f", m.getPowerFactor()));
                tvPhaseAngle.setText(format("%.2f\u00B0", m.getPhaseAngle()));

                sum = em.getSummary(phase);
                tvPhaseForwardActiveEnergy.setText(format("%.5f kWh", sum.getTotalForwardActiveEnergy()));
                tvPhaseReverseActiveEnergy.setText(format("%.5f kWh", sum.getTotalReverseActiveEnergy()));
                tvPhaseForwardReactiveEnergy.setText(format("%.5f kvarh", sum.getTotalForwardReactiveEnergy()));
                tvPhaseReverseReactiveEnergy.setText(format("%.5f kvarh", sum.getTotalReverseReactiveEnergy()));
            }
        }

        displayMeasurementDetails(vars);
    }

    public void setData(ChannelBase channel) {
        super.setData(channel);
        channelExtendedDataToViews(true);
    }

    @Override
    public View getContentView() {
        return inflateLayout(R.layout.detail_em);
    }

    @Override
    public void OnChannelDataChanged() {
        channelExtendedDataToViews(false);
    }

    private void setBtnBackground(Button btn, int i) {

        Drawable d = getResources().getDrawable(i);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            btn.setBackgroundDrawable(d);
        } else {
            btn.setBackground(d);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == ivGraph) {
            showChart(v.getTag() == null);
        } else if (v instanceof Button && v.getTag() instanceof Integer) {
            phase = (Integer)v.getTag();
            channelExtendedDataToViews(false);
        }
    }

    private void runDownloadTask() {
        if (demm != null && !demm.isAlive(90)) {
            demm.cancel(true);
            demm = null;
        }

        if (demm == null) {
            demm = new DownloadElectricityMeterMeasurements(this.getContext());
            demm.setChannelId(getRemoteId());
            demm.setDelegate(this);
            demm.execute();
        }
    }

    @Override
    public void onDetailShow() {
        super.onDetailShow();

        emProgress.setVisibility(INVISIBLE);
        showChart(false);

        SuplaApp.getApp().CancelAllRestApiClientTasks(true);

        if (timer1 == null) {
            timer1 = new Timer();
            timer1.schedule(new TimerTask() {
                @Override
                public void run() {
                    final Runnable r = new Runnable() {
                        public void run() {
                            runDownloadTask();
                        }
                    };

                    mHandler.post(r);
                }
            }, 0, 30000);
        }


    }

    @Override
    public void onDetailHide() {
        super.onDetailHide();

        showChart(false);

        if (timer1 != null) {
            timer1.cancel();
            timer1 = null;
        }

        if (demm!=null) {
            SuplaApp.getApp().CancelAllRestApiClientTasks(true);
            demm.setDelegate(null);
        }
    }

    @Override
    public void onRestApiTaskStarted(SuplaRestApiClientTask task) {
        Trace.d("EM", "DOWNLOAD STARTED");
        emProgress.setVisibility(VISIBLE);
        chartHelper.setDownloadProgress(0d);
    }

    @Override
    public void onRestApiTaskFinished(SuplaRestApiClientTask task) {
        Trace.d("EM", "DOWNLOAD FINISHED");
        emProgress.setVisibility(INVISIBLE);
        chartHelper.setDownloadProgress(null);
        if (chartHelper.isVisible()) {
            chartHelper.load(getRemoteId());
        }
        demm = null;
    }

    @Override
    public void onRestApiTaskProgressUpdate(SuplaRestApiClientTask task, Double progress) {
        if (chartHelper.isVisible()) {
            chartHelper.setDownloadProgress(progress);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (ivGraph == null || ivGraph.getTag() == null) {
            return;
        }

        chartHelper.load(getRemoteId(), position);
        chartHelper.setVisibility(VISIBLE);
        chartHelper.animate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

