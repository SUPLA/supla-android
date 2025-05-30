package org.supla.core.shared.data.model.rest
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

data class ImpulseCounterPhotoDto(
  val id: String,
  val deviceGuid: String,
  val channelNo: Int,
  val createdAt: String,
  val replacedAt: String?,
  val processedAt: String?,
  val resultMeasurement: Int?,
  val processingTimeMs: Int?,
  val resultMeasurement2: Int?,
  val processingTimeMs2: Int?,
  val resultCode: Int,
  val resultMessage: String?,
  val measurementValid: Boolean,
  val image: String?,
  val imageCropped: String?
)
