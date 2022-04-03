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
import android.widget.Spinner;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.supla.android.R;
import org.supla.android.Preferences;
import org.supla.android.db.MeasurementsDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public abstract class ChartHelper implements IAxisValueFormatter {

    protected String unit;
    protected Context context;
    protected ChartType ctype = ChartType.Bar_Minutes;
    protected CombinedChart combinedChart;
    protected PieChart pieChart;
    protected Date dateFrom;
    protected Date dateTo;
    ArrayList<ILineDataSet> lineDataSets;
    ArrayList<Entry> lineEntries;
    private long minTimestamp;
    private LineDataSet lineDataSet;
    private Double downloadProgress;
    private Preferences prefs;
    private int channelId;

    public ChartHelper(Context context) {
        this.context = context;
        this.prefs = new Preferences(context);
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
        switch (chartType) {
            case Pie_HourRank:
            case Pie_WeekdayRank:
            case Pie_MonthRank:
            case Pie_PhaseRank:
                return true;
        }

        return false;
    }

    public boolean isComparsionChartType(ChartType chartType) {
        switch (chartType) {
            case Bar_Comparsion_MinMin:
            case Bar_Comparsion_HourHour:
            case Bar_Comparsion_DayDay:
            case Bar_Comparsion_MonthMonth:
            case Bar_Comparsion_YearYear:
                return true;
        }

        return false;
    }

    public boolean isBalanceChartType(ChartType chartType) {
        switch (chartType) {
            case Bar_AritmeticBalance_Minutes:
            case Bar_AritmeticBalance_Hours:
            case Bar_AritmeticBalance_Days:
            case Bar_AritmeticBalance_Months:
            case Bar_AritmeticBalance_Years:
            case Bar_VectorBalance_Minutes:
            case Bar_VectorBalance_Hours:
            case Bar_VectorBalance_Days:
            case Bar_VectorBalance_Months:
            case Bar_VectorBalance_Years:
                return true;
        }

        return false;
    }

    public boolean isVectorBalanceChartType(ChartType chartType) {
        switch (chartType) {
            case Bar_VectorBalance_Minutes:
            case Bar_VectorBalance_Hours:
            case Bar_VectorBalance_Days:
            case Bar_VectorBalance_Months:
            case Bar_VectorBalance_Years:
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

        if (isPieChartType(ctype)) {
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
        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return spf.format(new java.util.Date((minTimestamp + (long) (value * 600f)) * 1000));
    }

    abstract protected Cursor getCursor(MeasurementsDbHelper DBH, int channelId, String dateFormat);

    abstract protected void addBarEntries(int n, float time, Cursor c,
                                          ArrayList<BarEntry> entries);

    abstract protected void addLineEntries(int n, Cursor c, float time,
                                           ArrayList<Entry> entries);

    abstract protected void addPieEntries(SimpleDateFormat spf,
                                          Cursor c, ArrayList<PieEntry> entries);

    abstract protected long getTimestamp(Cursor c);

    protected IMarker getMarker() {
        return null;
    }

    protected void setMarker(Chart chart) {
        IMarker m = getMarker();
        chart.setMarker(m);
        chart.setDrawMarkers(m != null);
        if (m instanceof MarkerView) {
            ((MarkerView) m).setChartView(chart);
        }
    }

    protected void addFormattedValue(Cursor cursor, SimpleDateFormat spf) {
    }

    protected LineDataSet newLineDataSetInstance(ArrayList<Entry> lineEntries, String label) {
        LineDataSet result = new LineDataSet(lineEntries, label);
        result.setDrawValues(false);
        result.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        result.setCubicIntensity(0.05f);
        result.setDrawCircles(false);
        result.setDrawFilled(true);
        return result;
    }

    protected void newLineDataSet() {
        if (lineEntries != null
                && lineDataSets != null
                && lineEntries.size() > 0) {
            lineDataSet = newLineDataSetInstance(lineEntries, "");
            lineDataSets.add(lineDataSet);
        }
    }

    protected ArrayList<Entry> newLineEntries() {
        newLineDataSet();

        lineEntries = new ArrayList<>();
        return lineEntries;
    }

    protected SuplaBarDataSet newBarDataSetInstance(ArrayList<BarEntry> barEntries, String label) {
        SuplaBarDataSet result = new SuplaBarDataSet(barEntries, label);
        result.setDrawValues(false);
        return result;
    }

    protected void prepareBarDataSet(SuplaBarDataSet barDataSet) {
    }

    public void loadCombinedChart(int channelId) {

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
        combinedChart.setData(null);
        combinedChart.clear();
        combinedChart.invalidate();

        updateDescription();

        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        String DateFormat = "%Y-%m-%dT%H:%M:00.000";
        switch (ctype) {
            case Bar_Hours:
            case Bar_Comparsion_HourHour:
            case Bar_AritmeticBalance_Hours:
            case Bar_VectorBalance_Hours:
                DateFormat = "%Y-%m-%dT%H:00:00.000";
                spf = new SimpleDateFormat("yyyy-MM-dd HH");
                break;
            case Bar_Days:
            case Bar_Comparsion_DayDay:
            case Bar_AritmeticBalance_Days:
            case Bar_VectorBalance_Days:
                DateFormat = "%Y-%m-%dT00:00:00.000";
                spf = new SimpleDateFormat("yyyy-MM-dd");
                break;
            case Bar_Months:
            case Bar_Comparsion_MonthMonth:
            case Bar_AritmeticBalance_Months:
            case Bar_VectorBalance_Months:
                DateFormat = "%Y-%m-01T00:00:00.000";
                spf = new SimpleDateFormat("yyyy LLLL");
                break;
            case Bar_Years:
            case Bar_Comparsion_YearYear:
            case Bar_AritmeticBalance_Years:
            case Bar_VectorBalance_Years:
                DateFormat = "%Y-01-01T00:00:00.000";
                spf = new SimpleDateFormat("yyyy");
                break;

        }

        lineDataSets = new ArrayList<ILineDataSet>();
        ArrayList<IBarDataSet> barDataSets = new ArrayList<IBarDataSet>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        newLineEntries();

        MeasurementsDbHelper DBH = MeasurementsDbHelper.getInstance(context);
        Cursor c = getCursor(DBH, channelId, DateFormat);
        if (c != null) {
            if (c.moveToFirst()) {
                int n = 0;
                minTimestamp = getTimestamp(c);
                do {
                    n++;
                    addBarEntries(n, (getTimestamp(c) - minTimestamp) / 600f, c,
                            barEntries);
                    addLineEntries(n, c, (getTimestamp(c) - minTimestamp) / 600f,
                            lineEntries);
                    addFormattedValue(c, spf);

                } while (c.moveToNext());

            }

            c.close();
        }

        if (barEntries.size() > 0 && isComparsionChartType(ctype)) {
            for (int a = barEntries.size() - 1; a > 0; a--) {

                BarEntry e1 = barEntries.get(a);
                BarEntry e2 = barEntries.get(a - 1);
                e1.setVals(new float[]{e1.getY() - e2.getY()});
                barEntries.set(a, e1);
            }

            barEntries.remove(0);
        }

        if (barEntries.size() > 0) {
            SuplaBarDataSet barDataSet = newBarDataSetInstance(barEntries, "");
            prepareBarDataSet(barDataSet);
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

        setMarker(combinedChart);

        if (data.getDataSetCount() != 0) {
            combinedChart.setData(data);
        }

        combinedChart.invalidate();

        lineEntries = null;
        lineDataSet = null;
        lineDataSets = null;
    }

    public void loadPieChart(int channelId) {

        if (combinedChart != null) {
            combinedChart.setVisibility(View.GONE);
        }

        if (pieChart == null) {
            return;
        }

        pieChart.setVisibility(View.VISIBLE);
        pieChart.setData(null);
        pieChart.clear();
        pieChart.invalidate();

        SimpleDateFormat spf = new SimpleDateFormat("HH");

        String DateFormat = "2018-01-01T%H:00:00.000";
        switch (ctype) {
            case Pie_WeekdayRank:
                DateFormat = "2018-01-%wT00:00:00.000";
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

        MeasurementsDbHelper DBH = MeasurementsDbHelper.getInstance(context);
        Cursor c = getCursor(DBH, channelId, DateFormat);
        if (c != null) {

            if (c.moveToFirst()) {

                if (ctype.equals(ChartType.Pie_PhaseRank)) {
                    addPieEntries(spf, c, entries);
                } else {
                    do {
                        addPieEntries(spf, c, entries);
                    } while (c.moveToNext());
                }


            }

            c.close();
        }

        updateDescription();

        Collections.sort(entries, new Comparator<PieEntry>() {
            @Override
            public int compare(PieEntry p1, PieEntry p2) {
                if (p1.getValue() > p2.getValue())
                    return 1;
                if (p1.getValue() < p2.getValue())
                    return -1;
                return 0;
            }
        });

        SuplaPieDataSet set = new SuplaPieDataSet(entries, "");
        set.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(set);
        setMarker(pieChart);
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

    private void calculateDescPosition(Chart chart, Description desc) {
        float x = chart.getWidth() -
                chart.getViewPortHandler().offsetRight() - desc.getXOffset();
        float y = chart.getHeight() - desc.getYOffset();

        desc.setPosition(x, y);
    }

    private void updateDescription() {
        Resources r = context.getResources();
        String description = "";
        String noData = r.getString(R.string.no_chart_data_available);

        if (downloadProgress != null) {
            description =
                    r.getString(R.string.retrieving_data_from_the_server);
            if (downloadProgress > 0) {
                description += Integer.toString(downloadProgress.intValue()) + "%";
            }

            noData = description;
            description += " ";
        }

        if (unit != null) {
            if (description.length() > 0) {
                description += " | ";
            }
            description += unit;
        }

        if (combinedChart != null) {
            Description desc = combinedChart.getDescription();
            desc.setText(description);
            calculateDescPosition(combinedChart, desc);

            combinedChart.setDescription(desc);
            combinedChart.setNoDataText(noData);
            combinedChart.invalidate();
        }

        if (pieChart != null) {
            Description desc = pieChart.getDescription();
            desc.setText(description);
            calculateDescPosition(pieChart, desc);

            pieChart.setDescription(desc);
            pieChart.setNoDataText(noData);
            combinedChart.invalidate();
        }
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
        updateDescription();
    }

    public long getMinTimestamp() {
        return minTimestamp;
    }

    public void load(int channelId, ChartType ctype) {
        this.channelId = channelId;
        this.ctype = ctype;

        if (isPieChartType(ctype)) {
            loadPieChart(channelId);
        } else {
            loadCombinedChart(channelId);
        }
    }

    public void clearData() {
        if (combinedChart != null) {
            combinedChart.setData(null);
            combinedChart.invalidate();
        }

        if (pieChart != null) {
            pieChart.setData(null);
            pieChart.invalidate();
        }
    }

    public String[] getMasterSpinnerItems(int limit) {

        if (limit <= 0 || limit > 24) {
            limit = 24;
        }

        String[] result = new String[limit];
        Resources r = context.getResources();

        for (int a = 0; a < limit; a++) {
            switch (a) {
                case 0:
                    result[a] = r.getString(R.string.minutes);
                    break;
                case 1:
                    result[a] = r.getString(R.string.hours);
                    break;
                case 2:
                    result[a] = r.getString(R.string.days);
                    break;
                case 3:
                    result[a] = r.getString(R.string.months);
                    break;
                case 4:
                    result[a] = r.getString(R.string.years);
                    break;
                case 5:
                    result[a] = r.getString(R.string.comparsion_minmin);
                    break;
                case 6:
                    result[a] = r.getString(R.string.comparsion_hourhour);
                    break;
                case 7:
                    result[a] = r.getString(R.string.comparsion_dayday);
                    break;
                case 8:
                    result[a] = r.getString(R.string.comparsion_monthmonth);
                    break;
                case 9:
                    result[a] = r.getString(R.string.comparsion_yearyear);
                    break;
                case 10:
                    result[a] = r.getString(R.string.ranking_of_hours);
                    break;
                case 11:
                    result[a] = r.getString(R.string.ranking_of_weekdays);
                    break;
                case 12:
                    result[a] = r.getString(R.string.ranking_of_months);
                    break;
                case 13:
                    result[a] = r.getString(R.string.consumption_acording_to_phases);
                    break;
                case 14:
                    result[a] = r.getString(R.string.aritmetic_balance_minutes);
                    break;
                case 15:
                    result[a] = r.getString(R.string.aritmetic_balance_hours);
                    break;
                case 16:
                    result[a] = r.getString(R.string.aritmetic_balance_days);
                    break;
                case 17:
                    result[a] = r.getString(R.string.aritmetic_balance_months);
                    break;
                case 18:
                    result[a] = r.getString(R.string.aritmetic_balance_years);
                    break;
                case 19:
                    result[a] = r.getString(R.string.vector_balance_minutes);
                    break;
                case 20:
                    result[a] = r.getString(R.string.vector_balance_hours);
                    break;
                case 21:
                    result[a] = r.getString(R.string.vector_balance_days);
                    break;
                case 22:
                    result[a] = r.getString(R.string.vector_balance_months);
                    break;
                case 23:
                    result[a] = r.getString(R.string.vector_balance_years);
                    break;
            }

        }

        return result;
    }

    public ChartType chartTypeByIndex(int chartTypeIdx) {
        switch (chartTypeIdx) {
            case 1:
                return ChartType.Bar_Hours;
            case 2:
                return ChartType.Bar_Days;
            case 3:
                return ChartType.Bar_Months;
            case 4:
                return ChartType.Bar_Years;
            case 5:
                return ChartType.Bar_Comparsion_MinMin;
            case 6:
                return ChartType.Bar_Comparsion_HourHour;
            case 7:
                return ChartType.Bar_Comparsion_DayDay;
            case 8:
                return ChartType.Bar_Comparsion_MonthMonth;
            case 9:
                return ChartType.Bar_Comparsion_YearYear;
            case 10:
                return ChartType.Pie_HourRank;
            case 11:
                return ChartType.Pie_WeekdayRank;
            case 12:
                return ChartType.Pie_MonthRank;
            case 13:
                return ChartType.Pie_PhaseRank;
            case 14:
                return ChartType.Bar_AritmeticBalance_Minutes;
            case 15:
                return ChartType.Bar_AritmeticBalance_Hours;
            case 16:
                return ChartType.Bar_AritmeticBalance_Days;
            case 17:
                return ChartType.Bar_AritmeticBalance_Months;
            case 18:
                return ChartType.Bar_AritmeticBalance_Years;
            case 19:
                return ChartType.Bar_VectorBalance_Minutes;
            case 20:
                return ChartType.Bar_VectorBalance_Hours;
            case 21:
                return ChartType.Bar_VectorBalance_Days;
            case 22:
                return ChartType.Bar_VectorBalance_Months;
            case 23:
                return ChartType.Bar_VectorBalance_Years;
        }

        return ElectricityChartHelper.ChartType.Bar_Minutes;
    }

    public String[] getSlaveSpinnerItems(Spinner master) {

        ArrayList<String> result = new ArrayList<>();
        Resources r = context.getResources();

        result.add(r.getString(R.string.last24hours));
        result.add(r.getString(R.string.last7days));
        result.add(r.getString(R.string.last30days));
        result.add(r.getString(R.string.last90days));
        result.add(r.getString(R.string.all_available_history));

        if (master != null) {
            switch (chartTypeByIndex(master.getSelectedItemPosition())) {
                case Bar_Minutes:
                case Bar_Hours:
                case Bar_Comparsion_MinMin:
                case Bar_Comparsion_HourHour:
                case Bar_AritmeticBalance_Minutes:
                case Bar_AritmeticBalance_Hours:
                case Bar_VectorBalance_Minutes:
                case Bar_VectorBalance_Hours:
                    break;
                case Bar_Days:
                case Bar_Comparsion_DayDay:
                case Bar_AritmeticBalance_Days:
                case Bar_VectorBalance_Days:
                    result.remove(0);
                    break;
                default:
                    result.clear();
            }
        }

        return result.toArray(new String[0]);
    }

    public void load(int channelId, int chartTypeIdx) {
        load(channelId, chartTypeByIndex(chartTypeIdx));
    }

    public void load(int channelId) {
        load(channelId, ctype);
    }

    public void setDateRange(Date from, Date to) {
        dateFrom = from;
        dateTo = to;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void persistSpinners(Spinner master, Spinner slave) {
        prefs.setChartType(channelId, 0, master.getSelectedItemPosition());
        prefs.setChartType(channelId, 1, slave.getSelectedItemPosition());
    }

    public void persistSpinner(Spinner master) {
        prefs.setChartType(channelId, 3, master.getSelectedItemPosition());
    }

    public void restoreSpinner(Spinner master) {
        int mct = prefs.getChartType(channelId, 3, -1);
        if(mct > -1) {
            master.setSelection(mct);
        }
    }
    

    public void restoreSpinners(Spinner master, Spinner slave,
                                Runnable slaveReload) {
        int mct, sct;
        mct = prefs.getChartType(channelId, 0, -1);
        sct = prefs.getChartType(channelId, 1, -1);
        if(mct < 0) mct = 0;
        if(sct < 0) sct = 0;
        master.setSelection(mct);
        slaveReload.run();
        
        slave.setSelection(sct);
    }

    public void setDateRangeBySpinners(Spinner master, Spinner slave) {
        Date dateFrom = null;
        Date dateTo = null;

        int calendar_field = Calendar.DATE;
        int calendar_amount = 0;

        int position = slave.getSelectedItemPosition();

        if (master != null) {
            switch (chartTypeByIndex(master.getSelectedItemPosition())) {
                case Bar_Days:
                case Bar_Comparsion_DayDay:
                case Bar_AritmeticBalance_Days:
                case Bar_VectorBalance_Days:
                    position++;
                    break;
            }
        }

        switch (position) {
            case 0:
                calendar_amount = -24;
                calendar_field = Calendar.HOUR;
                break;
            case 1:
                calendar_amount = -7;
                break;
            case 2:
                calendar_amount = -30;
                break;
            case 3:
                calendar_amount = -90;
                break;
        }

        if (calendar_amount != 0) {
            Calendar now = Calendar.getInstance();
            now.add(calendar_field, calendar_amount);
            dateFrom = now.getTime();
        }

        if (dateFrom != null) {
            dateTo = new Date();
        }

        setDateRange(dateFrom, dateTo);
    }

    public boolean isVisible() {
        if (combinedChart != null && combinedChart.getVisibility() == View.VISIBLE) {
            return true;
        }

        return pieChart != null && pieChart.getVisibility() == View.VISIBLE;
    }

    public Double getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(Double downloadProgress) {
        this.downloadProgress = downloadProgress;
        updateDescription();
    }

    public enum ChartType {
        Bar_Minutes,
        Bar_Hours,
        Bar_Days,
        Bar_Months,
        Bar_Years,
        Bar_Comparsion_MinMin,
        Bar_Comparsion_HourHour,
        Bar_Comparsion_DayDay,
        Bar_Comparsion_MonthMonth,
        Bar_Comparsion_YearYear,
        Pie_HourRank,
        Pie_WeekdayRank,
        Pie_MonthRank,
        Pie_PhaseRank,
        Bar_AritmeticBalance_Minutes,
        Bar_AritmeticBalance_Hours,
        Bar_AritmeticBalance_Days,
        Bar_AritmeticBalance_Months,
        Bar_AritmeticBalance_Years,
        Bar_VectorBalance_Minutes,
        Bar_VectorBalance_Hours,
        Bar_VectorBalance_Days,
        Bar_VectorBalance_Months,
        Bar_VectorBalance_Years
    }
}
