package org.supla.android.data.source;

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
import android.database.Cursor;
import io.reactivex.rxjava3.core.Completable;
import java.util.ArrayList;
import java.util.List;
import org.supla.android.data.source.local.ChannelDao;
import org.supla.android.data.source.local.LocationDao;
import org.supla.android.data.source.local.entity.ChannelGroupEntity;
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity;
import org.supla.android.data.source.local.view.ChannelView;
import org.supla.android.db.Channel;
import org.supla.android.db.Location;

public class DefaultChannelRepository implements ChannelRepository {

  private final ChannelDao channelDao;
  private final LocationDao locationDao;

  public DefaultChannelRepository(ChannelDao channelDao, LocationDao locationDao) {
    this.channelDao = channelDao;
    this.locationDao = locationDao;
  }

  @Override
  public Cursor getChannelListCursorForGroup(int groupId) {
    String where =
        "C."
            + ChannelView.COLUMN_CHANNEL_REMOTE_ID
            + " IN ( SELECT "
            + ChannelGroupRelationEntity.COLUMN_CHANNEL_ID
            + " FROM "
            + ChannelGroupRelationEntity.TABLE_NAME
            + " WHERE "
            + ChannelGroupRelationEntity.COLUMN_GROUP_ID
            + " = "
            + groupId
            + " AND "
            + ChannelGroupRelationEntity.COLUMN_VISIBLE
            + " > 0 ) ";

    return channelDao.getChannelListCursorWithDefaultOrder(where);
  }

  @Override
  public boolean isZWaveBridgeChannelAvailable() {
    return channelDao.isZWaveBridgeChannelAvailable();
  }

  @Override
  public List<Channel> getZWaveBridgeChannels() {
    return channelDao.getZWaveBridgeChannels();
  }

  @Override
  public Completable reorderChannels(
      Long firstItemId, int firstItemLocationId, Long secondItemId, long profileId) {
    return Completable.fromRunnable(
        () -> doReorderChannels(firstItemId, firstItemLocationId, secondItemId, profileId));
  }

  @Override
  public Completable reorderChannelGroups(
      Long firstItemId, int firstItemLocationId, Long secondItemId, long profilId) {
    return Completable.fromRunnable(
        () -> doReorderChannelGroups(firstItemId, firstItemLocationId, secondItemId, profilId));
  }

  private void doReorderChannels(
      Long firstItemId, int firstItemLocationId, Long secondItemId, long profileId) {
    List<Long> orderedItems = getSortedChannelIdsForLocation(firstItemLocationId, profileId);

    reorderList(orderedItems, firstItemId, secondItemId);

    channelDao.updateChannelsOrder(orderedItems, firstItemLocationId);
  }

  @SuppressLint("Range")
  private List<Long> getSortedChannelIdsForLocation(int locationId, long profileId) {
    ArrayList<Long> orderedItems = new ArrayList<>();

    Location location = locationDao.getLocation(locationId, profileId);
    try (Cursor channelListCursor =
        channelDao.getSortedChannelIdsForLocationCursor(location.getCaption())) {
      if (channelListCursor.moveToFirst()) {
        do {
          orderedItems.add(
              channelListCursor.getLong(
                  channelListCursor.getColumnIndex(ChannelView.COLUMN_CHANNEL_ID)));
        } while (channelListCursor.moveToNext());
      }
    }

    return orderedItems;
  }

  private void doReorderChannelGroups(
      Long firstItemId, int firstItemLocationId, Long secondItemId, long profileId) {
    List<Long> orderedItems = getSortedChannelGroupIdsForLocation(firstItemLocationId, profileId);

    reorderList(orderedItems, firstItemId, secondItemId);

    channelDao.updateChannelGroupsOrder(orderedItems);
  }

  @SuppressLint("Range")
  private List<Long> getSortedChannelGroupIdsForLocation(int locationId, long profileId) {
    ArrayList<Long> orderedItems = new ArrayList<>();

    Location location = locationDao.getLocation(locationId, profileId);
    try (Cursor channelListCursor =
        channelDao.getSortedChannelGroupIdsForLocationCursor(location.getCaption())) {
      if (channelListCursor.moveToFirst()) {
        do {
          orderedItems.add(
              channelListCursor.getLong(
                  channelListCursor.getColumnIndex(ChannelGroupEntity.COLUMN_ID)));
        } while (channelListCursor.moveToNext());
      }
    }

    return orderedItems;
  }

  private void reorderList(List<Long> orderedItems, Long firstItemId, Long secondItemId) {
    // localize items to swipe in new list
    int initialPosition = -1, finalPosition = -1;
    for (int i = 0; i < orderedItems.size(); i++) {
      Long id = orderedItems.get(i);
      if (id.equals(firstItemId)) {
        initialPosition = i;
      }
      if (id.equals(secondItemId)) {
        finalPosition = i;
      }
    }
    if (initialPosition < 0 || finalPosition < 0) {
      throw new IllegalArgumentException("Swap items not found");
    }
    // Shift items in the table
    Long removedId = orderedItems.remove(initialPosition);
    orderedItems.add(finalPosition, removedId);
  }
}
