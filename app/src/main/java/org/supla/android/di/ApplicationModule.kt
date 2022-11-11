package org.supla.android.di

import android.appwidget.AppWidgetManager
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
import org.supla.android.widget.WidgetPreferences
import org.supla.android.widget.WidgetVisibilityHandler
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.scenes.SceneEventsManager
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
    profileIdHolder: ProfileIdHolder,
    widgetVisibilityHandler: WidgetVisibilityHandler,
    sceneEventsManager: SceneEventsManager
  ): ProfileManager {
    return MultiAccountProfileManager(
      dbHelper,
      Preferences.getDeviceID(context),
      profileRepository,
      profileIdHolder,
      widgetVisibilityHandler,
      sceneEventsManager
    )
  }

  @Provides
  @Singleton
  fun providePreferences(@ApplicationContext context: Context) =
    Preferences(context)

  @Provides
  @Singleton
  fun provideWidgetPreferences(@ApplicationContext context: Context) =
    WidgetPreferences(context)

  @Provides
  @Singleton
  fun provideAppWidgetManager(@ApplicationContext context: Context) =
    AppWidgetManager.getInstance(context)

  @Provides
  @Singleton
  fun provideSuplaClientMessageHandler(): SuplaClientMessageHandler =
    SuplaClientMessageHandler.getGlobalInstance()

}
