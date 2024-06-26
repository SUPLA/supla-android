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
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import org.supla.android.data.source.local.entity.UserIconEntity;

public class UserIconDao extends BaseDao {

  public UserIconDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
    super(databaseAccessProvider);
  }

  public void insert(int Id, Image[] images) {
    if (images.length != 8) {
      throw new IllegalArgumentException("Expects always 8 images");
    }

    ContentValues values = new ContentValues();
    values.put(UserIconEntity.COLUMN_REMOTE_ID, Id);
    values.put(UserIconEntity.COLUMN_PROFILE_ID, getCachedProfileId());

    for (Image image : images) {
      if (image.value != null) {
        values.put(image.column, image.value);
      }
    }

    write(
        sqLiteDatabase -> {
          sqLiteDatabase.insertWithOnConflict(
              UserIconEntity.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        });
  }

  public static class Image extends Key<byte[]> {
    public final int subId;
    public final long profileId;

    public Image(String column, byte[] value, int subId, long profileId) {
      super(column, value);
      this.subId = subId;
      this.profileId = profileId;
    }
  }
}
