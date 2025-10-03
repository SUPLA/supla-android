package org.supla.android.features.scenescleanup
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

import androidx.work.ExistingWorkPolicy
import org.supla.android.core.infrastructure.WorkManagerProxy
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoveHiddenScenesManager @Inject constructor(
  private val workManagerProxy: WorkManagerProxy
) {
  fun start() {
    Timber.i("Starting hidden scenes removal manager")
    workManagerProxy.enqueueUniqueWork(
      RemoveHiddenScenesWorker.WORK_ID,
      ExistingWorkPolicy.REPLACE,
      RemoveHiddenScenesWorker.build()
    )
  }

  fun kill() {
    Timber.i("Killing hidden scenes removal manager")
    workManagerProxy.cancelByTag(RemoveHiddenScenesWorker.TAG)
  }
}
