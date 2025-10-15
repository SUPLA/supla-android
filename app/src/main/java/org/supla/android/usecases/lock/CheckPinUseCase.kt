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

import io.reactivex.rxjava3.core.Single
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.infrastructure.ShaHashHelper
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.model.general.LockScreenSettings
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.features.lockscreen.UnlockAction
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val FIRST_STAGE_LOCK_TIME_SECS = 5
private const val SECOND_STAGE_LOCK_TIME_SECS = 60
private const val THIRD_STAGE_LOCK_TIME_SECS = 300
private const val FOURTH_STAGE_LOCK_TIME_SECS = 600

@Singleton
class CheckPinUseCase @Inject constructor(
  private val encryptedPreferences: EncryptedPreferences,
  private val shaHashHelper: ShaHashHelper,
  private val dateProvider: DateProvider,
  private val profileRepository: RoomProfileRepository,
  private val suplaClientStateHolder: SuplaClientStateHolder,
) {

  operator fun invoke(unlockAction: UnlockAction, pinAction: PinAction): Single<Result> = Single.fromCallable {
    val lockScreenSettings = encryptedPreferences.lockScreenSettings

    return@fromCallable try {
      if (pinAction.isAuthorized(shaHashHelper, lockScreenSettings.pinSum)) {
        encryptedPreferences.lockScreenSettings = lockScreenSettings.copy(failsCount = 0, lockTime = null)
        Result.Unlocked
      } else {
        onWrongPin(lockScreenSettings)
      }
    } catch (exception: Exception) {
      Timber.e(exception, "Could not check PIN!")
      onWrongPin(lockScreenSettings)
    }
  }.flatMap { result ->
    if (result == Result.Unlocked) {
      profileRepository.findActiveProfile()
        .map { Result.Unlocked as Result }
        .onErrorReturn { Result.UnlockedNoAccount }
        .map { performActionSpecificWork(unlockAction, it) }
    } else {
      Single.just(result)
    }
  }

  private fun onWrongPin(lockScreenSettings: LockScreenSettings): Result {
    val lockedTime = when {
      lockScreenSettings.failsCount == 5 -> dateProvider.currentTimestamp().plus(FIRST_STAGE_LOCK_TIME_SECS.times(1000))
      lockScreenSettings.failsCount == 10 -> dateProvider.currentTimestamp().plus(SECOND_STAGE_LOCK_TIME_SECS.times(1000))
      lockScreenSettings.failsCount == 15 -> dateProvider.currentTimestamp().plus(THIRD_STAGE_LOCK_TIME_SECS.times(1000))
      lockScreenSettings.failsCount >= 20 -> dateProvider.currentTimestamp().plus(FOURTH_STAGE_LOCK_TIME_SECS.times(1000))
      else -> lockScreenSettings.lockTime
    }
    encryptedPreferences.lockScreenSettings = lockScreenSettings.copy(
      failsCount = lockScreenSettings.failsCount + 1,
      lockTime = lockedTime
    )

    return Result.Failure
  }

  private fun performActionSpecificWork(unlockAction: UnlockAction, result: Result): Result {
    when (unlockAction) {
      UnlockAction.AuthorizeApplication ->
        if (result == Result.Unlocked) {
          suplaClientStateHolder.handleEvent(SuplaClientEvent.Unlock)
        } else if (result == Result.UnlockedNoAccount) {
          suplaClientStateHolder.handleEvent(SuplaClientEvent.NoAccount)
        }

      UnlockAction.TurnOffPin -> {
        encryptedPreferences.lockScreenSettings = LockScreenSettings.DEFAULT
      }

      UnlockAction.ConfirmAuthorizeApplication -> {
        encryptedPreferences.lockScreenSettings = encryptedPreferences.lockScreenSettings.copy(scope = LockScreenScope.APPLICATION)
      }

      UnlockAction.ConfirmAuthorizeAccounts -> {
        encryptedPreferences.lockScreenSettings = encryptedPreferences.lockScreenSettings.copy(scope = LockScreenScope.ACCOUNTS)
      }

      UnlockAction.AuthorizeAccountsCreate,
      is UnlockAction.AuthorizeAccountsEdit -> {
      } // Nothing to do
    }

    return result
  }

  sealed interface Result {
    data object Unlocked : Result
    data object UnlockedNoAccount : Result
    data object Failure : Result
  }

  sealed interface PinAction {
    fun isAuthorized(shaHashHelper: ShaHashHelper, pinSum: String?): Boolean

    data class CheckPin(val pin: String) : PinAction {
      override fun isAuthorized(shaHashHelper: ShaHashHelper, pinSum: String?) =
        shaHashHelper.getHash(pin) == pinSum
    }

    data object BiometricGranted : PinAction {
      override fun isAuthorized(shaHashHelper: ShaHashHelper, pinSum: String?) = true
    }

    data object BiometricRejected : PinAction {
      override fun isAuthorized(shaHashHelper: ShaHashHelper, pinSum: String?) = false
    }
  }
}
