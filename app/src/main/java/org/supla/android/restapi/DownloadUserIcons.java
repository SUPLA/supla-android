package org.supla.android.restapi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.supla.android.Trace;

public class DownloadUserIcons extends SuplaRestApiClientTask {

    public DownloadUserIcons(Context context) {
        super(context);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Trace.d("DownloadUserIcons", "DownloadUserIcons");

        return null;
    }

    public int downloadCount() {
        return 0;
    }
}
