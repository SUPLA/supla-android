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

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.GetChannelActionStringUseCase
import org.supla.core.shared.usecase.channel.GetChannelBatteryIconUseCase
import org.supla.core.shared.usecase.channel.GetChannelDefaultCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelIssuesForListUseCase
import org.supla.core.shared.usecase.channel.GetChannelLowBatteryIssueUseCase
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
  fun provide(
    getChannelLowBatteryIssueUseCase: GetChannelLowBatteryIssueUseCase,
    getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase
  ) = GetChannelIssuesForListUseCase(getChannelLowBatteryIssueUseCase, getChannelBatteryIconUseCase)
}
