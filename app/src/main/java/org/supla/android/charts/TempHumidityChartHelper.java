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

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import org.supla.android.R;
import org.supla.android.db.DbHelper;
import org.supla.android.db.SuplaContract;
import java.util.ArrayList;
import java.util.List;

public class TempHumidityChartHelper extends TemperatureChartHelper {

    boolean temperatureVisible;
    boolean humidityVisible;

    @Override
    protected Cursor getCursor(DbHelper DBH,
                               SQLiteDatabase db, int channelId, String dateFormat) {
        return DBH.getTempHumidityMeasurements(db, channelId, dateFormat, dateFrom, dateTo);
    }

    @Override
    protected void addBarEntries(int n, float time, Cursor c, ArrayList<BarEntry> entries) {
        if (humidityVisible) {
            entries.add(new BarEntry(time, (float) c.getDouble(
                    c.getColumnIndex(
                            SuplaContract.TempHumidityLogEntry.COLUMN_NAME_HUMIDITY))));
        }
    }

    @Override
    protected void addLineEntries(int n, Cursor c, float time, ArrayList<Entry> entries) {
        if (temperatureVisible) {
            super.addLineEntries(n, c, time, entries);
        }
    }

    @Override
    protected long getTimestamp(Cursor c) {
        return c.getLong(c.getColumnIndex(
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TIMESTAMP));
    }

    @Override
    protected float getTemperature(Cursor c) {
        return (float) c.getDouble(
                c.getColumnIndex(
                        SuplaContract.TemperatureLogEntry.COLUMN_NAME_TEMPERATURE));
    }

    public TempHumidityChartHelper(Context context) {
        super(context);
    }

    public boolean isTemperatureVisible() {
        return temperatureVisible;
    }

    public boolean isHumidityVisible() {
        return humidityVisible;
    }

    public void setTemperatureVisible(boolean temperatureVisible) {
        this.temperatureVisible = temperatureVisible;
    }

    public void setHumidityVisible(boolean humidityVisible) {
        this.humidityVisible = humidityVisible;
    }

    @Override
    protected BarDataSet newBarDataSetInstance(ArrayList<BarEntry> barEntries, String label) {
        BarDataSet result = super.newBarDataSetInstance(barEntries, label);

        Resources res = context.getResources();
        result.setStackLabels(new String[]{res.getString(R.string.humidity)});
        List<Integer> Colors = new ArrayList<Integer>(1);
        Colors.add(res.getColor(R.color.th_humidity));
        result.setColors(Colors);

        return result;
    }
}
