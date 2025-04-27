package org.supla.android.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.lib.actions.ActionId
import org.supla.android.usecases.channel.ActionException

data class ActionAlertDialogState(
  val messageRes: Int? = null,
  val messageString: String? = null,
  val positiveButtonRes: Int? = null,
  val negativeButtonRes: Int? = null,
  val actionId: ActionId? = null,
  val remoteId: Int? = null
) {
  @Composable
  fun View(
    onDismiss: () -> Unit = {},
    onPositiveClick: (remoteId: Int?, actionId: ActionId?) -> Unit = { _, _ -> },
    onNegativeClick: () -> Unit = {}
  ) {
    AlertDialog(
      title = stringResource(android.R.string.dialog_alert_title),
      message = messageString ?: messageRes?.let { stringResource(it) } ?: "",
      positiveButtonTitle = positiveButtonRes?.let { stringResource(it) },
      negativeButtonTitle = negativeButtonRes?.let { stringResource(it) },
      onDismiss = onDismiss,
      onPositiveClick = { onPositiveClick(remoteId, actionId) },
      onNegativeClick = onNegativeClick
    )
  }
}

val ActionException.ValveClosedManually.dialogState: ActionAlertDialogState
  get() = ActionAlertDialogState(
    messageRes = R.string.valve_warning_manually_closed,
    positiveButtonRes = R.string.yes,
    negativeButtonRes = R.string.no,
    actionId = ActionId.OPEN,
    remoteId = remoteId
  )

val ActionException.ValveFloodingAlarm.dialogState: ActionAlertDialogState
  get() = ActionAlertDialogState(
    messageRes = R.string.valve_warning_flooding,
    positiveButtonRes = R.string.yes,
    negativeButtonRes = R.string.no,
    actionId = ActionId.OPEN,
    remoteId = remoteId
  )

val ActionException.ValveMotorProblemClosing.dialogState: ActionAlertDialogState
  get() = ActionAlertDialogState(
    messageRes = R.string.valve_warning_motor_problem_closing,
    positiveButtonRes = R.string.yes,
    negativeButtonRes = R.string.no,
    actionId = ActionId.CLOSE,
    remoteId = remoteId
  )

val ActionException.ValveMotorProblemOpening.dialogState: ActionAlertDialogState
  get() = ActionAlertDialogState(
    messageRes = R.string.valve_warning_motor_problem_opening,
    positiveButtonRes = R.string.yes,
    negativeButtonRes = R.string.no,
    actionId = ActionId.OPEN,
    remoteId = remoteId
  )

val ActionException.ChannelExceedAmperage.dialogState: ActionAlertDialogState
  get() = ActionAlertDialogState(
    messageRes = R.string.overcurrent_question,
    positiveButtonRes = R.string.yes,
    negativeButtonRes = R.string.no,
    actionId = ActionId.TURN_ON,
    remoteId = remoteId
  )
