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
import android.annotation.SuppressLint;

import org.supla.android.lib.SuplaChannelAndTimerState;
import org.supla.android.lib.SuplaChannelElectricityMeterValue;
import org.supla.android.lib.SuplaChannelExtendedValue;
import org.supla.android.lib.SuplaChannelImpulseCounterValue;
import org.supla.android.lib.SuplaChannelState;
import org.supla.android.lib.SuplaChannelThermostatValue;
import org.supla.android.lib.SuplaConst;
import org.supla.android.lib.SuplaTimerState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ChannelExtendedValue extends DbItem {
    private int ChannelId;
    private int profileId;
    
    private SuplaChannelExtendedValue ExtendedValue;

    public static boolean valueExists(Cursor cursor) {
        int vidx = cursor.getColumnIndex(SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_VALUE);

        return cursor.getColumnIndex(SuplaContract.ChannelExtendedValueEntry._ID) > -1
                && cursor.getColumnIndex(SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_CHANNELID) > -1
                && vidx > -1
                && !cursor.isNull(vidx);
    }

    public int getChannelId() {
        return ChannelId;
    }

    public void setChannelId(int channelId) {
        ChannelId = channelId;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int pid) {
        profileId = pid;
    }

    public SuplaChannelExtendedValue getExtendedValue() {
        return ExtendedValue;
    }

    public void setExtendedValue(SuplaChannelExtendedValue extendedValue) {
        ExtendedValue = extendedValue;
    }

    private Object ByteArrayToObject(byte[] value) {
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(value);
            ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            return objectStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @SuppressLint("Range")
    public void AssignCursorData(Cursor cursor) {
        setId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ChannelExtendedValueEntry._ID)));
        setChannelId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_CHANNELID)));
        setProfileId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_PROFILEID)));

        byte[] value = cursor.getBlob(cursor.getColumnIndex(SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_VALUE));
        Object obj = ByteArrayToObject(value);

        if (obj instanceof SuplaChannelExtendedValue) {
            ExtendedValue = (SuplaChannelExtendedValue) obj;
        } else {
            ExtendedValue = new SuplaChannelExtendedValue();
        }
    }

    private byte[] ObjectToByteArray(Object obj) {
        byte[] value = new byte[0];
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(obj);
            objectStream.close();
            value = byteStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_CHANNELID, getChannelId());
        values.put(SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_VALUE,
                ObjectToByteArray(ExtendedValue));
        values.put(SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_PROFILEID,
                   getProfileId());
        
        return values;
    }
}
