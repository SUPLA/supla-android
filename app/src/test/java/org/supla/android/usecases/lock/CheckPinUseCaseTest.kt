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
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.infrastructure.ShaHashHelper
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.model.general.LockScreenSettings
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.features.lockscreen.UnlockAction

class CheckPinUseCaseTest {
  @MockK
  private lateinit var encryptedPreferences: EncryptedPreferences

  @MockK
  private lateinit var shaHashHelper: ShaHashHelper

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK
  private lateinit var profileRepository: RoomProfileRepository

  @MockK
  private lateinit var suplaClientStateHolder: SuplaClientStateHolder

  @InjectMockKs
  private lateinit var useCase: CheckPinUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should check pin - success - authorize application`() {
    // given
    val pin = "pin1"
    val pinSum = "pin1sum"
    val action = UnlockAction.AuthorizeApplication

    val lockScreenSettings = LockScreenSettings(LockScreenScope.APPLICATION, pinSum, false, 3, 14)

    every { encryptedPreferences.lockScreenSettings } returns lockScreenSettings
    every { encryptedPreferences.lockScreenSettings = lockScreenSettings.copy(failsCount = 0, lockTime = null) } answers {}
    every { shaHashHelper.getHash(pin) } returns pinSum
    every { profileRepository.findActiveProfile() } returns Single.just(mockk())
    every { suplaClientStateHolder.handleEvent(SuplaClientEvent.Unlock) } answers {}

    // when
    val observer = useCase.invoke(action, CheckPinUseCase.PinAction.CheckPin(pin)).test()

    // then
    observer.assertComplete()
    observer.assertResult(CheckPinUseCase.Result.Unlocked)

    verify {
      shaHashHelper.getHash(pin)
      encryptedPreferences.lockScreenSettings
      encryptedPreferences.lockScreenSettings = lockScreenSettings.copy(failsCount = 0, lockTime = null)
      profileRepository.findActiveProfile()
      suplaClientStateHolder.handleEvent(SuplaClientEvent.Unlock)
    }
    confirmVerified(shaHashHelper, encryptedPreferences, profileRepository, suplaClientStateHolder)
  }

  @Test
  fun `should check pin - success - authorize application - no account`() {
    // given
    val pin = "pin1"
    val pinSum = "pin1sum"
    val action = UnlockAction.AuthorizeApplication

    val lockScreenSettings = LockScreenSettings(LockScreenScope.APPLICATION, pinSum, false)

    every { encryptedPreferences.lockScreenSettings } returns lockScreenSettings
    every { encryptedPreferences.lockScreenSettings = lockScreenSettings } answers {}
    every { shaHashHelper.getHash(pin) } returns pinSum
    every { profileRepository.findActiveProfile() } returns Single.error(NoSuchElementException())
    every { suplaClientStateHolder.handleEvent(SuplaClientEvent.NoAccount) } answers {}

    // when
    val observer = useCase.invoke(action, CheckPinUseCase.PinAction.CheckPin(pin)).test()

    // then
    observer.assertComplete()
    observer.assertResult(CheckPinUseCase.Result.UnlockedNoAccount)

    verify {
      shaHashHelper.getHash(pin)
      encryptedPreferences.lockScreenSettings
      encryptedPreferences.lockScreenSettings = lockScreenSettings
      profileRepository.findActiveProfile()
      suplaClientStateHolder.handleEvent(SuplaClientEvent.NoAccount)
    }
    confirmVerified(shaHashHelper, encryptedPreferences, profileRepository, suplaClientStateHolder)
  }

  @Test
  fun `should check pin - success - turn off pin with biometrics`() {
    // given
    val pinSum = "pin1sum"
    val action = UnlockAction.TurnOffPin

    val lockScreenSettings = LockScreenSettings(LockScreenScope.APPLICATION, pinSum, false)

    every { encryptedPreferences.lockScreenSettings } returns lockScreenSettings
    every { encryptedPreferences.lockScreenSettings = lockScreenSettings } answers {}
    every { encryptedPreferences.lockScreenSettings = LockScreenSettings.DEFAULT } answers {}
    every { profileRepository.findActiveProfile() } returns Single.just(mockk())

    // when
    val observer = useCase.invoke(action, CheckPinUseCase.PinAction.BiometricGranted).test()

    // then
    observer.assertComplete()
    observer.assertResult(CheckPinUseCase.Result.Unlocked)

    verify {
      encryptedPreferences.lockScreenSettings
      encryptedPreferences.lockScreenSettings = lockScreenSettings
      encryptedPreferences.lockScreenSettings = LockScreenSettings.DEFAULT
      profileRepository.findActiveProfile()
    }
    confirmVerified(shaHashHelper, encryptedPreferences, profileRepository, suplaClientStateHolder)
  }

  @Test
  fun `should check pin - success - confirm authorize application`() {
    // given
    val pinSum = "pin1sum"
    val action = UnlockAction.ConfirmAuthorizeApplication

    val lockScreenSettings = LockScreenSettings(LockScreenScope.APPLICATION, pinSum, false)

    every { encryptedPreferences.lockScreenSettings } returns lockScreenSettings
    every { encryptedPreferences.lockScreenSettings = lockScreenSettings } answers {}
    every { encryptedPreferences.lockScreenSettings = lockScreenSettings.copy(scope = LockScreenScope.APPLICATION) } answers {}
    every { profileRepository.findActiveProfile() } returns Single.just(mockk())

    // when
    val observer = useCase.invoke(action, CheckPinUseCase.PinAction.BiometricGranted).test()

    // then
    observer.assertComplete()
    observer.assertResult(CheckPinUseCase.Result.Unlocked)

    verify {
      encryptedPreferences.lockScreenSettings
      encryptedPreferences.lockScreenSettings = lockScreenSettings
      encryptedPreferences.lockScreenSettings = lockScreenSettings.copy(scope = LockScreenScope.APPLICATION)
      profileRepository.findActiveProfile()
    }
    confirmVerified(shaHashHelper, encryptedPreferences, profileRepository, suplaClientStateHolder)
  }

  @Test
  fun `should check pin - success - confirm authorize account`() {
    // given
    val pinSum = "pin1sum"
    val action = UnlockAction.ConfirmAuthorizeAccounts

    val lockScreenSettings = LockScreenSettings(LockScreenScope.APPLICATION, pinSum, false)

    every { encryptedPreferences.lockScreenSettings } returns lockScreenSettings
    every { encryptedPreferences.lockScreenSettings = lockScreenSettings } answers {}
    every { encryptedPreferences.lockScreenSettings = lockScreenSettings.copy(scope = LockScreenScope.ACCOUNTS) } answers {}
    every { profileRepository.findActiveProfile() } returns Single.just(mockk())

    // when
    val observer = useCase.invoke(action, CheckPinUseCase.PinAction.BiometricGranted).test()

    // then
    observer.assertComplete()
    observer.assertResult(CheckPinUseCase.Result.Unlocked)

    verify {
      encryptedPreferences.lockScreenSettings
      encryptedPreferences.lockScreenSettings = lockScreenSettings
      encryptedPreferences.lockScreenSettings = lockScreenSettings.copy(scope = LockScreenScope.ACCOUNTS)
      profileRepository.findActiveProfile()
    }
    confirmVerified(shaHashHelper, encryptedPreferences, profileRepository, suplaClientStateHolder)
  }

  @Test
  fun `should check pin - success - authorize accounts create`() {
    // given
    val pinSum = "pin1sum"
    val action = UnlockAction.AuthorizeAccountsCreate

    val lockScreenSettings = LockScreenSettings(LockScreenScope.APPLICATION, pinSum, false)

    every { encryptedPreferences.lockScreenSettings } returns lockScreenSettings
    every { encryptedPreferences.lockScreenSettings = lockScreenSettings } answers {}
    every { profileRepository.findActiveProfile() } returns Single.just(mockk())

    // when
    val observer = useCase.invoke(action, CheckPinUseCase.PinAction.BiometricGranted).test()

    // then
    observer.assertComplete()
    observer.assertResult(CheckPinUseCase.Result.Unlocked)

    verify {
      encryptedPreferences.lockScreenSettings
      encryptedPreferences.lockScreenSettings = lockScreenSettings
      profileRepository.findActiveProfile()
    }
    confirmVerified(shaHashHelper, encryptedPreferences, profileRepository, suplaClientStateHolder)
  }

  @Test
  fun `should check pin - success - authorize accounts edit`() {
    // given
    val pinSum = "pin1sum"
    val action = UnlockAction.AuthorizeAccountsEdit(123)

    val lockScreenSettings = LockScreenSettings(LockScreenScope.APPLICATION, pinSum, false)

    every { encryptedPreferences.lockScreenSettings } returns lockScreenSettings
    every { encryptedPreferences.lockScreenSettings = lockScreenSettings } answers {}
    every { profileRepository.findActiveProfile() } returns Single.just(mockk())

    // when
    val observer = useCase.invoke(action, CheckPinUseCase.PinAction.BiometricGranted).test()

    // then
    observer.assertComplete()
    observer.assertResult(CheckPinUseCase.Result.Unlocked)

    verify {
      encryptedPreferences.lockScreenSettings
      encryptedPreferences.lockScreenSettings = lockScreenSettings
      profileRepository.findActiveProfile()
    }
    confirmVerified(shaHashHelper, encryptedPreferences, profileRepository, suplaClientStateHolder)
  }

  @Test
  fun `should check pin - failure`() {
    // given
    val pin = "pin1"
    val pinSum = "pin1sum"
    val action = UnlockAction.AuthorizeApplication

    val lockScreenSettings = LockScreenSettings(LockScreenScope.APPLICATION, pinSum, false)

    every { encryptedPreferences.lockScreenSettings } returns lockScreenSettings
    every { encryptedPreferences.lockScreenSettings = lockScreenSettings.copy(failsCount = 1) } answers {}
    every { shaHashHelper.getHash(pin) } returns "other pin sum"

    // when
    val observer = useCase.invoke(action, CheckPinUseCase.PinAction.CheckPin(pin)).test()

    // then
    observer.assertComplete()
    observer.assertResult(CheckPinUseCase.Result.Failure)

    verify {
      shaHashHelper.getHash(pin)
      encryptedPreferences.lockScreenSettings
      encryptedPreferences.lockScreenSettings = lockScreenSettings.copy(failsCount = 1)
    }
    confirmVerified(shaHashHelper, encryptedPreferences, profileRepository, suplaClientStateHolder)
  }

  @Test
  fun `should check pin - failure - biometric rejected - 5'th time`() {
    testWrongPin(5, 5000)
  }

  @Test
  fun `should check pin - failure - biometric rejected - 10'th time`() {
    testWrongPin(10, 60000)
  }

  @Test
  fun `should check pin - failure - biometric rejected - 15'th time`() {
    testWrongPin(15, 300000)
  }

  @Test
  fun `should check pin - failure - biometric rejected - 20'th time`() {
    testWrongPin(20, 600000)
  }

  @Test
  fun `should check pin - failure - biometric rejected - 21'st time`() {
    testWrongPin(21, 600000)
  }

  private fun testWrongPin(failsCount: Int, lockTime: Int) {
    // given
    val pinSum = "pin1sum"
    val action = UnlockAction.AuthorizeApplication
    val currentTimestamp = 1_000_000L

    val lockScreenSettings = LockScreenSettings(LockScreenScope.APPLICATION, pinSum, false, failsCount, null)

    every { dateProvider.currentTimestamp() } returns currentTimestamp
    every { encryptedPreferences.lockScreenSettings } returns lockScreenSettings
    every {
      encryptedPreferences.lockScreenSettings =
        lockScreenSettings.copy(failsCount = failsCount.plus(1), lockTime = currentTimestamp.plus(lockTime))
    } answers {}

    // when
    val observer = useCase.invoke(action, CheckPinUseCase.PinAction.BiometricRejected).test()

    // then
    observer.assertComplete()
    observer.assertResult(CheckPinUseCase.Result.Failure)

    verify {
      encryptedPreferences.lockScreenSettings
      encryptedPreferences.lockScreenSettings =
        lockScreenSettings.copy(failsCount = failsCount.plus(1), lockTime = currentTimestamp.plus(lockTime))
    }
    confirmVerified(shaHashHelper, encryptedPreferences, profileRepository, suplaClientStateHolder)
  }
}
