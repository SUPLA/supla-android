package org.supla.android.features.addwizard
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
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.supla.android.R
import org.supla.android.Trace
import org.supla.android.core.infrastructure.CurrentWifiNetworkInfoProvider
import org.supla.android.core.infrastructure.WiFiScanner
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.remote.esp.EspConfigurationSession
import org.supla.android.extensions.TAG
import org.supla.android.extensions.isNotNull
import org.supla.android.features.addwizard.configuration.AndroidEspConfigurationStateHolder
import org.supla.android.features.addwizard.model.AddWizardScreen
import org.supla.android.features.addwizard.model.Esp
import org.supla.android.features.addwizard.model.EspConfigResult
import org.supla.android.features.addwizard.usecase.AuthorizeEspUseCase
import org.supla.android.features.addwizard.usecase.CheckLocationEnabledUseCase
import org.supla.android.features.addwizard.usecase.ConfigureEspUseCase
import org.supla.android.features.addwizard.usecase.ConnectToSsidUseCase
import org.supla.android.features.addwizard.usecase.CreateEspPasswordUseCase
import org.supla.android.features.addwizard.usecase.FindEspSsidUseCase
import org.supla.android.features.addwizard.usecase.ReconnectToInternetUseCase
import org.supla.android.features.addwizard.usecase.receiver.ConnectResult
import org.supla.android.features.addwizard.view.AddWizardNetworkSelectionState
import org.supla.android.features.addwizard.view.AddWizardScope
import org.supla.android.features.addwizard.view.components.DeviceParameter
import org.supla.android.features.addwizard.view.dialogs.ProvidePasswordState
import org.supla.android.features.addwizard.view.dialogs.SetPasswordState
import org.supla.android.features.addwizard.view.dialogs.WiFiListDialogState
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.ui.dialogs.authorize.AuthorizationModelState
import org.supla.android.ui.dialogs.authorize.BaseAuthorizationViewModel
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.profile.LoadActiveProfileUseCase
import org.supla.core.shared.data.model.addwizard.EspConfigurationController
import org.supla.core.shared.data.model.addwizard.EspConfigurationError
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.Authorized
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.Back
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.Cancel
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.Canceled
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.Close
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.CredentialsNeeded
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.EspConfigurationFailure
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.EspConfigured
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.MultipleNetworksFound
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.NetworkFound
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.NetworkNotFound
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.NetworkScanDisabled
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.PasswordProvided
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.ReconnectTimeout
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.Reconnected
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.RegistrationActivated
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.RegistrationDisabled
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.RegistrationEnabled
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.RegistrationNotActivated
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.RegistrationUnknown
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.SetupNeeded
import org.supla.core.shared.data.model.addwizard.EspConfigurationEvent.Start
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.addwizard.CheckRegistrationEnabledUseCase
import org.supla.core.shared.usecase.addwizard.EnableRegistrationUseCase
import javax.inject.Inject

@HiltViewModel
class AddWizardViewModel @Inject constructor(
  @ApplicationContext private val context: Context,
  private val checkRegistrationEnabledUseCase: CheckRegistrationEnabledUseCase,
  private val currentWifiNetworkInfoProvider: CurrentWifiNetworkInfoProvider,
  private val checkLocationEnabledUseCase: CheckLocationEnabledUseCase,
  private val reconnectToInternetUseCase: ReconnectToInternetUseCase,
  private val enableRegistrationUseCase: EnableRegistrationUseCase,
  private val loadActiveProfileUseCase: LoadActiveProfileUseCase,
  private val createEspPasswordUseCase: CreateEspPasswordUseCase,
  private val espConfigurationSession: EspConfigurationSession,
  private val suplaClientStateHolder: SuplaClientStateHolder,
  private val connectToSsidUseCase: ConnectToSsidUseCase,
  private val encryptedPreferences: EncryptedPreferences,
  private val authorizeEspUseCase: AuthorizeEspUseCase,
  private val configureEspUseCase: ConfigureEspUseCase,
  private val findEspSsidUseCase: FindEspSsidUseCase,
  private val wiFiScanner: WiFiScanner,
  suplaClientProvider: SuplaClientProvider,
  profileRepository: RoomProfileRepository,
  authorizeUseCase: AuthorizeUseCase,
  loginUseCase: LoginUseCase,
  schedulers: SuplaSchedulers
) : BaseAuthorizationViewModel<AddWizardViewModelState, AddWizardViewEvent>(
  suplaClientProvider,
  profileRepository,
  loginUseCase,
  authorizeUseCase,
  AddWizardViewModelState(),
  schedulers
),
  AddWizardScope,
  EspConfigurationController {

  private var currentJob: Job? = null
  private lateinit var configurationStateHolder: AndroidEspConfigurationStateHolder

  override fun updateAuthorizationDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?) {
    updateState { it.copy(authorizationDialogState = updater(it.authorizationDialogState)) }
  }

  override fun onAuthorized(reason: AuthorizationReason) {
    closeAuthorizationDialog()
    configurationStateHolder.handleEvent(Authorized)
  }

  override fun onAuthorizationCancel() {
    super.onAuthorizationCancel()
    updateState { it.copy(processing = false) }
    configurationStateHolder = AndroidEspConfigurationStateHolder(this)
  }

  override fun onAuthorizationDismiss() {
    super.onAuthorizationDismiss()
    updateState { it.copy(processing = false) }
    configurationStateHolder = AndroidEspConfigurationStateHolder(this)
  }

  override fun onViewCreated() {
    configurationStateHolder = AndroidEspConfigurationStateHolder(this)
    if (hasWiFiConnection().not()) {
      updateState { it.openOnly(screen = AddWizardScreen.Message.NoWifi) }
    } else if (hasLocationEnabled().not()) {
      updateState { it.openOnly(screen = AddWizardScreen.Message.LocationDisabled) }
    } else {
      loadActiveProfileUseCase()
        .attach()
        .subscribeBy(
          onSuccess = { profile ->
            if (profile.emailAuth.not()) {
              updateState { it.openOnly(screen = AddWizardScreen.Message.WizardUnavailable) }
            } else {
              updateState { it.navigateTo(screen = AddWizardScreen.Welcome) }
              sendEvent(AddWizardViewEvent.CheckPermissions)
            }
          }
        )
        .disposeBySelf()
    }
  }

  override fun closeCloudDialog() {
    updateState { it.copy(showCloudFollowupPopup = false) }
  }

  override fun openCloud() {
    sendEvent(AddWizardViewEvent.OpenCloud)
  }

  override fun onBarCodeScan() {
    sendEvent(AddWizardViewEvent.OpenScanner)
  }

  override fun onNetworkNameChanged(name: String) {
    updateState { it.copy(networkSelectionState = it.networkSelectionState?.copy(networkName = name)) }
  }

  override fun onNetworkPasswordChanged(password: String) {
    updateState { it.copy(networkSelectionState = it.networkSelectionState?.copy(networkPassword = password)) }
  }

  override fun onNetworkPasswordVisibilityChanged() {
    updateState {
      it.copy(
        networkSelectionState = it.networkSelectionState?.copy(
          networkPasswordVisible = it.networkSelectionState.networkPasswordVisible.not()
        )
      )
    }
  }

  override fun onNetworkRememberPasswordChanged(remember: Boolean) {
    updateState { it.copy(networkSelectionState = it.networkSelectionState?.copy(rememberPassword = remember)) }
  }

  override fun onNetworkScanClicked() {
    updateState { it.copy(scannerDialogState = WiFiListDialogState()) }
    viewModelScope.launch {
      val ssids = withContext(Dispatchers.IO) { wiFiScanner.scan() }
      when (ssids) {
        is WiFiScanner.Result.NotAllowed ->
          updateState { state ->
            state.copy(
              scannerDialogState = state.scannerDialogState?.copy(
                selected = currentWifiNetworkInfoProvider.provide()?.ssid,
                items = ssids.cashed.filter { !Esp.isKnownNetworkName(it) },
                warning = true
              )
            )
          }

        is WiFiScanner.Result.Success ->
          updateState { state ->
            state.copy(
              scannerDialogState = state.scannerDialogState?.copy(
                selected = currentWifiNetworkInfoProvider.provide()?.ssid,
                items = ssids.ssids.filter { !Esp.isKnownNetworkName(it) },
                warning = false
              )
            )
          }
      }
    }
  }

  override fun onStepFinished(step: AddWizardScreen) {
    when (step) {
      AddWizardScreen.Welcome -> welcomeNextStep()
      is AddWizardScreen.NetworkSelection -> networkSelectionNextStep()
      AddWizardScreen.Configuration -> {
        if (!hasWiFiConnection()) {
          updateState { it.navigateTo(screen = AddWizardScreen.Message.NoWifi) }
        } else if (!hasLocationEnabled()) {
          updateState { it.navigateTo(screen = AddWizardScreen.Message.LocationDisabled) }
        } else {
          configurationStateHolder.handleEvent(Start)
          updateState { it.copy(processing = true) }
        }
      }

      is AddWizardScreen.Message, is AddWizardScreen.Success -> sendEvent(AddWizardViewEvent.Close)
    }
  }

  override fun onClose(step: AddWizardScreen) {
    if (step !is AddWizardScreen.Configuration || configurationStateHolder.isInactive) {
      sendEvent(AddWizardViewEvent.Close)
    } else {
      configurationStateHolder.handleEvent(Close)
    }
  }

  override fun onWiFiListDismiss() {
    if (currentState().screen is AddWizardScreen.NetworkSelection) {
      updateState { it.copy(scannerDialogState = null) }
    }
  }

  override fun onWiFiListCancel() {
    if (currentState().screen is AddWizardScreen.Configuration) {
      configurationStateHolder.handleEvent(NetworkNotFound)
    }
    updateState { it.copy(scannerDialogState = null) }
  }

  override fun onWiFiListSelect() {
    val state = currentState()
    if (state.screen is AddWizardScreen.Configuration) {
      if (state.scannerDialogState?.selected != null) {
        configurationStateHolder.handleEvent(NetworkFound(state.scannerDialogState.selected))
      } else {
        configurationStateHolder.handleEvent(NetworkNotFound)
      }
    }

    updateState {
      if (it.screen is AddWizardScreen.NetworkSelection) {
        it.copy(
          networkSelectionState = it.networkSelectionState?.copy(
            networkName = it.scannerDialogState?.selected ?: it.networkSelectionState.networkName
          ),
          scannerDialogState = null
        )
      } else {
        it.copy(scannerDialogState = null)
      }
    }
  }

  override fun onSsidSelected(ssid: String) {
    updateState { it.copy(scannerDialogState = it.scannerDialogState?.copy(selected = ssid)) }
  }

  override fun onForceNextClick() {
    configureEsp()
    updateState { it.copy(scannerDialogState = null) }
  }

  fun showMissingPermissionError(appName: String) {
    updateState {
      it.openOnly(
        screen = AddWizardScreen.Message(
          message = localizedString(R.string.wizard_not_enought_permissions, appName),
          showRepeat = false
        )
      )
    }
  }

  fun registerSsidObserver() {
    currentWifiNetworkInfoProvider.register()
  }

  fun onBackPressed() {
    val state = currentState()
    if (state.screens.size == 1) {
      return
    }

    if (configurationStateHolder.isInactive) {
      updateState { it.back() }
    } else {
      configurationStateHolder.handleEvent(event = Back)
    }
  }

  override fun updateProgress(progress: Float, descriptionLabel: LocalizedString?) {
    updateState { it.copy(processingProgress = progress, processingProgressLabel = descriptionLabel) }
  }

  override fun checkRegistration() {
    currentJob = viewModelScope.launch {
      var result = withContext(Dispatchers.IO) { checkRegistrationEnabledUseCase() }
      if (result == CheckRegistrationEnabledUseCase.Result.TIMEOUT) {
        Trace.i(TAG, "First timeout reached, awaiting second time")
        result = withContext(Dispatchers.IO) { checkRegistrationEnabledUseCase() }
      }

      when (result) {
        CheckRegistrationEnabledUseCase.Result.ENABLED -> configurationStateHolder.handleEvent(RegistrationEnabled)
        CheckRegistrationEnabledUseCase.Result.DISABLED -> configurationStateHolder.handleEvent(RegistrationDisabled)
        CheckRegistrationEnabledUseCase.Result.TIMEOUT -> configurationStateHolder.handleEvent(RegistrationUnknown)
      }
    }
  }

  override fun authorize() {
    showAuthorizationDialog(
      clarificationMessage = localizedString(R.string.add_wizard_password_clarification)
    )
  }

  override fun activateRegistration() {
    currentJob = viewModelScope.launch {
      val result = withContext(Dispatchers.IO) { enableRegistrationUseCase() }
      when (result) {
        EnableRegistrationUseCase.Result.SUCCESS -> configurationStateHolder.handleEvent(RegistrationActivated)
        else -> configurationStateHolder.handleEvent(RegistrationNotActivated)
      }
    }
  }

  override fun findEspNetwork() {
    currentJob = viewModelScope.launch {
      val result = withContext(Dispatchers.IO) { findEspSsidUseCase() }
      when (result) {
        is FindEspSsidUseCase.Result.Cached -> configurationStateHolder.handleEvent(NetworkScanDisabled(result.ssids))
        FindEspSsidUseCase.Result.Empty -> configurationStateHolder.handleEvent(NetworkNotFound)
        is FindEspSsidUseCase.Result.Multiple -> configurationStateHolder.handleEvent(MultipleNetworksFound(result.ssids))
        is FindEspSsidUseCase.Result.Single -> configurationStateHolder.handleEvent(NetworkFound(result.ssid))
      }
    }
  }

  override fun showNetworkSelector(ssids: List<String>, cached: Boolean) {
    updateState {
      it.copy(scannerDialogState = WiFiListDialogState(items = ssids, warning = cached, skipAndConnect = cached))
    }
  }

  override fun connectToNetwork(ssid: String) {
    currentJob = viewModelScope.launch {
      val result = withContext(Dispatchers.IO) { connectToSsidUseCase.connect(ssid) }
      when (result) {
        ConnectResult.SUCCESS -> configurationStateHolder.handleEvent(EspConfigurationEvent.NetworkConnected)
        ConnectResult.FAILURE,
        ConnectResult.TIMEOUT -> configurationStateHolder.handleEvent(EspConfigurationEvent.NetworkConnectionFailure)
      }
    }
  }

  override fun configureEsp() {
    val state = currentState()
    val ssid = state.networkSelectionState?.networkName
    val password = state.networkSelectionState?.networkPassword
    if (ssid == null || password == null) {
      configurationStateHolder.handleEvent(EspConfigurationFailure(EspConfigurationError.Wifi))
      return
    }

    currentJob = viewModelScope.launch {
      val result = withContext(Dispatchers.IO) { configureEspUseCase(ConfigureEspUseCase.InputData(ssid, password)) }
      when (result) {
        ConfigureEspUseCase.Result.ConnectionError ->
          configurationStateHolder.handleEvent(EspConfigurationFailure(EspConfigurationError.Communication))

        ConfigureEspUseCase.Result.Failed ->
          configurationStateHolder.handleEvent(EspConfigurationFailure(EspConfigurationError.Configuration))

        ConfigureEspUseCase.Result.Incompatible ->
          configurationStateHolder.handleEvent(EspConfigurationFailure(EspConfigurationError.Compatibility))

        ConfigureEspUseCase.Result.Timeout ->
          configurationStateHolder.handleEvent(EspConfigurationFailure(EspConfigurationError.ConfigureTimeout))

        ConfigureEspUseCase.Result.CredentialsNeeded ->
          configurationStateHolder.handleEvent(CredentialsNeeded)

        ConfigureEspUseCase.Result.SetupNeeded ->
          configurationStateHolder.handleEvent(SetupNeeded)

        ConfigureEspUseCase.Result.TemporarilyLocked ->
          configurationStateHolder.handleEvent(EspConfigurationFailure(EspConfigurationError.TemporarilyLocked))

        is ConfigureEspUseCase.Result.Success -> {
          updateState { it.copy(espConfigResult = result.result) }
          configurationStateHolder.handleEvent(EspConfigured)
        }
      }
    }
  }

  override fun configurePassword() {
    updateState {
      it.copy(
        setPasswordState = SetPasswordState(
          ssid = currentWifiNetworkInfoProvider.provide()?.ssid
        )
      )
    }
  }

  override fun providePassword() {
    updateState {
      it.copy(
        providePasswordState = ProvidePasswordState(
          ssid = currentWifiNetworkInfoProvider.provide()?.ssid
        )
      )
    }
  }

  override fun reconnect() {
    suplaClientStateHolder.handleEvent(SuplaClientEvent.AddWizardFinished)
    currentJob = viewModelScope.launch {
      val result = withContext(Dispatchers.IO) { reconnectToInternetUseCase(currentState().networkId) }

      when (result) {
        ConnectResult.SUCCESS -> configurationStateHolder.handleEvent(Reconnected)
        ConnectResult.FAILURE,
        ConnectResult.TIMEOUT -> configurationStateHolder.handleEvent(ReconnectTimeout)
      }
    }
  }

  override fun showFinished() {
    updateState {
      it.navigateTo(AddWizardScreen.Success)
        .copy(
          showCloudFollowupPopup = it.espConfigResult?.needsCloudConfig == true,
          processing = false
        )
    }
  }

  override fun showError(error: EspConfigurationError) {
    updateState {
      it.navigateTo(screen = AddWizardScreen.Message(messages = error.messages, true))
        .copy(processing = false)
    }
  }

  override fun cancel() {
    updateState { it.copy(canceling = true) }
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        if (currentJob?.isActive == true) {
          currentJob?.cancelAndJoin()
        }
      }
      configurationStateHolder.handleEvent(Canceled)
    }
  }

  override fun close() {
    sendEvent(AddWizardViewEvent.Close)
  }

  override fun back() {
    updateState { it.back().copy(processing = false, canceling = false) }
  }

  override fun reinitialize() {
    updateState { it.copy(processing = false, canceling = false) }
  }

  override fun setupEspConfiguration() {
    espConfigurationSession.reset()
  }

  private fun hasWiFiConnection(): Boolean {
    val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
  }

  private fun hasLocationEnabled(): Boolean = checkLocationEnabledUseCase()

  private fun welcomeNextStep() {
    if (!hasLocationEnabled()) {
      updateState { it.navigateTo(screen = AddWizardScreen.Message.LocationDisabled) }
    } else {
      val networkName = encryptedPreferences.wizardWifiName
      val networkPassword = encryptedPreferences.wizardWifiPassword
      val currentNetwork = currentWifiNetworkInfoProvider.provide()

      updateState {
        it.navigateTo(screen = AddWizardScreen.NetworkSelection)
          .copy(
            networkSelectionState = it.networkSelectionState ?: AddWizardNetworkSelectionState(
              networkName = networkName ?: currentNetwork?.ssid ?: "",
              networkPassword = networkName.isNotNull.ifTrue { encryptedPreferences.wizardWifiPassword } ?: "",
              rememberPassword = networkName.isNotNull && networkPassword.isNotNull,
              error = false,
            ),
            networkId = currentNetwork?.networkId
          )
      }
    }
  }

  private fun networkSelectionNextStep() {
    updateState { state ->
      if (state.networkSelectionState?.networkName.isNullOrEmpty() || state.networkSelectionState.networkPassword.isEmpty()) {
        state.copy(networkSelectionState = state.networkSelectionState?.copy(error = true))
      } else {
        val ssid = state.networkSelectionState.networkName
        if (ssid.isNotEmpty()) {
          encryptedPreferences.wizardWifiName = ssid
        }
        val password = state.networkSelectionState.networkPassword
        if (state.networkSelectionState.rememberPassword && password.isNotEmpty()) {
          encryptedPreferences.wizardWifiPassword = state.networkSelectionState.networkPassword
        } else if (!state.networkSelectionState.rememberPassword) {
          encryptedPreferences.wizardWifiPassword = null
        }
        state.navigateTo(screen = AddWizardScreen.Configuration)
      }
    }
  }

  override fun onAgain() {
    onBackPressed()
  }

  override fun closeProvidePasswordDialog() {
    updateState { it.copy(providePasswordState = null) }
    configurationStateHolder.handleEvent(Cancel)
  }

  override fun onPasswordProvided(password: String) {
    updateState { it.copy(providePasswordState = it.providePasswordState?.copy(error = null, processing = true)) }

    currentJob = viewModelScope.launch {
      val result = withContext(Dispatchers.IO) { authorizeEspUseCase(password) }

      when (result) {
        AuthorizeEspUseCase.Result.SUCCESS -> {
          updateState { it.copy(providePasswordState = null) }
          configurationStateHolder.handleEvent(PasswordProvided)
        }

        AuthorizeEspUseCase.Result.FAILURE_WRONG_PASSWORD ->
          updateState {
            it.copy(
              providePasswordState = it.providePasswordState?.copy(
                error = ProvidePasswordState.Error.INVALID_CREDENTIALS,
                processing = false
              )
            )
          }

        AuthorizeEspUseCase.Result.FAILURE_UNKNOWN ->
          updateState {
            it.copy(
              providePasswordState = it.providePasswordState?.copy(
                error = ProvidePasswordState.Error.UNKNOWN_ERROR,
                processing = false
              )
            )
          }

        AuthorizeEspUseCase.Result.TEMPORARILY_LOCKED -> {
          updateState { it.copy(providePasswordState = null) }
          configurationStateHolder.handleEvent(EspConfigurationFailure(EspConfigurationError.TemporarilyLocked))
        }
      }
    }
  }

  override fun closeSetPasswordDialog() {
    updateState { it.copy(setPasswordState = null) }
    configurationStateHolder.handleEvent(Cancel)
  }

  override fun onPasswordSet(password: String, repeatPassword: String) {
    if (password.isAcceptablePassword() && password == repeatPassword) {
      updateState { it.copy(setPasswordState = it.setPasswordState?.copy(processing = true)) }
      currentJob = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) { createEspPasswordUseCase(password) }

        when (result) {
          CreateEspPasswordUseCase.Result.SUCCESS -> {
            updateState { it.copy(setPasswordState = null) }
            configurationStateHolder.handleEvent(PasswordProvided)
          }

          CreateEspPasswordUseCase.Result.FAILURE ->
            updateState { it.copy(setPasswordState = it.setPasswordState?.copy(error = true, processing = false)) }

          CreateEspPasswordUseCase.Result.TEMPORARILY_LOCKED -> {
            updateState { it.copy(setPasswordState = null) }
            configurationStateHolder.handleEvent(EspConfigurationFailure(EspConfigurationError.TemporarilyLocked))
          }
        }
      }
    } else {
      updateState { it.copy(setPasswordState = it.setPasswordState?.copy(error = true)) }
    }
  }
}

sealed interface AddWizardViewEvent : ViewEvent {
  data object Close : AddWizardViewEvent
  data object OpenScanner : AddWizardViewEvent
  data object CheckPermissions : AddWizardViewEvent
  data object OpenCloud : AddWizardViewEvent
}

data class AddWizardViewModelState(
  val screens: List<AddWizardScreen> = emptyList(),
  val processing: Boolean = false,
  val processingProgress: Float = 0f,
  val processingProgressLabel: LocalizedString? = null,
  val networkSelectionState: AddWizardNetworkSelectionState? = null,
  val scannerDialogState: WiFiListDialogState? = null,
  val espConfigResult: EspConfigResult? = null,
  val showCloudFollowupPopup: Boolean = false,
  val canceling: Boolean = false,
  val networkId: Int? = null,
  val setPasswordState: SetPasswordState? = null,
  val providePasswordState: ProvidePasswordState? = null,
  override val authorizationDialogState: AuthorizationDialogState? = null
) : AuthorizationModelState() {

  val customBackEnabled: Boolean
    get() = screens.size > 1

  val screen: AddWizardScreen?
    get() = screens.lastOrNull()

  val parameters: List<DeviceParameter>
    get() = mutableListOf<DeviceParameter>().apply {
      espConfigResult?.deviceName?.let { add(DeviceParameter(R.string.wizard_iodev_name, it)) }
      espConfigResult?.deviceFirmwareVersion?.let { add(DeviceParameter(R.string.wizard_iodev_firmware, it)) }
      espConfigResult?.deviceMAC?.let { add(DeviceParameter(R.string.wizard_iodev_mac, it)) }
      espConfigResult?.deviceLastState?.let { add(DeviceParameter(R.string.wizard_iodev_laststate, it)) }
    }

  fun navigateTo(screen: AddWizardScreen): AddWizardViewModelState =
    copy(
      screens = mutableListOf<AddWizardScreen>().apply {
        addAll(screens)
        add(screen)
      }
    )

  fun openOnly(screen: AddWizardScreen): AddWizardViewModelState =
    copy(screens = listOf(screen))

  fun back(): AddWizardViewModelState =
    copy(
      screens = mutableListOf<AddWizardScreen>().apply {
        addAll(screens.subList(0, screens.size - 1))
      }
    )
}

private fun String.isAcceptablePassword(): Boolean {
  val hasLowercase = matches(Regex(".*[a-z].*"))
  val hasUppercase = matches(Regex(".*[A-Z].*"))
  val hasDigit = matches(Regex(".*[0-9].*"))

  return hasLowercase && hasUppercase && hasDigit
}
