package org.supla.android.features.nfc.detail
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.ViewState
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.formatting.LocalDateTimeFormatter
import org.supla.android.data.source.local.entity.NfcCallResult
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.tools.SuplaSmallPreview
import org.supla.android.ui.dialogs.AlertDialog
import org.supla.android.ui.dialogs.AlertDialogCritical
import org.supla.android.ui.views.EmptyListInfoView
import org.supla.android.ui.views.Small
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.IconButton
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.icons.LockIcon
import org.supla.android.ui.views.texts.BodyLarge
import org.supla.android.ui.views.texts.BodyMedium
import org.supla.android.ui.views.texts.BodySmall
import org.supla.android.ui.views.texts.Label
import org.supla.android.ui.views.texts.LabelLarge
import org.supla.android.ui.views.texts.TitleSmall
import org.supla.core.shared.infrastructure.LocalizedString
import java.time.LocalDateTime
import java.util.UUID

data class NfcTagDetailViewState(
  val tagName: String = "",
  val tagUuid: String = "",
  val tagLocked: Boolean = false,
  val actionId: ActionId? = null,
  val subjectName: LocalizedString? = null,
  val profileName: String? = null,
  val lastReadingItems: List<ReadingItem> = emptyList(),

  val dialogToShow: DialogType? = null
) : ViewState() {
  data class ReadingItem(
    val date: LocalDateTime,
    val result: NfcCallResult
  )

  enum class DialogType {
    DELETE_TAG, DELETE_LOCKED_TAG, INFO
  }
}

interface NfcTagDetailViewScope {
  fun onInfoClick()
  fun onLockClick()
  fun onEditClick()
  fun onDeleteClick()
  fun onDismissDialogs()
}

@Composable
fun NfcTagDetailViewScope.View(viewState: NfcTagDetailViewState) {
  Box {
    Column {
      Column(
        verticalArrangement = Arrangement.spacedBy(Distance.tiny),
        modifier = Modifier
          .verticalScroll(rememberScrollState())
          .weight(1f)
      ) {
        TagDetails(viewState)
        TagHistory(viewState.lastReadingItems)
      }
      Button(
        text = stringResource(R.string.edit_nfc_tag_title),
        modifier = Modifier
          .fillMaxWidth()
          .padding(Distance.default),
        onClick = { onEditClick() },
      )
    }
  }

  when (viewState.dialogToShow) {
    NfcTagDetailViewState.DialogType.DELETE_TAG -> DeleteDialog()
    NfcTagDetailViewState.DialogType.DELETE_LOCKED_TAG -> DeleteLockedDialog()
    NfcTagDetailViewState.DialogType.INFO -> InfoDialog()
    null -> {} // nothing to do
  }
}

@Composable
private fun NfcTagDetailViewScope.TagDetails(viewState: NfcTagDetailViewState) =
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(Distance.default)
  ) {
    TitleSmall(stringResource(R.string.nfc_detail_tag_data))
    Spacer(modifier = Modifier.height(Distance.small))

    Label(text = stringResource(R.string.edit_nfc_tag_name))
    BodyLarge(text = viewState.tagName)
    Spacer(modifier = Modifier.height(Distance.small))

    Label(text = stringResource(R.string.widget_configure_action_label))
    ActionView(viewState.actionId, viewState.subjectName, viewState.profileName)
    Spacer(modifier = Modifier.height(Distance.small))

    Label(text = "UUID")
    BodyLarge(text = viewState.tagUuid)
    Spacer(modifier = Modifier.height(Distance.small))

    if (viewState.tagLocked) {
      LockedRow()
    } else {
      Row(horizontalArrangement = Arrangement.spacedBy(Distance.tiny)) {
        LockButton(modifier = Modifier.weight(1f))
        InfoButton()
      }
    }
  }

@Composable
private fun ActionView(actionId: ActionId?, subjectName: LocalizedString?, profileName: String?) {
  if (actionId != null && subjectName != null && profileName != null) {
    val action = actionId.label(LocalContext.current)
    val subjectName = subjectName(LocalContext.current)
    BodyLarge(text = "$action - $subjectName ($profileName)")
  } else if (actionId != null && subjectName != null) {
    val action = actionId.label(LocalContext.current)
    val subjectName = subjectName(LocalContext.current)
    BodyLarge(text = "$action - $subjectName")
  } else {
    Row(
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
      verticalAlignment = Alignment.CenterVertically
    ) {
      WarningIcon()
      BodyLarge(text = stringResource(R.string.nfc_list_missing_action))
    }
  }
}

@Composable
private fun NfcTagDetailViewScope.LockButton(modifier: Modifier = Modifier) =
  OutlinedButton(
    onClick = { onLockClick() },
    modifier = modifier
  ) {
    LockIcon()
    LabelLarge(text = stringResource(R.string.nfc_detail_tag_lock))
  }

@Composable
private fun NfcTagDetailViewScope.LockedRow() =
  Row(horizontalArrangement = Arrangement.spacedBy(Distance.tiny)) {
    LockedLabel()
    InfoButton()
  }

@Composable
private fun RowScope.LockedLabel() =
  Box(
    modifier = Modifier
      .defaultMinSize(minHeight = dimensionResource(R.dimen.button_default_size))
      .background(
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
      )
      .weight(1f)
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.align(Alignment.Center)
    ) {
      LockIcon(color = MaterialTheme.colorScheme.onBackground)
      BodySmall(stringRes = R.string.nfc_detail_tag_locked)
    }
  }

@Composable
private fun NfcTagDetailViewScope.InfoButton() =
  IconButton(
    icon = R.drawable.ic_info_filled,
    onClick = { onInfoClick() },
    tint = MaterialTheme.colorScheme.primary,
    modifier = Modifier.background(
      color = MaterialTheme.colorScheme.surfaceVariant,
      shape = RoundedCornerShape(
        corner = CornerSize(
          dimensionResource(R.dimen.radius_default)
        )
      )
    )
  )

@Composable
private fun TagHistory(readings: List<NfcTagDetailViewState.ReadingItem>) =
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(Distance.default)
  ) {
    TitleSmall(stringResource(R.string.nfc_detail_last_readings))
    Spacer(modifier = Modifier.height(Distance.small))
    if (readings.isEmpty()) {
      EmptyListInfoView(
        modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .padding(Distance.default),
        size = Small
      )
    } else {
      Column {
        readings.forEachIndexed { index, item ->
          Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
          ) {
            TimelineDot(leading = index == 0, trailing = index == readings.size - 1)
            BodyMedium(
              text = LocalDateTimeFormatter.current.format(item.date).invoke(LocalContext.current),
              modifier = Modifier.padding(vertical = 10.dp)
            )
            SeparatorDot()
            ItemResultText(item.result)
          }
        }
      }
    }
  }

@Composable
private fun ItemResultText(result: NfcCallResult) =
  when (result) {
    NfcCallResult.SUCCESS -> {
      BodyMedium(R.string.notifications_active, color = MaterialTheme.colorScheme.primary)
      BodyMedium(R.string.nfc_detail_action_completed)
    }
    NfcCallResult.FAILURE -> {
      BodyMedium(R.string.notifications_inactive, color = MaterialTheme.colorScheme.error)
      BodyMedium(R.string.nfc_detail_action_failure_other)
    }
    NfcCallResult.ACTION_MISSING -> {
      BodyMedium(R.string.notifications_inactive, color = MaterialTheme.colorScheme.error)
      BodyMedium(R.string.nfc_detail_action_failure_missing)
    }
    NfcCallResult.TAG_ADDED -> {
      BodyMedium("✦", color = MaterialTheme.colorScheme.secondary)
      BodyMedium(R.string.nfc_detail_action_added)
    }
  }

@Composable
private fun SeparatorDot() =
  Box(
    modifier = Modifier
      .size(6.dp)
      .background(color = MaterialTheme.colorScheme.outline, shape = CircleShape)
  )

@Composable
private fun TimelineDot(leading: Boolean = false, trailing: Boolean = false) =
  Column(
    verticalArrangement = Arrangement.spacedBy(3.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxHeight()
  ) {
    if (leading) {
      Box(modifier = Modifier.weight(1f))
    } else {
      Box(
        modifier = Modifier
          .width(2.dp)
          .weight(1f)
          .background(color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(bottomStart = 1.dp, bottomEnd = 1.dp))
      )
    }
    Box(
      modifier = Modifier
        .size(8.dp)
        .background(color = MaterialTheme.colorScheme.onBackground, shape = CircleShape)
    )
    if (trailing) {
      Box(modifier = Modifier.weight(1f))
    } else {
      Box(
        modifier = Modifier
          .width(2.dp)
          .weight(1f)
          .background(color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(topStart = 1.dp, topEnd = 1.dp))
      )
    }
  }

@Composable
private fun WarningIcon() =
  Image(
    painter = painterResource(R.drawable.channel_warning_level1),
    contentDescription = null
  )

@Composable
private fun NfcTagDetailViewScope.DeleteDialog() =
  AlertDialogCritical(
    title = stringResource(R.string.nfc_detail_delete_dialog_title),
    message = stringResource(R.string.nfc_detail_delete_dialog_message),
    onPrimaryClick = { onDeleteClick() },
    onSecondaryClick = { onDismissDialogs() }
  )

@Composable
private fun NfcTagDetailViewScope.DeleteLockedDialog() =
  AlertDialogCritical(
    title = stringResource(R.string.nfc_detail_delete_locked_dialog_title),
    message = stringResource(R.string.nfc_detail_delete_locked_dialog_message),
    onPrimaryClick = { onDeleteClick() },
    onSecondaryClick = { onDismissDialogs() }
  )

@Composable
private fun NfcTagDetailViewScope.InfoDialog() =
  AlertDialog(
    title = stringResource(R.string.nfc_detail_info_dialog_title),
    message = stringResource(R.string.nfc_detail_info_dialog_message),
    positiveButtonTitle = null,
    negativeButtonTitle = stringResource(R.string.channel_btn_close),
    onNegativeClick = { onDismissDialogs() }
  )

private val previewScope = object : NfcTagDetailViewScope {
  override fun onInfoClick() {}
  override fun onLockClick() {}
  override fun onEditClick() {}
  override fun onDeleteClick() {}
  override fun onDismissDialogs() {}
}

private val readingItems = listOf(
  NfcTagDetailViewState.ReadingItem(
    date = LocalDateTime.now(),
    result = NfcCallResult.SUCCESS
  ),
  NfcTagDetailViewState.ReadingItem(
    date = LocalDateTime.now().minusMinutes(15),
    result = NfcCallResult.FAILURE
  ),
  NfcTagDetailViewState.ReadingItem(
    date = LocalDateTime.now().minusHours(4),
    result = NfcCallResult.ACTION_MISSING
  ),
  NfcTagDetailViewState.ReadingItem(
    date = LocalDateTime.now().minusDays(5),
    result = NfcCallResult.SUCCESS
  )
)

@SuplaPreview
@SuplaSmallPreview
@SuplaPreviewLandscape
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      viewState = NfcTagDetailViewState(
        tagName = "Living room light",
        tagUuid = UUID.randomUUID().toString(),
        actionId = ActionId.TURN_ON,
        lastReadingItems = readingItems
      )
    )
  }
}

@SuplaPreview
@Composable
private fun PreviewLocked() {
  SuplaTheme {
    previewScope.View(
      viewState = NfcTagDetailViewState(
        tagName = "Living room light",
        tagUuid = UUID.randomUUID().toString(),
        tagLocked = true,
        actionId = ActionId.TURN_ON
      )
    )
  }
}
