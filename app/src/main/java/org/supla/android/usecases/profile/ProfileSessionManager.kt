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

import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.extensions.subscribeBy
import org.supla.android.tools.SuplaThreading
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileSessionManager @Inject constructor(
  private val activateProfileUseCase: ActivateProfileUseCase,
  private val deleteProfileUseCase: DeleteProfileUseCase,
  private val profileRepository: ProfileRepository,
  private val threading: SuplaThreading
) {

  private val disposables = CompositeDisposable()

  fun activateProfile(
    id: Long,
    force: Boolean = false,
    onComplete: () -> Unit = {},
    onError: (Throwable) -> Unit = {}
  ) {
    activateProfileUseCase(id, force)
      .subscribeOn(threading.schedulers.io)
      .observeOn(threading.schedulers.ui)
      .subscribeBy(
        onComplete = onComplete,
        onError = { error ->
          Timber.e(error, "Profile activation failed")
          onError(error)
        }
      )
      .let(disposables::add)
  }

  fun deleteProfile(
    id: Long,
    onSuccess: (RemovalResult) -> Unit,
    onError: (Throwable) -> Unit
  ) {
    profileRepository.findProfile(id)
      .flatMap(this::deleteAndGetReturnInfo)
      .subscribeOn(threading.schedulers.io)
      .observeOn(threading.schedulers.ui)
      .subscribeBy(
        onSuccess = onSuccess,
        onError = { error ->
          Timber.e(error, "Profile deletion failed")
          onError(error)
        }
      )
      .let(disposables::add)
  }

  private fun deleteAndGetReturnInfo(profile: ProfileEntity) =
    deleteProfileUseCase(profile)
      .andThen(profileRepository.findAllProfiles())
      .map { RemovalResult(profile.serverForCurrentAuthMethod, it.isEmpty()) }
      .firstOrError()

  data class RemovalResult(
    val serverAddress: String?,
    val noAccountsRegistered: Boolean
  )
}
