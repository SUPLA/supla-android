package org.supla.android.features.details.rgbanddimmer.common.ui.dimmer
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import org.supla.android.R
import org.supla.android.core.ui.LocalSizeClassProvider
import org.supla.android.core.ui.SizeClass
import org.supla.android.core.ui.SizeClassBox
import org.supla.android.core.ui.isSmall
import org.supla.android.core.ui.padding
import org.supla.android.core.ui.theme.Distance
import org.supla.android.features.details.rgbanddimmer.common.SavedColor
import org.supla.android.features.details.rgbanddimmer.common.dimmer.DimmerDetailScope
import org.supla.android.features.details.rgbanddimmer.common.dimmer.DimmerDetailViewState
import org.supla.android.features.details.rgbanddimmer.common.dimmer.DimmerValue
import org.supla.android.features.details.rgbanddimmer.common.ui.SavedColorListScope
import org.supla.android.features.details.rgbanddimmer.common.ui.SavedColors
import org.supla.android.features.details.rgbanddimmer.common.ui.ValuesCard
import org.supla.android.ui.extensions.ifFalse
import org.supla.android.ui.lists.channelissues.ChannelIssuesView
import org.supla.android.ui.views.DeviceState
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.buttons.SwitchButtons
import org.supla.android.ui.views.buttons.SwitchIconButton
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults

@Composable
fun DimmerDetailScope.Scaffold(
  state: DimmerDetailViewState,
  brightnessControl: @Composable DimmerDetailScope.(DimmerDetailViewState, Modifier) -> Unit,
  savedColorItemContent: @Composable SavedColorListScope.(SavedColor, Boolean) -> Unit,
  brightnessBox: @Composable DimmerDetailScope.(DimmerValue, Modifier) -> Unit
) {
  SizeClassBox(Modifier.fillMaxSize()) {
    when (it) {
      SizeClass.LANDSCAPE_SMALL -> LandscapeNarrow(state, brightnessBox, brightnessControl, savedColorItemContent)

      SizeClass.SQUARE_SMALL,
      SizeClass.SQUARE_MEDIUM,
      SizeClass.SQUARE_BIG,
      SizeClass.PORTRAIT_SMALL,
      SizeClass.PORTRAIT_MEDIUM,
      SizeClass.PORTRAIT_BIG -> Portrait(state, brightnessBox, brightnessControl, savedColorItemContent)

      SizeClass.LANDSCAPE_MEDIUM,
      SizeClass.LANDSCAPE_BIG -> Landscape(state, brightnessBox, brightnessControl, savedColorItemContent)
    }

    if (state.loading) {
      LoadingScrim()
    }
  }
}

@Composable
private fun DimmerDetailScope.LandscapeNarrow(
  state: DimmerDetailViewState,
  brightnessBox: @Composable DimmerDetailScope.(DimmerValue, Modifier) -> Unit,
  brightnessControl: @Composable DimmerDetailScope.(DimmerDetailViewState, Modifier) -> Unit,
  savedColorItemContent: @Composable SavedColorListScope.(SavedColor, Boolean) -> Unit
) {
  Column {
    brightnessBox(state.value, Modifier.padding(horizontal = Distance.horizontal, vertical = Distance.small))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(horizontal = Distance.horizontal)
    ) {
      brightnessControl(state, Modifier.align(Alignment.Center))

      Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
          .fillMaxHeight()
          .align(Alignment.CenterEnd)
      ) {
        BrightnessSelectorTypeButton(state)
        state.offButtonState?.let {
          SwitchIconButton(
            state = it,
            disabled = state.offline || state.loading,
            colors = SuplaButtonDefaults.errorColors(),
            onClick = { turnOff() }
          )
        }
        state.onButtonState?.let {
          SwitchIconButton(
            state = it,
            disabled = state.offline || state.loading,
            colors = SuplaButtonDefaults.primaryColors(),
            onClick = { turnOn() }
          )
        }
      }
    }

    SavedColors(
      savedColors = state.savedColors,
      online = !state.offline,
      modifier = Modifier.padding(horizontal = Distance.horizontal, vertical = Distance.small),
      itemContent = savedColorItemContent
    )
  }
}

@Composable
private fun DimmerDetailScope.Landscape(
  state: DimmerDetailViewState,
  brightnessBox: @Composable DimmerDetailScope.(DimmerValue, Modifier) -> Unit,
  brightnessControl: @Composable DimmerDetailScope.(DimmerDetailViewState, Modifier) -> Unit,
  savedColorItemContent: @Composable SavedColorListScope.(SavedColor, Boolean) -> Unit
) {
  Row {
    Column(
      modifier = Modifier.weight(1f)
    ) {
      state.deviceStateData?.let { DeviceState(data = it) }
      state.channelIssues?.let { issues -> ChannelIssuesView(issues) }

      brightnessBox(state.value, Modifier)

      Spacer(Modifier.weight(1f))

      SavedColors(
        savedColors = state.savedColors,
        online = !state.offline,
        modifier = Modifier.padding(horizontal = Distance.horizontal),
        itemContent = savedColorItemContent
      )

      SwitchButtons(
        leftButton = state.offButtonState,
        rightButton = state.onButtonState,
        disabled = state.offline || state.loading,
        leftButtonClick = { turnOff() },
        rightButtonClick = { turnOn() },
      )
    }
    Column(
      modifier = Modifier.weight(1f),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      BrightnessSelectorBox(
        state = state,
        modifier = Modifier
          .padding(horizontal = Distance.horizontal, vertical = Distance.vertical)
          .fillMaxSize(),
        brightnessControl = brightnessControl
      )
    }
  }
}

@Composable
private fun DimmerDetailScope.Portrait(
  state: DimmerDetailViewState,
  brightnessBox: @Composable DimmerDetailScope.(DimmerValue, Modifier) -> Unit,
  brightnessControl: @Composable DimmerDetailScope.(DimmerDetailViewState, Modifier) -> Unit,
  savedColorItemContent: @Composable SavedColorListScope.(SavedColor, Boolean) -> Unit
) {
  val sizeClass = LocalSizeClassProvider.current

  Column {
    state.deviceStateData?.let { DeviceState(data = it, modifier = Modifier.padding(vertical = sizeClass.padding)) }
    sizeClass.isSmall.ifFalse {
      state.channelIssues?.let { issues -> ChannelIssuesView(issues) }
    }

    brightnessBox(state.value, Modifier.padding(horizontal = Distance.horizontal))

    BrightnessSelectorBox(
      state = state,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = Distance.horizontal, vertical = sizeClass.padding)
        .weight(1f)
        .align(Alignment.CenterHorizontally),
      brightnessControl = brightnessControl
    )

    SavedColors(
      savedColors = state.savedColors,
      online = !state.offline,
      modifier = Modifier.padding(horizontal = Distance.horizontal),
      itemContent = savedColorItemContent
    )

    SwitchButtons(
      leftButton = state.offButtonState,
      rightButton = state.onButtonState,
      disabled = state.offline || state.loading,
      leftButtonClick = { turnOff() },
      rightButtonClick = { turnOn() },
      modifier = Modifier.padding(horizontal = Distance.horizontal, vertical = sizeClass.padding)
    )
  }
}

@Composable
private fun DimmerDetailScope.BrightnessSelectorBox(
  state: DimmerDetailViewState,
  modifier: Modifier = Modifier,
  brightnessControl: @Composable DimmerDetailScope.(DimmerDetailViewState, Modifier) -> Unit
) {
  Box(
    modifier = modifier
  ) {
    brightnessControl(state, Modifier.align(Alignment.Center))
    BrightnessSelectorTypeButton(state, modifier = Modifier.align(Alignment.TopEnd))
  }
}

@Composable
private fun DimmerDetailScope.BrightnessSelectorTypeButton(state: DimmerDetailViewState, modifier: Modifier = Modifier) =
  SuplaButton(
    onClick = { toggleSelectorType() },
    modifier = modifier
  ) {
    Image(
      painter = painterResource(id = state.selectorType.swapIconRes),
      contentDescription = null,
      alignment = Alignment.Center,
      modifier = Modifier
        .size(dimensionResource(id = R.dimen.icon_default_size))
        .align(Alignment.Center),
    )
  }
