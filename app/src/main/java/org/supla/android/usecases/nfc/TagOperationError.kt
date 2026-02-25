package org.supla.android.usecases.nfc
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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.supla.android.R

enum class TagOperationError {
  UNSUPPORTED,
  WRITE_PROTECTED,
  NOT_ENOUGH_MEMORY,
  WRONG,
  PROTECTION_FAILED,
  WRITE_FAILED
}

@Composable
fun TagOperationError.message(tagName: String? = null): String =
  when (this) {
    TagOperationError.UNSUPPORTED -> stringResource(R.string.nfc_tag_error_unsupported)
    TagOperationError.WRITE_PROTECTED -> stringResource(R.string.nfc_tag_error_write_protected)
    TagOperationError.NOT_ENOUGH_MEMORY -> stringResource(R.string.nfc_tag_error_not_enough_memory)
    TagOperationError.WRONG -> stringResource(R.string.nfc_tag_error_wrong, tagName ?: "")
    TagOperationError.PROTECTION_FAILED -> stringResource(R.string.nfc_tag_error_protection_failed)
    TagOperationError.WRITE_FAILED -> stringResource(R.string.nfc_tag_error_write_failed)
  }
