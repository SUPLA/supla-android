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
import android.content.res.Resources;
import android.database.Cursor;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.IMarker;
import org.supla.android.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class IncrementalMeterChartHelper extends ChartHelper {

    protected double pricePerUnit;
    protected String currency;
    protected ArrayList<String> values = new ArrayList<>();

    public IncrementalMeterChartHelper(Context context) {
        super(context);
    }

    @Override
    protected IMarker getMarker() {
        return new IncrementalMeterMarkerView(this, context, R.layout.chart_marker);
    }

    @Override
    protected void addFormattedValue(Cursor cursor, SimpleDateFormat spf) {
        values.add(spf.format(new java.util.Date(getTimestamp(cursor) * 1000)));
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        value -= 1;


        if (value >= 0 && value < values.size()) {
            return values.get((int) value);
        }

        return "";
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public void load(int channelId, ChartType ctype) {
        values.clear();
        super.load(channelId, ctype);
    }

    protected List<Integer> getBarChartComparsionColors(boolean inverted) {
        Resources r = context.getResources();

        List<Integer> Colors = new ArrayList<Integer>(1);
        if (inverted) {
            Colors.add(r.getColor(R.color.chart_color_value_negative));
            Colors.add(r.getColor(R.color.chart_color_value_positive));
        } else {
            Colors.add(r.getColor(R.color.chart_color_value_positive));
            Colors.add(r.getColor(R.color.chart_color_value_negative));
        }

        return Colors;
    }

    protected void prepareBarDataSet(SuplaBarDataSet barDataSet) {
        if (isComparsionChartType(ctype)) {
            barDataSet.setColorDependsOnTheValue(true);
            barDataSet.setColors(getBarChartComparsionColors(false));
        }
    }

}
