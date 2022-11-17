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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.supla.android.R;
import org.supla.android.SuplaApp;
import org.supla.android.ValuesFormatterProvider;
import org.supla.android.data.ValuesFormatter;
import org.supla.android.db.MeasurementsDbHelper;
import org.supla.android.db.SuplaContract;

public class TemperatureChartHelper extends ChartHelper {

  private ValuesFormatterProvider valuesFormatterProvider;

  public TemperatureChartHelper(Context context) {
    super(context);
    valuesFormatterProvider = SuplaApp.getApp();
  }

  @Override
  protected Cursor getCursor(MeasurementsDbHelper DBH, int channelId, String dateFormat) {
    return DBH.getTemperatureMeasurements(channelId, dateFrom, dateTo);
  }

  @Override
  protected void addBarEntries(int n, float time, Cursor c, ArrayList<BarEntry> entries) {}

  @Override
  protected void addLineEntries(int n, Cursor c, float time, ArrayList<Entry> entries) {
    if (entries.size() > 0 && time - entries.get(entries.size() - 1).getX() > 1.5f) {
      entries = newLineEntries();
    }

    entries.add(new Entry(time, getTemperature(c)));
  }

  @Override
  protected void addPieEntries(SimpleDateFormat spf, Cursor c, ArrayList<PieEntry> entries) {}

  @SuppressLint("Range")
  protected float getTemperature(Cursor c) {
    ValuesFormatter tp = valuesFormatterProvider.getValuesFormatter();
    return (float)
        tp.getTemperatureInConfiguredUnit(
            c.getDouble(
                c.getColumnIndex(SuplaContract.TemperatureLogEntry.COLUMN_NAME_TEMPERATURE)));
  }

  @Override
  @SuppressLint("Range")
  protected long getTimestamp(Cursor c) {
    return c.getLong(c.getColumnIndex(SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP));
  }

  @Override
  protected LineDataSet newLineDataSetInstance(ArrayList<Entry> lineEntries, String label) {
    LineDataSet result = super.newLineDataSetInstance(lineEntries, label);
    Resources res = context.getResources();
    result.setFillColor(res.getColor(R.color.th_temperature_fill_color));
    result.setColor(res.getColor(R.color.th_temperature_line_color));
    return result;
  }

  @Override
  protected IMarker getMarker() {
    return new SuplaMarkerView(this, context, R.layout.chart_marker);
  }

  @Override
  public void moveToEnd() {
    super.moveToEnd();
    combinedChart.fitScreen();
  }
}
