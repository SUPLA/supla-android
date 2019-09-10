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

    private TextView tvTotalActiveEnergy;
    private TextView tvlTotalActiveEnergy;
    private TextView tvCurrentConsumptionProduction;
    private TextView tvlCurrentConsumptionProduction;
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
    private Button btnPhase123;

    final Handler mHandler = new Handler();
    ElectricityChartHelper chartHelper;
    private ImageView ivGraph;
    private ImageView ivDirection;
    private LinearLayout llDetails;
    private RelativeLayout rlButtons1;
    private RelativeLayout rlButtons2;
    private Spinner emSpinnerMaster;
    private Spinner emSpinnerSlave;
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

        tvTotalActiveEnergy = findViewById(R.id.emtv_TotalActiveEnergy);
        tvlTotalActiveEnergy = findViewById(R.id.emtv_lTotalActiveEnergy);
        tvCurrentConsumptionProduction = findViewById(R.id.emtv_CurrentConsumptionProduction);
        tvlCurrentConsumptionProduction = findViewById(R.id.emtv_lCurrentConsumptionProduction);
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
        btnPhase123 = findViewById(R.id.embtn_Phase123);

        btnPhase1.setOnClickListener(this);
        btnPhase2.setOnClickListener(this);
        btnPhase3.setOnClickListener(this);
        btnPhase123.setOnClickListener(this);

        btnPhase1.setTag(1);
        btnPhase2.setTag(2);
        btnPhase3.setTag(3);
        btnPhase123.setTag(0);

        llDetails = findViewById(R.id.emllDetails);
        rlButtons1 = findViewById(R.id.emrlButtons1);
        rlButtons2 = findViewById(R.id.emrlButtons2);

        chartHelper = new ElectricityChartHelper(getContext());
        chartHelper.setCombinedChart((CombinedChart) findViewById(R.id.emCombinedChart));
        chartHelper.setPieChart((PieChart) findViewById(R.id.emPieChart));
        chartHelper.setUnit("kWh");

        emSpinnerMaster = findViewById(R.id.emSpinnerMaster);
        emSpinnerMaster.setOnItemSelectedListener(this);

        emSpinnerSlave = findViewById(R.id.emSpinnerSlave);
        updateSlaveSpinnerItems();

        ivGraph = findViewById(R.id.emGraphImg);
        ivGraph.bringToFront();
        ivGraph.setOnClickListener(this);

        ivDirection = findViewById(R.id.emDirectionImg);
        ivDirection.bringToFront();
        ivDirection.setOnClickListener(this);

        emProgress = findViewById(R.id.emProgressBar);

        showChart(false);
    }

    private void updateSlaveSpinnerItems() {
        emSpinnerSlave.setOnItemSelectedListener(null);

        String[] items = chartHelper.getSlaveSpinnerItems(emSpinnerMaster);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item, items);
        emSpinnerSlave.setAdapter(adapter);
        emSpinnerSlave.setVisibility(items.length > 0 ? VISIBLE : GONE);

        emSpinnerSlave.setOnItemSelectedListener(this);
    }

    private void setImgBackground(ImageView img, int bg) {
        Drawable d = getResources().getDrawable(bg);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            img.setBackground(d);
        } else {
            img.setBackgroundDrawable(d);
        }
    }

    private void showChart(boolean show) {

        int bg;

        if (show) {
            llDetails.setVisibility(GONE);
            chartHelper.setVisibility(VISIBLE);
            rlButtons1.setVisibility(INVISIBLE);
            rlButtons2.setVisibility(VISIBLE);
            setImgBackground(ivGraph, R.drawable.graphon);
            ivGraph.setTag(1);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(),
                    android.R.layout.simple_spinner_item, chartHelper.getMasterSpinnerItems(0));
            emSpinnerMaster.setAdapter(adapter);

            postDelayed(new Runnable() {
                public void run() {
                    onItemSelected(null, null,
                            emSpinnerMaster.getSelectedItemPosition(),
                            emSpinnerMaster.getSelectedItemId());
                }
            }, 50);

        } else {
            llDetails.setVisibility(VISIBLE);
            chartHelper.setVisibility(GONE);
            chartHelper.clearData();
            rlButtons1.setVisibility(VISIBLE);
            rlButtons2.setVisibility(INVISIBLE);
            setImgBackground(ivGraph, R.drawable.graphoff);
            ivGraph.setTag(null);
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

    private void displayMeasurementDetail(long vars, int var1, int var2, TextView tv1, TextView tv2) {
        tv1.setVisibility((vars & var1) > 0 || (vars & var2) > 0 ? VISIBLE : GONE);
        tv2.setVisibility(tv1.getVisibility());
    }

    private void displayMeasurementDetail(long vars, int var1, TextView tv1, TextView tv2) {
        displayMeasurementDetail(vars, var1, 0, tv1, tv2);
    }

    private void displayMeasurementDetails(long vars, boolean sum) {
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FREQ,
                tvFreq, tvlFreq);

        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_VOLTAGE,
                tvVoltage, tvlVoltage);

        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_CURRENT,
                sum ? 0 : SuplaConst.EM_VAR_CURRENT_OVER_65A,
                tvCurrent, tvlCurrent);

        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_ACTIVE,
                tvPowerActive, tvlPowerActive);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_REACTIVE,
                tvPowerReactive, tvlPowerReactive);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_APPARENT,
                tvPowerApparent, tvlPowerApparent);

        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_POWER_FACTOR,
                tvPowerFactor, tvlPowerFactor);
        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_PHASE_ANGLE,
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
        setBtnBackground(btnPhase123, R.drawable.em_phase_btn_black);

        String empty = "----";
        tvTotalActiveEnergy.setText(empty);
        tvCurrentConsumptionProduction.setText(empty);
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

            double currentConsumption;
            double currentProduction;
            double currentCost;

            DbHelper mDBH = new DbHelper(getContext(), true);

            if (mDBH.electricityMeterMeasurementsStartsWithTheCurrentMonth(channel.getChannelId())) {
                currentConsumption = sum.getTotalForwardActiveEnergy();
                currentProduction = sum.getTotalReverseActiveEnergy();
                currentCost = em.getTotalCost();
            } else {
                double v0 = mDBH.getLastElectricityMeterMeasurementValue(0,
                        channel.getChannelId(), false);
                double v1 = mDBH.getLastElectricityMeterMeasurementValue(-1,
                        channel.getChannelId(), false);
                currentConsumption = v0-v1;
                currentCost = currentConsumption * em.getPricePerUnit();

                v0 = mDBH.getLastElectricityMeterMeasurementValue(0,
                        channel.getChannelId(), true);
                v1 = mDBH.getLastElectricityMeterMeasurementValue(-1,
                        channel.getChannelId(), true);
                currentProduction = v0-v1;
            }

            if (chartHelper.isProductionDataSource()) {
                tvTotalActiveEnergy
                        .setText(getActiveEnergyFormattedString(sum.getTotalReverseActiveEnergy()));
                tvCurrentConsumptionProduction.setText(String.format("%.2f kWh", currentProduction));
            } else {
                tvTotalActiveEnergy
                        .setText(getActiveEnergyFormattedString(sum.getTotalForwardActiveEnergy()));
                tvCurrentConsumptionProduction.setText(String.format("%.2f kWh", currentConsumption));
            }

            tvTotalCost.setText(String.format("%.2f "+em.getCurrency(), em.getTotalCost()));
            tvCurrentCost.setText(String.format("%.2f "+em.getCurrency(),
                    currentCost));

            Button btn = null;
            switch (phase) {
                case 1: btn = btnPhase1; break;
                case 2: btn = btnPhase2; break;
                case 3: btn = btnPhase3; break;
                default: btn = btnPhase123; break;
            }

            double freq = 0;
            double voltage = 0;
            double current = 0;
            double powerActive = 0;
            double powerReactive = 0;
            double powerApparent = 0;
            double powerFactor = 0;
            double phaseAngle = 0;
            double totalFAE = 0;
            double totalRAE = 0;
            double totalFRE = 0;
            double totalRRE = 0;

            for(int p=1;p<=3;p++) {

                if (phase > 0) {
                    p = phase;
                }

                SuplaChannelElectricityMeterValue.Measurement m = em.getMeasurement(p, 0);
                if (m!= null) {

                    sum = em.getSummary(p);

                    freq = m.getFreq();
                    if (voltage == 0) {
                        voltage = m.getVoltage();
                    }
                    current = m.getCurrent(em.currentIsOver65A());
                    powerActive += m.getPowerActive();
                    powerReactive += m.getPowerReactive();
                    powerApparent += m.getPowerApparent();
                    powerFactor = m.getPowerFactor();
                    phaseAngle = m.getPhaseAngle();
                    totalFAE += sum.getTotalForwardActiveEnergy();
                    totalRAE += sum.getTotalReverseActiveEnergy();
                    totalFRE += sum.getTotalForwardReactiveEnergy();
                    totalRRE += sum.getTotalReverseReactiveEnergy();
                }

                if (phase > 0) {
                    break;
                }
            }

            setBtnBackground(btn, voltage > 0 ? R.drawable.em_phase_btn_green : R.drawable.em_phase_btn_red);
            tvFreq.setText(format("%.2f Hz", freq));
            tvVoltage.setText(format("%.2f V", voltage));
            tvCurrent.setText(format("%."
                    +Integer.toString(em.currentIsOver65A() ? 2 : 3)+"f A", current));
            tvPowerActive.setText(format("%.5f W", powerActive));
            tvPowerReactive.setText(format("%.5f var", powerReactive));
            tvPowerApparent.setText(format("%.5f VA", powerApparent));
            tvPowerFactor.setText(format("%.3f", powerFactor));
            tvPhaseAngle.setText(format("%.2f\u00B0", phaseAngle));
            tvPhaseForwardActiveEnergy.setText(format("%.5f kWh", totalFAE));
            tvPhaseReverseActiveEnergy.setText(format("%.5f kWh", totalRAE));
            tvPhaseForwardReactiveEnergy.setText(format("%.5f kvarh", totalFRE));
            tvPhaseReverseReactiveEnergy.setText(format("%.5f kvarh", totalRRE));

            chartHelper.setTotalActiveEnergy(
                    em.getTotalActiveEnergyForAllPhases(chartHelper.isProductionDataSource()));
        }

        displayMeasurementDetails(vars, phase == 0);
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
        } else if (v == ivDirection) {
            setProductionDataSource(!chartHelper.isProductionDataSource(), true);
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

    private void setProductionDataSource(boolean production, boolean reload) {
        chartHelper.setProductionDataSource(production);

        if (production) {
            tvlTotalActiveEnergy.setText(R.string.em_reverse_active_energy);
            tvlCurrentConsumptionProduction.setText(R.string.current_production);
            setImgBackground(ivDirection, R.drawable.production);
        } else {
            tvlTotalActiveEnergy.setText(R.string.em_total_forward_avtive_energy);
            tvlCurrentConsumptionProduction.setText(R.string.current_consumption);
            setImgBackground(ivDirection, R.drawable.consumption);
        }

        if (reload) {
            OnChannelDataChanged();
            if (chartHelper.isVisible()) {
                showChart(true);
            }
        }
    }

    @Override
    public void onDetailShow() {
        super.onDetailShow();

        emProgress.setVisibility(INVISIBLE);
        setProductionDataSource(false, false);
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
            }, 0, 120000);
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

        if (parent != emSpinnerSlave) {
            updateSlaveSpinnerItems();
        }

        chartHelper.setDateRangeBySpinners(emSpinnerMaster, emSpinnerSlave);
        chartHelper.load(getRemoteId(), emSpinnerMaster.getSelectedItemPosition());
        chartHelper.setVisibility(VISIBLE);
        chartHelper.animate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

