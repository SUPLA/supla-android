package org.supla.android.usecases.profile

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Completable
import org.supla.android.Preferences
import org.supla.android.core.SuplaAppProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.db.AuthProfileItem
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.profile.ProfileManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteProfileUseCase @Inject constructor(
  @ApplicationContext private val context: Context,
  private val profileManager: ProfileManager,
  private val suplaClientProvider: SuplaClientProvider,
  private val suplaAppProvider: SuplaAppProvider,
  private val preferences: Preferences,
  private val profileIdHolder: ProfileIdHolder,
  private val activateProfileUseCase: ActivateProfileUseCase
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
    Completable.fromRunnable { stopClient() }
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
        }
      )

  private fun removeAndActivate(toRemove: AuthProfileItem, toActivate: AuthProfileItem): Completable =
    activateProfileUseCase(toActivate.id, true)
      .andThen(profileManager.delete(toRemove.id))

  private fun stopClient() {
    suplaClientProvider.provide()?.let {
      it.cancel()
      it.join()
    }
  }

  private fun startClient() {
    suplaAppProvider.provide().SuplaClientInitIfNeed(context)
  }
}
