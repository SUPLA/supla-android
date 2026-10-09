package org.supla.android.features.details.relayschedule.ui
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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.relayschedule.OperatingMode
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.extensions.ifTrue
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults

interface OperatingButtonsScope {
  fun onManual()
  fun onWeekly()
  fun onAuto()
}

enum class OperatingButtonsStyle {
  TEXT,
  ICONS
}

@Composable
fun OperatingButtonsScope.OperatingButtons(
  operatingMode: OperatingMode,
  manualButtonDisabled: Boolean = false,
  weeklyButtonDisabled: Boolean = false,
  autoButtonDisabled: Boolean = false,
  style: OperatingButtonsStyle = OperatingButtonsStyle.TEXT
) {
  OperatingButtons(
    operatingMode = operatingMode,
    modifier = Modifier.padding(horizontal = Distance.horizontal).padding(top = Distance.vertical),
    manualButtonDisabled = manualButtonDisabled,
    weeklyButtonDisabled = weeklyButtonDisabled,
    autoButtonDisabled = autoButtonDisabled,
    style = style
  )
}

@Composable
fun OperatingButtonsScope.OperatingButtons(
  operatingMode: OperatingMode,
  modifier: Modifier,
  manualButtonDisabled: Boolean = false,
  weeklyButtonDisabled: Boolean = false,
  autoButtonDisabled: Boolean = false,
  style: OperatingButtonsStyle = OperatingButtonsStyle.TEXT
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(Distance.small)
  ) {
    operatingMode.manualAllowed.ifTrue {
      OperatingButton(
        style = style,
        text = stringResource(R.string.thermostat_detail_mode_manual),
        iconRes = R.drawable.ic_manual,
        pressed = operatingMode.manualActive,
        disabled = manualButtonDisabled,
        onClick = { onManual() }
      )
    }

    operatingMode.weeklyAllowed.ifTrue {
      OperatingButton(
        style = style,
        text = stringResource(R.string.thermostat_detail_mode_weekly_schedule),
        iconRes = R.drawable.ic_schedule,
        pressed = operatingMode.weeklyActive,
        disabled = weeklyButtonDisabled,
        onClick = { onWeekly() }
      )
    }

    operatingMode.autoAllowed.ifTrue {
      OperatingButton(
        style = style,
        text = if (style == OperatingButtonsStyle.ICONS) "A" else stringResource(R.string.auto),
        pressed = operatingMode.autoActive,
        disabled = autoButtonDisabled,
        onClick = { onAuto() }
      )
    }
  }
}

@Composable
private fun RowScope.OperatingButton(
  style: OperatingButtonsStyle,
  text: String,
  @DrawableRes iconRes: Int? = null,
  pressed: Boolean,
  disabled: Boolean,
  onClick: () -> Unit
) {
  if (style == OperatingButtonsStyle.ICONS && iconRes != null) {
    SuplaButton(
      iconRes = iconRes,
      modifier = Modifier,
      colors = SuplaButtonDefaults.primaryColors(),
      pressed = pressed,
      disabled = disabled,
      onClick = onClick
    )
  } else {
    SuplaButton(
      text = text,
      modifier = if (style == OperatingButtonsStyle.ICONS) Modifier else Modifier.weight(1f),
      shape = SuplaButtonDefaults.allRoundedShape(),
      colors = SuplaButtonDefaults.primaryColors(),
      pressed = pressed,
      disabled = disabled,
      onClick = onClick
    )
  }
}

private val previewScope = object : OperatingButtonsScope {
  override fun onManual() {}
  override fun onWeekly() {}
  override fun onAuto() {}
}

private val previewOperatingMode = OperatingMode(
  manualAllowed = true,
  weeklyAllowed = true,
  autoAllowed = true,
  manualActive = true
)

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(modifier = Modifier.fillMaxSize()) {
      previewScope.OperatingButtons(
        operatingMode = previewOperatingMode,
        style = OperatingButtonsStyle.TEXT
      )
      previewScope.OperatingButtons(
        operatingMode = previewOperatingMode,
        style = OperatingButtonsStyle.ICONS
      )
    }
  }
}
