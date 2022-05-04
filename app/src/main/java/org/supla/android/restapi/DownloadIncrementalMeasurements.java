package org.supla.android.restapi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import org.supla.android.SuplaApp;
import org.supla.android.profile.ProfileManager;
import org.supla.android.db.IncrementalMeasurementItem;

public abstract class DownloadIncrementalMeasurements extends DownloadMeasurementLogs {

    private IncrementalMeasurementItem older_item = null;
    private IncrementalMeasurementItem younger_item = null;
    private ProfileManager profileManager;

    public DownloadIncrementalMeasurements(Context context) {
        super(context);
        profileManager = SuplaApp.getApp().getProfileManager();
    }

    @Override
    protected long getMaxTimestampInitialOffset() {
        return -2;
    }

    protected abstract IncrementalMeasurementItem newObject();

    protected abstract IncrementalMeasurementItem newObject(IncrementalMeasurementItem src);

    protected abstract void addIncrementalMeasurement(SQLiteDatabase db,
                                                      IncrementalMeasurementItem item);

    protected void SaveMeasurementItem(SQLiteDatabase db,
                                       long timestamp, JSONObject obj) throws JSONException {
        int profileId = getCurrentProfileId();
        younger_item = newObject();
        younger_item.AssignJSONObject(obj);
        younger_item.setChannelId(getChannelId());
        younger_item.setProfileId(profileId);

        boolean correctDateOrder = older_item == null
                || younger_item.getTimestamp() > older_item.getTimestamp();

        if (older_item != null && correctDateOrder) {

            IncrementalMeasurementItem citem = newObject(younger_item);
            citem.setProfileId(profileId);

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
                    citem.setComplement(a < n - 1);
                    addIncrementalMeasurement(db, citem);
                    citem.setTimestamp(citem.getTimestamp() - 600);
                }

            } else {
                addIncrementalMeasurement(db, citem);
            }
        }

        if (correctDateOrder) {
            older_item = younger_item;
        }
    }
}
