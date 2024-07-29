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
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity;

public class ElectricityMeasurementItem extends IncrementalMeasurementItem {

  private final double[] fae;
  private final double[] rae;
  private final double[] fre;
  private final double[] rre;
  private double faeBalanced;
  private double raeBalanced;

  public ElectricityMeasurementItem() {
    super();
    fae = new double[3];
    rae = new double[3];
    fre = new double[3];
    rre = new double[3];
  }

  public ElectricityMeasurementItem(ElectricityMeasurementItem emi) {
    super(emi);
    fae = emi.fae.clone();
    rae = emi.rae.clone();
    fre = emi.fre.clone();
    rre = emi.rre.clone();
    faeBalanced = emi.faeBalanced;
    raeBalanced = emi.raeBalanced;
  }

  public void setFae(int phase, double fae) {
    if (phase >= 1 && phase <= 3) {
      phase--;
      this.fae[phase] = fae;
    }
  }

  public double getFae(int phase) {
    if (phase >= 1 && phase <= 3) {
      phase--;
      return this.fae[phase];
    }
    return 0;
  }

  public void setRae(int phase, double rae) {
    if (phase >= 1 && phase <= 3) {
      phase--;
      this.rae[phase] = rae;
    }
  }

  public double getRae(int phase) {
    if (phase >= 1 && phase <= 3) {
      phase--;
      return this.rae[phase];
    }
    return 0;
  }

  public void setFre(int phase, double fre) {
    if (phase >= 1 && phase <= 3) {
      phase--;
      this.fre[phase] = fre;
    }
  }

  public double getFre(int phase) {
    if (phase >= 1 && phase <= 3) {
      phase--;
      return this.fre[phase];
    }
    return 0;
  }

  public void setRre(int phase, double rre) {
    if (phase >= 1 && phase <= 3) {
      phase--;
      this.rre[phase] = rre;
    }
  }

  public double getRre(int phase) {
    if (phase >= 1 && phase <= 3) {
      phase--;
      return this.rre[phase];
    }
    return 0;
  }

  public double getFaeBalanced() {
    return faeBalanced;
  }

  public void setFaeBalanced(double faeBalanced) {
    this.faeBalanced = faeBalanced;
  }

  public double getRaeBalanced() {
    return raeBalanced;
  }

  public void setRaeBalanced(double raeBalanced) {
    this.raeBalanced = raeBalanced;
  }

  public void AssignJSONObject(JSONObject obj) throws JSONException {

    setTimestamp(obj.getLong("date_timestamp"));

    for (int phase = 1; phase <= 3; phase++) {
      setFae(phase, getLong(obj, "phase" + phase + "_fae") / 100000.00);
      setRae(phase, getLong(obj, "phase" + phase + "_rae") / 100000.00);
      setFre(phase, getLong(obj, "phase" + phase + "_fre") / 100000.00);
      setRre(phase, getLong(obj, "phase" + phase + "_rre") / 100000.00);
    }

    setFaeBalanced(getLong(obj, "fae_balanced") / 100000.00);
    setRaeBalanced(getLong(obj, "rae_balanced") / 100000.00);
  }

  @SuppressLint("Range")
  public void AssignCursorData(Cursor cursor) {

    setId(cursor.getLong(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_ID)));
    setChannelId(cursor.getInt(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_CHANNEL_ID)));

    setTimestamp(cursor.getLong(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_TIMESTAMP)));

    setFae(1, cursor.getDouble(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_PHASE1_FAE)));

    setRae(1, cursor.getDouble(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_PHASE1_RAE)));

    setFre(1, cursor.getDouble(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_PHASE1_FRE)));

    setRre(1, cursor.getDouble(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_PHASE1_RRE)));

    setFae(2, cursor.getDouble(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_PHASE2_FAE)));

    setRae(2, cursor.getDouble(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_PHASE2_RAE)));

    setFre(2, cursor.getDouble(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_PHASE2_FRE)));

    setRre(2, cursor.getDouble(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_PHASE2_RRE)));

    setFae(3, cursor.getDouble(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_PHASE3_FAE)));

    setRae(3, cursor.getDouble(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_PHASE3_RAE)));

    setFre(3, cursor.getDouble(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_PHASE3_FRE)));

    setRre(3, cursor.getDouble(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_PHASE3_RRE)));

    setFaeBalanced(
        cursor.getDouble(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_FAE_BALANCED)));

    setRaeBalanced(
        cursor.getDouble(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_RAE_BALANCED)));

    Complement =
        cursor.getInt(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_MANUALLY_COMPLEMENTED))
            > 0;

    setProfileId(
        cursor.getLong(cursor.getColumnIndex(ElectricityMeterLogEntity.COLUMN_PROFILE_ID)));
  }

  public ContentValues getContentValues() {

    ContentValues values = new ContentValues();

    values.put(ElectricityMeterLogEntity.COLUMN_CHANNEL_ID, getChannelId());
    values.put(ElectricityMeterLogEntity.COLUMN_TIMESTAMP, getTimestamp());

    putNullOrDouble(values, ElectricityMeterLogEntity.COLUMN_PHASE1_FAE, getFae(1));
    putNullOrDouble(values, ElectricityMeterLogEntity.COLUMN_PHASE1_RAE, getRae(1));
    putNullOrDouble(values, ElectricityMeterLogEntity.COLUMN_PHASE1_FRE, getFre(1));
    putNullOrDouble(values, ElectricityMeterLogEntity.COLUMN_PHASE1_RRE, getRre(1));

    putNullOrDouble(values, ElectricityMeterLogEntity.COLUMN_PHASE2_FAE, getFae(2));
    putNullOrDouble(values, ElectricityMeterLogEntity.COLUMN_PHASE2_RAE, getRae(2));
    putNullOrDouble(values, ElectricityMeterLogEntity.COLUMN_PHASE2_FRE, getFre(2));
    putNullOrDouble(values, ElectricityMeterLogEntity.COLUMN_PHASE2_RRE, getRre(2));

    putNullOrDouble(values, ElectricityMeterLogEntity.COLUMN_PHASE3_FAE, getFae(3));
    putNullOrDouble(values, ElectricityMeterLogEntity.COLUMN_PHASE3_RAE, getRae(3));
    putNullOrDouble(values, ElectricityMeterLogEntity.COLUMN_PHASE3_FRE, getFre(3));
    putNullOrDouble(values, ElectricityMeterLogEntity.COLUMN_PHASE3_RRE, getRre(3));

    putNullOrDouble(values, ElectricityMeterLogEntity.COLUMN_FAE_BALANCED, getFaeBalanced());

    putNullOrDouble(values, ElectricityMeterLogEntity.COLUMN_RAE_BALANCED, getRaeBalanced());

    values.put(ElectricityMeterLogEntity.COLUMN_MANUALLY_COMPLEMENTED, isComplement() ? 1 : 0);
    values.put(ElectricityMeterLogEntity.COLUMN_PROFILE_ID, getProfileId());

    return values;
  }

  public void Calculate(IncrementalMeasurementItem item) {
    ElectricityMeasurementItem emi = (ElectricityMeasurementItem) item;

    for (int phase = 1; phase <= 3; phase++) {

      setFae(phase, calculateValue(getFae(phase), emi.getFae(phase)));
      setRae(phase, calculateValue(getRae(phase), emi.getRae(phase)));
      setFre(phase, calculateValue(getFre(phase), emi.getFre(phase)));
      setRre(phase, calculateValue(getRre(phase), emi.getRre(phase)));
    }

    setFaeBalanced(calculateValue(getFaeBalanced(), emi.getFaeBalanced()));
    setRaeBalanced(calculateValue(getRaeBalanced(), emi.getRaeBalanced()));

    Calculated = true;
  }

  public void DivideBy(long div) {
    for (int phase = 1; phase <= 3; phase++) {
      setFae(phase, getFae(phase) / div);
      setRae(phase, getRae(phase) / div);
      setFre(phase, getFre(phase) / div);
      setRre(phase, getRre(phase) / div);
    }

    setFaeBalanced(getFaeBalanced() / div);
    setRaeBalanced(getRaeBalanced() / div);

    Divided = true;
  }
}
