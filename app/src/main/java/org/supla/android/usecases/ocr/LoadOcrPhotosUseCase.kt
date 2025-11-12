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

@Singleton
class LoadOcrPhotosUseCase @Inject constructor(
  private val suplaCloudServiceProvider: SuplaCloudService.Provider
) {

  operator fun invoke(remoteId: Int): Observable<List<OcrPhoto>> =
    suplaCloudServiceProvider.provide().getImpulseCounterPhotos(remoteId)
      .onErrorResumeNext { Observable.just(emptyList()) }
      .map { photos ->
        photos.mapNotNull { photo ->
          photo.imageCropped?.let { imageCropped ->
            val localDateTime = LocalDateTime.parse(photo.createdAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            Date.from(localDateTime.atZone(ZoneId.of("UTC")).toInstant())?.let { date ->
              ValuesFormatter.getFullDateString(date)?.let { formattedDate ->
                OcrPhoto(formattedDate, null, Base64.decode(imageCropped, Base64.DEFAULT), photo.toValue())
              }
            }
          }
        }
      }
}
