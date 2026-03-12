package org.supla.android.core.infrastructure.storage
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

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.core.storage.EncryptedPreferences
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A custom Timber Tree that writes log messages to a file for persistent logging.
 * Logs are stored in the app's internal files directory, which doesn't require
 * extra permissions.
 */
@Singleton
class DebugFileLoggingTree @Inject constructor(
  @param:ApplicationContext private val context: Context,
  private val preferences: EncryptedPreferences
) : Timber.DebugTree() {

  private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
  private val tagFormat = "%-23s" // Fixed width for clean column alignment
  val logFile = File(context.cacheDir, FILE_NAME)

  // This is the core function called every time Timber.log() is used.
  @SuppressLint("LogNotTimber")
  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    if (skipLogging(tag, message, t)) {
      return
    }

    try {
      val timestamp = dateFormat.format(Date())
      val priorityStr = getPriorityString(priority)
      val formattedTag = String.format(tagFormat, tag ?: "NO_TAG")

      // Format the log entry as: [TIMESTAMP] [PRIORITY] [TAG] MESSAGE
      val logEntry = "[$timestamp] $priorityStr $formattedTag: $message\n"

      FileWriter(logFile, true).use { writer ->
        writer.append(logEntry)
      }
    } catch (e: IOException) {
      Log.e("FileLogger", "Failed to write log to file: ${e.message}", e)
    }
  }

  fun cleanup(): Boolean {
    try {
      logFile.delete()
      return true
    } catch (ex: Exception) {
      Timber.e(ex, "Log file deletion failed!")
      return false
    }
  }

  private fun getPriorityString(priority: Int): String {
    return when (priority) {
      Log.VERBOSE -> "V"
      Log.DEBUG -> "D"
      Log.INFO -> "I"
      Log.WARN -> "W"
      Log.ERROR -> "E"
      Log.ASSERT -> "A"
      else -> "?"
    }
  }

  private fun skipLogging(tag: String?, message: String, t: Throwable?): Boolean {
    if (t != null) {
      return false
    }
    val filter = preferences.devLogFilteringString
    if (filter == null || filter.isEmpty()) {
      return false
    }

    return if (tag != null) {
      !message.contains(filter, true) || !tag.contains(filter, true)
    } else {
      !message.contains(filter, true)
    }
  }

  companion object Companion {
    const val FILE_NAME = "supla_debug.log"
  }
}
