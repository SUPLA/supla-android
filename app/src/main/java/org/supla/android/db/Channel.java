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

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import org.supla.android.R;
import org.supla.android.ValuesFormatterProvider;
import org.supla.android.data.ValuesFormatter;
import org.supla.android.data.source.local.entity.ChannelEntity;
import org.supla.android.data.source.remote.channel.SuplaChannelFlag;
import org.supla.android.lib.DigiglassValue;
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

  public Channel() {
    super();
  }

  public Channel(ValuesFormatterProvider p) {
    super(p);
  }

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

    if (Value == null) {
      Value = new ChannelValue();
    }

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
    setId(cursor.getLong(cursor.getColumnIndex(ChannelEntity.COLUMN_ID)));
    setDeviceID(cursor.getInt(cursor.getColumnIndex(ChannelEntity.COLUMN_DEVICE_ID)));
    setRemoteId(cursor.getInt(cursor.getColumnIndex(ChannelEntity.COLUMN_CHANNEL_REMOTE_ID)));
    setType(cursor.getInt(cursor.getColumnIndex(ChannelEntity.COLUMN_TYPE)));
    setFunc(cursor.getInt(cursor.getColumnIndex(ChannelEntity.COLUMN_FUNCTION)));
    setCaption(cursor.getString(cursor.getColumnIndex(ChannelEntity.COLUMN_CAPTION)));
    setVisible(cursor.getInt(cursor.getColumnIndex(ChannelEntity.COLUMN_VISIBLE)));
    setLocationId(cursor.getLong(cursor.getColumnIndex(ChannelEntity.COLUMN_LOCATION_ID)));
    setAltIcon(cursor.getInt(cursor.getColumnIndex(ChannelEntity.COLUMN_ALT_ICON)));
    setUserIconId(cursor.getInt(cursor.getColumnIndex(ChannelEntity.COLUMN_USER_ICON)));
    setManufacturerID(cursor.getShort(cursor.getColumnIndex(ChannelEntity.COLUMN_MANUFACTURER_ID)));
    setProductID(cursor.getShort(cursor.getColumnIndex(ChannelEntity.COLUMN_PRODUCT_ID)));
    setFlags(cursor.getInt(cursor.getColumnIndex(ChannelEntity.COLUMN_FLAGS)));
    setProtocolVersion(cursor.getInt(cursor.getColumnIndex(ChannelEntity.COLUMN_PROTOCOL_VERSION)));
    setPosition(cursor.getInt(cursor.getColumnIndex(ChannelEntity.COLUMN_POSITION)));
    setProfileId(cursor.getLong(cursor.getColumnIndex(ChannelEntity.COLUMN_PROFILE_ID)));

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

    values.put(ChannelEntity.COLUMN_CHANNEL_REMOTE_ID, getChannelId());
    values.put(ChannelEntity.COLUMN_DEVICE_ID, getDeviceID());
    values.put(ChannelEntity.COLUMN_CAPTION, getCaption());
    values.put(ChannelEntity.COLUMN_FUNCTION, getFunc());
    values.put(ChannelEntity.COLUMN_TYPE, getType());
    values.put(ChannelEntity.COLUMN_VISIBLE, getVisible());
    values.put(ChannelEntity.COLUMN_LOCATION_ID, getLocationId());
    values.put(ChannelEntity.COLUMN_ALT_ICON, getAltIcon());
    values.put(ChannelEntity.COLUMN_USER_ICON, getUserIconId());
    values.put(ChannelEntity.COLUMN_MANUFACTURER_ID, getManufacturerID());
    values.put(ChannelEntity.COLUMN_PRODUCT_ID, getProductID());
    values.put(ChannelEntity.COLUMN_FLAGS, getFlags());
    values.put(ChannelEntity.COLUMN_PROTOCOL_VERSION, getProtocolVersion());
    values.put(ChannelEntity.COLUMN_POSITION, getPosition());
    values.put(ChannelEntity.COLUMN_PROFILE_ID, getProfileId());

    return values;
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

  @SuppressLint("DefaultLocale")
  protected CharSequence getHumanReadableValue(WhichOne whichOne, ChannelValue value) {

    switch (getFunc()) {
      case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
      case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
      case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:
        if (value.getSubValueType() == SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS) {
          double doubleValue = value.getImpulseCounterCalculatedValue(true);
          return doubleValue > 0
              ? String.format("%.2f " + getUnit(), doubleValue)
              : ValuesFormatter.NO_VALUE_TEXT;
        } else if (value.getSubValueType()
            == SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS) {
          double doubleValue = value.getTotalForwardActiveEnergy(true);
          return doubleValue > 0
              ? String.format("%.2f kWh", doubleValue)
              : ValuesFormatter.NO_VALUE_TEXT;
        }
        break;
    }

    // TODO: Remove channel type checking in future versions. Check function instead of type. #
    // 140-issue
    if (getType() == SuplaConst.SUPLA_CHANNELTYPE_IMPULSE_COUNTER) {
      double doubleValue = value.getImpulseCounterCalculatedValue();
      return doubleValue > 0
          ? String.format("%.2f " + getUnit(), doubleValue)
          : ValuesFormatter.NO_VALUE_TEXT;
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
                context
                    .getResources()
                    .getString(
                        getAltIcon() == 2
                            ? (lightSourceLifespanLeft <= 5
                                ? R.string.uv_warning2
                                : R.string.uv_warning1)
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
            message.append(
                context.getResources().getString(R.string.dgf_planned_regeneration_in_progress));
          }
          return 1;
        } else if (dgfVal.regenerationAfter20hInProgress()) {
          if (message != null) {
            message.append(context.getResources().getString(R.string.dgf_regeneration_after20h));
          }
          return 1;
        } else if (dgfVal.isTooLongOperationWarningPresent()) {
          if (message != null) {
            message.append(
                context.getResources().getString(R.string.dgf_too_long_operation_warning));
          }
          return 2;
        } else {
          return 0;
        }
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
    return getChannelWarningLevel(null, null);
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
            && SuplaChannelFlag.CHANNEL_STATE.inside(getFlags())
            && SuplaChannelFlag.OFFLINE_DURING_REGISTRATION.inside(getFlags())))) {
      SuplaChannelState state = getChannelState();

      if (state != null || SuplaChannelFlag.CHANNEL_STATE.inside(getFlags())) {
        return R.drawable.ic_info;
      }
    }

    return 0;
  }

  @Override
  public String toString() {
    return "{channelId="
        + getRemoteId()
        + ", profileId="
        + getProfileId()
        + ", value="
        + Value
        + "}";
  }
}
