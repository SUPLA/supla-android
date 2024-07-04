package org.supla.android.usecases.client
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
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import org.junit.Before
import org.junit.Test
import org.supla.android.core.SuplaAppApi
import org.supla.android.core.SuplaAppProvider

class ReconnectUseCaseTest {
  @MockK
  private lateinit var applicationContext: Context

  @MockK
  private lateinit var disconnectUseCase: DisconnectUseCase

  @MockK
  private lateinit var suplaAppProvider: SuplaAppProvider

  @InjectMockKs
  private lateinit var useCase: ReconnectUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should disconnect and initialize supla client`() {
    // given
    val suplaAppApi: SuplaAppApi = mockk {
      every { SuplaClientInitIfNeed(applicationContext) } returns mockk()
    }
    every { suplaAppProvider.provide() } returns suplaAppApi
    every { disconnectUseCase.invoke() } returns Completable.complete()

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    verify {
      disconnectUseCase.invoke()
      suplaAppApi.SuplaClientInitIfNeed(applicationContext)
    }
    confirmVerified(disconnectUseCase, suplaAppApi)
  }
}
