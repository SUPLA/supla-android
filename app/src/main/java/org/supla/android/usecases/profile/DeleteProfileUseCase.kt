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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Completable
import org.supla.android.Preferences
import org.supla.android.core.SuplaAppProvider
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.db.AuthProfileItem
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.profile.ProfileManager
import org.supla.android.usecases.client.DisconnectUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteProfileUseCase @Inject constructor(
  @ApplicationContext private val context: Context,
  private val profileManager: ProfileManager,
  private val suplaAppProvider: SuplaAppProvider,
  private val preferences: Preferences,
  private val profileIdHolder: ProfileIdHolder,
  private val activateProfileUseCase: ActivateProfileUseCase,
  private val suplaClientStateHolder: SuplaClientStateHolder,
  private val disconnectUseCase: DisconnectUseCase
) {
  operator fun invoke(profileId: Long): Completable =
    profileManager.read(profileId).toSingle()
      .flatMapCompletable(this::removeProfile)

  private fun removeProfile(profile: AuthProfileItem): Completable {
    return if (!profile.isActive) {
      profileManager.delete(profile.id)
    } else {
      removeActiveProfile(profile)
    }
  }

  private fun removeActiveProfile(profile: AuthProfileItem): Completable =
    disconnectUseCase()
      .andThen(profileManager.getAllProfiles())
      .map { profiles -> profiles.filter { !it.isActive } }
      .flatMapCompletable { profiles ->
        return@flatMapCompletable if (profiles.isEmpty()) {
          removeLastProfile(profile)
        } else {
          removeAndActivate(
            toRemove = profile,
            toActivate = profiles.first()
          ).andThen(Completable.fromRunnable { startClient() })
        }
      }

  private fun removeLastProfile(profile: AuthProfileItem): Completable =
    profileManager.delete(profile.id)
      .andThen(
        Completable.fromRunnable {
          preferences.isAnyAccountRegistered = false
          profileIdHolder.profileId = null

          suplaClientStateHolder.handleEvent(SuplaClientEvent.NoAccount)
        }
      )

  private fun removeAndActivate(toRemove: AuthProfileItem, toActivate: AuthProfileItem): Completable =
    activateProfileUseCase(toActivate.id, true)
      .andThen(profileManager.delete(toRemove.id))

  private fun startClient() {
    suplaAppProvider.provide().SuplaClientInitIfNeed(context)
  }
}
