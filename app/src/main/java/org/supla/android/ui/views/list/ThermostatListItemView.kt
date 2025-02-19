package org.supla.android.ui.views.list
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

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.preferences
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.layouts.BaseSlideableContent
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.ui.lists.data.default
import org.supla.android.ui.views.list.components.ListItemIcon
import org.supla.android.ui.views.list.components.ListItemMainRow
import org.supla.android.ui.views.list.components.ListItemValue
import org.supla.android.ui.views.list.components.SetpointTemperature
import org.supla.android.usecases.list.CreateListItemUpdateEventDataUseCase
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.infrastructure.LocalizedString
import javax.inject.Inject

@AndroidEntryPoint
class ThermostatListItemView : BaseSlideableContent<SlideableListItemData.Thermostat> {

  constructor(context: Context) : super(context, null, 0)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0)

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  @Inject
  override lateinit var createListItemUpdateEventDataUseCase: CreateListItemUpdateEventDataUseCase

  @Inject
  override lateinit var schedulers: SuplaSchedulers

  @Composable
  override fun Content() {
    val data = this.data ?: SlideableListItemData.Thermostat.default()

    SuplaTheme {
      ThermostatListItemView(
        data = data,
        hasLeftButton = hasLeftButton,
        hasRightButton = hasRightButton,
        onInfoClick = onInfoClick,
        onIssueClick = onIssueClick,
        onTitleLongClick = onTitleLongClick,
        onItemClick = onItemClick
      )
    }
  }
}

@Composable
fun ThermostatListItemView(
  data: SlideableListItemData.Thermostat,
  hasLeftButton: Boolean = false,
  hasRightButton: Boolean = false,
  showInfoIcon: Boolean = LocalContext.current.preferences.isShowChannelInfo,
  scale: Float = LocalContext.current.preferences.scale,
  onInfoClick: () -> Unit = { },
  onIssueClick: (ListItemIssues) -> Unit = { },
  onItemClick: () -> Unit = { },
  onTitleLongClick: () -> Unit = { }
) {
  ListItemScaffold(
    itemTitle = data.title(LocalContext.current),
    itemOnlineState = data.onlineState,
    itemEstimatedEndDate = data.estimatedTimerEndDate,
    hasLeftButton = hasLeftButton,
    hasRightButton = hasRightButton,
    onInfoClick = onInfoClick,
    onTitleLongClick = onTitleLongClick,
    showInfoIcon = showInfoIcon && data.infoSupported,
    issues = data.issues,
    onIssueClick = onIssueClick,
    onItemClick = onItemClick,
    scale = scale
  ) {
    ListItemMainRow(scale = scale) {
      data.icon?.let {
        ListItemIcon(imageId = it, scale = scale)
      }

      if (scale <= 1f) {
        ListItemValue(value = data.value, scale = scale)
        SetpointTemperature(indicatorIcon = data.indicatorIcon, subValue = data.subValue, scale = scale)
      } else {
        Column {
          ListItemValue(value = data.value, scale = scale)
          SetpointTemperature(indicatorIcon = data.indicatorIcon, subValue = data.subValue, scale = scale)
        }
      }
    }
  }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
  SuplaTheme {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
      Column(
        modifier = Modifier
          .width(600.dp)
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(1.5f))
      ) {
        ThermostatListItemView(
          data = SlideableListItemData.Thermostat(
            onlineState = ListOnlineState.ONLINE,
            title = LocalizedString.Constant("Thermostat"),
            icon = ImageId(R.drawable.fnc_thermostat_cool),
            value = "20,7°C",
            subValue = "21,0°",
            indicatorIcon = R.drawable.ic_cooling,
            issues = ListItemIssues(IssueIcon.Warning),
            estimatedTimerEndDate = null,
            infoSupported = true
          ),
          true,
          showInfoIcon = true,
          scale = 1.5f
        )
      }
      Column(
        modifier = Modifier
          .width(600.dp)
          .height(dimensionResource(id = R.dimen.channel_layout_height))
      ) {
        ThermostatListItemView(
          data = SlideableListItemData.Thermostat(
            onlineState = ListOnlineState.PARTIALLY_ONLINE,
            title = LocalizedString.Constant("Thermostat"),
            icon = ImageId(R.drawable.fnc_thermostat_cool),
            value = "20,7°C",
            subValue = "21,0°",
            indicatorIcon = R.drawable.ic_cooling,
            issues = ListItemIssues(IssueIcon.Error),
            estimatedTimerEndDate = null,
            infoSupported = true
          ),
          false,
          showInfoIcon = true,
          scale = 1.0f
        )
      }
      Column(
        modifier = Modifier
          .width(600.dp)
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(0.6f))
      ) {
        ThermostatListItemView(
          data = SlideableListItemData.Thermostat(
            onlineState = ListOnlineState.ONLINE,
            title = LocalizedString.Constant("Thermostat"),
            icon = ImageId(R.drawable.fnc_thermostat_cool),
            value = "20,7°C",
            subValue = "21,0°",
            indicatorIcon = R.drawable.ic_cooling,
            issues = ListItemIssues.empty,
            estimatedTimerEndDate = null,
            infoSupported = true
          ),
          true,
          showInfoIcon = true,
          scale = 0.6f
        )
      }
      Column(
        modifier = Modifier
          .width(600.dp)
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(0.6f))
      ) {
        ThermostatListItemView(
          data = SlideableListItemData.Thermostat(
            onlineState = ListOnlineState.ONLINE,
            title = LocalizedString.Constant("Thermostat with very long name which goes out of the screen and must be cut"),
            icon = ImageId(R.drawable.fnc_thermostat_cool),
            value = "20,7°C",
            subValue = "21,0°",
            indicatorIcon = R.drawable.ic_cooling,
            issues = ListItemIssues(IssueIcon.Warning),
            estimatedTimerEndDate = null,
            infoSupported = true
          ),
          false,
          showInfoIcon = true,
          scale = 0.6f
        )
      }
    }
  }
}

@Preview(device = Devices.NEXUS_5)
@Preview(device = Devices.NEXUS_5, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_Narrow() {
  SuplaTheme {
    Column(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .width(350.dp)
    ) {
      Column(
        modifier = Modifier
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(1.5f))
      ) {
        ThermostatListItemView(
          data = SlideableListItemData.Thermostat(
            onlineState = ListOnlineState.ONLINE,
            title = LocalizedString.Constant("Thermostat"),
            icon = ImageId(R.drawable.fnc_thermostat_cool),
            value = "20,7°C",
            subValue = "21,0°",
            indicatorIcon = R.drawable.ic_cooling,
            issues = ListItemIssues(IssueIcon.Warning),
            estimatedTimerEndDate = null,
            infoSupported = true
          ),
          true,
          showInfoIcon = true,
          scale = 1.5f
        )
      }

      Column(
        modifier = Modifier
          .height(dimensionResource(id = R.dimen.channel_layout_height))
      ) {
        ThermostatListItemView(
          data = SlideableListItemData.Thermostat(
            onlineState = ListOnlineState.ONLINE,
            title = LocalizedString.Constant("Thermostat"),
            icon = ImageId(R.drawable.fnc_thermostat_cool),
            value = "20,7°C",
            subValue = "21,0°",
            indicatorIcon = R.drawable.ic_cooling,
            issues = ListItemIssues(IssueIcon.Error),
            estimatedTimerEndDate = null,
            infoSupported = true
          ),
          false,
          showInfoIcon = true,
          scale = 1.0f
        )
      }
    }
  }
}
