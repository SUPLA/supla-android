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

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.supla.android.data.source.local.entity.ChannelEntity;
import org.supla.android.data.source.local.entity.ChannelGroupEntity;
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity;
import org.supla.android.data.source.local.entity.ChannelValueEntity;
import org.supla.android.data.source.local.entity.LocationEntity;
import org.supla.android.data.source.local.entity.UserIconEntity;
import org.supla.android.data.source.local.view.ChannelView;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.ChannelGroupRelation;
import org.supla.android.db.Location;
import org.supla.android.lib.SuplaConst;

public class ChannelDao extends BaseDao {

  public ChannelDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
    super(databaseAccessProvider);
  }

  public Channel getChannel(int channelId) {
    return getItem(
        Channel::new,
        ChannelView.INSTANCE.getALL_COLUMNS(),
        ChannelView.NAME,
        key(ChannelView.COLUMN_CHANNEL_REMOTE_ID, channelId),
        key(ChannelValueEntity.COLUMN_PROFILE_ID, getCachedProfileId()));
  }

  public ChannelGroup getChannelGroup(int groupId) {
    String[] projection = {
      ChannelGroupEntity.COLUMN_ID,
      ChannelGroupEntity.COLUMN_REMOTE_ID,
      ChannelGroupEntity.COLUMN_CAPTION,
      ChannelGroupEntity.COLUMN_ONLINE,
      ChannelGroupEntity.COLUMN_FUNCTION,
      ChannelGroupEntity.COLUMN_VISIBLE,
      ChannelGroupEntity.COLUMN_LOCATION_ID,
      ChannelGroupEntity.COLUMN_ALT_ICON,
      ChannelGroupEntity.COLUMN_USER_ICON,
      ChannelGroupEntity.COLUMN_FLAGS,
      ChannelGroupEntity.COLUMN_TOTAL_VALUE,
      ChannelGroupEntity.COLUMN_POSITION,
      ChannelGroupEntity.COLUMN_PROFILE_ID
    };

    return getItem(
        ChannelGroup::new,
        projection,
        ChannelGroupEntity.TABLE_NAME,
        key(ChannelGroupEntity.COLUMN_REMOTE_ID, groupId),
        key(ChannelValueEntity.COLUMN_PROFILE_ID, getCachedProfileId()));
  }

  public ChannelGroupRelation getChannelGroupRelation(int channelId, int groupId) {
    String[] projection = {
      ChannelGroupRelationEntity.COLUMN_ID,
      ChannelGroupRelationEntity.COLUMN_GROUP_ID,
      ChannelGroupRelationEntity.COLUMN_CHANNEL_ID,
      ChannelGroupRelationEntity.COLUMN_VISIBLE,
      ChannelGroupRelationEntity.COLUMN_PROFILE_ID
    };

    return getItem(
        ChannelGroupRelation::new,
        projection,
        ChannelGroupRelationEntity.TABLE_NAME,
        key(ChannelGroupRelationEntity.COLUMN_GROUP_ID, groupId),
        key(ChannelGroupRelationEntity.COLUMN_CHANNEL_ID, channelId),
        key(ChannelGroupRelationEntity.COLUMN_PROFILE_ID, getCachedProfileId()));
  }

  public void insert(ChannelGroup channelGroup) {
    channelGroup.setProfileId(getCachedProfileId());
    insert(channelGroup, ChannelGroupEntity.TABLE_NAME);
  }

  public void update(ChannelGroup channelGroup) {
    update(
        channelGroup,
        ChannelGroupEntity.TABLE_NAME,
        key(ChannelGroupEntity.COLUMN_ID, channelGroup.getId()),
        key(ChannelGroupEntity.COLUMN_PROFILE_ID, channelGroup.getProfileId()));
  }

  public void insert(ChannelGroupRelation channelGroupRelation) {
    channelGroupRelation.setProfileId(getCachedProfileId());
    insert(channelGroupRelation, ChannelGroupRelationEntity.TABLE_NAME);
  }

  public void update(ChannelGroupRelation channelGroupRelation) {
    update(
        channelGroupRelation,
        ChannelGroupRelationEntity.TABLE_NAME,
        key(ChannelGroupRelationEntity.COLUMN_ID, channelGroupRelation.getId()),
        key(ChannelValueEntity.COLUMN_PROFILE_ID, channelGroupRelation.getProfileId()));
  }

  public int getChannelCount() {
    return getCount(
        ChannelEntity.TABLE_NAME, null, key(ChannelEntity.COLUMN_PROFILE_ID, getCachedProfileId()));
  }

  public boolean setChannelsVisible(int visible, int whereVisible) {
    return setVisible(
        ChannelEntity.TABLE_NAME, visible, key(ChannelEntity.COLUMN_VISIBLE, whereVisible));
  }

  public boolean setChannelGroupsVisible(int visible, int whereVisible) {
    return setVisible(
        ChannelGroupEntity.TABLE_NAME,
        visible,
        key(ChannelGroupEntity.COLUMN_VISIBLE, whereVisible));
  }

  public boolean setChannelGroupRelationsVisible(int visible, int whereVisible) {
    return setVisible(
        ChannelGroupRelationEntity.TABLE_NAME,
        visible,
        key(ChannelGroupRelationEntity.COLUMN_VISIBLE, whereVisible));
  }

  public boolean setChannelsOffline() {
    String selection = ChannelValueEntity.COLUMN_ONLINE + " = ?";
    String[] selectionArgs = {String.valueOf(1)};

    ContentValues values = new ContentValues();
    values.put(ChannelValueEntity.COLUMN_ONLINE, 0);

    return write(
            sqLiteDatabase -> {
              return sqLiteDatabase.update(
                  ChannelValueEntity.TABLE_NAME, values, selection, selectionArgs);
            })
        > 0;
  }

  public Cursor getChannelListCursorWithDefaultOrder(String where) {
    where +=
        " AND (C." + ChannelView.COLUMN_CHANNEL_PROFILE_ID + " = " + getCachedProfileId() + ") ";

    String orderBY =
        "L."
            + LocationEntity.COLUMN_SORT_ORDER
            + ", "
            + "L."
            + LocationEntity.COLUMN_CAPTION
            + " COLLATE LOCALIZED, "
            + "C."
            + ChannelEntity.COLUMN_POSITION
            + ", "
            + "C."
            + ChannelView.COLUMN_CHANNEL_FUNCTION
            + " DESC, "
            + "C."
            + ChannelView.COLUMN_CHANNEL_CAPTION
            + " COLLATE LOCALIZED";

    return getChannelListCursor(orderBY, where);
  }

  public boolean isZWaveBridgeChannelAvailable() {
    String[] projection = {ChannelView.COLUMN_CHANNEL_ID};

    String selection =
        ChannelView.COLUMN_CHANNEL_PROFILE_ID
            + " = ? "
            + " AND "
            + ChannelView.COLUMN_CHANNEL_TYPE
            + " = ?"
            + " AND "
            + ChannelView.COLUMN_CHANNEL_VISIBLE
            + " > 0"
            + " AND ("
            + ChannelView.COLUMN_CHANNEL_FLAGS
            + " & ?) > 0";

    String[] selectionArgs = {
      String.valueOf(getCachedProfileId()),
      String.valueOf(SuplaConst.SUPLA_CHANNELTYPE_BRIDGE),
      String.valueOf(SuplaConst.SUPLA_CHANNEL_FLAG_ZWAVE_BRIDGE)
    };

    return read(
        sqLiteDatabase -> {
          try (Cursor cursor =
              sqLiteDatabase.query(
                  ChannelView.NAME, projection, selection, selectionArgs, null, null, null, "1")) {
            return cursor.getCount() > 0;
          }
        });
  }

  public List<Channel> getZWaveBridgeChannels() {
    String conditions =
        ChannelView.COLUMN_CHANNEL_TYPE
            + " = "
            + SuplaConst.SUPLA_CHANNELTYPE_BRIDGE
            + " AND ("
            + ChannelView.COLUMN_CHANNEL_FLAGS
            + " & "
            + SuplaConst.SUPLA_CHANNEL_FLAG_ZWAVE_BRIDGE
            + " ) > 0 "
            + " AND (C."
            + ChannelView.COLUMN_CHANNEL_PROFILE_ID
            + " = "
            + getCachedProfileId()
            + ")";

    String orderBy =
        "C." + ChannelView.COLUMN_CHANNEL_DEVICE_ID + ", " + "C." + ChannelView.COLUMN_CHANNEL_ID;

    ArrayList<Channel> result = new ArrayList<>();
    try (Cursor cursor = getChannelListCursor(orderBy, conditions)) {
      if (cursor.moveToFirst()) {
        do {
          Channel channel = new Channel();
          channel.AssignCursorData(cursor);
          result.add(channel);
        } while (cursor.moveToNext());
      }
    }

    return result;
  }

  public Cursor getSortedChannelIdsForLocationCursor(String locationCaption) {
    return getChannelListCursorWithDefaultOrder(
        "L."
            + LocationEntity.COLUMN_CAPTION
            + " = "
            + DatabaseUtils.sqlEscapeString(locationCaption));
  }

  public Cursor getSortedChannelGroupIdsForLocationCursor(String locationCaption) {
    String where =
        "G."
            + ChannelGroupEntity.COLUMN_PROFILE_ID
            + " = "
            + getCachedProfileId()
            + " AND L."
            + LocationEntity.COLUMN_CAPTION
            + " = "
            + DatabaseUtils.sqlEscapeString(locationCaption);
    return getChannelGroupListCursor(where);
  }

  public void updateChannelsOrder(List<Long> reorderedIds, int locationId) {
    write(
        sqLiteDatabase -> {
          sqLiteDatabase.beginTransaction();
          try {
            sqLiteDatabase.execSQL(
                "UPDATE "
                    + LocationEntity.TABLE_NAME
                    + " SET "
                    + LocationEntity.COLUMN_SORTING
                    + " = '"
                    + Location.SortingType.USER_DEFINED.name()
                    + "' WHERE "
                    + LocationEntity.COLUMN_REMOTE_ID
                    + " = "
                    + locationId);

            int position = 1;
            for (Long id : reorderedIds) {
              sqLiteDatabase.execSQL(
                  "UPDATE "
                      + ChannelEntity.TABLE_NAME
                      + " SET "
                      + ChannelEntity.COLUMN_POSITION
                      + " = "
                      + position
                      + " WHERE "
                      + ChannelEntity.COLUMN_ID
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

  @SuppressLint("Range")
  public List<Integer> getChannelUserIconIdsToDownload() {
    String sql =
        "SELECT C."
            + ChannelEntity.COLUMN_USER_ICON
            + " "
            + ChannelEntity.COLUMN_USER_ICON
            + " FROM "
            + ChannelEntity.TABLE_NAME
            + " AS C"
            + " LEFT JOIN "
            + UserIconEntity.TABLE_NAME
            + " AS U ON (C."
            + ChannelEntity.COLUMN_USER_ICON
            + " = "
            + "U."
            + UserIconEntity.COLUMN_REMOTE_ID
            + " AND "
            + "C."
            + ChannelEntity.COLUMN_PROFILE_ID
            + " = "
            + "U."
            + UserIconEntity.COLUMN_PROFILE_ID
            + ")"
            + " WHERE "
            + ChannelEntity.COLUMN_VISIBLE
            + " > 0 AND "
            + ChannelEntity.COLUMN_USER_ICON
            + " > 0 AND U."
            + UserIconEntity.COLUMN_REMOTE_ID
            + " IS NULL"
            + " AND (C."
            + ChannelEntity.COLUMN_PROFILE_ID
            + " = "
            + getCachedProfileId()
            + ")";

    ArrayList<Integer> ids = new ArrayList<>();
    try (Cursor cursor = read(sqLiteDatabase -> sqLiteDatabase.rawQuery(sql, null))) {
      if (cursor.moveToFirst()) {
        do {
          Integer id = cursor.getInt(cursor.getColumnIndex(ChannelEntity.COLUMN_USER_ICON));
          if (!ids.contains(id)) {
            ids.add(id);
          }
        } while (cursor.moveToNext());
      }
    }

    return ids;
  }

  @SuppressLint("Range")
  public List<Integer> getChannelGroupUserIconIdsToDownload() {
    String sql =
        "SELECT C."
            + ChannelGroupEntity.COLUMN_USER_ICON
            + " "
            + ChannelGroupEntity.COLUMN_USER_ICON
            + " FROM "
            + ChannelGroupEntity.TABLE_NAME
            + " AS C"
            + " LEFT JOIN "
            + UserIconEntity.TABLE_NAME
            + " AS U ON (C."
            + ChannelGroupEntity.COLUMN_USER_ICON
            + " = "
            + "U."
            + UserIconEntity.COLUMN_REMOTE_ID
            + " AND "
            + "C."
            + ChannelGroupEntity.COLUMN_PROFILE_ID
            + " = "
            + "U."
            + UserIconEntity.COLUMN_PROFILE_ID
            + ")"
            + " WHERE "
            + ChannelGroupEntity.COLUMN_VISIBLE
            + " > 0 AND "
            + ChannelGroupEntity.COLUMN_USER_ICON
            + " > 0 AND U."
            + UserIconEntity.COLUMN_REMOTE_ID
            + " IS NULL"
            + " AND (C."
            + ChannelGroupEntity.COLUMN_PROFILE_ID
            + " = "
            + getCachedProfileId()
            + ")";

    ArrayList<Integer> ids = new ArrayList<>();
    try (Cursor cursor = read(sqLiteDatabase -> sqLiteDatabase.rawQuery(sql, null))) {
      if (cursor.moveToFirst()) {
        do {
          Integer id = cursor.getInt(cursor.getColumnIndex(ChannelEntity.COLUMN_USER_ICON));
          if (!ids.contains(id)) {
            ids.add(id);
          }
        } while (cursor.moveToNext());
      }
    }

    return ids;
  }

  @SuppressLint("Range")
  public int getChannelGroupLastPositionInLocation(int locationId) {
    String where =
        "G."
            + ChannelGroupEntity.COLUMN_PROFILE_ID
            + " = "
            + getCachedProfileId()
            + " AND G."
            + ChannelGroupEntity.COLUMN_LOCATION_ID
            + " = "
            + locationId;
    Cursor cursor = getChannelGroupListCursor(where);
    if (!cursor.moveToFirst()) {
      throw new NoSuchElementException();
    }
    if (cursor.moveToLast()) {
      return cursor.getInt(cursor.getColumnIndex(ChannelGroupEntity.COLUMN_POSITION));
    }
    return 0;
  }

  private Cursor getChannelListCursor(@NonNull String orderBy, @Nullable String where) {
    return read(
        sqLiteDatabase -> {
          String localWhere = "";
          if (where != null) {
            localWhere = " AND (" + where + ")";
          }

          String sql =
              "SELECT "
                  + "C."
                  + ChannelView.COLUMN_CHANNEL_ID
                  + " "
                  + ChannelView.COLUMN_CHANNEL_ID
                  + ", L."
                  + LocationEntity.COLUMN_CAPTION
                  + " AS section"
                  + ", L."
                  + LocationEntity.COLUMN_COLLAPSED
                  + " "
                  + LocationEntity.COLUMN_COLLAPSED
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_DEVICE_ID
                  + " "
                  + ChannelView.COLUMN_CHANNEL_DEVICE_ID
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_REMOTE_ID
                  + " "
                  + ChannelView.COLUMN_CHANNEL_REMOTE_ID
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_CAPTION
                  + " "
                  + ChannelView.COLUMN_CHANNEL_CAPTION
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_TYPE
                  + " "
                  + ChannelView.COLUMN_CHANNEL_TYPE
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_FUNCTION
                  + " "
                  + ChannelView.COLUMN_CHANNEL_FUNCTION
                  + ", C."
                  + ChannelView.COLUMN_VALUE_ID
                  + " "
                  + ChannelView.COLUMN_VALUE_ID
                  + ", C."
                  + ChannelView.COLUMN_EXTENDED_VALUE_ID
                  + " "
                  + ChannelView.COLUMN_EXTENDED_VALUE_ID
                  + ", C."
                  + ChannelView.COLUMN_VALUE_ONLINE
                  + " "
                  + ChannelView.COLUMN_VALUE_ONLINE
                  + ", C."
                  + ChannelView.COLUMN_VALUE_SUB_VALUE
                  + " "
                  + ChannelView.COLUMN_VALUE_SUB_VALUE
                  + ", C."
                  + ChannelView.COLUMN_VALUE_SUB_VALUE_TYPE
                  + " "
                  + ChannelView.COLUMN_VALUE_SUB_VALUE_TYPE
                  + ", C."
                  + ChannelView.COLUMN_VALUE_VALUE
                  + " "
                  + ChannelView.COLUMN_VALUE_VALUE
                  + ", C."
                  + ChannelView.COLUMN_EXTENDED_VALUE_VALUE
                  + " "
                  + ChannelView.COLUMN_EXTENDED_VALUE_VALUE
                  + ", C."
                  + ChannelView.COLUMN_EXTENDED_VALUE_TIMER_START_TIME
                  + " "
                  + ChannelView.COLUMN_EXTENDED_VALUE_TIMER_START_TIME
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_VISIBLE
                  + " "
                  + ChannelView.COLUMN_CHANNEL_VISIBLE
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_LOCATION_ID
                  + " "
                  + ChannelView.COLUMN_CHANNEL_LOCATION_ID
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_ALT_ICON
                  + " "
                  + ChannelView.COLUMN_CHANNEL_ALT_ICON
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_USER_ICON
                  + " "
                  + ChannelView.COLUMN_CHANNEL_USER_ICON
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_MANUFACTURER_ID
                  + " "
                  + ChannelView.COLUMN_CHANNEL_MANUFACTURER_ID
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_PRODUCT_ID
                  + " "
                  + ChannelView.COLUMN_CHANNEL_PRODUCT_ID
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_FLAGS
                  + " "
                  + ChannelView.COLUMN_CHANNEL_FLAGS
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_PROTOCOL_VERSION
                  + " "
                  + ChannelView.COLUMN_CHANNEL_PROTOCOL_VERSION
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_POSITION
                  + " "
                  + ChannelView.COLUMN_CHANNEL_POSITION
                  + ", C."
                  + ChannelView.COLUMN_USER_ICON_IMAGE_1
                  + " "
                  + ChannelView.COLUMN_USER_ICON_IMAGE_1
                  + ", C."
                  + ChannelView.COLUMN_USER_ICON_IMAGE_2
                  + " "
                  + ChannelView.COLUMN_USER_ICON_IMAGE_2
                  + ", C."
                  + ChannelView.COLUMN_USER_ICON_IMAGE_3
                  + " "
                  + ChannelView.COLUMN_USER_ICON_IMAGE_3
                  + ", C."
                  + ChannelView.COLUMN_USER_ICON_IMAGE_4
                  + " "
                  + ChannelView.COLUMN_USER_ICON_IMAGE_4
                  + " "
                  + ", C."
                  + ChannelView.COLUMN_CHANNEL_PROFILE_ID
                  + " "
                  + ChannelView.COLUMN_CHANNEL_PROFILE_ID
                  + " FROM "
                  + ChannelView.NAME
                  + " C"
                  + " JOIN "
                  + LocationEntity.TABLE_NAME
                  + " L"
                  + " ON (C."
                  + ChannelView.COLUMN_CHANNEL_LOCATION_ID
                  + " = L."
                  + LocationEntity.COLUMN_REMOTE_ID
                  + " AND C."
                  + ChannelView.COLUMN_CHANNEL_PROFILE_ID
                  + " = L."
                  + LocationEntity.COLUMN_PROFILE_ID
                  + ")"
                  + " WHERE C."
                  + ChannelView.COLUMN_CHANNEL_VISIBLE
                  + " > 0 "
                  + localWhere
                  + " ORDER BY "
                  + orderBy
                  + " COLLATE LOCALIZED ASC"; // For proper ordering of language special characters
          // like ą, ł, ü, ö
          return sqLiteDatabase.rawQuery(sql, null);
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

  private boolean setVisible(String table, int visible, Key<Integer> key) {
    String selection = key.asSelection() + " AND " + ChannelEntity.COLUMN_PROFILE_ID + " = ?";
    String[] selectionArgs = {String.valueOf(key.value), String.valueOf(getCachedProfileId())};

    ContentValues values = new ContentValues();
    values.put(key.column, visible);

    return write(
            sqLiteDatabase -> {
              return sqLiteDatabase.update(table, values, selection, selectionArgs);
            })
        > 0;
  }
}
