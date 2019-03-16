package org.supla.android.db;

import android.content.ContentValues;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class MeasurementItem extends DbItem {
    protected int ChannelId;
    protected long Timestamp;

    public int getChannelId() {
        return ChannelId;
    }

    public void setChannelId(int channelId) {
        ChannelId = channelId;
    }

    public long getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(long timestamp) {
        Timestamp = timestamp;
    }

    protected Double getDouble(JSONObject obj, String name) throws JSONException {
        if (!obj.isNull(name)) {
            return obj.getDouble(name);
        }
        return null;
    }

    protected long getLong(JSONObject obj, String name) throws JSONException {
        if (!obj.isNull(name)) {
            return obj.getLong(name);
        }
        return 0;
    }

    protected boolean getBoolean(JSONObject obj, String name) throws JSONException {
        boolean result = false;

        if (!obj.isNull(name)) {
            try {
                result = obj.getBoolean(name);
            } catch (JSONException e) {
                result = obj.getInt(name) > 0;
            }
        }
        return result;
    }

    protected void putNullOrDouble(ContentValues values, String name, double value) {
        if (value == 0) {
            values.putNull(name);
        } else {
            values.put(name, value);
        }
    }

    protected Double getTemperature(JSONObject obj, String name) throws JSONException {
        Double result = getDouble(obj, name);
        return result != null && result > -273 ? result : null;
    }

    protected void putNullOrDouble(ContentValues values, String name, Double value) {
        if (value == null) {
            values.putNull(name);
        } else {
            values.put(name, value);
        }
    }


}
