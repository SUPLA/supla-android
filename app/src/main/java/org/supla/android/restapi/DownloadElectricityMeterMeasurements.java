package org.supla.android.restapi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;
import org.supla.android.Trace;
import org.supla.android.db.ElectricityMeasurementItem;


public class DownloadElectricityMeterMeasurements extends DownloadMeasurementLogs {

    ElectricityMeasurementItem younger_emi;

    public DownloadElectricityMeterMeasurements(Context context) {
        super(context);
    }

    protected long getMaxTimestamp() {
        return getMeasurementsDbH().getElectricityMeterMaxTimestamp(getChannelId());
    }

    protected void SaveMeasurementItem(SQLiteDatabase db, long timestamp,
                                       JSONObject obj) throws JSONException {

        ElectricityMeasurementItem emi = new ElectricityMeasurementItem();
        emi.AssignJSONObject(obj);
        emi.setChannelId(getChannelId());


        if (younger_emi!=null) {
            if (emi.getTimestamp() >= younger_emi.getTimestamp()) {
                throw new JSONException("Wrong timestamp order!");
            }

            if (!younger_emi.isCalculated()) {
                younger_emi.Calculate(emi);
                if (younger_emi.isCalculated()) {
                    getMeasurementsDbH().addElectricityMeasurement(db, younger_emi);
                }
            }
        }

        younger_emi = emi;
    }

}
