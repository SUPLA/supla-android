package org.supla.android.db;

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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import dagger.hilt.android.EntryPointAccessors;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.supla.android.core.infrastructure.DateProvider;
import org.supla.android.data.source.ChannelRepository;
import org.supla.android.data.source.ColorListRepository;
import org.supla.android.data.source.DefaultChannelRepository;
import org.supla.android.data.source.DefaultColorListRepository;
import org.supla.android.data.source.DefaultSceneRepository;
import org.supla.android.data.source.DefaultUserIconRepository;
import org.supla.android.data.source.SceneRepository;
import org.supla.android.data.source.UserIconRepository;
import org.supla.android.data.source.local.ChannelDao;
import org.supla.android.data.source.local.ColorListDao;
import org.supla.android.data.source.local.LocationDao;
import org.supla.android.data.source.local.SceneDao;
import org.supla.android.data.source.local.UserIconDao;
import org.supla.android.di.entrypoints.ProfileIdHolderEntryPoint;
import org.supla.android.images.ImageCacheProxy;
import org.supla.android.lib.SuplaChannelGroup;
import org.supla.android.lib.SuplaChannelGroupRelation;
import org.supla.android.lib.SuplaLocation;
import org.supla.android.profile.ProfileIdHolder;

public class DbHelper extends BaseDbHelper {

  public static final int DATABASE_VERSION = 37;
  public static final String DATABASE_NAME = "supla.db";
  private static final Object mutex = new Object();

  private static DbHelper instance;

  private final ChannelRepository channelRepository;
  private final ColorListRepository colorListRepository;
  private final UserIconRepository userIconRepository;
  private final SceneRepository sceneRepository;

  private DbHelper(Context context, ProfileIdProvider profileIdProvider) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION, profileIdProvider);
    this.channelRepository =
        new DefaultChannelRepository(
            new ChannelDao(this), new LocationDao(this), new DateProvider());
    this.colorListRepository = new DefaultColorListRepository(new ColorListDao(this));
    this.userIconRepository =
        new DefaultUserIconRepository(
            new UserIconDao(this), new ImageCacheProxy(), profileIdProvider);
    this.sceneRepository = new DefaultSceneRepository(new SceneDao(this));
  }

  /**
   * Gets a single instance of the {@link DbHelper} class. If the instance does not exist, is
   * created like in classic Singleton pattern.
   *
   * @param context The context.
   * @return {@link DbHelper} instance.
   */
  public static DbHelper getInstance(Context context) {
    DbHelper result = instance;
    if (result == null) {
      synchronized (mutex) {
        result = instance;
        if (result == null) {
          ProfileIdHolder profileIdHolder =
              EntryPointAccessors.fromApplication(
                      context.getApplicationContext(), ProfileIdHolderEntryPoint.class)
                  .provideProfileIdHolder();
          instance = result = new DbHelper(context, profileIdHolder::getProfileId);
        }
      }
    }
    return result;
  }

  public SceneRepository getSceneRepository() {
    return sceneRepository;
  }

  @NonNull
  @Override
  public String getDatabaseNameForLog() {
    return DATABASE_NAME;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    // Moved to Room (see LegacySchema.onCreate())
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // Moved to Room (see DatabaseModule)
  }

  public boolean updateLocation(SuplaLocation suplaLocation) {
    return channelRepository.updateLocation(suplaLocation);
  }

  public void updateLocation(Location location) {
    channelRepository.updateLocation(location);
  }

  public Channel getChannel(int channelId) {
    return channelRepository.getChannel(channelId);
  }

  public ChannelGroup getChannelGroup(int groupId) {
    return channelRepository.getChannelGroup(groupId);
  }

  public void updateChannel(Channel channel) {
    channelRepository.updateChannel(channel);
  }

  public boolean updateChannelGroup(SuplaChannelGroup suplaChannelGroup) {
    return channelRepository.updateChannelGroup(suplaChannelGroup);
  }

  public boolean updateChannelGroupRelation(SuplaChannelGroupRelation suplaChannelGroupRelation) {
    return channelRepository.updateChannelGroupRelation(suplaChannelGroupRelation);
  }

  public boolean setChannelsVisible(int visible, int whereVisible) {
    return channelRepository.setChannelsVisible(visible, whereVisible);
  }

  public boolean setChannelGroupsVisible(int visible, int whereVisible) {
    return channelRepository.setChannelGroupsVisible(visible, whereVisible);
  }

  public boolean setChannelGroupRelationsVisible(int visible, int whereVisible) {
    return channelRepository.setChannelGroupRelationsVisible(visible, whereVisible);
  }

  public boolean setChannelsOffline() {
    return channelRepository.setChannelsOffline();
  }

  public int getChannelCount() {
    return channelRepository.getChannelCount();
  }

  public Cursor getChannelListCursorForGroup(int groupId) {
    return channelRepository.getChannelListCursorForGroup(groupId);
  }

  public ColorListItem getColorListItem(int id, boolean group, int idx) {
    return colorListRepository.getColorListItem(id, group, idx);
  }

  public void updateColorListItemValue(ColorListItem item) {
    colorListRepository.updateColorListItemValue(item);
  }

  public List<Integer> iconsToDownload() {
    Set<Integer> result = new LinkedHashSet<>();
    result.addAll(channelRepository.getChannelUserIconIdsToDownload());
    result.addAll(sceneRepository.getSceneUserIconIdsToDownload());
    return new ArrayList<>(result);
  }

  public boolean addUserIcons(
      int id,
      byte[] img1,
      byte[] img2,
      byte[] img3,
      byte[] img4,
      byte[] img1dark,
      byte[] img2dark,
      byte[] img3dark,
      byte[] img4dark) {
    return userIconRepository.addUserIcons(
        id, img1, img2, img3, img4, img1dark, img2dark, img3dark, img4dark);
  }

  public boolean isZWaveBridgeChannelAvailable() {
    return channelRepository.isZWaveBridgeChannelAvailable();
  }

  public List<Channel> getZWaveBridgeChannels() {
    return channelRepository.getZWaveBridgeChannels();
  }

  public ChannelRepository getChannelRepository() {
    return channelRepository;
  }
}
