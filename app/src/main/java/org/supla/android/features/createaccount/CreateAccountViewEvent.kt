package org.supla.android.features.createaccount

import org.supla.android.core.ui.ViewEvent
import org.supla.android.features.deleteaccountweb.DeleteAccountWebFragment

sealed class CreateAccountViewEvent : ViewEvent {
  object Close : CreateAccountViewEvent()
  object Reconnect : CreateAccountViewEvent()
  object RestartFlow : CreateAccountViewEvent()
  object ConfirmDelete : CreateAccountViewEvent()
  object NavigateToCreateAccount : CreateAccountViewEvent()
  object ShowBasicModeUnavailableDialog : CreateAccountViewEvent()
  object ShowEmptyNameDialog : CreateAccountViewEvent()
  object ShowDuplicatedNameDialog : CreateAccountViewEvent()
  object ShowRequiredDataMissingDialog : CreateAccountViewEvent()
  object ShowUnknownErrorDialog : CreateAccountViewEvent()
  object ShowRemovalFailureDialog : CreateAccountViewEvent()
  data class NavigateToWebRemoval(
    val serverAddress: String?,
    val destination: DeleteAccountWebFragment.EndDestination
  ) : CreateAccountViewEvent()
}