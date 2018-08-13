package org.supla.android.db;

import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

public class ElectricityMeasurementItem extends DbItem {

    private int ChannelId;
    private long Timestamp;
    private double[] fae;
    private double[] rae;
    private double[] fre;
    private double[] rre;
    private boolean Calculated;

    public ElectricityMeasurementItem() {
        fae = new double[3];
        rae = new double[3];
        fre = new double[3];
        rre = new double[3];
    }

    public int getChannelId() {
        return ChannelId;
    }

    public void setChannelId(int channelId) {
        ChannelId = channelId;
    }

    public long getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(long timestamp) {
        Timestamp = timestamp;
    }

    public void setFae(int phase, double fae) {
        if (phase>=1 && phase<=3) {
            phase--;
            this.fae[phase] = fae;
        }
    }

    public double getFae(int phase) {
        if (phase>=1 && phase<=3) {
            phase--;
            return this.fae[phase];
        }
        return 0;
    }

    public void setRae(int phase, double rae) {
        if (phase>=1 && phase<=3) {
            phase--;
            this.rae[phase] = rae;
        }
    }

    public double getRae(int phase) {
        if (phase>=1 && phase<=3) {
            phase--;
            return this.rae[phase];
        }
        return 0;
    }

    public void setFre(int phase, double fre) {
        if (phase>=1 && phase<=3) {
            phase--;
            this.fre[phase] = fre;
        }
    }

    public double getFre(int phase) {
        if (phase>=1 && phase<=3) {
            phase--;
            return this.fre[phase];
        }
        return 0;
    }

    public void setRre(int phase, double rre) {
        if (phase>=1 && phase<=3) {
            phase--;
            this.rre[phase] = rre;
        }
    }

    public double getRre(int phase) {
        if (phase>=1 && phase<=3) {
            phase--;
            return this.rre[phase];
        }
        return 0;
    }

    public boolean isCalculated() {
        return Calculated;
    }

    private void putNullOrDouble(ContentValues values, String name, double value) {
        if (value == 0) {
            values.putNull(name);
        } else {
            values.put(name, value);
        }
    }

    protected long getLong(JSONObject obj, String name) throws JSONException {
        if (!obj.isNull(name)) {
            return obj.getLong(name);
        }
        return 0;
    }

    public void AssignJSONObject(JSONObject obj) throws JSONException {

        setTimestamp(obj.getLong("date_timestamp"));

        for(int phase=1;phase<=3;phase++) {
            setFae(phase, getLong(obj,"phase"+Integer.toString(phase)+"_fae") / 100000.00);
            setRae(phase, getLong(obj,"phase"+Integer.toString(phase)+"_rae") / 100000.00);
            setFre(phase, getLong(obj,"phase"+Integer.toString(phase)+"_fre") / 100000.00);
            setRre(phase, getLong(obj,"phase"+Integer.toString(phase)+"_rre") / 100000.00);
        }
    }

    public void AssignCursorData(Cursor cursor) {

        setId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ElectricityMeterLogEntry._ID)));
        setChannelId(cursor.getInt(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID)));

        setTimestamp(cursor.getLong(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP)));

        setFae(1, cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FAE)));

        setRae(1, cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RAE)));

        setFre(1, cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FRE)));

        setRre(1, cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RRE)));

        setFae(2, cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FAE)));

        setRae(2, cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RAE)));

        setFre(2, cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FRE)));

        setRre(2, cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RRE)));

        setFae(3, cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FAE)));

        setRae(3, cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RAE)));

        setFre(3, cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FRE)));

        setRre(3, cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RRE)));

        Calculated = cursor.getInt(cursor.getColumnIndex(
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED)) > 0;

    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID, getChannelId());
        values.put(SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP, getTimestamp());

        putNullOrDouble(values,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FAE, getFae(1));
        putNullOrDouble(values,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RAE, getRae(1));
        putNullOrDouble(values,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FRE, getFre(1));
        putNullOrDouble(values,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RRE, getRre(1));

        putNullOrDouble(values,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FAE, getFae(2));
        putNullOrDouble(values,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RAE, getRae(2));
        putNullOrDouble(values,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FRE, getFre(2));
        putNullOrDouble(values,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RRE, getRre(2));

        putNullOrDouble(values,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FAE, getFae(3));
        putNullOrDouble(values,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RAE, getRae(3));
        putNullOrDouble(values,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FRE, getFre(3));
        putNullOrDouble(values,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RRE, getRre(3));

        values.put(SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED,
                isCalculated() ? 1 : 0);

        return values;
    }

    public void Calculate(ElectricityMeasurementItem emi) {
        if (emi.getTimestamp() >= getTimestamp() || Calculated) return;

        for(int phase = 1; phase <= 3; phase++) {
            setFae(phase, getFae(phase) - emi.getFae(phase));
            setRae(phase, getRae(phase) - emi.getRae(phase));
            setFre(phase, getFre(phase) - emi.getFre(phase));
            setRre(phase, getRre(phase) - emi.getRre(phase));
        }

        Calculated = true;
    }
}
