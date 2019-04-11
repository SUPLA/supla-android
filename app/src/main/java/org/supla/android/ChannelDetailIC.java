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
import android.util.AttributeSet;
import android.view.View;

import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;
import org.supla.android.restapi.DownloadImpulseCounterMeasurements;
import org.supla.android.restapi.SuplaRestApiClientTask;

public class ChannelDetailIC extends DetailLayout implements SuplaRestApiClientTask.IAsyncResults {

    private DownloadImpulseCounterMeasurements dtm;

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
    public View getContentView() {
        return inflateLayout(R.layout.detail_ic);
    }

    @Override
    public void OnChannelDataChanged() {

    }

    @Override
    public void onDetailShow() {
        super.onDetailShow();

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

    }

    @Override
    public void onRestApiTaskFinished(SuplaRestApiClientTask task) {

    }
}
