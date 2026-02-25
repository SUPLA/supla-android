package org.supla.android.features.nfc.list
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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.dialogs.AlertDialog
import org.supla.android.ui.extensions.ifTrue
import org.supla.android.ui.views.EmptyListInfoView
import org.supla.android.ui.views.buttons.FloatingAddButton
import org.supla.android.ui.views.forms.WarningMessage
import org.supla.android.ui.views.icons.LockIcon
import org.supla.android.ui.views.list.components.ListItemIcon
import org.supla.android.ui.views.list.components.ListItemTitle
import org.supla.core.shared.infrastructure.LocalizedString

data class NfcTagListViewState(
  val items: List<NfcTagItem> = emptyList(),
  val nfcState: NfcState = NfcState.NOT_SUPPORTED,
  val showNfcDialog: Boolean = false
) {
  enum class NfcState {
    NOT_SUPPORTED,
    DISABLED,
    ENABLED;

    val supported: Boolean
      get() = this != NOT_SUPPORTED
  }
}

data class NfcTagItem(
  val id: Long,
  val name: String,
  val icon: ImageId?,
  val profileName: String?,
  val channelName: LocalizedString?,
  val action: ActionId?,
  val readOnly: Boolean,
  val channelNotExists: Boolean
)

interface NfcTagListScope {
  fun onAddClick()
  fun onItemClick(item: NfcTagItem)
  fun onNfcSettingsClick()
  fun onNfcDialogDismiss()
}

@Composable
fun NfcTagListScope.View(viewState: NfcTagListViewState) {
  Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      WarningMessage(viewState.nfcState)

      if (viewState.items.isNotEmpty()) {
        LazyColumn(
          state = rememberLazyListState(),
          modifier = Modifier.fillMaxSize()
        ) {
          items(
            items = viewState.items,
            key = { it.id },
            itemContent = { item ->
              ItemView(
                item = item,
                onItemClick = { onItemClick(item) },
              )
            }
          )
        }
      } else if (viewState.nfcState != NfcTagListViewState.NfcState.NOT_SUPPORTED) {
        Spacer(modifier = Modifier.weight(1f))
        EmptyListInfoView(modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.weight(1f))
      }
    }

    viewState.nfcState.supported.ifTrue { AddButton(modifier = Modifier.align(Alignment.BottomEnd)) }
    viewState.showNfcDialog.ifTrue { NfcDisabledDialog() }
  }
}

@Composable
private fun NfcTagListScope.WarningMessage(nfcState: NfcTagListViewState.NfcState) =
  when (nfcState) {
    NfcTagListViewState.NfcState.NOT_SUPPORTED ->
      WarningMessage(
        textRes = R.string.nfc_list_not_supported,
        iconRes = R.drawable.channel_warning_level2,
        modifier = Modifier.padding(Distance.default),
        withArrow = false
      )

    NfcTagListViewState.NfcState.DISABLED ->
      WarningMessage(
        textRes = R.string.nfc_list_nfc_disabled,
        modifier = Modifier.padding(Distance.default),
        onClick = { onNfcSettingsClick() }
      )

    NfcTagListViewState.NfcState.ENABLED -> {}
  }

@Composable
private fun NfcTagListScope.AddButton(modifier: Modifier = Modifier) =
  FloatingAddButton(
    modifier = modifier.padding(Distance.default),
    contentDescription = stringResource(R.string.nfc_list_add),
    onClick = { onAddClick() }
  )

@Composable
private fun NfcTagListScope.NfcDisabledDialog() =
  AlertDialog(
    title = stringResource(R.string.nfc_list_disabled_dialog_title),
    message = stringResource(R.string.nfc_list_disabled_dialog_message),
    positiveButtonTitle = stringResource(R.string.settings),
    negativeButtonTitle = stringResource(R.string.cancel),
    onDismiss = { onNfcDialogDismiss() },
    onPositiveClick = { onNfcSettingsClick() },
    onNegativeClick = { onNfcDialogDismiss() }
  )

@Composable
private fun ItemView(
  item: NfcTagItem,
  onItemClick: () -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 1.dp)
      .background(MaterialTheme.colorScheme.surface)
      .clickable(onClick = onItemClick)
      .padding(start = Distance.small, end = Distance.default)
      .padding(vertical = Distance.small)
  ) {
    item.icon?.let { ListItemIcon(imageId = it, scale = 1f) }
    Column(
      modifier = Modifier
        .padding(start = Distance.tiny)
        .weight(1f),
      verticalArrangement = Arrangement.spacedBy(Distance.tiny)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
      ) {
        ListItemTitle(
          text = item.name,
          onItemClick = onItemClick,
          onLongClick = onItemClick,
          maxLines = 1
        )
        item.readOnly.ifTrue { LockIcon() }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
      ) {
        if (item.channelNotExists) {
          ErrorIcon()
        } else if (item.action == null) {
          WarningIcon()
        }
        ActionText(item.action, item.channelName, item.profileName)
      }
    }
    ArrowIcon()
  }
}

@Composable
private fun ErrorIcon() =
  Image(
    painter = painterResource(R.drawable.channel_warning_level2),
    contentDescription = stringResource(R.string.nfc_list_warning_channel_missing)
  )

@Composable
private fun WarningIcon() =
  Image(
    painter = painterResource(R.drawable.channel_warning_level1),
    contentDescription = null
  )

@Composable
private fun ArrowIcon() =
  Icon(
    painter = painterResource(R.drawable.ic_arrow_right),
    contentDescription = null,
    modifier = Modifier.size(dimensionResource(R.dimen.icon_small_size)),
  )

@Composable
private fun ActionText(action: ActionId?, channelName: LocalizedString?, profileName: String?) {
  val context = LocalContext.current
  val text =
    if (action != null && channelName != null && profileName != null) {
      "${action.label(context)} - ${channelName(context)} ($profileName)"
    } else if (action != null && channelName != null) {
      "${action.label(context)} - ${channelName(context)}"
    } else {
      stringResource(R.string.nfc_list_missing_action)
    }
  Text(
    text = text,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurface,
    overflow = TextOverflow.Ellipsis,
    maxLines = 1
  )
}

private val previewScope = object : NfcTagListScope {
  override fun onAddClick() {}
  override fun onItemClick(item: NfcTagItem) {}
  override fun onNfcSettingsClick() {}
  override fun onNfcDialogDismiss() {}
}

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      viewState = NfcTagListViewState(
        items = listOf(
          item(0, "Tag 1"),
          item(1, "Tag 2", ActionId.OPEN, "Front door"),
          item(2, "Tag 3", ActionId.TURN_ON, "Living room light")
        ),
        nfcState = NfcTagListViewState.NfcState.DISABLED
      )
    )
  }
}

@SuplaPreview
@Composable
private fun Preview_NfcUnsupported() {
  SuplaTheme {
    previewScope.View(
      viewState = NfcTagListViewState(
        nfcState = NfcTagListViewState.NfcState.NOT_SUPPORTED
      )
    )
  }
}

private fun item(id: Long, name: String, action: ActionId? = null, channelName: String? = null): NfcTagItem =
  NfcTagItem(
    id = id,
    name = name,
    icon = null,
    profileName = if (id < 2) "Default" else null,
    action = action,
    channelName = channelName?.let { LocalizedString.Constant(it) },
    readOnly = id.mod(2) == 1,
    channelNotExists = id == 2L
  )
