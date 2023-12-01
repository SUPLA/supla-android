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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.supla.android.core.networking.suplacloud.SuplaDateConverter
import java.util.Date
import javax.inject.Named
import javax.inject.Singleton

const val GSON_FOR_API = "GSON_FOR_API"
const val GSON_FOR_REPO = "GSON_FOR_REPO"

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

  @Provides
  @Singleton
  @Named(GSON_FOR_API)
  fun provideGsonForApi(): Gson =
    GsonBuilder().registerTypeAdapter(Date::class.java, SuplaDateConverter()).create()

  @Provides
  @Singleton
  @Named(GSON_FOR_REPO)
  fun providesGsonForRepo(): Gson =
    GsonBuilder().create()
}
