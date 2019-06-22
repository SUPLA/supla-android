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
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.supla.android.R;
import org.supla.android.db.DbHelper;
import org.supla.android.db.SuplaContract;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ElectricityChartHelper extends IncrementalMeterChartHelper {

    private double totalForwardActiveEnergy[];

    public ElectricityChartHelper(Context context) {
        super(context);
        totalForwardActiveEnergy = new double[]{0,0,0};
    }

    @Override
    protected Cursor getCursor(DbHelper DBH,
                               SQLiteDatabase db, int channelId, String dateFormat) {
        return DBH.getElectricityMeasurements(db, channelId, dateFormat, dateFrom, dateTo);
    }

    @Override
    protected void addBarEntries(int n, float time, Cursor c, ArrayList<BarEntry> entries) {
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
    }

    @Override
    protected void addLineEntries(int n, Cursor c, float time, ArrayList<Entry> entries) {

    }

    @Override
    protected void addPieEntries(ChartType ctype, SimpleDateFormat spf,
                                 Cursor c, ArrayList<PieEntry>entries) {
        if (ctype.equals(ChartType.Pie_PhaseRank)) {
            Resources res = context.getResources();

            entries.add(new PieEntry((float)totalForwardActiveEnergy[0], res.getText(R.string.em_phase1)));
            entries.add(new PieEntry((float)totalForwardActiveEnergy[1], res.getText(R.string.em_phase2)));
            entries.add(new PieEntry((float)totalForwardActiveEnergy[2], res.getText(R.string.em_phase3)));

        } else {

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

        }
    }

    @Override
    protected long getTimestamp(Cursor c) {
        return c.getLong(c.getColumnIndex(
                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP));
    }

    @Override
    protected SuplaBarDataSet newBarDataSetInstance(ArrayList<BarEntry> barEntries, String label) {
        SuplaBarDataSet result = super.newBarDataSetInstance(barEntries, label);

        Resources res = context.getResources();

        result.setStackLabels(new String[]{
                res.getString(R.string.em_phase1),
                res.getString(R.string.em_phase2),
                res.getString(R.string.em_phase3)});

        List<Integer> Colors = new ArrayList<Integer>(3);
        Colors.add(res.getColor(R.color.phase1));
        Colors.add(res.getColor(R.color.phase2));
        Colors.add(res.getColor(R.color.phase3));
        result.setColors(Colors);

        return result;
    }

    public void setTotalForwardActiveEnergy(double[] totalForwardActiveEnergy) {
        if (totalForwardActiveEnergy != null && totalForwardActiveEnergy.length == 3) {
            this.totalForwardActiveEnergy = totalForwardActiveEnergy;
        }
    }
}
