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

import org.supla.core.shared.data.model.rest.ImpulseCounterPhotoDto
import org.supla.core.shared.infrastructure.LocalizedString

fun ImpulseCounterPhotoDto.toValue(): OcrPhoto.Value =
  when {
    processedAt == null -> OcrPhoto.Value.Waiting
    measurementValid && resultMeasurement != null -> OcrPhoto.Value.Success(LocalizedString.Constant(resultMeasurement.toString()))
    resultMeasurement != null -> OcrPhoto.Value.Warning(LocalizedString.Constant(resultMeasurement.toString()))
    else -> OcrPhoto.Value.Error
  }
