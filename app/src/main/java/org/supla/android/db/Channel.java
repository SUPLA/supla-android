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
import org.supla.android.lib.SuplaChannel;


public class Channel extends ChannelBase {

    private ChannelValue Value;
    private int ProtocolVersion;

    public int getChannelId() {
        return getRemoteId();
    }


    public void setProtocolVersion(int protocolVersion) {
        ProtocolVersion = protocolVersion;
    }

    public int getProtocolVersion() {
        return ProtocolVersion;
    }

    protected int _getOnLine() {
        return Value != null && Value.getOnLine() ? 100 : 0;
    }

    public void setValue(ChannelValue value) {
        Value = value;
    }

    public ChannelValue getValue() {

        if (Value == null)
            Value = new ChannelValue();

        return Value;
    }

    public void AssignCursorData(Cursor cursor) {

        setId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ChannelEntry._ID)));
        setRemoteId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID)));
        setFunc(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_FUNC)));
        setCaption(cursor.getString(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION)));
        setVisible(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE)));
        setLocationId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID)));
        setAltIcon(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON)));
        setFlags(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS)));
        setProtocolVersion(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION)));

        ChannelValue cv = new ChannelValue();
        cv.AssignCursorData(cursor);
        setValue(cv);
    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID, getChannelId());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION, getCaption());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_FUNC, getFunc());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE, getVisible());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID, getLocationId());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON, getAltIcon());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS, getFlags());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION, getProtocolVersion());

        return values;
    }

    public void Assign(SuplaChannel channel) {

        super.Assign(channel);
        setProtocolVersion(channel.ProtocolVersion);
        getValue().AssignSuplaChannelValue(channel.Value);

    }


    public boolean Diff(SuplaChannel channel) {

        return super.Diff(channel)
                || channel.ProtocolVersion != getProtocolVersion()
                || getValue().Diff(channel.Value);

    }

    public boolean Diff(Channel channel) {

        return super.Diff(channel)
                || channel.getProtocolVersion() != getProtocolVersion()
                || getValue().Diff(channel.getValue());

    }

    public double getDouble(double unknown) {
        return Value != null ? Value.getDouble(unknown) : unknown;
    }

    public double getHumidity() {
        return Value != null ? Value.getDistance() : -1;
    }

    public double getTemp() {
        return Value != null ? Value.getTemp(getFunc()) : -275;
    }

    public double getDistance() {
        return Value != null ? Value.getDistance() : -1;
    }

    public byte getRollerShutterPosition() {

        byte p = Value != null ? Value.getPercent() : 0;

        if (p < 100 && getSubValueHi() == true)
            p = 100;

        return p;
    }

    public byte getColorBrightness() {
        return Value != null ? Value.getColorBrightness() : 0;
    }

    public byte getBrightness() {
        return Value != null ? Value.getBrightness() : 0;
    }

    public int getColor() {
        return Value != null ? Value.getColor() : 0;
    }

    public boolean getSubValueHi() {
        return Value != null && Value.getSubValueHi() > 0;
    }

    public int getImageIdx(WhichOne whichImage) {
        return super.getImageIdx(whichImage, Value);
    }

    public String getHumanReadableValue(WhichOne whichOne) {
        return super.getHumanReadableValue(whichOne, Value);
    }
    public String getHumanReadableValue() {
        return super.getHumanReadableValue(WhichOne.First, Value);
    }

}

