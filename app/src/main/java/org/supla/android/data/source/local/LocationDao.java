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

import android.support.annotation.NonNull;

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
                SuplaContract.LocationEntry.COLUMN_NAME_SORTING
        };

        return getItem(Location::new, projection, SuplaContract.LocationEntry.TABLE_NAME,
                key(SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID, locationId));
    }

    public void insert(Location location) {
        insert(location, SuplaContract.LocationEntry.TABLE_NAME);
    }

    public void update(Location location) {
        update(location, SuplaContract.LocationEntry.TABLE_NAME, key(SuplaContract.LocationEntry._ID, location.getId()));
    }
}
