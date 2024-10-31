package org.supla.android.extensions
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
import dagger.hilt.android.EntryPointAccessors
import org.supla.android.Preferences
import org.supla.android.data.ValuesFormatter
import org.supla.android.di.entrypoints.GetChannelDefaultCaptionUseCaseEntryPoint
import org.supla.android.di.entrypoints.GetChannelIconUseCaseEntryPoint
import org.supla.android.di.entrypoints.PreferencesEntryPoint
import org.supla.android.di.entrypoints.ValuesFormatterEntryPoint
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.usecase.channel.GetChannelDefaultCaptionUseCase

val Context.valuesFormatter: ValuesFormatter
  get() = EntryPointAccessors.fromApplication(
    applicationContext,
    ValuesFormatterEntryPoint::class.java
  ).provideValuesFormatter()

val Context.preferences: Preferences
  get() = EntryPointAccessors.fromApplication(
    applicationContext,
    PreferencesEntryPoint::class.java
  ).providePreferences()

val Context.getChannelIconUseCase: GetChannelIconUseCase
  get() = EntryPointAccessors.fromApplication(
    applicationContext,
    GetChannelIconUseCaseEntryPoint::class.java
  ).provideGetChannelIconUseCase()

val Context.getChannelDefaultCaptionUseCase: GetChannelDefaultCaptionUseCase
  get() = EntryPointAccessors.fromApplication(
    applicationContext,
    GetChannelDefaultCaptionUseCaseEntryPoint::class.java
  ).provideGetChannelDefaultCaptionUseCase()
