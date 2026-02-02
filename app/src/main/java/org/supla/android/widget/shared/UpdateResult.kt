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

import org.supla.android.lib.SuplaConst.SUPLA_RESULT_HOST_NOT_FOUND
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.widget.WidgetConfiguration

sealed interface UpdateResult {
  data class Success(val configuration: WidgetConfiguration) : UpdateResult
  data class Error(val result: SingleCall.Result) : UpdateResult
  data object Empty : UpdateResult
}

fun UpdateResult.whenSuccess(block: (WidgetConfiguration) -> Unit) {
  if (this is UpdateResult.Success) {
    block(configuration)
  }
}

fun UpdateResult.whenFailure(block: (cleanConfiguration: Boolean, errorResult: UpdateResult.Error) -> Unit) {
  if (this is UpdateResult.Error) {
    val cleanConfiguration = when (result) {
      is SingleCall.Result.ConnectionError -> result.code != SUPLA_RESULT_HOST_NOT_FOUND
      else -> true
    }
    block(cleanConfiguration, this)
  }
}
