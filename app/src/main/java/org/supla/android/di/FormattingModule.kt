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
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.formatters.GpmValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.formatters.ThermometerAndHumidityValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.formatters.ThermometerValueFormatter
import javax.inject.Named
import javax.inject.Singleton

const val FORMATTER_THERMOMETER = "FORMATTER_THERMOMETER"
const val FORMATTER_THERMOMETER_AND_HUMIDITY = "FORMATTER_HUMIDITY"
const val FORMATTER_GPM = "FORMATTER_GPM"

@Module
@InstallIn(SingletonComponent::class)
class FormattingModule {

  @Singleton
  @Provides
  @Named(FORMATTER_THERMOMETER)
  fun provideThermometerValueFormatter(
    applicationPreferences: ApplicationPreferences
  ): ValueFormatter = ThermometerValueFormatter(applicationPreferences)

  @Singleton
  @Provides
  @Named(FORMATTER_THERMOMETER_AND_HUMIDITY)
  fun provideThermometerAndHumidityValueFormatter(
    applicationPreferences: ApplicationPreferences
  ): ValueFormatter = ThermometerAndHumidityValueFormatter(applicationPreferences)

  @Singleton
  @Provides
  @Named(FORMATTER_GPM)
  fun provideGpmValueFormatter(): ValueFormatter = GpmValueFormatter()
}
