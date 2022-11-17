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
import android.annotation.SuppressLint;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;

import org.supla.android.R;
import org.supla.android.SuplaApp;
import org.supla.android.TemperaturePresenterFactory;
import org.supla.android.data.TemperatureFormatter;
import org.supla.android.db.MeasurementsDbHelper;
import org.supla.android.db.SuplaContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ThermostatChartHelper extends ChartHelper {

    private TemperaturePresenterFactory temperaturePresenterFactory;

    public ThermostatChartHelper(Context context) {
        super(context);
        temperaturePresenterFactory = SuplaApp.getApp();
    }

    @Override
    protected Cursor getCursor(MeasurementsDbHelper DBH, int channelId, String dateFormat) {
        return DBH.getThermostatMeasurements(channelId);
    }

    @Override
    @SuppressLint("Range")
    protected void addBarEntries(int n, float time, Cursor c, ArrayList<BarEntry> entries) {
        float[] phases = new float[1];
        TemperatureFormatter tp = temperaturePresenterFactory.getTemperaturePresenter();
        phases[0] = (float) tp.getTemperatureInConfiguredUnit(c.getDouble(
                c.getColumnIndex(
                        SuplaContract.ThermostatLogEntry.COLUMN_NAME_MEASUREDTEMPERATURE)));

        entries.add(new BarEntry(time, phases));
    }

    @Override
    protected void addLineEntries(int n, Cursor c, float time, ArrayList<Entry> entries) {

    }

    @Override
    protected void addPieEntries(SimpleDateFormat spf, Cursor c, ArrayList<PieEntry> entries) {

    }

    @Override
    @SuppressLint("Range")
    protected long getTimestamp(Cursor c) {
        return c.getLong(c.getColumnIndex(
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP));
    }

    @Override
    protected SuplaBarDataSet newBarDataSetInstance(ArrayList<BarEntry> barEntries, String label) {
        SuplaBarDataSet result = super.newBarDataSetInstance(barEntries, label);

        Resources res = context.getResources();
        result.setStackLabels(new String[]{res.getString(R.string.hp_room_temperature)});

        List<Integer> Colors = new ArrayList<Integer>(1);
        Colors.add(res.getColor(R.color.hp_chart_room_temperature));
        result.setColors(Colors);

        return result;
    }

}
