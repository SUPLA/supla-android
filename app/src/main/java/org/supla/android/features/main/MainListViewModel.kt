package org.supla.android.features.main
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
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst
import kotlinx.coroutines.withContext
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.notifications.NotificationsHelper
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.core.ui.EventBasedViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.ui.dialogs.authorize.BaseAuthorizationViewModelScope
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.profile.ActivateProfileUseCase
import org.supla.android.usecases.profile.ReadAllProfilesUseCase
import javax.inject.Inject

@HiltViewModel
class MainListViewModel @Inject constructor(
  override val suplaClientProvider: SuplaClientProvider,
  override val profileRepository: ProfileRepository,
  override val authorizeUseCase: AuthorizeUseCase,
  override val loginUseCase: LoginUseCase,
  override val schedulers: SuplaSchedulers,
  private val activateProfileUseCase: ActivateProfileUseCase,
  private val applicationPreferences: ApplicationPreferences,
  private val readAllProfilesUseCase: ReadAllProfilesUseCase,
  private val encryptedPreferences: EncryptedPreferences,
  private val notificationsHelper: NotificationsHelper,
  private val channelRepository: ChannelRepository
) : EventBasedViewModel<MainListViewEvent>(), BaseAuthorizationViewModelScope {

  private val authorizationDialogStateFlow: MutableStateFlow<AuthorizationDialogState?> = MutableStateFlow(null)
  val authorizationDialogState: StateFlow<AuthorizationDialogState?> = authorizationDialogStateFlow

  private val zWaveAvailableFlow = MutableStateFlow(false)
  val zWaveAvailable: StateFlow<Boolean> = zWaveAvailableFlow

  private val developerOptionsVisibleFlow = MutableStateFlow(false)
  val developerOptionsVisible: StateFlow<Boolean> = developerOptionsVisibleFlow

  private val showNotificationInfoFlow = MutableStateFlow(false)
  val showNotificationInfo: StateFlow<Boolean> = showNotificationInfoFlow

  private val profileSelectionStateFlow = MutableStateFlow<ProfileSelectionDialogState?>(null)
  val profileSelectionState: StateFlow<ProfileSelectionDialogState?> = profileSelectionStateFlow

  override fun onViewCreated() {
    viewModelScope.launch {
      val zWaveAvailable = withContext(Dispatchers.IO) {
        runCatching { channelRepository.isZWaveBridgeChannelAvailable().await() }.getOrNull() ?: false
      }
      zWaveAvailableFlow.tryEmit(zWaveAvailable)
      developerOptionsVisibleFlow.tryEmit(encryptedPreferences.devModeActive)
    }

    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
      showNotificationInfoFlow.tryEmit(applicationPreferences.isNotificationsPopupDisplayed.not())
    }
  }

  override fun onStart() {
    viewModelScope.launch {
      developerOptionsVisibleFlow.tryEmit(encryptedPreferences.devModeActive)
    }
  }

  override fun updateAuthorizationDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?) {
    authorizationDialogStateFlow.tryEmit(
      value = updater(authorizationDialogStateFlow.value)
    )
  }

  override fun getAuthorizationDialogState(): AuthorizationDialogState? =
    authorizationDialogStateFlow.value

  override fun onAuthorized(reason: AuthorizationReason) {
    if (reason == AuthorizationReason.ZWaveWizard) {
      sendEvent(MainListViewEvent.OpenZWaveWizard)
    }
  }

  override fun launch(launcher: suspend CoroutineScope.() -> Unit) {
    viewModelScope.launch {
      launcher()
    }
  }

  fun onSkipNotification() {
    applicationPreferences.isNotificationsPopupDisplayed = true
    showNotificationInfoFlow.tryEmit(false)
  }

  fun hideNotificationsDialog() {
    showNotificationInfoFlow.tryEmit(false)
  }

  fun onNotificationPermissionGranted(context: Context) {
    applicationPreferences.isNotificationsPopupDisplayed = true

    notificationsHelper.setupNotificationChannel(context)
    notificationsHelper.setupBackgroundNotificationChannel(context)

    // Because for disabling we're sending an empty token, after right is granted we need to update token on server
    notificationsHelper.updateToken()
  }

  fun showProfilesPopup() {
    viewModelScope.launch {
      val profiles = schedulers.io {
        readAllProfilesUseCase().awaitFirst()
          .map { ProfileVo(it.id, it.name, it.active) }
      }

      profileSelectionStateFlow.tryEmit(
        ProfileSelectionDialogState(profiles)
      )
    }
  }

  fun onDismissProfileSelection() {
    profileSelectionStateFlow.tryEmit(null)
  }

  fun onProfileSelected(id: Long) {
    viewModelScope.launch {
      schedulers.io {
        activateProfileUseCase(id, force = false).await()
      }
      profileSelectionStateFlow.tryEmit(null)
    }
  }
}

sealed interface MainListViewEvent : ViewEvent {
  data object OpenZWaveWizard : MainListViewEvent
}
