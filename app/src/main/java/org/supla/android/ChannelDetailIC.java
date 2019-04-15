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
import android.widget.TextView;

import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.images.ImageCache;
import org.supla.android.lib.SuplaChannelImpulseCounterValue;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;
import org.supla.android.restapi.DownloadImpulseCounterMeasurements;
import org.supla.android.restapi.SuplaRestApiClientTask;

public class ChannelDetailIC extends DetailLayout implements SuplaRestApiClientTask.IAsyncResults {

    private DownloadImpulseCounterMeasurements dtm;
    private ProgressBar icProgress;
    private TextView tvChannelTitle;
    private TextView tvMeterValue;
    private TextView tvCost;
    private TextView tvImpulsesCount;
    private ImageView icImgIcon;

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
        tvCost = findViewById(R.id.ictv_Cost);
        tvImpulsesCount = findViewById(R.id.ictv_ImpulsesCount);
        icImgIcon = findViewById(R.id.icimgIcon);
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

        tvMeterValue.setText("---");
        tvCost.setText("---");
        tvImpulsesCount.setText("---");

        ChannelExtendedValue cev = channel.getExtendedValue();
        if (cev != null
                && cev.getExtendedValue() != null
                && cev.getExtendedValue().ImpulseCounterValue != null) {

            SuplaChannelImpulseCounterValue ic = cev.getExtendedValue().ImpulseCounterValue;

            tvMeterValue.setText(String.format("%.2f "+channel.getUnit(), ic.getCalculatedValue()));
            tvCost.setText(String.format("%.2f "+ic.getCurrency(), ic.getTotalCost()));
            tvImpulsesCount.setText(Long.toString(ic.getCounter()));
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
        runDownloadTask();
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
    }
}
