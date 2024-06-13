package org.supla.android.usecases.lock
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

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.model.general.LockScreenSettings

class GetLockScreenSettingUseCaseTest {
  @MockK
  private lateinit var encryptedPreferences: EncryptedPreferences

  @InjectMockKs
  private lateinit var useCase: GetLockScreenSettingUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should get account when pin set`() {
    // given
    every { encryptedPreferences.lockScreenSettings } returns LockScreenSettings(LockScreenScope.ACCOUNTS, "sum", true)

    // when
    val scope = useCase.invoke()

    // then
    assertThat(scope).isEqualTo(LockScreenScope.ACCOUNTS)
  }

  @Test
  fun `should get none when pin not set`() {
    // given
    every { encryptedPreferences.lockScreenSettings } returns LockScreenSettings(LockScreenScope.ACCOUNTS, null, true)

    // when
    val scope = useCase.invoke()

    // then
    assertThat(scope).isEqualTo(LockScreenScope.NONE)
  }

  @Test
  fun `should get none when none set`() {
    // given
    every { encryptedPreferences.lockScreenSettings } returns LockScreenSettings(LockScreenScope.NONE, null, true)

    // when
    val scope = useCase.invoke()

    // then
    assertThat(scope).isEqualTo(LockScreenScope.NONE)
  }
}
