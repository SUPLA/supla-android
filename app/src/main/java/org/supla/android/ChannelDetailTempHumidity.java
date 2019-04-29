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
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;

import org.supla.android.charts.ChartHelper;
import org.supla.android.charts.TempHumidityChartHelper;
import org.supla.android.db.ChannelBase;
import org.supla.android.images.ImageCache;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;
import org.supla.android.restapi.DownloadMeasurementLogs;
import org.supla.android.restapi.DownloadTempHumidityMeasurements;
import org.supla.android.restapi.SuplaRestApiClientTask;

public class ChannelDetailTempHumidity extends DetailLayout implements SuplaRestApiClientTask.IAsyncResults, View.OnClickListener {


    private ChartHelper chartHelper;
    private DownloadMeasurementLogs downloadMeasurementLogs;
    private ProgressBar thProgress;
    private TextView thChannelTitle;
    private ImageView thThermometerIcon;
    private ImageView thHumidityIcon;
    private Spinner thSpinner;
    private ImageView thGraph;

    public ChannelDetailTempHumidity(Context context, ChannelListView cLV) {
        super(context, cLV);
    }

    public ChannelDetailTempHumidity(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelDetailTempHumidity(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChannelDetailTempHumidity(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public View getContentView() {
        return inflateLayout(R.layout.detail_temphumidity);
    }

    protected DownloadMeasurementLogs getDMLInstance() {
        return new DownloadTempHumidityMeasurements(getContext());
    }

    protected ChartHelper getChartHelperInstance() {
        return new TempHumidityChartHelper(getContext());
    }

    @Override
    protected void init() {
        super.init();
        thProgress = findViewById(R.id.thProgressBar);
        thChannelTitle = findViewById(R.id.thtv_ChannelTitle);
        thSpinner = findViewById(R.id.thSpinner);
        thThermometerIcon = findViewById(R.id.thThermometerIcon);
        thHumidityIcon = findViewById(R.id.thHumidityIcon);
        thGraph = findViewById(R.id.thGraphImg);
        thGraph.setOnClickListener(this);
        chartHelper = getChartHelperInstance();
        chartHelper.setCombinedChart((CombinedChart)findViewById(R.id.thCombinedChart));
    }

    private void runDownloadTask() {
        if (downloadMeasurementLogs != null && !downloadMeasurementLogs.isAlive(90)) {
            downloadMeasurementLogs.cancel(true);
            downloadMeasurementLogs = null;
        }

        if (downloadMeasurementLogs == null) {
            downloadMeasurementLogs = getDMLInstance();
            downloadMeasurementLogs.setChannelId(getRemoteId());
            downloadMeasurementLogs.setDelegate(this);
            downloadMeasurementLogs.execute();
        }
    }

    @Override
    public void OnChannelDataChanged() {

    }

    @Override
    public void setData(ChannelBase cbase) {
        super.setData(cbase);

        thThermometerIcon.setBackgroundColor(Color.TRANSPARENT);
        thThermometerIcon.setImageBitmap(ImageCache.getBitmap(getContext(), cbase.getImageIdx()));

        thHumidityIcon.setBackgroundColor(Color.TRANSPARENT);
        thHumidityIcon.setImageBitmap(ImageCache.getBitmap(getContext(),
                cbase.getImageIdx(ChannelBase.WhichOne.Second)));

        OnChannelDataChanged();
    }

    @Override
    public void onRestApiTaskStarted(SuplaRestApiClientTask task) {
        thProgress.setVisibility(VISIBLE);
    }

    @Override
    public void onRestApiTaskFinished(SuplaRestApiClientTask task) {
        thProgress.setVisibility(INVISIBLE);
        chartHelper.load(getRemoteId());
    }

    private TempHumidityChartHelper getTempHumidityCHartHelper() {
        return (TempHumidityChartHelper)chartHelper;
    }

    @Override
    public void onDetailShow() {
        super.onDetailShow();
        thProgress.setVisibility(INVISIBLE);
        onClick(thGraph);


        getTempHumidityCHartHelper().setTemperatureVisible(true);
        getTempHumidityCHartHelper().setHumidityVisible(true);

        chartHelper.load(getRemoteId());
        chartHelper.setVisibility(VISIBLE);
        chartHelper.moveToEnd();
        chartHelper.animate();
    }

    @Override
    public void onClick(View v) {
        if (v == thGraph && thProgress.getVisibility() == INVISIBLE) {
            runDownloadTask();
        }
    }
}
