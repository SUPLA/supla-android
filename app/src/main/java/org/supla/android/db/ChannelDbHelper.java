package org.supla.android.db;

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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Substitute of a {@link Channel}'s repository. Should contains all methods for DB operations connected to channels.
 */
public class ChannelDbHelper {


    public static List<Long> getSortedChannelIds(Cursor channelListCursor) {
        ArrayList<Long> orderedItems = new ArrayList<>();

        if (channelListCursor.moveToFirst()) {
            do {
                orderedItems.add(channelListCursor.getLong(
                        channelListCursor.getColumnIndex(SuplaContract.ChannelViewEntry._ID)));
            } while (channelListCursor.moveToNext());
        }
        channelListCursor.close();

        return orderedItems;
    }

    public static void updateChannelsOrder(SQLiteDatabase db, List<Long> reorderedIds, int locationId) {
        db.beginTransaction();
        try {
            db.execSQL("UPDATE " + SuplaContract.LocationEntry.TABLE_NAME +
                    " SET " + SuplaContract.LocationEntry.COLUMN_NAME_SORTING + " = '" + Location.SortingType.USER_DEFINED.name() +
                    "' WHERE " + SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID + " = " + locationId);

            int position = 1;
            for (Long id : reorderedIds) {
                db.execSQL("UPDATE " + SuplaContract.ChannelEntry.TABLE_NAME +
                        " SET " + SuplaContract.ChannelEntry.COLUMN_NAME_POSITION + " = " + position +
                        " WHERE " + SuplaContract.ChannelEntry._ID + " = " + id);
                position++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
