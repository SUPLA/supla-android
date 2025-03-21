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
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.gray
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.buttons.IconButton
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.core.shared.infrastructure.LocalizedString

data class StateDialogViewState(
  val title: LocalizedString,
  val online: Boolean,
  val subtitle: LocalizedString? = null,
  val loading: Boolean = true,
  val showArrows: Boolean = false,
  val function: LocalizedString? = null,
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
  var offset by remember { mutableFloatStateOf(0f) }

  Dialog(
    onDismiss = { onStateDialogDismiss() },
    modifier = Modifier.pointerInput(Unit) {
      detectHorizontalDragGestures(
        onDragStart = { offset = 0f },
        onHorizontalDrag = { _, dragAmount -> offset += dragAmount },
        onDragEnd = {
          if (state.showArrows) {
            if (offset > 100.dp.toPx()) {
              onStateDialogPrevious()
            } else if (offset < -(100.dp.toPx())) {
              onStateDialogNext()
            }
          }
        }
      )
    }
  ) {
    Header(state)
    Separator(style = SeparatorStyle.LIGHT)

    Box(modifier = Modifier.height(IntrinsicSize.Max)) {
      Column {
        Spacer(modifier = Modifier.height(Distance.small))
        state.function?.let { ValueRow(R.string.function, it) }
        state.values.forEach { ValueRow(it.key.captionResource, it.value) }

        if (state.showChangeLifespanButton) {
          ChangeLifespanButton()
        } else {
          Spacer(modifier = Modifier.height(Distance.small))
        }
      }

      if (state.loading) {
        Loader()
      } else if (!state.online) {
        OfflineView()
      }
    }

    Separator(style = SeparatorStyle.LIGHT)
    Buttons(state = state)
  }
}

@Composable
private fun Loader() =
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.surface)
  ) {
    CircularProgressIndicator(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(vertical = Distance.default)
        .size(64.dp)
    )
  }

@Composable
private fun OfflineView() =
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
    modifier = Modifier
      .fillMaxSize()
      .padding(vertical = Distance.small)
      .background(MaterialTheme.colorScheme.surface)
  ) {
    Spacer(modifier = Modifier.weight(1f))
    Image(R.drawable.ic_offline, tint = MaterialTheme.colorScheme.gray)
    Text(
      text = "offline",
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.gray
    )
    Spacer(modifier = Modifier.weight(1f))
  }

@Composable
private fun ValueRow(labelRes: Int, value: LocalizedString) =
  Row(
    horizontalArrangement = Arrangement.spacedBy(1.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .height(IntrinsicSize.Max)
      .padding(horizontal = Distance.tiny)
      .background(MaterialTheme.colorScheme.outline)
  ) {
    Text(
      text = stringResource(id = labelRes),
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier
        .background(MaterialTheme.colorScheme.surface)
        .weight(0.5f)
        .padding(start = Distance.tiny, end = 4.dp)
        .fillMaxHeight()
    )
    Text(
      text = value(LocalContext.current),
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
      modifier = Modifier
        .background(MaterialTheme.colorScheme.surface)
        .weight(0.5f)
        .padding(start = 4.dp, end = Distance.tiny)
        .fillMaxHeight()
    )
  }

@Composable
private fun Header(state: StateDialogViewState) {
  Row(verticalAlignment = Alignment.CenterVertically) {
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
  }
}

@Composable
private fun StateDialogScope.Buttons(state: StateDialogViewState) =
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = Distance.default, horizontal = Distance.small),
    horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (state.showArrows) {
      LeftArrow()
    }
    Spacer(modifier = Modifier.weight(0.5f))
    OutlinedButton(
      onClick = { onStateDialogDismiss() },
      text = stringResource(id = R.string.channel_btn_close),
      modifier = Modifier.height(48.dp)
    )
    Spacer(modifier = Modifier.weight(0.5f))
    if (state.showArrows) {
      RightArrow()
    }
  }

@Composable
private fun Title(text: String) =
  Text(
    text = text,
    style = MaterialTheme.typography.headlineSmall,
    textAlign = TextAlign.Center,
    modifier = Modifier.padding(horizontal = Distance.default),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis
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
    rotate = true,
    tint = MaterialTheme.colorScheme.onBackground,
    modifier = Modifier.border(
      width = 1.dp,
      color = MaterialTheme.colorScheme.primary,
      shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
    )
  )

@Composable
private fun StateDialogScope.RightArrow() =
  IconButton(
    icon = R.drawable.ic_arrow_right,
    onClick = { onStateDialogNext() },
    tint = MaterialTheme.colorScheme.onBackground,
    modifier = Modifier.border(
      width = 1.dp,
      color = MaterialTheme.colorScheme.primary,
      shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
    )
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
        online = true,
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
        online = true,
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

@Composable
@Preview
private fun Preview_Offline() {
  SuplaTheme {
    emptyScope.StateDialog(
      state = StateDialogViewState(
        title = LocalizedString.Constant("Dimmer"),
        online = false,
        loading = false,
        values = mapOf(
          StateDialogItem.CHANNEL_ID to LocalizedString.Constant("123456"),
          StateDialogItem.BRIDGE_SIGNAL to LocalizedString.Constant("100%")
        ),
      )
    )
  }
}

@Composable
@Preview
private fun Preview_Loading() {
  SuplaTheme {
    emptyScope.StateDialog(
      state = StateDialogViewState(
        title = LocalizedString.Constant("Dimmer"),
        online = false,
        loading = true,
        values = mapOf(
          StateDialogItem.CHANNEL_ID to LocalizedString.Constant("123456"),
          StateDialogItem.BRIDGE_SIGNAL to LocalizedString.Constant("100%")
        ),
      )
    )
  }
}
