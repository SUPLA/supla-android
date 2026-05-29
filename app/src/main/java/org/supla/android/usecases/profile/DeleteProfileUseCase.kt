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
import kotlinx.coroutines.rx3.rxCompletable
import org.supla.android.core.SuplaAppProvider
import org.supla.android.core.infrastructure.suplaclient.SingleCallProvider
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.di.CoroutineDispatchers
import org.supla.android.lib.SuplaClient
import org.supla.android.usecases.client.DisconnectUseCase
import org.supla.android.widget.WidgetManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteProfileUseCase @Inject constructor(
  @param:ApplicationContext private val context: Context,
  private val deleteProfileRelatedDataUseCase: DeleteProfileRelatedDataUseCase,
  private val activateProfileUseCase: ActivateProfileUseCase,
  private val suplaClientStateHolder: SuplaClientStateHolder,
  private val encryptedPreferences: EncryptedPreferences,
  private val profileRepository: ProfileRepository,
  private val singleCallProvider: SingleCallProvider,
  private val disconnectUseCase: DisconnectUseCase,
  private val suplaAppProvider: SuplaAppProvider,
  private val dispatchers: CoroutineDispatchers,
  private val widgetManager: WidgetManager
) {

  operator fun invoke(profileEntity: ProfileEntity): Completable =
    if (!profileEntity.active) {
      deleteProfile(profileEntity)
    } else {
      removeActiveProfile(profileEntity)
    }

  private fun removeActiveProfile(profile: ProfileEntity): Completable =
    disconnectUseCase()
      .andThen(profileRepository.findAllProfiles().firstOrError())
      .map { profiles -> profiles.filter { !it.active } }
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

  private fun removeLastProfile(profile: ProfileEntity): Completable =
    deleteProfile(profile)
      .andThen(Completable.fromRunnable { suplaClientStateHolder.handleEvent(SuplaClientEvent.NoAccount) })

  private fun removeAndActivate(toRemove: ProfileEntity, toActivate: ProfileEntity): Completable =
    activateProfileUseCase(toActivate.id, true)
      .andThen(deleteProfile(toRemove))

  private fun startClient() {
    suplaAppProvider.provide().SuplaClientInitIfNeed(context)
  }

  private fun deleteProfile(profileEntity: ProfileEntity): Completable =
    Completable.fromRunnable {
      try {
        singleCallProvider.provide(profileEntity.id).registerPushNotificationClientToken(SuplaClient.SUPLA_APP_ID, "", profileEntity)
      } catch (ex: Exception) {
        Timber.w(ex, "Token cleanup failed while profile removal (profile id: `${profileEntity.id}`)")
      }
      widgetManager.onProfileRemoved(profileEntity.id)
    }
      .andThen(rxCompletable(dispatchers.io()) { encryptedPreferences.removeProfileCredentials(profileEntity.id) })
      .andThen(profileRepository.deleteProfile(profileEntity))
      .let { completable ->
        profileEntity.id.let { deleteProfileRelatedDataUseCase(it).andThen(completable) }
      }

  interface ProfileRemover {
    fun deleteByProfile(profileId: Long): Completable
  }
}
