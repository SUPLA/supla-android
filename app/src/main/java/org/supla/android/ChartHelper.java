package org.supla.android;

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
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.supla.android.db.DbHelper;
import org.supla.android.db.SuplaContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ChartHelper implements IAxisValueFormatter {

    private Context context;
    private ChartType ctype = ChartType.Bar_Hourly;
    private BarChart barChart;
    private PieChart pieChart;
    private long timestampMin;
    private long timestampMax;
    private long timestampStep;
    private int xValueMax;

    public ChartHelper(Context context) {
        this.context = context;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        double ts = timestampMin + ((value - 1) * timestampStep);
        if (ts > timestampMax) {
            ts = timestampMax;
        }

        SimpleDateFormat spf = null;

        switch (ctype) {
            case Bar_Minutely:
                spf = new SimpleDateFormat("HH:mm");
                break;
            case Bar_Hourly:
                spf = new SimpleDateFormat("HH");
                break;
            case Bar_Daily:
                spf = new SimpleDateFormat("yy-MM-dd");
                break;
            case Bar_Monthly:
                spf = new SimpleDateFormat("MMM");
                break;
            case Bar_Yearly:
                spf = new SimpleDateFormat("yyyy");
                break;
        }

        return spf == null ? "" : spf.format(new java.util.Date((long) ts * 1000));
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

    private void loadBarChart(int channelId, ChartType ctype) {

        if (barChart == null) {
            return;
        }

        barChart.getXAxis().setValueFormatter(this);
        barChart.getXAxis().setGranularity(1f);
        barChart.getAxisLeft().setDrawLabels(false);

        Description desc = barChart.getDescription();
        desc.setText("");
        barChart.setDescription(desc);

        String DateFormat = "%Y-%m-%dT%H:%M:00.000";
        switch (ctype) {
            case Bar_Hourly:
                DateFormat = "%Y-%m-%dT%H:00:00.000";
                break;
            case Bar_Daily:
                DateFormat = "%Y-%m-%dT00:00:00.000";
                break;
            case Bar_Monthly:
                DateFormat = "%Y-%m-01T00:00:00.000";
                break;
            case Bar_Yearly:
                DateFormat = "%Y-01-01T00:00:00.000";
                break;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();

        DbHelper DBH = new DbHelper(context, true);
        SQLiteDatabase db = DBH.getReadableDatabase();
        try {
            Cursor c = DBH.getElectricityMeasurements(db, channelId, DateFormat);

            if (c != null) {

                if (c.moveToFirst()) {

                    timestampMin = c.getLong(c.getColumnIndex(
                            SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP));
                    int n = 0;

                    do {
                        n++;
                        float[] phases = new float[3];
                        phases[0] = (float) c.getDouble(
                                c.getColumnIndex(
                                        SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE));
                        phases[1] = (float) c.getDouble(
                                c.getColumnIndex(
                                        SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE));
                        phases[2] = (float) c.getDouble(
                                c.getColumnIndex(
                                        SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE));

                        entries.add(new BarEntry(n, phases));
                        xValueMax = n;

                        if (c.isLast()) {
                            timestampMax = c.getLong(
                                    c.getColumnIndex(
                                            SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP));
                        }
                    } while (c.moveToNext());

                    timestampStep = (timestampMax - timestampMin) / n;
                }

                c.close();
            }
        } finally {
            db.close();
        }

        BarDataSet dataset = new BarDataSet(entries, "");
        dataset.setDrawValues(false);

        Resources res = context.getResources();

        dataset.setStackLabels(new String[]{
                res.getString(R.string.em_phase1),
                res.getString(R.string.em_phase2),
                res.getString(R.string.em_phase3)});
        dataset.setColors(res.getColor(R.color.phase1),
                res.getColor(R.color.phase2),
                res.getColor(R.color.phase3));
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(dataset);

        barChart.setDrawMarkers(true);
        barChart.setData(new BarData(dataSets));
        barChart.invalidate();
    }

    private void loadPieChart(int channelId, ChartType ctype) {

        if (pieChart == null) {
            return;
        }

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
            Cursor c = DBH.getElectricityMeasurements(db, channelId, DateFormat);

            if (c != null) {

                if (c.moveToFirst()) {

                    if (ctype.equals(ChartType.Pie_PhaseRank)) {
                        Resources res = context.getResources();

                        float phase = (float) c.getDouble(
                                c.getColumnIndex(
                                        SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE));

                        entries.add(new PieEntry(phase, res.getText(R.string.em_phase1)));

                        phase = (float) c.getDouble(
                                c.getColumnIndex(
                                        SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE));

                        entries.add(new PieEntry(phase, res.getText(R.string.em_phase2)));

                        phase = (float) c.getDouble(
                                c.getColumnIndex(
                                        SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE));

                        entries.add(new PieEntry(phase, res.getText(R.string.em_phase3)));

                    } else {
                        do {
                            float phases;
                            phases = (float) c.getDouble(
                                    c.getColumnIndex(
                                            SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE));
                            phases += (float) c.getDouble(
                                    c.getColumnIndex(
                                            SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE));
                            phases += (float) c.getDouble(
                                    c.getColumnIndex(
                                            SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE));


                            long timestamp = c.getLong(
                                    c.getColumnIndex(
                                            SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP));

                            entries.add(new PieEntry(phases,
                                    spf.format(new java.util.Date(timestamp * 1000))));

                        } while (c.moveToNext());
                    }


                }

                c.close();
            }
        } finally {
            db.close();
        }

        Description desc = pieChart.getDescription();
        desc.setText("");
        pieChart.setDescription(desc);

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(set);
        pieChart.setData(data);

        pieChart.invalidate();
    }

    public void loadElectricityMeasurements(int channelId, ChartType ctype) {

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

    public void loadElectricityMeasurements(int channelId) {
        loadElectricityMeasurements(channelId, ctype);
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

}
