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

import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.core.infrastructure.nfc.tagUuid
import org.supla.android.core.infrastructure.nfc.uriFromUriRecord
import org.supla.android.core.infrastructure.nfc.uuidFromMimeRecord
import org.supla.android.tools.SuplaSchedulers
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val HOST = "supla.org"
private const val PATH = "tag"
private const val URL = "https://$HOST/$PATH/"

@Singleton
class PrepareNfcTagUseCase @Inject constructor(
  @param:ApplicationContext private val context: Context,
  private val schedulers: SuplaSchedulers
) {
  suspend operator fun invoke(tag: Tag): Result {
    val ndef = Ndef.get(tag)
    if (ndef != null) {
      return schedulers.io { ndef.prepareForSupla() }
    }

    val formatable = NdefFormatable.get(tag)
    if (formatable != null) {
      return schedulers.io { formatable.formatForSupla() }
    }

    Timber.w("Only NDEF tag can be locked!")
    return Failure(TagOperationError.UNSUPPORTED)
  }

  private fun Ndef.prepareForSupla(): Result {
    try {
      connect()

      val message = ndefMessage ?: return writeSuplaRecord()
      val uuidFromMimeRecord = message.uuidFromMimeRecord
      val uuidFromUriRecord = message.uriFromUriRecord?.tagUuid

      if (uuidFromMimeRecord != null && uuidFromMimeRecord == uuidFromUriRecord) {
        Timber.i("NFC tag for supla found (id: $uuidFromMimeRecord)")
        return Success(uuidFromMimeRecord, !isWritable)
      }

      Timber.d("No supla NFC record found, trying to create one")
      return writeSuplaRecord()
    } catch (ex: Exception) {
      Timber.e(ex, "NFC tag processing failed!")
      return Failure(TagOperationError.WRITE_FAILED)
    } finally {
      runCatching { close() }
    }
  }

  private fun Ndef.writeSuplaRecord(): Result {
    if (isWritable) {
      val id = UUID.randomUUID().toString()
      val message = createNdefMessage(id)

      val recordsSize = message.toByteArray().size
      return if (recordsSize < maxSize) {
        Timber.d("Writing supla message to NFC tag")
        writeNdefMessage(message)
        Success(id, false)
      } else {
        Timber.e("Supla message is too big to write to NFC tag ($recordsSize vs $maxSize")
        Failure(TagOperationError.NOT_ENOUGH_MEMORY)
      }
    } else {
      Timber.d("NFC tag is not writable")
      return Failure(TagOperationError.WRITE_PROTECTED)
    }
  }

  private fun NdefFormatable.formatForSupla(): Result {
    try {
      Timber.i("Trying to format for supla")
      val id = UUID.randomUUID().toString()
      connect()
      format(createNdefMessage(id))

      return Success(id, false)
    } catch (ex: Exception) {
      Timber.e(ex)
      return Failure(TagOperationError.WRITE_FAILED)
    } finally {
      runCatching { close() }
    }
  }

  private fun createNdefMessage(id: String): NdefMessage {
    val wwwUri = NdefRecord.createUri("${URL}$id")
    val aarRecord = NdefRecord.createApplicationRecord(context.packageName)
    return NdefMessage(arrayOf(wwwUri, aarRecord))
  }


  sealed interface Result
  data class Success(val uuid: String, val readOnly: Boolean) : Result
  data class Failure(val error: TagOperationError) : Result
}