package org.supla.android;

public class VLCfgParameters {

    public final static int MODE_UNKNOWN = -1;
    public final static int MODE_AUTO = 0;
    public final static int MODE_1 = 1;
    public final static int MODE_2 = 2;
    public final static int MODE_3 = 3;

    public final static int DRIVE_UNKNOWN = -1;
    public final static int DRIVE_AUTO = 0;
    public final static int DRIVE_YES = 1;
    public final static int DRIVE_NO = 2;

    private short LeftEdge;
    private short RightEdge;
    private short Minimum;
    private short Maximum;
    private byte Mode;
    private byte Drive;
    private short DriveLevel;

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

    public byte getDrive() {
        return Drive;
    }

    public short getDriveLevel() {
        return DriveLevel;
    }

    private short getShort(byte[] data, int offset) {
        int x = (int)data[offset+1] & 0xFF;
        x<<=8;
        x|=(int)data[offset] & 0xFF;
        return (short)x;
    }

    public boolean setParams(byte[] data) {
        if (data == null || data.length != 13) {
            return false;
        }

        LeftEdge = getShort(data, 0);
        RightEdge = getShort(data, 2);
        Minimum = getShort(data, 4);
        Maximum = getShort(data, 6);
        Mode = data[8];
        Drive = data[9];
        DriveLevel = getShort(data, 10);
        
        return true;
    }
}
