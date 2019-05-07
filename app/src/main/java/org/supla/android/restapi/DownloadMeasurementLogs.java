package org.supla.android.restapi;

/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

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

    abstract protected long getMinTimestamp();
    abstract protected long getMaxTimestamp();
    abstract protected int getLocalTotalCount();
    abstract protected void EraseMeasurements(SQLiteDatabase db);
    abstract protected void SaveMeasurementItem(SQLiteDatabase db, long timestamp,
                                                JSONObject obj) throws JSONException;
    protected void noRemoteDataAvailable(SQLiteDatabase db) throws JSONException {}

    protected int itemsLimitPerRequest() {return 1000;}

    @Override
    protected Object doInBackground(Object[] objects) {

        if (getChannelId() <=0) {
            return null;
        }

        ApiRequestResult result = apiRequest("channels/"
                +Integer.toString(getChannelId())
                +"/measurement-logs?order=ASC"
                +"&limit=2&offset=0");

        if (result!=null && result.getCode() == 200) {
            boolean doErase = false;
            if (result.getTotalCount() == 0) {
                doErase = true;
            } else if (result.getJObj() instanceof JSONArray) {
                JSONArray arr = (JSONArray)result.getJObj();
                boolean found = false;
                long min = getMinTimestamp();
                for(int a=0;a<arr.length();a++) {
                    try {
                        JSONObject obj = arr.getJSONObject(a);
                        if (min == obj.getLong("date_timestamp")) {
                            found = true;
                            break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                doErase = !found;
            }

            if (doErase) {
                SQLiteDatabase db = getMeasurementsDbH().getWritableDatabase();
                EraseMeasurements(db);
                db.close();
            }
        }

        AfterTimestamp = getMaxTimestamp();
        int LocalTotalCount = getLocalTotalCount();
        Double percent = 0d;

        do {
            result = apiRequest("channels/"
                    +Integer.toString(getChannelId())
                    +"/measurement-logs?order=ASC"
                    +"&limit="+Integer.toString(itemsLimitPerRequest())
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


                        LocalTotalCount++;

                        if (result.getTotalCount() > 0) {
                            Double new_percent = LocalTotalCount*100d/result.getTotalCount();
                            if (new_percent - percent >= 1d) {
                                percent = new_percent;
                                publishProgress(percent);
                            }
                        }

                        keepAlive();
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
