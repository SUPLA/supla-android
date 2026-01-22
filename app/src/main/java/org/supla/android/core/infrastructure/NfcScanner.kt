package org.supla.android.core.infrastructure
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
import android.nfc.Tag
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.supla.android.core.infrastructure.nfc.isNfcAction
import org.supla.android.core.infrastructure.nfc.nfcTag
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

private val TIMEOUT = 15.seconds

@Singleton
class NfcScanner @Inject constructor() {
  private val semaphore = Semaphore(1, 1)
  private val mutex = Mutex()

  private var processing = false
  private var intent: Intent? = null

  suspend fun scan(): Result {
    mutex.withLock {
      Timber.i("Scan started")
      processing = true
      intent = null
      semaphore.tryAcquire() // release semaphore if already acquired

      try {
        return withTimeoutOrNull(TIMEOUT) {
          semaphore.acquire()
          Timber.d("Scan finished")

          intent?.nfcTag?.let { Result.Success(it) } ?: Result.Failure
        } ?: Result.Timeout
      } catch (ex: Exception) {
        Timber.e(ex, "Scan failed!")
        return Result.Failure
      } finally {
        processing = false
      }
    }
  }

  fun handleIntent(intent: Intent) {
    if (!processing) return // Skip intents if not processing

    val action = intent.action ?: return
    if (action.isNfcAction) {
      this.intent = intent
      this.semaphore.release()
    }
  }

  sealed interface Result {
    data object Timeout : Result
    data object Failure : Result
    data class Success(val tag: Tag) : Result
  }
}
