package org.supla.android;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import org.supla.android.db.DbHelper;
import org.supla.android.db.SuplaContract;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ElectricityChartHelper extends ChartHelper {

    public ElectricityChartHelper(Context context) {
        super(context);
    }

    protected Cursor getCursor(DbHelper DBH,
                               SQLiteDatabase db, int channelId, String dateFormat) {
        return DBH.getElectricityMeasurements(db, channelId, dateFormat);
    }

    protected float[] getValues(Cursor c) {
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

        return phases;
    }

    protected long getTimestamp(Cursor c) {
        return c.getLong(c.getColumnIndex(
                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP));
    }

    protected String[] getStackLabels() {
        Resources res = context.getResources();

        return new String[]{
                res.getString(R.string.em_phase1),
                res.getString(R.string.em_phase2),
                res.getString(R.string.em_phase3)};
    }

    protected List<Integer> getColors() {
        Resources res = context.getResources();

        List<Integer> Colors = new ArrayList<Integer>();
        Colors.set(0, res.getColor(R.color.phase1));
        Colors.set(1, res.getColor(R.color.phase2));
        Colors.set(2, res.getColor(R.color.phase2));

        return Colors;
    }


    public void loadPieChart(int channelId, ChartType ctype) {

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

}
