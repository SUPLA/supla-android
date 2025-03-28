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

  private byte closingPercentage;
  private short flags;

  public RollerShutterValue(byte[] value) {
    closingPercentage = -1;

    if (value != null && value.length >= 5) {
      closingPercentage = value[0];
      flags = value[3];
      flags |= (short) (value[4] << 8);
    }

    if (closingPercentage < -1 || closingPercentage > 100) {
      closingPercentage = -1;
    }
  }

  public byte getClosingPercentage() {
    return closingPercentage;
  }

  public short getFlags() {
    return flags;
  }
}
