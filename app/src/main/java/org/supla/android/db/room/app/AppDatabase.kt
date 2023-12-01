package org.supla.android.db.room.app
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

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.supla.android.data.source.local.dao.ChannelConfigDao
import org.supla.android.data.source.local.dao.ChannelDao
import org.supla.android.data.source.local.dao.ChannelExtendedValueDao
import org.supla.android.data.source.local.dao.ChannelGroupDao
import org.supla.android.data.source.local.dao.ChannelGroupRelationDao
import org.supla.android.data.source.local.dao.ChannelRelationDao
import org.supla.android.data.source.local.dao.ChannelValueDao
import org.supla.android.data.source.local.dao.ColorDao
import org.supla.android.data.source.local.dao.LocationDao
import org.supla.android.data.source.local.dao.ProfileDao
import org.supla.android.data.source.local.dao.SceneDao
import org.supla.android.data.source.local.dao.UserIconDao
import org.supla.android.data.source.local.entity.ChannelConfigEntity
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.ColorEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.local.entity.UserIconEntity
import org.supla.android.db.DbHelper

@Database(
  entities = [
    ChannelEntity::class,
    SceneEntity::class,
    ChannelRelationEntity::class,
    ChannelConfigEntity::class,
    ProfileEntity::class,
    LocationEntity::class,
    ChannelValueEntity::class,
    ColorEntity::class,
    ChannelGroupEntity::class,
    UserIconEntity::class,
    ChannelGroupRelationEntity::class,
    ChannelExtendedValueEntity::class
  ],
  version = DbHelper.DATABASE_VERSION,
  exportSchema = false
)
@TypeConverters(AppDatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun channelRelationDao(): ChannelRelationDao
  abstract fun channelConfigDao(): ChannelConfigDao
  abstract fun channelDao(): ChannelDao
  abstract fun profileDao(): ProfileDao
  abstract fun sceneDao(): SceneDao
  abstract fun locationDao(): LocationDao
  abstract fun channelValueDao(): ChannelValueDao
  abstract fun colorDao(): ColorDao
  abstract fun channelGroupDao(): ChannelGroupDao
  abstract fun userIconDao(): UserIconDao
  abstract fun channelGroupRelationDao(): ChannelGroupRelationDao
  abstract fun channelExtendedValueDao(): ChannelExtendedValueDao
}
