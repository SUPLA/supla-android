package org.supla.android.tools
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

import android.os.Process
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuplaThreading @Inject constructor() {
  val schedulers: SuplaRxSchedulers
    get() = SuplaRxSchedulers

  val dispatchers: SuplaCoroutineDispatchers
    get() = SuplaCoroutineDispatchers

  suspend fun <T> io(block: suspend CoroutineScope.() -> T): T = withContext(dispatchers.io, block)
  suspend fun <T> ui(block: suspend CoroutineScope.() -> T): T = withContext(dispatchers.main, block)
  suspend fun <T> low(block: suspend CoroutineScope.() -> T): T = withContext(dispatchers.low, block)
}

object SuplaCoroutineDispatchers {
  val io: CoroutineDispatcher
    get() = Dispatchers.IO

  val main: CoroutineDispatcher
    get() = Dispatchers.Main

  val low: CoroutineDispatcher
    field: ExecutorCoroutineDispatcher = Executors.newSingleThreadExecutor { task ->
      Thread(
        {
          Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST)
          task.run()
        },
        "LowPriorityDispatcher"
      )
    }.asCoroutineDispatcher()
}

object SuplaRxSchedulers {
  val io: Scheduler
    get() = Schedulers.io()
  val ui: Scheduler
    get() = AndroidSchedulers.mainThread()
  val computation: Scheduler
    get() = Schedulers.computation()
}