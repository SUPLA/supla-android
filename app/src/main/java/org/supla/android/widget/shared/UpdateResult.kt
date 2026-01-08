package org.supla.android.widget.shared
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

import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_ACCESSID_DISABLED
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_ACCESSID_INACTIVE
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_BAD_CREDENTIALS
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_CHANNEL_IS_OFFLINE
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_CLIENT_DISABLED
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_CLIENT_NOT_EXISTS
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_SUBJECT_NOT_FOUND
import org.supla.android.lib.SuplaConst.SUPLA_RESULT_CANT_CONNECT_TO_HOST
import org.supla.android.lib.SuplaConst.SUPLA_RESULT_HOST_NOT_FOUND
import org.supla.android.lib.SuplaConst.SUPLA_RESULT_RESPONSE_TIMEOUT
import org.supla.android.lib.singlecall.ResultException
import org.supla.android.widget.WidgetConfiguration

sealed interface UpdateResult {
  data class Success(val configuration: WidgetConfiguration) : UpdateResult
  data class CommandError(val code: Int) : UpdateResult
  data class ConnectionError(val code: Int) : UpdateResult
  data class AccessError(val code: Int) : UpdateResult

  data object Empty : UpdateResult
  data object Offline : UpdateResult
  data object NotFound : UpdateResult
  data object UnknownError : UpdateResult
}

fun UpdateResult.whenSuccess(block: (WidgetConfiguration) -> Unit) {
  if (this is UpdateResult.Success) {
    block(configuration)
  }
}

fun UpdateResult.whenFailure(block: () -> Unit) {
  if (this !is UpdateResult.Success) {
    block()
  }
}

val ResultException.toUpdateResult: UpdateResult
  get() = when (result) {
    SUPLA_RESULT_HOST_NOT_FOUND,
    SUPLA_RESULT_CANT_CONNECT_TO_HOST,
    SUPLA_RESULT_RESPONSE_TIMEOUT -> UpdateResult.ConnectionError(result)

    SUPLA_RESULTCODE_CLIENT_NOT_EXISTS,
    SUPLA_RESULTCODE_BAD_CREDENTIALS,
    SUPLA_RESULTCODE_CLIENT_DISABLED,
    SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED,
    SUPLA_RESULTCODE_ACCESSID_DISABLED,
    SUPLA_RESULTCODE_ACCESSID_INACTIVE -> UpdateResult.AccessError(result)

    SUPLA_RESULTCODE_CHANNEL_IS_OFFLINE -> UpdateResult.Offline
    SUPLA_RESULTCODE_SUBJECT_NOT_FOUND -> UpdateResult.NotFound
    else -> UpdateResult.CommandError(result)
  }
