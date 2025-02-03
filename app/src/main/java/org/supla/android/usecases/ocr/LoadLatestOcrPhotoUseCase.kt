package org.supla.android.usecases.ocr
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

import android.util.Base64
import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.remote.rest.SuplaCloudService
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

data class OcrPhoto(
  val date: String,
  val original: Any?,
  val cropped: Any?,
  val value: String
)

@Singleton
class LoadLatestOcrPhotoUseCase @Inject constructor(
  private val suplaCloudServiceProvider: SuplaCloudService.Provider,
  private val valuesFormatter: ValuesFormatter
) {

  operator fun invoke(remoteId: Int): Observable<OcrPhoto> =
    with(suplaCloudServiceProvider.provide()) {
      getLatestImpulseCounterPhotoOld(remoteId)
        .onErrorResumeNext { getLatestImpulseCounterPhoto(remoteId) }
        .map { photoDto ->
          val localDateTime = LocalDateTime.parse(photoDto.createdAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
          val date = Date.from(localDateTime.atZone(ZoneId.of("UTC")).toInstant())
            ?.let { valuesFormatter.getFullDateString(it) } ?: ValuesFormatter.NO_VALUE_TEXT

          OcrPhoto(
            date = date,
            original = photoDto.image?.let { Base64.decode(it, Base64.DEFAULT) },
            cropped = photoDto.imageCropped?.let { Base64.decode(it, Base64.DEFAULT) },
            value = photoDto.resultMeasurement?.toString() ?: ValuesFormatter.NO_VALUE_TEXT
          )
        }
    }
}
