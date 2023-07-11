package org.supla.android.lib;

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

public class SuplaChannelBasicCfg {
  private String deviceName;
  private String deviceSoftwareVersion;
  private int deviceId;
  private int deviceFlags;
  private int manufacturerId;
  private int productId;
  private int channelId;
  private int number;
  private int channelType;
  private int func;
  private int funcList;
  private int channelFlags;
  private String caption;

  public SuplaChannelBasicCfg(
      String deviceName,
      String deviceSoftwareVersion,
      int deviceId,
      int deviceFlags,
      int manufacturerId,
      int productId,
      int channelId,
      int number,
      int channelType,
      int func,
      int funcList,
      int channelFlags,
      String caption) {
    this.deviceName = deviceName;
    this.deviceSoftwareVersion = deviceSoftwareVersion;
    this.deviceId = deviceId;
    this.deviceFlags = deviceFlags;
    this.manufacturerId = manufacturerId;
    this.productId = productId;
    this.channelId = channelId;
    this.number = number;
    this.channelType = channelType;
    this.func = func;
    this.funcList = funcList;
    this.channelFlags = channelFlags;
    this.caption = caption;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public String getDeviceSoftwareVersion() {
    return deviceSoftwareVersion;
  }

  public int getDeviceId() {
    return deviceId;
  }

  public int getDeviceFlags() {
    return deviceFlags;
  }

  public int getManufacturerId() {
    return manufacturerId;
  }

  public int getProductId() {
    return productId;
  }

  public int getChannelId() {
    return channelId;
  }

  public int getNumber() {
    return number;
  }

  public int getChannelType() {
    return channelType;
  }

  public int getFunc() {
    return func;
  }

  public int getFuncList() {
    return funcList;
  }

  public int getChannelFlags() {
    return channelFlags;
  }

  public String getCaption() {
    return caption;
  }
}
