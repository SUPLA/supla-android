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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.annotation.SuppressLint;

import org.supla.android.db.Channel;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.ChannelGroupRelation;
import org.supla.android.db.ChannelValue;
import org.supla.android.db.Location;
import org.supla.android.db.SuplaContract;
import org.supla.android.lib.SuplaConst;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ChannelDao extends BaseDao {
    public ChannelDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
        super(databaseAccessProvider);
    }

    public Channel getChannel(int channelId) {
        String[] projection = {
                SuplaContract.ChannelViewEntry._ID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_DEVICEID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_TYPE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUEID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUEID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_ONLINE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE_TYPE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_LOCATIONID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_ALTICON,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_MANUFACTURERID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_PRODUCTID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_FLAGS,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_PROTOCOLVERSION,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_POSITION,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE1,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE2,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE3,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE4,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_PROFILEID
        };

        return getItem(Channel::new, projection, SuplaContract.ChannelViewEntry.VIEW_NAME,
                key(SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID, channelId),
                key(SuplaContract.ChannelValueEntry.COLUMN_NAME_PROFILEID, getCachedProfileId()));
    }

    public ChannelValue getChannelValue(int channelId) {
        String[] projection = {
                SuplaContract.ChannelValueEntry._ID,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE_TYPE,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_VALUE,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_PROFILEID
        };

        return getItem(ChannelValue::new, projection, SuplaContract.ChannelValueEntry.TABLE_NAME,
                key(SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID, channelId),
                key(SuplaContract.ChannelValueEntry.COLUMN_NAME_PROFILEID, getCachedProfileId()));
    }

    public ChannelGroup getChannelGroup(int groupId) {
        String[] projection = {
                SuplaContract.ChannelGroupEntry._ID,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_ONLINE,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_ALTICON,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_FLAGS,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_TOTALVALUE,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_POSITION,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_PROFILEID
        };

        return getItem(ChannelGroup::new, projection, SuplaContract.ChannelGroupEntry.TABLE_NAME,
                key(SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID, groupId),
                key(SuplaContract.ChannelValueEntry.COLUMN_NAME_PROFILEID, getCachedProfileId()));
    }

    public ChannelGroupRelation getChannelGroupRelation(int channelId, int groupId) {
        String[] projection = {
                SuplaContract.ChannelGroupRelationEntry._ID,
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_GROUPID,
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_VISIBLE,
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_PROFILEID
        };

        return getItem(ChannelGroupRelation::new, projection, SuplaContract.ChannelGroupRelationEntry.TABLE_NAME,
                key(SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_GROUPID, groupId),
                key(SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID, channelId),
                key(SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_PROFILEID, getCachedProfileId()));
    }

    public ChannelExtendedValue getChannelExtendedValue(int channelId) {
        String[] projection = {
                SuplaContract.ChannelExtendedValueEntry._ID,
                SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_VALUE,
                SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_PROFILEID
        };

        return getItem(ChannelExtendedValue::new, projection, SuplaContract.ChannelExtendedValueEntry.TABLE_NAME,
                key(SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_CHANNELID, channelId),
                key(SuplaContract.ChannelValueEntry.COLUMN_NAME_PROFILEID, getCachedProfileId()));
    }

    public Cursor getChannelGroupValueViewEntryCursor() {
        return read(sqLiteDatabase -> {
            String[] projection = {
                    SuplaContract.ChannelGroupValueViewEntry._ID,
                    SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_FUNC,
                    SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_GROUPID,
                    SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_CHANNELID,
                    SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_ONLINE,
                    SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_SUBVALUE_TYPE,
                    SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_SUBVALUE,
                    SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_VALUE,
                    SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_PROFILEID
            };

            String selection = SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_PROFILEID
                    + " = ?";

            String[] selectionArgs = {
                    String.valueOf(getCachedProfileId())
            };

            return sqLiteDatabase.query(SuplaContract.ChannelGroupValueViewEntry.VIEW_NAME,
                    projection, selection, selectionArgs, null, null,
                    SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_GROUPID
            );
        });
    }

    public void insert(Channel channel) {
        if(channel.getProfileId() == -1) {
            channel.setProfileId(getCachedProfileId());
        }
        insert(channel, SuplaContract.ChannelEntry.TABLE_NAME);
    }

    public void update(Channel channel) {
        update(channel, SuplaContract.ChannelEntry.TABLE_NAME, key(SuplaContract.ChannelEntry._ID, channel.getId()),
               key(SuplaContract.ChannelEntry.COLUMN_NAME_PROFILEID, channel.getProfileId()));
    }

    public void insert(ChannelValue channelValue) {
        channelValue.setProfileId(getCachedProfileId());
        insert(channelValue, SuplaContract.ChannelValueEntry.TABLE_NAME);
    }

    public void update(ChannelValue channelValue) {
        update(channelValue, SuplaContract.ChannelValueEntry.TABLE_NAME,
                key(SuplaContract.ChannelValueEntry._ID, channelValue.getId()),
                key(SuplaContract.ChannelValueEntry.COLUMN_NAME_PROFILEID, channelValue.getProfileId()));
    }

    public void insert(ChannelExtendedValue channelExtendedValue) {
        channelExtendedValue.setProfileId(getCachedProfileId());
        insert(channelExtendedValue, SuplaContract.ChannelExtendedValueEntry.TABLE_NAME);
    }

    public void update(ChannelExtendedValue channelExtendedValue) {
        update(channelExtendedValue, SuplaContract.ChannelExtendedValueEntry.TABLE_NAME,
                key(SuplaContract.ChannelExtendedValueEntry._ID, channelExtendedValue.getId()),
                key(SuplaContract.ChannelValueEntry.COLUMN_NAME_PROFILEID, channelExtendedValue.getProfileId()));
    }

    public void insert(ChannelGroup channelGroup) {
        channelGroup.setProfileId(getCachedProfileId());
        insert(channelGroup, SuplaContract.ChannelGroupEntry.TABLE_NAME);
    }

    public void update(ChannelGroup channelGroup) {
        update(channelGroup, SuplaContract.ChannelGroupEntry.TABLE_NAME,
                key(SuplaContract.ChannelGroupEntry._ID, channelGroup.getId()),
                key(SuplaContract.ChannelGroupEntry.COLUMN_NAME_PROFILEID, channelGroup.getProfileId()));
    }

    public void insert(ChannelGroupRelation channelGroupRelation) {
        channelGroupRelation.setProfileId(getCachedProfileId());
        insert(channelGroupRelation, SuplaContract.ChannelGroupRelationEntry.TABLE_NAME);
    }

    public void update(ChannelGroupRelation channelGroupRelation) {
        update(channelGroupRelation, SuplaContract.ChannelGroupRelationEntry.TABLE_NAME,
                key(SuplaContract.ChannelGroupRelationEntry._ID, channelGroupRelation.getId()),
                key(SuplaContract.ChannelValueEntry.COLUMN_NAME_PROFILEID, channelGroupRelation.getProfileId()));
    }

    public int getChannelCountForLocation(int locationId) {
        return getChannelCount(key(SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID, locationId));
    }

    public int getChannelCount() {
        return getChannelCount(null);
    }

    public boolean setChannelsVisible(int visible, int whereVisible) {
        return setVisible(SuplaContract.ChannelEntry.TABLE_NAME, visible,
                key(SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE, whereVisible));
    }

    public boolean setChannelGroupsVisible(int visible, int whereVisible) {
        return setVisible(SuplaContract.ChannelGroupEntry.TABLE_NAME, visible,
                key(SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE, whereVisible));
    }

    public boolean setChannelGroupRelationsVisible(int visible, int whereVisible) {
        return setVisible(SuplaContract.ChannelGroupRelationEntry.TABLE_NAME, visible,
                key(SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_VISIBLE, whereVisible));
    }

    public boolean setChannelsOffline() {
        String selection = SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE + " = ?";
        String[] selectionArgs = {String.valueOf(1)};

        ContentValues values = new ContentValues();
        values.put(SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE, 0);

        return write(sqLiteDatabase -> {
            return sqLiteDatabase.update(
                    SuplaContract.ChannelValueEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
        }) > 0;
    }

    public Cursor getChannelListCursorWithDefaultOrder(String where) {
        where += " AND (C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_PROFILEID + " = "
                + getCachedProfileId() + ") ";

        String orderBY = "L." + SuplaContract.LocationEntry.COLUMN_NAME_SORT_ORDER + ", "
                + "L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + " COLLATE LOCALIZED, "
                + "C." + SuplaContract.ChannelEntry.COLUMN_NAME_POSITION + ", "
                + "C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC + " DESC, "
                + "C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION + " COLLATE LOCALIZED";

        return getChannelListCursor(orderBY, where);
    }

    public Cursor getAllChannels(String where) {
        String orderBY = "L." + SuplaContract.LocationEntry.COLUMN_NAME_SORT_ORDER + ", "
                + "L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + " COLLATE LOCALIZED, "
                + "C." + SuplaContract.ChannelEntry.COLUMN_NAME_POSITION + ", "
                + "C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC + " DESC, "
                + "C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION + " COLLATE LOCALIZED";

        return getChannelListCursor(orderBY, where);
    }

    public Cursor getChannelGroupListCursor() {
        return getChannelGroupListCursor(null);
    }

    public boolean isZWaveBridgeChannelAvailable() {
        String[] projection = {
                SuplaContract.ChannelViewEntry._ID
        };

        String selection = SuplaContract.ChannelViewEntry.COLUMN_NAME_PROFILEID
                + " = ? "
                + " AND "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_TYPE
                + " = ?"
                + " AND "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE + " > 0"
                + " AND ("
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_FLAGS + " & ?) > 0";

        String[] selectionArgs = {
                String.valueOf(getCachedProfileId()),
                String.valueOf(SuplaConst.SUPLA_CHANNELTYPE_BRIDGE),
                String.valueOf(SuplaConst.SUPLA_CHANNEL_FLAG_ZWAVE_BRIDGE)
        };

        return read(sqLiteDatabase -> {
            try (Cursor cursor = sqLiteDatabase.query(SuplaContract.ChannelViewEntry.VIEW_NAME,
                    projection, selection, selectionArgs, null, null, null, "1")) {
                return cursor.getCount() > 0;
            }
        });
    }

    public List<Channel> getZWaveBridgeChannels() {
        String conditions =
                SuplaContract.ChannelViewEntry.COLUMN_NAME_TYPE
                        + " = " + SuplaConst.SUPLA_CHANNELTYPE_BRIDGE
                        + " AND ("
                        + SuplaContract.ChannelViewEntry.COLUMN_NAME_FLAGS
                        + " & " + SuplaConst.SUPLA_CHANNEL_FLAG_ZWAVE_BRIDGE
                        + " ) > 0 "
                        + " AND (C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_PROFILEID + " = "
                        + getCachedProfileId() + ")";

        String orderBy = "C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_DEVICEID + ", "
                + "C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID;

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

    public Cursor getSortedChannelIdsForLocationCursor(int locationId) {
        return getChannelListCursorWithDefaultOrder("C." + SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID + " = " + locationId);
    }

    public Cursor getSortedChannelGroupIdsForLocationCursor(int locationId) {
        return getChannelGroupListCursor("G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID + " = " + locationId);
    }

    public void updateChannelsOrder(List<Long> reorderedIds, int locationId) {
        write(sqLiteDatabase -> {
            sqLiteDatabase.beginTransaction();
            try {
                sqLiteDatabase.execSQL("UPDATE " + SuplaContract.LocationEntry.TABLE_NAME +
                        " SET " + SuplaContract.LocationEntry.COLUMN_NAME_SORTING + " = '" + Location.SortingType.USER_DEFINED.name() +
                        "' WHERE " + SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID + " = " + locationId);

                int position = 1;
                for (Long id : reorderedIds) {
                    sqLiteDatabase.execSQL("UPDATE " + SuplaContract.ChannelEntry.TABLE_NAME +
                            " SET " + SuplaContract.ChannelEntry.COLUMN_NAME_POSITION + " = " + position +
                            " WHERE " + SuplaContract.ChannelEntry._ID + " = " + id);
                    position++;
                }
                sqLiteDatabase.setTransactionSuccessful();
            } finally {
                sqLiteDatabase.endTransaction();
            }
        });
    }

    public void updateChannelGroupsOrder(List<Long> reorderedIds) {
        write(sqLiteDatabase -> {
            sqLiteDatabase.beginTransaction();
            try {
                int position = 1;
                for (Long id : reorderedIds) {
                    sqLiteDatabase.execSQL("UPDATE " + SuplaContract.ChannelGroupEntry.TABLE_NAME +
                            " SET " + SuplaContract.ChannelGroupEntry.COLUMN_NAME_POSITION + " = " + position +
                            " WHERE " + SuplaContract.ChannelGroupEntry._ID + " = " + id);
                    position++;
                }
                sqLiteDatabase.setTransactionSuccessful();
            } finally {
                sqLiteDatabase.endTransaction();
            }
        });
    }

    @SuppressLint("Range")
    public List<Integer> getChannelUserIconIds() {
        String sql = "SELECT C." + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON
                + " " + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON
                + " FROM " + SuplaContract.ChannelEntry.TABLE_NAME + " AS C"
                + " LEFT JOIN " + SuplaContract.UserIconsEntry.TABLE_NAME + " AS U ON (C."
                + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON + " = "
                + "U." + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
                + " AND "
                + "C." + SuplaContract.ChannelEntry.COLUMN_NAME_PROFILEID + " = "
                + "U." + SuplaContract.UserIconsEntry.COLUMN_NAME_PROFILEID + ")"
                + " WHERE " + SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE +
                " > 0 AND " + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON +
                " > 0 AND U." + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID 
                + " IS NULL"
                + " AND (C." + SuplaContract.ChannelValueEntry.COLUMN_NAME_PROFILEID + " = "
                + getCachedProfileId() + ")";

        ArrayList<Integer> ids = new ArrayList<>();
        try (Cursor cursor = read(sqLiteDatabase -> sqLiteDatabase.rawQuery(sql, null))) {
            if (cursor.moveToFirst()) {
                do {
                    Integer id = cursor.getInt(
                            cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_USERICON));
                    if (!ids.contains(id)) {
                        ids.add(id);
                    }
                } while (cursor.moveToNext());
            }
        }

        return ids;
    }

    @SuppressLint("Range")
    public List<Integer> getChannelGroupUserIconIds() {
        String sql = "SELECT C." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON
                + " " + SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON
                + " FROM " + SuplaContract.ChannelGroupEntry.TABLE_NAME + " AS C"
                + " LEFT JOIN " + SuplaContract.UserIconsEntry.TABLE_NAME + " AS U ON (C."
                + SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON + " = "
                + "U." + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
                + " AND "
                + "C." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_PROFILEID + " = "
                + "U." + SuplaContract.UserIconsEntry.COLUMN_NAME_PROFILEID + ")"
                + " WHERE " + SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE +
                " > 0 AND " + SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON +
                " > 0 AND U." + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
                + " IS NULL"
                + " AND (C." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_PROFILEID + " = "
                + getCachedProfileId() + ")";

        ArrayList<Integer> ids = new ArrayList<>();
        try (Cursor cursor = read(sqLiteDatabase -> sqLiteDatabase.rawQuery(sql, null))) {
            if (cursor.moveToFirst()) {
                do {
                    Integer id = cursor.getInt(
                            cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_USERICON));
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
        Cursor cursor = getChannelGroupListCursor("G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID + " = " + locationId);
        if (!cursor.moveToFirst()) {
            throw new NoSuchElementException();
        }
        if (cursor.moveToLast()) {
            return cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_POSITION));
        }
        return 0;
    }

    private Cursor getChannelListCursor(@NonNull String orderBy, @Nullable String where) {
        return read(sqLiteDatabase -> {
            String localWhere = "";
            if (where != null) {
                localWhere = " AND (" + where + ")";
            }

            String sql = "SELECT "
                    + "C." + SuplaContract.ChannelViewEntry._ID + " "
                    + SuplaContract.ChannelViewEntry._ID
                    + ", L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + " AS section"
                    + ", L." + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED + " "
                    + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_DEVICEID + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_DEVICEID
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_TYPE + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_TYPE
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUEID + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUEID
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUEID + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUEID
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_ONLINE + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_ONLINE
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE_TYPE + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE_TYPE
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUE + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUE
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUE + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUE
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_LOCATIONID + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_LOCATIONID
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_ALTICON + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_ALTICON
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_MANUFACTURERID + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_MANUFACTURERID
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_PRODUCTID + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_PRODUCTID
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_FLAGS + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_FLAGS
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_PROTOCOLVERSION + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_PROTOCOLVERSION
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_POSITION + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_POSITION
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE1 + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE1
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE2 + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE2
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE3 + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE3
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE4 + " "
                    + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE4 + " "
                    + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_PROFILEID + " " + SuplaContract.ChannelViewEntry.COLUMN_NAME_PROFILEID

                    + " FROM " + SuplaContract.ChannelViewEntry.VIEW_NAME + " C"
                    + " JOIN " + SuplaContract.LocationEntry.TABLE_NAME + " L"
                    + " ON (C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_LOCATIONID + " = L."
                    + SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID
                    + " AND C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_PROFILEID + " = L."
                    + SuplaContract.LocationEntry.COLUMN_NAME_PROFILEID + ")"
                    + " WHERE C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE + " > 0 "
                    + localWhere
                    + " ORDER BY " + orderBy + " COLLATE LOCALIZED ASC"; // For proper ordering of language special characters like ą, ł, ü, ö
            return sqLiteDatabase.rawQuery(sql, null);
        });
    }

    private Cursor getChannelGroupListCursor(@Nullable String where) {
        return read(sqLiteDatabase -> {
            String localWhere = "";
            if (where != null) {
                localWhere = " AND (" + where + ")";
            }

            String sql = "SELECT "
                    + "G." + SuplaContract.ChannelGroupEntry._ID + " "
                    + SuplaContract.ChannelGroupEntry._ID
                    + ", L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + " AS section"
                    + ", L." + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED + " "
                    + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED
                    + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID + " "
                    + SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID
                    + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION + " "
                    + SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION
                    + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC + " "
                    + SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC
                    + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_ONLINE + " "
                    + SuplaContract.ChannelGroupEntry.COLUMN_NAME_ONLINE
                    + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_TOTALVALUE + " "
                    + SuplaContract.ChannelGroupEntry.COLUMN_NAME_TOTALVALUE
                    + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID + " "
                    + SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID
                    + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_ALTICON + " "
                    + SuplaContract.ChannelGroupEntry.COLUMN_NAME_ALTICON
                    + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON + " "
                    + SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON
                    + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_FLAGS + " "
                    + SuplaContract.ChannelGroupEntry.COLUMN_NAME_FLAGS + " "
                    + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE + " "
                    + SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE
                    + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_POSITION + " "
                    + SuplaContract.ChannelGroupEntry.COLUMN_NAME_POSITION
                    + ", I." + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1 + " "
                    + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1
                    + ", I." + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2 + " "
                    + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2
                    + ", I." + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3 + " "
                    + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3
                    + ", I." + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4 + " "
                    + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4
                    + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_PROFILEID + " " + SuplaContract.ChannelGroupEntry.COLUMN_NAME_PROFILEID

                    + " FROM " + SuplaContract.ChannelGroupEntry.TABLE_NAME + " G"
                    + " JOIN " + SuplaContract.LocationEntry.TABLE_NAME + " L"
                    + " ON (G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID + " = L."
                    + SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID
                    + " AND G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_PROFILEID + " = L."
                    + SuplaContract.LocationEntry.COLUMN_NAME_PROFILEID + ")"
                    + " LEFT JOIN " + SuplaContract.UserIconsEntry.TABLE_NAME + " I"
                    + " ON (G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON + " = I."
                    + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
                    + " AND G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_PROFILEID + " = I."
                    + SuplaContract .UserIconsEntry.COLUMN_NAME_PROFILEID + ")"
                    + " WHERE G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE + " > 0"
                    + " AND G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_PROFILEID + " = " + getCachedProfileId()
                    + localWhere
                    + " ORDER BY " + "L." + SuplaContract.LocationEntry.COLUMN_NAME_SORT_ORDER + ", "
                    + "L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + " COLLATE LOCALIZED, "
                    + "G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_POSITION + ", "
                    + "G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC + " DESC, "
                    + "G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION;


            return sqLiteDatabase.rawQuery(sql, null);
        });
    }

    private int getChannelCount(@Nullable Key<?> key) {
        return getCount(SuplaContract.ChannelEntry.TABLE_NAME, key,
                        key(SuplaContract.ChannelEntry.COLUMN_NAME_PROFILEID, getCachedProfileId()));
    }

    private boolean setVisible(String table, int visible, Key<Integer> key) {
        String selection = key.asSelection() + " AND " + SuplaContract.ChannelEntry.COLUMN_NAME_PROFILEID + " = ?";
        String[] selectionArgs = {String.valueOf(key.value), String.valueOf(getCachedProfileId())};

        ContentValues values = new ContentValues();
        values.put(key.column, visible);
        
        return write(sqLiteDatabase -> {
            return sqLiteDatabase.update(
                    table,
                    values,
                    selection,
                    selectionArgs);
        }) > 0;

    }
}
