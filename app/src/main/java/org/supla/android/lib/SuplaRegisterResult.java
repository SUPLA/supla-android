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

import org.supla.android.tools.UsedFromNativeCode;

public class SuplaRegisterResult {

  public int ResultCode;
  public int ClientID;
  public int LocationCount;
  public int ChannelCount;
  public int ChannelGroupCount;
  public int SceneCount;
  public int Flags;
  public int ActivityTimeout;
  public int Version;
  public int VersionMin;

  @UsedFromNativeCode
  public SuplaRegisterResult() {
    // This constructor is used by native code
  }

  public SuplaRegisterResult(SuplaRegisterResult result) {
    if (result != null) {
      ResultCode = result.ResultCode;
      ClientID = result.ClientID;
      LocationCount = result.LocationCount;
      ChannelCount = result.ChannelCount;
      ChannelGroupCount = result.ChannelGroupCount;
      SceneCount = result.SceneCount;
      Flags = result.Flags;
      ActivityTimeout = result.ActivityTimeout;
      Version = result.Version;
      VersionMin = result.VersionMin;
    }
  }
}
