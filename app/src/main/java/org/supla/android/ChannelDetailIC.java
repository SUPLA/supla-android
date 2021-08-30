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
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;

import org.supla.android.charts.ImpulseCounterChartHelper;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.db.DbHelper;
import org.supla.android.db.MeasurementsDbHelper;
import org.supla.android.images.ImageCache;
import org.supla.android.lib.SuplaChannelImpulseCounterValue;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;
import org.supla.android.restapi.DownloadImpulseCounterMeasurements;
import org.supla.android.restapi.SuplaRestApiClientTask;

import java.util.Timer;
import java.util.TimerTask;

public class ChannelDetailIC extends DetailLayout implements SuplaRestApiClientTask.IAsyncResults,
        AdapterView.OnItemSelectedListener, View.OnClickListener {

    final Handler mHandler = new Handler();
    private ImpulseCounterChartHelper chartHelper;
    private DownloadImpulseCounterMeasurements dtm;
    private ProgressBar icProgress;
    private TextView tvMeterValue;
    private TextView tvCurrentConsumption;
    private TextView tvCurrentCost;
    private TextView tvTotalCost;
    private ImageView icImgIcon;
    private SuplaWarningIcon warningIcon;
    private Spinner icSpinnerMaster;
    private Spinner icSpinnerSlave;
    private ImageView ivGraph;
    private Timer timer1;

    public ChannelDetailIC(Context context, ChannelListView cLV) {
        super(context, cLV);
    }

    public ChannelDetailIC(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelDetailIC(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChannelDetailIC(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        super.init();
        icProgress = findViewById(R.id.icProgressBar);
        tvMeterValue = findViewById(R.id.ictv_MeterValue);
        tvTotalCost = findViewById(R.id.ictv_TotalCost);
        tvCurrentConsumption = findViewById(R.id.ictv_CurrentConsumption);
        tvCurrentCost = findViewById(R.id.ictv_CurrentCost);
        icImgIcon = findViewById(R.id.icimgIcon);
        icImgIcon.setClickable(true);
        icImgIcon.setOnClickListener(this);
        warningIcon = findViewById(R.id.icWarningIcon);

        chartHelper = new ImpulseCounterChartHelper(getContext());
        chartHelper.setCombinedChart((CombinedChart) findViewById(R.id.icCombinedChart));
        chartHelper.setPieChart((PieChart) findViewById(R.id.icPieChart));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item, chartHelper.getMasterSpinnerItems(13));

        icSpinnerMaster = findViewById(R.id.icSpinnerMaster);
        icSpinnerMaster.setAdapter(adapter);
        icSpinnerMaster.setOnItemSelectedListener(this);

        icSpinnerSlave = findViewById(R.id.icSpinnerSlave);

        ivGraph = findViewById(R.id.icGraphImg);
        ivGraph.bringToFront();
        ivGraph.setOnClickListener(this);
    }

    private void updateSlaveSpinnerItems() {
        icSpinnerSlave.setOnItemSelectedListener(null);

        String[] items = chartHelper.getSlaveSpinnerItems(icSpinnerMaster);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item, items);
        icSpinnerSlave.setAdapter(adapter);
        icSpinnerSlave.setVisibility(items.length > 0 ? VISIBLE : GONE);

        icSpinnerSlave.setOnItemSelectedListener(this);
    }

    @Override
    public View inflateContentView() {
        return inflateLayout(R.layout.detail_ic);
    }

    private void channelExtendedDataToViews() {
        Channel channel = (Channel) getChannelFromDatabase();

        if (!icImgIcon.getTag().equals(channel.getImageIdx())) {
            icImgIcon.setBackgroundColor(Color.TRANSPARENT);
            icImgIcon.setImageBitmap(ImageCache.getBitmap(getContext(), channel.getImageIdx()));
            icImgIcon.setTag(channel.getImageIdx());
        }

        warningIcon.setChannel(getChannelBase());

        MeasurementsDbHelper mDBH = MeasurementsDbHelper.getInstance(getContext());

        tvMeterValue.setText("---");
        tvCurrentConsumption.setText("---");
        tvTotalCost.setText("---");
        tvCurrentCost.setText("---");
        chartHelper.setUnit(channel.getUnit());

        ChannelExtendedValue cev = channel.getExtendedValue();
        if (cev != null
                && cev.getExtendedValue() != null
                && cev.getExtendedValue().ImpulseCounterValue != null) {

            SuplaChannelImpulseCounterValue ic = cev.getExtendedValue().ImpulseCounterValue;

            double currentConsumption;
            double currentCost;

            if (mDBH.impulseCounterMeasurementsStartsWithTheCurrentMonth(channel.getChannelId())) {
                currentConsumption = ic.getCalculatedValue();
                currentCost = ic.getTotalCost();
            } else {
                double v0 = mDBH.getLastImpulseCounterMeasurementValue(0,
                        channel.getChannelId());
                double v1 = mDBH.getLastImpulseCounterMeasurementValue(-1,
                        channel.getChannelId());
                currentConsumption = v0 - v1;
                currentCost = currentConsumption * ic.getPricePerUnit();
            }

            tvCurrentCost.setText(String.format("%.2f " + ic.getCurrency(),
                    currentCost));

            tvMeterValue.setText(String.format("%.2f " + channel.getUnit(), ic.getCalculatedValue()));
            tvCurrentConsumption.setText(String.format("%.2f " + channel.getUnit(), currentConsumption));
            tvTotalCost.setText(String.format("%.2f " + ic.getCurrency(), ic.getTotalCost()));
            chartHelper.setPricePerUnit(ic.getPricePerUnit());
            chartHelper.setCurrency(ic.getCurrency());

        }
    }

    @Override
    public void OnChannelDataChanged() {
        channelExtendedDataToViews();
    }

    @Override
    public void setData(ChannelBase cbase) {
        super.setData(cbase);
        icImgIcon.setTag(-1);
        channelExtendedDataToViews();
    }

    @Override
    public void onDetailShow() {
        super.onDetailShow();
        icProgress.setVisibility(INVISIBLE);
        onClick(ivGraph);
        onItemSelected(null, null,
                icSpinnerMaster.getSelectedItemPosition(),
                icSpinnerMaster.getSelectedItemId());

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

        if (timer1 != null) {
            timer1.cancel();
            timer1 = null;
        }
    }

    private void runDownloadTask() {
        if (dtm != null && !dtm.isAlive(90)) {
            dtm.cancel(true);
            dtm = null;
        }

        if (dtm == null) {
            dtm = new DownloadImpulseCounterMeasurements(this.getContext());
            dtm.setChannelId(getRemoteId());
            dtm.setDelegate(this);
            dtm.execute();
        }
    }

    @Override
    public void onRestApiTaskStarted(SuplaRestApiClientTask task) {
        icProgress.setVisibility(VISIBLE);
        chartHelper.setDownloadProgress(0d);
    }

    @Override
    public void onRestApiTaskFinished(SuplaRestApiClientTask task) {
        icProgress.setVisibility(INVISIBLE);
        chartHelper.setDownloadProgress(null);
        chartHelper.load(getRemoteId());
        channelExtendedDataToViews();
    }

    @Override
    public void onRestApiTaskProgressUpdate(SuplaRestApiClientTask task, Double progress) {
        chartHelper.setDownloadProgress(progress);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (parent != icSpinnerSlave) {
            updateSlaveSpinnerItems();
        }

        chartHelper.setDateRangeBySpinners(icSpinnerMaster, icSpinnerSlave);
        chartHelper.load(getRemoteId(), icSpinnerMaster.getSelectedItemPosition());
        chartHelper.setVisibility(VISIBLE);
        chartHelper.animate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        if (v == icImgIcon) {
            Channel channel = (Channel) getChannelFromDatabase();
            if (channel != null) {
                if (channel.getFunc() == SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
                        || channel.getFunc() == SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
                        || channel.getFunc() == SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER) {
                    SuplaClient client = SuplaApp.getApp().getSuplaClient();
                    if (client != null) {
                        client.turnOnOff(getContext(), !channel.getValue().hiValue(),
                                channel.getRemoteId(), false, channel.getFunc(),
                                true);
                    }
                }
            }
        } else if (v == ivGraph && icProgress.getVisibility() == INVISIBLE) {
            runDownloadTask();
        }
    }
}
