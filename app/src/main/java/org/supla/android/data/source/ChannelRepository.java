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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.supla.android.db.Channel;
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.ChannelValue;
import org.supla.android.db.Location;
import org.supla.android.lib.SuplaChannel;
import org.supla.android.lib.SuplaChannelExtendedValue;
import org.supla.android.lib.SuplaChannelGroup;
import org.supla.android.lib.SuplaChannelGroupRelation;
import org.supla.android.lib.SuplaChannelValue;
import org.supla.android.lib.SuplaLocation;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;

public interface ChannelRepository {
    Channel getChannel(int channelId);

    ChannelValue getChannelValue(int channelId);

    ChannelGroup getChannelGroup(int groupId);

    boolean updateChannel(SuplaChannel suplaChannel);

    boolean updateChannelGroup(SuplaChannelGroup suplaChannelGroup);

    boolean updateChannelValue(SuplaChannelValue channelValue, int channelId, boolean online);

    boolean updateChannelExtendedValue(SuplaChannelExtendedValue suplaChannelExtendedValue, int channelId);

    boolean updateChannelGroupRelation(SuplaChannelGroupRelation suplaChannelGroupRelation);

    List<Integer> updateAllChannelGroups();

    int getChannelCount();

    boolean setChannelsVisible(int visible, int whereVisible);

    boolean setChannelGroupsVisible(int visible, int whereVisible);

    boolean setChannelGroupRelationsVisible(int visible, int whereVisible);

    boolean setChannelsOffline();

    Cursor getChannelListCursorWithDefaultOrder(String where);

    Cursor getChannelListCursorWithDefaultOrder();

    Cursor getChannelGroupListCursor();

    boolean isZWaveBridgeChannelAvailable();

    List<Channel> getZWaveBridgeChannels();

    List<Integer> getChannelUserIconIds();

    Completable reorderChannels(Long firstItemId, int firstItemLocationId, Long secondItemId);

    // Location looks rather as a channel location, that's why here

    Location getLocation(int locationId);

    boolean updateLocation(SuplaLocation suplaLocation);

    void updateLocation(Location location);
}
