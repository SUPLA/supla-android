package org.supla.android.restapi;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.supla.android.Trace;

public abstract class DownloadMeasurementLogs extends SuplaRestApiClientTask {

    private long BeforeTimestamp = 0;
    private long AfterTimestamp = 0;

    public DownloadMeasurementLogs(Context context) {
        super(context);
    }

    abstract protected long getMaxTimestamp();
    abstract protected void SaveMeasurementItem(long timestamp, JSONObject obj) throws JSONException;

    @Override
    protected Object doInBackground(Object[] objects) {

        if (getChannelId() <=0) {
            return null;
        }

        AfterTimestamp = getMaxTimestamp();

        do {
            ApiRequestResult result = apiRequest("channels/"
                    +Integer.toString(getChannelId())
                    +"/measurement-logs?beforeTimestamp="
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

            if (arr.length() == 0) {
                return null;
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

                    SaveMeasurementItem(timestamp, obj);

                } catch (JSONException e) {
                    return null;
                }

                if (isCancelled()) {
                    break;
                }
            }

        } while (!isCancelled());


        return null;
    }
}
