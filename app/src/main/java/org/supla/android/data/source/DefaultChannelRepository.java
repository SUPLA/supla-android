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
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.ChannelGroupRelation;
import org.supla.android.db.ChannelValue;
import org.supla.android.db.Location;
import org.supla.android.db.SuplaContract;
import org.supla.android.lib.SuplaChannel;
import org.supla.android.lib.SuplaChannelExtendedValue;
import org.supla.android.lib.SuplaChannelGroup;
import org.supla.android.lib.SuplaChannelGroupRelation;
import org.supla.android.lib.SuplaChannelValue;
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
  public ChannelValue getChannelValue(int channelId) {
    return channelDao.getChannelValue(channelId);
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
  public void updateChannelGroup(ChannelGroup channelGroup) {
    channelDao.update(channelGroup);
  }

  @Override
  public boolean updateChannel(SuplaChannel suplaChannel) {
    Location location = getLocation(suplaChannel.LocationID);
    if (location == null) {
      return false;
    }

    Channel channel = getChannel(suplaChannel.Id);
    if (channel == null) {
      channel = new Channel();
      channel.Assign(suplaChannel, channelDao.getCachedProfileId().intValue());
      channel.setVisible(1);
      updateChannelPosition(location, channel);

      channelDao.insert(channel);
      return true;
    } else if (channel.Diff(suplaChannel)
        || channel.getLocationId() != suplaChannel.LocationID
        || channel.getVisible() != 1) {

      if (channel.getLocationId() != suplaChannel.LocationID) {
        // channel changed location - position update needed.
        updateChannelPosition(location, channel);
      }
      channel.Assign(suplaChannel, channelDao.getCachedProfileId().intValue());
      channel.setVisible(1);

      channelDao.update(channel);
      return true;
    }

    return false;
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
  public boolean updateChannelValue(SuplaChannelValue channelValue, int channelId, boolean online) {
    ChannelValue value = getChannelValue(channelId);

    if (value == null) {
      value = new ChannelValue();
      value.AssignSuplaChannelValue(channelValue);
      value.setChannelId(channelId);
      value.setOnLine(online);

      channelDao.insert(value);
      return true;
    } else if (value.Diff(channelValue) || value.getOnLine() != online) {

      if (online) {
        value.AssignSuplaChannelValue(channelValue);
      }
      value.setOnLine(online);

      channelDao.update(value);
      return true;
    }

    return false;
  }

  @Override
  public boolean updateChannelExtendedValue(
      SuplaChannelExtendedValue suplaChannelExtendedValue, int channelId) {
    ChannelExtendedValue value = channelDao.getChannelExtendedValue(channelId);
    if (value == null) {
      value = new ChannelExtendedValue();
      value.setExtendedValue(suplaChannelExtendedValue);
      value.setChannelId(channelId);
      if (value.hasTimerSet()) {
        value.setTimerStartTimestamp(dateProvider.currentTimestamp());
      } else {
        value.setTimerStartTimestamp(null);
      }

      channelDao.insert(value);
    } else {
      value.setExtendedValue(suplaChannelExtendedValue);
      if (value.getTimerStartTimestamp() == null && value.hasTimerSet()) {
        value.setTimerStartTimestamp(dateProvider.currentTimestamp());
      } else if (value.getTimerStartTimestamp() != null && !value.hasTimerSet()) {
        value.setTimerStartTimestamp(null);
      }
      channelDao.update(value);
    }
    return true;
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
  @SuppressLint("Range")
  public List<Integer> updateAllChannelGroups() {
    ArrayList<Integer> result = new ArrayList<>();

    Cursor c = channelDao.getChannelGroupValueViewEntryCursor();
    ChannelGroup channelGroup = null;
    if (c.moveToFirst()) {
      do {
        int groupId =
            c.getInt(
                c.getColumnIndex(SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_GROUPID));

        if (channelGroup == null) {
          channelGroup = getChannelGroup(groupId);
          if (channelGroup == null) {
            break;
          }

          channelGroup.resetBuffer();
        }

        if (channelGroup.getGroupId() == groupId) {
          ChannelValue val = new ChannelValue();
          val.AssignCursorDataFromGroupView(c);
          channelGroup.addValueToBuffer(val);
        }

        if (!c.isLast()) {
          c.moveToNext();
          groupId =
              c.getInt(
                  c.getColumnIndex(SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_GROUPID));
          c.moveToPrevious();
        }

        if (c.isLast() || channelGroup.getGroupId() != groupId) {
          if (channelGroup.DiffWithBuffer()) {
            channelGroup.assignBuffer();
            channelDao.update(channelGroup);
            result.add(channelGroup.getGroupId());
          }

          if (!c.isLast()) {
            channelGroup = null;
          }
        }

      } while (c.moveToNext());
    }
    c.close();

    return result;
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
            + SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID
            + " IN ( SELECT "
            + SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID
            + " FROM "
            + SuplaContract.ChannelGroupRelationEntry.TABLE_NAME
            + " WHERE "
            + SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_GROUPID
            + " = "
            + groupId
            + " AND "
            + SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_VISIBLE
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
        SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC
            + " <> 0 "
            + " AND (C."
            + SuplaContract.ChannelViewEntry.COLUMN_NAME_PROFILEID
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
                  channelListCursor.getColumnIndex(SuplaContract.ChannelViewEntry._ID)));
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
                  channelListCursor.getColumnIndex(SuplaContract.ChannelGroupEntry._ID)));
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

  private void updateChannelPosition(Location location, Channel channel) {
    if (location.getSorting() == Location.SortingType.DEFAULT) {
      channel.setPosition(0);
    } else {
      channel.setPosition(channelDao.getChannelCountForLocation(location.getLocationId()) + 1);
    }
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
