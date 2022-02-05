package org.supla.android;

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

public class DeviceCfgParameters {
    public final static int LED_UNKNOWN = -1;
    public final static int LED_ON_WHEN_CONNECTED = 0;
    public final static int LED_OFF_WHEN_CONNECTED = 1;
    public final static int LED_ALWAYS_OFF = 2;

    private Byte LedConfig = null;

    protected short getShort(byte[] data, int offset) {
        int x = (int) data[offset + 1] & 0xFF;
        x <<= 8;
        x |= (int) data[offset] & 0xFF;
        return (short) x;
    }

    public Byte getLedConfig() {
        return LedConfig;
    }

    protected void setLedConfig(Byte ledConfig) {
        LedConfig = ledConfig;
    }
}
