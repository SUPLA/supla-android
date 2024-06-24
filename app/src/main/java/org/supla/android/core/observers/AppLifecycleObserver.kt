package org.supla.android.core.observers
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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.usecases.client.FinishConnectionWorker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleObserver @Inject constructor(
  @ApplicationContext private val context: Context,
  private val suplaClientStateHolder: SuplaClientStateHolder,
) : DefaultLifecycleObserver {

  override fun onStart(owner: LifecycleOwner) {
    super.onStart(owner)
    WorkManager.getInstance(context).cancelUniqueWork(FinishConnectionWorker.NAME)
    suplaClientStateHolder.handleEvent(SuplaClientEvent.OnStart)
  }

  override fun onStop(owner: LifecycleOwner) {
    super.onStop(owner)
    WorkManager.getInstance(context).enqueueUniqueWork(
      FinishConnectionWorker.NAME,
      ExistingWorkPolicy.REPLACE,
      FinishConnectionWorker.build()
    )
  }
}
