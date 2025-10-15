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

import android.content.Context
import android.net.Uri
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.channels.FileChannel

/**
 * Utility class for managing the core file copying logic for database export.
 * This function is designed to work with the Storage Access Framework (SAF).
 */
object FileExporter {

  /**
   * Copies the current application database file to a new, specified location.
   * This is useful for creating backups or preparing a staging copy.
   *
   * @param source Source file.
   * @param destination Destination file.
   * @return true if the copy was successful, false otherwise.
   */
  fun copyFile(source: File, destination: File): Boolean {
    if (!source.exists()) {
      Timber.e("Source database file does not exist: ${source.absolutePath}")
      return false
    }

    var sourceChannel: FileChannel? = null
    var destinationChannel: FileChannel? = null

    try {
      sourceChannel = FileInputStream(source).channel
      destinationChannel = FileOutputStream(destination).channel
      destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size())

      Timber.i("Database successfully copied from ${source.absolutePath} to ${destination.absolutePath}")
      return true
    } catch (e: IOException) {
      Timber.e(e, "Failed to copy database file.")
      return false
    } finally {
      try {
        sourceChannel?.close()
        destinationChannel?.close()
      } catch (e: IOException) {
        Timber.e(e, "Error closing file channels.")
      }
    }
  }

  /**
   * Copies the application's private SQLite database file to a destination URI
   * provided by the Storage Access Framework (SAF).
   *
   * @param context The application context.
   * @param databaseName The name of the SQLite database file (e.g., "my_app_db.db").
   * @param outputUri The destination URI chosen by the user via SAF.
   * @return True if the copy was successful, false otherwise.
   */
  fun copyDatabaseToUri(context: Context, databaseName: String, outputUri: Uri): Boolean {
    return copyFileToUri(context, context.getDatabasePath(databaseName), outputUri)
  }

  /**
   * Copies the application's private SQLite database file to a destination URI
   * provided by the Storage Access Framework (SAF).
   *
   * @param context The application context.
   * @param file File to be exported.
   * @param outputUri The destination URI chosen by the user via SAF.
   * @return True if the copy was successful, false otherwise.
   */
  fun copyFileToUri(context: Context, file: File, outputUri: Uri): Boolean {
    if (!file.exists()) {
      Timber.e("Database file not found at: ${file.absolutePath}")
      return false
    }

    // 2. Open input and output channels
    var sourceChannel: FileChannel? = null
    var destinationChannel: FileChannel? = null
    var outputStream: OutputStream? = null

    try {
      // Input stream from the internal database file
      sourceChannel = FileInputStream(file).channel

      // Output stream to the user-selected destination URI using ContentResolver
      outputStream = context.contentResolver.openOutputStream(outputUri)
      if (outputStream == null) {
        Timber.e("Could not open output stream for URI: $outputUri")
        return false
      }

      // Get a channel for the output stream
      destinationChannel = (outputStream as? FileOutputStream)?.channel
      if (destinationChannel == null) {
        Timber.e("Could not get channel for output stream")
        return false
      }

      // Transfer data from the internal file to the external URI location
      destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size())

      Timber.i("Successfully exported database to URI: $outputUri")
      return true
    } catch (e: Exception) {
      Timber.e(e, "Error copying database file using SAF: ${e.message}")
      return false
    } finally {
      try {
        outputStream?.close()
        sourceChannel?.close()
        destinationChannel?.close()
      } catch (closeException: IOException) {
        Timber.e(closeException, "Error closing channels: ${closeException.message}")
      }
    }
  }
}
