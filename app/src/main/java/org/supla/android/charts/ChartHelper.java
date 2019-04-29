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
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.supla.android.db.DbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class ChartHelper implements IAxisValueFormatter {

    protected String unit;
    protected Context context;
    protected ChartType ctype = ChartType.Bar_Hourly;
    protected BarChart barChart;
    protected PieChart pieChart;
    protected ArrayList<String> values = new ArrayList<>();

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


        if (value > 0 && value < values.size()) {
            return values.get((int) value);
        }

        return "";
    }


    abstract protected Cursor getCursor(DbHelper DBH,
                                      SQLiteDatabase db, int channelId, String dateFormat);

    abstract protected void addEntries(int x, Cursor c, ArrayList<BarEntry> entries);

    abstract protected void addPieEntries(ChartType ctype, SimpleDateFormat spf, Cursor c,
                                          ArrayList<PieEntry>entries);

    abstract protected long getTimestamp(Cursor c);

    abstract protected String[] getStackLabels();

    abstract protected List<Integer> getColors();

    protected IMarker getMarker() {
        return null;
    }

    public void loadBarChart(int channelId, ChartType ctype) {

        if (pieChart != null) {
            pieChart.setVisibility(View.GONE);
        }

        if (barChart == null) {
            return;
        }

        barChart.setVisibility(View.VISIBLE);
        barChart.getXAxis().setValueFormatter(this);
        barChart.getXAxis().setLabelCount(3);
        barChart.getAxisLeft().setDrawLabels(false);

        setUnit(getUnit());

        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        String DateFormat = "%Y-%m-%dT%H:%M:00.000";
        switch (ctype) {
            case Bar_Hourly:
                DateFormat = "%Y-%m-%dT%H:00:00.000";
                spf = new SimpleDateFormat("yyyy-MM-dd HH");
                break;
            case Bar_Daily:
                DateFormat = "%Y-%m-%dT00:00:00.000";
                spf = new SimpleDateFormat("yyyy-MM-dd");
                break;
            case Bar_Monthly:
                DateFormat = "%Y-%m-01T00:00:00.000";
                spf = new SimpleDateFormat("yyyy MMM");
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
                        addEntries(n, c, entries);
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

        barChart.setMarker(getMarker());
        barChart.setDrawMarkers(barChart.getMarker()!=null);
        barChart.setData(new BarData(dataSets));
        barChart.invalidate();
    }

    public void loadPieChart(int channelId, ChartType ctype) {

        if (barChart != null) {
            barChart.setVisibility(View.GONE);
        }

        if (pieChart == null) {
            return;
        }

        pieChart.setVisibility(View.VISIBLE);

        SimpleDateFormat spf = new SimpleDateFormat("HH");

        String DateFormat = "2018-01-01T%H:00:00.000";
        switch (ctype) {
            case Pie_DayRank:
                DateFormat = "2018-01-%dT00:00:00.000";
                spf = new SimpleDateFormat("EE");
                break;
            case Pie_MonthRank:
                DateFormat = "%Y-%m-01T00:00:00.000";
                spf = new SimpleDateFormat("MMM");
                break;
            case Pie_PhaseRank:
                DateFormat = "2018-01-01T00:00:00.000";
                break;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();

        DbHelper DBH = new DbHelper(context, true);
        SQLiteDatabase db = DBH.getReadableDatabase();
        try {
            Cursor c = getCursor(DBH, db, channelId, DateFormat);

            if (c != null) {

                if (c.moveToFirst()) {

                    if (ctype.equals(ChartType.Pie_PhaseRank)) {
                        addPieEntries(ctype, spf, c, entries);
                    } else {
                        do {
                            addPieEntries(ctype, spf, c, entries);
                        } while (c.moveToNext());
                    }


                }

                c.close();
            }
        } finally {
            db.close();
        }

        setUnit(getUnit());

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(set);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    public void moveToEnd(float maxXRange1, float maxXRange2) {
        barChart.setVisibleXRangeMaximum(maxXRange1);
        barChart.moveViewToX(barChart.getXChartMax());
        barChart.setVisibleXRangeMaximum(maxXRange2);
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;

        if (barChart!=null) {
            Description desc = barChart.getDescription();
            desc.setText(unit == null ? "" : unit);
            barChart.setDescription(desc);
        }

        if (pieChart!=null) {
            Description desc = pieChart.getDescription();
            desc.setText(unit == null ? "" : unit);
            pieChart.setDescription(desc);
        }
    }

    public void load(int channelId, ChartType ctype) {

        if (!this.ctype.equals(ctype)) {
            this.ctype = ctype;
        }

        switch (ctype) {
            case Bar_Minutely:
            case Bar_Hourly:
            case Bar_Daily:
            case Bar_Monthly:
            case Bar_Yearly:
                loadBarChart(channelId, ctype);
                break;
            case Pie_HourRank:
            case Pie_DayRank:
            case Pie_MonthRank:
            case Pie_PhaseRank:
                loadPieChart(channelId, ctype);
                break;
        }

    }

    public void load(int channelId, int chartTypeIdx) {
        ElectricityChartHelper.ChartType ctype = ElectricityChartHelper.ChartType.Bar_Minutely;
        switch (chartTypeIdx) {
            case 1:
                ctype = ElectricityChartHelper.ChartType.Bar_Hourly;
                break;
            case 2:
                ctype = ElectricityChartHelper.ChartType.Bar_Daily;
                break;
            case 3:
                ctype = ElectricityChartHelper.ChartType.Bar_Monthly;
                break;
            case 4:
                ctype = ElectricityChartHelper.ChartType.Bar_Yearly;
                break;
            case 5:
                ctype = ElectricityChartHelper.ChartType.Pie_HourRank;
                break;
            case 6:
                ctype = ElectricityChartHelper.ChartType.Pie_DayRank;
                break;
            case 7:
                ctype = ElectricityChartHelper.ChartType.Pie_MonthRank;
                break;
            case 8:
                ctype = ElectricityChartHelper.ChartType.Pie_PhaseRank;
                break;
        }

        load(channelId, ctype);
    }

    public void load(int channelId) {
        load(channelId, ctype);
    }

    public boolean isVisible() {
        if (barChart != null && barChart.getVisibility() == View.VISIBLE) {
           return true;
        }

        if (pieChart != null && pieChart.getVisibility() == View.VISIBLE) {
            return true;
        }

        return false;
    }

}
