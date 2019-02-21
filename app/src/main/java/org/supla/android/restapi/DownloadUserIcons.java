package org.supla.android.restapi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.supla.android.Trace;

public abstract class DownloadUserIcons extends SuplaRestApiClientTask {

    public DownloadUserIcons(Context context) {
        super(context);
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        if (getChannelId() <=0) {
            return null;
        }


        return null;
    }
}
