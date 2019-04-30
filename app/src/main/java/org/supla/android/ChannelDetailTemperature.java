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
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import com.github.mikephil.charting.charts.CombinedChart;
import org.supla.android.charts.ChartHelper;
import org.supla.android.charts.TemperatureChartHelper;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.images.ImageCache;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;
import org.supla.android.restapi.DownloadMeasurementLogs;
import org.supla.android.restapi.DownloadTemperatureMeasurements;
import org.supla.android.restapi.SuplaRestApiClientTask;
import java.util.Calendar;
import java.util.Date;

public class ChannelDetailTemperature extends DetailLayout implements
        SuplaRestApiClientTask.IAsyncResults, View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    protected ChartHelper chartHelper;
    private DownloadMeasurementLogs downloadMeasurementLogs;
    private ProgressBar tvProgress;
    private TextView tvChannelTitle;
    private ImageView ivThermometerIcon;
    private Spinner thSpinner;
    private ImageView ivGraph;
    private TextView tvTemperature;

    public ChannelDetailTemperature(Context context, ChannelListView cLV) {
        super(context, cLV);
    }

    public ChannelDetailTemperature(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelDetailTemperature(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChannelDetailTemperature(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected DownloadMeasurementLogs getDMLInstance() {
        return new DownloadTemperatureMeasurements(getContext());
    }

    protected ChartHelper getChartHelperInstance() {
        return new TemperatureChartHelper(getContext());
    }

    @Override
    public View getContentView() {
        return inflateLayout(R.layout.detail_temperature);
    }

    @Override
    protected void init() {
        super.init();
        tvProgress = findViewById(R.id.thProgressBar);
        tvChannelTitle = findViewById(R.id.thtv_ChannelTitle);
        thSpinner = findViewById(R.id.thSpinner);
        ivThermometerIcon = findViewById(R.id.thThermometerIcon);

        ivGraph = findViewById(R.id.thGraphImg);
        ivGraph.setOnClickListener(this);
        chartHelper = getChartHelperInstance();
        chartHelper.setCombinedChart((CombinedChart)findViewById(R.id.thCombinedChart));
        tvTemperature = findViewById(R.id.thTvTemperature);

        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/OpenSans-Regular.ttf");
        tvTemperature.setTypeface(tf);

        Resources r = getResources();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{
                        r.getString(R.string.last24hours),
                        r.getString(R.string.last7days),
                        r.getString(R.string.last30days),
                        r.getString(R.string.last90days),
                        r.getString(R.string.all_available_history)});

        thSpinner = findViewById(R.id.thSpinner);
        thSpinner.setAdapter(adapter);
        thSpinner.setOnItemSelectedListener(this);
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

    protected void OnChannelDataChanged(Channel channel) {
        tvChannelTitle.setText(channel.getNotEmptyCaption(getContext()));
        tvTemperature.setText(channel.getHumanReadableValue());
    }

    @Override
    public void OnChannelDataChanged() {
        Channel channel = (Channel) getChannelFromDatabase();
        OnChannelDataChanged(channel);
    }

    @Override
    public void setData(ChannelBase cbase) {
        super.setData(cbase);
        ivThermometerIcon.setBackgroundColor(Color.TRANSPARENT);
        ivThermometerIcon.setImageBitmap(ImageCache.getBitmap(getContext(), cbase.getImageIdx()));

        OnChannelDataChanged();
    }

    @Override
    public void onRestApiTaskStarted(SuplaRestApiClientTask task) {
        tvProgress.setVisibility(VISIBLE);
    }

    protected void beforeLoadChart() {};

    @Override
    public void onRestApiTaskFinished(SuplaRestApiClientTask task) {
        tvProgress.setVisibility(INVISIBLE);
        beforeLoadChart();
        chartHelper.load(getRemoteId());
    }

    private TemperatureChartHelper getTemperatureChartHelper() {
        return (TemperatureChartHelper)chartHelper;
    }

    protected void __onDetailShow() { }

    @Override
    public void onDetailShow() {
        super.onDetailShow();
        __onDetailShow();
        tvProgress.setVisibility(INVISIBLE);
        onClick(ivGraph);
        onSpinnerItemSelected();
    }

    protected void onSpinnerItemSelected() {
        onItemSelected(null, null,
                thSpinner.getSelectedItemPosition(),
                thSpinner.getSelectedItemId());
    }

    @Override
    public void onClick(View v) {
        if (v == ivGraph && tvProgress.getVisibility() == INVISIBLE) {
            runDownloadTask();
        };
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Date dateFrom = null;
        Date dateTo = null;

        int calendar_field = Calendar.DATE;
        int calendar_amount = 0;

        switch(position) {
            case 0:
                calendar_amount = -24;
                calendar_field = Calendar.HOUR;
                break;
            case 1:
                calendar_amount = -7;
                break;
            case 2:
                calendar_amount = -30;
                break;
            case 3:
                calendar_amount = -90;
                break;
        }

        if (calendar_amount != 0) {
            Calendar now = Calendar.getInstance();
            now.add(calendar_field, calendar_amount);
            dateFrom = now.getTime();
        }

        if (dateFrom!=null) {
            dateTo = new Date();
        }

        getTemperatureChartHelper().setDateRange(dateFrom, dateTo);

        beforeLoadChart();
        chartHelper.load(getRemoteId());
        chartHelper.setVisibility(VISIBLE);
        chartHelper.moveToEnd();
        chartHelper.animate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
