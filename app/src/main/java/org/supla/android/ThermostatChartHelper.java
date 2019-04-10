package org.supla.android;

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

import org.supla.android.db.DbHelper;
import org.supla.android.db.SuplaContract;
import java.util.ArrayList;
import java.util.List;

public class ThermostatChartHelper extends ChartHelper {

    protected Cursor getCursor(DbHelper DBH,
                               SQLiteDatabase db, int channelId, String dateFormat) {
        return DBH.getThermostatMeasurements(db, channelId, dateFormat);
    }

    protected float[] getValues(Cursor c) {
        float[] phases = new float[1];
        phases[0] = (float) c.getDouble(
                c.getColumnIndex(
                        SuplaContract.ThermostatLogEntry.COLUMN_NAME_MEASUREDTEMPERATURE));

        return phases;
    }

    protected long getTimestamp(Cursor c) {
        return c.getLong(c.getColumnIndex(
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP));
    }

    protected String[] getStackLabels() {
        Resources res = context.getResources();

        return new String[]{
                res.getString(R.string.hp_room_temperature)};
    }

    protected List<Integer> getColors() {
        Resources res = context.getResources();

        List<Integer> Colors = new ArrayList<Integer>(1);
        Colors.add(res.getColor(R.color.hp_chart_room_temperature));

        return Colors;
    }

    public ThermostatChartHelper(Context context) {
        super(context);
    }


    public void loadThermostatMeasurements(int channelId) {
        loadBarChart(channelId, ChartType.Bar_Minutely);
    }
}
