package org.supla.android.restapi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;
import org.supla.android.db.IncrementalMeasurementItem;

public abstract class DownloadIncrementalMeasurements extends DownloadMeasurementLogs {

    private IncrementalMeasurementItem older_item = null;
    private IncrementalMeasurementItem younger_item = null;
    private boolean added = false;

    public DownloadIncrementalMeasurements(Context context) {
        super(context);
    }

    protected abstract IncrementalMeasurementItem newObject();

    protected abstract IncrementalMeasurementItem newObject(IncrementalMeasurementItem src);

    protected abstract IncrementalMeasurementItem getOlderUncalculatedIncrementalMeasurement(
            SQLiteDatabase db, int channelId, long timestamp);

    protected abstract void deleteUncalculatedIncrementalMeasurements(SQLiteDatabase db,
                                                                      int channelId);

    protected abstract void addIncrementalMeasurement(SQLiteDatabase db,
                                                      IncrementalMeasurementItem item);

    protected void SaveMeasurementItem(SQLiteDatabase db,
                                       long timestamp, JSONObject obj) throws JSONException {

        younger_item = newObject();
        younger_item.AssignJSONObject(obj);
        younger_item.setChannelId(getChannelId());

        if (older_item == null) {
            older_item = getOlderUncalculatedIncrementalMeasurement(db,
                    younger_item.getChannelId(),
                    younger_item.getTimestamp());

            if (older_item != null) {
                deleteUncalculatedIncrementalMeasurements(db, younger_item.getChannelId());
            }
        }

        boolean correctDateOrder = older_item == null
                || younger_item.getTimestamp() > older_item.getTimestamp();

        if (older_item != null && correctDateOrder) {

            IncrementalMeasurementItem citem = newObject(younger_item);

            if (!citem.isCalculated()) {
                citem.Calculate(older_item);
            }

            long diff = citem.getTimestamp() - older_item.getTimestamp();

            if (diff >= 1200) {

                long n = diff / 600;
                if (!citem.isDivided()) {
                    citem.DivideBy(n);
                }

                for (int a = 0; a < n; a++) {
                    if (a < n - 1) {
                        citem.setComplement(true);
                    }
                    addIncrementalMeasurement(db, citem);
                    citem.setTimestamp(citem.getTimestamp() - 600);
                }

            } else {
                addIncrementalMeasurement(db, citem);
            }

            added = true;
        }

        if (correctDateOrder) {
            older_item = younger_item;
        }
    }

    protected void onFirstItem(SQLiteDatabase db) throws JSONException {
        super.onFirstItem(db);
        added = false;
        older_item = null;
    }

    protected void onLastItem(SQLiteDatabase db) throws JSONException {
        super.onLastItem(db);
        if (older_item != null
                && added) {
            addIncrementalMeasurement(db, older_item);
            older_item = null;
        }
    }

}
