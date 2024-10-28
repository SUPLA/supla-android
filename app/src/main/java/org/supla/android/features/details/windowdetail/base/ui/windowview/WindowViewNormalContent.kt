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

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.windowdetail.base.data.RollerShutterWindowState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.WindowState
import org.supla.android.features.details.windowdetail.base.data.facadeblinds.FacadeBlindWindowState
import org.supla.android.features.details.windowdetail.base.data.verticalblinds.VerticalBlindWindowState
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemAction
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.features.details.windowdetail.base.ui.facadeblinds.SlatTiltSlider
import org.supla.android.features.details.windowdetail.base.ui.facadeblinds.SlatTiltSliderDimens
import org.supla.android.features.details.windowdetail.base.ui.windowview.controls.HoldToMoveVerticalButtons
import org.supla.android.features.details.windowdetail.base.ui.windowview.controls.IssuesView
import org.supla.android.features.details.windowdetail.base.ui.windowview.controls.PressTimeInfo
import org.supla.android.features.details.windowdetail.base.ui.windowview.controls.WindowControlView
import org.supla.android.ui.views.buttons.supla.LeftRightControlButton
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults
import org.supla.android.ui.views.buttons.supla.TOTAL_HEIGHT
import org.supla.android.ui.views.buttons.supla.UpDownControlButton
import org.supla.android.ui.views.buttons.supla.controlbutton.ControlButtonIcon

@Composable
fun WindowViewNormalContent(
  availableWidth: Dp,
  availableHeight: Dp,
  viewState: WindowViewState,
  windowState: WindowState,
  onAction: (ShadingSystemAction) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(
        top = Distance.small,
        end = Distance.default,
        bottom = Distance.default,
        start = Distance.default
      ),
    verticalArrangement = Arrangement.spacedBy(Distance.default),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    val controlsHeight = when (viewState.orientation) {
      ShadingSystemOrientation.HORIZONTAL -> 250.dp
      ShadingSystemOrientation.VERTICAL -> TOTAL_HEIGHT
    }
    val heightForWindow = availableHeight
      .minus(WindowViewTopMenu.getTopHeight(availableHeight))
      .minus(dimensionResource(id = R.dimen.custom_shadow_height))
      .minus(Distance.small)
      .minus(controlsHeight) // UpDownControlButton
      .minus(Distance.default) // Distance between window and buttons
      .minus(Distance.default)
      .let {
        if (windowState is FacadeBlindWindowState) {
          it.minus(Distance.small).minus(SlatTiltSliderDimens.thumbSize)
        } else {
          it
        }
      }
      .let {
        if (viewState.issues.isNotEmpty()) {
          it.minus(dimensionResource(id = R.dimen.channel_warning_image_size).times(viewState.issues.count()))
            .minus(Distance.default) // Distance between buttons and issues
        } else {
          it
        }
      }

    WindowControlView(
      viewState = viewState,
      windowState = windowState,
      onAction = onAction,
      modifier = Modifier
        .width(min(availableWidth, 300.dp))
        .height(min(heightForWindow, 300.dp.div(WindowDimens.RATIO)))
    )

    Box(modifier = Modifier.weight(1f)) {
      when (viewState.orientation) {
        ShadingSystemOrientation.VERTICAL -> VerticalControlButtons(
          viewState = viewState,
          windowState = windowState,
          modifier = Modifier.align(Alignment.Center),
          onAction = onAction
        )

        ShadingSystemOrientation.HORIZONTAL -> HorizontalControlButtons(
          viewState = viewState,
          windowState = windowState,
          modifier = Modifier.align(Alignment.Center),
          onAction = onAction
        )
      }
    }

    if (viewState.issues.isNotEmpty()) {
      IssuesView(issues = viewState.issues)
    }
  }
}

@Composable
private fun VerticalControlButtons(
  viewState: WindowViewState,
  windowState: WindowState,
  modifier: Modifier = Modifier,
  onAction: (ShadingSystemAction) -> Unit
) =
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Absolute.spacedBy(Distance.small)
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(Distance.default),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      HoldToMoveVerticalButtons(viewState.enabled, onAction = onAction)
      Box(modifier = Modifier.height(TOTAL_HEIGHT)) {
        PressTimeInfo(touchTime = viewState.touchTime, modifier = Modifier.align(Alignment.TopCenter))
        StopMoveButton(viewState.enabled, onAction = onAction, modifier = Modifier.align(Alignment.Center))
      }
      PressToMoveVerticalButtons(viewState.enabled, onAction = onAction)
    }
    (windowState as? FacadeBlindWindowState)?.let {
      SlatTiltSlider(
        value = windowState.slatTilt?.value ?: 0f,
        modifier = Modifier.width(240.dp),
        enabled = viewState.enabled && windowState.slatTilt != null,
        slatsTiltDegrees = windowState.slatTiltDegrees ?: 0f,
        onValueChange = { onAction(ShadingSystemAction.TiltTo(it)) },
        onValueChangeFinished = { onAction(ShadingSystemAction.TiltSetTo(it)) }
      )
    }
  }

@Composable
private fun PressToMoveVerticalButtons(enabled: Boolean, onAction: (ShadingSystemAction) -> Unit) =
  UpDownControlButton(
    disabled = !enabled,
    upContent = { OpenControlIcon(textColor = it) },
    downContent = { CloseControlIcon(textColor = it) },
    upEventHandler = { handleEvents(onClick = { onAction(ShadingSystemAction.Open) }) },
    downEventHandler = { handleEvents(onClick = { onAction(ShadingSystemAction.Close) }) }
  )

@Composable
private fun BoxScope.OpenControlIcon(textColor: Color) =
  ControlButtonIcon(iconRes = R.drawable.ic_arrow_open, textColor = textColor, modifier = Modifier.align(Alignment.Center))

@Composable
private fun BoxScope.CloseControlIcon(textColor: Color) =
  ControlButtonIcon(iconRes = R.drawable.ic_arrow_open, textColor = textColor, rotate = 180f, modifier = Modifier.align(Alignment.Center))

@Composable
private fun HorizontalControlButtons(
  viewState: WindowViewState,
  windowState: WindowState,
  modifier: Modifier = Modifier,
  onAction: (ShadingSystemAction) -> Unit
) =
  Column(
    modifier = modifier.width(300.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Absolute.spacedBy(Distance.tiny)
  ) {
    PressTimeInfoHorizontal(touchTime = viewState.touchTime)
    HoldToMoveHorizontalButtons(viewState.enabled, onAction = onAction)
    Spacer(modifier = Modifier.height(8.dp))
    PressToMoveHorizontalButtons(viewState.enabled, onAction = onAction)
    Spacer(modifier = Modifier.height(8.dp))
    (windowState as? VerticalBlindWindowState)?.let {
      SlatTiltSlider(
        value = windowState.slatTilt?.value ?: 0f,
        modifier = Modifier
          .width(300.dp)
          .height(40.dp),
        enabled = viewState.enabled && windowState.slatTilt != null,
        slatsTiltDegrees = windowState.slatTiltDegrees ?: 0f,
        onValueChange = { onAction(ShadingSystemAction.TiltTo(it)) },
        onValueChangeFinished = { onAction(ShadingSystemAction.TiltSetTo(it)) }
      )
    }
  }

@Composable
private fun HoldToMoveHorizontalButtons(enabled: Boolean, modifier: Modifier = Modifier, onAction: (ShadingSystemAction) -> Unit) =
  LeftRightControlButton(
    disabled = !enabled,
    leftContent = { HoldToCloseCurtainControlIcon(textColor = it) },
    rightContent = { HoldToOpenCurtainControlIcon(textColor = it) },
    leftEventHandler = {
      handleEvents(
        onTouchDown = { onAction(ShadingSystemAction.MoveDown) },
        onTouchUp = { onAction(ShadingSystemAction.Stop) }
      )
    },
    rightEventHandler = {
      handleEvents(
        onTouchDown = { onAction(ShadingSystemAction.MoveUp) },
        onTouchUp = { onAction(ShadingSystemAction.Stop) }
      )
    },
    modifier = modifier
  )

@Composable
private fun BoxScope.HoldToCloseCurtainControlIcon(textColor: Color) =
  Row(modifier = Modifier.align(Alignment.Center)) {
    ControlButtonIcon(iconRes = R.drawable.ic_arrow_right, textColor = textColor)
    ControlButtonIcon(iconRes = R.drawable.ic_arrow_right, textColor = textColor, rotate = 180f)
  }

@Composable
private fun BoxScope.HoldToOpenCurtainControlIcon(textColor: Color) =
  Row(modifier = Modifier.align(Alignment.Center)) {
    ControlButtonIcon(iconRes = R.drawable.ic_arrow_right, textColor = textColor, rotate = 180f)
    ControlButtonIcon(iconRes = R.drawable.ic_arrow_right, textColor = textColor)
  }

@Composable
private fun PressToMoveHorizontalButtons(enabled: Boolean, modifier: Modifier = Modifier, onAction: (ShadingSystemAction) -> Unit) =
  LeftRightControlButton(
    disabled = !enabled,
    leftContent = { ClickToCloseCurtainControlIcon(textColor = it) },
    rightContent = { ClickToOpenCurtainControlIcon(textColor = it) },
    middleContent = { ControlButtonIcon(iconRes = R.drawable.ic_stop, textColor = it, modifier = Modifier.align(Alignment.Center)) },
    leftEventHandler = { handleEvents(onClick = { onAction(ShadingSystemAction.Close) }) },
    rightEventHandler = { handleEvents(onClick = { onAction(ShadingSystemAction.Open) }) },
    middleEventHandler = { handleEvents(onClick = { onAction(ShadingSystemAction.Stop) }) },
    modifier = modifier
  )

@Composable
private fun BoxScope.ClickToCloseCurtainControlIcon(textColor: Color) =
  Row(modifier = Modifier.align(Alignment.Center)) {
    ControlButtonIcon(iconRes = R.drawable.ic_arrow_close, textColor = textColor, rotate = 270f)
    ControlButtonIcon(iconRes = R.drawable.ic_arrow_close, textColor = textColor, rotate = 90f)
  }

@Composable
private fun BoxScope.ClickToOpenCurtainControlIcon(textColor: Color) =
  Row(modifier = Modifier.align(Alignment.Center)) {
    ControlButtonIcon(iconRes = R.drawable.ic_arrow_close, textColor = textColor, rotate = 90f)
    ControlButtonIcon(iconRes = R.drawable.ic_arrow_close, textColor = textColor, rotate = 270f)
  }

@Composable
private fun StopMoveButton(enabled: Boolean, modifier: Modifier = Modifier, onAction: (ShadingSystemAction) -> Unit) =
  SuplaButton(
    iconRes = R.drawable.ic_stop,
    onClick = { onAction(ShadingSystemAction.Stop) },
    disabled = !enabled,
    shape = SuplaButtonDefaults.allRoundedShape(radius = 32.dp),
    modifier = modifier
  )

@Composable
@SuppressLint("DefaultLocale")
fun PressTimeInfoHorizontal(touchTime: Float?) {
  if (touchTime != null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(26.dp)) {
      Icon(painter = painterResource(id = R.drawable.ic_touch_hand), contentDescription = null)
      Text(text = String.format("%.1fs", touchTime), style = MaterialTheme.typography.bodyMedium)
    }
  } else {
    Box(modifier = Modifier.height(26.dp))
  }
}

@Preview
@Composable
private fun Preview_Vertical() {
  SuplaTheme {
    Box(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .height(500.dp)
        .width(350.dp)
    ) {
      WindowViewNormalContent(
        availableWidth = 350.dp,
        availableHeight = 500.dp,
        viewState = WindowViewState(enabled = true),
        windowState = RollerShutterWindowState(position = WindowGroupedValue.Similar(10f)),
        onAction = { }
      )
    }
  }
}

@Preview
@Composable
private fun Preview_Horizontal() {
  SuplaTheme {
    Box(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .height(500.dp)
        .width(350.dp)
    ) {
      WindowViewNormalContent(
        availableWidth = 350.dp,
        availableHeight = 500.dp,
        viewState = WindowViewState(enabled = true, orientation = ShadingSystemOrientation.HORIZONTAL, touchTime = 12.3f),
        windowState = VerticalBlindWindowState(position = WindowGroupedValue.Similar(10f)),
        onAction = { },
      )
    }
  }
}
