package org.supla.android.features.channelscleanup
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
import org.supla.android.Trace
import org.supla.android.core.infrastructure.WorkManagerProxy
import org.supla.android.extensions.TAG
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoveHiddenChannelsManager @Inject constructor(
  private val workManagerProxy: WorkManagerProxy
) {
  fun start() {
    Trace.i(TAG, "Starting hidden channels removal manager")
    workManagerProxy.enqueueUniqueWork(
      RemoveHiddenChannelsWorker.WORK_ID,
      ExistingWorkPolicy.REPLACE,
      RemoveHiddenChannelsWorker.build()
    )
  }

  fun kill() {
    Trace.i(TAG, "Killing hidden channels removal manager")
    workManagerProxy.cancelByTag(RemoveHiddenChannelsWorker.TAG)
  }
}
