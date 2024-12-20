package org.supla.android.usecases.channel.measurements.impulsecounter
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

import io.reactivex.rxjava3.core.Maybe
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.usecases.channel.measurements.ImpulseCounterMeasurements
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadImpulseCounterMeasurementsUseCase @Inject constructor(
  private val impulseCounterLogRepository: ImpulseCounterLogRepository,
  private val profileRepository: RoomProfileRepository,
  private val dateProvider: DateProvider
) {

  operator fun invoke(remoteId: Int, startDate: Date? = null, endDate: Date? = null): Maybe<ImpulseCounterMeasurements> {
    return profileRepository.findActiveProfile()
      .flatMapObservable {
        impulseCounterLogRepository.findMeasurements(
          remoteId,
          it.id!!,
          startDate ?: Date(0),
          endDate ?: dateProvider.currentDate()
        )
      }
      .firstElement()
      .map { measurements ->
        ImpulseCounterMeasurements(
          counter = measurements.map { it.calculatedValue }.sum()
        )
      }
  }
}
