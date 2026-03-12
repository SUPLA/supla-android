package org.supla.android.features.nfc.add
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import org.supla.android.R
import org.supla.android.core.ui.ViewState
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.dialogs.DialogWithIcon
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.forms.InfoMessage
import org.supla.android.ui.views.icons.Warning
import org.supla.android.ui.views.texts.TitleMedium
import org.supla.android.usecases.nfc.TagOperationError
import org.supla.android.usecases.nfc.message

data class AddNfcTagViewState(
  val dialog: AddNfcTagDialog? = null
) : ViewState()

sealed interface AddNfcTagDialog {
  data class Failure(val error: TagOperationError) : AddNfcTagDialog
  data class Duplicate(val id: Long, val name: String) : AddNfcTagDialog
}

interface AddNfcTagScope {
  fun onTryAgain()
  fun onOpenTag(tagId: Long)
  fun onClose()
}

@Composable
fun AddNfcTagScope.View(viewState: AddNfcTagViewState) =
  Box(
    modifier = Modifier
      .padding(Distance.default)
      .fillMaxSize(),
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Distance.default)
    ) {
      Image(
        drawableId = R.drawable.image_scan_nfc,
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1f)
      )
      TitleMedium(
        stringRes = R.string.add_nfc_scanning_hint,
        textAlign = TextAlign.Center
      )
      InfoMessage(stringResource(R.string.add_nfc_override_warning))
    }

    viewState.dialog?.let {
      when (it) {
        is AddNfcTagDialog.Failure -> ErrorDialog(it.error)
        is AddNfcTagDialog.Duplicate -> DuplicateDialog(it.id, it.name)
      }
    }
  }

@Composable
private fun AddNfcTagScope.ErrorDialog(error: TagOperationError) =
  DialogWithIcon(
    title = stringResource(R.string.nfc_lock_tag_error_title),
    message = error.message(),
    iconType = Warning,
    primaryButtonTitle = stringResource(R.string.status_try_again),
    secondaryButtonTitle = stringResource(R.string.exit),
    onPrimaryClick = { onTryAgain() },
    onSecondaryClick = { onClose() }
  )

@Composable
private fun AddNfcTagScope.DuplicateDialog(tagId: Long, name: String) =
  DialogWithIcon(
    title = stringResource(R.string.nfc_duplicate_dialog_title),
    message = stringResource(R.string.nfc_duplicate_dialog_message, name),
    iconType = Warning,
    primaryButtonTitle = stringResource(R.string.nfc_duplicate_open_tag),
    secondaryButtonTitle = stringResource(R.string.exit),
    onPrimaryClick = { onOpenTag(tagId) },
    onSecondaryClick = { onClose() }
  )

private val previewScope = object : AddNfcTagScope {
  override fun onTryAgain() {}
  override fun onOpenTag(tagId: Long) {}
  override fun onClose() {}
}

@SuplaPreview
@Composable
private fun PreviewTagConfiguration() {
  SuplaTheme {
    previewScope.View(AddNfcTagViewState())
  }
}

@SuplaPreview
@Composable
private fun PreviewDuplicate() {
  SuplaTheme {
    previewScope.View(
      viewState = AddNfcTagViewState(dialog = AddNfcTagDialog.Duplicate(10, "Livingroom light"))
    )
  }
}

@SuplaPreview
@Composable
private fun PreviewErrorNotUsable() {
  SuplaTheme {
    previewScope.View(
      viewState = AddNfcTagViewState(dialog = AddNfcTagDialog.Failure(error = TagOperationError.UNSUPPORTED))
    )
  }
}

@SuplaPreview
@Composable
private fun PreviewAnotherError() {
  SuplaTheme {
    previewScope.View(
      viewState = AddNfcTagViewState(dialog = AddNfcTagDialog.Failure(error = TagOperationError.WRITE_FAILED))
    )
  }
}
