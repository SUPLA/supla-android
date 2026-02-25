package org.supla.android.features.nfc.lock
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.core.ui.ViewState
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.dialogs.DialogWithIcon
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.forms.InfoMessage
import org.supla.android.ui.views.icons.Warning
import org.supla.android.ui.views.texts.BodyLarge
import org.supla.android.ui.views.texts.TitleLarge
import org.supla.android.ui.views.texts.TitleMedium
import org.supla.android.usecases.nfc.TagOperationError
import org.supla.android.usecases.nfc.message

data class LockTagViewState(
  var tagName: String = "",
  val state: State = State.AWAITING_TAG,
  val error: TagOperationError? = null
) : ViewState() {
  enum class State {
    AWAITING_TAG, SUCCESS
  }
}

interface LockTagViewScope {
  fun onCloseClick()
  fun onRetryClick()
}

@Composable
fun LockTagViewScope.View(viewState: LockTagViewState) {
  Box(modifier = Modifier.fillMaxSize()) {
    when (viewState.state) {
      LockTagViewState.State.AWAITING_TAG -> {
        AwaitingTagScreen(viewState.tagName)
        viewState.error?.let { ErrorDialog(it, viewState.tagName) }
      }
      LockTagViewState.State.SUCCESS -> SuccessScreen()
    }
  }
}

@Composable
private fun AwaitingTagScreen(tagName: String) =
  Column(
    modifier = Modifier
      .verticalScroll(rememberScrollState())
      .padding(Distance.default),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Distance.default)
  ) {
    Image(
      drawableId = R.drawable.image_scan_nfc,
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
    )

    TitleMedium(text = stringResource(R.string.nfc_lock_tag_title, tagName))

    InfoMessage(
      text = stringResource(R.string.nfc_lock_tag_message)
    )
  }

@Composable
private fun LockTagViewScope.SuccessScreen() =
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(Distance.default)
  ) {
    Column(
      modifier = Modifier.align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Distance.default)
    ) {
      Image(
        drawableId = R.drawable.image_success,
        modifier = Modifier
      )
      TitleLarge(stringRes = R.string.nfc_lock_tag_success_title)
      BodyLarge(stringRes = R.string.nfc_lock_tag_success_message_1)
      BodyLarge(stringRes = R.string.nfc_lock_tag_success_message_2)
    }

    Button(
      text = stringResource(R.string.exit),
      onClick = { onCloseClick() },
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
    )
  }

@Composable
private fun LockTagViewScope.ErrorDialog(error: TagOperationError, tagName: String) =
  DialogWithIcon(
    title = stringResource(R.string.nfc_lock_tag_error_title),
    message = error.message(tagName),
    iconType = Warning,
    primaryButtonTitle = stringResource(R.string.status_try_again),
    secondaryButtonTitle = stringResource(R.string.exit),
    onPrimaryClick = { onRetryClick() },
    onSecondaryClick = { onCloseClick() }
  )

private val previewScope = object : LockTagViewScope {
  override fun onCloseClick() {}
  override fun onRetryClick() {}
}

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      viewState = LockTagViewState(tagName = "Living room light")
    )
  }
}

@SuplaPreview
@Composable
private fun PreviewSuccess() {
  SuplaTheme {
    previewScope.View(
      viewState = LockTagViewState(tagName = "Living room light", state = LockTagViewState.State.SUCCESS)
    )
  }
}

@SuplaPreview
@Composable
private fun PreviewError() {
  SuplaTheme {
    previewScope.View(
      viewState = LockTagViewState(tagName = "Living room light", error = TagOperationError.NOT_ENOUGH_MEMORY)
    )
  }
}
