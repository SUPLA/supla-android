package org.supla.android.features.details.windowdetail.base.ui.verticalblinds
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
import androidx.compose.ui.graphics.NativePaint
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.limit
import org.supla.android.extensions.toPx
import org.supla.android.features.details.windowdetail.base.data.ShadingBlindMarker
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.verticalblinds.VerticalBlindWindowState
import org.supla.android.features.details.windowdetail.base.ui.MoveState
import org.supla.android.features.details.windowdetail.base.ui.facadeblinds.SlatTiltSliderDimens
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDimens
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDimensBase
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDrawerBase
import kotlin.math.abs

val slatShadowRadius = 1.dp
private const val MOVE_HYSTERESIS = 20

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VerticalBlindsWindowView(
  windowState: VerticalBlindWindowState,
  modifier: Modifier = Modifier,
  enabled: Boolean = false,
  onPositionChanging: ((tilt: Float, position: Float) -> Unit)? = null,
  onPositionChanged: ((tilt: Float, position: Float) -> Unit)? = null
) {
  val (windowDimens, updateDimens) = remember { mutableStateOf<RuntimeDimens?>(null) }
  val moveState = remember { mutableStateOf(MoveState()) }
  val colors = VerticalBlindColors.standard()

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
                  initialVerticalPercentage = windowState.slatTilt?.value ?: 0f,
                  initialHorizontalPercentage = windowState.position.value,
                  horizontalAllowed = true,
                  verticalAllowed = false
                )
              }
            }

            MotionEvent.ACTION_MOVE -> {
              val verticalAllowed = moveState.value.initialPoint?.let {
                abs(it.y - event.y) > MOVE_HYSTERESIS.dp.toPx()
              } ?: false

              moveState.value = moveState.value.copy(
                lastPoint = Offset(event.x, event.y),
                verticalAllowed = moveState.value.verticalAllowed || verticalAllowed
              )
              windowDimens
                .getPositionFromState(moveState.value, bidirectional = true)
                ?.let { offset -> onPositionChanging?.let { it(offset.y, offset.x) } }
            }

            MotionEvent.ACTION_UP -> {
              onPositionChanged?.let { it(windowState.slatTilt?.value ?: 0f, windowState.position.value) }
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
    if (enabled.not()) {
      drawRect(colors.disabledOverlay)
    }
  }
}

private object WindowDrawer : WindowDrawerBase<RuntimeDimens, VerticalBlindWindowState, VerticalBlindColors>() {

  private val maxTilt = 8.dp.toPx()
  private val tiltInitialCorrection = 1.dp.toPx()
  private const val TILT_RANGE = 180f
  private const val TILT_HALF_RANGE = 90f

  context (DrawScope)
  override fun drawShadowingElements(windowState: VerticalBlindWindowState, runtimeDimens: RuntimeDimens, colors: VerticalBlindColors) {
    val leftCorrection = windowState.position.value
      .times(runtimeDimens.movementLimit)
      .div(100f)

    for (slat in runtimeDimens.leftSlats) {
      drawSlat(
        horizontalCorrection = leftCorrection - runtimeDimens.movementLimit,
        rect = slat,
        runtimeDimens = runtimeDimens,
        colors = colors,
        tilt = windowState.slatTiltDegrees
      )
    }

    for (slat in runtimeDimens.rightSlats) {
      drawSlat(
        horizontalCorrection = runtimeDimens.movementLimit - leftCorrection,
        rect = slat,
        runtimeDimens = runtimeDimens,
        colors = colors,
        tilt = windowState.slatTiltDegrees
      )
    }
  }

  context(DrawScope)
  override fun drawMarkers(
    windowState: VerticalBlindWindowState,
    runtimeDimens: RuntimeDimens,
    colors: VerticalBlindColors
  ) {
    windowState.markers.forEach { marker ->
      val leftPosition = runtimeDimens.topLineRect.left
        .plus(runtimeDimens.slatWidth)
        .plus(runtimeDimens.movementLimit.times(marker.position).div(100f))

      val tilt0 = windowState.tilt0Angle ?: VerticalBlindWindowState.DEFAULT_TILT_0_ANGLE
      val tilt100 = windowState.tilt100Angle ?: VerticalBlindWindowState.DEFAULT_TILT_100_ANGLE
      val degrees = tilt0.plus(tilt100.minus(tilt0).times(marker.tilt).div(100))
      val trimmed = SlatTiltSliderDimens.trimAngle(degrees)

      drawMarker(
        Offset(leftPosition, runtimeDimens.topLineRect.bottom),
        trimmed,
        runtimeDimens,
        colors
      )
    }
  }

  context(DrawScope)
  private fun drawSlat(
    horizontalCorrection: Float,
    rect: Rect,
    runtimeDimens: RuntimeDimens,
    colors: VerticalBlindColors,
    tilt: Float?
  ) {
    val verticalSlatCorrection: Float
    val horizontalSlatCorrection: Float
    if (tilt != null) {
      val correctedTilt = if (tilt <= TILT_HALF_RANGE) tilt else TILT_RANGE - tilt
      verticalSlatCorrection = maxTilt.times(correctedTilt).div(TILT_HALF_RANGE).div(2)
        .plus(tiltInitialCorrection)
        .let { if (tilt <= TILT_HALF_RANGE) it else -it }
      val maxSlatCorrection = rect.width
      horizontalSlatCorrection = maxSlatCorrection.times(correctedTilt).div(TILT_HALF_RANGE).div(2)
    } else {
      verticalSlatCorrection = tiltInitialCorrection
      horizontalSlatCorrection = 0f
    }

    val bottom = rect.bottom
    val top = rect.top
    val left = rect.left.plus(horizontalCorrection)
      .limit(min = runtimeDimens.topLineRect.left, max = runtimeDimens.topLineRect.right.minus(rect.width))
      .plus(horizontalSlatCorrection)
    val right = rect.right.plus(horizontalCorrection)
      .limit(min = runtimeDimens.topLineRect.left.plus(rect.width), max = runtimeDimens.topLineRect.right)
      .minus(horizontalSlatCorrection)

    path.reset()
    path.moveTo(left, top)
    path.lineTo(right, top)
    path.lineTo(right, bottom + verticalSlatCorrection)
    path.lineTo(left, bottom - verticalSlatCorrection)
    path.close()

    paint.applyForSlat(colors)
    drawContext.canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
    drawPath(path = path, color = colors.slatBorder, style = Stroke(width = 1.dp.toPx()))
  }

  context (DrawScope)
  private fun drawMarker(offset: Offset, tilt: Float, runtimeDimens: RuntimeDimens, windowColors: VerticalBlindColors) {
    drawCircle(color = windowColors.slatBackground, radius = runtimeDimens.markerInfoRadius, center = offset)
    drawCircle(
      color = windowColors.slatBorder,
      radius = runtimeDimens.markerInfoRadius,
      center = offset,
      style = Stroke(width = 1.5.dp.toPx())
    )
    val lineOffset = runtimeDimens.markerInfoRadius / 2

    val lines = listOf(
      offset.minus(Offset(0f, 2.5.dp.toPx())),
      offset,
      offset.plus(Offset(0f, 2.5.dp.toPx()))
    )
    lines.forEach {
      rotate(tilt, it) {
        drawLine(
          color = windowColors.markerBorder,
          start = Offset(it.x - lineOffset, it.y),
          end = Offset(it.x + lineOffset, it.y),
          strokeWidth = 1.dp.toPx()
        )
      }
    }
  }
}

context(DrawScope)
private fun NativePaint.applyForSlat(colors: VerticalBlindColors) {
  style = android.graphics.Paint.Style.FILL
  strokeCap = android.graphics.Paint.Cap.SQUARE
  color = colors.slatBackground.toArgb()
  setShadowLayer(slatShadowRadius.toPx(), 0f, 1.5.dp.toPx(), colors.shadow.toArgb())
}

private data class RuntimeDimens(
  override val canvasRect: Rect,
  override val topLineRect: Rect,
  override val windowRect: Rect,
  override val scale: Float,
  val slatWidth: Float,
  val leftSlats: List<Rect>,
  val rightSlats: List<Rect>,
  val slatDistance: Float,
  val markerInfoRadius: Float
) : WindowDimensBase {

  override val movementLimit: Float
    get() = topLineRect.width.div(2).minus(SLAT_WIDTH.times(scale)).minus(slatDistance)

  companion object {
    const val MARKER_INFO_RADIUS = 12f
    const val SLAT_DISTANCE = 4f
    const val SLAT_WIDTH = 24f
    const val SLAT_HEIGHT = 308f

    operator fun invoke(viewSize: IntSize): RuntimeDimens {
      val canvasRect = WindowDimensBase.canvasRect(viewSize = viewSize)
      val scale = canvasRect.width / WindowDimens.WIDTH
      val topLineRect = Rect(
        Offset(canvasRect.left, canvasRect.top),
        Size(canvasRect.width, WindowDimens.TOP_LINE_HEIGHT.times(scale))
      )
      val windowRect = WindowDimensBase.windowRect(scale, canvasRect, topLineRect)
      val leftSlats = leftSlats(scale, topLineRect)
      val rightSlats = rightSlats(scale, topLineRect)
      val slatDistance = SLAT_DISTANCE.times(scale)

      return RuntimeDimens(
        canvasRect = canvasRect,
        topLineRect = topLineRect,
        windowRect = windowRect,
        slatWidth = SLAT_WIDTH.times(scale),
        leftSlats = leftSlats,
        rightSlats = rightSlats,
        scale = scale,
        slatDistance = slatDistance,
        markerInfoRadius = MARKER_INFO_RADIUS.times(scale),
      )
    }

    private fun leftSlats(scale: Float, topLineRect: Rect): List<Rect> {
      val slatDistance = SLAT_DISTANCE.times(scale)
      val slatSize = Size(SLAT_WIDTH.times(scale), SLAT_HEIGHT.times(scale))

      val slatSpace = slatSize.width.plus(slatDistance)
      val slatsCount = topLineRect.width.div(2).div(slatSpace).toInt()
      val top = topLineRect.bottom

      return mutableListOf<Rect>().also {
        for (i in 0 until slatsCount) {
          it.add(Rect(Offset(topLineRect.left.plus(slatSpace.times(i)), top), slatSize))
        }
      }
    }

    private fun rightSlats(scale: Float, topLineRect: Rect): List<Rect> {
      val slatDistance = SLAT_DISTANCE.times(scale)
      val slatSize = Size(SLAT_WIDTH.times(scale), SLAT_HEIGHT.times(scale))

      val slatSpace = slatSize.width.plus(slatDistance)
      val slatsCount = topLineRect.width.div(2).div(slatSpace).toInt()
      val top = topLineRect.bottom
      val left = topLineRect.right.minus(slatSize.width)

      return mutableListOf<Rect>().also {
        for (i in 0 until slatsCount) {
          it.add(Rect(Offset(left.minus(slatSpace.times(i)), top), slatSize))
        }
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
        VerticalBlindsWindowView(
          windowState = VerticalBlindWindowState(WindowGroupedValue.Similar(100f)),
          enabled = true,
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
        val markers = listOf(ShadingBlindMarker(0f, 50f), ShadingBlindMarker(35f, 25f))
        VerticalBlindsWindowView(
          windowState = VerticalBlindWindowState(WindowGroupedValue.Similar(75f), markers = markers),
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
        VerticalBlindsWindowView(
          windowState = VerticalBlindWindowState(WindowGroupedValue.Similar(25f)),
          enabled = true,
          modifier = Modifier
            .fillMaxSize()
            .padding(all = Distance.small)
        )
      }
    }
  }
}
