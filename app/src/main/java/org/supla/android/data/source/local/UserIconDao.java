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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;

import org.supla.android.db.SuplaContract;

public class UserIconDao extends BaseDao {

    public UserIconDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
        super(databaseAccessProvider);
    }

    public void insert(int Id, Image images[], long profileId) {
        if (images.length != 4) {
            throw new IllegalArgumentException("Expects allways 4 images");
        }

        ContentValues values = new ContentValues();
        values.put(SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID, Id);

        for (Image image : images) {
            if (image.value != null) {
                values.put(image.column, image.value);
            }
        }
        values.put(SuplaContract.UserIconsEntry.COLUMN_NAME_PROFILE_ID, profileId);

        write(sqLiteDatabase -> {
            sqLiteDatabase.insertWithOnConflict(SuplaContract.UserIconsEntry.TABLE_NAME,
                    null, values, SQLiteDatabase.CONFLICT_IGNORE);
        });
    }

    public void delete(long profileId) {
        delete(SuplaContract.UserIconsEntry.TABLE_NAME,
               key(SuplaContract.UserIconsEntry.COLUMN_NAME_PROFILE_ID, profileId));
    }

    public Cursor getUserIcons(long profileId) {
        String sql = "SELECT " + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
                + ", " + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1
                + ", " + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2
                + ", " + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3
                + ", " + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4
                + " FROM " + SuplaContract.UserIconsEntry.TABLE_NAME
                + " WHERE " + SuplaContract.UserIconsEntry.COLUMN_NAME_PROFILE_ID
                + " = " + profileId
            ;

        return read(sqLiteDatabase -> sqLiteDatabase.rawQuery(sql, null));
    }

    public static class Image extends Key<byte[]> {
        public final int subId;

        public Image(String column, byte[] value, int subId) {
            super(column, value);
            this.subId = subId;
        }
    }
}
