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
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import org.supla.android.db.DbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class ChartHelper implements IAxisValueFormatter {

    protected Context context;
    protected ChartType ctype = ChartType.Bar_Hourly;
    protected BarChart barChart;
    protected PieChart pieChart;
    protected ArrayList<String> values = new ArrayList<>();
    protected int xValueMax;

    public enum ChartType {
        Bar_Minutely,
        Bar_Hourly,
        Bar_Daily,
        Bar_Monthly,
        Bar_Yearly,
        Pie_HourRank,
        Pie_DayRank,
        Pie_MonthRank,
        Pie_PhaseRank
    }

    public ChartHelper(Context context) {
        this.context = context;
    }


    public BarChart getBarChart() {
        return barChart;
    }

    public void setBarChart(BarChart chart) {
        barChart = chart;
    }

    public PieChart getPieChart() {
        return pieChart;
    }

    public void setPieChart(PieChart chart) {
        pieChart = chart;
    }

    public void setVisibility(int visibility) {
        if (barChart != null) {
            barChart.setVisibility(View.GONE);
        }

        if (pieChart != null) {
            pieChart.setVisibility(View.GONE);
        }

        switch (ctype) {
            case Bar_Minutely:
            case Bar_Hourly:
            case Bar_Daily:
            case Bar_Monthly:
            case Bar_Yearly:
                if (barChart != null) {
                    barChart.setVisibility(visibility);
                }
                break;
            case Pie_HourRank:
            case Pie_DayRank:
            case Pie_MonthRank:
            case Pie_PhaseRank:
                if (pieChart != null) {
                    pieChart.setVisibility(visibility);
                }
                break;
        }
    }

    public void animate() {
        if (barChart != null
                && barChart.getVisibility() == View.VISIBLE) {
            barChart.animateY(1000);
        } else if (pieChart != null && pieChart.getVisibility() == View.VISIBLE) {
            pieChart.spin(500, 0, -360f, Easing.EasingOption.EaseInOutQuad);
        }
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        value -= 1;

        if (value >= 0 && value < values.size()) {
            return values.get((int) value);
        }

        return "";
    }


    abstract protected Cursor getCursor(DbHelper DBH,
                                      SQLiteDatabase db, int channelId, String dateFormat);

    abstract protected float[] getValues(Cursor c);

    abstract protected long getTimestamp(Cursor c);

    abstract protected String[] getStackLabels();

    abstract protected List<Integer> getColors();

    public void loadBarChart(int channelId, ChartType ctype) {

        if (barChart == null) {
            return;
        }

        barChart.getXAxis().setValueFormatter(this);
        barChart.getXAxis().setGranularity(1f);
        barChart.getAxisLeft().setDrawLabels(false);

        Description desc = barChart.getDescription();
        desc.setText("");
        barChart.setDescription(desc);

        SimpleDateFormat spf = new SimpleDateFormat("HH:mm");

        String DateFormat = "%Y-%m-%dT%H:%M:00.000";
        switch (ctype) {
            case Bar_Hourly:
                DateFormat = "%Y-%m-%dT%H:00:00.000";
                spf = new SimpleDateFormat("HH");
                break;
            case Bar_Daily:
                DateFormat = "%Y-%m-%dT00:00:00.000";
                spf = new SimpleDateFormat("yy-MM-dd");
                break;
            case Bar_Monthly:
                DateFormat = "%Y-%m-01T00:00:00.000";
                spf = new SimpleDateFormat("MMM");
                break;
            case Bar_Yearly:
                DateFormat = "%Y-01-01T00:00:00.000";
                spf = new SimpleDateFormat("yyyy");
                break;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        values.clear();

        DbHelper DBH = new DbHelper(context, true);
        SQLiteDatabase db = DBH.getReadableDatabase();
        try {
            Cursor c = getCursor(DBH, db, channelId, DateFormat);

            if (c != null) {
                if (c.moveToFirst()) {
                    int n = 0;
                    do {
                        n++;
                        entries.add(new BarEntry(n, getValues(c)));
                        xValueMax = n;

                        values.add(spf.format(new java.util.Date(getTimestamp(c) * 1000)));

                    } while (c.moveToNext());

                }

                c.close();
            }
        } finally {
            db.close();
        }

        BarDataSet dataset = new BarDataSet(entries, "");
        dataset.setDrawValues(false);

        Resources res = context.getResources();

        dataset.setStackLabels(getStackLabels());
        dataset.setColors(getColors());
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(dataset);

        barChart.setDrawMarkers(true);
        barChart.setData(new BarData(dataSets));
        barChart.invalidate();
    }

    public void moveToEnd(float maxXRange1, float maxXRange2) {
        barChart.setVisibleXRangeMaximum(maxXRange1);
        barChart.moveViewToX(barChart.getXChartMax());
        barChart.setVisibleXRangeMaximum(maxXRange2);
    }
}
