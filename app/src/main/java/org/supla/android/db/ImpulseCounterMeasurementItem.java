package org.supla.android.db;

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
import android.content.ContentValues;
import android.database.Cursor;
import org.json.JSONException;
import org.json.JSONObject;
import org.supla.android.data.source.local.entity.measurements.ImpulseCounterLogEntity;

public class ImpulseCounterMeasurementItem extends IncrementalMeasurementItem {

  private long counter;
  private double calculatedValue;

  public ImpulseCounterMeasurementItem() {
    super();
    counter = 0;
    calculatedValue = 0.0;
  }

  public ImpulseCounterMeasurementItem(ImpulseCounterMeasurementItem src) {
    super(src);
    counter = src.counter;
    calculatedValue = src.calculatedValue;
  }

  public long getCounter() {
    return counter;
  }

  public void setCounter(long counter) {
    this.counter = counter;
  }

  public double getCalculatedValue() {
    return calculatedValue;
  }

  public void setCalculatedValue(double calculatedValue) {
    this.calculatedValue = calculatedValue;
  }

  public void AssignJSONObject(JSONObject obj) throws JSONException {
    setTimestamp(obj.getLong("date_timestamp"));
    setCounter(getLong(obj, "counter"));
    setCalculatedValue(getDouble(obj, "calculated_value"));
  }

  @SuppressLint("Range")
  public void AssignCursorData(Cursor cursor) {

    setId(cursor.getLong(cursor.getColumnIndex(ImpulseCounterLogEntity.COLUMN_ID)));
    setChannelId(cursor.getInt(cursor.getColumnIndex(ImpulseCounterLogEntity.COLUMN_CHANNEL_ID)));

    setTimestamp(cursor.getLong(cursor.getColumnIndex(ImpulseCounterLogEntity.COLUMN_TIMESTAMP)));

    setCounter(cursor.getLong(cursor.getColumnIndex(ImpulseCounterLogEntity.COLUMN_COUNTER)));

    setCalculatedValue(
        cursor.getDouble(cursor.getColumnIndex(ImpulseCounterLogEntity.COLUMN_CALCULATED_VALUE)));

    Complement =
        cursor.getInt(cursor.getColumnIndex(ImpulseCounterLogEntity.COLUMN_MANUALLY_COMPLEMENTED))
            > 0;
    setProfileId(cursor.getLong(cursor.getColumnIndex(ImpulseCounterLogEntity.COLUMN_PROFILE_ID)));
  }

  public ContentValues getContentValues() {

    ContentValues values = new ContentValues();

    values.put(ImpulseCounterLogEntity.COLUMN_CHANNEL_ID, getChannelId());
    values.put(ImpulseCounterLogEntity.COLUMN_TIMESTAMP, getTimestamp());
    values.put(ImpulseCounterLogEntity.COLUMN_COUNTER, getCounter());
    values.put(ImpulseCounterLogEntity.COLUMN_CALCULATED_VALUE, getCalculatedValue());
    values.put(ImpulseCounterLogEntity.COLUMN_MANUALLY_COMPLEMENTED, isComplement() ? 1 : 0);
    values.put(ImpulseCounterLogEntity.COLUMN_PROFILE_ID, getProfileId());
    return values;
  }

  public void Calculate(IncrementalMeasurementItem item) {
    ImpulseCounterMeasurementItem ic = (ImpulseCounterMeasurementItem) item;

    long diff = getCounter() - ic.getCounter();
    if (diff >= 0 || Math.abs(diff) <= ic.getCounter() * 0.1) {
      // Use increment values only in case no reset detected
      setCalculatedValue(calculateValue(getCalculatedValue(), ic.getCalculatedValue()));
      setCounter(calculateValue(getCounter(), ic.getCounter()));
    }

    Calculated = true;
  }

  public void DivideBy(long div) {
    setCalculatedValue(getCalculatedValue() / div);
    setCounter(getCounter() / div);

    Divided = true;
  }
}
