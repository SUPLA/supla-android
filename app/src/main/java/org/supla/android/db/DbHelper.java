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
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import java.util.List;
import org.supla.android.data.source.ChannelRepository;
import org.supla.android.data.source.DefaultChannelRepository;
import org.supla.android.data.source.local.ChannelDao;
import org.supla.android.data.source.local.LocationDao;

public class DbHelper extends BaseDbHelper {

  public static final int DATABASE_VERSION = 47;
  public static final String DATABASE_NAME = "supla.db";
  private static final Object mutex = new Object();

  private static DbHelper instance;

  private final ChannelRepository channelRepository;

  private DbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    this.channelRepository =
        new DefaultChannelRepository(new ChannelDao(this), new LocationDao(this));
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
          instance = result = new DbHelper(context);
        }
      }
    }
    return result;
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
