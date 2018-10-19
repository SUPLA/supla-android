package org.supla.android;

import android.content.Context;
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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;

import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.lib.SuplaChannelElectricityMeterValue;
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
    private TextView tvTotalReverseActiveEnergy;
    private TextView tvTotalForwardReactiveEnergy;
    private TextView tvTotalReverseReactiveEnergy;

    private TextView tvFreq;
    private TextView tvVoltage;
    private TextView tvCurrent;
    private TextView tvPowerActive;
    private TextView tvPowerReactive;
    private TextView tvPowerApparent;
    private TextView tvPowerFactor;
    private TextView tvPhaseAngle;
    private TextView tvPhaseForwardActiveEnergy;
    private TextView tvPhaseReverseActiveEnergy;
    private TextView tvPhaseForwardReactiveEnergy;
    private TextView tvPhaseReverseReactiveEnergy;

    private TextView tvChannelTitle;

    private Button btnPhase1;
    private Button btnPhase2;
    private Button btnPhase3;

    final Handler mHandler = new Handler();
    ChartHelper chartHelper;
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

        tvTotalForwardActiveEnergy = (TextView)findViewById(R.id.emtv_TotalForwardActiveEnergy);
        tvTotalReverseActiveEnergy = (TextView)findViewById(R.id.emtv_TotalReverseActiveEnergy);
        tvTotalForwardReactiveEnergy = (TextView)findViewById(R.id.emtv_TotalForwardRectiveEnergy);
        tvTotalReverseReactiveEnergy = (TextView)findViewById(R.id.emtv_TotalReverseRectiveEnergy);

        tvFreq = (TextView)findViewById(R.id.emtv_Freq);
        tvVoltage = (TextView)findViewById(R.id.emtv_Voltage);
        tvCurrent = (TextView)findViewById(R.id.emtv_Current);
        tvPowerActive = (TextView)findViewById(R.id.emtv_PowerActive);
        tvPowerReactive = (TextView)findViewById(R.id.emtv_PowerReactive);
        tvPowerApparent = (TextView)findViewById(R.id.emtv_PowerApparent);
        tvPowerFactor = (TextView)findViewById(R.id.emtv_PowerFactor);
        tvPhaseAngle = (TextView)findViewById(R.id.emtv_PhaseAngle);
        tvPhaseForwardActiveEnergy = (TextView)findViewById(R.id.emtv_PhaseForwardActiveEnergy);
        tvPhaseReverseActiveEnergy = (TextView)findViewById(R.id.emtv_PhaseReverseActiveEnergy);
        tvPhaseForwardReactiveEnergy = (TextView)findViewById(R.id.emtv_PhaseForwardRectiveEnergy);
        tvPhaseReverseReactiveEnergy = (TextView)findViewById(R.id.emtv_PhaseReverseRectiveEnergy);

        tvChannelTitle = (TextView)findViewById(R.id.emtv_ChannelTitle);

        btnPhase1 = (Button) findViewById(R.id.embtn_Phase1);
        btnPhase2 = (Button) findViewById(R.id.embtn_Phase2);
        btnPhase3 = (Button) findViewById(R.id.embtn_Phase3);

        btnPhase1.setOnClickListener(this);
        btnPhase2.setOnClickListener(this);
        btnPhase3.setOnClickListener(this);

        btnPhase1.setTag(1);
        btnPhase2.setTag(2);
        btnPhase3.setTag(3);

        llDetails = (LinearLayout) findViewById(R.id.emllDetails);
        rlButtons1 = (RelativeLayout) findViewById(R.id.emrlButtons1);
        rlButtons2 = (RelativeLayout) findViewById(R.id.emrlButtons2);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Minuty", "Godziny", "Dni", "Miesiące", "Lata",
                        "Ranking godzin", "Ranking dni", "Ranking miesięcy",
                        "Zużycie wg. faz"});

        emSpinner = (Spinner) findViewById(R.id.emSpinner);
        emSpinner.setAdapter(adapter);
        emSpinner.setOnItemSelectedListener(this);

        chartHelper = new ChartHelper(getContext());
        chartHelper.setBarChart((BarChart) findViewById(R.id.emBarChart));
        chartHelper.setPieChart((PieChart) findViewById(R.id.emPieChart));

        ivGraph = (ImageView) findViewById(R.id.emGraphImg);
        ivGraph.bringToFront();
        ivGraph.setOnClickListener(this);

        emProgress = (ProgressBar) findViewById(R.id.emProgressBar);

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

    public void channelExtendedDataToViews() {

        Channel channel = (Channel) getChannelFromDatabase();

        tvChannelTitle.setText(channel.getNotEmptyCaption(getContext()));

        ChannelExtendedValue cev = DBH.getChannelExtendedValue(getRemoteId());

        setBtnBackground(btnPhase1, R.drawable.em_phase_btn_black);
        setBtnBackground(btnPhase2, R.drawable.em_phase_btn_black);
        setBtnBackground(btnPhase3, R.drawable.em_phase_btn_black);

        String empty = "----";
        tvTotalForwardActiveEnergy.setText(empty);
        tvTotalReverseActiveEnergy.setText(empty);
        tvTotalForwardReactiveEnergy.setText(empty);
        tvTotalReverseReactiveEnergy.setText(empty);

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

        if (cev != null
                && cev.getExtendedValue() != null
                && cev.getExtendedValue().ElectricityMeterValue != null) {

            SuplaChannelElectricityMeterValue em = cev.getExtendedValue().ElectricityMeterValue;

            SuplaChannelElectricityMeterValue.Summary sum = em.getSummary();

            tvTotalForwardActiveEnergy.setText(format("%.5f kWh", sum.getTotalForwardActiveEnergy()));
            tvTotalReverseActiveEnergy.setText(format("%.5f kWh", sum.getTotalReverseActiveEnergy()));
            tvTotalForwardReactiveEnergy.setText(format("%.5f kvar", sum.getTotalForwardReactiveEnergy()));
            tvTotalReverseReactiveEnergy.setText(format("%.5f kvar", sum.getTotalReverseReactiveEnergy()));

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
                tvPhaseForwardReactiveEnergy.setText(format("%.5f kvar", sum.getTotalForwardReactiveEnergy()));
                tvPhaseReverseReactiveEnergy.setText(format("%.5f kvar", sum.getTotalReverseReactiveEnergy()));
            }
        }
    }

    public void setData(ChannelBase channel) {
        super.setData(channel);
        channelExtendedDataToViews();
    }

    @Override
    public View getContentView() {
        return inflateLayout(R.layout.detail_em);
    }

    @Override
    public void OnChannelDataChanged() {
        channelExtendedDataToViews();
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
            channelExtendedDataToViews();
        }
    }

    private void runDownloadTask() {
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
    }

    @Override
    public void onRestApiTaskFinished(SuplaRestApiClientTask task) {
        Trace.d("EM", "DOWNLOAD FINISHED");
        emProgress.setVisibility(INVISIBLE);
        chartHelper.loadElectricityMeasurements(getRemoteId());
        demm = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (ivGraph == null || ivGraph.getTag() == null) {
            return;
        }

        ChartHelper.ChartType ctype = ChartHelper.ChartType.Bar_Minutely;

       /*
                       new String[] {"Minuty", "Godziny", "Dni", "Miesiące", "Lata",
                        "Ranking godzin", "Ranking dni", "Ranking miesięcy",
                        "Zużycie wg. faz"});
       * */

        switch (position) {
            case 1:
                ctype = ChartHelper.ChartType.Bar_Hourly;
                break;
            case 2:
                ctype = ChartHelper.ChartType.Bar_Daily;
                break;
            case 3:
                ctype = ChartHelper.ChartType.Bar_Monthly;
                break;
            case 4:
                ctype = ChartHelper.ChartType.Bar_Yearly;
                break;
            case 5:
                ctype = ChartHelper.ChartType.Pie_HourRank;
                break;
            case 6:
                ctype = ChartHelper.ChartType.Pie_DayRank;
                break;
            case 7:
                ctype = ChartHelper.ChartType.Pie_MonthRank;
                break;
            case 8:
                ctype = ChartHelper.ChartType.Pie_PhaseRank;
                break;
        }

        chartHelper.loadElectricityMeasurements(getRemoteId(), ctype);
        chartHelper.setVisibility(VISIBLE);
        chartHelper.animate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

