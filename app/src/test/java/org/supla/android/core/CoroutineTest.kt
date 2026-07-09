package org.supla.android.core
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

import androidx.annotation.CallSuper
import io.mockk.coEvery
import kotlinx.coroutines.CoroutineScope
import org.supla.android.extensions.isNotNull
import org.supla.android.tools.SuplaSchedulers

interface CoroutineTest {
  val mainDispatcherRule: MainDispatcherRule?
  val schedulers: SuplaSchedulers

  @CallSuper
  fun setUp() {
    if (mainDispatcherRule.isNotNull) {
      coEvery { schedulers.io<Any?>(any()) } answers {
        val block = arg<suspend CoroutineScope.() -> Any?>(0)
        kotlinx.coroutines.runBlocking { block(this) }
      }
      coEvery { schedulers.ui<Any?>(any()) } answers {
        val block = arg<suspend CoroutineScope.() -> Any?>(0)
        kotlinx.coroutines.runBlocking { block(this) }
      }
    }
  }
}
