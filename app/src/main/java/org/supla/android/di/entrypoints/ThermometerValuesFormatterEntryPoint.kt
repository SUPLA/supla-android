package org.supla.android.di.entrypoints

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.supla.android.di.FORMATTER_THERMOMETER
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import javax.inject.Named

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ThermometerValuesFormatterEntryPoint {
  @Named(FORMATTER_THERMOMETER)
  fun provideThermometerValueFormatter(): ValueFormatter
}
