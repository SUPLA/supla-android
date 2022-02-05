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

    protected void eraseMeasurements() {
        getMeasurementsDbH().deleteImpulseCounterMeasurements(getChannelId());
    }

    protected IncrementalMeasurementItem newObject() {
        return new ImpulseCounterMeasurementItem();
    }

    protected IncrementalMeasurementItem newObject(IncrementalMeasurementItem src) {
        return new ImpulseCounterMeasurementItem((ImpulseCounterMeasurementItem) src);
    }

    protected void addIncrementalMeasurement(SQLiteDatabase db,
                                             IncrementalMeasurementItem item) {
        getMeasurementsDbH().addImpulseCounterMeasurement((ImpulseCounterMeasurementItem) item);
    }
}
