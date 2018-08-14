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

    protected void SaveMeasurementItem(SQLiteDatabase db, ElectricityMeasurementItem emi) throws JSONException {


        if (younger_emi!=null) {
            if (emi.getTimestamp() >= younger_emi.getTimestamp()) {
                throw new JSONException("Wrong timestamp order!");
            }

            if (!younger_emi.isCalculated()) {
                younger_emi.Calculate(emi);
                if (younger_emi.isCalculated()) {

                    long diff = younger_emi.getTimestamp() - emi.getTimestamp();

                    if (diff >= 1200 ) {

                        long n = diff / 600;
                        younger_emi.DivideBy(n);

                        for(int a=0;a<n;a++) {
                            getMeasurementsDbH().addElectricityMeasurement(db, younger_emi);
                            younger_emi.setTimestamp(younger_emi.getTimestamp()-600);
                        }

                        Trace.d(log_tag, "Difference: "+Long.toString(younger_emi.getTimestamp() - emi.getTimestamp()));
                    } else {
                        getMeasurementsDbH().addElectricityMeasurement(db, younger_emi);
                    }

                }
            }
        }

        if (younger_emi == null && !emi.isCalculated()) {
            getMeasurementsDbH().addElectricityMeasurement(db, emi);
        }

        younger_emi = emi;
    }

    protected void SaveMeasurementItem(SQLiteDatabase db, long timestamp, JSONObject obj) throws JSONException {
        ElectricityMeasurementItem emi = new ElectricityMeasurementItem();
        emi.AssignJSONObject(obj);
        emi.setChannelId(getChannelId());

        SaveMeasurementItem(db, emi);
    }

    protected void noRemoteDataAvailable(SQLiteDatabase db) throws JSONException {
        super.noRemoteDataAvailable(db);

        if (younger_emi!=null) {
            ElectricityMeasurementItem emi =
                    getMeasurementsDbH().getOlderUncalculatedElectricityMeasurement(db,
                    getChannelId(),
                    younger_emi.getTimestamp());

            if (emi!=null) {
                Trace.d(log_tag, "!!getOlderElectricityMeasurement!!");
                SaveMeasurementItem(db, emi);
                getMeasurementsDbH().deleteElectricityMeasurementItem(db, emi.getId());
            }
        }
    }

}
