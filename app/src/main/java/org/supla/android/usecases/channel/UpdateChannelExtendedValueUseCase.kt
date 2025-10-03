package org.supla.android.usecases.channel
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

import androidx.room.rxjava3.EmptyResultSetException
import io.reactivex.rxjava3.core.Single
import kotlinx.serialization.json.Json
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.model.general.EntityUpdateResult
import org.supla.android.data.source.ChannelExtendedValueRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.usecases.channelstate.UpdateChannelStateUseCase
import timber.log.Timber
import java.io.IOException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class UpdateChannelExtendedValueUseCase @Inject constructor(
  private val profileRepository: RoomProfileRepository,
  private val channelExtendedValueRepository: ChannelExtendedValueRepository,
  private val updateChannelStateUseCase: UpdateChannelStateUseCase,
  private val dateProvider: DateProvider
) {

  operator fun invoke(channelId: Int, suplaChannelExtendedValue: SuplaChannelExtendedValue): Single<UpdateExtendedValueResult> =
    updateChannelStateUseCase.invoke(suplaChannelExtendedValue.ChannelStateValue?.copy(channelId = channelId))
      .andThen(channelExtendedValueRepository.findByRemoteId(channelId))
      .flatMap { value ->
        val oldDate: Date? = value.getSuplaValue()?.timerEstimatedEndDate
        val newDate: Date? = suplaChannelExtendedValue.timerEstimatedEndDate
        val setDate = newDate?.let { oldDate == null || abs(oldDate.time - newDate.time) > 1000 } ?: false
        val clearDate = value.timerStartTime != null && newDate == null
        val timerStartTime = when {
          setDate -> dateProvider.currentDate()
          clearDate -> null
          else -> value.timerStartTime
        }

        channelExtendedValueRepository.update(
          value.copy(
            value = suplaChannelExtendedValue.toByteArray(),
            timerStartTime = timerStartTime
          )
        ).toSingle {
          UpdateExtendedValueResult(EntityUpdateResult.UPDATED, setDate || clearDate)
        }
      }
      .onErrorResumeNext { throwable ->
        if (throwable is NoSuchElementException || throwable is EmptyResultSetException) {
          insert(channelId, suplaChannelExtendedValue)
        } else {
          Timber.e(throwable, "Could not create/update channel extended value")
          Single.just(UpdateExtendedValueResult(EntityUpdateResult.ERROR, false))
        }
      }

  private fun insert(channelId: Int, suplaChannelExtendedValue: SuplaChannelExtendedValue): Single<UpdateExtendedValueResult> =
    profileRepository.findActiveProfile()
      .flatMap { profile ->
        channelExtendedValueRepository.insert(
          ChannelExtendedValueEntity(
            id = null,
            channelId = channelId,
            value = suplaChannelExtendedValue.toByteArray(),
            timerStartTime = suplaChannelExtendedValue.timerEstimatedEndDate?.let { dateProvider.currentDate() },
            profileId = profile.id!!
          )
        ).toSingle {
          UpdateExtendedValueResult(EntityUpdateResult.UPDATED, suplaChannelExtendedValue.timerEstimatedEndDate != null)
        }
      }
}

data class UpdateExtendedValueResult(
  val result: EntityUpdateResult,
  val timerChanged: Boolean
)

fun SuplaChannelExtendedValue.toByteArray(): ByteArray? {
  try {
    return Json.encodeToString(this).toByteArray()
  } catch (ex: IOException) {
    Timber.e(ex, "Could not convert value to byte array")
    return null
  }
}
