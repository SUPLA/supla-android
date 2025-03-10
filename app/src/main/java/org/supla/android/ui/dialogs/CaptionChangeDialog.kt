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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.lib.actions.SubjectType
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.TextField
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.core.shared.infrastructure.LocalizedString

data class CaptionChangeDialogState(
  val remoteId: Int,
  val profileId: Long,
  val subjectType: SubjectType,
  val caption: String,
  val authorized: Boolean = false,
  val loading: Boolean = false,
  val error: LocalizedString? = null
)

interface CaptionChangeDialogScope {
  fun onCaptionChangeDismiss()
  fun onStateChange(state: CaptionChangeDialogState)
  fun onCaptionChangeConfirmed()
}

@Composable
fun CaptionChangeDialogScope.CaptionChangeDialog(
  state: CaptionChangeDialogState
) {
  if (state.authorized) {
    Dialog(onDismiss = { onCaptionChangeDismiss() }) {
      DialogHeader(title = stringResource(R.string.change_caption_header))
      Separator(style = SeparatorStyle.LIGHT)

      CaptionTextField(
        state = state,
        onStateChange = { onStateChange(it) }
      )
      state.error?.let {
        ErrorText(text = it(LocalContext.current))
      }

      Separator(style = SeparatorStyle.LIGHT, modifier = Modifier.padding(top = Distance.default))
      DialogButtonsRow {
        OutlinedButton(
          onClick = { onCaptionChangeDismiss() },
          text = stringResource(id = R.string.cancel),
          modifier = Modifier.weight(1f)
        )

        if (state.loading) {
          Box(modifier = Modifier.weight(1f)) {
            CircularProgressIndicator(
              modifier = Modifier
                .align(Alignment.Center)
                .size(32.dp)
            )
          }
        } else {
          Button(
            onClick = { onCaptionChangeConfirmed() },
            text = stringResource(id = R.string.ok),
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }
}

@Composable
private fun CaptionTextField(
  state: CaptionChangeDialogState,
  onStateChange: (CaptionChangeDialogState) -> Unit = { }
) {
  TextField(
    value = state.caption,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = Distance.default, top = Distance.default, end = Distance.default),
    label = { Text(text = stringResource(id = state.captionLabelRes)) },
    singleLine = true,
    onValueChange = { onStateChange(state.copy(caption = it)) }
  )
}

@Composable
private fun ErrorText(text: String) =
  Text(
    text = text,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.error,
    modifier = Modifier.padding(
      start = Distance.default.plus(Distance.small),
      end = Distance.default.plus(Distance.small),
      top = 4.dp
    )
  )

private val CaptionChangeDialogState.captionLabelRes: Int
  get() = when (subjectType) {
    SubjectType.CHANNEL -> R.string.channel_name
    SubjectType.GROUP -> R.string.group_name
    SubjectType.SCENE -> R.string.scene_name
  }

private val emptyScope = object : CaptionChangeDialogScope {
  override fun onCaptionChangeDismiss() {}
  override fun onStateChange(state: CaptionChangeDialogState) {}
  override fun onCaptionChangeConfirmed() {}
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    emptyScope.CaptionChangeDialog(
      CaptionChangeDialogState(
        remoteId = 123,
        profileId = 1L,
        subjectType = SubjectType.CHANNEL,
        caption = "Thermostat",
        error = null
      )
    )
  }
}

@Preview
@Composable
private fun Preview_Error() {
  SuplaTheme {
    emptyScope.CaptionChangeDialog(
      CaptionChangeDialogState(
        remoteId = 123,
        profileId = 1L,
        subjectType = SubjectType.CHANNEL,
        caption = "Thermostat",
        error = LocalizedString.Constant("Some error!")
      )
    )
  }
}
