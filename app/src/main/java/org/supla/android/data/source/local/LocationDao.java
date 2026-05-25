package org.supla.android.data.source.local;

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

import androidx.annotation.NonNull;
import org.supla.android.data.source.local.entity.LocationEntity;
import org.supla.android.db.Location;

public class LocationDao extends BaseDao {
  public LocationDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
    super(databaseAccessProvider);
  }

  public Location getLocation(int locationId, long profileId) {
    String[] projection = {
      LocationEntity.COLUMN_ID,
      LocationEntity.COLUMN_REMOTE_ID,
      LocationEntity.COLUMN_CAPTION,
      LocationEntity.COLUMN_VISIBLE,
      LocationEntity.COLUMN_COLLAPSED,
      LocationEntity.COLUMN_SORTING,
      LocationEntity.COLUMN_SORT_ORDER,
      LocationEntity.COLUMN_PROFILE_ID
    };

    return getItem(
        Location::new,
        projection,
        LocationEntity.TABLE_NAME,
        key(LocationEntity.COLUMN_REMOTE_ID, locationId),
        key(LocationEntity.COLUMN_PROFILE_ID, profileId));
  }
}
