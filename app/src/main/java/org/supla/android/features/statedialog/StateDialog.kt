package org.supla.android.features.statedialog
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtonsRow
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.buttons.IconButton
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.core.shared.infrastructure.LocalizedString

data class StateDialogViewState(
  val title: LocalizedString,
  val subtitle: LocalizedString? = null,
  val loading: Boolean = true,
  val showArrows: Boolean = false,
  val values: Map<StateDialogItem, LocalizedString> = emptyMap(),
  val showChangeLifespanButton: Boolean = false
)

interface StateDialogScope {
  fun onStateDialogDismiss()
  fun onStateDialogNext()
  fun onStateDialogPrevious()
  fun onStateDialogChangeLifespan()
}

@Composable
fun StateDialogScope.StateDialog(
  state: StateDialogViewState
) {
  Dialog(onDismiss = { onStateDialogDismiss() }) {
    Header(state)
    Separator(style = SeparatorStyle.LIGHT)

    if (state.loading) {
      CircularProgressIndicator(
        modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .padding(vertical = Distance.default)
          .size(64.dp)
      )
    } else {
      Spacer(modifier = Modifier.height(Distance.small))
      state.values.forEach { ValueRow(it.key, it.value) }

      if (state.showChangeLifespanButton) {
        ChangeLifespanButton()
      } else {
        Spacer(modifier = Modifier.height(Distance.small))
      }
    }

    Separator(style = SeparatorStyle.LIGHT)
    DialogButtonsRow {
      OutlinedButton(
        onClick = { onStateDialogDismiss() },
        text = stringResource(id = R.string.channel_btn_close),
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun ValueRow(item: StateDialogItem, value: LocalizedString) =
  Row(
    horizontalArrangement = Arrangement.spacedBy(1.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .height(IntrinsicSize.Max)
      .padding(horizontal = Distance.tiny)
      .background(MaterialTheme.colorScheme.outline)
  ) {
    Text(
      text = stringResource(id = item.captionResource),
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier
        .background(MaterialTheme.colorScheme.surface)
        .weight(0.5f)
        .padding(horizontal = Distance.tiny)
        .fillMaxHeight()
    )
    Text(
      text = value(LocalContext.current),
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier
        .background(MaterialTheme.colorScheme.surface)
        .weight(0.5f)
        .padding(horizontal = Distance.tiny)
        .fillMaxHeight()
    )
  }

@Composable
private fun StateDialogScope.Header(state: StateDialogViewState) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    if (state.showArrows) {
      LeftArrow()
    }
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .weight(1f)
        .padding(vertical = if (state.showArrows) Distance.small else Distance.default)
    ) {
      Title(text = state.title(LocalContext.current))
      state.subtitle?.let {
        SubTitle(text = it(LocalContext.current))
      }
    }
    if (state.showArrows) {
      RightArrow()
    }
  }
}

@Composable
private fun Title(text: String) =
  Text(
    text = text,
    style = MaterialTheme.typography.headlineSmall,
    textAlign = TextAlign.Center,
    modifier = Modifier
  )

@Composable
private fun SubTitle(text: String) =
  Text(
    text = text,
    style = MaterialTheme.typography.labelSmall,
    textAlign = TextAlign.Center,
    modifier = Modifier
  )

@Composable
private fun StateDialogScope.LeftArrow() =
  IconButton(
    icon = R.drawable.ic_arrow_right,
    onClick = { onStateDialogPrevious() },
    rotate = true
  )

@Composable
private fun StateDialogScope.RightArrow() =
  IconButton(
    icon = R.drawable.ic_arrow_right,
    onClick = { onStateDialogNext() }
  )

@Composable
private fun StateDialogScope.ChangeLifespanButton() =
  TextButton(
    onClick = { onStateDialogChangeLifespan() },
    modifier = Modifier.padding(horizontal = Distance.default)
  ) {
    Text(
      text = stringResource(R.string.chanel_lightsource_lifespan_settings),
      style = MaterialTheme.typography.labelSmall,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth()
    )
  }

private val emptyScope = object : StateDialogScope {
  override fun onStateDialogDismiss() {}
  override fun onStateDialogNext() {}
  override fun onStateDialogPrevious() {}
  override fun onStateDialogChangeLifespan() {}
}

@Composable
@Preview
private fun Preview() {
  SuplaTheme {
    emptyScope.StateDialog(
      state = StateDialogViewState(
        title = LocalizedString.Constant("Dimmer"),
        loading = false,
        values = mapOf(
          StateDialogItem.CHANNEL_ID to LocalizedString.Constant("123456"),
          StateDialogItem.BRIDGE_SIGNAL to LocalizedString.Constant("100%")
        )
      )
    )
  }
}

@Composable
@Preview
private fun Preview_ManyIds() {
  SuplaTheme {
    emptyScope.StateDialog(
      state = StateDialogViewState(
        title = LocalizedString.Constant("Dimmer 1"),
        subtitle = LocalizedString.WithResourceIntInt(R.string.state_dialog_index, 1, 2),
        loading = false,
        showArrows = true,
        values = mapOf(
          StateDialogItem.CHANNEL_ID to LocalizedString.Constant("123456"),
          StateDialogItem.BRIDGE_SIGNAL to LocalizedString.Constant("100%")
        ),
        showChangeLifespanButton = true
      )
    )
  }
}
