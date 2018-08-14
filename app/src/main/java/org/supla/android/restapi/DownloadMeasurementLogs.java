package org.supla.android.restapi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.supla.android.Trace;
import org.supla.android.db.DbHelper;

public abstract class DownloadMeasurementLogs extends SuplaRestApiClientTask {

    private long BeforeTimestamp = 0;
    private long AfterTimestamp = 0;

    public DownloadMeasurementLogs(Context context) {
        super(context);
    }

    abstract protected long getMaxTimestamp();
    abstract protected void SaveMeasurementItem(SQLiteDatabase db, long timestamp, JSONObject obj) throws JSONException;
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
                    +"/measurement-logs?order=DESC&beforeTimestamp="
                    +Long.toString(BeforeTimestamp)
                    +"&afterTimestamp="
                    +Long.toString(AfterTimestamp));

            if (result==null || result.getCode() != 200) {
                return null;
            }

            if (!(result.getJObj() instanceof JSONArray)) {
                return null;
            }

            JSONArray arr = (JSONArray)result.getJObj();

            Trace.d(log_tag, "ArrayLength: "+Integer.toString(arr.length()));

            long t = System.currentTimeMillis();

            SQLiteDatabase db = getMeasurementsDbH().getWritableDatabase();
            try {

                Trace.d(log_tag, "begin transaction");
                db.beginTransaction();

                if (arr.length() <= 0) {
                    try {
                        noRemoteDataAvailable(db);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }
                    Trace.d(log_tag, "noRemoteDataAvailable");
                    break;
                }

                for(int a=0;a<arr.length();a++) {

                    try {
                        JSONObject obj = obj = arr.getJSONObject(a);
                        long timestamp = obj.getLong("date_timestamp");

                        if (timestamp <= 0) {
                            return null;
                        }

                        if (timestamp < BeforeTimestamp || BeforeTimestamp == 0) {
                            BeforeTimestamp = timestamp;
                        }

                        SaveMeasurementItem(db, timestamp, obj);


                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }

                    if (isCancelled()) {
                        break;
                    }
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
                Trace.d(log_tag, "end transaction");
            }

            Trace.d(log_tag, "TIME: "+Long.toString(System.currentTimeMillis()-t));

        } while (!isCancelled());


        return null;
    }
}
