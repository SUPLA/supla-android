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
import android.database.DatabaseUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import org.supla.android.data.source.local.entity.ChannelGroupEntity;
import org.supla.android.data.source.local.entity.LocationEntity;
import org.supla.android.data.source.local.entity.ProfileEntity;
import org.supla.android.data.source.local.entity.UserIconEntity;

public class ChannelDao extends BaseDao {

  public ChannelDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
    super(databaseAccessProvider);
  }

  public Cursor getSortedChannelGroupIdsForLocationCursor(String locationCaption) {
    String where =
        "G."
            + ChannelGroupEntity.COLUMN_PROFILE_ID
            + " = "
            + ProfileEntity.SUBQUERY_ACTIVE
            + " AND L."
            + LocationEntity.COLUMN_CAPTION
            + " = "
            + DatabaseUtils.sqlEscapeString(locationCaption);
    return getChannelGroupListCursor(where);
  }

  public void updateChannelGroupsOrder(List<Long> reorderedIds) {
    write(
        sqLiteDatabase -> {
          sqLiteDatabase.beginTransaction();
          try {
            int position = 1;
            for (Long id : reorderedIds) {
              sqLiteDatabase.execSQL(
                  "UPDATE "
                      + ChannelGroupEntity.TABLE_NAME
                      + " SET "
                      + ChannelGroupEntity.COLUMN_POSITION
                      + " = "
                      + position
                      + " WHERE "
                      + ChannelGroupEntity.COLUMN_ID
                      + " = "
                      + id);
              position++;
            }
            sqLiteDatabase.setTransactionSuccessful();
          } finally {
            sqLiteDatabase.endTransaction();
          }
        });
  }

  private Cursor getChannelGroupListCursor(@Nullable String where) {
    return read(
        sqLiteDatabase -> {
          String localWhere = "";
          if (where != null) {
            localWhere = " AND (" + where + ")";
          }

          String sql =
              "SELECT "
                  + "G."
                  + ChannelGroupEntity.COLUMN_ID
                  + " "
                  + ChannelGroupEntity.COLUMN_ID
                  + ", L."
                  + LocationEntity.COLUMN_CAPTION
                  + " AS section"
                  + ", L."
                  + LocationEntity.COLUMN_COLLAPSED
                  + " "
                  + LocationEntity.COLUMN_COLLAPSED
                  + ", G."
                  + ChannelGroupEntity.COLUMN_REMOTE_ID
                  + " "
                  + ChannelGroupEntity.COLUMN_REMOTE_ID
                  + ", G."
                  + ChannelGroupEntity.COLUMN_CAPTION
                  + " "
                  + ChannelGroupEntity.COLUMN_CAPTION
                  + ", G."
                  + ChannelGroupEntity.COLUMN_FUNCTION
                  + " "
                  + ChannelGroupEntity.COLUMN_FUNCTION
                  + ", G."
                  + ChannelGroupEntity.COLUMN_ONLINE
                  + " "
                  + ChannelGroupEntity.COLUMN_ONLINE
                  + ", G."
                  + ChannelGroupEntity.COLUMN_TOTAL_VALUE
                  + " "
                  + ChannelGroupEntity.COLUMN_TOTAL_VALUE
                  + ", G."
                  + ChannelGroupEntity.COLUMN_LOCATION_ID
                  + " "
                  + ChannelGroupEntity.COLUMN_LOCATION_ID
                  + ", G."
                  + ChannelGroupEntity.COLUMN_ALT_ICON
                  + " "
                  + ChannelGroupEntity.COLUMN_ALT_ICON
                  + ", G."
                  + ChannelGroupEntity.COLUMN_USER_ICON
                  + " "
                  + ChannelGroupEntity.COLUMN_USER_ICON
                  + ", G."
                  + ChannelGroupEntity.COLUMN_FLAGS
                  + " "
                  + ChannelGroupEntity.COLUMN_FLAGS
                  + " "
                  + ", G."
                  + ChannelGroupEntity.COLUMN_VISIBLE
                  + " "
                  + ChannelGroupEntity.COLUMN_VISIBLE
                  + ", G."
                  + ChannelGroupEntity.COLUMN_POSITION
                  + " "
                  + ChannelGroupEntity.COLUMN_POSITION
                  + ", I."
                  + UserIconEntity.COLUMN_IMAGE_1
                  + " "
                  + UserIconEntity.COLUMN_IMAGE_1
                  + ", I."
                  + UserIconEntity.COLUMN_IMAGE_2
                  + " "
                  + UserIconEntity.COLUMN_IMAGE_2
                  + ", I."
                  + UserIconEntity.COLUMN_IMAGE_3
                  + " "
                  + UserIconEntity.COLUMN_IMAGE_3
                  + ", I."
                  + UserIconEntity.COLUMN_IMAGE_4
                  + " "
                  + UserIconEntity.COLUMN_IMAGE_4
                  + ", G."
                  + ChannelGroupEntity.COLUMN_PROFILE_ID
                  + " "
                  + ChannelGroupEntity.COLUMN_PROFILE_ID
                  + " FROM "
                  + ChannelGroupEntity.TABLE_NAME
                  + " G"
                  + " JOIN "
                  + LocationEntity.TABLE_NAME
                  + " L"
                  + " ON (G."
                  + ChannelGroupEntity.COLUMN_LOCATION_ID
                  + " = L."
                  + LocationEntity.COLUMN_REMOTE_ID
                  + " AND G."
                  + ChannelGroupEntity.COLUMN_PROFILE_ID
                  + " = L."
                  + LocationEntity.COLUMN_PROFILE_ID
                  + ")"
                  + " LEFT JOIN "
                  + UserIconEntity.TABLE_NAME
                  + " I"
                  + " ON (G."
                  + ChannelGroupEntity.COLUMN_USER_ICON
                  + " = I."
                  + UserIconEntity.COLUMN_REMOTE_ID
                  + " AND G."
                  + ChannelGroupEntity.COLUMN_PROFILE_ID
                  + " = I."
                  + UserIconEntity.COLUMN_PROFILE_ID
                  + ")"
                  + " WHERE G."
                  + ChannelGroupEntity.COLUMN_VISIBLE
                  + " > 0"
                  + localWhere
                  + " ORDER BY "
                  + "L."
                  + LocationEntity.COLUMN_SORT_ORDER
                  + ", "
                  + "L."
                  + LocationEntity.COLUMN_CAPTION
                  + " COLLATE LOCALIZED, "
                  + "G."
                  + ChannelGroupEntity.COLUMN_POSITION
                  + ", "
                  + "G."
                  + ChannelGroupEntity.COLUMN_FUNCTION
                  + " DESC, "
                  + "G."
                  + ChannelGroupEntity.COLUMN_CAPTION;

          return sqLiteDatabase.rawQuery(sql, null);
        });
  }
}
