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
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.db.AuthProfileItem
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.deleteaccountweb.DeleteAccountWebFragment
import org.supla.android.profile.ProfileManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.client.ReconnectUseCase
import org.supla.android.usecases.profile.DeleteProfileUseCase
import org.supla.android.usecases.profile.SaveProfileUseCase
import timber.log.Timber
import javax.inject.Inject

/**
A view model responsible for user credential input views. Handles both
initial authentication screen and profile editing.

@param profileManager profile manager to use for accessing account database
@param item account currently being edited
@param allowsBasicMode whether to allow basic mode (initial screen usually allows
while profile editing mode does not.
 */
@HiltViewModel
class CreateAccountViewModel @Inject constructor(
  private val profileManager: ProfileManager,
  private val saveProfileUseCase: SaveProfileUseCase,
  private val deleteProfileUseCase: DeleteProfileUseCase,
  private val profileRepository: RoomProfileRepository,
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
      profileManager.read(profileId)
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

  private fun onProfileLoaded(profile: AuthProfileItem) = profile.apply {
    updateState {
      it.copy(
        advancedMode = advancedAuthSetup,
        accountName = name,
        emailAddress = authInfo.emailAddress,
        authorizeByEmail = authInfo.emailAuth,
        autoServerAddress = authInfo.serverAutoDetect,
        emailAddressServer = authInfo.serverForEmail,
        accessIdentifier = authInfo.accessID.toAccessIdentifierString(),
        accessIdentifierPassword = authInfo.accessIDpwd,
        accessIdentifierServer = authInfo.serverForAccessID
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

  fun saveProfile(profileId: Long?, defaultName: String) {
    getSaveSingle(profileId, defaultName)
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

  private fun getSaveSingle(profileId: Long?, defaultName: String): Completable = profileId.let { id ->
    return@let if (id == null) {
      profileRepository.findAllProfiles()
        .firstOrError()
        .map { profiles ->
          val firstProfile = profiles.isEmpty()
          val item = currentState().toProfileItem()
          item.isActive = firstProfile
          if (firstProfile) {
            item.name = defaultName
          }
          item
        }
        .flatMapCompletable { profile ->
          saveProfileUseCase(profile).let { if (profile.isActive) it.andThen(reconnectUseCase()) else it }
        }
    } else {
      profileManager.read(id)
        .toSingle()
        .flatMapCompletable { profile ->
          val state = currentState()
          val authSettingsChanged = authSettingChanged(profile, state)
          state.updateProfile(profile)

          saveProfileUseCase(profile).let { if (authSettingsChanged) it.andThen(reconnectUseCase()) else it }
        }
    }
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

  private fun authSettingChanged(profile: AuthProfileItem, state: CreateAccountViewState): Boolean {
    return isAccessIdentifierEqual(profile, state).not() ||
      profile.authInfo.emailAddress != state.emailAddress ||
      profile.authInfo.serverForEmail != state.emailAddressServer ||
      profile.authInfo.serverForAccessID != state.accessIdentifierServer ||
      profile.authInfo.accessIDpwd != state.accessIdentifierPassword ||
      profile.authInfo.emailAuth != state.authorizeByEmail ||
      profile.authInfo.serverAutoDetect != state.autoServerAddress
  }

  private fun isAccessIdentifierEqual(profile: AuthProfileItem, state: CreateAccountViewState): Boolean {
    return if (state.accessIdentifier.isNotEmpty() && profile.authInfo.accessID != 0) {
      profile.authInfo.accessID == state.accessIdentifier.toInt()
    } else {
      profile.authInfo.accessID == 0 && (state.accessIdentifier.isEmpty() || state.accessIdentifier == "0")
    }
  }

  fun onDeleteProfile() {
    sendEvent(CreateAccountViewEvent.ConfirmDelete)
  }

  fun deleteProfile(profileId: Long?) {
    profileId?.let {
      profileManager.read(profileId)
        .toSingle()
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
    profileId?.let {
      profileManager.read(profileId)
        .toSingle()
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

  private fun deleteAndGetReturnInfo(profile: AuthProfileItem): Single<RemovalBackInfo> =
    deleteProfileUseCase(profile.id)
      .andThen(profileRepository.findAllProfiles())
      .map { RemovalBackInfo(profile.authInfo.serverAddress, it.isEmpty()) }
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
