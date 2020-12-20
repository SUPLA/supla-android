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

import org.supla.android.db.ColorListItem;
import org.supla.android.db.SuplaContract;

public class ColorListDao extends BaseDao {

    public ColorListDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
        super(databaseAccessProvider);
    }

    public ColorListItem getColorListItem(int id, boolean group, int idx) {
        String[] projection = {
                SuplaContract.ColorListItemEntry._ID,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_REMOTEID,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_GROUP,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_COLOR,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_BRIGHTNESS,

        };

        return getItem(ColorListItem::new, projection, SuplaContract.ColorListItemEntry.TABLE_NAME,
                key(SuplaContract.ColorListItemEntry.COLUMN_NAME_REMOTEID, id),
                key(SuplaContract.ColorListItemEntry.COLUMN_NAME_GROUP, group ? 1 : 0),
                key(SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX, idx));
    }

    public void insert(ColorListItem item) {
        insert(item, SuplaContract.ColorListItemEntry.TABLE_NAME);
    }

    public void update(ColorListItem item) {
        update(item, SuplaContract.ColorListItemEntry.TABLE_NAME,
                key(SuplaContract.ColorListItemEntry.COLUMN_NAME_REMOTEID, item.getRemoteId()),
                key(SuplaContract.ColorListItemEntry.COLUMN_NAME_GROUP, item.getGroup() ? 1 : 0),
                key(SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX, item.getIdx()));
    }
}
