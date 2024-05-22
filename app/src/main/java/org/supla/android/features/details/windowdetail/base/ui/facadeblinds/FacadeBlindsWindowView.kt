package org.supla.android.features.details.windowdetail.base.ui.facadeblinds
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
import org.supla.android.extensions.toPx
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.facadeblinds.FacadeBlindMarker
import org.supla.android.features.details.windowdetail.base.data.facadeblinds.FacadeBlindWindowState
import org.supla.android.features.details.windowdetail.base.ui.MoveState
import org.supla.android.features.details.windowdetail.base.ui.windowview.SlatDimens
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDimens
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDimensBase
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDrawerBase
import kotlin.math.abs
import kotlin.math.ceil

val slatShadowRadius = 1.dp
private const val HORIZONTAL_MOVE_HYSTERESIS = 20

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FacadeBlindsWindowView(
  windowState: FacadeBlindWindowState,
  modifier: Modifier = Modifier,
  colors: FacadeBlindColors = FacadeBlindColors.standard(),
  onPositionChanging: ((tilt: Float, position: Float) -> Unit)? = null,
  onPositionChanged: ((tilt: Float, position: Float) -> Unit)? = null
) {
  val (windowDimens, updateDimens) = remember { mutableStateOf<RuntimeDimens?>(null) }
  val moveState = remember { mutableStateOf(MoveState()) }

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
                  initialVerticalPercentage = windowState.position.value,
                  initialHorizontalPercentage = windowState.slatTilt?.value ?: 0f,
                  horizontalAllowed = false
                )
              }
            }

            MotionEvent.ACTION_MOVE -> {
              val horizontalAllowed = moveState.value.initialPoint?.let {
                abs(it.x - event.x) > HORIZONTAL_MOVE_HYSTERESIS.dp.toPx()
              } ?: false

              moveState.value = moveState.value.copy(
                lastPoint = Offset(event.x, event.y),
                horizontalAllowed = moveState.value.horizontalAllowed || horizontalAllowed
              )
              windowDimens
                .getPositionFromState(moveState.value)
                ?.let { offset -> onPositionChanging?.let { it(offset.x, offset.y) } }
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
  }
}

private object WindowDrawer : WindowDrawerBase<RuntimeDimens, FacadeBlindWindowState, FacadeBlindColors>() {

  private val maxTilt = 8.dp.toPx()
  private val tiltInitialCorrection = 1.dp.toPx()
  private const val TILT_RANGE = 180f
  private const val TILT_HALF_RANGE = 90f

  context (DrawScope)
  override fun drawShadowingElements(windowState: FacadeBlindWindowState, runtimeDimens: RuntimeDimens, colors: FacadeBlindColors) {
    val topCorrection = windowState.position.value.div(100f)
      .times(runtimeDimens.movementLimit)
      .plus(runtimeDimens.slatDistance.times(1.5f)) // Needed to align slats bottom with window bottom

    // When the roller shutter position is bigger then bottom position we need to start "closing slats".
    // Here the available space for "opened" slats is calculated
    val slatsCount = runtimeDimens.slats.size
    val progression = windowState.slatTiltDegrees?.let { if (it > 50) 1..slatsCount else null } ?: (slatsCount downTo 1)
    for (i in progression) {
      drawSlat(
        topCorrection = topCorrection - runtimeDimens.movementLimit,
        rect = runtimeDimens.slats[i - 1],
        runtimeDimens = runtimeDimens,
        colors = colors,
        tilt = windowState.slatTiltDegrees
      )
    }
  }

  context(DrawScope)
  override fun drawMarkers(
    windowState: FacadeBlindWindowState,
    runtimeDimens: RuntimeDimens,
    colors: FacadeBlindColors
  ) {
    windowState.markers.forEach { marker ->
      val topPosition = runtimeDimens.topLineRect.bottom
        .plus(runtimeDimens.movementLimit.minus(runtimeDimens.slatDistance).times(marker.position).div(100f))

      val tilt0 = windowState.tilt0Angle ?: FacadeBlindWindowState.DEFAULT_TILT_0_ANGLE
      val tilt100 = windowState.tilt100Angle ?: FacadeBlindWindowState.DEFAULT_TILT_100_ANGLE
      val degrees = tilt0.plus(tilt100.minus(tilt0).times(marker.tilt).div(100))
      val trimmed = SlatTiltSliderDimens.trimAngle(degrees)

      drawMarker(
        Offset(runtimeDimens.windowRect.left, topPosition),
        trimmed,
        runtimeDimens,
        colors
      )
    }
  }

  context(DrawScope)
  private fun drawSlat(
    topCorrection: Float,
    rect: Rect,
    runtimeDimens: RuntimeDimens,
    colors: FacadeBlindColors,
    tilt: Float?
  ) {
    val horizontalSlatCorrection: Float
    val verticalSlatCorrection: Float
    if (tilt != null) {
      val correctedTilt = if (tilt <= TILT_HALF_RANGE) tilt else TILT_RANGE - tilt
      horizontalSlatCorrection = maxTilt.times(correctedTilt).div(TILT_HALF_RANGE).div(2)
        .plus(tiltInitialCorrection)
        .let { if (tilt <= TILT_HALF_RANGE) it else -it }
      val maxSlatCorrection = rect.height.minus(2.dp.toPx())
      verticalSlatCorrection = maxSlatCorrection.times(correctedTilt).div(TILT_HALF_RANGE).div(2)
    } else {
      horizontalSlatCorrection = tiltInitialCorrection
      verticalSlatCorrection = 0f
    }

    val bottom = rect.bottom + topCorrection - verticalSlatCorrection
    if (bottom < runtimeDimens.topLineRect.bottom) {
      // skip slats over screen
      return
    }
    val top = rect.top.plus(topCorrection).plus(verticalSlatCorrection).let {
      if (it < runtimeDimens.topLineRect.bottom) runtimeDimens.topLineRect.bottom else it
    }

    path.reset()
    path.moveTo(rect.left + horizontalSlatCorrection, top)
    path.lineTo(rect.right - horizontalSlatCorrection, top)
    path.lineTo(rect.right + horizontalSlatCorrection, bottom)
    path.lineTo(rect.left - horizontalSlatCorrection, bottom)
    path.close()

    paint.applyForSlat(colors)
    drawContext.canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
    drawPath(path = path, color = colors.slatBorder, style = Stroke(width = 1.dp.toPx()))
  }

  context (DrawScope)
  private fun drawMarker(offset: Offset, tilt: Float, runtimeDimens: RuntimeDimens, windowColors: FacadeBlindColors) {
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
private fun NativePaint.applyForSlat(colors: FacadeBlindColors) {
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
  val slats: List<Rect>,
  val slatDistance: Float,
  val slatsDistances: Float,
  val markerInfoRadius: Float
) : WindowDimensBase {

  companion object {
    const val MARKER_INFO_RADIUS = 12f

    operator fun invoke(viewSize: IntSize): RuntimeDimens {
      val canvasRect = WindowDimensBase.canvasRect(viewSize = viewSize)
      val scale = canvasRect.width / WindowDimens.WIDTH
      val topLineRect = Rect(
        Offset(canvasRect.left, canvasRect.top),
        Size(canvasRect.width, WindowDimens.TOP_LINE_HEIGHT.times(scale))
      )
      val windowRect = WindowDimensBase.windowRect(scale, canvasRect, topLineRect)
      val slats = slats(scale, canvasRect, topLineRect)
      val slatDistance = SlatDimens.SLAT_DISTANCE.times(scale)

      return RuntimeDimens(
        canvasRect = canvasRect,
        topLineRect = topLineRect,
        windowRect = windowRect,
        slats = slats,
        scale = scale,
        slatDistance = slatDistance,
        slatsDistances = (slats.size - 1) * slatDistance,
        markerInfoRadius = MARKER_INFO_RADIUS.times(scale),
      )
    }

    private fun slats(scale: Float, canvasRect: Rect, topLineRect: Rect): List<Rect> {
      val slatHorizontalMargin = SlatDimens.SLAT_HORIZONTAL_MARGIN.times(scale)
      val slatSize = Size(
        canvasRect.width.minus(slatHorizontalMargin.times(2)),
        SlatDimens.SLAT_HEIGHT.times(scale)
      )

      val slatSpace = slatSize.height.minus(2.dp.toPx())
      val slatsCount = ceil(canvasRect.height.minus(topLineRect.height).div(slatSpace)).toInt()
      val top = topLineRect.bottom - slatSpace

      return mutableListOf<Rect>().also {
        for (i in 0 until slatsCount) {
          it.add(Rect(Offset(canvasRect.left.plus(slatHorizontalMargin), top.plus(slatSpace.times(i))), slatSize))
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
        FacadeBlindsWindowView(
          windowState = FacadeBlindWindowState(WindowGroupedValue.Similar(75f)),
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
        val markers = listOf(FacadeBlindMarker(0f, 50f), FacadeBlindMarker(35f, 25f))
        FacadeBlindsWindowView(
          windowState = FacadeBlindWindowState(WindowGroupedValue.Similar(75f), markers = markers),
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
        FacadeBlindsWindowView(
          windowState = FacadeBlindWindowState(WindowGroupedValue.Similar(25f)),
          modifier = Modifier
            .fillMaxSize()
            .padding(all = Distance.small)
        )
      }
    }
  }
}
