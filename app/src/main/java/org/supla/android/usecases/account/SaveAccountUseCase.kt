package org.supla.android.usecases.account

import io.reactivex.rxjava3.core.Completable
import org.supla.android.Preferences
import org.supla.android.db.AuthProfileItem
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.profile.ProfileManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveAccountUseCase @Inject constructor(
  private val profileManager: ProfileManager,
  private val preferences: Preferences,
  private val profileIdHolder: ProfileIdHolder
) {

  operator fun invoke(profile: AuthProfileItem): Completable =
    profileManager.getAllProfiles()
      .flatMapCompletable { validation(profile, it) }
      .andThen(save(profile))
      .andThen(setAccountRegistered(profile))

  private fun save(profile: AuthProfileItem): Completable = if (profile.id == null) {
    profileManager.create(profile)
  } else {
    profileManager.update(profile)
  }

  private fun validation(
    profile: AuthProfileItem,
    allProfiles: List<AuthProfileItem>
  ): Completable = Completable.fromRunnable {

    if (allProfiles.isNotEmpty() && profile.name.trim().isEmpty() && preferences.isAnyAccountRegistered) {
      throw SaveAccountException.EmptyName
    } else if (isNameDuplicated(profile, allProfiles)) {
      throw SaveAccountException.DuplicatedName
    } else if (!profile.authInfo.isAuthDataComplete) {
      throw SaveAccountException.DataIncomplete
    }
  }

  private fun isNameDuplicated(
    profile: AuthProfileItem,
    allProfiles: List<AuthProfileItem>
  ): Boolean =
    allProfiles
      .filter { it.id != profile.id }
      .firstOrNull { it.name == profile.name.trim() } != null

  private fun setAccountRegistered(profile: AuthProfileItem): Completable = Completable.fromRunnable {
    if (preferences.isAnyAccountRegistered.not()) {
      preferences.isAnyAccountRegistered = true
      profileIdHolder.profileId = profile.id
    }
  }

  sealed class SaveAccountException(message: String? = null, parent: RuntimeException? = null) : RuntimeException(message, parent) {
    object EmptyName : SaveAccountException()
    object DuplicatedName : SaveAccountException()
    object DataIncomplete : SaveAccountException()
  }
}