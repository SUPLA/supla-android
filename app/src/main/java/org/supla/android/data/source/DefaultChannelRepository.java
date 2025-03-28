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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.rxjava3.core.Completable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.supla.android.core.infrastructure.DateProvider;
import org.supla.android.data.source.local.ChannelDao;
import org.supla.android.data.source.local.LocationDao;
import org.supla.android.data.source.local.entity.ChannelGroupEntity;
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity;
import org.supla.android.data.source.local.view.ChannelView;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.ChannelGroupRelation;
import org.supla.android.db.Location;
import org.supla.android.lib.SuplaChannelGroup;
import org.supla.android.lib.SuplaChannelGroupRelation;
import org.supla.android.lib.SuplaLocation;

public class DefaultChannelRepository implements ChannelRepository {

  private final ChannelDao channelDao;
  private final LocationDao locationDao;
  private final DateProvider dateProvider;

  public DefaultChannelRepository(
      ChannelDao channelDao, LocationDao locationDao, DateProvider dateProvider) {
    this.channelDao = channelDao;
    this.locationDao = locationDao;
    this.dateProvider = dateProvider;
  }

  @Override
  public Channel getChannel(int channelId) {
    return channelDao.getChannel(channelId);
  }

  @Override
  public ChannelGroup getChannelGroup(int groupId) {
    return channelDao.getChannelGroup(groupId);
  }

  @Override
  public void updateChannel(Channel channel) {
    channelDao.update(channel);
  }

  @Override
  public boolean updateChannelGroup(SuplaChannelGroup suplaChannelGroup) {
    Location location = getLocation(suplaChannelGroup.LocationID);
    if (location == null) {
      return false;
    }

    ChannelGroup channelGroup = getChannelGroup(suplaChannelGroup.Id);
    if (channelGroup == null) {
      channelGroup = new ChannelGroup();
      channelGroup.Assign(suplaChannelGroup, channelDao.getCachedProfileId().intValue());
      channelGroup.setVisible(1);
      channelGroup.setProfileId(channelDao.getCachedProfileId());
      updateChannelGroupPosition(location, channelGroup);

      channelDao.insert(channelGroup);
      return true;
    } else if (channelGroup.Diff(suplaChannelGroup)
        || channelGroup.getLocationId() != suplaChannelGroup.LocationID
        || channelGroup.getVisible() != 1) {

      if (channelGroup.getLocationId() != suplaChannelGroup.LocationID) {
        // channel changed location - position update needed.
        updateChannelGroupPosition(location, channelGroup);
      }

      channelGroup.Assign(suplaChannelGroup, channelDao.getCachedProfileId().intValue());
      channelGroup.setVisible(1);

      channelDao.update(channelGroup);
      return true;
    }
    return false;
  }

  @Override
  public boolean updateChannelGroupRelation(SuplaChannelGroupRelation suplaChannelGroupRelation) {
    ChannelGroupRelation channelGroupRelation =
        channelDao.getChannelGroupRelation(
            suplaChannelGroupRelation.ChannelID, suplaChannelGroupRelation.ChannelGroupID);

    if (channelGroupRelation == null) {
      channelGroupRelation = new ChannelGroupRelation();
      channelGroupRelation.Assign(suplaChannelGroupRelation);
      channelGroupRelation.setVisible(1);

      channelDao.insert(channelGroupRelation);
      return true;
    } else if (channelGroupRelation.getVisible() != 1) {
      channelGroupRelation.Assign(suplaChannelGroupRelation);
      channelGroupRelation.setVisible(1);

      channelDao.update(channelGroupRelation);
      return true;
    }
    return false;
  }

  @Override
  public int getChannelCount() {
    return channelDao.getChannelCount();
  }

  @Override
  public boolean setChannelsVisible(int visible, int whereVisible) {
    return channelDao.setChannelsVisible(visible, whereVisible);
  }

  @Override
  public boolean setChannelGroupsVisible(int visible, int whereVisible) {
    return channelDao.setChannelGroupsVisible(visible, whereVisible);
  }

  @Override
  public boolean setChannelGroupRelationsVisible(int visible, int whereVisible) {
    return channelDao.setChannelGroupRelationsVisible(visible, whereVisible);
  }

  @Override
  public boolean setChannelsOffline() {
    return channelDao.setChannelsOffline();
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
  public List<Integer> getChannelUserIconIdsToDownload() {
    Set<Integer> result = new LinkedHashSet<>();
    result.addAll(channelDao.getChannelUserIconIdsToDownload());
    result.addAll(channelDao.getChannelGroupUserIconIdsToDownload());
    return new ArrayList<>(result);
  }

  @Override
  public Completable reorderChannels(Long firstItemId, int firstItemLocationId, Long secondItemId) {
    return Completable.fromRunnable(
        () -> doReorderChannels(firstItemId, firstItemLocationId, secondItemId));
  }

  @Override
  public Completable reorderChannelGroups(
      Long firstItemId, int firstItemLocationId, Long secondItemId) {
    return Completable.fromRunnable(
        () -> doReorderChannelGroups(firstItemId, firstItemLocationId, secondItemId));
  }

  @Override
  public Location getLocation(int locationId) {
    return locationDao.getLocation(locationId);
  }

  @Override
  public boolean updateLocation(SuplaLocation suplaLocation) {
    Location location = getLocation(suplaLocation.Id);
    if (location == null) {
      location = new Location();
      location.AssignSuplaLocation(suplaLocation);
      location.setVisible(1);
      location.setSorting(Location.SortingType.DEFAULT);

      locationDao.insert(location);
      return true;
    } else if (location.Diff(suplaLocation)) {

      location.AssignSuplaLocation(suplaLocation);
      location.setVisible(1);

      locationDao.update(location);
      return true;
    }
    return false;
  }

  @Override
  public void updateLocation(@Nullable Location location) {
    if (location == null) {
      return;
    }

    locationDao.update(location);
  }

  @Override
  public Cursor getAllProfileChannels(Long profileId) {
    return channelDao.getAllChannels(
        ChannelView.COLUMN_CHANNEL_FUNCTION
            + " <> 0 "
            + " AND (C."
            + ChannelView.COLUMN_CHANNEL_PROFILE_ID
            + " = "
            + profileId
            + ") ");
  }

  @Override
  public Cursor getAllProfileChannelGroups(Long profileId) {
    return channelDao.getAllChannelGroupsForProfileId(profileId);
  }

  @NonNull
  @Override
  public List<Location> getAllLocations() {
    return locationDao.getLocations();
  }

  private void doReorderChannels(Long firstItemId, int firstItemLocationId, Long secondItemId) {
    List<Long> orderedItems = getSortedChannelIdsForLocation(firstItemLocationId);

    reorderList(orderedItems, firstItemId, secondItemId);

    channelDao.updateChannelsOrder(orderedItems, firstItemLocationId);
  }

  @SuppressLint("Range")
  private List<Long> getSortedChannelIdsForLocation(int locationId) {
    ArrayList<Long> orderedItems = new ArrayList<>();

    Location location = locationDao.getLocation(locationId);
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
      Long firstItemId, int firstItemLocationId, Long secondItemId) {
    List<Long> orderedItems = getSortedChannelGroupIdsForLocation(firstItemLocationId);

    reorderList(orderedItems, firstItemId, secondItemId);

    channelDao.updateChannelGroupsOrder(orderedItems);
  }

  @SuppressLint("Range")
  private List<Long> getSortedChannelGroupIdsForLocation(int locationId) {
    ArrayList<Long> orderedItems = new ArrayList<>();

    Location location = locationDao.getLocation(locationId);
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

  private void updateChannelGroupPosition(Location location, ChannelGroup channelGroup) {
    try {
      int lastPosition = channelDao.getChannelGroupLastPositionInLocation(location.getLocationId());
      if (lastPosition == 0) {
        channelGroup.setPosition(0);
      } else {
        channelGroup.setPosition(lastPosition + 1);
      }
    } catch (NoSuchElementException ex) {
      channelGroup.setPosition(0);
    }
  }
}
