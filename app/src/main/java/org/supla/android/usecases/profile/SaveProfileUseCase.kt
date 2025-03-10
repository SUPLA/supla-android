package org.supla.android.usecases.profile
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

import io.reactivex.rxjava3.core.Completable
import org.supla.android.Preferences
import org.supla.android.db.AuthProfileItem
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.profile.ProfileManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveProfileUseCase @Inject constructor(
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
    if (allProfiles.isNotEmpty() && profile.name.isEmpty() && preferences.isAnyAccountRegistered) {
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
      // New profile name is trimmed by creation. Old profile name may not be trimmed!
      .firstOrNull { it.name.trim() == profile.name } != null

  private fun setAccountRegistered(profile: AuthProfileItem): Completable = Completable.fromRunnable {
    if (preferences.isAnyAccountRegistered.not()) {
      preferences.isAnyAccountRegistered = true
      profileIdHolder.profileId = profile.id
    }
  }

  sealed class SaveAccountException : RuntimeException(null, null) {
    data object EmptyName : SaveAccountException() {
      private fun readResolve(): Any = EmptyName
    }

    data object DuplicatedName : SaveAccountException() {
      private fun readResolve(): Any = DuplicatedName
    }

    data object DataIncomplete : SaveAccountException() {
      private fun readResolve(): Any = DataIncomplete
    }
  }
}
