package org.supla.android.features.details.windowdetail.base.ui
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.general.ChannelIssueItem
import org.supla.android.features.details.windowdetail.base.data.RollerShutterWindowState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.WindowState
import org.supla.android.features.details.windowdetail.base.ui.windowview.ShadingSystemOrientation
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowViewNormalContent
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowViewSmallContent
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowViewTopMenu
import org.supla.android.ui.lists.data.IssueIconType

sealed interface ShadingSystemAction {
  object Open : ShadingSystemAction
  object Close : ShadingSystemAction
  object Stop : ShadingSystemAction
  object MoveUp : ShadingSystemAction
  object MoveDown : ShadingSystemAction
  object Calibrate : ShadingSystemAction

  data class OpenAt(val position: Float) : ShadingSystemAction
  data class MoveTo(val position: Float) : ShadingSystemAction
  data class TiltTo(val tilt: Float) : ShadingSystemAction
  data class TiltSetTo(val tilt: Float) : ShadingSystemAction
  data class MoveAndTiltTo(val position: Float, val tilt: Float) : ShadingSystemAction
  data class MoveAndTiltSetTo(val position: Float, val tilt: Float) : ShadingSystemAction
}

data class WindowViewState(
  val issues: List<ChannelIssueItem> = emptyList(),
  val enabled: Boolean = false,
  val showClosingPercentage: Boolean = false,
  val calibrating: Boolean = false,
  val positionUnknown: Boolean = false,
  val calibrationPossible: Boolean = false,
  val touchTime: Float? = null,
  val isGroup: Boolean = false,
  val onlineStatusString: String? = null,
  val orientation: ShadingSystemOrientation = ShadingSystemOrientation.VERTICAL
)

@Composable
fun WindowView(
  windowState: WindowState,
  viewState: WindowViewState,
  onAction: (ShadingSystemAction) -> Unit
) {
  BoxWithConstraints {
    val height = maxHeight
    val width = maxWidth
    Column(
      modifier = Modifier
        .fillMaxSize()
    ) {
      WindowViewTopMenu(
        viewState = viewState,
        windowState = windowState,
        height = WindowViewTopMenu.getTopHeight(height),
        onAction = onAction
      )

      RollerShutterScreen(
        availableHeight = height,
        availableWidth = width,
        windowState = windowState,
        viewState = viewState,
        onAction = onAction
      )
    }
  }
}

@Composable
private fun RollerShutterScreen(
  availableHeight: Dp,
  availableWidth: Dp,
  viewState: WindowViewState,
  windowState: WindowState,
  onAction: (ShadingSystemAction) -> Unit
) {
  if (WindowViewTopMenu.isSmallScreen(availableHeight)) {
    WindowViewSmallContent(
      viewState = viewState,
      windowState = windowState,
      onAction = onAction
    )
  } else {
    WindowViewNormalContent(
      availableWidth = availableWidth,
      availableHeight = availableHeight,
      viewState = viewState,
      windowState = windowState,
      onAction = onAction
    )
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Box(
      modifier = Modifier
        .background(MaterialTheme.colors.background)
        .height(700.dp)
    ) {
      WindowView(
        windowState = RollerShutterWindowState(WindowGroupedValue.Similar(75f), 90f),
        viewState = WindowViewState(
          enabled = true,
          issues = listOf(ChannelIssueItem(IssueIconType.WARNING, R.string.motor_problem)),
          showClosingPercentage = false,
          calibrating = false,
          positionUnknown = false,
          calibrationPossible = true,
          touchTime = 15.4f
        )
      ) {}
    }
  }
}

@Preview
@Composable
private fun Preview_Horizontal() {
  SuplaTheme {
    Box(
      modifier = Modifier
        .background(MaterialTheme.colors.background)
        .height(700.dp)
    ) {
      WindowView(
        windowState = RollerShutterWindowState(WindowGroupedValue.Similar(75f), 90f),
        viewState = WindowViewState(
          enabled = true,
          issues = listOf(ChannelIssueItem(IssueIconType.WARNING, R.string.motor_problem)),
          showClosingPercentage = false,
          calibrating = false,
          positionUnknown = false,
          calibrationPossible = true,
          touchTime = 15.4f,
          orientation = ShadingSystemOrientation.HORIZONTAL
        )
      ) {}
    }
  }
}

@Preview
@Composable
private fun Preview_High() {
  SuplaTheme {
    Box(
      modifier = Modifier
        .background(MaterialTheme.colors.background)
        .height(900.dp)
    ) {
      WindowView(
        windowState = RollerShutterWindowState(WindowGroupedValue.Similar(75f), 90f),
        viewState = WindowViewState(
          enabled = true,
          issues = listOf(ChannelIssueItem(IssueIconType.WARNING, R.string.motor_problem)),
          showClosingPercentage = false,
          calibrating = false,
          positionUnknown = false,
          calibrationPossible = true,
          touchTime = 15.4f
        )
      ) {}
    }
  }
}

@Preview
@Composable
private fun Preview_Small() {
  SuplaTheme {
    Box(
      modifier = Modifier
        .background(MaterialTheme.colors.background)
        .height(470.dp)
    ) {
      WindowView(
        windowState = RollerShutterWindowState(WindowGroupedValue.Similar(75f), 90f),
        viewState = WindowViewState(
          enabled = true,
          issues = listOf(ChannelIssueItem(IssueIconType.WARNING, R.string.motor_problem)),
          showClosingPercentage = false,
          calibrating = true,
          positionUnknown = true,
          calibrationPossible = true,
          touchTime = 15.4f
        )
      ) {}
    }
  }
}

@Preview
@Composable
private fun Preview_Small_Horizontal() {
  SuplaTheme {
    Box(
      modifier = Modifier
        .background(MaterialTheme.colors.background)
        .height(470.dp)
    ) {
      WindowView(
        windowState = RollerShutterWindowState(WindowGroupedValue.Similar(75f), 90f),
        viewState = WindowViewState(
          enabled = true,
          issues = listOf(ChannelIssueItem(IssueIconType.WARNING, R.string.motor_problem)),
          showClosingPercentage = false,
          calibrating = true,
          positionUnknown = true,
          calibrationPossible = true,
          touchTime = 15.4f,
          orientation = ShadingSystemOrientation.HORIZONTAL
        )
      ) {}
    }
  }
}
