package org.supla.android.listview;

/*
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

 Author: Przemyslaw Zygmunt p.zygmunt@acsoftware.pl [AC SOFTWARE]
 */

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.supla.android.R;

public class SectionLayout extends LinearLayout {

    private TextView Caption;

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

        int height = getResources().getDimensionPixelSize(R.dimen.channel_section_height);
        int sheight = getResources().getDimensionPixelSize(R.dimen.channel_separator_height);

        setBackgroundColor(getResources().getColor(R.color.channel_cell));
        setOrientation(VERTICAL);

        setLayoutParams(new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, height));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height - sheight);

        lp.setMargins(getResources().getDimensionPixelSize(R.dimen.channel_separator_text_margin), 0, 0, 0);

        Caption = new TextView(context);
        Caption.setLayoutParams(lp);

        Typeface type = Typeface.createFromAsset(context.getAssets(),"fonts/Quicksand-Regular.ttf");
        Caption.setTypeface(type);
        Caption.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.channel_section_text));
        Caption.setTextColor(getResources().getColor(R.color.channel_section_text));
        Caption.setGravity(Gravity.CENTER_VERTICAL);

        addView(Caption);
        addView(new LineView(context));
    }

    public void setCaption(String caption) {

        Caption.setText(caption);

    }
}
