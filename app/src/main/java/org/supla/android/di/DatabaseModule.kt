package org.supla.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.LocalProfileRepository
import org.supla.android.db.DbHelper
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

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
}