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

public abstract class DownloadMeasurementLogs extends SuplaRestApiClientTask {

  private long AfterTimestamp = 0;
  private final long _profileId;

  public DownloadMeasurementLogs(Context context, int profileId) {
    super(context);
    _profileId = profileId;
  }

  protected abstract long getMinTimestamp();

  protected abstract long getMaxTimestamp();

  protected abstract int getLocalTotalCount();

  protected abstract void eraseMeasurements();

  protected abstract void SaveMeasurementItem(SQLiteDatabase db, long timestamp, JSONObject obj)
      throws JSONException;

  protected long getMaxTimestampInitialOffset() {
    return 1;
  }

  protected void onFirstItem(SQLiteDatabase db) throws JSONException {}

  protected void onLastItem(SQLiteDatabase db) throws JSONException {}

  protected int itemsLimitPerRequest() {
    return 10000;
  }

  protected long getCurrentProfileId() {
    return _profileId;
  }

  @Override
  protected Object doInBackground(Object[] objects) {

    if (getChannelId() <= 0) {
      return null;
    }

    ApiRequestResult result =
        apiRequest(
            "channels/" + getChannelId() + "/measurement-logs?order=ASC" + "&limit=2&offset=0");

    if (result != null && result.getCode() == 200) {
      boolean doErase = false;
      if (result.getTotalCount() == 0) {
        doErase = true;
      } else if (result.getJObj() instanceof JSONArray arr) {
        boolean found = false;
        long min = getMinTimestamp();
        for (int a = 0; a < arr.length(); a++) {
          try {
            JSONObject obj = arr.getJSONObject(a);
            if (Math.abs(min - obj.getLong("date_timestamp")) < 1800) {
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
        eraseMeasurements();
      }
    }

    AfterTimestamp = getMaxTimestamp() + getMaxTimestampInitialOffset();
    if (AfterTimestamp <= 0) {
      AfterTimestamp = 1;
    }

    int LocalTotalCount = getLocalTotalCount();
    Double percent = 0d;

    do {
      result =
          apiRequest(
              "channels/"
                  + getChannelId()
                  + "/measurement-logs?order=ASC"
                  + "&limit="
                  + itemsLimitPerRequest()
                  + "&afterTimestamp="
                  + AfterTimestamp);

      if (result == null || result.getCode() != 200) {
        return null;
      }

      if (!(result.getJObj() instanceof JSONArray arr)) {
        return null;
      }

      if (arr == null || arr.length() <= 0) {
        break;
      }

      long t = System.currentTimeMillis();

      SQLiteDatabase db = getMeasurementsDbH().getWritableDatabase();
      try {

        db.beginTransaction();
        try {

          for (int a = 0; a < arr.length(); a++) {

            JSONObject obj = obj = arr.getJSONObject(a);
            long timestamp = obj.getLong("date_timestamp");

            if (timestamp <= 0) {
              return null;
            }

            if (timestamp > AfterTimestamp) {
              AfterTimestamp = timestamp;
            }

            if (a == 0) {
              onFirstItem(db);
            }

            SaveMeasurementItem(db, timestamp, obj);

            if (a == arr.length() - 1) {
              onLastItem(db);
            }

            if (isCancelled()) {
              break;
            }

            LocalTotalCount++;

            if (result.getTotalCount() > 0) {
              Double new_percent = LocalTotalCount * 100d / result.getTotalCount();
              if (new_percent - percent >= 1d) {
                percent = new_percent;
                publishProgress(percent);
                // Trace.d(log_tag, "PERCENT: "+Double.toString(percent)
                // + " L:"+Integer.toString(LocalTotalCount)
                // + " R:"+Integer.toString(result.getTotalCount()));
              }
            }

            keepAlive();
          }

        } catch (JSONException e) {
          Trace.e("Supla", "download error", e);
          return null;
        }

        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }

      Trace.d(log_tag, "TIME: " + (System.currentTimeMillis() - t));

    } while (!isCancelled());

    return null;
  }
}
