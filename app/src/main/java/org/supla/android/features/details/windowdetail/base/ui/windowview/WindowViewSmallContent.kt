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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.windowdetail.base.data.RollerShutterWindowState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.WindowState
import org.supla.android.features.details.windowdetail.base.data.facadeblinds.FacadeBlindWindowState
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemAction
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.features.details.windowdetail.base.ui.facadeblinds.SlatTiltSlider
import org.supla.android.features.details.windowdetail.base.ui.windowview.controls.HoldToMoveVerticalButtons
import org.supla.android.features.details.windowdetail.base.ui.windowview.controls.IssuesView
import org.supla.android.features.details.windowdetail.base.ui.windowview.controls.PressTimeInfo
import org.supla.android.features.details.windowdetail.base.ui.windowview.controls.WindowControlView
import org.supla.android.ui.views.buttons.supla.DownControlIcon
import org.supla.android.ui.views.buttons.supla.StopControlIcon
import org.supla.android.ui.views.buttons.supla.UpControlIcon
import org.supla.android.ui.views.buttons.supla.UpDownControlButton
import org.supla.android.ui.views.buttons.supla.controlbutton.ControlButtonIcon

@Composable
fun WindowViewSmallContent(
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
    verticalArrangement = Arrangement.Absolute.spacedBy(4.dp)
  ) {
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(modifier = Modifier.fillMaxHeight()) {
        PressTimeInfo(touchTime = viewState.touchTime, modifier = Modifier.align(Alignment.TopCenter))
        when (viewState.orientation) {
          ShadingSystemOrientation.HORIZONTAL ->
            HorizontalControlButtons(viewState.enabled, onAction = onAction, modifier = Modifier.align(Alignment.Center))

          ShadingSystemOrientation.VERTICAL ->
            HoldToMoveVerticalButtons(viewState.enabled, onAction = onAction, modifier = Modifier.align(Alignment.Center))
        }
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        WindowControlView(
          viewState = viewState,
          windowState = windowState,
          onAction = onAction,
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        )
        (windowState as? FacadeBlindWindowState)?.let {
          SlatTiltSlider(
            value = windowState.slatTilt?.value ?: 0f,
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.enabled && windowState.slatTilt != null,
            slatsTiltDegrees = windowState.slatTiltDegrees ?: 0f,
            onValueChange = { onAction(ShadingSystemAction.TiltTo(it)) },
            onValueChangeFinished = { onAction(ShadingSystemAction.TiltSetTo(it)) }
          )
        }
      }

      Column(
        verticalArrangement = Arrangement.spacedBy(Distance.small)
      ) {
        when (viewState.orientation) {
          ShadingSystemOrientation.HORIZONTAL ->
            HorizontalPressToMoveButtons(enabled = viewState.enabled, onAction = onAction)

          ShadingSystemOrientation.VERTICAL ->
            VerticalPressToMoveButtons(enabled = viewState.enabled, onAction = onAction)
        }
      }
    }

    if (viewState.issues.isNotEmpty()) {
      IssuesView(issues = viewState.issues)
    }
  }
}

@Composable
private fun HorizontalControlButtons(enabled: Boolean, modifier: Modifier = Modifier, onAction: (ShadingSystemAction) -> Unit) =
  UpDownControlButton(
    disabled = !enabled,
    upContent = {
      Row(modifier = Modifier.align(Alignment.Center)) {
        ControlButtonIcon(iconRes = R.drawable.ic_arrow_right, rotate = 180f)
        ControlButtonIcon(iconRes = R.drawable.ic_arrow_right)
      }
    },
    downContent = {
      Row(modifier = Modifier.align(Alignment.Center)) {
        ControlButtonIcon(iconRes = R.drawable.ic_arrow_right)
        ControlButtonIcon(iconRes = R.drawable.ic_arrow_right, rotate = 180f)
      }
    },
    upEventHandler = {
      handleEvents(
        onTouchDown = { onAction(ShadingSystemAction.MoveUp) },
        onTouchUp = { onAction(ShadingSystemAction.Stop) }
      )
    },
    downEventHandler = {
      handleEvents(
        onTouchDown = { onAction(ShadingSystemAction.MoveDown) },
        onTouchUp = { onAction(ShadingSystemAction.Stop) }
      )
    },
    modifier = modifier
  )

@Composable
private fun HorizontalPressToMoveButtons(enabled: Boolean, modifier: Modifier = Modifier, onAction: (ShadingSystemAction) -> Unit) =
  UpDownControlButton(
    disabled = !enabled,
    upContent = {
      Row(modifier = Modifier.align(Alignment.Center)) {
        ControlButtonIcon(iconRes = R.drawable.ic_arrow_right, rotate = 180f)
        ControlButtonIcon(iconRes = R.drawable.ic_arrow_right)
      }
    },
    downContent = {
      Row(modifier = Modifier.align(Alignment.Center)) {
        ControlButtonIcon(iconRes = R.drawable.ic_arrow_right)
        ControlButtonIcon(iconRes = R.drawable.ic_arrow_right, rotate = 180f)
      }
    },
    middleContent = { StopControlIcon(textColor = it) },
    upEventHandler = {
      handleEvents(onClick = { onAction(ShadingSystemAction.MoveUp) })
    },
    downEventHandler = {
      handleEvents(onClick = { onAction(ShadingSystemAction.MoveDown) })
    },
    middleEventHandler = {
      handleEvents(onClick = { onAction(ShadingSystemAction.Stop) })
    },
    modifier = modifier
  )

@Composable
private fun VerticalPressToMoveButtons(enabled: Boolean, modifier: Modifier = Modifier, onAction: (ShadingSystemAction) -> Unit) =
  UpDownControlButton(
    disabled = !enabled,
    upContent = { UpControlIcon(textColor = it) },
    downContent = { DownControlIcon(textColor = it) },
    middleContent = { StopControlIcon(textColor = it) },
    upEventHandler = {
      handleEvents(onClick = { onAction(ShadingSystemAction.MoveUp) })
    },
    downEventHandler = {
      handleEvents(onClick = { onAction(ShadingSystemAction.MoveDown) })
    },
    middleEventHandler = {
      handleEvents(onClick = { onAction(ShadingSystemAction.Stop) })
    },
    modifier = modifier
  )

@Preview
@Composable
private fun Preview_Vertical() {
  SuplaTheme {
    Box(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .height(350.dp)
        .width(350.dp)
    ) {
      WindowViewSmallContent(
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
        .height(350.dp)
        .width(350.dp)
    ) {
      WindowViewSmallContent(
        viewState = WindowViewState(enabled = true, orientation = ShadingSystemOrientation.HORIZONTAL),
        windowState = RollerShutterWindowState(position = WindowGroupedValue.Similar(10f)),
        onAction = { }
      )
    }
  }
}

@Preview
@Composable
private fun Preview_FacadeBlind() {
  SuplaTheme {
    Box(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .height(300.dp)
        .width(400.dp)
    ) {
      WindowViewSmallContent(
        viewState = WindowViewState(enabled = true, orientation = ShadingSystemOrientation.HORIZONTAL),
        windowState = FacadeBlindWindowState(position = WindowGroupedValue.Similar(10f)),
        onAction = { }
      )
    }
  }
}
