package org.supla.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.supla.android.Preferences
import org.supla.android.data.source.ProfileRepository
import org.supla.android.db.DbHelper
import org.supla.android.profile.MultiAccountProfileManager
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.profile.ProfileManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

    @Provides
    @Singleton
    fun provideProfileIdHolder() = ProfileIdHolder(null)

    @Provides
    @Singleton
    fun provideProfileManager(
            @ApplicationContext context: Context,
            dbHelper: DbHelper,
            profileRepository: ProfileRepository,
            profileIdHolder: ProfileIdHolder): ProfileManager {
        return MultiAccountProfileManager(dbHelper, Preferences.getDeviceID(context), profileRepository, profileIdHolder)
    }
}