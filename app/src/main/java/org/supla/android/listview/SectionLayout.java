package org.supla.android.listview;

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
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.supla.android.R;
import org.supla.android.SuplaApp;
import org.supla.android.Trace;

import java.util.Timer;
import java.util.TimerTask;

public class SectionLayout extends LinearLayout  {

    private int locationId;
    private TextView Caption;
    private FrameLayout frmCollapsed;
    private ChannelListView parentListView;
    private Timer timer;
    private boolean longClick;

    public SectionLayout(Context context) {
        super(context);
        Init(context);
    }

    public SectionLayout(Context context, ChannelListView parentListView) {
        super(context);
        Init(context);
        this.parentListView = parentListView;
    }

    public SectionLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init(context);
    }

    public SectionLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Init(context);
    }

    private void Init(Context context) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View lv = inflater.inflate(R.layout.listview_section, null);

            Caption = lv.findViewById(R.id.tvSectionCaption);
            Caption.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());
            frmCollapsed = lv.findViewById(R.id.frmSectionCollapsed);

            // Unfortunately, LongClickListener and ClickListener
            // stop working after moving the list.
            //Caption.setOnLongClickListener(this);
            //Caption.setOnClickListener(this);

            addView(lv);
        }

    }

    public void setCaption(String caption) {
        Caption.setText(caption);
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public void setCollapsed(boolean collapsed) {
        frmCollapsed.setVisibility(collapsed ? VISIBLE : GONE);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            longClick = false;

            if (timer != null) {
                timer.cancel();
            }

            final int x = (int)event.getX();
            final int y = (int)event.getY();

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Handler handler = new Handler(getContext().getMainLooper());
                    final Runnable r = new Runnable() {
                        public void run() {
                            longClick = true;

                            if (timer != null) {
                                timer.cancel();
                                timer = null;
                            }

                            if (parentListView != null
                                    && parentListView.getOnCaptionLongClickListener() != null) {

                                Rect bounds = new Rect();
                                Caption.getDrawingRect(bounds);

                                if (bounds.contains(x, y)) {
                                    parentListView.getOnCaptionLongClickListener().
                                            onLocationCaptionLongClick(parentListView, locationId);
                                } else {
                                    // .... OnSectionLongClick
                                }
                            }


                        }
                    };
                    handler.post(r);
                }
            }, 800, 1000);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {

            if (!longClick) {
                if (parentListView != null
                        && parentListView.getOnSectionLayoutTouchListener() != null) {
                    parentListView
                            .getOnSectionLayoutTouchListener()
                            .onSectionClick(parentListView, Caption.getText().toString(), locationId);
                }
            }

            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }

        return true;
    }
}


