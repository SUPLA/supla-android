package org.supla.android.db;

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

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Base64;

import org.supla.android.lib.SuplaChannelValue;
import org.supla.android.lib.SuplaConst;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ChannelValue extends DbItem {

    private int ChannelId;
    private boolean OnLine;
    private byte[] Value;
    private byte[] SubValue;

    private boolean ValueDiff(byte[] v1, byte[] v2) {

        if (v1 == null && v2 == null) return false;
        if (v1 == null && v2 != null) return true;
        if (v1 != null && v2 == null) return true;

        return !Arrays.equals(v1, v2);
    }

    public int getChannelId() {
        return ChannelId;
    }

    public void setChannelId(int channelId) {
        ChannelId = channelId;
    }

    byte[] getChannelValue() {
        return Value.clone();
    }

    void setChannelValue(byte[] value) {
        if (value == null
                || value.length == SuplaConst.SUPLA_CHANNELVALUE_SIZE
                || value.length == 0)
            Value = value;
    }

    String getChannelStringValue() {
        return Base64.encodeToString(Value, Base64.DEFAULT);
    }

    void setChannelStringValue(String value) {
        setChannelValue(Base64.decode(value, Base64.DEFAULT));
    }

    byte[] getChannelSubValue() {
        return SubValue;
    }

    void setChannelSubValue(byte[] value) {
        if (value == null
                || value.length == SuplaConst.SUPLA_CHANNELVALUE_SIZE
                || value.length == 0)
            SubValue = value;
    }

    String getChannelStringSubValue() {
        return Base64.encodeToString(SubValue, Base64.DEFAULT);
    }

    void setChannelStringSubValue(String value) {
        setChannelSubValue(Base64.decode(value, Base64.DEFAULT));
    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID, getChannelId());
        values.put(SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE, getOnLine());
        values.put(SuplaContract.ChannelValueEntry.COLUMN_NAME_VALUE, getChannelStringValue());
        values.put(SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE, getChannelStringSubValue());

        return values;
    }

    public void AssignCursorData(Cursor cursor) {

        setId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ChannelValueEntry._ID)));
        setChannelId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID)));
        setOnLine(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE)) != 0);
        setChannelStringValue(cursor.getString(cursor.getColumnIndex(SuplaContract.ChannelValueEntry.COLUMN_NAME_VALUE)));
        setChannelStringSubValue(cursor.getString(cursor.getColumnIndex(SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE)));

    }

    public void AssignCursorDataFromGroupView(Cursor cursor) {

        setId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ChannelGroupValueViewEntry._ID)));
        setChannelId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_CHANNELID)));
        setOnLine(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_ONLINE)) != 0);
        setChannelStringValue(cursor.getString(cursor.getColumnIndex(SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_VALUE)));
        setChannelStringSubValue(cursor.getString(cursor.getColumnIndex(SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_SUBVALUE)));

    }

    public void AssignSuplaChannelValue(SuplaChannelValue channelValue) {
        setChannelValue(channelValue.Value);
        setChannelSubValue(channelValue.SubValue);
    }

    public boolean Diff(SuplaChannelValue channelValue) {
        return ValueDiff(channelValue.Value, Value) || ValueDiff(channelValue.SubValue, SubValue);
    }

    public boolean Diff(ChannelValue channelValue) {
        return ValueDiff(channelValue.Value, Value) || ValueDiff(channelValue.SubValue, SubValue);
    }

    public boolean getOnLine() {
        return OnLine;
    }

    public void setOnLine(boolean onLine) {
        OnLine = onLine;
    }

    public double getDouble(double unknown) {

        byte[] t = getChannelValue();

        if (t.length > 0) {

            byte b;
            int l = t.length;
            int hl = l / 2;

            for (int a = 0; a < hl; a++) {
                b = t[a];
                t[a] = t[l - 1 - a];
                t[l - 1 - a] = b;
            }


            return ByteBuffer.wrap(t).getDouble();
        }

        return unknown;
    }

    public double getHumidity() {

        byte[] t = getChannelValue();

        if (t.length > 0) {

            byte[] i = new byte[4];
            i[0] = t[7];
            i[1] = t[6];
            i[2] = t[5];
            i[3] = t[4];

            return ByteBuffer.wrap(i).getInt() / 1000.00;
        }

        return -1;
    }

    public double getTemp(int func) {
        if (Value != null) {

            if (func == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {

                byte[] t = getChannelValue();

                if (t.length >= 4) {

                    byte[] i = new byte[4];
                    i[0] = t[3];
                    i[1] = t[2];
                    i[2] = t[1];
                    i[3] = t[0];

                    return ByteBuffer.wrap(i).getInt() / 1000.00;
                }

            } else if (func == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER) {

                return getDouble(-275);
            } else if (func == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT
                    || func == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS) {

                byte[] t = getChannelValue();
                if (t.length >= 4) {
                    int x = (int) t[3] & 0xFF;
                    x <<= 8;
                    x |= (int) t[2] & 0xFF;

                    return x * 0.01;
                }
            }


        }

        return -273;
    }

    public double getMeasuredTemp(int func) {
        return getTemp(func);
    }

    public double getPresetTemp(int func) {
        if (Value != null) {
            if (func == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT
                    || func == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS) {

                byte[] t = getChannelValue();
                if (t.length >= 6) {
                    int x = (int) t[5] & 0xFF;
                    x <<= 8;
                    x |= (int) t[4] & 0xFF;

                    return x * 0.01;
                }
            }
        }

        return -273;
    }

    public double getDistance() {
        return getDouble(-1);
    }

    private byte getPercent(short n) {

        byte[] t = getChannelValue();
        byte result = 0;

        if (t.length > n) {

            result = t[n];

            if (result > 100)
                result = 0;
        }

        return result;
    }

    public byte getPercent() {
        return getPercent((short) 0);
    }

    private byte getBrightness(short n) {
        return getPercent(n);
    }

    public byte getColorBrightness() {

        return getBrightness((short) 1);
    }

    public byte getBrightness() {
        return getBrightness((short) 0);
    }

    public int getColor() {

        int result = 0;

        byte[] t = getChannelValue();

        if (t.length > 4) {

            result = ((int) t[4] << 16) & 0x00FF0000;
            result |= ((int) t[3] << 8) & 0x0000FF00;
            result |= (int) t[2] & 0x00000FF;
        }

        return result;
    }

    public boolean hiValue() {


        byte[] value = getChannelValue();

        return value.length > 0
                && value[0] == 1;

    }

    public boolean isClosed() {
        return hiValue();
    }

    public byte getSubValueHi() {

        byte result = 0;

        byte[] sub_value = getChannelSubValue();
        if (sub_value.length > 0
                && sub_value[0] == 1) {
            result = 0x1;
        }

        if (sub_value.length > 1
                && sub_value[1] == 1) {
            result |= 0x2;
        }

        return result;
    }

    public double getTotalForwardActiveEnergy() {

        byte[] t = getChannelValue();

        if (t.length >= 5) {

            byte[] i = new byte[4];
            i[0] = t[4];
            i[1] = t[3];
            i[2] = t[2];
            i[3] = t[1];

            return ByteBuffer.wrap(i).getInt() / 100.00;
        }

        return 0.00;
    }

    public long getLong() {
        byte[] t = getChannelValue();

        if (t.length == 8) {

            byte[] i = new byte[8];

            for (int a = 0; a < 8; a++) {
                i[a] = t[7 - a];
            }

            return ByteBuffer.wrap(i).getLong();
        }

        return 0;
    }

    public double getImpulseCounterCalculatedValue() {
        return getLong() / 1000.0;
    }

    public boolean isManuallyClosed() {
        byte[] value = getChannelValue();

        return value.length > 1
                && (value[1] & SuplaConst.SUPLA_VALVE_FLAG_MANUALLY_CLOSED) > 0;
    }

    public boolean flooding() {
        byte[] value = getChannelValue();

        return value.length > 1
                && (value[1] & SuplaConst.SUPLA_VALVE_FLAG_FLOODING) > 0;
    }
}
