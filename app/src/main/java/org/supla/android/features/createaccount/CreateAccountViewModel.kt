package org.supla.android.features.createaccount
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
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.deleteaccountweb.DeleteAccountWebFragment
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.client.ReconnectUseCase
import org.supla.android.usecases.profile.DeleteProfileUseCase
import org.supla.android.usecases.profile.SaveProfileUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
  private val saveProfileUseCase: SaveProfileUseCase,
  private val deleteProfileUseCase: DeleteProfileUseCase,
  private val profileRepository: ProfileRepository,
  private val reconnectUseCase: ReconnectUseCase,
  schedulers: SuplaSchedulers
) : BaseViewModel<CreateAccountViewState, CreateAccountViewEvent>(CreateAccountViewState(), schedulers) {

  fun loadProfile(profileId: Long?) {
    profileRepository.findAllProfiles()
      .attach()
      .subscribeBy(
        onNext = { profiles ->
          updateState {
            it.copy(
              profileNameVisible = profiles.isNotEmpty(),
              deleteButtonVisible = profiles.isNotEmpty() && profileId != null
            )
          }
        }
      )
      .disposeBySelf()

    if (profileId != null) {
      profileRepository.findProfile(profileId)
        .attach()
        .subscribeBy(
          onSuccess = this::onProfileLoaded,
          onError = { throwable ->
            Timber.e(throwable, "Could not find profile")
          }
        )
        .disposeBySelf()
    }
  }

  private fun onProfileLoaded(profile: ProfileEntity) = profile.apply {
    updateState {
      it.copy(
        advancedMode = advancedMode == true,
        accountName = name,
        emailAddress = email ?: "",
        authorizeByEmail = emailAuth,
        autoServerAddress = serverAutoDetect,
        emailAddressServer = serverForEmail ?: "",
        accessIdentifier = accessId?.toAccessIdentifierString() ?: "0",
        accessIdentifierPassword = accessIdPassword ?: "",
        accessIdentifierServer = serverForAccessId ?: ""
      )
    }
  }

  fun changeMode(advancedMode: Boolean) {
    updateState { it.copy(advancedMode = advancedMode) }

    if (!advancedMode && (!currentState().authorizeByEmail || !currentState().autoServerAddress)) {
      sendEvent(CreateAccountViewEvent.ShowBasicModeUnavailableDialog)
      updateState { it.copy(advancedMode = true) }
    }
  }

  fun changeProfileName(profileName: String) {
    updateState { it.copy(accountName = profileName) }
  }

  fun changeEmail(email: String) {
    updateState { it.copy(emailAddress = email) }
  }

  fun changeAuthorizeByEmail(authorizeByEmail: Boolean) {
    updateState { it.copy(authorizeByEmail = authorizeByEmail) }
  }

  fun changeEmailAddressServer(server: String) {
    updateState { it.copy(emailAddressServer = server) }
  }

  fun changeAccessIdentifier(identifier: String) {
    updateState { it.copy(accessIdentifier = identifier) }
  }

  fun changeAccessIdentifierPassword(password: String) {
    updateState { it.copy(accessIdentifierPassword = password) }
  }

  fun changeAccessIdentifierServer(server: String) {
    updateState { it.copy(accessIdentifierServer = server) }
  }

  fun createAccount() {
    sendEvent(CreateAccountViewEvent.NavigateToCreateAccount)
  }

  fun toggleServerAutoDiscovery() {
    val currentState = currentState()
    val newState =
      if (currentState.autoServerAddress && currentState.emailAddressServer == "" && currentState.emailAddress.isNotEmpty()) {
        currentState.copy(
          autoServerAddress = false,
          emailAddressServer = currentState.emailAddress.substringAfter("@")
        )
      } else if (currentState.autoServerAddress) {
        currentState.copy(autoServerAddress = false)
      } else {
        currentState.copy(
          autoServerAddress = true,
          emailAddressServer = ""
        )
      }

    updateState { newState }
  }

  fun saveProfile(profileId: Long?) {
    getSaveSingle(profileId)
      .flatMap { saveProfileUseCase(it) }
      .flatMapCompletable {
        if (it.reconnectNeeded) {
          reconnectUseCase()
        } else {
          Completable.complete()
        }
      }
      .attach()
      .subscribeBy(
        onComplete = { sendEvent(CreateAccountViewEvent.Close) },
        onError = this::handleSaveError
      )
      .disposeBySelf()
  }

  override fun setLoading(loading: Boolean) {
    updateState { it.copy(loading = loading) }
  }

  private fun getSaveSingle(profileId: Long?): Single<ProfileEntity> =
    if (profileId == null) {
      Single.just(currentState().toProfileItem())
    } else {
      profileRepository.findProfile(profileId)
        .map { currentState().updateProfile(it) }
    }

  private fun handleSaveError(error: Throwable) = when (error) {
    is SaveProfileUseCase.SaveAccountException.EmptyName ->
      sendEvent(CreateAccountViewEvent.ShowEmptyNameDialog)
    is SaveProfileUseCase.SaveAccountException.DuplicatedName ->
      sendEvent(CreateAccountViewEvent.ShowDuplicatedNameDialog)
    is SaveProfileUseCase.SaveAccountException.DataIncomplete ->
      sendEvent(CreateAccountViewEvent.ShowRequiredDataMissingDialog)
    else -> sendEvent(CreateAccountViewEvent.ShowUnknownErrorDialog)
  }

  fun onDeleteProfile() {
    sendEvent(CreateAccountViewEvent.ConfirmDelete)
  }

  fun deleteProfile(profileId: Long?) {
    profileId?.let { id ->
      profileRepository.findProfile(id)
        .flatMap(this::deleteAndGetReturnInfo)
        .attachLoadable()
        .subscribeBy(
          onSuccess = {
            if (it.noAccountsRegistered) {
              sendEvent(CreateAccountViewEvent.RestartFlow)
            } else {
              sendEvent(CreateAccountViewEvent.Close)
            }
          },
          onError = { sendEvent(CreateAccountViewEvent.ShowRemovalFailureDialog) }
        )
    }
  }

  fun deleteProfileWithCloud(profileId: Long?) {
    profileId?.let { id ->
      profileRepository.findProfile(id)
        .flatMap(this::deleteAndGetReturnInfo)
        .attach()
        .subscribeBy(
          onSuccess = {
            val destination = if (it.noAccountsRegistered) {
              DeleteAccountWebFragment.EndDestination.RESTART
            } else {
              DeleteAccountWebFragment.EndDestination.CLOSE
            }

            sendEvent(CreateAccountViewEvent.NavigateToWebRemoval(it.serverAddress, destination))
          },
          onError = { sendEvent(CreateAccountViewEvent.ShowRemovalFailureDialog) }
        )
    }
  }

  private fun deleteAndGetReturnInfo(profile: ProfileEntity): Single<RemovalBackInfo> =
    deleteProfileUseCase(profile)
      .andThen(profileRepository.findAllProfiles())
      .map { RemovalBackInfo(profile.serverForCurrentAuthMethod, it.isEmpty()) }
      .firstOrError()

  private fun Int.toAccessIdentifierString(): String = if (this == 0) {
    ""
  } else {
    this.toString()
  }

  private data class RemovalBackInfo(
    val serverAddress: String?,
    val noAccountsRegistered: Boolean
  )
}
