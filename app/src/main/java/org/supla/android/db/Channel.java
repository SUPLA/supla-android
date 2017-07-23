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
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;

import org.supla.android.R;
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
        return getCaption(null);
    }

    public String getNotEmptyCaption(Context context) {
        return getCaption(context);
    }

    private String getCaption(Context context) {

        if ( context != null && Caption.equals("") ) {

            int idx = -1;

            switch(Func) {
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY:
                    idx = R.string.channel_func_gatewayopeningsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
                    idx = R.string.channel_func_gateway;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATE:
                    idx = R.string.channel_func_gateopeningsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
                    idx = R.string.channel_func_gate;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR:
                    idx = R.string.channel_func_garagedooropeningsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
                    idx = R.string.channel_func_garagedoor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_DOOR:
                    idx = R.string.channel_func_dooropeningsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
                    idx = R.string.channel_func_door;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER:
                    idx = R.string.channel_func_rsopeningsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
                    idx = R.string.channel_func_rollershutter;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
                    idx = R.string.channel_func_powerswith;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
                    idx = R.string.channel_func_lightswith;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:
                    idx = R.string.channel_func_thermometer;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_HUMIDITY:
                    idx = R.string.channel_func_humidity;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE:
                    idx = R.string.channel_func_humidityandtemperature;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_NOLIQUIDSENSOR:
                    idx = R.string.channel_func_noliquidsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                    idx = R.string.channel_func_dimmer;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
                    idx = R.string.channel_func_rgblighting;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                    idx = R.string.channel_func_dimmerandrgblighting;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR:
                    idx = R.string.channel_func_depthsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR:
                    idx = R.string.channel_func_distancesensor;
                    break;
            }



            if ( idx == -1 )
                Caption = Integer.toString(Func);
            else
                Caption = context.getResources().getString(idx);
        }

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

    public double getDouble(double unknown) {

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

        return unknown;
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

        return -1;
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

            } else if ( getFunc() == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ) {

                return getDouble(-275);
            }


        }

        return -275;
    }

    public double getDistance() {
        return getDouble(-1);
    }

    private byte getPercent(short n) {

        byte[] t = Value.getChannelValue();
        byte result = 0;

        if ( t.length > n ) {

            result = t[n];

            if ( result > 100 )
                result = 0;
        }

        return result;
    }

    public byte getPercent() {
        return getPercent((short)0);
    }

    private byte getBrightness(short n) {
        return getPercent(n);
    }

    public byte getColorBrightness() {

       return getBrightness((short)1);
    }

    public byte getBrightness() {
        return getBrightness((short)0);
    }

    public int getColor() {

        int result = 0;

        byte[] t = Value.getChannelValue();

        if ( t.length > 4 ) {

            result = ((int)t[4] << 16) & 0x00FF0000;
            result |= ((int)t[3] << 8) & 0x0000FF00;
            result |= (int)t[2] & 0x00000FF;
        }

        return result;
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

    public byte getSubValueHi() {

        if ( getValue() != null ) {
            byte[] sub_value = getValue().getChannelSubValue();
            if ( sub_value.length > 0
                    && sub_value[0] == 1 ) {
                return 1;
            }

        }

        return 0;
    }

    public int StateUp() {

        if ( getOnLine() ) {
            switch(getFunc()) {

                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:

                    return getSubValueHi();

                case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:

                    return 1;

                case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                    return getBrightness() > 0 ? 1 : 0;

                case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
                    return getColorBrightness() > 0 ? 1 : 0;

                case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                {
                    int result = 0;

                    if ( getBrightness() > 0 )
                        result |= 0x1;

                    if ( getColorBrightness() > 0 )
                        result |= 0x2;

                    return result;
                }


            }
        }

        return hiValue() ? 1 : 0;

    }

}

