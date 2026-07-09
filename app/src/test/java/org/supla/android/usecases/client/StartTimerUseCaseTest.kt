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

import io.mockk.*
import io.mockk.Called
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.tools.VibrationHelper

class StartTimerUseCaseTest {

  @MockK
  private lateinit var suplaClientProvider: SuplaClientProvider

  @MockK
  private lateinit var vibrationHelper: VibrationHelper

  @InjectMockKs
  private lateinit var useCase: StartTimerUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should arm timer and vibrate`() {
    // given
    val remoteId = 123
    val turnOn = false
    val duration = 151
    val durationMs = duration.times(1000)

    val suplaClient: SuplaClientApi = mockk()
    every { suplaClient.timerArm(remoteId, turnOn, durationMs) } returns true
    every { vibrationHelper.vibrate() } just Runs
    every { suplaClientProvider.provide() } returns suplaClient

    // when
    val observer = useCase.invoke(remoteId, turnOn, duration).test()

    // then
    observer.assertComplete()

    verify {
      suplaClient.timerArm(remoteId, turnOn, durationMs)
      suplaClientProvider.provide()
      vibrationHelper.vibrate()
    }
    confirmVerified(suplaClient, suplaClientProvider, vibrationHelper)
  }

  @Test
  fun `should not arm timer when invalid time provided`() {
    // given
    val remoteId = 123
    val turnOn = false
    val duration = 0

    // when
    val observer = useCase.invoke(remoteId, turnOn, duration).test()

    // then
    observer.assertFailure(StartTimerUseCase.InvalidTimeException::class.java)
    verify {
      suplaClientProvider wasNot Called
      vibrationHelper wasNot Called
    }
  }

  @Test
  fun `should arm timer and do not vibrate when timer not armed successfully`() {
    // given
    val remoteId = 123
    val turnOn = false
    val duration = 151
    val durationMs = duration.times(1000)

    val suplaClient: SuplaClientApi = mockk()
    every { suplaClient.timerArm(remoteId, turnOn, durationMs) } returns false

    every { suplaClientProvider.provide() } returns suplaClient

    // when
    val observer = useCase.invoke(remoteId, turnOn, duration).test()

    // then
    observer.assertComplete()

    verify { suplaClient.timerArm(remoteId, turnOn, durationMs) }
    verify { suplaClientProvider.provide() }
    confirmVerified(suplaClient, suplaClientProvider)
    verify {
      vibrationHelper wasNot Called
    }
  }

  @Test
  fun `should not fail when no supla client is provided`() {
    // given
    val remoteId = 123
    val turnOn = false
    val duration = 151

    every { suplaClientProvider.provide() } returns null

    // when
    val observer = useCase.invoke(remoteId, turnOn, duration).test()

    // then
    observer.assertComplete()

    verify { suplaClientProvider.provide() }
    confirmVerified(suplaClientProvider)
    verify {
      vibrationHelper wasNot Called
    }
  }
}
