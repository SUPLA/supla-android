package org.supla.android.usecases.list
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

import kotlinx.coroutines.rx3.awaitFirstOrNull
import kotlinx.coroutines.rx3.awaitSingleOrNull
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.model.settings.ImpulseCounterSettings
import org.supla.android.data.model.settings.ListValue
import org.supla.android.data.source.ChannelExtendedValueRepository
import org.supla.android.data.source.ChannelValueRepository
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.local.entity.measurements.ImpulseCounterLogEntity
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import org.supla.core.shared.usecase.channel.valueformatter.formatters.ImpulseCounterValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.withUnit
import timber.log.Timber
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefreshImpulseCounterAggregatedValueUseCase @Inject constructor(
  private val channelExtendedValueRepository: ChannelExtendedValueRepository,
  private val impulseCounterLogRepository: ImpulseCounterLogRepository,
  private val channelValueRepository: ChannelValueRepository,
  private val userStateHolder: UserStateHolder,
  private val dateProvider: DateProvider
) {

  val formatter = ImpulseCounterValueFormatter()

  suspend operator fun invoke(profileId: Long, remoteId: Int) {
    val settings = userStateHolder.getImpulseCounterSettings(profileId, remoteId)
    if (settings.showOnList == ListValue.COUNTER_STATE) {
      Timber.w("Refresh impulse counter aggregated value started for counter state!")
      return
    }

    val currentDate = dateProvider.currentDate()
    val lastEntry = impulseCounterLogRepository.findOldestEntity(remoteId, profileId).awaitSingleOrNull()
    if (lastEntry == null) {
      Timber.i("Data up to date - no update needed.")
      return
    }

    val entriesStartDate = settings.aggregationStartDate?.toEpochSecond()
    if (entriesStartDate == null) {
      Timber.e("Got NULL as entries start date")
      return
    }

    val entries: List<ImpulseCounterLogEntity>? = impulseCounterLogRepository
      .findMeasurements(remoteId, profileId, Date(entriesStartDate * 1000), currentDate)
      .awaitFirstOrNull()

    if (entries.isNullOrEmpty()) {
      Timber.i("No entries found")
      channelValueRepository.updateAggregatedValue(profileId, remoteId, NO_VALUE_TEXT)
      return
    }

    val unit = channelExtendedValueRepository.findBy(profileId, remoteId)?.getSuplaValue()?.ImpulseCounterValue?.unit
    val aggregatedValue = entries.fold(0f) { acc, entity -> acc + entity.calculatedValue }
    val formatted = formatter.format(aggregatedValue, withUnit(unit, showNoValueText = false))

    Timber.d("Aggregated value set to $formatted")
    channelValueRepository.updateAggregatedValue(profileId, remoteId, formatted)
  }

  val ImpulseCounterSettings.aggregationStartDate: ZonedDateTime?
    get() = when (showOnList) {
      ListValue.LAST_24_HOURS -> dateProvider.currentDateTime.minusHours(24)
      ListValue.LAST_7_DAYS -> dateProvider.currentDateTime.minusDays(7)
      ListValue.LAST_30_DAYS -> dateProvider.currentDateTime.minusDays(30)
      ListValue.LAST_90_DAYS -> dateProvider.currentDateTime.minusDays(90)
      ListValue.LAST_365_DAYS -> dateProvider.currentDateTime.minusDays(365)
      ListValue.CURRENT_HOUR ->
        dateProvider.currentDateTime
          .withMinute(0)
          .withSecond(0)
          .withNano(0)
      ListValue.CURRENT_DAY ->
        dateProvider.currentDateTime
          .withHour(0)
          .withMinute(0)
          .withSecond(0)
          .withNano(0)
      ListValue.CURRENT_WEEK ->
        dateProvider.currentDateTime
          .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
          .withHour(0)
          .withMinute(0)
          .withSecond(0)
          .withNano(0)
      ListValue.CURRENT_MONTH ->
        dateProvider.currentDateTime
          .withDayOfMonth(1)
          .withHour(0)
          .withMinute(0)
          .withSecond(0)
          .withNano(0)
      ListValue.CURRENT_YEAR ->
        dateProvider.currentDateTime
          .withMonth(1)
          .withDayOfMonth(1)
          .withHour(0)
          .withMinute(0)
          .withSecond(0)
          .withNano(0)
      ListValue.COUNTER_STATE -> null
    }
}
