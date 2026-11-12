package org.supla.android.features.details.actiontrigger.general
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.ViewState
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.ucFirst
import org.supla.android.features.details.relayschedule.OperatingMode
import org.supla.android.features.details.relayschedule.ui.OperatingButtons
import org.supla.android.features.details.relayschedule.ui.OperatingButtonsScope
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtonsColumn
import org.supla.android.ui.dialogs.DialogButtonsRow
import org.supla.android.ui.dialogs.DialogHeader
import org.supla.android.ui.dialogs.DialogNegativeButton
import org.supla.android.ui.dialogs.DialogPositiveButton
import org.supla.android.ui.extensions.isPhoneLandscape
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.Switch
import org.supla.android.ui.views.buttons.SwitchButtonsLayout
import org.supla.android.ui.views.buttons.TextButton
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults
import org.supla.android.ui.views.channelstate.ChannelCurrentData
import org.supla.android.ui.views.channelstate.ChannelProgramData
import org.supla.android.ui.views.channelstate.ChannelState
import org.supla.android.ui.views.forms.TimePicker
import org.supla.android.ui.views.icons.LockIcon
import org.supla.android.ui.views.icons.LockOpenIcon
import org.supla.android.ui.views.texts.BodyMedium
import org.supla.android.ui.views.texts.Label
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

data class ActionTriggerGeneralViewState(
  val deviceStateData: ChannelCurrentData? = null,
  val programInfo: List<ChannelProgramData> = emptyList(),
  val operatingMode: OperatingMode? = null,
  val manualButtonDisabled: Boolean = false,
  val weeklyButtonDisabled: Boolean = false,
  val showLockButtons: Boolean = false,
  val lockButtonActive: Boolean = false,
  val unlockButtonActive: Boolean = false,
  val lockButtonDisabled: Boolean = false,
  val unlockButtonDisabled: Boolean = false,
  val showTemporaryUnlockIcon: Boolean = false,
  val showAllowTemporaryUnlock: Boolean = false,
  val allowTemporaryUnlock: Boolean = false,
  val allowTemporaryUnlockDisabled: Boolean = false,
  val unlockDialog: UnlockDialogState? = null
) : ViewState()

data class UnlockDialogState(
  val temporaryUnlockDurationS: Int,
  val unlockTemporaryAllowed: Boolean
)

interface ActionTriggerGeneralScope : OperatingButtonsScope {
  fun onLock()
  fun onUnlock()
  fun onAllowTemporaryUnlockChanged(allow: Boolean)
  fun onUnlockDialogDurationChange(value: Int)
  fun onUnlockDialogDismiss()
  fun onUnlockDialogUnlock()
  fun onUnlockDialogTemporaryUnlock()
}

@Composable
fun ActionTriggerGeneralScope.View(state: ActionTriggerGeneralViewState) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    if (state.deviceStateData != null || state.programInfo.isNotEmpty()) {
      ChannelState(state.deviceStateData, state.programInfo)
    }
    Spacer(modifier = Modifier.weight(1f))

    if (state.showAllowTemporaryUnlock) {
      AllowTemporaryUnlockRow(
        checked = state.allowTemporaryUnlock,
        enabled = !state.allowTemporaryUnlockDisabled,
        onCheckedChange = { onAllowTemporaryUnlockChanged(it) }
      )
    }

    state.operatingMode?.let { operatingMode ->
      OperatingButtons(
        operatingMode = operatingMode,
        manualButtonDisabled = state.manualButtonDisabled,
        weeklyButtonDisabled = state.weeklyButtonDisabled,
        autoButtonDisabled = true,
        modifier = Modifier
          .padding(horizontal = Distance.horizontal)
          .padding(top = Distance.vertical)
          .widthIn(max = 612.dp)
      )
    }

    if (state.showLockButtons) {
      SwitchButtonsLayout(
        modifier = Modifier
          .padding(horizontal = Distance.horizontal)
          .padding(vertical = Distance.vertical)
      ) {
        ActionTriggerModeButton(
          labelRes = R.string.general_unlock,
          locked = false,
          active = state.unlockButtonActive,
          disabled = state.unlockButtonDisabled,
          showTemporaryUnlockIcon = state.showTemporaryUnlockIcon,
          onClick = { onUnlock() }
        )
        ActionTriggerModeButton(
          labelRes = R.string.general_lock,
          locked = true,
          active = state.lockButtonActive,
          disabled = state.lockButtonDisabled,
          onClick = { onLock() }
        )
      }
    }
  }

  state.unlockDialog?.let { UnlockDialog(it) }
}

@Composable
private fun AllowTemporaryUnlockRow(
  checked: Boolean,
  enabled: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.padding(horizontal = Distance.horizontal).widthIn(max = 612.dp)
  ) {
    BodyMedium(
      text = stringResource(R.string.at_allow_temporary_unlock),
      color = if (enabled) MaterialTheme.colorScheme.onSurface else colorResource(R.color.disabled),
      modifier = Modifier.weight(1f),
      textAlign = TextAlign.Start
    )
    Switch(
      checked = checked,
      enabled = enabled,
      onCheckedChange = onCheckedChange
    )
  }
}

@Composable
private fun ActionTriggerModeButton(
  labelRes: Int,
  locked: Boolean,
  active: Boolean,
  disabled: Boolean = false,
  showTemporaryUnlockIcon: Boolean = false,
  onClick: () -> Unit
) {
  SuplaButton(
    colors = SuplaButtonDefaults.primaryColors(),
    active = active,
    disabled = disabled,
    onClick = onClick
  ) { color ->
    Row(
      modifier = Modifier.align(Alignment.Center),
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (locked) {
        LockIcon(color = color)
      } else if (showTemporaryUnlockIcon) {
        Image(
          drawableId = R.drawable.ic_lock_temporary_open,
          tint = color
        )
      } else {
        LockOpenIcon(color = color)
      }
      Text(text = stringResource(labelRes).ucFirst(), color = color)
    }
  }
}

@Composable
private fun ActionTriggerGeneralScope.UnlockDialog(state: UnlockDialogState) {
  Dialog(onDismiss = ::onUnlockDialogDismiss) {
    DialogHeader(stringResource(R.string.action_trigger_unlock_dialog_title))
    Label(
      text = stringResource(R.string.action_trigger_schedule_temporary_unlock_duration),
      modifier = Modifier.padding(
        start = Distance.default + 12.dp,
        end = Distance.default + 12.dp
      )
    )
    TimePicker(
      timeInSeconds = state.temporaryUnlockDurationS,
      maxTimeInSeconds = 64_800,
      onTimeChanged = ::onUnlockDialogDurationChange,
      modifier = Modifier.fillMaxWidth().padding(horizontal = Distance.horizontal)
    )
    Buttons(state)
  }
}

@Composable
private fun ActionTriggerGeneralScope.Buttons(state: UnlockDialogState) {
  if (LocalConfiguration.current.isPhoneLandscape) {
    DialogButtonsRow {
      TextButton(
        onClick = ::onUnlockDialogDismiss,
        text = stringResource(R.string.cancel)
      )
      DialogNegativeButton(
        onClick = ::onUnlockDialogUnlock,
        modifier = Modifier.weight(1f),
        textRes = R.string.general_unlock
      )
      DialogPositiveButton(
        onClick = ::onUnlockDialogTemporaryUnlock,
        modifier = Modifier.weight(1f),
        enabled = state.unlockTemporaryAllowed,
        textRes = R.string.action_trigger_unlock_temporarily
      )
    }
  } else {
    DialogButtonsColumn(spacing = 0.dp) {
      DialogPositiveButton(
        onClick = ::onUnlockDialogTemporaryUnlock,
        modifier = Modifier.fillMaxWidth().padding(bottom = Distance.small),
        enabled = state.unlockTemporaryAllowed,
        textRes = R.string.action_trigger_unlock_temporarily
      )
      DialogNegativeButton(
        onClick = ::onUnlockDialogUnlock,
        modifier = Modifier.fillMaxWidth().padding(bottom = Distance.tiny),
        textRes = R.string.general_unlock
      )
      TextButton(
        onClick = ::onUnlockDialogDismiss,
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.cancel)
      )
    }
  }
}

private val previewScope = object : ActionTriggerGeneralScope {
  override fun onManual() {}
  override fun onWeekly() {}
  override fun onAuto() {}
  override fun onLock() {}
  override fun onUnlock() {}
  override fun onAllowTemporaryUnlockChanged(allow: Boolean) {}
  override fun onUnlockDialogDurationChange(value: Int) {}
  override fun onUnlockDialogDismiss() {}
  override fun onUnlockDialogUnlock() {}
  override fun onUnlockDialogTemporaryUnlock() {}
}

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun PreviewManual() {
  SuplaTheme {
    previewScope.View(
      state = ActionTriggerGeneralViewState(
        deviceStateData = ChannelCurrentData(
          label = localizedString(R.string.details_timer_state_label),
          icon = null,
          value = LocalizedString.Constant("normal")
        ),
        showLockButtons = true,
        showAllowTemporaryUnlock = true,
        allowTemporaryUnlock = true,
        unlockButtonActive = true
      )
    )
  }
}

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun PreviewWeeklySchedule() {
  SuplaTheme {
    previewScope.View(
      state = ActionTriggerGeneralViewState(
        programInfo = listOf(
          ChannelProgramData(
            type = ChannelProgramData.Type.CURRENT,
            time = LocalizedString.Constant("until 12:30"),
            description = LocalizedString.Constant("normal")
          ),
          ChannelProgramData(
            type = ChannelProgramData.Type.NEXT,
            description = LocalizedString.Constant("triggered")
          )
        ),
        operatingMode = OperatingMode(manualAllowed = true, weeklyAllowed = true, weeklyActive = true),
        showLockButtons = true,
        showAllowTemporaryUnlock = true,
        allowTemporaryUnlock = false,
        allowTemporaryUnlockDisabled = true,
      )
    )
  }
}

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun PreviewDialog() {
  SuplaTheme {
    previewScope.View(
      state = ActionTriggerGeneralViewState(
        deviceStateData = ChannelCurrentData(
          label = localizedString(R.string.details_timer_state_label),
          icon = null,
          value = LocalizedString.Constant("normal")
        ),
        unlockDialog = UnlockDialogState(10, true)
      )
    )
  }
}
