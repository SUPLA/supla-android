package org.supla.android.features.createaccount

import org.supla.android.core.ui.ViewEvent

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
}