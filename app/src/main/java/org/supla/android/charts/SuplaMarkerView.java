package org.supla.android.charts;

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
import android.widget.TextView;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import org.supla.android.R;

public class SuplaMarkerView extends MarkerView {

    protected ChartHelper helper;
    protected TextView tvTime;
    protected TextView tvValue2;
    protected TextView tvValue1;

    public SuplaMarkerView(ChartHelper helper, Context context,
                           int layoutResource) {
        super(context, layoutResource);
        this.helper = helper;
        tvTime = findViewById(R.id.tvmTime);
        tvValue1 = findViewById(R.id.tvmValue1);
        tvValue2 = findViewById(R.id.tvmValue2);

    }


    protected String getString(String str) {
        return str == null ? "" : str;
    }

    protected void setText(TextView tv, String text) {
        tv.setText(text);
        tv.setVisibility(tv.getText().length() > 0 ? VISIBLE : GONE);
    }

    protected String getTime(Entry e) {
        return helper.getFormattedValue(e.getX(), null);
    }

    protected String getValue1(Entry e) {
        return String.format("%.2f "+getString(helper.getUnit()), e.getY());
    }

    protected String getValue2(Entry e) {
        return "";
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        setText(tvTime, getTime(e));
        setText(tvValue1, getValue1(e));
        setText(tvValue2, getValue2(e));
        super.refreshContent(e, highlight);
    }
}
