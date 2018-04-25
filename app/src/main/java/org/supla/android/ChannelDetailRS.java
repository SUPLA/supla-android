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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelGroup;
import org.supla.android.lib.SuplaClient;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ChannelDetailRS extends DetailLayout implements SuplaRollerShutter.OnTouchListener, View.OnTouchListener, View.OnLayoutChangeListener {

    private SuplaRollerShutter rs;
    private TextView tvTitle;
    private TextView tvPercentCaption;
    private TextView tvPercent;
    private Button btnUp;
    private Button btnDown;
    private Button btnStop;
    private Button btnOpen;
    private Button btnClose;
    private Timer delayTimer1;
    private boolean withoutDelay;

    public ChannelDetailRS(Context context, ChannelListView cLV) {
        super(context, cLV);
    }

    public ChannelDetailRS(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelDetailRS(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChannelDetailRS(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void init() {

        super.init();
        rs = (SuplaRollerShutter) findViewById(R.id.rs1);
        rs.setMarkerColor(getResources().getColor(R.color.detail_rs_marker));
        rs.setOnPercentTouchListener(this);

        btnUp = (Button) findViewById(R.id.rsBtnUp);
        btnDown = (Button) findViewById(R.id.rsBtnDown);
        btnStop = (Button) findViewById(R.id.rsBtnStop);
        btnOpen = (Button) findViewById(R.id.rsBtnOpen);
        btnClose = (Button) findViewById(R.id.rsBtnClose);

        btnUp.setOnTouchListener(this);
        btnDown.setOnTouchListener(this);
        btnStop.setOnTouchListener(this);
        btnOpen.setOnTouchListener(this);
        btnClose.setOnTouchListener(this);

        Typeface type = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Bold.ttf");

        tvPercentCaption = (TextView) findViewById(R.id.rsDetailPercentCaption);
        tvPercentCaption.setTypeface(type);

        tvPercent = (TextView) findViewById(R.id.rsDetailPercent);
        tvPercent.setTypeface(type);

        type = Typeface.createFromAsset(getContext().getAssets(), "fonts/Quicksand-Regular.ttf");

        tvTitle = (TextView) findViewById(R.id.rsDetailTitle);
        tvTitle.setTypeface(type);

        addOnLayoutChangeListener(this);
        delayTimer1 = null;
        withoutDelay = false;
    }

    @Override
    public View getContentView() {
        return inflateLayout(R.layout.detail_rs);
    }

    @Override
    public void OnChannelDataChanged() {

        if (delayTimer1 != null) {
            delayTimer1.cancel();
            delayTimer1 = null;
        }

        if (!isGroup()) {
            Channel channel = (Channel) getChannelFromDatabase();

            byte p = channel.getRollerShutterPosition();

            tvTitle.setText(channel.getNotEmptyCaption(getContext()));

            rs.setMarkers(null);
            rs.setPercent(p);

            if (p < 0) {
                tvPercent.setText(R.string.calibration);
            } else {
                tvPercent.setText(Integer.toString((int) p) + "%");
            }
        } else {
            ChannelGroup cgroup = (ChannelGroup) getChannelFromDatabase();
            tvTitle.setText(cgroup.getNotEmptyCaption(getContext()));
            rs.setPercent(0);

            ArrayList<Integer> positions = cgroup.getRollerShutterPositions();

            int percent = -1;

            for (int a = 0; a < positions.size(); a++) {
                int p = positions.get(a).intValue();
                if (p < 0) {
                    positions.remove(a);
                    a--;
                } else if (percent == -1) {
                    percent = p;
                } else if (percent != -2 && percent != p) {
                    percent = -2;
                }
            }

            if (percent >= 0) {

                rs.setMarkers(positions);

                delayTimer1 = new Timer();
                delayTimer1.schedule(new DelayTask(percent) {
                    @Override
                    public void run() {
                        if (getContext() instanceof Activity) {
                            ((Activity) getContext()).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    rs.setMarkers(null);
                                    rs.setPercent(percent);
                                    tvPercent.setText(Integer.toString(percent) + "%");
                                }
                            });
                        }
                    }

                }, withoutDelay ? 0 : 2000);

            } else if (percent == -1) {
                // All of RS wait for calibration
                rs.setPercent(0);
                rs.setMarkers(null);
                tvPercent.setText(R.string.calibration);
            } else {
                rs.setPercent(0);
                rs.setMarkers(positions);
                tvPercent.setText("---");
            }

            withoutDelay = false;
        }

    }

    public void setData(ChannelBase channel) {

        if (channel != null && channel.getRemoteId() != getRemoteId()) {
            withoutDelay = true;
        }

        super.setData(channel);
        OnChannelDataChanged();

    }

    @Override
    public void onPercentChanged(SuplaRollerShutter rs, float percent) {

        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if (client == null || !isDetailVisible())
            return;

        Trace.d("XYZ", "XYZ");
        client.Open(getRemoteId(), isGroup(), (int) (10 + percent));
        OnChannelDataChanged();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPercentChangeing(SuplaRollerShutter rs, float percent) {
        tvPercent.setText(Integer.toString((int) percent) + "%");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;

            default:
                return false;
        }


        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if (client == null)
            return false;

        if (v == btnUp) {

            client.Open(getRemoteId(), isGroup(), action == MotionEvent.ACTION_DOWN ? 2 : 0);

        } else if (v == btnDown) {

            client.Open(getRemoteId(), isGroup(), action == MotionEvent.ACTION_DOWN ? 1 : 0);

        } else if (v == btnStop) {

            if (action == MotionEvent.ACTION_DOWN)
                client.Open(getRemoteId(), isGroup(), 0);
            else return false;

        } else if (v == btnOpen) {

            if (action == MotionEvent.ACTION_DOWN)
                client.Open(getRemoteId(), isGroup(), 10);
            else return false;

        } else if (v == btnClose) {

            if (action == MotionEvent.ACTION_DOWN)
                client.Open(getRemoteId(), isGroup(), 110);
            else return false;
        }

        SuplaApp.Vibrate(getContext());

        return true;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

        RelativeLayout rlButtons = (RelativeLayout) findViewById(R.id.rlButtons);

        int margin = rlButtons.getMeasuredHeight() - (btnDown.getTop() + btnDown.getHeight());

        if (margin < 0) {
            RelativeLayout rlRS = (RelativeLayout) findViewById(R.id.rlRS);
            rlRS.getLayoutParams().height += margin;
            rlRS.requestLayout();
        }

    }

    private class DelayTask extends TimerTask {
        int percent;

        public DelayTask(int percent) {
            this.percent = percent;
        }

        @Override
        public void run() {
            // You can do anything you want with param
        }
    }
}


