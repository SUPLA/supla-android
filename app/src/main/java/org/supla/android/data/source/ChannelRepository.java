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

import android.database.Cursor;
import io.reactivex.rxjava3.core.Completable;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.Location;
import org.supla.android.lib.SuplaChannelGroup;
import org.supla.android.lib.SuplaChannelGroupRelation;
import org.supla.android.lib.SuplaLocation;

public interface ChannelRepository {
  Channel getChannel(int channelId);

  ChannelGroup getChannelGroup(int groupId);

  void updateChannel(Channel channel);

  boolean updateChannelGroup(SuplaChannelGroup suplaChannelGroup);

  boolean updateChannelGroupRelation(SuplaChannelGroupRelation suplaChannelGroupRelation);

  int getChannelCount();

  boolean setChannelsVisible(int visible, int whereVisible);

  boolean setChannelGroupsVisible(int visible, int whereVisible);

  boolean setChannelGroupRelationsVisible(int visible, int whereVisible);

  boolean setChannelsOffline();

  Cursor getChannelListCursorForGroup(int groupId);

  boolean isZWaveBridgeChannelAvailable();

  List<Channel> getZWaveBridgeChannels();

  List<Integer> getChannelUserIconIdsToDownload();

  Completable reorderChannels(Long firstItemId, int firstItemLocationId, Long secondItemId);

  Completable reorderChannelGroups(Long firstItemId, int firstItemLocationId, Long secondItemId);

  // Location looks rather as a channel location, that's why here

  Location getLocation(int locationId);

  boolean updateLocation(SuplaLocation suplaLocation);

  void updateLocation(Location location);

  Cursor getAllProfileChannels(Long profileId);

  Cursor getAllProfileChannelGroups(Long profileId);

  @NotNull
  List<Location> getAllLocations();
}
