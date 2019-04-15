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
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.supla.android.R;

public class SectionLayout extends LinearLayout {

    public interface OnSectionLayoutTouchListener {
        void onSectionLayoutTouch(Object sender, String caption, int locationId);
    }

    private int locationId;
    private TextView Caption;
    private FrameLayout frmCollapsed;
    private OnSectionLayoutTouchListener onSectionLayoutTouchListener;

    public SectionLayout(Context context) {
        super(context);
        Init(context);
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
            Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/Quicksand-Regular.ttf");
            Caption.setTypeface(type);

            frmCollapsed = lv.findViewById(R.id.frmSectionCollapsed);

            addView(lv);
        }

    }

    public void setCaption(String caption) {
        Caption.setText(caption);
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public void setOnSectionLayoutTouchListener(OnSectionLayoutTouchListener listener) {
        onSectionLayoutTouchListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_UP) {
            onSectionLayoutTouchListener.onSectionLayoutTouch(this, Caption.getText().toString(), locationId);
        }

        //return super.onTouchEvent(event);
        return true;
    }

    public void setCollapsed(boolean collapsed) {
        frmCollapsed.setVisibility(collapsed ? VISIBLE : INVISIBLE);
    }
}


