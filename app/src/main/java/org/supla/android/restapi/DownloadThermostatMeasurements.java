package org.supla.android.restapi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;
import org.supla.android.Trace;
import org.supla.android.db.ElectricityMeasurementItem;
import org.supla.android.db.ThermostatMeasurementItem;

public class DownloadThermostatMeasurements extends DownloadMeasurementLogs {

    public DownloadThermostatMeasurements(Context context) {
        super(context);
    }

    protected long getMinTimestamp() {
        return getMeasurementsDbH().getThermostatMeasurementTimestamp(getChannelId(), true);
    }

    protected long getMaxTimestamp() {
        return getMeasurementsDbH().getThermostatMeasurementTimestamp(getChannelId(), false);
    }

    protected void EraseMeasurements(SQLiteDatabase db) {
        getMeasurementsDbH().deleteThermostatMeasurements(db, getChannelId());
    }

    protected void SaveMeasurementItem(SQLiteDatabase db,
                                       long timestamp, JSONObject obj) throws JSONException {

        ThermostatMeasurementItem thi  = new ThermostatMeasurementItem();
        thi.AssignJSONObject(obj);
        thi.setChannelId(getChannelId());

        getMeasurementsDbH().addThermostatMeasurement(db, thi);

        Trace.d("DOWNLOAD", Double.toString(thi.getMeasuredTemperature()));
    }

}
