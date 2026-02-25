package org.supla.android.usecases.nfc
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

import android.nfc.Tag
import android.nfc.tech.Ndef
import org.supla.android.core.infrastructure.nfc.tagUuid
import org.supla.android.core.infrastructure.nfc.uriFromUriRecord
import org.supla.android.core.infrastructure.nfc.uuidFromMimeRecord
import org.supla.android.data.source.NfcTagRepository
import org.supla.android.tools.SuplaSchedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockTagUseCase @Inject constructor(
  private val nfcTagRepository: NfcTagRepository,
  private val schedulers: SuplaSchedulers
) {
  suspend operator fun invoke(tag: Tag, tagId: Long): Result {
    val tagEntity = nfcTagRepository.findById(tagId)
    if (tagEntity == null) {
      Timber.e("NFC tag not found!")
      return Failure(TagOperationError.PROTECTION_FAILED)
    }

    val ndef = Ndef.get(tag)
    if (ndef != null) {
      return schedulers.io {
        val result = handleNdef(ndef, tagEntity.uuid)
        if (result is Success) {
          // Update tag readOnly flag after successful lock
          nfcTagRepository.save(tagEntity.copy(readOnly = true))
        }

        result
      }
    }

    Timber.w("Only NDEF tag can be locked!")
    return Failure(TagOperationError.UNSUPPORTED)
  }

  private fun handleNdef(ndef: Ndef, uuid: String): Result {
    try {
      ndef.connect()

      val message = ndef.ndefMessage
      if (message == null) {
        Timber.d("Got tag without SUPLA message")
        return Failure(TagOperationError.WRONG)
      }

      val tagUuid = message.uuidFromMimeRecord ?: message.uriFromUriRecord?.tagUuid
      if (tagUuid == null) {
        Timber.d("Got tag without SUPLA records")
        return Failure(TagOperationError.WRONG)
      }

      if (tagUuid != uuid) {
        Timber.d("Got tag with wrong UUID")
        return Failure(TagOperationError.WRONG)
      }

      ndef.makeReadOnly()
      return Success
    } catch (ex: Exception) {
      Timber.e(ex, "NFC tag processing failed!")
      return Failure(TagOperationError.PROTECTION_FAILED)
    } finally {
      ndef.close()
    }
  }

  sealed interface Result
  data object Success : Result
  data class Failure(val error: TagOperationError) : Result
}
