package org.supla.android.restapi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;
import org.supla.android.Trace;


public class DownloadElectricityMeterMeasurements extends DownloadMeasurementLogs {

    public DownloadElectricityMeterMeasurements(Context context) {
        super(context);
    }

    protected long getMaxTimestamp() {
        return getDbH().getElectricityMeterMaxTimestamp(getChannelId());
    }

    protected void SaveMeasurementItem(SQLiteDatabase db, long timestamp,
                                       JSONObject obj) throws JSONException {
        getDbH().addElectricityMeasurement(db, getChannelId(), timestamp,
                getLong(obj,"phase1_fae"),
                getLong(obj,"phase1_rae"),
                getLong(obj,"phase1_fre"),
                getLong(obj,"phase1_rre"),
                getLong(obj,"phase2_fae"),
                getLong(obj,"phase2_rae"),
                getLong(obj,"phase2_fre"),
                getLong(obj,"phase2_rre"),
                getLong(obj,"phase3_fae"),
                getLong(obj,"phase3_rae"),
                getLong(obj,"phase3_fre"),
                getLong(obj,"phase3_rre"));
    }

}
