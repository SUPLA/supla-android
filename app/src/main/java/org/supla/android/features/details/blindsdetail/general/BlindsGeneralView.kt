package org.supla.android.features.details.blindsdetail.general
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.grey
import org.supla.android.data.model.general.ChannelIssueItem
import org.supla.android.features.details.blindsdetail.ui.BlindRollerState
import org.supla.android.features.details.blindsdetail.ui.WINDOW_VIEW_RATIO
import org.supla.android.features.details.blindsdetail.ui.WindowColors
import org.supla.android.features.details.blindsdetail.ui.WindowView
import org.supla.android.ui.lists.data.IssueIconType
import org.supla.android.ui.views.buttons.animatable.CircleControlButton
import org.supla.android.ui.views.buttons.animatable.ControlButtonIcon
import org.supla.android.ui.views.buttons.animatable.DownControlIcon
import org.supla.android.ui.views.buttons.animatable.TOTAL_HEIGHT
import org.supla.android.ui.views.buttons.animatable.UpControlIcon
import org.supla.android.ui.views.buttons.animatable.UpDownControlButton
import org.supla.android.ui.views.tools.Shadow
import org.supla.android.ui.views.tools.ShadowOrientation

sealed interface BlindsAction {
  object Open : BlindsAction
  object Close : BlindsAction
  object Stop : BlindsAction
  object MoveUp : BlindsAction
  object MoveDown : BlindsAction
  object Calibrate : BlindsAction

  data class OpenAt(val position: Float) : BlindsAction
  data class MoveTo(val position: Float) : BlindsAction
}

data class BlindsGeneralViewState(
  val issues: List<ChannelIssueItem> = emptyList(),
  val enabled: Boolean = false,
  val showClosingPercentage: Boolean = false,
  val calibrating: Boolean = false,
  val positionUnknown: Boolean = false,
  val calibrationPossible: Boolean = false,
  val touchTime: Float? = null,
  val isGroup: Boolean = false,
  val onlineStatusString: String? = null,
  val positionText: String = ""
)

private val TOP_MENU_HEIGHT_SMALL_SCREEN = 64.dp
private val TOP_MENU_HEIGHT_NORMAL_SCREEN = 80.dp

@Composable
fun BlindsGeneralView(
  rollerState: BlindRollerState,
  viewState: BlindsGeneralViewState,
  onAction: (BlindsAction) -> Unit
) {
  BoxWithConstraints {
    val height = maxHeight
    val width = maxWidth
    Column(
      modifier = Modifier
        .fillMaxSize()
    ) {
      Top(
        height = getTopHeight(height),
        blindRollerState = rollerState,
        viewState = viewState,
        onAction = onAction
      )

      BlindsScreen(
        availableHeight = height,
        availableWidth = width,
        rollerState = rollerState,
        enabled = viewState.enabled,
        issues = viewState.issues,
        touchTime = viewState.touchTime,
        onAction = onAction
      )
    }
  }
}

@Composable
private fun BlindsScreen(
  availableHeight: Dp,
  availableWidth: Dp,
  enabled: Boolean,
  rollerState: BlindRollerState,
  issues: List<ChannelIssueItem>,
  touchTime: Float?,
  onAction: (BlindsAction) -> Unit
) {
  if (isSmallScreen(availableHeight)) {
    BlindsSmallScreen(
      enabled = enabled,
      rollerState = rollerState,
      issues = issues,
      onAction = onAction
    )
  } else {
    BlindsNormalScreen(
      availableWidth = availableWidth,
      availableHeight = availableHeight,
      enabled = enabled,
      rollerState = rollerState,
      issues = issues,
      touchTime = touchTime,
      onAction = onAction
    )
  }
}

private fun isSmallScreen(availableHeight: Dp) = availableHeight < 550.dp

private fun getTopHeight(availableHeight: Dp) =
  if (isSmallScreen(availableHeight)) {
    TOP_MENU_HEIGHT_SMALL_SCREEN
  } else {
    TOP_MENU_HEIGHT_NORMAL_SCREEN
  }

@Composable
private fun BlindsSmallScreen(
  enabled: Boolean,
  rollerState: BlindRollerState,
  issues: List<ChannelIssueItem>,
  onAction: (BlindsAction) -> Unit
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
    verticalArrangement = Arrangement.spacedBy(Distance.tiny)
  ) {
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
      verticalAlignment = Alignment.CenterVertically
    ) {
      HoldToMoveButtons(enabled, onAction = onAction)

      WindowView(
        rollerState = rollerState,
        colors = if (enabled) WindowColors.standard() else WindowColors.offline(),
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        onPositionChanging = {
          if (enabled) {
            onAction(BlindsAction.MoveTo(it))
          }
        },
        onPositionChanged = {
          if (enabled) {
            onAction(BlindsAction.OpenAt(it))
          }
        }
      )

      Column(
        verticalArrangement = Arrangement.spacedBy(Distance.small)
      ) {
        OpenButton(enabled = enabled, onAction = onAction)
        StopMoveButton(enabled, onAction = onAction)
        CloseButton(enabled = enabled, onAction = onAction)
      }
    }

    if (issues.isNotEmpty()) {
      IssuesView(issues = issues)
    }
  }
}

@Composable
private fun BlindsNormalScreen(
  availableWidth: Dp,
  availableHeight: Dp,
  enabled: Boolean,
  rollerState: BlindRollerState,
  issues: List<ChannelIssueItem>,
  touchTime: Float?,
  onAction: (BlindsAction) -> Unit
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
    val heightForWindow = availableHeight
      .minus(getTopHeight(availableHeight))
      .minus(dimensionResource(id = R.dimen.custom_shadow_height))
      .minus(Distance.small)
      .minus(TOTAL_HEIGHT) // UpDownControlButton
      .minus(Distance.default) // Distance between window and buttons
      .minus(Distance.default)
      .let {
        if (issues.isNotEmpty()) {
          it.minus(dimensionResource(id = R.dimen.channel_warning_image_size).times(issues.count()))
            .minus(Distance.default) // Distance between buttons and issues
        } else {
          it
        }
      }

    WindowView(
      rollerState = rollerState,
      colors = if (enabled) WindowColors.standard() else WindowColors.offline(),
      modifier = Modifier
        .width(min(availableWidth, 300.dp))
        .height(min(heightForWindow, 300.dp.div(WINDOW_VIEW_RATIO))),
      onPositionChanging = { if (enabled) onAction(BlindsAction.MoveTo(it)) },
      onPositionChanged = { if (enabled) onAction(BlindsAction.OpenAt(it)) }
    )

    Box(modifier = Modifier.weight(1f)) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(Distance.default),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.align(Alignment.Center)
      ) {
        HoldToMoveButtons(enabled, onAction = onAction)
        StopMoveButton(enabled, onAction = onAction)
        PressToMoveButtons(enabled, onAction = onAction)
      }

      touchTime?.let { time ->
        Column(modifier = Modifier.align(Alignment.TopCenter), horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(painter = painterResource(id = R.drawable.ic_touch_hand), contentDescription = null)
          Text(text = String.format("%.1fs", time), style = MaterialTheme.typography.body2)
        }
      }
    }

    if (issues.isNotEmpty()) {
      IssuesView(issues = issues)
    }
  }
}

context (BoxScope)
@Composable
private fun OpenControlIcon(textColor: Color) =
  ControlButtonIcon(iconRes = R.drawable.ic_arrow_open, textColor = textColor)

context (BoxScope)
@Composable
private fun CloseControlIcon(textColor: Color) =
  ControlButtonIcon(iconRes = R.drawable.ic_arrow_open, textColor = textColor, rotate = 180f)

@Composable
private fun HoldToMoveButtons(enabled: Boolean, onAction: (BlindsAction) -> Unit) =
  UpDownControlButton(
    disabled = !enabled,
    upContent = { UpControlIcon(textColor = it) },
    downContent = { DownControlIcon(textColor = it) },
    upEventHandler = {
      handleEvents(
        onTouchDown = { onAction(BlindsAction.MoveUp) },
        onTouchUp = { onAction(BlindsAction.Stop) }
      )
    },
    downEventHandler = {
      handleEvents(
        onTouchDown = { onAction(BlindsAction.MoveDown) },
        onTouchUp = { onAction(BlindsAction.Stop) }
      )
    }
  )

@Composable
private fun PressToMoveButtons(enabled: Boolean, onAction: (BlindsAction) -> Unit) =
  UpDownControlButton(
    disabled = !enabled,
    upContent = { OpenControlIcon(textColor = it) },
    downContent = { CloseControlIcon(textColor = it) },
    upEventHandler = { handleEvents(onClick = { onAction(BlindsAction.Open) }) },
    downEventHandler = { handleEvents(onClick = { onAction(BlindsAction.Close) }) }
  )

@Composable
private fun StopMoveButton(enabled: Boolean, onAction: (BlindsAction) -> Unit) =
  CircleControlButton(
    iconPainter = painterResource(id = R.drawable.ic_stop),
    width = 64.dp,
    height = 64.dp,
    padding = 0.dp,
    disabled = !enabled,
    onClick = { onAction(BlindsAction.Stop) }
  )

@Composable
private fun OpenButton(enabled: Boolean, onAction: (BlindsAction) -> Unit) =
  CircleControlButton(
    iconPainter = painterResource(id = R.drawable.ic_arrow_open),
    width = 64.dp,
    height = 64.dp,
    padding = 0.dp,
    disabled = !enabled,
    onClick = { onAction(BlindsAction.Open) }
  )

@Composable
private fun CloseButton(enabled: Boolean, onAction: (BlindsAction) -> Unit) =
  CircleControlButton(
    iconPainter = painterResource(id = R.drawable.ic_arrow_close),
    width = 64.dp,
    height = 64.dp,
    padding = 0.dp,
    disabled = !enabled,
    onClick = { onAction(BlindsAction.Close) }
  )

@Composable
private fun IssuesView(issues: List<ChannelIssueItem>, modifier: Modifier = Modifier, smallScreen: Boolean = false) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_tiny))
  ) {
    val rowHeight = dimensionResource(id = R.dimen.channel_warning_image_size)
    issues.forEach {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_small)),
        modifier = Modifier
          .fillMaxWidth()
          .height(rowHeight)
      ) {
        Image(
          painter = painterResource(id = it.issueIconType.icon),
          contentDescription = null,
          modifier = Modifier.size(rowHeight)
        )
        Text(text = stringResource(id = it.descriptionRes), style = MaterialTheme.typography.body2)
      }

      if (smallScreen) {
        return
      }
    }
  }
}

@Composable
private fun Top(
  blindRollerState: BlindRollerState,
  viewState: BlindsGeneralViewState,
  height: Dp = 80.dp,
  onAction: (BlindsAction) -> Unit
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
      Icon(painter = painterResource(id = R.drawable.ic_offline), contentDescription = null, tint = MaterialTheme.colors.grey)
      TopTextLabelView(text = stringResource(id = R.string.offline))
      Spacer(modifier = Modifier.weight(1f))
    } else {
      if (viewState.calibrating) {
        CircularProgressIndicator(modifier = Modifier.size(dimensionResource(id = R.dimen.icon_default_size)))
        TopTextLabelView(text = stringResource(id = R.string.blinds_calibration))
      } else if (!viewState.positionUnknown) {
        PercentageLabelView(showClosingPercentage = viewState.showClosingPercentage)
        PercentageValueView(viewState.positionText)
      } else {
        if (viewState.isGroup) {
          PercentageLabelView(showClosingPercentage = viewState.showClosingPercentage)
          PercentageValueView("---")
        } else {
          TopTextLabelView(text = stringResource(id = R.string.blinds_calibration_needed))
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      if (viewState.calibrationPossible) {
        CircleControlButton(
          iconPainter = painterResource(id = R.drawable.recalibrate),
          width = 48.dp,
          height = 48.dp,
          padding = 0.dp,
          onClick = { onAction(BlindsAction.Calibrate) }
        )
      } else if (viewState.onlineStatusString != null) {
        TopTextLabelView(text = "ONLINE:")
        PercentageValueView(viewState.onlineStatusString)
      }
    }
  }
  Shadow(orientation = ShadowOrientation.STARTING_TOP)
}

@Composable
private fun PercentageLabelView(showClosingPercentage: Boolean) {
  if (showClosingPercentage) {
    TopTextLabelView(text = stringResource(id = R.string.blinds_closing_percentage))
  } else {
    TopTextLabelView(text = stringResource(id = R.string.blinds_opening_percentage))
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
    color = MaterialTheme.colors.grey
  )

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Box(
      modifier = Modifier
        .background(MaterialTheme.colors.background)
        .height(700.dp)
    ) {
      BlindsGeneralView(
        rollerState = BlindRollerState(75f, 90f),
        viewState = BlindsGeneralViewState(
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
private fun Preview_High() {
  SuplaTheme {
    Box(
      modifier = Modifier
        .background(MaterialTheme.colors.background)
        .height(900.dp)
    ) {
      BlindsGeneralView(
        rollerState = BlindRollerState(75f, 90f),
        viewState = BlindsGeneralViewState(
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
        .height(450.dp)
    ) {
      BlindsGeneralView(
        rollerState = BlindRollerState(75f, 90f),
        viewState = BlindsGeneralViewState(
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
