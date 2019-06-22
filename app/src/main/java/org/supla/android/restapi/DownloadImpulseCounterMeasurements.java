package org.supla.android.restapi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import org.supla.android.db.ImpulseCounterMeasurementItem;
import org.supla.android.db.IncrementalMeasurementItem;

public class DownloadImpulseCounterMeasurements extends DownloadIncrementalMeasurements {

    public DownloadImpulseCounterMeasurements(Context context) {
        super(context);
    }

    protected long getMinTimestamp() {
        return getMeasurementsDbH().getImpulseCounterMeasurementTimestamp(getChannelId(), true);
    }

    protected long getMaxTimestamp() {
        return getMeasurementsDbH().getImpulseCounterMeasurementTimestamp(getChannelId(), false);
    }

    @Override
    protected int getLocalTotalCount() {
        return getMeasurementsDbH().getImpulseCounterMeasurementTotalCount(getChannelId(),
                true);
    }

    protected void EraseMeasurements(SQLiteDatabase db) {
        getMeasurementsDbH().deleteImpulseCounterMeasurements(db, getChannelId());
    }

    protected IncrementalMeasurementItem newObject() {
        return new ImpulseCounterMeasurementItem();
    }

    protected IncrementalMeasurementItem newObject(IncrementalMeasurementItem src) {
        return new ImpulseCounterMeasurementItem((ImpulseCounterMeasurementItem)src);
    }

    protected IncrementalMeasurementItem getOlderUncalculatedIncrementalMeasurement(
            SQLiteDatabase db, int channelId, long timestamp) {
        return getMeasurementsDbH().getOlderUncalculatedImpulseCounterMeasurement(db,
                channelId,
                timestamp);
    }

    protected void deleteUncalculatedIncrementalMeasurements(SQLiteDatabase db,
                                                             int channelId) {
        getMeasurementsDbH().deleteUncalculatedImpulseCounterMeasurements(db,
                channelId);
    }

    protected void addIncrementalMeasurement(SQLiteDatabase db,
                                             IncrementalMeasurementItem item) {
        getMeasurementsDbH().addImpulseCounterMeasurement(db, (ImpulseCounterMeasurementItem) item);
    }
}
