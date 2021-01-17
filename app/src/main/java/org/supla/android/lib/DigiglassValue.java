package org.supla.android.lib;

public class DigiglassValue {

    public static final byte TOO_LONG_OPERATION_WARNING = 0x1;
    public static final byte PLANNED_REGENERATION_IN_PROGRESS = 0x2;
    public static final byte REGENERATION_AFTER_20H_IN_PROGRESS = 0x4;

    private byte flags;
    private byte sectionCount;
    private short mask;

    public DigiglassValue(byte[] value) {
        flags = 0;
        sectionCount = 0;
        mask = 0;
        if (value != null && value.length == SuplaConst.SUPLA_CHANNELVALUE_SIZE) {
            flags = value[0];
            sectionCount = value[1];
            mask = value[2];
            mask |= (short)(value[3] << 8);
        }
    }

    public byte getFlags() {
        return flags;
    }

    public byte getSectionCount() {
        return sectionCount;
    }

    public short getMask() {
        return mask;
    }

    public boolean isTooLongOperationWarningPresent() {
        return (flags & TOO_LONG_OPERATION_WARNING) > 0;
    }

    public boolean isPlannedRegenerationInProgress() {
        return (flags & PLANNED_REGENERATION_IN_PROGRESS) > 0;
    }

    public boolean regenerationAfter20hInProgress() {
        return (flags & REGENERATION_AFTER_20H_IN_PROGRESS) > 0;
    }

    public boolean isSectionTransparent(int sectionNumber) {
        if (sectionNumber < sectionCount) {
            byte bit = (byte) (1 << sectionNumber);
            return (mask & bit) > 0;
        }
        return false;
    }

    public boolean isAnySectionTransparent() {
        short activeBits = (short) (Math.pow(2, sectionCount) - 1);
        return (mask & activeBits) > 0;
    }
}
