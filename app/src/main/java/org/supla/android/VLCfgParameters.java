package org.supla.android;

public class VLCfgParameters extends DeviceCfgParameters {

  public static final int MODE_UNKNOWN = -1;
  public static final int MODE_AUTO = 0;
  public static final int MODE_1 = 1;
  public static final int MODE_2 = 2;
  public static final int MODE_3 = 3;

  public static final int BOOST_UNKNOWN = -1;
  public static final int BOOST_AUTO = 0;
  public static final int BOOST_YES = 1;
  public static final int BOOST_NO = 2;

  public static final int MASK_MODE_AUTO_DISABLED = 0x1;
  public static final int MASK_MODE_1_DISABLED = 0x2;
  public static final int MASK_MODE_2_DISABLED = 0x4;
  public static final int MASK_MODE_3_DISABLED = 0x8;

  public static final int MASK_BOOST_AUTO_DISABLED = 0x1;
  public static final int MASK_BOOST_YES_DISABLED = 0x2;
  public static final int MASK_BOOST_NO_DISABLED = 0x4;

  private short LeftEdge;
  private short RightEdge;
  private short Minimum;
  private short Maximum;
  private byte Mode;
  private byte ModeMask = (byte) 0xFF;
  private byte Boost;
  private byte BoostMask = (byte) 0xFF;
  private short BoostLevel;
  private String PicFirmwareVersion;

  public short getLeftEdge() {
    return LeftEdge;
  }

  public short getRightEdge() {
    return RightEdge;
  }

  public short getMinimum() {
    return Minimum;
  }

  public short getMaximum() {
    return Maximum;
  }

  public byte getMode() {
    return Mode;
  }

  public byte getModeMask() {
    return ModeMask;
  }

  public boolean isModeDisabled(int mode) {

    switch (mode) {
      case VLCfgParameters.MODE_AUTO:
        return (ModeMask & MASK_MODE_AUTO_DISABLED) > 0;
      case VLCfgParameters.MODE_1:
        return (ModeMask & MASK_MODE_1_DISABLED) > 0;
      case VLCfgParameters.MODE_2:
        return (ModeMask & MASK_MODE_2_DISABLED) > 0;
      case VLCfgParameters.MODE_3:
        return (ModeMask & MASK_MODE_3_DISABLED) > 0;
    }

    return false;
  }

  public byte getBoost() {
    return Boost;
  }

  public byte getBoostMask() {
    return BoostMask;
  }

  public short getBoostLevel() {
    return BoostLevel;
  }

  public boolean isBoostDisabled(int boost) {

    switch (boost) {
      case VLCfgParameters.BOOST_AUTO:
        return (BoostMask & MASK_BOOST_AUTO_DISABLED) > 0;
      case VLCfgParameters.BOOST_YES:
        return (BoostMask & MASK_BOOST_YES_DISABLED) > 0;
      case VLCfgParameters.BOOST_NO:
        return (BoostMask & MASK_BOOST_NO_DISABLED) > 0;
    }

    return false;
  }

  public String getPicFirmwareVersion() {
    return PicFirmwareVersion == null ? "" : PicFirmwareVersion;
  }

  public boolean setParams(byte[] data) {
    if (data == null || data.length < 15 || data.length > 37) {
      return false;
    }

    LeftEdge = getShort(data, 0);
    RightEdge = getShort(data, 2);
    Minimum = getShort(data, 4);
    Maximum = getShort(data, 6);
    Mode = data[8];
    Boost = data[9];
    BoostLevel = getShort(data, 10);
    ModeMask = data[13];
    BoostMask = data[14];

    if (data.length >= 17 && data[15] == 2) {
      setLedConfig(Byte.valueOf(data[16]));
    } else {
      setLedConfig(null);
    }

    StringBuilder sb = new StringBuilder();

    if (data.length >= 37) {
      for (int a = 0; a < 20; a++) {
        if (data[17 + a] == 0) {
          break;
        } else {
          sb.append((char) data[17 + a]);
        }
      }
    }

    PicFirmwareVersion = sb.toString();

    return true;
  }
}
