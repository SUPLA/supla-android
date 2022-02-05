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
