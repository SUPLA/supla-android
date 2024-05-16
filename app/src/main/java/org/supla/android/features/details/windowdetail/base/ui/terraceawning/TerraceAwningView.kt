package org.supla.android.features.details.windowdetail.base.ui.terraceawning
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
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
import org.supla.android.features.details.windowdetail.base.data.TerraceAwningState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.ui.MoveState
import org.supla.android.features.details.windowdetail.base.ui.applyForAwningMarker
import org.supla.android.features.details.windowdetail.base.ui.applyForShadow
import org.supla.android.features.details.windowdetail.base.ui.applyForSlat
import org.supla.android.features.details.windowdetail.base.ui.applyForWindow
import org.supla.android.features.details.windowdetail.base.ui.windowview.WINDOW_FRAME_RADIUS

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TerraceAwningView(
  windowState: TerraceAwningState,
  modifier: Modifier = Modifier,
  colors: TerraceAwningColors = TerraceAwningColors.standard(),
  onPositionChanging: ((Float) -> Unit)? = null,
  onPositionChanged: ((Float) -> Unit)? = null
) {
  val (windowDimens, updateDimens) = remember { mutableStateOf<RuntimeDimens?>(null) }
  val moveState = remember { mutableStateOf(MoveState()) }

  Canvas(
    modifier = modifier
      .onSizeChanged { updateDimens(RuntimeDimens.make(it)) }
      .pointerInteropFilter { event ->
        if (windowDimens != null) {
          when (event.action) {
            MotionEvent.ACTION_DOWN -> {
              val point = Offset(event.x, event.y)
              if (windowDimens.canvasRect.contains(point)) {
                moveState.value = moveState.value.copy(
                  initialPoint = Offset(event.x, event.y),
                  initialVerticalPercentage = windowState.position.value
                )
              }
            }

            MotionEvent.ACTION_MOVE -> {
              moveState.value = moveState.value.copy(lastPoint = Offset(event.x, event.y))
              windowDimens
                .getPositionFromState(moveState.value)
                ?.let { position -> onPositionChanging?.let { it(position) } }
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

    if (windowState.markers.isEmpty()) {
      TerraceAwningDrawer.awningShadow(windowDimens, colors, windowState.position.value)
    } else {
      TerraceAwningDrawer.awningShadow(windowDimens, colors, windowState.markers.max())
    }

    TerraceAwningDrawer.window(windowDimens, colors)

    if (windowState.markers.isEmpty()) {
      TerraceAwningDrawer.awning(windowDimens, colors, windowState.position.value)
    } else {
      windowState.markers.sortedDescending().forEachIndexed { index, position ->
        TerraceAwningDrawer.awningLikeMarker(windowDimens, colors, position, index == 0)
      }
    }
  }
}

private object TerraceAwningDrawer {

  private val paint = Paint().asFrameworkPaint()
  private val path = Path()

  context(DrawScope)
  fun window(windowDimens: RuntimeDimens, colors: TerraceAwningColors) {
    val windowFrameRadius = WINDOW_FRAME_RADIUS.times(windowDimens.scale)

    paint.applyForWindow(colors.window, colors.shadow)
    drawContext.canvas.nativeCanvas.drawRoundRect(
      windowDimens.windowRect.left,
      windowDimens.windowRect.top,
      windowDimens.windowRect.right,
      windowDimens.windowRect.bottom,
      windowFrameRadius,
      windowFrameRadius,
      paint
    )

    glasses(windowDimens, colors)
  }

  context(DrawScope)
  private fun glasses(windowDimens: RuntimeDimens, colors: TerraceAwningColors) {
    val glassMargin = TerraceAwningDimens.GLASS_MARGIN.times(windowDimens.scale)
    val glassWidth = windowDimens.windowRect.width.minus(glassMargin.times(3f)).div(2f)
    val glassHeight = windowDimens.windowRect.height.minus(glassMargin.times(2f))

    val brush = Brush.verticalGradient(listOf(colors.glassTop, colors.glassBottom))
    drawRect(
      brush = brush,
      topLeft = Offset(
        windowDimens.windowRect.left.plus(glassMargin),
        windowDimens.windowRect.top.plus(glassMargin)
      ),
      size = Size(glassWidth, glassHeight)
    )

    drawRect(
      brush = brush,
      topLeft = Offset(
        windowDimens.windowRect.left.plus(glassMargin.times(2)).plus(glassWidth),
        windowDimens.windowRect.top.plus(glassMargin)
      ),
      size = Size(glassWidth, glassHeight)
    )
  }

  context(DrawScope)
  fun awning(windowDimens: RuntimeDimens, colors: TerraceAwningColors, position: Float) {
    val awningMinWidth = windowDimens.awningClosedWidth
    val awningMaxWidth = windowDimens.awningOpenedWidth
    val awningLeftMargin = windowDimens.canvasRect.width.minus(awningMinWidth).div(2)
    val frontMinHeight = windowDimens.awningFrontHeight.times(0.6f)

    val deepByPosition = windowDimens.awningMaxDeep.times(position).div(100)
    val widthDeltaByPosition = awningMaxWidth.minus(awningMinWidth).times(position).div(100f)
    val maxWidthByPosition = awningMinWidth.plus(widthDeltaByPosition)
    val maxWidthMarginByPosition = windowDimens.canvasRect.width.minus(maxWidthByPosition).div(2f)

    path.reset()
    path.moveTo(awningLeftMargin, 0f)
    path.relativeLineTo(awningMinWidth, 0f)
    path.relativeLineTo(widthDeltaByPosition.div(2), deepByPosition)
    path.relativeLineTo(-maxWidthByPosition, 0f)
    path.close()

    paint.applyForSlat(colors.awningBackground, colors.shadow)
    drawContext.canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
    drawPath(path = path, color = colors.awningBorder, style = Stroke(width = 1.dp.toPx()))

    val frontHeight = frontMinHeight.plus(windowDimens.awningFrontHeight.minus(frontMinHeight).times(position).div(100f))
    val frontOffset = Offset(maxWidthMarginByPosition, deepByPosition)
    val frontSize = Size(maxWidthByPosition, frontHeight)
    drawRect(color = colors.awningBackground, topLeft = frontOffset, size = frontSize)
    drawRect(color = colors.awningBorder, topLeft = frontOffset, size = frontSize, style = Stroke(width = 1.dp.toPx()))
  }

  context(DrawScope)
  fun awningShadow(windowDimens: RuntimeDimens, colors: TerraceAwningColors, position: Float) {
    val shadowMinWidth = windowDimens.awningClosedWidth
    val shadowMaxWidth = windowDimens.awningOpenedWidth
    val shadowTopMargin = windowDimens.canvasRect.width.minus(shadowMinWidth).div(2)

    val deepByPosition = windowDimens.awningMaxDeep.times(position).div(100)
    val widthDeltaByPosition = shadowMaxWidth.minus(shadowMinWidth).times(position).div(100f)
    val maxWidthByPosition = shadowMinWidth.plus(widthDeltaByPosition)

    path.reset()
    path.moveTo(shadowTopMargin, windowDimens.windowRect.bottom)
    path.relativeLineTo(shadowMinWidth, 0f)
    path.relativeLineTo(widthDeltaByPosition.div(2), deepByPosition)
    path.relativeLineTo(-maxWidthByPosition, 0f)
    path.close()

    paint.applyForShadow(colors.awningBackground)
    drawContext.canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
  }

  context(DrawScope)
  fun awningLikeMarker(windowDimens: RuntimeDimens, colors: TerraceAwningColors, position: Float, withFront: Boolean) {
    val awningMinWidth = windowDimens.awningClosedWidth
    val awningMaxWidth = windowDimens.awningOpenedWidth
    val awningTopMargin = windowDimens.canvasRect.width.minus(awningMinWidth).div(2)

    val frontMinHeight = windowDimens.awningFrontHeight.times(0.6f)

    val deepByPosition = windowDimens.awningMaxDeep.times(position).div(100)
    val widthDeltaByPosition = awningMaxWidth.minus(awningMinWidth).times(position).div(100f)
    val maxWidthByPosition = awningMinWidth.plus(widthDeltaByPosition)
    val maxWidthMarginByPosition = windowDimens.canvasRect.width.minus(maxWidthByPosition).div(2f)

    path.reset()
    path.moveTo(awningTopMargin, 0f)
    path.relativeLineTo(awningMinWidth, 0f)
    path.relativeLineTo(widthDeltaByPosition.div(2), deepByPosition)
    path.relativeLineTo(-maxWidthByPosition, 0f)
    path.close()

    paint.applyForAwningMarker(colors.awningBackground)
    drawContext.canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
    drawPath(path = path, color = colors.awningBorder, style = Stroke(width = 1.dp.toPx()))

    val frontHeight = frontMinHeight.plus(windowDimens.awningFrontHeight.minus(frontMinHeight).times(position).div(100f))
    if (withFront) {
      drawRect(
        color = colors.awningBackground,
        topLeft = Offset(maxWidthMarginByPosition, deepByPosition),
        size = Size(maxWidthByPosition, frontHeight)
      )
      drawRect(
        color = colors.awningBorder,
        topLeft = Offset(maxWidthMarginByPosition, deepByPosition),
        size = Size(maxWidthByPosition, frontHeight),
        style = Stroke(width = 1.dp.toPx())
      )
    }
  }
}

data class RuntimeDimens(
  val canvasRect: Rect,
  val scale: Float,
  val windowRect: Rect,
  val awningClosedWidth: Float,
  val awningOpenedWidth: Float,
  val awningMaxDeep: Float,
  val awningFrontHeight: Float
) {

  fun getPositionFromState(state: MoveState): Float? {
    val (initial) = guardLet(state.initialPoint) { return null }

    val verticalDiffAsPercentage =
      state.lastPoint.y.minus(initial.y)
        .div(awningMaxDeep)
        .times(100f)

    return state.initialVerticalPercentage.plus(verticalDiffAsPercentage).let {
      // Trim to 0% - 100%
      if (it < 0f) {
        0f
      } else if (it > 100f) {
        100f
      } else {
        it
      }
    }
  }

  companion object {

    fun make(viewSize: IntSize): RuntimeDimens {
      val canvasRect = canvasRect(viewSize)
      val scale = canvasRect.width / TerraceAwningDimens.WIDTH

      return RuntimeDimens(
        canvasRect = canvasRect,
        scale = scale,
        windowRect = windowRect(scale, canvasRect),
        awningOpenedWidth = TerraceAwningDimens.AWNING_OPENED_WIDTH.times(scale),
        awningClosedWidth = TerraceAwningDimens.AWNING_CLOSED_WIDTH.times(scale),
        awningMaxDeep = TerraceAwningDimens.AWNING_MAX_DEEP.times(scale),
        awningFrontHeight = TerraceAwningDimens.AWNING_FRONT_HEIGHT.times(scale),
      )
    }

    private fun canvasRect(viewSize: IntSize): Rect {
      val size = getSize(viewSize = viewSize)
      return Rect(
        Offset(viewSize.width.minus(size.width).div(2), 0f),
        size
      )
    }

    private fun windowRect(scale: Float, canvasRect: Rect): Rect {
      val windowWidth = TerraceAwningDimens.WINDOW_WIDTH.times(scale)
      val windowHeight = TerraceAwningDimens.WINDOW_HEIGHT.times(scale)
      val windowSize = Size(windowWidth, windowHeight)

      val windowTop = TerraceAwningDimens.WINDOW_TOP_DISTANCE.times(scale)
      val windowLeft = canvasRect.width.minus(windowWidth).div(2)
      val windowOffset = Offset(windowLeft, windowTop)

      return Rect(windowOffset, windowSize)
    }

    private fun getSize(viewSize: IntSize): Size {
      val canvasRatio = viewSize.width / viewSize.height.toFloat()
      return if (canvasRatio > TerraceAwningDimens.RATIO) {
        Size(viewSize.height.times(TerraceAwningDimens.RATIO), viewSize.height.toFloat())
      } else {
        Size(viewSize.width.toFloat(), viewSize.width.div(TerraceAwningDimens.RATIO))
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
        TerraceAwningView(
          windowState = TerraceAwningState(WindowGroupedValue.Similar(75f)),
          modifier = Modifier
            .fillMaxSize()
            .padding(all = Distance.small)
        )
      }
      Box(
        modifier = Modifier
          .width(231.dp)
          .height(336.dp)
          .background(color = colorResource(id = R.color.background))
      ) {
        TerraceAwningView(
          windowState = TerraceAwningState(WindowGroupedValue.Similar(50f), markers = listOf(0f, 10f, 50f, 100f)),
          modifier = Modifier
            .fillMaxSize()
          // .padding(all = Distance.small)
        )
      }
      Box(
        modifier = Modifier
          .width(200.dp)
          .height(100.dp)
          .background(color = colorResource(id = R.color.background))
      ) {
        TerraceAwningView(
          windowState = TerraceAwningState(WindowGroupedValue.Similar(25f)),
          modifier = Modifier
            .fillMaxSize()
            .padding(all = Distance.small)
        )
      }
    }
  }
}
