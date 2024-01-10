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

    setId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ImpulseCounterLogEntry._ID)));
    setChannelId(
        cursor.getInt(
            cursor.getColumnIndex(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID)));

    setTimestamp(
        cursor.getLong(
            cursor.getColumnIndex(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP)));

    setCounter(
        cursor.getLong(
            cursor.getColumnIndex(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COUNTER)));

    setCalculatedValue(
        cursor.getDouble(
            cursor.getColumnIndex(
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CALCULATEDVALUE)));

    Complement =
        cursor.getInt(
                cursor.getColumnIndex(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT))
            > 0;
    setProfileId(
        cursor.getLong(
            cursor.getColumnIndex(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_PROFILEID)));
  }

  public ContentValues getContentValues() {

    ContentValues values = new ContentValues();

    values.put(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID, getChannelId());
    values.put(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP, getTimestamp());
    values.put(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COUNTER, getCounter());
    values.put(
        SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CALCULATEDVALUE, getCalculatedValue());
    values.put(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT, isComplement() ? 1 : 0);
    values.put(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_PROFILEID, getProfileId());
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
