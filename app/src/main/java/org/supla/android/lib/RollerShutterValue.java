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

public class RollerShutterValue {

    private byte position;
    private byte tilt;
    private byte bottomPosition;
    private short flags;

    public RollerShutterValue(byte[] value) {
        position = -1;

        if (value != null && value.length >= 5) {
            position = value[0];
            tilt = value[1];
            bottomPosition = value[2];
            flags = value[3];
            flags |= (short)(value[4] << 8);
        }

        if (position < -1 || position > 100) {
            position = -1;
        }
    }

    public byte getPosition() {
        return position;
    }

    public byte getTilt() {
        return tilt;
    }

    public byte getBottomPosition() {
        return bottomPosition;
    }

    public short getFlags() {
        return flags;
    }
}
