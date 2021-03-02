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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.supla.android.R;
import org.supla.android.SuplaApp;

public class SectionLayout extends LinearLayout implements View.OnLongClickListener, View.OnClickListener {

    private int locationId;
    private TextView Caption;
    private FrameLayout frmCollapsed;
    private ChannelListView parentListView;

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
            Caption.setOnLongClickListener(this);
            Caption.setOnClickListener(this);

            frmCollapsed = lv.findViewById(R.id.frmSectionCollapsed);
            frmCollapsed.setOnClickListener(this);

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
    public boolean onLongClick(View v) {
        if (parentListView.getOnCaptionLongClickListener() != null) {
            parentListView.getOnCaptionLongClickListener().
                    onLocationCaptionLongClick(parentListView, locationId);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (parentListView != null && parentListView.getOnSectionLayoutTouchListener() != null)
            parentListView
                    .getOnSectionLayoutTouchListener()
                    .onSectionClick(parentListView, Caption.getText().toString(), locationId);
    }
}


