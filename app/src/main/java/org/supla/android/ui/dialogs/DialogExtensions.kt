package org.supla.android.ui.dialogs
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

import android.app.AlertDialog
import android.content.DialogInterface
import org.supla.android.R
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.ui.BaseFragment
import org.supla.android.lib.actions.ActionId

fun BaseFragment<*, *>.valveClosedManuallyDialog(channelId: Int, suplaClient: SuplaClientApi?): AlertDialog {
  val builder = AlertDialog.Builder(context)
  builder.setTitle(android.R.string.dialog_alert_title)
  builder.setMessage(R.string.valve_warning_manually_closed)
  builder.setPositiveButton(R.string.yes) { dialog: DialogInterface, _ ->
    vibrationHelper.vibrate()
    suplaClient?.open(channelId, false, 1)
    dialog.cancel()
  }
  builder.setNeutralButton(R.string.no) { dialog: DialogInterface, _ -> dialog.cancel() }
  return builder.create()
}

fun BaseFragment<*, *>.valveFloodingDialog(channelId: Int, suplaClient: SuplaClientApi?): AlertDialog {
  val builder = AlertDialog.Builder(context)
  builder.setTitle(android.R.string.dialog_alert_title)
  builder.setMessage(R.string.valve_warning_flooding)
  builder.setPositiveButton(R.string.yes) { dialog: DialogInterface, _ ->
    vibrationHelper.vibrate()
    suplaClient?.open(channelId, false, 1)
    dialog.cancel()
  }
  builder.setNeutralButton(R.string.no) { dialog: DialogInterface, _ -> dialog.cancel() }
  return builder.create()
}

fun BaseFragment<*, *>.valveMotorProblemDialog(channelId: Int, actionId: ActionId, suplaClient: SuplaClientApi?): AlertDialog {
  val builder = AlertDialog.Builder(context)
  builder.setTitle(android.R.string.dialog_alert_title)
  if (actionId == ActionId.OPEN) {
    builder.setMessage(R.string.valve_warning_motor_problem_opening)
  } else {
    builder.setMessage(R.string.valve_warning_motor_problem_closing)
  }
  builder.setPositiveButton(R.string.yes) { dialog: DialogInterface, _ ->
    vibrationHelper.vibrate()
    if (actionId == ActionId.OPEN) {
      suplaClient?.open(channelId, false, 1)
    } else {
      suplaClient?.open(channelId, false, 0)
    }
    dialog.cancel()
  }
  builder.setNeutralButton(R.string.no) { dialog: DialogInterface, _ -> dialog.cancel() }
  return builder.create()
}

fun BaseFragment<*, *>.exceededAmperageDialog(channelId: Int, suplaClient: SuplaClientApi?): AlertDialog {
  val builder = AlertDialog.Builder(context)
  builder.setTitle(android.R.string.dialog_alert_title)
  builder.setMessage(R.string.overcurrent_question)
  builder.setPositiveButton(R.string.yes) { dialog, _ ->
    dialog.dismiss()
    vibrationHelper.vibrate()
    suplaClient?.open(channelId, false, 1)
  }
  builder.setNeutralButton(R.string.no) { dialog, _ -> dialog.cancel() }
  return builder.create()
}
