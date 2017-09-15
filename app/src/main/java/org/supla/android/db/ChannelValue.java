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

import android.database.Cursor;
import android.util.Base64;

import org.supla.android.lib.SuplaChannelValue;
import org.supla.android.lib.SuplaConst;

import java.util.Arrays;

public class ChannelValue {
    private byte[] Value;
    private byte[] SubValue;

    private boolean ValueDiff(byte[] v1, byte[] v2) {

        if ( v1 == null && v2 == null ) return false;
        if ( v1 == null && v2 != null ) return true;
        if ( v1 != null && v2 == null ) return true;

        return !Arrays.equals(v1, v2);
    }

    byte[] getChannelValue() {
        return Value.clone();
    }

    String getChannelStringValue() {
        return Base64.encodeToString(Value, Base64.DEFAULT);
    }

    void setChannelValue(byte [] value) {
        if ( value == null
             || value.length == SuplaConst.SUPLA_CHANNELVALUE_SIZE
             || value.length == 0 )
            Value = value;
    }

    void setChannelStringValue(String value) {
        setChannelValue(Base64.decode(value, Base64.DEFAULT));
    }

    byte[] getChannelSubValue() {
        return SubValue;
    }

    String getChannelStringSubValue() {
        return Base64.encodeToString(SubValue, Base64.DEFAULT);
    }

    void setChannelSubValue(byte [] value) {
        if ( value == null
                || value.length == SuplaConst.SUPLA_CHANNELVALUE_SIZE
                || value.length == 0 )
            SubValue = value;
    }

    void setChannelStringSubValue(String value) {
        setChannelSubValue(Base64.decode(value, Base64.DEFAULT));
    }

    public void AssignCursorData(Cursor cursor) {

        setChannelStringValue(cursor.getString(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_VALUE)));
        setChannelStringSubValue(cursor.getString(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_SUBVALUE)));

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

}
