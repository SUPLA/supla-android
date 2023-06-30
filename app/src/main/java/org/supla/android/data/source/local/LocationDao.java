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
import org.supla.android.db.Location;
import org.supla.android.db.SuplaContract;

public class LocationDao extends BaseDao {
  public LocationDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
    super(databaseAccessProvider);
  }

  public Location getLocation(int locationId) {
    String[] projection = {
      SuplaContract.LocationEntry._ID,
      SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID,
      SuplaContract.LocationEntry.COLUMN_NAME_CAPTION,
      SuplaContract.LocationEntry.COLUMN_NAME_VISIBLE,
      SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED,
      SuplaContract.LocationEntry.COLUMN_NAME_SORTING,
      SuplaContract.LocationEntry.COLUMN_NAME_SORT_ORDER,
      SuplaContract.LocationEntry.COLUMN_NAME_PROFILEID
    };

    return getItem(
        Location::new,
        projection,
        SuplaContract.LocationEntry.TABLE_NAME,
        key(SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID, locationId),
        key(SuplaContract.LocationEntry.COLUMN_NAME_PROFILEID, getCachedProfileId()));
  }

  public void insert(Location location) {
    location.setProfileId(getCachedProfileId());
    insert(location, SuplaContract.LocationEntry.TABLE_NAME);
  }

  public void update(Location location) {
    update(
        location,
        SuplaContract.LocationEntry.TABLE_NAME,
        key(SuplaContract.LocationEntry._ID, location.getId()));
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
            + SuplaContract.LocationEntry._ID
            + ", "
            + "L."
            + SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID
            + ", "
            + "L."
            + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION
            + ", "
            + "L."
            + SuplaContract.LocationEntry.COLUMN_NAME_VISIBLE
            + ", "
            + "L."
            + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED
            + ", "
            + "L."
            + SuplaContract.LocationEntry.COLUMN_NAME_SORTING
            + ", "
            + "L."
            + SuplaContract.LocationEntry.COLUMN_NAME_SORT_ORDER
            + ", "
            + "L."
            + SuplaContract.LocationEntry.COLUMN_NAME_PROFILEID
            + " FROM "
            + SuplaContract.LocationEntry.TABLE_NAME
            + " AS L "
            + " WHERE "
            + SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID
            + " IN ("
            + "SELECT "
            + SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID
            + " FROM "
            + SuplaContract.ChannelEntry.TABLE_NAME
            + " WHERE "
            + SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE
            + " > 0 "
            + " AND "
            + SuplaContract.ChannelEntry.COLUMN_NAME_PROFILEID
            + " = "
            + getCachedProfileId()
            + " UNION "
            + "SELECT "
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID
            + " FROM "
            + SuplaContract.ChannelGroupEntry.TABLE_NAME
            + " WHERE "
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE
            + " > 0"
            + " AND "
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_PROFILEID
            + " = "
            + getCachedProfileId()
            + ")"
            + " AND "
            + "L."
            + SuplaContract.LocationEntry.COLUMN_NAME_PROFILEID
            + " = "
            + getCachedProfileId()
            + " ORDER BY "
            + "L."
            + SuplaContract.LocationEntry.COLUMN_NAME_SORT_ORDER
            + ", "
            + "L."
            + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION
            + " COLLATE LOCALIZED";
    return db.rawQuery(sql, null);
  }
}
