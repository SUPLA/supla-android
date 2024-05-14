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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
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
import androidx.compose.ui.platform.LocalContext
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
import org.supla.android.core.ui.theme.gray
import org.supla.android.data.model.general.ChannelIssueItem
import org.supla.android.features.details.windowdetail.base.data.RollerShutterWindowState
import org.supla.android.features.details.windowdetail.base.data.RoofWindowState
import org.supla.android.features.details.windowdetail.base.data.TerraceAwningState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.WindowState
import org.supla.android.features.details.windowdetail.base.data.facadeblinds.FacadeBlindWindowState
import org.supla.android.features.details.windowdetail.base.ui.facadeblinds.FacadeBlindsWindowView
import org.supla.android.features.details.windowdetail.base.ui.facadeblinds.SlatTiltSlider
import org.supla.android.features.details.windowdetail.base.ui.facadeblinds.SlatTiltSliderDimens
import org.supla.android.features.details.windowdetail.base.ui.rollershutter.RollerShutterWindowView
import org.supla.android.features.details.windowdetail.base.ui.roofwindow.RoofWindowView
import org.supla.android.features.details.windowdetail.base.ui.terraceawning.TerraceAwningView
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDimens
import org.supla.android.ui.lists.data.IssueIconType
import org.supla.android.ui.views.buttons.animatable.CircleControlButton
import org.supla.android.ui.views.buttons.animatable.ControlButtonIcon
import org.supla.android.ui.views.buttons.animatable.DownControlIcon
import org.supla.android.ui.views.buttons.animatable.TOTAL_HEIGHT
import org.supla.android.ui.views.buttons.animatable.UpControlIcon
import org.supla.android.ui.views.buttons.animatable.UpDownControlButton
import org.supla.android.ui.views.tools.Shadow
import org.supla.android.ui.views.tools.ShadowOrientation

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
)

private val TOP_MENU_HEIGHT_SMALL_SCREEN = 64.dp
private val TOP_MENU_HEIGHT_NORMAL_SCREEN = 80.dp

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
      Top(
        viewState = viewState,
        windowState = windowState,
        height = getTopHeight(height),
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
  if (isSmallScreen(availableHeight)) {
    RollerShutterSmallScreen(
      viewState = viewState,
      windowState = windowState,
      onAction = onAction
    )
  } else {
    RollerShutterNormalScreen(
      availableWidth = availableWidth,
      availableHeight = availableHeight,
      viewState = viewState,
      windowState = windowState,
      onAction = onAction
    )
  }
}

private fun isSmallScreen(availableHeight: Dp) = availableHeight < 480.dp

private fun getTopHeight(availableHeight: Dp) =
  if (isSmallScreen(availableHeight)) {
    TOP_MENU_HEIGHT_SMALL_SCREEN
  } else {
    TOP_MENU_HEIGHT_NORMAL_SCREEN
  }

@Composable
private fun RollerShutterSmallScreen(
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
    verticalArrangement = spacedBy(4.dp)
  ) {
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(modifier = Modifier.fillMaxHeight()) {
        PressTimeInfo(viewState = viewState, modifier = Modifier.align(Alignment.TopCenter))
        HoldToMoveButtons(viewState.enabled, onAction = onAction, modifier = Modifier.align(Alignment.Center))
      }

      WindowView(
        viewState = viewState,
        windowState = windowState,
        onAction = onAction,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
      )

      Column(
        verticalArrangement = Arrangement.spacedBy(Distance.small)
      ) {
        OpenButton(enabled = viewState.enabled, onAction = onAction)
        StopMoveButton(viewState.enabled, onAction = onAction)
        CloseButton(enabled = viewState.enabled, onAction = onAction)
      }
    }

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

    if (viewState.issues.isNotEmpty()) {
      IssuesView(issues = viewState.issues)
    }
  }
}

@Composable
private fun RollerShutterNormalScreen(
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
    val heightForWindow = availableHeight
      .minus(getTopHeight(availableHeight))
      .minus(dimensionResource(id = R.dimen.custom_shadow_height))
      .minus(Distance.small)
      .minus(TOTAL_HEIGHT) // UpDownControlButton
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

    WindowView(
      viewState = viewState,
      windowState = windowState,
      onAction = onAction,
      modifier = Modifier
        .width(min(availableWidth, 300.dp))
        .height(min(heightForWindow, 300.dp.div(WindowDimens.RATIO)))
    )

    Box(modifier = Modifier.weight(1f)) {
      Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(Distance.small)
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(Distance.default),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          HoldToMoveButtons(viewState.enabled, onAction = onAction)
          Box(modifier = Modifier.height(TOTAL_HEIGHT)) {
            PressTimeInfo(viewState = viewState, modifier = Modifier.align(Alignment.TopCenter))
            StopMoveButton(viewState.enabled, onAction = onAction, modifier = Modifier.align(Alignment.Center))
          }
          PressToMoveButtons(viewState.enabled, onAction = onAction)
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
    }

    if (viewState.issues.isNotEmpty()) {
      IssuesView(issues = viewState.issues)
    }
  }
}

@Composable
private fun WindowView(
  viewState: WindowViewState,
  windowState: WindowState,
  onAction: (ShadingSystemAction) -> Unit,
  modifier: Modifier = Modifier
) {
  when (windowState) {
    is RoofWindowState ->
      RoofWindowView(
        windowState = windowState,
        colors = if (viewState.enabled) WindowColors.standard() else WindowColors.offline(),
        modifier = modifier,
        onPositionChanging = { if (viewState.enabled) onAction(ShadingSystemAction.MoveTo(it)) },
        onPositionChanged = { if (viewState.enabled) onAction(ShadingSystemAction.OpenAt(it)) }
      )
    is RollerShutterWindowState ->
      RollerShutterWindowView(
        windowState = windowState,
        colors = if (viewState.enabled) WindowColors.standard() else WindowColors.offline(),
        modifier = modifier,
        onPositionChanging = { if (viewState.enabled) onAction(ShadingSystemAction.MoveTo(it)) },
        onPositionChanged = { if (viewState.enabled) onAction(ShadingSystemAction.OpenAt(it)) }
      )
    is FacadeBlindWindowState ->
      FacadeBlindsWindowView(
        windowState = windowState,
        colors = if (viewState.enabled) WindowColors.standard() else WindowColors.offline(),
        modifier = modifier,
        onPositionChanging = { tilt, position -> if (viewState.enabled) onAction(ShadingSystemAction.MoveAndTiltTo(position, tilt)) },
        onPositionChanged = { tilt, position -> if (viewState.enabled) onAction(ShadingSystemAction.MoveAndTiltSetTo(position, tilt)) }
      )
    is TerraceAwningState ->
      TerraceAwningView(
        windowState = windowState,
        colors = if (viewState.enabled) WindowColors.standard() else WindowColors.offline(),
        modifier = modifier,
        onPositionChanging = { position -> if (viewState.enabled) onAction(ShadingSystemAction.MoveTo(position)) },
        onPositionChanged = { position -> if (viewState.enabled) onAction(ShadingSystemAction.OpenAt(position)) }
      )
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
private fun HoldToMoveButtons(enabled: Boolean, modifier: Modifier = Modifier, onAction: (ShadingSystemAction) -> Unit) =
  UpDownControlButton(
    disabled = !enabled,
    upContent = { UpControlIcon(textColor = it) },
    downContent = { DownControlIcon(textColor = it) },
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
private fun PressToMoveButtons(enabled: Boolean, onAction: (ShadingSystemAction) -> Unit) =
  UpDownControlButton(
    disabled = !enabled,
    upContent = { OpenControlIcon(textColor = it) },
    downContent = { CloseControlIcon(textColor = it) },
    upEventHandler = { handleEvents(onClick = { onAction(ShadingSystemAction.Open) }) },
    downEventHandler = { handleEvents(onClick = { onAction(ShadingSystemAction.Close) }) }
  )

@Composable
private fun StopMoveButton(enabled: Boolean, modifier: Modifier = Modifier, onAction: (ShadingSystemAction) -> Unit) =
  CircleControlButton(
    iconPainter = painterResource(id = R.drawable.ic_stop),
    onClick = { onAction(ShadingSystemAction.Stop) },
    disabled = !enabled,
    width = 64.dp,
    height = 64.dp,
    padding = 0.dp,
    iconColor = MaterialTheme.colors.onBackground,
    modifier = modifier
  )

@Composable
private fun OpenButton(enabled: Boolean, onAction: (ShadingSystemAction) -> Unit) =
  CircleControlButton(
    iconPainter = painterResource(id = R.drawable.ic_arrow_open),
    onClick = { onAction(ShadingSystemAction.Open) },
    disabled = !enabled,
    width = 64.dp,
    height = 64.dp,
    padding = 0.dp,
    iconColor = MaterialTheme.colors.onBackground
  )

@Composable
private fun CloseButton(enabled: Boolean, onAction: (ShadingSystemAction) -> Unit) =
  CircleControlButton(
    iconPainter = painterResource(id = R.drawable.ic_arrow_close),
    onClick = { onAction(ShadingSystemAction.Close) },
    disabled = !enabled,
    width = 64.dp,
    height = 64.dp,
    padding = 0.dp,
    iconColor = MaterialTheme.colors.onBackground
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
        Column(verticalArrangement = spacedBy(Distance.tiny)) {
          OpenClosePercentageLabel(viewState.showClosingPercentage, windowState.positionText())
          (windowState as? FacadeBlindWindowState)?.let {
            SlatsTiltLabel(value = windowState.slatTiltText()(LocalContext.current))
          }
        }
      } else if (viewState.isGroup) {
        OpenClosePercentageLabel(viewState.showClosingPercentage, "---")
      } else {
        TopTextLabelView(text = stringResource(id = R.string.roller_shutter_calibration_needed))
      }

      Spacer(modifier = Modifier.weight(1f))

      if (viewState.calibrationPossible) {
        CircleControlButton(
          iconPainter = painterResource(id = R.drawable.ic_recalibrate),
          onClick = { onAction(ShadingSystemAction.Calibrate) },
          width = 48.dp,
          height = 48.dp,
          padding = 0.dp
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
private fun OpenClosePercentageLabel(showClosing: Boolean, value: String) {
  Row(horizontalArrangement = Arrangement.spacedBy(Distance.tiny)) {
    PercentageLabelView(showClosingPercentage = showClosing)
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
private fun PercentageLabelView(showClosingPercentage: Boolean) {
  if (showClosingPercentage) {
    TopTextLabelView(text = stringResource(id = R.string.roller_shutter_closing_percentage))
  } else {
    TopTextLabelView(text = stringResource(id = R.string.roller_shutter_opening_percentage))
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

@Composable
private fun PressTimeInfo(viewState: WindowViewState, modifier: Modifier = Modifier) {
  viewState.touchTime?.let { time ->
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(painter = painterResource(id = R.drawable.ic_touch_hand), contentDescription = null)
      Text(text = String.format("%.1fs", time), style = MaterialTheme.typography.body2)
    }
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
