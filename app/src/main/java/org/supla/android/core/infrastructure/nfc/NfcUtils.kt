package org.supla.android.core.infrastructure.nfc
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

import android.content.Intent
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Build.VERSION.SDK_INT
import androidx.navigation3.runtime.NavKey
import org.supla.android.features.nfc.call.CallActionFromData
import org.supla.android.features.nfc.call.CallActionFromUrl
import org.supla.core.shared.extensions.ifTrue
import timber.log.Timber
import java.util.UUID

private const val HOST = "supla.org"
private const val PATH = "tag"
private const val URL = "https://$HOST/$PATH/"
private const val MIME = "application/vnd.org.supla.tag"

sealed interface TagProcessingResult {
  data object NotUsable : TagProcessingResult
  data class Success(val uuid: String, val readOnly: Boolean) : TagProcessingResult
  data object NotEnoughSpace : TagProcessingResult
  data object Failure : TagProcessingResult
}

val Uri.tagUuid: String?
  get() =
    if (host == HOST && pathSegments.size == 2 && pathSegments[0] == PATH) {
      pathSegments[1]
    } else {
      Timber.w("Could not find tag UUID in URI $this (host: $host, path: $path)")
      null
    }

val Intent.nfcTag: Tag?
  get() = when {
    SDK_INT >= 33 -> getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
    else ->
      @Suppress("DEPRECATION")
      getParcelableExtra(NfcAdapter.EXTRA_TAG)
  }

val Intent.navKey: NavKey
  get() {
    val tag = nfcTag

    if (ndefMessages != null) {
      for (message in ndefMessages) {
        message.uriFromUriRecord?.let {
          Timber.d("Found URI in message: $it")
          return CallActionFromUrl(it.toString(), !tag.isWritable)
        }
        message.uuidFromMimeRecord?.let {
          Timber.d("Found UUID in message: $it")
          return CallActionFromData(it, !tag.isWritable)
        }
      }
    }

    return CallActionFromUrl(data?.toString(), !tag.isWritable)
  }

val Intent.ndefMessages: List<NdefMessage>?
  get() = when {
    SDK_INT >= 33 -> getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)?.toList()
    else ->
      @Suppress("DEPRECATION")
      getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.map { it as NdefMessage }
  }

val NdefMessage.uuidFromMimeRecord: String?
  get() =
    records
      .firstOrNull { record ->
        record.tnf == NdefRecord.TNF_MIME_MEDIA &&
          record.type.contentEquals(MIME.toByteArray(Charsets.UTF_8))
      }?.payload?.toString(Charsets.UTF_8)

val NdefMessage.uriFromUriRecord: Uri?
  get() =
    records
      .firstOrNull { it.tnf == NdefRecord.TNF_WELL_KNOWN && it.type.contentEquals(NdefRecord.RTD_URI) }
      ?.toUri()

val Tag?.isWritable: Boolean
  get() = if (this == null) false else Ndef.get(this)?.isWritable ?: (NdefFormatable.get(this) != null)

fun Tag.prepareForSupla(lockTag: Boolean): TagProcessingResult {
  val ndef = Ndef.get(this)
  if (ndef != null) {
    return ndef.prepareForSupla(lockTag)
  }

  val formatable = NdefFormatable.get(this)
  if (formatable != null) {
    var result = formatable.formatForSupla()
    if (result is TagProcessingResult.Success && lockTag) {
      makeReadOnlyAfterFormat()
      result = result.copy(readOnly = true)
    }
    return result
  }

  return TagProcessingResult.NotUsable
}

private fun NdefFormatable.formatForSupla(): TagProcessingResult {
  try {
    Timber.i("Trying to format for supla")

    val id = UUID.randomUUID().toString()

    connect()
    format(createNdefMessage(id))

    return TagProcessingResult.Success(id, false)
  } catch (ex: Exception) {
    Timber.e(ex)
    return TagProcessingResult.Failure
  } finally {
    runCatching { close() }
  }
}

private fun Ndef.prepareForSupla(lockTag: Boolean): TagProcessingResult {
  try {
    connect()

    val message = ndefMessage ?: return writeSuplaRecord(lockTag)
    val uuidFromMimeRecord = message.uuidFromMimeRecord
    val uuidFromUriRecord = message.uriFromUriRecord?.tagUuid

    if (uuidFromMimeRecord != null && uuidFromMimeRecord == uuidFromUriRecord) {
      Timber.i("NFC tag for supla found (id: $uuidFromMimeRecord)")
      val readOnly = (lockTag && isWritable).ifTrue { makeReadOnly() } ?: !isWritable
      return TagProcessingResult.Success(uuidFromMimeRecord, readOnly)
    }

    Timber.d("No supla NFC record found, trying to create one")
    return writeSuplaRecord(lockTag)
  } catch (ex: Exception) {
    Timber.e(ex, "NFC tag processing failed!")
    return TagProcessingResult.Failure
  } finally {
    runCatching { close() }
  }
}

private fun Tag.makeReadOnlyAfterFormat() {
  val ndef = Ndef.get(this) ?: return
  try {
    ndef.connect()
    ndef.makeReadOnly()
  } catch (e: Exception) {
    Timber.e(e, "Read only tag making failed")
  } finally {
    runCatching { ndef.close() }
  }
}

private fun Ndef.writeSuplaRecord(lockTag: Boolean): TagProcessingResult {
  if (isWritable) {
    val id = UUID.randomUUID().toString()
    val message = createNdefMessage(id)

    val recordsSize = message.toByteArray().size
    return if (recordsSize < maxSize) {
      Timber.d("Writing supla message to NFC tag")
      writeNdefMessage(message)
      val readOnly = lockTag.ifTrue { makeReadOnly() } ?: false
      TagProcessingResult.Success(id, readOnly)
    } else {
      Timber.e("Supla message is too big to write to NFC tag ($recordsSize vs $maxSize")
      TagProcessingResult.NotEnoughSpace
    }
  } else {
    Timber.d("NFC tag is not writable")
    return TagProcessingResult.NotUsable
  }
}

private fun createNdefMessage(id: String): NdefMessage {
  val mimeRecord = NdefRecord.createMime(MIME, id.toByteArray(Charsets.UTF_8))
  val wwwUri = NdefRecord.createUri("${URL}$id")
  return NdefMessage(arrayOf(mimeRecord, wwwUri))
}
