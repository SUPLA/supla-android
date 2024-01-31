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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import java.util.LinkedList;
import java.util.List;
import org.supla.android.data.source.local.entity.ChannelEntity;
import org.supla.android.data.source.local.entity.ChannelGroupEntity;
import org.supla.android.data.source.local.entity.LocationEntity;
import org.supla.android.db.Location;

public class LocationDao extends BaseDao {
  public LocationDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
    super(databaseAccessProvider);
  }

  public Location getLocation(int locationId) {
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
        key(LocationEntity.COLUMN_PROFILE_ID, getCachedProfileId()));
  }

  public void insert(Location location) {
    location.setProfileId(getCachedProfileId());
    insert(location, LocationEntity.TABLE_NAME);
  }

  public void update(Location location) {
    update(location, LocationEntity.TABLE_NAME, key(LocationEntity.COLUMN_ID, location.getId()));
  }

  public List<Location> getLocations() {
    return read(
        db -> {
          List<Location> rv = new LinkedList<>();
          Cursor c = getLocations(db);
          if (c.moveToFirst()) {
            do {
              Location l = new Location();
              l.AssignCursorData(c);
              rv.add(l);
            } while (c.moveToNext());
          }
          return rv;
        });
  }

  private Cursor getLocations(SQLiteDatabase db) {
    String sql =
        "SELECT DISTINCT "
            + "L."
            + LocationEntity.COLUMN_ID
            + ", "
            + "L."
            + LocationEntity.COLUMN_REMOTE_ID
            + ", "
            + "L."
            + LocationEntity.COLUMN_CAPTION
            + ", "
            + "L."
            + LocationEntity.COLUMN_VISIBLE
            + ", "
            + "L."
            + LocationEntity.COLUMN_COLLAPSED
            + ", "
            + "L."
            + LocationEntity.COLUMN_SORTING
            + ", "
            + "L."
            + LocationEntity.COLUMN_SORT_ORDER
            + ", "
            + "L."
            + LocationEntity.COLUMN_PROFILE_ID
            + " FROM "
            + LocationEntity.TABLE_NAME
            + " AS L "
            + " WHERE "
            + LocationEntity.COLUMN_REMOTE_ID
            + " IN ("
            + "SELECT "
            + ChannelEntity.COLUMN_LOCATION_ID
            + " FROM "
            + ChannelEntity.TABLE_NAME
            + " WHERE "
            + ChannelEntity.COLUMN_VISIBLE
            + " > 0 "
            + " AND "
            + ChannelEntity.COLUMN_PROFILE_ID
            + " = "
            + getCachedProfileId()
            + " UNION "
            + "SELECT "
            + ChannelGroupEntity.COLUMN_LOCATION_ID
            + " FROM "
            + ChannelGroupEntity.TABLE_NAME
            + " WHERE "
            + ChannelGroupEntity.COLUMN_VISIBLE
            + " > 0"
            + " AND "
            + ChannelGroupEntity.COLUMN_PROFILE_ID
            + " = "
            + getCachedProfileId()
            + ")"
            + " AND "
            + "L."
            + LocationEntity.COLUMN_PROFILE_ID
            + " = "
            + getCachedProfileId()
            + " ORDER BY "
            + "L."
            + LocationEntity.COLUMN_SORT_ORDER
            + ", "
            + "L."
            + LocationEntity.COLUMN_CAPTION
            + " COLLATE LOCALIZED";
    return db.rawQuery(sql, null);
  }
}
