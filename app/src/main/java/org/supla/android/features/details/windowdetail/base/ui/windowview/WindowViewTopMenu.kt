package org.supla.android.features.details.windowdetail.base.ui.windowview
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.gray
import org.supla.android.features.details.windowdetail.base.data.WindowState
import org.supla.android.features.details.windowdetail.base.data.facadeblinds.FacadeBlindWindowState
import org.supla.android.features.details.windowdetail.base.data.verticalblinds.VerticalBlindWindowState
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemAction
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemPositionPresentation
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.ui.views.buttons.SuplaButton
import org.supla.android.ui.views.tools.Shadow
import org.supla.android.ui.views.tools.ShadowOrientation

private val TOP_MENU_HEIGHT_SMALL_SCREEN = 64.dp
private val TOP_MENU_HEIGHT_NORMAL_SCREEN = 80.dp

@Composable
fun WindowViewTopMenu(
  viewState: WindowViewState,
  windowState: WindowState,
  height: Dp = 80.dp,
  onAction: (ShadingSystemAction) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .background(color = MaterialTheme.colors.surface)
      .height(height)
      .padding(
        start = dimensionResource(id = R.dimen.distance_default),
        end = dimensionResource(id = R.dimen.distance_default)
      ),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
  ) {
    if (viewState.enabled.not()) {
      Spacer(modifier = Modifier.weight(1f))
      Icon(painter = painterResource(id = R.drawable.ic_offline), contentDescription = null, tint = MaterialTheme.colors.gray)
      TopTextLabelView(text = stringResource(id = R.string.offline))
      Spacer(modifier = Modifier.weight(1f))
    } else {
      if (viewState.calibrating) {
        CircularProgressIndicator(modifier = Modifier.size(dimensionResource(id = R.dimen.icon_default_size)))
        TopTextLabelView(text = stringResource(id = R.string.roller_shutter_calibrating))
      } else if (!viewState.positionUnknown) {
        Column(verticalArrangement = Arrangement.Absolute.spacedBy(Distance.tiny)) {
          OpenClosePercentageLabel(viewState.positionPresentation, windowState.positionText())
          (windowState as? FacadeBlindWindowState)?.let {
            SlatsTiltLabel(value = windowState.slatTiltText()(LocalContext.current))
          }
          (windowState as? VerticalBlindWindowState)?.let {
            SlatsTiltLabel(value = windowState.slatTiltText()(LocalContext.current))
          }
        }
      } else if (viewState.isGroup) {
        OpenClosePercentageLabel(viewState.positionPresentation, "---")
      } else {
        TopTextLabelView(text = stringResource(id = R.string.roller_shutter_calibration_needed))
      }

      Spacer(modifier = Modifier.weight(1f))

      if (viewState.calibrationPossible) {
        SuplaButton(
          iconRes = R.drawable.ic_recalibrate,
          onClick = { onAction(ShadingSystemAction.Calibrate) },
          radius = 24.dp
        )
      } else if (viewState.onlineStatusString != null) {
        TopTextLabelView(text = "ONLINE:")
        PercentageValueView(viewState.onlineStatusString)
      }
    }
  }
  Shadow(orientation = ShadowOrientation.STARTING_TOP)
}

object WindowViewTopMenu {
  fun getTopHeight(availableHeight: Dp) =
    if (isSmallScreen(availableHeight)) {
      TOP_MENU_HEIGHT_SMALL_SCREEN
    } else {
      TOP_MENU_HEIGHT_NORMAL_SCREEN
    }

  fun isSmallScreen(availableHeight: Dp) = availableHeight < 480.dp
}

@Composable
private fun OpenClosePercentageLabel(positionPresentation: ShadingSystemPositionPresentation, value: String) {
  Row(horizontalArrangement = Arrangement.spacedBy(Distance.tiny)) {
    TopTextLabelView(text = stringResource(id = positionPresentation.stringRes))
    PercentageValueView(value)
  }
}

@Composable
private fun SlatsTiltLabel(value: String) {
  Row(horizontalArrangement = Arrangement.spacedBy(Distance.tiny)) {
    TopTextLabelView(text = stringResource(id = R.string.facade_blind_slat_tilt))
    PercentageValueView(value)
  }
}

@Composable
private fun PercentageValueView(value: String) =
  Text(
    text = value,
    style = MaterialTheme.typography.body2,
    color = MaterialTheme.colors.onSurface,
    fontWeight = FontWeight.Bold
  )

@Composable
private fun TopTextLabelView(text: String) =
  Text(
    text = text.uppercase(),
    style = MaterialTheme.typography.body2,
    color = MaterialTheme.colors.gray
  )
