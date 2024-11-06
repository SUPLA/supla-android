package org.supla.core.shared.usecase.channel
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

import org.supla.core.shared.data.model.rest.ImpulseCounterPhoto
import org.supla.core.shared.infrastructure.Base64Helper
import org.supla.core.shared.usecase.channel.ocr.OcrImageNamingProvider
import org.supla.core.shared.usecase.file.StoreFileInDirectoryUseCase

class StoreChannelOcrPhotoUseCase(
  private val storeFileInDirectoryUseCase: StoreFileInDirectoryUseCase,
  private val ocrImageNamingProvider: OcrImageNamingProvider,
  private val base64Helper: Base64Helper
) {
  operator fun invoke(remoteId: Int, profileId: Long, photo: ImpulseCounterPhoto) {
    photo.image?.let {
      storeFileInDirectoryUseCase(
        directoryName = ocrImageNamingProvider.directory,
        fileName = ocrImageNamingProvider.imageName(profileId, remoteId),
        fileData = base64Helper.decode(it)
      )
    }

    photo.imageCropped?.let {
      storeFileInDirectoryUseCase(
        directoryName = ocrImageNamingProvider.directory,
        fileName = ocrImageNamingProvider.imageCroppedName(profileId, remoteId),
        fileData = base64Helper.decode(it)
      )
    }
  }
}
