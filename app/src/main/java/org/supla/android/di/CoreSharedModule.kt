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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.supla.android.core.infrastructure.CacheFileAccessProxy
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.lib.AndroidSuplaClientMessageHandler
import org.supla.core.shared.infrastructure.Base64Helper
import org.supla.core.shared.infrastructure.storage.CacheFileAccess
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.GetChannelActionStringUseCase
import org.supla.core.shared.usecase.addwizard.CheckRegistrationEnabledUseCase
import org.supla.core.shared.usecase.addwizard.EnableRegistrationUseCase
import org.supla.core.shared.usecase.channel.CheckOcrPhotoExistsUseCase
import org.supla.core.shared.usecase.channel.GetAllChannelIssuesUseCase
import org.supla.core.shared.usecase.channel.GetChannelBatteryIconUseCase
import org.supla.core.shared.usecase.channel.GetChannelDefaultCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelIssuesForListUseCase
import org.supla.core.shared.usecase.channel.GetChannelIssuesForSlavesUseCase
import org.supla.core.shared.usecase.channel.GetChannelLowBatteryIssueUseCase
import org.supla.core.shared.usecase.channel.GetChannelSpecificIssuesUseCase
import org.supla.core.shared.usecase.channel.StoreChannelOcrPhotoUseCase
import org.supla.core.shared.usecase.channel.ocr.OcrImageNamingProvider
import org.supla.core.shared.usecase.file.StoreFileInDirectoryUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CoreSharedModule {

  @Provides
  @Singleton
  fun provideGetChannelActionStringUseCase() = GetChannelActionStringUseCase()

  @Provides
  @Singleton
  fun provideGetChannelDefaultCaptionUseCase() = GetChannelDefaultCaptionUseCase()

  @Provides
  @Singleton
  fun provideGetChannelBatteryIconUseCase() = GetChannelBatteryIconUseCase()

  @Provides
  @Singleton
  fun provideGetCaptionUseCase(
    getChannelDefaultCaptionUseCase: GetChannelDefaultCaptionUseCase
  ) = GetCaptionUseCase(getChannelDefaultCaptionUseCase)

  @Singleton
  @Provides
  fun provideGetChannelLowBatteryIssueUseCase(
    getCaptionUseCase: GetCaptionUseCase,
    applicationPreferences: ApplicationPreferences
  ) = GetChannelLowBatteryIssueUseCase(getCaptionUseCase, applicationPreferences)

  @Singleton
  @Provides
  fun provideGetChannelSpecificIssuesUseCase() = GetChannelSpecificIssuesUseCase()

  @Singleton
  @Provides
  fun provideGetChannelIssuesForListUseCase(
    getChannelLowBatteryIssueUseCase: GetChannelLowBatteryIssueUseCase,
    getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase,
    getChannelSpecificIssuesUseCase: GetChannelSpecificIssuesUseCase
  ) = GetChannelIssuesForListUseCase(getChannelLowBatteryIssueUseCase, getChannelBatteryIconUseCase, getChannelSpecificIssuesUseCase)

  @Singleton
  @Provides
  fun provideGetChannelIssuesForSlavesUseCase(
    getChannelLowBatteryIssueUseCase: GetChannelLowBatteryIssueUseCase,
    getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase,
    getChannelSpecificIssuesUseCase: GetChannelSpecificIssuesUseCase
  ) = GetChannelIssuesForSlavesUseCase(getChannelLowBatteryIssueUseCase, getChannelBatteryIconUseCase, getChannelSpecificIssuesUseCase)

  @Singleton
  @Provides
  fun provideGetAllChannelIssuesUseCase(
    getChannelLowBatteryIssueUseCase: GetChannelLowBatteryIssueUseCase,
    getChannelSpecificIssuesUseCase: GetChannelSpecificIssuesUseCase
  ) = GetAllChannelIssuesUseCase(getChannelLowBatteryIssueUseCase, getChannelSpecificIssuesUseCase)

  @Singleton
  @Provides
  fun provideBase64Helper() = Base64Helper()

  @Singleton
  @Provides
  fun provideOcrImageNamingProvider() = OcrImageNamingProvider()

  @Singleton
  @Provides
  fun provideCacheFileAccessProxy(@ApplicationContext context: Context): CacheFileAccess =
    CacheFileAccessProxy(context)

  @Singleton
  @Provides
  fun provideStoreFileInDirectoryUseCase(
    cacheFileAccess: CacheFileAccess
  ) = StoreFileInDirectoryUseCase(cacheFileAccess)

  @Singleton
  @Provides
  fun provideStoreChannelOcrPhotoUseCase(
    storeFileInDirectoryUseCase: StoreFileInDirectoryUseCase,
    ocrImageNamingProvider: OcrImageNamingProvider,
    base64Helper: Base64Helper
  ) = StoreChannelOcrPhotoUseCase(storeFileInDirectoryUseCase, ocrImageNamingProvider, base64Helper)

  @Singleton
  @Provides
  fun provideCheckOcrPhotoExistsUseCase(
    ocrImageNamingProvider: OcrImageNamingProvider,
    cacheFileAccess: CacheFileAccess
  ) = CheckOcrPhotoExistsUseCase(ocrImageNamingProvider, cacheFileAccess)

  @Singleton
  @Provides
  fun provideCheckRegistrationEnabledUseCase(
    suplaClientProvider: SuplaClientProvider
  ) = CheckRegistrationEnabledUseCase(AndroidSuplaClientMessageHandler.getGlobalInstance(), suplaClientProvider)

  @Singleton
  @Provides
  fun provideEnableRegistrationUseCase(
    suplaClientProvider: SuplaClientProvider
  ) = EnableRegistrationUseCase(AndroidSuplaClientMessageHandler.getGlobalInstance(), suplaClientProvider)
}
