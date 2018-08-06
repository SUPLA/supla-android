package org.supla.android.restapi;

import android.content.Context;

import org.json.JSONArray;
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

    protected void SaveMeasurementItem(long timestamp, JSONObject obj) throws JSONException {
        getDbH().addElectricityMeasurement(getChannelId(), timestamp,
                obj.getLong("phase1_fae"),
                obj.getLong("phase1_rae"),
                obj.getLong("phase1_fre"),
                obj.getLong("phase1_rre"),
                obj.getLong("phase2_fae"),
                obj.getLong("phase2_rae"),
                obj.getLong("phase2_fre"),
                obj.getLong("phase2_rre"),
                obj.getLong("phase3_fae"),
                obj.getLong("phase3_rae"),
                obj.getLong("phase3_fre"),
                obj.getLong("phase3_rre"));
    }
}
