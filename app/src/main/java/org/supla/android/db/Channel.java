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
import android.content.Context;
import android.database.Cursor;
import android.annotation.SuppressLint;

import org.supla.android.R;
import org.supla.android.SuplaApp;
import org.supla.android.TemperaturePresenterFactory;
import org.supla.android.data.presenter.TemperaturePresenter;
import org.supla.android.images.ImageId;
import org.supla.android.lib.DigiglassValue;
import org.supla.android.lib.SuplaChannel;
import org.supla.android.lib.SuplaChannelState;
import org.supla.android.lib.SuplaChannelValue;
import org.supla.android.lib.SuplaConst;


public class Channel extends ChannelBase {

    private ChannelValue Value;
    private ChannelExtendedValue ExtendedValue;
    private int Type;
    private int ProtocolVersion;
    private short ManufacturerID;
    private short ProductID;
    private int DeviceID;
    private int position;

    public Channel() { super(); }
    public Channel(TemperaturePresenterFactory p) { super(p); }

    public int getChannelId() {
        return getRemoteId();
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public int getProtocolVersion() {
        return ProtocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        ProtocolVersion = protocolVersion;
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    protected int _getOnLine() {
        return Value != null && Value.getOnLine() ? 100 : 0;
    }

    public ChannelValue getValue() {

        if (Value == null)
            Value = new ChannelValue();

        return Value;
    }

    public void setValue(ChannelValue value) {
        Value = value;
    }

    public ChannelExtendedValue getExtendedValue() {
        return ExtendedValue;
    }

    public void setExtendedValue(ChannelExtendedValue extendedValue) {
        ExtendedValue = extendedValue;
    }

    @SuppressLint("Range")
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
        setPosition(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_POSITION)));
        setProfileId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_PROFILEID)));

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
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_POSITION, getPosition());
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_PROFILEID, getProfileId());

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
        return Value != null ? getTemperaturePresenter().getTemp(Value, this) : TEMPERATURE_NA_VALUE;
    }

    public double getDistance() {
        return Value != null ? Value.getDistance() : -1;
    }

    public byte getClosingPercentage() {

        byte p = Value != null ? Value.getRollerShutterValue().getClosingPercentage() : 0;

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
                && (getFunc() == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
                   || getFunc() == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW)
                && getClosingPercentage() >= 100) {
            return 1;
        }

        return super.imgActive(value);
    }

    @Override
    public ImageId getImageIdx(WhichOne whichImage) {
        return super.getImageIdx(whichImage, Value);
    }

    public String getUnit() {
        if (getExtendedValue() != null
                && getExtendedValue().getExtendedValue() != null
                && getExtendedValue().getExtendedValue().ImpulseCounterValue != null) {

            String unit = getExtendedValue().getExtendedValue().ImpulseCounterValue.getUnit();
            if (unit != null && unit.length() > 0) {
                return unit;
            }
        }
        return "";
    }

    protected CharSequence getHumanReadableValue(WhichOne whichOne, ChannelValue value) {

        switch (getFunc()) {
            case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
            case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
            case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:
                if (value.getSubValueType() == SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS) {
                    return String.format("%.1f " + getUnit(),
                            value.getImpulseCounterCalculatedValue(true));
                } else if (value.getSubValueType()
                        == SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS) {
                    return String.format("%.2f kWh", value.getTotalForwardActiveEnergy(true));
                }
                break;
        }

        // TODO: Remove channel type checking in future versions. Check function instead of type. # 140-issue
        if (getType() == SuplaConst.SUPLA_CHANNELTYPE_IMPULSE_COUNTER) {
            return getOnLine() ?
                    String.format("%.1f " + getUnit(), value.getImpulseCounterCalculatedValue()) :
                    "--- " + getUnit();
        }

        return super.getHumanReadableValue(whichOne, value);
    }

    public CharSequence getHumanReadableValue(WhichOne whichOne) {
        return getHumanReadableValue(whichOne, Value);
    }

    public CharSequence getHumanReadableValue() {
        return getHumanReadableValue(WhichOne.First, Value);
    }

    public SuplaChannelState getChannelState() {
        ChannelExtendedValue ev = getExtendedValue();

        if (ev != null) {
            return ev.getExtendedValue().ChannelStateValue;
        }

        return null;
    }

    public Float getLightSourceLifespanLeft() {
        SuplaChannelState state = getChannelState();
        if (state != null
                && state.getLightSourceLifespan() != null
                && state.getLightSourceLifespan() > 0) {

            if (state.getLightSourceLifespanLeft() != null) {
                return state.getLightSourceLifespanLeft();
            } else if (state.getLightSourceOperatingTimePercentLeft() != null) {
                return state.getLightSourceOperatingTimePercentLeft();
            }
        }
        return null;
    }

    private int getChannelWarningLevel(Context context, StringBuilder message) {

        switch (getFunc()) {
            case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
            case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
            case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:
                if (getValue().overcurrentRelayOff()) {
                    if (message != null) {
                        message.append(context.getResources().getString(R.string.overcurrent_warning));
                    }
                    return 2;
                }
                break;
        }

        switch (getFunc()) {
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW:
                ChannelValue value = getValue();
                if (value.calibrationFailed()) {
                    if (message != null) {
                        message.append(context.getResources().getString(R.string.calibration_failed));
                    }
                    return 1;
                } else if (value.calibrationLost()) {
                    if (message != null) {
                        message.append(context.getResources().getString(R.string.calibration_lost));
                    }
                    return 1;
                } else if (value.motorProblem()) {
                    if (message != null) {
                        message.append(context.getResources().getString(R.string.motor_problem));
                    }
                    return 2;
                }
                break;
            case SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE:
            case SuplaConst.SUPLA_CHANNELFNC_VALVE_PERCENTAGE:
                if (getValue().isManuallyClosed() || getValue().flooding()) {
                    if (message != null) {
                        message.append(context.getResources().getString(R.string.valve_warning));
                    }
                    return 2;
                }
                return 0;
            case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
                Float lightSourceLifespanLeft = getLightSourceLifespanLeft();
                if (lightSourceLifespanLeft != null && lightSourceLifespanLeft <= 20) {

                    if (message != null) {
                        message.append(
                                context.getResources().getString(
                                        getAltIcon() == 2
                                                ? (lightSourceLifespanLeft <= 5
                                                ? R.string.uv_warning2: R.string.uv_warning1)
                                                : R.string.lightsource_warning,
                                        String.format("%.2f%%", lightSourceLifespanLeft)));

                    }

                    return lightSourceLifespanLeft <= 5 ? 2 : 1;
                }
                break;
            case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL:
            case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL:
                DigiglassValue dgfVal = getValue().getDigiglassValue();

                if (dgfVal.isPlannedRegenerationInProgress()) {
                    if (message != null) {
                        message.append(context.getResources().
                                getString(R.string.dgf_planned_regeneration_in_progress));
                    }
                    return 1;
                } else if (dgfVal.regenerationAfter20hInProgress()) {
                    if (message != null) {
                        message.append(context.getResources().
                                getString(R.string.dgf_regeneration_after20h));
                    }
                    return 1;
                } else if (dgfVal.isTooLongOperationWarningPresent()) {
                    if (message != null) {
                        message.append(context.getResources().
                                getString(R.string.dgf_too_long_operation_warning));
                    }
                    return 2;
                } else
                    return 0;
        }

        return 0;
    }

    public String getChannelWarningMessage(Context context) {
        StringBuilder result = new StringBuilder();
        getChannelWarningLevel(context, result);
        if (result.length() > 0) {
            return result.toString();
        }
        return null;
    }

    public int getChannelWarningLevel() {
        return getChannelWarningLevel(null,null);
    }

    public int getChannelWarningIcon() {

        switch (getChannelWarningLevel()) {
            case 1:
                return R.drawable.channel_warning_level1;
            case 2:
                return R.drawable.channel_warning_level2;
        }

        return 0;
    }

    public int getChannelStateIcon() {
        if ((getOnLine()
                || (getType() == SuplaConst.SUPLA_CHANNELTYPE_BRIDGE
                && (getFlags() & SuplaConst.SUPLA_CHANNEL_FLAG_CHANNELSTATE) > 0
                && (getFlags()
                & SuplaConst.SUPLA_CHANNEL_FLAG_OFFLINE_DURING_REGISTRATION) > 0))) {
            SuplaChannelState state = getChannelState();

            if (state != null
                    || (getFlags() & SuplaConst.SUPLA_CHANNEL_FLAG_CHANNELSTATE) != 0) {
                if (state != null && (state.getFields() & state.getDefaultIconField()) != 0) {
                    switch (state.getDefaultIconField()) {
                        case SuplaChannelState.FIELD_BATTERYPOWERED:
                            if (state.isBatteryPowered()) {
                                return R.drawable.battery;
                            }
                            break;
                    }
                }

                return R.drawable.channelstateinfo;
            }
        }

        return 0;
    }

    @Override
    public String toString() {
        return "{channelId=" + getRemoteId() + ", profileId=" + getProfileId() + ", value=" + Value + "}";
    }
}

