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
import android.database.Cursor;
import androidx.annotation.NonNull;
import org.supla.android.ValuesFormatterProvider;
import org.supla.android.data.source.local.entity.ChannelEntity;

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

  @NonNull
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
