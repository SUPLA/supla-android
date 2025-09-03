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

import android.app.NotificationManager
import android.app.UiModeManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.PowerManager
import androidx.biometric.BiometricManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.supla.android.Preferences
import org.supla.android.SuplaApp
import org.supla.android.core.SuplaAppApi
import org.supla.android.core.SuplaAppProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.formatting.DateFormatter
import org.supla.android.data.source.ProfileRepository
import org.supla.android.lib.AndroidSuplaClientMessageHandler
import org.supla.android.lib.SuplaClient
import org.supla.android.profile.MultiAccountProfileManager
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.profile.ProfileManager
import org.supla.android.widget.WidgetPreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

  @Provides
  @Singleton
  fun provideProfileIdHolder() = ProfileIdHolder(null)

  @Provides
  @Singleton
  fun provideProfileManager(profileRepository: ProfileRepository): ProfileManager = MultiAccountProfileManager(profileRepository)

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
  fun provideSuplaClientProvider(): SuplaClientProvider = object : SuplaClientProvider {
    override fun provide(): SuplaClient? = SuplaApp.getApp().getSuplaClient()
  }

  @Provides
  @Singleton
  fun provideSuplaAppProvider(): SuplaAppProvider = object : SuplaAppProvider {
    override fun provide(): SuplaAppApi = SuplaApp.getApp()
  }

  @Provides
  @Singleton
  fun provideSuplaClientMessageHandler(): AndroidSuplaClientMessageHandler =
    AndroidSuplaClientMessageHandler.getGlobalInstance()

  @Provides
  @Singleton
  fun provideNotificationManager(@ApplicationContext context: Context) =
    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  @Provides
  @Singleton
  fun provideModeManager(@ApplicationContext context: Context) =
    context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager

  @Provides
  @Singleton
  fun provideBiometricManager(@ApplicationContext context: Context) =
    BiometricManager.from(context)

  @Provides
  @Singleton
  fun providePowerManager(@ApplicationContext context: Context) =
    context.getSystemService(Context.POWER_SERVICE) as PowerManager

  @Provides
  @Singleton
  fun provideWifiManager(@ApplicationContext context: Context): WifiManager =
    context.getSystemService(WifiManager::class.java)

  @Provides
  @Singleton
  fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager =
    context.getSystemService(ConnectivityManager::class.java)

  @Provides
  @Singleton
  fun provideLocationManager(@ApplicationContext context: Context): LocationManager =
    context.getSystemService(LocationManager::class.java)

  @Provides
  @Singleton
  fun provideDateFormatter() = DateFormatter()
}
