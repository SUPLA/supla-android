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

public class IncrementalMeterMarkerView extends MarkerView {

    private IncrementalMeterChartHelper helper;
    private TextView tvTime;
    private TextView tvValue;
    private TextView tvPrice;

    public IncrementalMeterMarkerView(IncrementalMeterChartHelper helper, Context context,
                                      int layoutResource) {
        super(context, layoutResource);
        this.helper = helper;
        tvTime = findViewById(R.id.tvmTime);
        tvPrice = findViewById(R.id.tvmPrice);
        tvValue = findViewById(R.id.tvmValue);

    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        tvTime.setText(helper.getFormattedValue(e.getX(), null));
        tvTime.setVisibility(tvTime.getText().length() > 0 ? VISIBLE : GONE);

        tvValue.setText(String.format("%.2f "+helper.getUnit(), e.getY()));
        tvValue.setVisibility(tvValue.getText().length() > 0 ? VISIBLE : GONE);

        tvPrice.setText(String.format("%.2f "+helper.getCurrency(),
                helper.getPricePerUnit()*e.getY()));
        tvPrice.setVisibility(tvPrice.getText().length() > 0 ? VISIBLE : GONE);

        super.refreshContent(e, highlight);
    }
}
