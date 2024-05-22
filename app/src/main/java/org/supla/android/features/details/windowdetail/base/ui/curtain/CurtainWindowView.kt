package org.supla.android.features.details.windowdetail.base.ui.curtain
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

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.guardLet
import org.supla.android.features.details.windowdetail.base.data.CurtainWindowState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.ui.MoveState
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDimens
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDimensBase
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDrawerBase

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CurtainWindowView(
  windowState: CurtainWindowState,
  modifier: Modifier = Modifier,
  colors: CurtainColors = CurtainColors.standard(),
  onPositionChanging: ((Float) -> Unit)? = null,
  onPositionChanged: ((Float) -> Unit)? = null
) {
  val (windowDimens, updateDimens) = remember { mutableStateOf<RuntimeDimens?>(null) }
  val moveState = remember { mutableStateOf(MoveState(horizontalAllowed = true)) }

  Canvas(
    modifier = modifier
      .onSizeChanged { updateDimens(RuntimeDimens(viewSize = it)) }
      .pointerInteropFilter { event ->
        if (windowDimens != null) {
          when (event.action) {
            MotionEvent.ACTION_DOWN -> {
              val point = Offset(event.x, event.y)
              if (windowDimens.windowRect.contains(point)) {
                moveState.value = moveState.value.copy(
                  initialPoint = Offset(event.x, event.y),
                  initialHorizontalPercentage = windowState.position.value
                )
              }
            }

            MotionEvent.ACTION_MOVE -> {
              moveState.value = moveState.value.copy(lastPoint = Offset(event.x, event.y))
              windowDimens
                .getPositionFromState(moveState.value)
                ?.let { position -> onPositionChanging?.let { it(position.x) } }
            }

            MotionEvent.ACTION_UP -> {
              onPositionChanged?.let { it(windowState.position.value) }
              moveState.value = moveState.value.copy(initialPoint = null)
            }
          }
        }
        true
      }
  ) {
    if (windowDimens == null) {
      return@Canvas // Skip drawing when view size is not set yet
    }

    WindowDrawer.drawWindow(runtimeDimens = windowDimens, colors = colors, windowState = windowState)
  }
}

private object WindowDrawer : WindowDrawerBase<RuntimeDimens, CurtainWindowState, CurtainColors>() {

  context (DrawScope)
  override fun drawShadowingElements(windowState: CurtainWindowState, runtimeDimens: RuntimeDimens, colors: CurtainColors) {
    val position = if (windowState.markers.isEmpty()) windowState.position.value else windowState.markers.max()

    val curtainWidthDiff = runtimeDimens.leftCurtainRect.width.minus(runtimeDimens.curtainMinWidth).times(100.minus(position)).div(100)

    drawCurtain(runtimeDimens.leftCurtainRect.narrowToLeft(curtainWidthDiff), colors)
    drawCurtain(runtimeDimens.rightCurtainRect.narrowToRight(curtainWidthDiff), colors)
  }

  context(DrawScope)
  override fun drawMarkers(windowState: CurtainWindowState, runtimeDimens: RuntimeDimens, colors: CurtainColors) {
    windowState.markers.forEach { markerPosition ->
      val curtainWidthDiffForMarker = runtimeDimens.leftCurtainRect.width
        .minus(runtimeDimens.curtainMinWidth)
        .times(100.minus(markerPosition))
        .div(100)

      val leftCurtainOffset = Offset(runtimeDimens.leftCurtainRect.right - curtainWidthDiffForMarker, 0f)
      drawMarker(leftCurtainOffset, runtimeDimens, colors)

      val rightCurtainOffset = Offset(runtimeDimens.rightCurtainRect.left + curtainWidthDiffForMarker, 0f)
      drawMarker(rightCurtainOffset, runtimeDimens, colors)
    }
  }

  context (DrawScope)
  private fun drawCurtain(rect: Rect, colors: CurtainColors) {
    drawRect(colors.curtainBackground, rect.topLeft, rect.size)
    drawRect(colors.curtainBorder, rect.topLeft, rect.size, style = Stroke(width = 1.dp.toPx()))
  }

  context(DrawScope)
  private fun drawMarker(offset: Offset, runtimeDimens: RuntimeDimens, colors: CurtainColors) {
    runtimeDimens.markerPath.translate(offset)
    drawPath(runtimeDimens.markerPath, colors.markerBackground)
    drawPath(runtimeDimens.markerPath, colors.markerBorder, style = Stroke(1.dp.toPx()))
    runtimeDimens.markerPath.translate(offset.times(-1f))
  }
}

private fun Rect.narrowToLeft(by: Float): Rect =
  copy(right = right - by)

private fun Rect.narrowToRight(by: Float): Rect =
  copy(left = left + by)

private data class RuntimeDimens(
  override val canvasRect: Rect,
  override val topLineRect: Rect,
  override val windowRect: Rect,
  override val scale: Float,
  var curtainMinWidth: Float,
  var leftCurtainRect: Rect,
  var rightCurtainRect: Rect,
  val markerPath: Path
) : WindowDimensBase {

  override fun getPositionFromState(state: MoveState): Offset? {
    val (initial) = guardLet(state.initialPoint) { return null }

    val horizontalDiffAsPercentage =
      state.lastPoint.x.minus(initial.x)
        .div(windowRect.width.div(2))
        .times(100f)
        .let {
          if (initial.x < windowRect.center.x) {
            it
          } else {
            -it
          }
        }

    val verticalDiffAsPercentage =
      state.lastPoint.y.minus(initial.y)
        .div(movementLimit)
        .times(100f)

    val x = state.initialHorizontalPercentage.plus(horizontalDiffAsPercentage).let {
      // Trim to 0% - 100%
      if (it < 0f) {
        0f
      } else if (it > 100f) {
        100f
      } else {
        it
      }
    }

    val y = state.initialVerticalPercentage.plus(verticalDiffAsPercentage).let {
      // Trim to 0% - 100%
      if (it < 0f) {
        0f
      } else if (it > 100f) {
        100f
      } else {
        it
      }
    }

    return if (state.horizontalAllowed) {
      Offset(x, y)
    } else {
      Offset(state.initialHorizontalPercentage, y)
    }
  }

  companion object {
    const val CURTAIN_WIDTH = 142
    const val CURTAIN_HEIGHT = 328
    const val CURTAIN_MIN_WIDTH = 24
    const val MARKER_WIDTH = 8f
    const val MARKER_HEIGHT = 28f

    operator fun invoke(viewSize: IntSize): RuntimeDimens {
      val canvasRect = WindowDimensBase.canvasRect(viewSize = viewSize)
      val scale = canvasRect.width / WindowDimens.WIDTH
      val topLineRect = Rect(
        Offset(canvasRect.left, canvasRect.top),
        Size(canvasRect.width, WindowDimens.TOP_LINE_HEIGHT.times(scale))
      )
      val windowRect = WindowDimensBase.windowRect(scale, canvasRect, topLineRect)

      return RuntimeDimens(
        canvasRect = canvasRect,
        topLineRect = topLineRect,
        windowRect = windowRect,
        scale = scale,
        curtainMinWidth = CURTAIN_MIN_WIDTH.times(scale),
        leftCurtainRect = leftCurtainRect(scale, topLineRect),
        rightCurtainRect = rightCurtainRect(scale, topLineRect),
        markerPath = getMarkerPath(scale, topLineRect)
      )
    }

    private fun leftCurtainRect(scale: Float, topLineRect: Rect): Rect {
      val curtainWidth = CURTAIN_WIDTH.times(scale)
      val curtainHeight = CURTAIN_HEIGHT.times(scale).minus(topLineRect.height)

      return Rect(offset = topLineRect.bottomLeft, size = Size(curtainWidth, curtainHeight))
    }

    private fun rightCurtainRect(scale: Float, topLineRect: Rect): Rect {
      val curtainWidth = CURTAIN_WIDTH.times(scale)
      val curtainHeight = CURTAIN_HEIGHT.times(scale).minus(topLineRect.height)
      val left = topLineRect.right.minus(curtainWidth)

      return Rect(offset = Offset(left, topLineRect.bottom), size = Size(curtainWidth, curtainHeight))
    }

    private fun getMarkerPath(scale: Float, topLineRect: Rect): Path {
      val height = MARKER_HEIGHT.times(scale)
      val width = MARKER_WIDTH.times(scale)
      val halfWidth = width.div(2)
      val top = topLineRect.bottom

      return Path().apply {
        moveTo(0f, top)
        lineTo(halfWidth, top.plus(halfWidth))
        lineTo(halfWidth, top.plus(height))
        lineTo(-halfWidth, top.plus(height))
        lineTo(-halfWidth, top.plus(halfWidth))
        close()
      }
    }
  }
}

@Preview
@Composable
private fun Preview_Normal() {
  SuplaTheme {
    Column(verticalArrangement = Arrangement.spacedBy(Distance.tiny)) {
      Box(
        modifier = Modifier
          .width(200.dp)
          .height(200.dp)
          .background(color = colorResource(id = R.color.background))
      ) {
        CurtainWindowView(
          windowState = CurtainWindowState(WindowGroupedValue.Similar(75f)),
          modifier = Modifier
            .fillMaxSize()
            .padding(all = Distance.small)
        )
      }
      Box(
        modifier = Modifier
          .width(200.dp)
          .height(350.dp)
          .background(color = colorResource(id = R.color.background))
      ) {
        CurtainWindowView(
          windowState = CurtainWindowState(WindowGroupedValue.Similar(75f), markers = listOf(0f, 10f, 50f, 80f)),
          modifier = Modifier
            .fillMaxSize()
            .padding(all = Distance.small)
        )
      }
      Box(
        modifier = Modifier
          .width(200.dp)
          .height(100.dp)
          .background(color = colorResource(id = R.color.background))
      ) {
        CurtainWindowView(
          windowState = CurtainWindowState(WindowGroupedValue.Similar(25f)),
          modifier = Modifier
            .fillMaxSize()
            .padding(all = Distance.small)
        )
      }
    }
  }
}
