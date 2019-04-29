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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.View;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.supla.android.Trace;
import org.supla.android.db.DbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class ChartHelper implements IAxisValueFormatter {

    protected String unit;
    protected Context context;
    protected ChartType ctype = ChartType.Bar_Hourly;
    protected CombinedChart combinedChart;
    protected PieChart pieChart;
    private long minTimestamp;
    private LineDataSet lineDataSet;
    ArrayList<ILineDataSet> lineDataSets;
    ArrayList<Entry> lineEntries;

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


    public CombinedChart getCombinedChart() {
        return combinedChart;
    }

    public void setCombinedChart(CombinedChart chart) {
        combinedChart = chart;
    }

    public PieChart getPieChart() {
        return pieChart;
    }

    public void setPieChart(PieChart chart) {
        pieChart = chart;
    }

    public boolean isPieChartType(ChartType chartType) {
        switch (ctype) {
            case Pie_HourRank:
            case Pie_DayRank:
            case Pie_MonthRank:
            case Pie_PhaseRank:
                return true;
        }

        return false;
    }

    public void setVisibility(int visibility) {
        if (combinedChart != null) {
            combinedChart.setVisibility(View.GONE);
        }

        if (pieChart != null) {
            pieChart.setVisibility(View.GONE);
        }

        if ( isPieChartType(ctype) ) {
            if (pieChart != null) {
                pieChart.setVisibility(visibility);
            }
        } else {
            if (combinedChart != null) {
                combinedChart.setVisibility(visibility);
            }
        }
    }

    public void animate() {
        if (combinedChart != null
                && combinedChart.getVisibility() == View.VISIBLE) {
            combinedChart.animateY(1000);
        } else if (pieChart != null && pieChart.getVisibility() == View.VISIBLE) {
            pieChart.spin(500, 0, -360f, Easing.EasingOption.EaseInOutQuad);
        }
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return Float.toString(value);
    }


    abstract protected Cursor getCursor(DbHelper DBH,
                                      SQLiteDatabase db, int channelId, String dateFormat);

    abstract protected void addBarEntries(int n, float time, Cursor c,
                                          ArrayList<BarEntry> entries);

    abstract protected void addLineEntries(int n, Cursor c, float time,
                                           ArrayList<Entry> entries);

    abstract protected void addPieEntries(ChartType ctype, SimpleDateFormat spf,
                                          Cursor c, ArrayList<PieEntry>entries);

    abstract protected long getTimestamp(Cursor c);

    abstract protected String[] getStackLabels();

    abstract protected List<Integer> getColors();

    protected IMarker getMarker() {
        return null;
    }

    protected void addFormatterValue(Cursor cursor, SimpleDateFormat spf) {}

    protected void newLineDataSet() {
        if (lineEntries != null
                && lineDataSets != null
                && lineEntries.size() > 0) {
            lineDataSet = new LineDataSet(lineEntries, "");
            lineDataSet.setDrawValues(false);
            lineDataSet.setColors(Color.RED);
            lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            lineDataSet.setCubicIntensity(0.05f);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawFilled(true);
            lineDataSets.add(lineDataSet);
        }
    }

    protected ArrayList<Entry> newLineEntries() {
        newLineDataSet();

        lineEntries = new ArrayList<>();
        return lineEntries;
    }

    public void loadCombinedChart(int channelId, ChartType ctype) {

        if (pieChart != null) {
            pieChart.setVisibility(View.GONE);
        }

        if (combinedChart == null) {
            return;
        }

        lineEntries = null;
        lineDataSet = null;
        lineDataSets = null;

        combinedChart.setVisibility(View.VISIBLE);
        combinedChart.getXAxis().setValueFormatter(this);
        combinedChart.getXAxis().setLabelCount(3);
        combinedChart.getAxisLeft().setDrawLabels(false);
        combinedChart.getLegend().setEnabled(false);

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

        lineDataSets = new ArrayList<ILineDataSet>();
        ArrayList<IBarDataSet> barDataSets = new ArrayList<IBarDataSet>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        newLineEntries();

        DbHelper DBH = new DbHelper(context, true);
        SQLiteDatabase db = DBH.getReadableDatabase();
        try {
            Cursor c = getCursor(DBH, db, channelId, DateFormat);

            if (c != null) {
                if (c.moveToFirst()) {
                    int n = 0;
                    minTimestamp = getTimestamp(c);
                    do {
                        n++;
                        addBarEntries(n, (getTimestamp(c)-minTimestamp) / 600, c,
                                barEntries);
                        addLineEntries(n, c, (getTimestamp(c)-minTimestamp) / 600,
                                lineEntries);
                        addFormatterValue(c, spf);

                    } while (c.moveToNext());

                }

                c.close();
            }
        } finally {
            db.close();
        }

        if (barEntries.size() > 0) {
            BarDataSet barDataSet = new BarDataSet(barEntries, "");
            barDataSet.setDrawValues(false);
            barDataSet.setStackLabels(getStackLabels());
            barDataSet.setColors(getColors());
            barDataSets.add(barDataSet);
        }

        newLineDataSet();

        CombinedData data = new CombinedData();
        if (barDataSets.size() > 0) {
            data.setData(new BarData(barDataSets));
        }

        if (lineDataSets.size() > 0) {
            data.setData(new LineData(lineDataSets));
        }

        combinedChart.setMarker(getMarker());
        combinedChart.setDrawMarkers(combinedChart.getMarker()!=null);

        if (data.getDataSetCount() == 0) {
            combinedChart.setData(null);
        } else {
            combinedChart.setData(data);
        }

        combinedChart.invalidate();

        lineEntries = null;
        lineDataSet = null;
        lineDataSets = null;
    }

    public void loadPieChart(int channelId, ChartType ctype) {

        if (combinedChart != null) {
            combinedChart.setVisibility(View.GONE);
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
        combinedChart.setVisibleXRangeMaximum(maxXRange1);
        combinedChart.moveViewToX(combinedChart.getXChartMax());
        combinedChart.setVisibleXRangeMaximum(maxXRange2);
    }

    public void moveToEnd() {
        moveToEnd(20, 1000);
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;

        if (combinedChart !=null) {
            Description desc = combinedChart.getDescription();
            desc.setText(unit == null ? "" : unit);
            combinedChart.setDescription(desc);
        }

        if (pieChart!=null) {
            Description desc = pieChart.getDescription();
            desc.setText(unit == null ? "" : unit);
            pieChart.setDescription(desc);
        }
    }

    public long getMinTimestamp() {
        return minTimestamp;
    }

    public void load(int channelId, ChartType ctype) {

        if (!this.ctype.equals(ctype)) {
            this.ctype = ctype;
        }

        minTimestamp = 0;

        switch (ctype) {
            case Bar_Minutely:
            case Bar_Hourly:
            case Bar_Daily:
            case Bar_Monthly:
            case Bar_Yearly:
                loadCombinedChart(channelId, ctype);
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
        if (combinedChart != null && combinedChart.getVisibility() == View.VISIBLE) {
           return true;
        }

        if (pieChart != null && pieChart.getVisibility() == View.VISIBLE) {
            return true;
        }

        return false;
    }

}
