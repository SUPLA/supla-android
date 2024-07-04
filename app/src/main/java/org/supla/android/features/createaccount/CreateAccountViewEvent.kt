package org.supla.android.features.createaccount

import org.supla.android.core.ui.ViewEvent
import org.supla.android.features.deleteaccountweb.DeleteAccountWebFragment

sealed class CreateAccountViewEvent : ViewEvent {
  data object Close : CreateAccountViewEvent()
  data object RestartFlow : CreateAccountViewEvent()
  data object ConfirmDelete : CreateAccountViewEvent()
  data object NavigateToCreateAccount : CreateAccountViewEvent()
  data object ShowBasicModeUnavailableDialog : CreateAccountViewEvent()
  data object ShowEmptyNameDialog : CreateAccountViewEvent()
  data object ShowDuplicatedNameDialog : CreateAccountViewEvent()
  data object ShowRequiredDataMissingDialog : CreateAccountViewEvent()
  data object ShowUnknownErrorDialog : CreateAccountViewEvent()
  data object ShowRemovalFailureDialog : CreateAccountViewEvent()
  data class NavigateToWebRemoval(
    val serverAddress: String?,
    val destination: DeleteAccountWebFragment.EndDestination
  ) : CreateAccountViewEvent()
}
