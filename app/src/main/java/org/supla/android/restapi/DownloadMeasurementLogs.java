package org.supla.android.restapi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.supla.android.Trace;
import org.supla.android.db.DbHelper;

public abstract class DownloadMeasurementLogs extends SuplaRestApiClientTask {

    private long AfterTimestamp = 0;

    public DownloadMeasurementLogs(Context context) {
        super(context);
    }

    abstract protected long getMaxTimestamp();
    abstract protected void SaveMeasurementItem(SQLiteDatabase db, long timestamp,
                                                JSONObject obj) throws JSONException;
    protected void noRemoteDataAvailable(SQLiteDatabase db) throws JSONException {};

    @Override
    protected Object doInBackground(Object[] objects) {

        if (getChannelId() <=0) {
            return null;
        }

        AfterTimestamp = getMaxTimestamp();

        do {
            ApiRequestResult result = apiRequest("channels/"
                    +Integer.toString(getChannelId())
                    +"/measurement-logs?order=ASC"
                    +"&afterTimestamp="
                    +Long.toString(AfterTimestamp));

            if (result==null || result.getCode() != 200) {
                return null;
            }

            if (!(result.getJObj() instanceof JSONArray)) {
                return null;
            }

            JSONArray arr = (JSONArray)result.getJObj();

            long t = System.currentTimeMillis();

            SQLiteDatabase db = getMeasurementsDbH().getWritableDatabase();
            try {

                db.beginTransaction();
                try {

                    if (arr.length() <= 0) {
                        noRemoteDataAvailable(db);
                        db.setTransactionSuccessful();
                        break;
                    }

                    for(int a=0;a<arr.length();a++) {

                        JSONObject obj = obj = arr.getJSONObject(a);
                        long timestamp = obj.getLong("date_timestamp");

                        if (timestamp <= 0) {
                            return null;
                        }

                        if (timestamp > AfterTimestamp) {
                            AfterTimestamp = timestamp;
                        }

                        SaveMeasurementItem(db, timestamp, obj);

                        if (isCancelled()) {
                            break;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }

            Trace.d(log_tag, "TIME: "+Long.toString(System.currentTimeMillis()-t));

        } while (!isCancelled());


        return null;
    }
}
