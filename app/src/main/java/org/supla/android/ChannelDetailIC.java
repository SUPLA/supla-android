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
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;

import org.supla.android.charts.ChartHelper;
import org.supla.android.charts.ImpulseCounterChartHelper;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.db.DbHelper;
import org.supla.android.images.ImageCache;
import org.supla.android.lib.SuplaChannelImpulseCounterValue;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;
import org.supla.android.restapi.DownloadImpulseCounterMeasurements;
import org.supla.android.restapi.SuplaRestApiClientTask;

public class ChannelDetailIC extends DetailLayout implements SuplaRestApiClientTask.IAsyncResults,
        AdapterView.OnItemSelectedListener, View.OnClickListener {

    private ImpulseCounterChartHelper chartHelper;
    private DownloadImpulseCounterMeasurements dtm;
    private ProgressBar icProgress;
    private TextView tvChannelTitle;
    private TextView tvMeterValue;
    private TextView tvCurrentConsumption;
    private TextView tvCurrentCost;
    private TextView tvTotalCost;
    private ImageView icImgIcon;
    private Spinner icSpinner;
    private ImageView ivGraph;

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
        tvChannelTitle = findViewById(R.id.ictv_ChannelTitle);
        tvMeterValue = findViewById(R.id.ictv_MeterValue);
        tvTotalCost = findViewById(R.id.ictv_TotalCost);
        tvCurrentConsumption = findViewById(R.id.ictv_CurrentConsumption);
        tvCurrentCost = findViewById(R.id.ictv_CurrentCost);
        icImgIcon = findViewById(R.id.icimgIcon);

        Resources r = getResources();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{
                        r.getString(R.string.minutes),
                        r.getString(R.string.hours),
                        r.getString(R.string.days),
                        r.getString(R.string.months),
                        r.getString(R.string.years),
                        r.getString(R.string.ranking_of_hours),
                        r.getString(R.string.ranking_of_days),
                        r.getString(R.string.ranking_of_months)});

        icSpinner = findViewById(R.id.icSpinner);
        icSpinner.setAdapter(adapter);
        icSpinner.setOnItemSelectedListener(this);

        ivGraph = findViewById(R.id.icGraphImg);
        ivGraph.bringToFront();
        ivGraph.setOnClickListener(this);

        chartHelper = new ImpulseCounterChartHelper(getContext());
        chartHelper.setBarChart((BarChart) findViewById(R.id.icBarChart));
        chartHelper.setPieChart((PieChart) findViewById(R.id.icPieChart));
    }

    @Override
    public View getContentView() {
        return inflateLayout(R.layout.detail_ic);
    }

    private void channelExtendedDataToViews(boolean setIcon) {
        Channel channel = (Channel) getChannelFromDatabase();
        tvChannelTitle.setText(channel.getNotEmptyCaption(getContext()));

        if (setIcon) {
            icImgIcon.setBackgroundColor(Color.TRANSPARENT);
            icImgIcon.setImageBitmap(ImageCache.getBitmap(getContext(), channel.getImageIdx()));
        }

        DbHelper mDBH = new DbHelper(getContext(), true);

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

            double v0 = mDBH.getLastImpulseCounterMeasurementValue(0,
                    channel.getChannelId());
            double v1 = mDBH.getLastImpulseCounterMeasurementValue(-1,
                    channel.getChannelId());
            tvCurrentCost.setText(String.format("%.2f "+ic.getCurrency(),
                    (v0-v1) * ic.getPricePerUnit()));

            tvMeterValue.setText(String.format("%.2f "+channel.getUnit(), ic.getCalculatedValue()));
            tvCurrentConsumption.setText(String.format("%.2f "+channel.getUnit(), v0-v1));
            tvTotalCost.setText(String.format("%.2f "+ic.getCurrency(), ic.getTotalCost()));
            chartHelper.setPricePerUnit(ic.getPricePerUnit());
            chartHelper.setCurrency(ic.getCurrency());

        }
    }

    @Override
    public void OnChannelDataChanged() {
        channelExtendedDataToViews(false);
    }

    @Override
    public void setData(ChannelBase cbase) {
        super.setData(cbase);
        channelExtendedDataToViews(true);
    }

    @Override
    public void onDetailShow() {
        super.onDetailShow();
        icProgress.setVisibility(INVISIBLE);
        onClick(ivGraph);
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
    }

    @Override
    public void onRestApiTaskFinished(SuplaRestApiClientTask task) {
        icProgress.setVisibility(INVISIBLE);
        chartHelper.load(getRemoteId());
        channelExtendedDataToViews(false);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        chartHelper.load(getRemoteId(), position);
        chartHelper.setVisibility(VISIBLE);
        chartHelper.animate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        if (v == ivGraph && icProgress.getVisibility() == INVISIBLE) {
            runDownloadTask();
        }
    }
}
