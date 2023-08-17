package org.supla.android.di
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

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.SceneRepository
import org.supla.android.data.source.local.LocalProfileRepository
import org.supla.android.db.DbHelper
import org.supla.android.db.room.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

  @Provides
  @Singleton
  fun provideAppDatabase(@ApplicationContext context: Context) =
    Room.databaseBuilder(context, AppDatabase::class.java, DbHelper.DATABASE_NAME).build()

  @Provides
  @Singleton
  fun provideChannelRelationDao(appDatabase: AppDatabase) = appDatabase.channelRelationDao()

  @Provides
  @Singleton
  fun provideDbHelper(@ApplicationContext context: Context) =
    DbHelper.getInstance(context)

  @Provides
  @Singleton
  fun provideProfileRepository(dbHelper: DbHelper): ProfileRepository =
    LocalProfileRepository(dbHelper)

  @Provides
  @Singleton
  fun provideChannelRepository(dbHelper: DbHelper): ChannelRepository =
    dbHelper.channelRepository

  @Provides
  @Singleton
  fun provideScenesRepository(dbHelper: DbHelper): SceneRepository =
    dbHelper.sceneRepository
}
