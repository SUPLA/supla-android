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

import java.util.Date;
import org.supla.android.tools.UsedFromNativeCode;

public class SuplaRegistrationEnabled {

  public long ClientTimestamp;
  public long IODeviceTimestamp;

  @UsedFromNativeCode
  public SuplaRegistrationEnabled() {
    // This constructor is used by native code
  }

  public SuplaRegistrationEnabled(SuplaRegistrationEnabled regen) {
    if (regen != null) {
      ClientTimestamp = regen.ClientTimestamp;
      IODeviceTimestamp = regen.IODeviceTimestamp;
    }
  }

  public Date IODeviceRegistrationExpirationDate() {
    if (IODeviceTimestamp > 0) {
      return new Date(IODeviceTimestamp * 1000L);
    }

    return null;
  }

  public boolean IsIODeviceRegistrationEnabled() {
    Date d = IODeviceRegistrationExpirationDate();
    return d != null && (new Date()).before(d);
  }
}
