package org.supla.android.features.details.thermostatdetail.schedule.ui.components
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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.extensions.toDp
import org.supla.android.features.details.thermostatdetail.schedule.data.ScheduleDetailProgramBox
import org.supla.android.features.details.thermostatdetail.schedule.extensions.colorRes
import org.supla.android.ui.views.buttons.IconButton

@Composable
fun ScheduleInfo(boxSize: Size, onClose: () -> Unit) {
  val state = remember {
    MutableTransitionState(false).apply {
      targetState = true // Start the animation immediately.
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(colorResource(id = R.color.info_scrim))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = { }
      )
  ) {
    val boxWidth = boxSize.width.toDp()
    val boxHeight = boxSize.height.toDp()

    CloseIcon(onClick = onClose)
    ProgramButtonInfo(state)

    val topMargin = 94.dp.plus(boxHeight.times(6))
    ProgramBoxInfo(state = state, topMargin = topMargin, boxWidth = boxWidth, boxHeight = boxHeight)
    DarkCornerInfo(state = state, topMargin = topMargin, boxWidth = boxWidth, boxHeight = boxHeight)
  }
}

@Composable
private fun FadeInBlock(
  state: MutableTransitionState<Boolean>,
  modifier: Modifier = Modifier,
  delay: Int = 0,
  content: @Composable AnimatedVisibilityScope.() -> Unit
) =
  AnimatedVisibility(
    visibleState = state,
    enter = fadeIn(animationSpec = TweenSpec(1000, delay)),
    modifier = modifier,
    content = content
  )

context (BoxScope)
@Composable
private fun CloseIcon(onClick: () -> Unit) =
  IconButton(
    icon = R.drawable.ic_close,
    onClick = onClick,
    modifier = Modifier
      .align(Alignment.TopEnd)
      .padding(top = Distance.tiny, end = Distance.small),
    tint = MaterialTheme.colorScheme.onPrimaryContainer
  )

@Composable
private fun ProgramButtonInfo(state: MutableTransitionState<Boolean>) {
  FadeInBlock(state = state) {
    SampleProgramButton()
  }

  FadeInBlock(state = state) {
    ProgramButtonArrow()
  }

  FadeInBlock(state = state) {
    ProgramButtonText()
  }
}

@Composable
private fun SampleProgramButton() =
  ScheduleProgramButton(
    programBox = ScheduleDetailProgramBox(
      channelFunction = 0,
      thermostatFunction = ThermostatSubfunction.HEAT,
      scheduleProgram = SuplaWeeklyScheduleProgram(
        program = SuplaScheduleProgram.PROGRAM_1,
        mode = SuplaHvacMode.HEAT,
        setpointTemperatureHeat = 1900
      )
    ),
    active = false,
    onClick = { },
    onLongClick = { },
    modifier = Modifier.padding(horizontal = 26.dp, vertical = 18.dp)
  )

@Composable
private fun ProgramButtonArrow() =
  Arrow(modifier = Modifier.padding(start = 100.dp, top = 30.dp))

@Composable
private fun ProgramButtonText() =
  Text(
    text = stringResource(id = R.string.thermostat_detail_program_info),
    style = MaterialTheme.typography.bodyLarge,
    color = MaterialTheme.colorScheme.onPrimaryContainer,
    modifier = Modifier
      .padding(top = 85.dp, start = 25.dp)
      .width(250.dp),
    textAlign = TextAlign.Center
  )

context (BoxScope)
@Composable
private fun ProgramBoxInfo(
  state: MutableTransitionState<Boolean>,
  topMargin: Dp,
  boxWidth: Dp,
  boxHeight: Dp
) {
  FadeInBlock(state = state, delay = 250, modifier = Modifier.align(Alignment.TopEnd)) {
    SampleProgramBox(topMargin = topMargin, boxWidth = boxWidth, boxHeight = boxHeight)
  }

  FadeInBlock(state = state, delay = 250, modifier = Modifier.align(Alignment.TopEnd)) {
    SampleBoxArrow(topMargin = topMargin, boxWidth = boxWidth, boxHeight = boxHeight)
  }

  FadeInBlock(state = state, delay = 250, modifier = Modifier.align(Alignment.TopEnd)) {
    SampleBoxText(topMargin = topMargin, boxWidth = boxWidth, boxHeight = boxHeight)
  }
}

@Composable
private fun SampleProgramBox(topMargin: Dp, boxWidth: Dp, boxHeight: Dp) =
  Box(
    modifier = Modifier
      .padding(top = topMargin, end = 26.dp)
      .size(width = boxWidth, height = boxHeight)
      .background(
        color = colorResource(id = SuplaScheduleProgram.PROGRAM_1.colorRes()),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_small))
      )
  )

@Composable
private fun SampleBoxArrow(topMargin: Dp, boxWidth: Dp, boxHeight: Dp) =
  Arrow(
    modifier = Modifier
      .padding(
        top = topMargin
          .plus(boxHeight)
          .plus(4.dp),
        end = boxWidth
          .div(2)
          .plus(24.dp)
      )
      .rotate(90f)
  )

@Composable
private fun SampleBoxText(topMargin: Dp, boxWidth: Dp, boxHeight: Dp) =
  Text(
    text = stringResource(id = R.string.thermostat_detail_box_info),
    style = MaterialTheme.typography.bodyLarge,
    color = MaterialTheme.colorScheme.onPrimaryContainer,
    modifier = Modifier
      .padding(
        top = topMargin
          .plus(boxHeight.times(2))
          .plus(4.dp),
        end = boxWidth
          .div(2)
          .plus(74.dp)
      )
      .width(250.dp),
    textAlign = TextAlign.Center
  )

context (BoxScope)
@Composable
private fun DarkCornerInfo(
  state: MutableTransitionState<Boolean>,
  topMargin: Dp,
  boxWidth: Dp,
  boxHeight: Dp
) {
  val arrowTopMargin = topMargin.plus(boxHeight.times(11)).plus(44.dp)
  FadeInBlock(state = state, delay = 500, modifier = Modifier.align(Alignment.TopEnd)) {
    SampleDarkCornerBox(topMargin = arrowTopMargin, boxWidth = boxWidth, boxHeight = boxHeight)
  }

  FadeInBlock(state = state, delay = 500, modifier = Modifier.align(Alignment.TopEnd)) {
    SampleDarkCornerArrow(topMargin = arrowTopMargin, boxWidth = boxWidth, boxHeight = boxHeight)
  }

  FadeInBlock(state = state, delay = 500, modifier = Modifier.align(Alignment.TopEnd)) {
    SampleDarkCornerText(topMargin = arrowTopMargin, boxWidth = boxWidth, boxHeight = boxHeight)
  }
}

@Composable
private fun SampleDarkCornerBox(topMargin: Dp, boxWidth: Dp, boxHeight: Dp) {
  val cornerRadius = dimensionResource(id = R.dimen.radius_small)
  val halfHeight = boxHeight.div(2)
  Box(
    modifier = Modifier
      .padding(
        top = topMargin,
        end = 26.dp
      )
      .size(width = boxWidth, height = boxHeight)
      .background(
        color = colorResource(id = SuplaScheduleProgram.PROGRAM_2.colorRes()),
        shape = RoundedCornerShape(cornerRadius)
      )
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
    ) {
      val path = Path()
      path.moveTo(cornerRadius.toPx(), 0f)
      path.relativeLineTo(halfHeight.toPx(), 0f)
      path.relativeLineTo((-halfHeight).minus(cornerRadius).toPx(), halfHeight.plus(cornerRadius).toPx())
      path.relativeLineTo(0f, (-halfHeight).toPx())
      path.close()
      drawPath(path = path, color = Color.Black)
    }
  }
}

@Composable
private fun SampleDarkCornerArrow(topMargin: Dp, boxWidth: Dp, boxHeight: Dp) =
  Arrow(
    modifier = Modifier
      .padding(
        top = topMargin
          .plus(boxHeight)
          .plus(4.dp),
        end = boxWidth
          .div(2)
          .plus(24.dp)
      )
      .rotate(90f)
  )

@Composable
private fun SampleDarkCornerText(topMargin: Dp, boxWidth: Dp, boxHeight: Dp) =
  Text(
    text = stringResource(id = R.string.thermostat_detail_arrow_info),
    style = MaterialTheme.typography.bodyLarge,
    color = MaterialTheme.colorScheme.onPrimaryContainer,
    modifier = Modifier
      .padding(
        top = topMargin
          .plus(boxHeight.times(2))
          .plus(4.dp),
        end = boxWidth
          .div(2)
          .plus(78.dp)
      )
      .width(200.dp),
    textAlign = TextAlign.Center
  )

@Composable
private fun Arrow(modifier: Modifier) {
  val color = MaterialTheme.colorScheme.onPrimaryContainer
  Canvas(
    modifier = modifier
      .size(50.dp, 50.dp)
  ) {
    val path = Path()
    path.moveTo(0f, 5.dp.toPx())
    path.cubicTo(12.5.dp.toPx(), 0f, 50.dp.toPx(), 12.5.dp.toPx(), 50.dp.toPx(), 50.dp.toPx())
    path.moveTo(0f, 5.dp.toPx())
    path.lineTo(5.dp.toPx(), 0.dp.toPx())
    path.moveTo(0f, 5.dp.toPx())
    path.lineTo(5.dp.toPx(), 10.dp.toPx())
    drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx()))
  }
}
