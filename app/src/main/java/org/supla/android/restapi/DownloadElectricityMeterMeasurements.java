package org.supla.android.restapi;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.supla.android.Trace;

public class DownloadElectricityMeterMeasurements extends SuplaRestApiClientTask {

    private long BeforeTimestamp = 0;
    private long AfterTimestamp = 0;

    public DownloadElectricityMeterMeasurements(Context context) {
        super(context);
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        if (getChannelId() <=0) {
            return null;
        }

        AfterTimestamp = getDbH().getElectricityMeterMaxTimestamp(getChannelId());

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

                } catch (JSONException e) {
                    return null;
                }
            }

        } while (!isCancelled());


        return null;
    }
}
