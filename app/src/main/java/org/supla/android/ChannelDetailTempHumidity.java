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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.supla.android.charts.ChartHelper;
import org.supla.android.charts.TempHumidityChartHelper;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.images.ImageCache;
import org.supla.android.listview.ChannelListView;
import org.supla.android.restapi.DownloadMeasurementLogs;
import org.supla.android.restapi.DownloadTempHumidityMeasurements;

public class ChannelDetailTempHumidity extends ChannelDetailTemperature {
    private ImageView ivHumidityIcon;
    private TextView tvHumidity;
    private CheckBox cbHumidity;
    private CheckBox cbTemperature;

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

    protected DownloadMeasurementLogs getDMLInstance() {
        return new DownloadTempHumidityMeasurements(getContext());
    }

    @Override
    public View inflateContentView() {
        return inflateLayout(R.layout.detail_temphumidity);
    }

    @Override
    protected ChartHelper getChartHelperInstance() {
        return new TempHumidityChartHelper(getContext());
    }

    @Override
    protected void init() {
        super.init();
        cbTemperature = findViewById(R.id.thCbTemperature);
        cbTemperature.setOnClickListener(this);
        cbHumidity = findViewById(R.id.thCbHumidity);
        cbHumidity.setOnClickListener(this);
        tvHumidity = findViewById(R.id.thTvHumidity);
        ivHumidityIcon = findViewById(R.id.thHumidityIcon);

        tvHumidity.setTypeface(SuplaApp.getApp().getTypefaceOpenSansRegular());
    }

    @Override
    protected void OnChannelDataChanged(Channel channel) {
        super.OnChannelDataChanged(channel);
        tvHumidity.setText(channel.getHumanReadableValue(ChannelBase.WhichOne.Second));
    }

    @Override
    public void setData(ChannelBase cbase) {
        super.setData(cbase);

        ivHumidityIcon.setBackgroundColor(Color.TRANSPARENT);
        ivHumidityIcon.setImageBitmap(ImageCache.getBitmap(getContext(),
                cbase.getImageIdx(ChannelBase.WhichOne.Second)));
    }

    private TempHumidityChartHelper getTempHumidityChartHelper() {
        return (TempHumidityChartHelper) chartHelper;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v == cbHumidity || v == cbTemperature) {
            if (v == cbHumidity
                    && !cbHumidity.isChecked()
                    && !cbTemperature.isChecked()) {
                cbTemperature.setChecked(true);
            } else if (v == cbTemperature
                    && !cbTemperature.isChecked()
                    && !cbHumidity.isChecked()) {
                cbHumidity.setChecked(true);
            }

            onSpinnerItemSelected();
        }
    }

    @Override
    protected void __onDetailShow() {
        super.__onDetailShow();
        cbTemperature.setChecked(true);
        cbHumidity.setChecked(true);
    }

    @Override
    protected void beforeLoadChart() {
        super.beforeLoadChart();

        getTempHumidityChartHelper().setTemperatureVisible(cbTemperature.isChecked());
        getTempHumidityChartHelper().setHumidityVisible(cbHumidity.isChecked());
    }
}
