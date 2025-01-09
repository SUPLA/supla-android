package org.supla.android.usecases.app
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
import org.supla.android.Trace
import org.supla.android.core.infrastructure.BuildConfigProxy
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.infrastructure.ThreadHandler
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.db.DbHelper
import org.supla.android.db.room.app.AppDatabase
import org.supla.android.db.room.measurements.MeasurementsDatabase
import org.supla.android.extensions.TAG
import javax.inject.Inject
import javax.inject.Singleton

private const val INITIALIZATION_MIN_TIME_MS = 500

@Singleton
class InitializationUseCase @Inject constructor(
  private val stateHolder: SuplaClientStateHolder,
  private val appDatabase: AppDatabase,
  private val measurementsDatabase: MeasurementsDatabase,
  private val profileRepository: RoomProfileRepository,
  private val encryptedPreferences: EncryptedPreferences,
  private val dateProvider: DateProvider,
  private val threadHandler: ThreadHandler,
  private val buildConfigProxy: BuildConfigProxy
) {
  operator fun invoke(context: Context) {
    val initializationStartTime = dateProvider.currentTimestamp()

    // Open databases and migrate if needed.
    migrateDatabases(context)

    // Check if there is an active profile
    val profileFound = try {
      profileRepository.findActiveProfile().blockingGet().active ?: false
    } catch (_: Exception) {
      // No active profile
      false
    }

    // Check pin
    val pinRequired = try {
      encryptedPreferences.lockScreenSettings.pinForAppRequired
    } catch (exception: Exception) {
      Trace.e(TAG, "Could not check lock screen settings!", exception)
      false
    }

    // Wait a moment to avoid screen blinking
    val initializationEndTime = dateProvider.currentTimestamp()
    val initializationTime = initializationEndTime - initializationStartTime
    if (initializationTime < INITIALIZATION_MIN_TIME_MS) {
      try {
        threadHandler.sleep(INITIALIZATION_MIN_TIME_MS - initializationTime)
      } catch (_: Exception) {
        // Nothing to do
      }
    }

    // Go to next state
    Trace.d(TAG, "Active profile found: $profileFound, pin required: $pinRequired")
    if (pinRequired) {
      stateHolder.handleEvent(SuplaClientEvent.Lock)
    } else if (profileFound) {
      stateHolder.handleEvent(SuplaClientEvent.Initialized)
    } else {
      stateHolder.handleEvent(SuplaClientEvent.NoAccount)
    }
  }

  private fun migrateDatabases(context: Context) {
    try {
      appDatabase.openHelper.readableDatabase
      measurementsDatabase.openHelper.readableDatabase
    } catch (exception: Exception) {
      if (buildConfigProxy.debug) {
        throw exception
      }

      Trace.e(TAG, "Could not migrate database, trying to delete it", exception)
      context.deleteDatabase(DbHelper.DATABASE_NAME)
      context.deleteDatabase(MeasurementsDatabase.NAME)
      Trace.e(TAG, "Database deletion finished")
    }
  }
}
