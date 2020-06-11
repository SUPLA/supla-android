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

    final Handler mHandler = new Handler();
    ElectricityChartHelper chartHelper;
    private Integer phase;
    private TextView tvTotalActiveEnergy;
    private TextView tvlTotalActiveEnergy;
    private TextView tvCurrentConsumptionProduction;
    private TextView tvlCurrentConsumptionProduction;
    private TextView tvCurrentCost;
    private TextView tvTotalCost;
    private TextView tvFreq;
    private TextView tvlFreq;
    private TextView tvuFreq;
    private TextView tvVoltage;
    private TextView tvlVoltage;
    private TextView tvuVoltage;
    private TextView tvCurrent;
    private TextView tvlCurrent;
    private TextView tvuCurrent;
    private TextView tvPowerActive;
    private TextView tvlPowerActive;
    private TextView tvuPowerActive;
    private TextView tvPowerReactive;
    private TextView tvlPowerReactive;
    private TextView tvuPowerReactive;
    private TextView tvPowerApparent;
    private TextView tvlPowerApparent;
    private TextView tvuPowerApparent;
    private TextView tvPowerFactor;
    private TextView tvlPowerFactor;
    private TextView tvuPowerFactor;
    private TextView tvPhaseAngle;
    private TextView tvlPhaseAngle;
    private TextView tvuPhaseAngle;
    private TextView tvPhaseForwardActiveEnergy;
    private TextView tvlPhaseForwardActiveEnergy;
    private TextView tvuPhaseForwardActiveEnergy;
    private TextView tvPhaseReverseActiveEnergy;
    private TextView tvlPhaseReverseActiveEnergy;
    private TextView tvuPhaseReverseActiveEnergy;
    private TextView tvPhaseForwardReactiveEnergy;
    private TextView tvlPhaseForwardReactiveEnergy;
    private TextView tvuPhaseForwardReactiveEnergy;
    private TextView tvPhaseReverseReactiveEnergy;
    private TextView tvlPhaseReverseReactiveEnergy;
    private TextView tvuPhaseReverseReactiveEnergy;

    private TextView tvFreq1;
    private TextView tvFreq2;
    private TextView tvFreq3;
    private TextView tvlFreq1;
    private TextView tvuFreq1;
    private TextView tvVoltage1;
    private TextView tvVoltage2;
    private TextView tvVoltage3;
    private TextView tvlVoltage1;
    private TextView tvuVoltage1;
    private TextView tvCurrent1;
    private TextView tvCurrent2;
    private TextView tvCurrent3;
    private TextView tvlCurrent1;
    private TextView tvuCurrent1;
    private TextView tvPowerActive1;
    private TextView tvPowerActive2;
    private TextView tvPowerActive3;
    private TextView tvlPowerActive1;
    private TextView tvuPowerActive1;
    private TextView tvPowerReactive1;
    private TextView tvPowerReactive2;
    private TextView tvPowerReactive3;
    private TextView tvlPowerReactive1;
    private TextView tvuPowerReactive1;
    private TextView tvPowerApparent1;
    private TextView tvPowerApparent2;
    private TextView tvPowerApparent3;
    private TextView tvlPowerApparent1;
    private TextView tvuPowerApparent1;
    private TextView tvPowerFactor1;
    private TextView tvPowerFactor2;
    private TextView tvPowerFactor3;
    private TextView tvlPowerFactor1;
    private TextView tvuPowerFactor1;
    private TextView tvPhaseAngle1;
    private TextView tvPhaseAngle2;
    private TextView tvPhaseAngle3;
    private TextView tvlPhaseAngle1;
    private TextView tvuPhaseAngle1;
    private TextView tvPhaseForwardActiveEnergy1;
    private TextView tvPhaseForwardActiveEnergy2;
    private TextView tvPhaseForwardActiveEnergy3;
    private TextView tvlPhaseForwardActiveEnergy1;
    private TextView tvuPhaseForwardActiveEnergy1;
    private TextView tvPhaseReverseActiveEnergy1;
    private TextView tvPhaseReverseActiveEnergy2;
    private TextView tvPhaseReverseActiveEnergy3;
    private TextView tvlPhaseReverseActiveEnergy1;
    private TextView tvuPhaseReverseActiveEnergy1;
    private TextView tvPhaseForwardReactiveEnergy1;
    private TextView tvPhaseForwardReactiveEnergy2;
    private TextView tvPhaseForwardReactiveEnergy3;
    private TextView tvlPhaseForwardReactiveEnergy1;
    private TextView tvuPhaseForwardReactiveEnergy1;
    private TextView tvPhaseReverseReactiveEnergy1;
    private TextView tvPhaseReverseReactiveEnergy2;
    private TextView tvPhaseReverseReactiveEnergy3;
    private TextView tvlPhaseReverseReactiveEnergy1;
    private TextView tvuPhaseReverseReactiveEnergy1;

    private TextView tvPhaseForwardActiveEnergyBalanced;
    private TextView tvlPhaseForwardActiveEnergyBalanced;
    private TextView tvuPhaseForwardActiveEnergyBalanced;
    private TextView tvPhaseReverseActiveEnergyBalanced;
    private TextView tvlPhaseReverseActiveEnergyBalanced;
    private TextView tvuPhaseReverseActiveEnergyBalanced;
    private ImageView emImgIcon;
    private Button btnPhase1;
    private Button btnPhase2;
    private Button btnPhase3;
    private Button btnPhase123;
    private Button btnPhase1v2v3;
    private ImageView ivGraph;
    private ImageView ivDirection;
    private LinearLayout llDetails;
    private LinearLayout llDetails2;
    private LinearLayout llDetails3;
    private RelativeLayout rlButtons1;
    private RelativeLayout rlButtons2;
    private Spinner emSpinnerMaster;
    private Spinner emSpinnerSlave;
    private ProgressBar emProgress;
    private LinearLayout llBalance;
    private TextView tvlBalance;
    private Timer timer1;
    private DownloadElectricityMeterMeasurements demm = null;
    private boolean mBalanceAvailable;

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
        tvuFreq = findViewById(R.id.emtv_uFreq);
        tvVoltage = findViewById(R.id.emtv_Voltage);
        tvlVoltage = findViewById(R.id.emtv_lVoltage);
        tvuVoltage = findViewById(R.id.emtv_uVoltage);
        tvCurrent = findViewById(R.id.emtv_Current);
        tvlCurrent = findViewById(R.id.emtv_lCurrent);
        tvuCurrent = findViewById(R.id.emtv_uCurrent);
        tvPowerActive = findViewById(R.id.emtv_PowerActive);
        tvlPowerActive = findViewById(R.id.emtv_lPowerActive);
        tvuPowerActive = findViewById(R.id.emtv_uPowerActive);
        tvPowerReactive = findViewById(R.id.emtv_PowerReactive);
        tvlPowerReactive = findViewById(R.id.emtv_lPowerReactive);
        tvuPowerReactive = findViewById(R.id.emtv_uPowerReactive);
        tvPowerApparent = findViewById(R.id.emtv_PowerApparent);
        tvlPowerApparent = findViewById(R.id.emtv_lPowerApparent);
        tvuPowerApparent = findViewById(R.id.emtv_uPowerApparent);
        tvPowerFactor = findViewById(R.id.emtv_PowerFactor);
        tvlPowerFactor = findViewById(R.id.emtv_lPowerFactor);
        tvuPowerFactor = findViewById(R.id.emtv_uPowerFactor);
        tvPhaseAngle = findViewById(R.id.emtv_PhaseAngle);
        tvlPhaseAngle = findViewById(R.id.emtv_lPhaseAngle);
        tvuPhaseAngle = findViewById(R.id.emtv_uPhaseAngle);
        tvPhaseForwardActiveEnergy = findViewById(R.id.emtv_PhaseForwardActiveEnergy);
        tvlPhaseForwardActiveEnergy = findViewById(R.id.emtv_lPhaseForwardActiveEnergy);
        tvuPhaseForwardActiveEnergy = findViewById(R.id.emtv_uPhaseForwardActiveEnergy);
        tvPhaseReverseActiveEnergy = findViewById(R.id.emtv_PhaseReverseActiveEnergy);
        tvlPhaseReverseActiveEnergy = findViewById(R.id.emtv_lPhaseReverseActiveEnergy);
        tvuPhaseReverseActiveEnergy = findViewById(R.id.emtv_uPhaseReverseActiveEnergy);
        tvPhaseForwardReactiveEnergy = findViewById(R.id.emtv_PhaseForwardRectiveEnergy);
        tvlPhaseForwardReactiveEnergy = findViewById(R.id.emtv_lPhaseForwardRectiveEnergy);
        tvuPhaseForwardReactiveEnergy = findViewById(R.id.emtv_uPhaseForwardRectiveEnergy);
        tvPhaseReverseReactiveEnergy = findViewById(R.id.emtv_PhaseReverseRectiveEnergy);
        tvlPhaseReverseReactiveEnergy = findViewById(R.id.emtv_lPhaseReverseRectiveEnergy);
        tvuPhaseReverseReactiveEnergy = findViewById(R.id.emtv_uPhaseReverseRectiveEnergy);

        tvPhaseForwardActiveEnergyBalanced =
                findViewById(R.id.emtv_PhaseForwardActiveEnergyBalanced);
        tvlPhaseForwardActiveEnergyBalanced =
                findViewById(R.id.emtv_lPhaseForwardActiveEnergyBalanced);
        tvuPhaseForwardActiveEnergyBalanced =
                findViewById(R.id.emtv_uPhaseForwardActiveEnergyBalanced);
        tvPhaseReverseActiveEnergyBalanced =
                findViewById(R.id.emtv_PhaseReverseActiveEnergyBalanced);
        tvlPhaseReverseActiveEnergyBalanced =
                findViewById(R.id.emtv_lPhaseReverseActiveEnergyBalanced);
        tvuPhaseReverseActiveEnergyBalanced =
                findViewById(R.id.emtv_uPhaseReverseActiveEnergyBalanced);

        tvFreq1 = findViewById(R.id.emtv_Freq1);
        tvFreq2 = findViewById(R.id.emtv_Freq2);
        tvFreq3 = findViewById(R.id.emtv_Freq3);
        tvlFreq1 = findViewById(R.id.emtv_lFreq1);
        tvuFreq1 = findViewById(R.id.emtv_uFreq1);

        tvVoltage1 = findViewById(R.id.emtv_Voltage1);
        tvVoltage2 = findViewById(R.id.emtv_Voltage2);
        tvVoltage3 = findViewById(R.id.emtv_Voltage3);
        tvlVoltage1 = findViewById(R.id.emtv_lVoltage1);
        tvuVoltage1 = findViewById(R.id.emtv_uVoltage1);

        tvCurrent1 = findViewById(R.id.emtv_Current1);
        tvCurrent2 = findViewById(R.id.emtv_Current2);
        tvCurrent3 = findViewById(R.id.emtv_Current3);
        tvlCurrent1 = findViewById(R.id.emtv_lCurrent1);
        tvuCurrent1 = findViewById(R.id.emtv_uCurrent1);

        tvPowerActive1 = findViewById(R.id.emtv_PowerActive1);
        tvPowerActive2 = findViewById(R.id.emtv_PowerActive2);
        tvPowerActive3 = findViewById(R.id.emtv_PowerActive3);
        tvlPowerActive1 = findViewById(R.id.emtv_lPowerActive1);
        tvuPowerActive1 = findViewById(R.id.emtv_uPowerActive1);

        tvPowerReactive1 = findViewById(R.id.emtv_PowerReactive1);
        tvPowerReactive2 = findViewById(R.id.emtv_PowerReactive2);
        tvPowerReactive3 = findViewById(R.id.emtv_PowerReactive3);
        tvlPowerReactive1 = findViewById(R.id.emtv_lPowerReactive1);
        tvuPowerReactive1 = findViewById(R.id.emtv_uPowerReactive1);

        tvPowerApparent1 = findViewById(R.id.emtv_PowerApparent1);
        tvPowerApparent2 = findViewById(R.id.emtv_PowerApparent2);
        tvPowerApparent3 = findViewById(R.id.emtv_PowerApparent3);
        tvlPowerApparent1 = findViewById(R.id.emtv_lPowerApparent1);
        tvuPowerApparent1 = findViewById(R.id.emtv_uPowerApparent1);

        tvPowerFactor1 = findViewById(R.id.emtv_PowerFactor1);
        tvPowerFactor2 = findViewById(R.id.emtv_PowerFactor2);
        tvPowerFactor3 = findViewById(R.id.emtv_PowerFactor3);
        tvlPowerFactor1 = findViewById(R.id.emtv_lPowerFactor1);
        tvuPowerFactor1 = findViewById(R.id.emtv_uPowerFactor1);

        tvPhaseAngle1 = findViewById(R.id.emtv_PhaseAngle1);
        tvPhaseAngle2 = findViewById(R.id.emtv_PhaseAngle2);
        tvPhaseAngle3 = findViewById(R.id.emtv_PhaseAngle3);
        tvlPhaseAngle1 = findViewById(R.id.emtv_lPhaseAngle1);
        tvuPhaseAngle1 = findViewById(R.id.emtv_uPhaseAngle1);

        tvPhaseForwardActiveEnergy1 = findViewById(R.id.emtv_PhaseForwardActiveEnergy1);
        tvPhaseForwardActiveEnergy2 = findViewById(R.id.emtv_PhaseForwardActiveEnergy2);
        tvPhaseForwardActiveEnergy3 = findViewById(R.id.emtv_PhaseForwardActiveEnergy3);
        tvlPhaseForwardActiveEnergy1 = findViewById(R.id.emtv_lPhaseForwardActiveEnergy1);
        tvuPhaseForwardActiveEnergy1 = findViewById(R.id.emtv_uPhaseForwardActiveEnergy1);

        tvPhaseReverseActiveEnergy1 = findViewById(R.id.emtv_PhaseReverseActiveEnergy1);
        tvPhaseReverseActiveEnergy2 = findViewById(R.id.emtv_PhaseReverseActiveEnergy2);
        tvPhaseReverseActiveEnergy3 = findViewById(R.id.emtv_PhaseReverseActiveEnergy3);
        tvlPhaseReverseActiveEnergy1 = findViewById(R.id.emtv_lPhaseReverseActiveEnergy1);
        tvuPhaseReverseActiveEnergy1 = findViewById(R.id.emtv_uPhaseReverseActiveEnergy1);

        tvPhaseForwardReactiveEnergy1 = findViewById(R.id.emtv_PhaseForwardRectiveEnergy1);
        tvPhaseForwardReactiveEnergy2 = findViewById(R.id.emtv_PhaseForwardRectiveEnergy2);
        tvPhaseForwardReactiveEnergy3 = findViewById(R.id.emtv_PhaseForwardRectiveEnergy3);
        tvlPhaseForwardReactiveEnergy1 = findViewById(R.id.emtv_lPhaseForwardRectiveEnergy1);
        tvuPhaseForwardReactiveEnergy1 = findViewById(R.id.emtv_uPhaseForwardRectiveEnergy1);

        tvPhaseReverseReactiveEnergy1 = findViewById(R.id.emtv_PhaseReverseRectiveEnergy1);
        tvPhaseReverseReactiveEnergy2 = findViewById(R.id.emtv_PhaseReverseRectiveEnergy2);
        tvPhaseReverseReactiveEnergy3 = findViewById(R.id.emtv_PhaseReverseRectiveEnergy3);
        tvlPhaseReverseReactiveEnergy1 = findViewById(R.id.emtv_lPhaseReverseRectiveEnergy1);
        tvuPhaseReverseReactiveEnergy1 = findViewById(R.id.emtv_uPhaseReverseRectiveEnergy1);


        emImgIcon = findViewById(R.id.emimgIcon);

        llBalance = findViewById(R.id.emtv_llBalance);
        tvlBalance = findViewById(R.id.emtv_lBalance);

        btnPhase1 = findViewById(R.id.embtn_Phase1);
        btnPhase2 = findViewById(R.id.embtn_Phase2);
        btnPhase3 = findViewById(R.id.embtn_Phase3);
        btnPhase123 = findViewById(R.id.embtn_Phase123);
        btnPhase1v2v3 = findViewById(R.id.embtn_Phase1v2v3);

        btnPhase1.setOnClickListener(this);
        btnPhase2.setOnClickListener(this);
        btnPhase3.setOnClickListener(this);
        btnPhase123.setOnClickListener(this);
        btnPhase1v2v3.setOnClickListener(this);

        btnPhase1.setTag(1);
        btnPhase2.setTag(2);
        btnPhase3.setTag(3);
        btnPhase123.setTag(0);
        btnPhase1v2v3.setTag(-1);

        llDetails = findViewById(R.id.emllDetails);
        llDetails2 = findViewById(R.id.emllDetails2);
        llDetails3 = findViewById(R.id.emllDetails3);
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
                    android.R.layout.simple_spinner_item,
                    chartHelper.getMasterSpinnerItems(mBalanceAvailable ? 0 : 19));
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
        return format("%." + Integer.toString(precision) + "f kWh", energy);
    }

    private void displayMeasurementDetail(long vars, int var1, int var2, TextView tv1, TextView tv2, TextView tv3) {
        tv1.setVisibility((vars & var1) > 0 || (vars & var2) > 0 ? VISIBLE : GONE);
        tv2.setVisibility(tv1.getVisibility());
        tv3.setVisibility(tv2.getVisibility());
    }

    private void displayMeasurementDetail(long vars, int var1, TextView tv1, TextView tv2, TextView tv3) {
        displayMeasurementDetail(vars, var1, 0, tv1, tv2, tv3);
    }

    private void displayMeasurementDetails(long vars, boolean sum) {
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FREQ,
                tvFreq, tvlFreq, tvuFreq);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FREQ,
                tvFreq1, tvlFreq1, tvuFreq1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FREQ,
                tvFreq2, tvlFreq1, tvuFreq1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FREQ,
                tvFreq3, tvlFreq1, tvuFreq1);

        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_VOLTAGE,
                tvVoltage, tvlVoltage, tvuVoltage);
        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_VOLTAGE,
                tvVoltage1, tvlVoltage1, tvuVoltage1);
        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_VOLTAGE,
                tvVoltage2, tvlVoltage1, tvuVoltage1);
        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_VOLTAGE,
                tvVoltage3, tvlVoltage1, tvuVoltage1);

        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_CURRENT,
                sum ? 0 : SuplaConst.EM_VAR_CURRENT_OVER_65A,
                tvCurrent, tvlCurrent, tvuCurrent);
        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_CURRENT,
                sum ? 0 : SuplaConst.EM_VAR_CURRENT_OVER_65A,
                tvCurrent1, tvlCurrent1, tvuCurrent1);
        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_CURRENT,
                sum ? 0 : SuplaConst.EM_VAR_CURRENT_OVER_65A,
                tvCurrent2, tvlCurrent1, tvuCurrent1);
        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_CURRENT,
                sum ? 0 : SuplaConst.EM_VAR_CURRENT_OVER_65A,
                tvCurrent3, tvlCurrent1, tvuCurrent1);

        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_ACTIVE,
                tvPowerActive, tvlPowerActive, tvuPowerActive);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_ACTIVE,
                tvPowerActive1, tvlPowerActive1, tvuPowerActive1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_ACTIVE,
                tvPowerActive2, tvlPowerActive1, tvuPowerActive1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_ACTIVE,
                tvPowerActive3, tvlPowerActive1, tvuPowerActive1);

        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_REACTIVE,
                tvPowerReactive, tvlPowerReactive, tvuPowerReactive);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_REACTIVE,
                tvPowerReactive1, tvlPowerReactive1, tvuPowerReactive1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_REACTIVE,
                tvPowerReactive2, tvlPowerReactive1, tvuPowerReactive1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_REACTIVE,
                tvPowerReactive3, tvlPowerReactive1, tvuPowerReactive1);

        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_APPARENT,
                tvPowerApparent, tvlPowerApparent, tvuPowerApparent);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_APPARENT,
                tvPowerApparent1, tvlPowerApparent1, tvuPowerApparent1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_APPARENT,
                tvPowerApparent2, tvlPowerApparent1, tvuPowerApparent1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_POWER_APPARENT,
                tvPowerApparent3, tvlPowerApparent1, tvuPowerApparent1);

        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_POWER_FACTOR,
                tvPowerFactor, tvlPowerFactor, tvuPowerFactor);
        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_POWER_FACTOR,
                tvPowerFactor1, tvlPowerFactor1, tvuPowerFactor1);
        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_POWER_FACTOR,
                tvPowerFactor2, tvlPowerFactor1, tvuPowerFactor1);
        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_POWER_FACTOR,
                tvPowerFactor3, tvlPowerFactor1, tvuPowerFactor1);

        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_PHASE_ANGLE,
                tvPhaseAngle, tvlPhaseAngle, tvuPhaseAngle);
        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_PHASE_ANGLE,
                tvPhaseAngle1, tvlPhaseAngle1, tvuPhaseAngle1);
        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_PHASE_ANGLE,
                tvPhaseAngle2, tvlPhaseAngle1, tvuPhaseAngle1);
        displayMeasurementDetail(vars, sum ? 0 : SuplaConst.EM_VAR_PHASE_ANGLE,
                tvPhaseAngle3, tvlPhaseAngle1, tvuPhaseAngle1);

        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FORWARD_ACTIVE_ENERGY,
                tvPhaseForwardActiveEnergy, tvlPhaseForwardActiveEnergy, tvuPhaseForwardActiveEnergy);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FORWARD_ACTIVE_ENERGY,
                tvPhaseForwardActiveEnergy1, tvlPhaseForwardActiveEnergy1, tvuPhaseForwardActiveEnergy1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FORWARD_ACTIVE_ENERGY,
                tvPhaseForwardActiveEnergy2, tvlPhaseForwardActiveEnergy1, tvuPhaseForwardActiveEnergy1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FORWARD_ACTIVE_ENERGY,
                tvPhaseForwardActiveEnergy3, tvlPhaseForwardActiveEnergy1, tvuPhaseForwardActiveEnergy1);

        displayMeasurementDetail(vars, SuplaConst.EM_VAR_REVERSE_ACTIVE_ENERGY,
                tvPhaseReverseActiveEnergy, tvlPhaseReverseActiveEnergy, tvuPhaseReverseActiveEnergy);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_REVERSE_ACTIVE_ENERGY,
                tvPhaseReverseActiveEnergy1, tvlPhaseReverseActiveEnergy1, tvuPhaseReverseActiveEnergy1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_REVERSE_ACTIVE_ENERGY,
                tvPhaseReverseActiveEnergy2, tvlPhaseReverseActiveEnergy1, tvuPhaseReverseActiveEnergy1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_REVERSE_ACTIVE_ENERGY,
                tvPhaseReverseActiveEnergy3, tvlPhaseReverseActiveEnergy1, tvuPhaseReverseActiveEnergy1);

        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FORWARD_REACTIVE_ENERGY,
                tvPhaseForwardReactiveEnergy, tvlPhaseForwardReactiveEnergy, tvuPhaseForwardReactiveEnergy);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FORWARD_REACTIVE_ENERGY,
                tvPhaseForwardReactiveEnergy1, tvlPhaseForwardReactiveEnergy1, tvuPhaseForwardReactiveEnergy1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FORWARD_REACTIVE_ENERGY,
                tvPhaseForwardReactiveEnergy2, tvlPhaseForwardReactiveEnergy1, tvuPhaseForwardReactiveEnergy1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_FORWARD_REACTIVE_ENERGY,
                tvPhaseForwardReactiveEnergy3, tvlPhaseForwardReactiveEnergy1, tvuPhaseForwardReactiveEnergy1);

        displayMeasurementDetail(vars, SuplaConst.EM_VAR_REVERSE_REACTIVE_ENERGY,
                tvPhaseReverseReactiveEnergy, tvlPhaseReverseReactiveEnergy, tvuPhaseReverseReactiveEnergy);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_REVERSE_REACTIVE_ENERGY,
                tvPhaseReverseReactiveEnergy1, tvlPhaseReverseReactiveEnergy1, tvuPhaseReverseReactiveEnergy1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_REVERSE_REACTIVE_ENERGY,
                tvPhaseReverseReactiveEnergy2, tvlPhaseReverseReactiveEnergy1, tvuPhaseReverseReactiveEnergy1);
        displayMeasurementDetail(vars, SuplaConst.EM_VAR_REVERSE_REACTIVE_ENERGY,
                tvPhaseReverseReactiveEnergy3, tvlPhaseReverseReactiveEnergy1, tvuPhaseReverseReactiveEnergy1);

        mBalanceAvailable = ((vars & SuplaConst.EM_VAR_FORWARD_ACTIVE_ENERGY_BALANCED) > 0
                || (vars & SuplaConst.EM_VAR_REVERSE_ACTIVE_ENERGY_BALANCED) > 0);

        if (sum && mBalanceAvailable) {
            llBalance.setVisibility(VISIBLE);
            tvlBalance.setVisibility(VISIBLE);

            displayMeasurementDetail(vars, SuplaConst.EM_VAR_FORWARD_ACTIVE_ENERGY_BALANCED,
                    tvPhaseForwardActiveEnergyBalanced, tvlPhaseForwardActiveEnergyBalanced, tvuPhaseForwardActiveEnergyBalanced);
            displayMeasurementDetail(vars, SuplaConst.EM_VAR_REVERSE_ACTIVE_ENERGY_BALANCED,
                    tvPhaseReverseActiveEnergyBalanced, tvlPhaseReverseActiveEnergyBalanced, tvuPhaseReverseActiveEnergyBalanced);
        } else {
            llBalance.setVisibility(INVISIBLE);
            tvlBalance.setVisibility(INVISIBLE);
        }

    }

    public void channelExtendedDataToViews(boolean setIcon) {

        Channel channel = (Channel) getChannelFromDatabase();

        if (setIcon) {
            emImgIcon.setBackgroundColor(Color.TRANSPARENT);
            emImgIcon.setImageBitmap(ImageCache.getBitmap(getContext(), channel.getImageIdx()));
        }

        ChannelExtendedValue cev = channel.getExtendedValue();

        setBtnBackground(btnPhase1, R.drawable.em_phase_btn_black);
        setBtnBackground(btnPhase2, R.drawable.em_phase_btn_black);
        setBtnBackground(btnPhase3, R.drawable.em_phase_btn_black);
        setBtnBackground(btnPhase123, R.drawable.em_phase_btn_black);
        setBtnBackground(btnPhase1v2v3, R.drawable.em_phase_btn_black);

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
        tvPhaseForwardActiveEnergyBalanced.setText(empty);
        tvPhaseReverseActiveEnergyBalanced.setText(empty);

        tvFreq1.setText(empty);
        tvFreq2.setText(empty);
        tvFreq3.setText(empty);
        tvVoltage1.setText(empty);
        tvVoltage2.setText(empty);
        tvVoltage3.setText(empty);
        tvCurrent1.setText(empty);
        tvCurrent2.setText(empty);
        tvCurrent3.setText(empty);
        tvPowerActive1.setText(empty);
        tvPowerActive2.setText(empty);
        tvPowerActive3.setText(empty);
        tvPowerReactive1.setText(empty);
        tvPowerReactive2.setText(empty);
        tvPowerReactive3.setText(empty);
        tvPowerApparent1.setText(empty);
        tvPowerApparent2.setText(empty);
        tvPowerApparent3.setText(empty);
        tvPowerFactor1.setText(empty);
        tvPowerFactor2.setText(empty);
        tvPowerFactor3.setText(empty);
        tvPhaseAngle1.setText(empty);
        tvPhaseAngle2.setText(empty);
        tvPhaseAngle3.setText(empty);
        tvPhaseForwardActiveEnergy1.setText(empty);
        tvPhaseForwardActiveEnergy2.setText(empty);
        tvPhaseForwardActiveEnergy3.setText(empty);
        tvPhaseReverseActiveEnergy1.setText(empty);
        tvPhaseReverseActiveEnergy2.setText(empty);
        tvPhaseReverseActiveEnergy3.setText(empty);
        tvPhaseForwardReactiveEnergy1.setText(empty);
        tvPhaseForwardReactiveEnergy2.setText(empty);
        tvPhaseForwardReactiveEnergy3.setText(empty);
        tvPhaseReverseReactiveEnergy1.setText(empty);
        tvPhaseReverseReactiveEnergy2.setText(empty);
        tvPhaseReverseReactiveEnergy3.setText(empty);

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
                currentConsumption = v0 - v1;
                currentCost = currentConsumption * em.getPricePerUnit();

                v0 = mDBH.getLastElectricityMeterMeasurementValue(0,
                        channel.getChannelId(), true);
                v1 = mDBH.getLastElectricityMeterMeasurementValue(-1,
                        channel.getChannelId(), true);
                currentProduction = v0 - v1;
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

            tvTotalCost.setText(String.format("%.2f " + em.getCurrency(), em.getTotalCost()));
            tvCurrentCost.setText(String.format("%.2f " + em.getCurrency(),
                    currentCost));

            Button btn = null;
            switch (phase) {
                case 1:
                    btn = btnPhase1;
                    break;
                case 2:
                    btn = btnPhase2;
                    break;
                case 3:
                    btn = btnPhase3;
                    break;
                case -1:
                    btn = btnPhase1v2v3;
                    break;
                default:
                    btn = btnPhase123;
                    break;
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

            for (int p = 1; p <= 3; p++) {

                if (phase > 0) {
                    p = phase;
                }

                SuplaChannelElectricityMeterValue.Measurement m = em.getMeasurement(p, 0);
                if (m != null) {

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
            tvFreq.setText(format("%.2f ", freq));
            tvVoltage.setText(format("%.2f ", voltage));
            tvCurrent.setText(format("%."
                    + Integer.toString(em.currentIsOver65A() ? 2 : 3) + "f ", current));
            tvPowerActive.setText(format("%.5f ", powerActive));
            tvPowerReactive.setText(format("%.5f ", powerReactive));
            tvPowerApparent.setText(format("%.5f ", powerApparent));
            tvPowerFactor.setText(format("%.3f ", powerFactor));
            tvPhaseAngle.setText(format("%.2f ", phaseAngle));
            tvPhaseForwardActiveEnergy.setText(format("%.5f ", totalFAE));
            tvPhaseReverseActiveEnergy.setText(format("%.5f ", totalRAE));
            tvPhaseForwardReactiveEnergy.setText(format("%.5f ", totalFRE));
            tvPhaseReverseReactiveEnergy.setText(format("%.5f ", totalRRE));

            tvFreq1.setText(format("%.2f ", em.getMeasurement(1, 0).getFreq()));
            tvFreq2.setText(format("%.2f ", em.getMeasurement(2, 0).getFreq()));
            tvFreq3.setText(format("%.2f ", em.getMeasurement(3, 0).getFreq()));

            tvVoltage1.setText(format("%.2f ", em.getMeasurement(1, 0).getVoltage()));
            tvVoltage2.setText(format("%.2f ", em.getMeasurement(2, 0).getVoltage()));
            tvVoltage3.setText(format("%.2f ", em.getMeasurement(3, 0).getVoltage()));

            tvCurrent1.setText(format("%.3f ", em.getMeasurement(1,0).getCurrent(em.currentIsOver65A())));
            tvCurrent2.setText(format("%.3f ", em.getMeasurement(2,0).getCurrent(em.currentIsOver65A())));
            tvCurrent3.setText(format("%.3f ", em.getMeasurement(3,0).getCurrent(em.currentIsOver65A())));

            tvPowerActive1.setText(format("%.1f ", em.getMeasurement(1,0).getPowerActive()));
            tvPowerActive2.setText(format("%.1f ", em.getMeasurement(2,0).getPowerActive()));
            tvPowerActive3.setText(format("%.1f ", em.getMeasurement(3,0).getPowerActive()));

            tvPowerReactive1.setText(format("%.1f ", em.getMeasurement(1,0).getPowerReactive()));
            tvPowerReactive2.setText(format("%.1f ", em.getMeasurement(2,0).getPowerReactive()));
            tvPowerReactive3.setText(format("%.1f ", em.getMeasurement(3,0).getPowerReactive()));

            tvPowerApparent1.setText(format("%.1f ", em.getMeasurement(1,0).getPowerApparent()));
            tvPowerApparent2.setText(format("%.1f ", em.getMeasurement(2,0).getPowerApparent()));
            tvPowerApparent3.setText(format("%.1f ", em.getMeasurement(3,0).getPowerApparent()));

            tvPowerFactor1.setText(format("%.3f ", em.getMeasurement(1,0).getPowerFactor()));
            tvPowerFactor2.setText(format("%.3f ", em.getMeasurement(2,0).getPowerFactor()));
            tvPowerFactor3.setText(format("%.3f ", em.getMeasurement(3,0).getPowerFactor()));

            tvPhaseAngle1.setText(format("%.2f ", em.getMeasurement(1,0).getPhaseAngle()));
            tvPhaseAngle2.setText(format("%.2f ", em.getMeasurement(2,0).getPhaseAngle()));
            tvPhaseAngle3.setText(format("%.2f ", em.getMeasurement(3,0).getPhaseAngle()));

            tvPhaseForwardActiveEnergy1.setText(format("%.1f ", em.getSummary(1).getTotalForwardActiveEnergy()));
            tvPhaseForwardActiveEnergy2.setText(format("%.1f ", em.getSummary(2).getTotalForwardActiveEnergy()));
            tvPhaseForwardActiveEnergy3.setText(format("%.1f ", em.getSummary(3).getTotalForwardActiveEnergy()));

            tvPhaseReverseActiveEnergy1.setText(format("%.1f ", em.getSummary(1).getTotalReverseActiveEnergy()));
            tvPhaseReverseActiveEnergy2.setText(format("%.1f ", em.getSummary(2).getTotalReverseActiveEnergy()));
            tvPhaseReverseActiveEnergy3.setText(format("%.1f ", em.getSummary(3).getTotalReverseActiveEnergy()));

            tvPhaseForwardReactiveEnergy1.setText(format("%.1f ", em.getSummary(1).getTotalForwardReactiveEnergy()));
            tvPhaseForwardReactiveEnergy2.setText(format("%.1f ", em.getSummary(2).getTotalForwardReactiveEnergy()));
            tvPhaseForwardReactiveEnergy3.setText(format("%.1f ", em.getSummary(3).getTotalForwardReactiveEnergy()));

            tvPhaseReverseReactiveEnergy1.setText(format("%.1f ", em.getSummary(1).getTotalReverseReactiveEnergy()));
            tvPhaseReverseReactiveEnergy2.setText(format("%.1f ", em.getSummary(2).getTotalReverseReactiveEnergy()));
            tvPhaseReverseReactiveEnergy3.setText(format("%.1f ", em.getSummary(3).getTotalReverseReactiveEnergy()));

            tvPhaseForwardActiveEnergyBalanced.setText(format("%.5f ",
                    em.getTotalForwardActiveEnergyBalanced()));
            tvPhaseReverseActiveEnergyBalanced.setText(format("%.5f ",
                    em.getTotalReverseActiveEnergyBalanced()));

            chartHelper.setTotalActiveEnergy(
                    em.getTotalActiveEnergyForAllPhases(chartHelper.isProductionDataSource()));

            if(phase==-1) {
                llDetails2.setVisibility(View.GONE);
                llDetails3.setVisibility(View.VISIBLE);
            }else{
                llDetails2.setVisibility(View.VISIBLE);
                llDetails3.setVisibility(View.GONE);
            }
        }

        displayMeasurementDetails(vars, phase == 0);
    }

    public void setData(ChannelBase channel) {
        super.setData(channel);
        channelExtendedDataToViews(true);
    }

    @Override
    public View inflateContentView() {
        return inflateLayout(R.layout.detail_em);
    }

    @Override
    public void OnChannelDataChanged() {
        channelExtendedDataToViews(false);
    }

    private void setBtnBackground(Button btn, int i) {

        Drawable d = getResources().getDrawable(i);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
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
            phase = (Integer) v.getTag();
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

        mBalanceAvailable = false;
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

        if (demm != null) {
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

