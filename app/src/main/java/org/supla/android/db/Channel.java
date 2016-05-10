package org.supla.android.db;

/*
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

 Author: Przemyslaw Zygmunt p.zygmunt@acsoftware.pl [AC SOFTWARE]
 */


import android.content.ContentValues;
import android.database.Cursor;

import org.supla.android.Trace;
import org.supla.android.lib.SuplaChannel;
import org.supla.android.lib.SuplaConst;

import java.nio.ByteBuffer;


public class Channel {

    private long Id;
    private int ChannelId;
    private String Caption;
    private int Func;
    private boolean OnLine;
    private ChannelValue Value;
    private int Visible;
    private long LocationId;

    public void setId(long id) {
        Id = id;
    }

    public long getId() {
        return Id;
    }

    public void setChannelId(int channelId) {
        ChannelId = channelId;
    }

    public int getChannelId() {
        return ChannelId;
    }

    public void setCaption(String caption) {
        Caption = caption;
    }

    public String getCaption() {
        return Caption;
    }

    public void setFunc(int func) {
        Func = func;
    }

    public int getFunc() {
        return Func;
    }

    public void setOnLine(boolean onLine) {
        OnLine = onLine;
    };

    public boolean getOnLine() {
        return OnLine;
    };

    public void setValue(ChannelValue value) {
        Value = value;
    };

    public ChannelValue getValue() {

        if ( Value == null )
            Value = new ChannelValue();

        return Value;
    };

    public void setVisible(int visible) {
        Visible = visible;
    }

    public int getVisible() {
        return Visible;
    }

    public void setLocationId(long locationId) {
        LocationId = locationId;
    }

    public long getLocationId() {
        return LocationId;
    }

    public void AssignCursorData(Cursor cursor) {

        setId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ChannelEntry._ID)));
        setChannelId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID)));
        setFunc(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_FUNC)));
        setCaption(cursor.getString(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION)));
        setOnLine(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_ONLINE)) == 0 ? false : true);
        setVisible(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE)));
        setLocationId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID)));

        ChannelValue cv = new ChannelValue();
        cv.AssignCursorData(cursor);
        setValue(cv);

    }


    public void AssignSuplaChannel(SuplaChannel channel) {

        setChannelId(channel.Id);
        setCaption(channel.Caption);
        setOnLine(channel.OnLine);
        setFunc(channel.Func);

        getValue().AssignSuplaChannelValue(channel.Value);


    }

    public boolean Diff(SuplaChannel channel) {

        return channel.Id != getChannelId()
                || channel.Caption.equals(getCaption()) == false
                || channel.OnLine != getOnLine()
                || getValue().Diff(channel.Value);

    }

    public boolean Diff(Channel channel) {

        return channel.getChannelId() != getChannelId()
                || channel.getCaption().equals(getCaption()) == false
                || channel.getOnLine() != getOnLine()
                || getValue().Diff(channel.getValue());

    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID, getChannelId());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION, getCaption());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_FUNC, getFunc());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_ONLINE, getOnLine() == true ? 1 : 0);
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_VALUE, getValue().getChannelStringValue());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_SUBVALUE, getValue().getChannelStringSubValue());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE, getVisible());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID, getLocationId());

        return values;
    }

    public double getHumidity() {

        byte[] t = Value.getChannelValue();

        if ( t.length > 0 ) {

            byte[] i = new byte[4];
            i[0] = t[7];
            i[1] = t[6];
            i[2] = t[5];
            i[3] = t[4];

            return ByteBuffer.wrap(i).getInt() / 1000.00;
        }

        return 0;
    }

    public double getTemp() {

        if ( Value != null ) {

            if ( getFunc() == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ) {

                byte[] t = Value.getChannelValue();

                if ( t.length > 0 ) {

                    byte[] i = new byte[4];
                    i[0] = t[3];
                    i[1] = t[2];
                    i[2] = t[1];
                    i[3] = t[0];

                    return ByteBuffer.wrap(i).getInt() / 1000.00;
                }

            } else {

                byte[] t = Value.getChannelValue();

                if ( t.length > 0 ) {

                    byte b;
                    int l = t.length;
                    int hl = l/2;

                    for(int a=0;a<hl;a++) {
                        b = t[a];
                        t[a] = t[l-1-a];
                        t[l-1-a] = b;
                    }

                    return ByteBuffer.wrap(t).getDouble();
                }
            }


        }

        return 0;
    }

    private boolean hiValue() {

        if ( getOnLine()
             && getValue() != null ) {
            byte[] value = getValue().getChannelValue();

            if ( value.length > 0
                    && value[0] == 1 ) {
                return true;
            }
        }


        return false;
    }


    public boolean StateUp() {

        if ( getOnLine() ) {
            switch(getFunc()) {

                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:

                    if ( getValue() != null ) {
                        byte[] sub_value = getValue().getChannelSubValue();
                        if ( sub_value.length > 0
                                && sub_value[0] == 1 ) {
                            return true;
                        }

                    }

                    return false;

                case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:

                    return false;

            }
        }

        return hiValue();

    }

}

