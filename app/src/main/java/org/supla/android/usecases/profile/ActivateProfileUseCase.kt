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

import androidx.room.rxjava3.EmptyResultSetException
import io.reactivex.rxjava3.core.Completable
import org.supla.android.core.networking.suplacloud.SuplaCloudConfigHolder
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.usecases.client.ReconnectUseCase
import org.supla.android.usecases.icon.LoadUserIconsIntoCacheUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivateProfileUseCase @Inject constructor(
  private val profileRepository: RoomProfileRepository,
  private val profileIdHolder: ProfileIdHolder,
  private val suplaCloudConfigHolder: SuplaCloudConfigHolder,
  private val loadUserIconsIntoCacheUseCase: LoadUserIconsIntoCacheUseCase,
  private val reconnectUseCase: ReconnectUseCase
) {

  operator fun invoke(id: Long, force: Boolean = false): Completable =
    profileRepository.findActiveProfile()
      .flatMapCompletable {
        if (it.id == id && !force) {
          return@flatMapCompletable Completable.complete()
        }

        activate(id)
      }
      .onErrorResumeNext { error ->
        if (error is EmptyResultSetException) {
          // when no active profile found, just activate the selected one.
          activate(id)
        } else {
          Completable.error(error)
        }
      }

  private fun activate(id: Long) =
    profileRepository.activateProfile(id)
      .andThen(
        Completable.fromRunnable {
          profileIdHolder.profileId = id
          suplaCloudConfigHolder.clean()
        }
      )
      .andThen(reconnectUseCase())
      .andThen(loadUserIconsIntoCacheUseCase())
}
