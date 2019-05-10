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

import org.supla.android.Trace;
import org.supla.android.images.ImageId;
import org.supla.android.lib.SuplaChannel;
import org.supla.android.lib.SuplaConst;


public class Channel extends ChannelBase {

    private ChannelValue Value;
    private ChannelExtendedValue ExtendedValue;
    private int Type;
    private int ProtocolVersion;
    private short ManufacturerID;
    private short ProductID;
    private int DeviceID;

    public int getChannelId() {
        return getRemoteId();
    }

    public int getType() { return Type; }

    public void setType(int type) {
        Type = type;
    }

    public void setProtocolVersion(int protocolVersion) {
        ProtocolVersion = protocolVersion;
    }

    public int getProtocolVersion() {
        return ProtocolVersion;
    }

    public short getManufacturerID() {
        return ManufacturerID;
    }

    public void setManufacturerID(short manufacturerID) {
        ManufacturerID = manufacturerID;
    }

    public short getProductID() {
        return ProductID;
    }

    public void setProductID(short productID) {
        ProductID = productID;
    }

    public int getDeviceID() {
        return DeviceID;
    }

    public void setDeviceID(int deviceID) {
        DeviceID = deviceID;
    }

    protected int _getOnLine() {
        return Value != null && Value.getOnLine() ? 100 : 0;
    }

    public void setValue(ChannelValue value) {
        Value = value;
    }

    public void setExtendedValue(ChannelExtendedValue extendedValue) {
        ExtendedValue = extendedValue;
    }

    public ChannelValue getValue() {

        if (Value == null)
            Value = new ChannelValue();

        return Value;
    }

    public ChannelExtendedValue getExtendedValue() {
        return ExtendedValue;
    }

    public void AssignCursorData(Cursor cursor) {
        setId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ChannelEntry._ID)));
        setDeviceID(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_DEVICEID)));
        setRemoteId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID)));
        setType(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_TYPE)));
        setFunc(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_FUNC)));
        setCaption(cursor.getString(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION)));
        setVisible(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE)));
        setLocationId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID)));
        setAltIcon(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON)));
        setUserIconId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_USERICON)));
        setManufacturerID(cursor.getShort(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_MANUFACTURERID)));
        setProductID(cursor.getShort(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_PRODUCTID)));
        setFlags(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS)));
        setProtocolVersion(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION)));

        ChannelValue cv = new ChannelValue();
        cv.AssignCursorData(cursor);
        setValue(cv);

        if (ChannelExtendedValue.valueExists(cursor)) {
            ChannelExtendedValue cev = new ChannelExtendedValue();
            cev.AssignCursorData(cursor);
            setExtendedValue(cev);
        } else {
            setExtendedValue(null);
        }
    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID, getChannelId());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_DEVICEID, getDeviceID());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION, getCaption());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_FUNC, getFunc());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_TYPE, getType());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE, getVisible());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID, getLocationId());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON, getAltIcon());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_USERICON, getUserIconId());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_MANUFACTURERID, getManufacturerID());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_PRODUCTID, getProductID());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS, getFlags());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION, getProtocolVersion());

        return values;
    }

    public void Assign(SuplaChannel channel) {
        super.Assign(channel);
        setDeviceID(channel.DeviceID);
        setType(channel.Type);
        setProtocolVersion(channel.ProtocolVersion);
        setManufacturerID(channel.ManufacturerID);
        setProductID(channel.ProductID);

        getValue().AssignSuplaChannelValue(channel.Value);
    }


    public boolean Diff(SuplaChannel channel) {
        return super.Diff(channel)
                || channel.Type != getType()
                || channel.DeviceID != getDeviceID()
                || channel.ProtocolVersion != getProtocolVersion()
                || channel.ManufacturerID != getManufacturerID()
                || channel.ProductID != getProductID()
                || getValue().Diff(channel.Value);
    }

    public boolean Diff(Channel channel) {

        return super.Diff(channel)
                || channel.getType() != getType()
                || channel.getDeviceID() != getDeviceID()
                || channel.getProtocolVersion() != getProtocolVersion()
                || channel.getManufacturerID() != getManufacturerID()
                || channel.getProductID() != getProductID()
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

        if (p < 100 && getSubValueHi())
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

    @Override
    protected int imgActive(ChannelValue value) {

        if (getOnLine()
                && getFunc() == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
                && getRollerShutterPosition() >= 100) {
            return 1;
        }

        return super.imgActive(value);
    }

    @Override
    public ImageId getImageIdx(WhichOne whichImage) {
        return super.getImageIdx(whichImage, Value);
    }

    public String getUnit(String defaultUnit) {
        if (getType() == SuplaConst.SUPLA_CHANNELTYPE_IMPULSE_COUNTER
                && getExtendedValue() != null
                && getExtendedValue().getType() == SuplaConst.EV_TYPE_IMPULSE_COUNTER_DETAILS_V1
                && getExtendedValue().getExtendedValue() != null
                && getExtendedValue().getExtendedValue().ImpulseCounterValue != null) {

            String unit = getExtendedValue().getExtendedValue().ImpulseCounterValue.getUnit();
            if (unit != null && unit.length() > 0) {
                return unit;
            }
        }
        return defaultUnit;
    }

    public String getUnit() {
        if (getOnLine() && getType() == SuplaConst.SUPLA_CHANNELTYPE_IMPULSE_COUNTER) {

            String dUnit = "";
            switch (getFunc()) {
                case SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER:
                    dUnit = "kWh";
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_GAS_METER:
                case SuplaConst.SUPLA_CHANNELFNC_WATER_METER:
                    dUnit = "m\u00B3";
                    break;
            }
            return getUnit(dUnit);
        }

        return "";
    }

    protected CharSequence getHumanReadableValue(WhichOne whichOne, ChannelValue value) {

        if (getOnLine() && getType() == SuplaConst.SUPLA_CHANNELTYPE_IMPULSE_COUNTER) {
            return String.format("%.1f "+getUnit(), value.getImpulseCounterCalculatedValue());
        }

        return super.getHumanReadableValue(whichOne, value);
    }

    public CharSequence getHumanReadableValue(WhichOne whichOne) {
        return getHumanReadableValue(whichOne, Value);
    }
    public CharSequence getHumanReadableValue() {
        return getHumanReadableValue(WhichOne.First, Value);
    }

}

