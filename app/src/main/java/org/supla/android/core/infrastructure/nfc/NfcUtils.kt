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
import timber.log.Timber
import java.util.UUID

private const val HOST = "www.supla.org"
private const val PATH = "/nfc"
private const val ID_ARGUMENT = "id"
private const val URL = "https://${HOST}$PATH?${ID_ARGUMENT}="

sealed interface TagProcessingResult {
  data object NotUsable : TagProcessingResult
  data class Success(val uuid: String) : TagProcessingResult
  data object Failure : TagProcessingResult
}

val Uri.tagUuid: String?
  get() =
    if (host == HOST && path == PATH) {
      getQueryParameter(ID_ARGUMENT)
        ?.let { if (it.startsWith("=")) it.substring(1) else it }
    } else {
      Timber.w("Could not find tag UUID in URI $this (host: $host, path: $path)")
      null
    }

val String.isNfcAction: Boolean
  get() = when (this) {
    NfcAdapter.ACTION_NDEF_DISCOVERED,
    NfcAdapter.ACTION_TECH_DISCOVERED,
    NfcAdapter.ACTION_TAG_DISCOVERED -> true

    else -> false
  }

val Intent.nfcTag: Tag?
  get() = when {
    SDK_INT >= 33 -> getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
    else ->
      @Suppress("DEPRECATION")
      getParcelableExtra(NfcAdapter.EXTRA_TAG)
  }

fun Tag.prepareForSupla(): TagProcessingResult =
  Ndef.get(this)?.prepareForSupla()
    ?: NdefFormatable.get(this)?.formatForSupla()
    ?: TagProcessingResult.NotUsable

private fun NdefFormatable.formatForSupla(): TagProcessingResult {
  try {
    Timber.i("Trying to format for supla")

    val id = UUID.randomUUID().toString()

    connect()
    format(createNdefMessage(id))

    return TagProcessingResult.Success(id)
  } catch (ex: Exception) {
    Timber.e(ex)
    return TagProcessingResult.Failure
  } finally {
    runCatching { close() }
  }
}

private fun Ndef.prepareForSupla(): TagProcessingResult {
  try {
    connect()

    val message = ndefMessage ?: return writeSuplaRecord()
    for (record in message.records) {
      Timber.d("Processing NFC tag record: ${String(record.payload)}")

      if (record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_URI)) {
        val url = runCatching { record.toUri() }.getOrNull() ?: continue
        val id = url.getQueryParameter("id")
        if (url.host == "supla.org" && url.path == "/nfc" && id != null) {
          Timber.i("Found supla message in NFC tag (id: $id)")
          return TagProcessingResult.Success(id)
        }
      }
    }

    Timber.d("No supla NFC record found, trying to create one")
    return writeSuplaRecord()
  } catch (ex: Exception) {
    Timber.e(ex, "NFC tag processing failed!")
    return TagProcessingResult.Failure
  } finally {
    runCatching { close() }
  }
}

private fun Ndef.writeSuplaRecord(): TagProcessingResult {
  if (isWritable) {
    val id = UUID.randomUUID().toString()
    val message = createNdefMessage(id)

    val recordsSize = message.toByteArray().size
    return if (recordsSize < maxSize) {
      Timber.d("Writing supla message to NFC tag")
      writeNdefMessage(message)
      TagProcessingResult.Success(id)
    } else {
      Timber.e("Supla message is too big to write to NFC tag ($recordsSize vs $maxSize")
      TagProcessingResult.Failure
    }
  } else {
    Timber.d("NFC tag is not writable")
    return TagProcessingResult.NotUsable
  }
}

private fun createNdefMessage(id: String): NdefMessage {
  val wwwUri = NdefRecord.createUri("$URL=$id")
  return NdefMessage(arrayOf(wwwUri))
}
