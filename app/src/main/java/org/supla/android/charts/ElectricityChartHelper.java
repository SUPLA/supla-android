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

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;

import org.supla.android.R;
import org.supla.android.db.DbHelper;
import org.supla.android.db.SuplaContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ElectricityChartHelper extends IncrementalMeterChartHelper {

    private double totalActiveEnergy[];
    private boolean mProductionDataSource;
    private String colPhase1;
    private String colPhase2;
    private String colPhase3;

    public ElectricityChartHelper(Context context) {
        super(context);
        totalActiveEnergy = new double[]{0, 0, 0};
        setProductionDataSource(false);
    }

    @Override
    protected Cursor getCursor(DbHelper DBH,
                               SQLiteDatabase db, int channelId, String dateFormat) {
        return DBH.getElectricityMeasurements(db, channelId, dateFormat, dateFrom, dateTo);
    }

    @Override
    protected void addBarEntries(int n, float time, Cursor c, ArrayList<BarEntry> entries) {
        if (isBalanceChartType(ctype)) {

            float value = 0;

            if (isVectorBalanceChartType(ctype)) {
                double prod = c.getDouble(
                        c.getColumnIndex(
                                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_RAE_BALANCED));
                double cons = c.getDouble(
                        c.getColumnIndex(
                                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_FAE_BALANCED));
                value = (float) (cons - prod);
            } else {
                double prod1 = c.getDouble(
                        c.getColumnIndex(
                                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_RAE));
                double prod2 = c.getDouble(
                        c.getColumnIndex(
                                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_RAE));
                double prod3 = c.getDouble(
                        c.getColumnIndex(
                                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_RAE));

                double cons1 = c.getDouble(
                        c.getColumnIndex(
                                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE));
                double cons2 = c.getDouble(
                        c.getColumnIndex(
                                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE));
                double cons3 = c.getDouble(
                        c.getColumnIndex(
                                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE));

                value = (float) ((cons1 + cons2 + cons3) - (prod1 + prod2 + prod3));
            }

            entries.add(new BarEntry(n, value));

        } else {
            float[] phases = new float[3];
            phases[0] = (float) c.getDouble(
                    c.getColumnIndex(colPhase1));
            phases[1] = (float) c.getDouble(
                    c.getColumnIndex(colPhase2));
            phases[2] = (float) c.getDouble(
                    c.getColumnIndex(colPhase3));

            entries.add(new BarEntry(n, phases));
        }
    }

    @Override
    protected void addLineEntries(int n, Cursor c, float time, ArrayList<Entry> entries) {

    }

    @Override
    protected void addPieEntries(SimpleDateFormat spf, Cursor c, ArrayList<PieEntry> entries) {
        if (ctype.equals(ChartType.Pie_PhaseRank)) {
            Resources res = context.getResources();

            entries.add(new PieEntry((float) totalActiveEnergy[0],
                    res.getText(R.string.em_phase1).toString()));
            entries.add(new PieEntry((float) totalActiveEnergy[1],
                    res.getText(R.string.em_phase2).toString()));
            entries.add(new PieEntry((float) totalActiveEnergy[2],
                    res.getText(R.string.em_phase3).toString()));

        } else {

            float phases;
            phases = (float) c.getDouble(
                    c.getColumnIndex(colPhase1));
            phases += (float) c.getDouble(
                    c.getColumnIndex(colPhase2));
            phases += (float) c.getDouble(
                    c.getColumnIndex(colPhase3));


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

    @Override
    protected void prepareBarDataSet(SuplaBarDataSet barDataSet) {
        if ((isProductionDataSource() && isComparsionChartType(ctype))
                || isBalanceChartType(ctype)) {
            barDataSet.setColorDependsOnTheValue(true);
            barDataSet.setColors(getBarChartComparsionColors(!isBalanceChartType(ctype)));
        } else {
            super.prepareBarDataSet(barDataSet);
        }
    }

    public void setTotalActiveEnergy(double[] totalActiveEnergy) {
        if (totalActiveEnergy != null && totalActiveEnergy.length == 3) {
            this.totalActiveEnergy = totalActiveEnergy;
        }
    }

    @Override
    public String[] getMasterSpinnerItems(int limit) {
        String[] result = super.getMasterSpinnerItems(limit);
        if (mProductionDataSource && result != null && result.length >= 14) {
            Resources r = context.getResources();
            result[13] = r.getString(R.string.production_acording_to_phases);
        }
        return result;
    }

    public boolean isProductionDataSource() {
        return mProductionDataSource;
    }

    public void setProductionDataSource(boolean mProduction) {
        this.mProductionDataSource = mProduction;
        if (mProduction) {
            colPhase1 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_RAE;
            colPhase2 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_RAE;
            colPhase3 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_RAE;
        } else {
            colPhase1 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE;
            colPhase2 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE;
            colPhase3 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE;
        }
    }

    @Override
    public String getCurrency() {
        return mProductionDataSource ? null : currency;
    }
}
