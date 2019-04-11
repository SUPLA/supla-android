package org.supla.android.restapi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.supla.android.db.IncrementalMeasurementItem;

public class DownloadImpulseCounterMeasurements extends DownloadIncrementalMeasurements {

    public DownloadImpulseCounterMeasurements(Context context) {
        super(context);
    }

    protected long getMinTimestamp() {
        return getMeasurementsDbH().getElectricityMeterMeasurementTimestamp(getChannelId(), true);
    }

    protected long getMaxTimestamp() {
        return getMeasurementsDbH().getElectricityMeterMeasurementTimestamp(getChannelId(), false);
    }

    protected void EraseMeasurements(SQLiteDatabase db) {

    }

    protected IncrementalMeasurementItem newObject() {
       return null;
    }

    protected IncrementalMeasurementItem newObject(IncrementalMeasurementItem src) {
        return null;
    }

    protected IncrementalMeasurementItem getOlderUncalculatedIncrementalMeasurement(
            SQLiteDatabase db, int channelId, long timestamp) {
        return null;
    }

    protected void deleteUncalculatedIncrementalMeasurements(SQLiteDatabase db,
                                                             int channelId) {
    }
    protected void addIncrementalMeasurement(SQLiteDatabase db,
                                             IncrementalMeasurementItem emi) {

    }
}
