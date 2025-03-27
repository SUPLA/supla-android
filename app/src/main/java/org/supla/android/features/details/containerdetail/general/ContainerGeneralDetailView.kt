package org.supla.android.features.details.containerdetail.general
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.containerdetail.general.ui.ContainerIconView
import org.supla.android.features.details.containerdetail.general.ui.ContainerType
import org.supla.android.features.details.containerdetail.general.ui.ControlLevel
import org.supla.android.features.details.containerdetail.general.ui.ErrorLevel
import org.supla.android.features.details.containerdetail.general.ui.WarningLevel
import org.supla.android.images.ImageId
import org.supla.android.tools.BACKGROUND_COLOR
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.lists.channelissues.ChannelIssuesView
import org.supla.android.ui.lists.sensordata.SensorItemData
import org.supla.android.ui.lists.sensordata.SensorsItemsView
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.LocalizedStringId

data class ContainerGeneralDetailViewState(
  val fluidLevel: Float? = null,
  val fluidLevelString: String = "",
  val containerType: ContainerType = ContainerType.DEFAULT,
  val controlLevels: List<ControlLevel> = emptyList(),
  val scale: Float = 1f,
  val sensors: List<SensorItemData> = emptyList(),
  val issues: List<ChannelIssueItem> = emptyList(),

  val soundOn: Boolean = false
)

interface ContainerGeneralDetailViewScope {
  fun onMuteClick()
}

@Composable
fun ContainerGeneralDetailViewScope.View(
  state: ContainerGeneralDetailViewState,
  showStateDialog: (Int) -> Unit,
  showCaptionChangeDialog: (String, Int, Long) -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Distance.default),
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .verticalScroll(rememberScrollState())
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
      modifier = Modifier
        .height(288.dp)
        .padding(Distance.default)
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
          Text(
            text = stringResource(R.string.container_fill_level),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = state.fluidLevelString,
            style = MaterialTheme.typography.displaySmall
          )
        }

        if (state.soundOn) {
          SuplaButton(
            iconRes = R.drawable.ic_sound_off,
            colors = SuplaButtonDefaults.buttonColors(content = MaterialTheme.colorScheme.primary),
            onClick = { onMuteClick() },
            modifier = Modifier.align(Alignment.BottomCenter)
          )
        }
      }
      ContainerIconView(
        fillLevel = state.fluidLevel,
        controlLevels = state.controlLevels,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        type = state.containerType
      )
    }
    ChannelIssuesView(issues = state.issues)
    SensorsItemsView(
      sensors = state.sensors,
      scale = state.scale,
      onInfoClick = { showStateDialog(it.channelId) },
      onCaptionLongPress = { showCaptionChangeDialog(it.userCaption, it.channelId, it.profileId) }
    )
  }
}

private val emptyScope = object : ContainerGeneralDetailViewScope {
  override fun onMuteClick() {}
}

@Preview(showBackground = true, showSystemUi = true, backgroundColor = BACKGROUND_COLOR)
@Composable
private fun Preview() {
  SuplaTheme {
    emptyScope.View(
      state = ContainerGeneralDetailViewState(
        fluidLevel = 0.6f,
        fluidLevelString = "60%",
        controlLevels = listOf(
          ErrorLevel(0.9f, "90%", ControlLevel.Type.UPPER)
        ),
        sensors = listOf(
          SensorItemData(
            channelId = 123,
            profileId = 123L,
            onlineState = ListOnlineState.ONLINE,
            icon = ImageId(R.drawable.fnc_container_level_sensor_on),
            caption = LocalizedString.WithId(LocalizedStringId.CHANNEL_CAPTION_CONTAINER_LEVEL_SENSOR),
            userCaption = "",
            batteryIcon = IssueIcon.Battery50,
            showChannelStateIcon = true
          ),
          SensorItemData(
            channelId = 123,
            profileId = 123L,
            onlineState = ListOnlineState.OFFLINE,
            icon = ImageId(R.drawable.fnc_container_level_sensor_off),
            caption = LocalizedString.WithId(LocalizedStringId.CHANNEL_CAPTION_CONTAINER_LEVEL_SENSOR),
            userCaption = "",
            batteryIcon = null,
            showChannelStateIcon = false
          )
        )
      ),
      showStateDialog = {},
      showCaptionChangeDialog = { _, _, _ -> }
    )
  }
}

@Preview(showBackground = true, showSystemUi = true, backgroundColor = BACKGROUND_COLOR)
@Composable
private fun Preview_NoLevel() {
  SuplaTheme {
    emptyScope.View(
      state = ContainerGeneralDetailViewState(
        fluidLevel = null,
        fluidLevelString = "---",
        controlLevels = listOf(
          WarningLevel(0.85f, "90%", ControlLevel.Type.UPPER)
        ),
        sensors = listOf(
          SensorItemData(
            channelId = 123,
            profileId = 123L,
            onlineState = ListOnlineState.ONLINE,
            icon = ImageId(R.drawable.fnc_container_level_sensor_on),
            caption = LocalizedString.WithId(LocalizedStringId.CHANNEL_CAPTION_CONTAINER_LEVEL_SENSOR),
            userCaption = "",
            batteryIcon = IssueIcon.Battery50,
            showChannelStateIcon = true
          ),
          SensorItemData(
            channelId = 123,
            profileId = 123L,
            onlineState = ListOnlineState.OFFLINE,
            icon = ImageId(R.drawable.fnc_container_level_sensor_off),
            caption = LocalizedString.WithId(LocalizedStringId.CHANNEL_CAPTION_CONTAINER_LEVEL_SENSOR),
            userCaption = "",
            batteryIcon = null,
            showChannelStateIcon = false
          )
        ),
        soundOn = true
      ),
      showStateDialog = {},
      showCaptionChangeDialog = { _, _, _ -> }
    )
  }
}
