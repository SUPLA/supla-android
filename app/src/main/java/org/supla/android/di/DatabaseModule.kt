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
import org.supla.android.BuildConfig
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.SceneRepository
import org.supla.android.data.source.local.LocalProfileRepository
import org.supla.android.db.DbHelper
import org.supla.android.db.MeasurementsDbHelper
import org.supla.android.db.room.EmptyMigration
import org.supla.android.db.room.app.AppDatabase
import org.supla.android.db.room.app.AppDatabaseCallback
import org.supla.android.db.room.app.migrations.MIGRATION_23_24
import org.supla.android.db.room.app.migrations.MIGRATION_24_25
import org.supla.android.db.room.app.migrations.MIGRATION_28_29
import org.supla.android.db.room.app.migrations.MIGRATION_29_30
import org.supla.android.db.room.app.migrations.MIGRATION_30_31
import org.supla.android.db.room.app.migrations.MIGRATION_31_32
import org.supla.android.db.room.app.migrations.Migration25to26
import org.supla.android.db.room.app.migrations.Migration26to27
import org.supla.android.db.room.app.migrations.Migration27to28
import org.supla.android.db.room.measurements.MeasurementsDatabase
import org.supla.android.db.room.measurements.MeasurementsDatabaseCallback
import org.supla.android.db.room.measurements.migrations.MEASUREMENTS_DB_MIGRATION_31_32
import org.supla.android.db.room.measurements.migrations.MeasurementsDbMigration29to30
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

  @Provides
  @Singleton
  fun provideAppDatabase(
    @ApplicationContext context: Context,
    callback: AppDatabaseCallback,
    migration25to26: Migration25to26,
    migration26to27: Migration26to27,
    migration27to28: Migration27to28
  ) =
    Room.databaseBuilder(context, AppDatabase::class.java, DbHelper.DATABASE_NAME)
      .apply {
        if (!BuildConfig.DEBUG) {
          // Destructive migration should be activated only in production. For development we need to know about all migration failures
          fallbackToDestructiveMigration()
        }
      }
      .addCallback(callback)
      .addMigrations(
        MIGRATION_23_24,
        MIGRATION_24_25,
        migration25to26,
        migration26to27,
        migration27to28,
        MIGRATION_28_29,
        MIGRATION_29_30,
        MIGRATION_30_31,
        MIGRATION_31_32,
      )
      .build()

  @Provides
  @Singleton
  fun provideChannelRelationDao(appDatabase: AppDatabase) =
    appDatabase.channelRelationDao()

  @Provides
  @Singleton
  fun provideChannelConfigDao(appDatabase: AppDatabase) =
    appDatabase.channelConfigDao()

  @Provides
  @Singleton
  fun provideChannelDao(appDatabase: AppDatabase) =
    appDatabase.channelDao()

  @Provides
  @Singleton
  fun provideProfileDao(appDatabase: AppDatabase) =
    appDatabase.profileDao()

  @Provides
  @Singleton
  fun provideSceneDao(appDatabase: AppDatabase) =
    appDatabase.sceneDao()

  @Provides
  @Singleton
  fun provideLocationDao(appDatabase: AppDatabase) =
    appDatabase.locationDao()

  @Provides
  @Singleton
  fun provideChannelValueDao(appDatabase: AppDatabase) =
    appDatabase.channelValueDao()

  @Provides
  @Singleton
  fun provideColorDao(appDatabase: AppDatabase) =
    appDatabase.colorDao()

  @Provides
  @Singleton
  fun provideChannelGroupDao(appDatabase: AppDatabase) =
    appDatabase.channelGroupDao()

  @Provides
  @Singleton
  fun provideUserIconDao(appDatabase: AppDatabase) =
    appDatabase.userIconDao()

  @Provides
  @Singleton
  fun provideChannelGroupRelationDao(appDatabase: AppDatabase) =
    appDatabase.channelGroupRelationDao()

  @Provides
  @Singleton
  fun provideChannelExtendedValueDao(appDatabase: AppDatabase) =
    appDatabase.channelExtendedValueDao()

  @Provides
  @Singleton
  fun provideMeasurementsDatabase(
    @ApplicationContext context: Context,
    callback: MeasurementsDatabaseCallback,
    migration29to30: MeasurementsDbMigration29to30
  ) =
    Room.databaseBuilder(context, MeasurementsDatabase::class.java, MeasurementsDbHelper.DATABASE_NAME)
      .apply {
        if (!BuildConfig.DEBUG) {
          // Destructive migration should be activated only in production. For development we need to know about all migration failures
          fallbackToDestructiveMigration()
        }
      }
      .addCallback(callback)
      .addMigrations(
        migration29to30,
        EmptyMigration(30, 31),
        MEASUREMENTS_DB_MIGRATION_31_32
      )
      .build()

  @Provides
  @Singleton
  fun provideTemperatureLogDao(measurementsDatabase: MeasurementsDatabase) =
    measurementsDatabase.temperatureLogDao()

  @Provides
  @Singleton
  fun provideTemperatureAndHumidityLogDao(measurementsDatabase: MeasurementsDatabase) =
    measurementsDatabase.temperatureAndHumidityLogDao()

  @Provides
  @Singleton
  fun provideGeneralPurposeMeterLogDao(measurementsDatabase: MeasurementsDatabase) =
    measurementsDatabase.generalPurposeMeterLogDao()

  @Provides
  @Singleton
  fun provideGeneralPurposeMeasurementLogDao(measurementsDatabase: MeasurementsDatabase) =
    measurementsDatabase.generalPurposeMeasurementLogDao()

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
