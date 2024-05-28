package org.supla.android.features.details.windowdetail.base.ui.rollershutter
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
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
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
import org.supla.android.features.details.windowdetail.base.data.RollerShutterWindowState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.ui.MoveState
import org.supla.android.features.details.windowdetail.base.ui.applyForSlat
import org.supla.android.features.details.windowdetail.base.ui.windowview.SlatDimens
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDimens
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDimensBase
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDrawerBase
import kotlin.math.ceil

val slatShadowRadius = 1.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RollerShutterWindowView(
  windowState: RollerShutterWindowState,
  modifier: Modifier = Modifier,
  enabled: Boolean = false,
  onPositionChanging: ((Float) -> Unit)? = null,
  onPositionChanged: ((Float) -> Unit)? = null
) {
  val (windowDimens, updateDimens) = remember { mutableStateOf<RuntimeDimens?>(null) }
  val moveState = remember { mutableStateOf(MoveState()) }
  val colors = RollerShutterColors.standard()

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
                  initialVerticalPercentage = windowState.position.value
                )
              }
            }

            MotionEvent.ACTION_MOVE -> {
              moveState.value = moveState.value.copy(lastPoint = Offset(event.x, event.y))
              windowDimens
                .getPositionFromState(moveState.value)
                ?.let { position -> onPositionChanging?.let { it(position.y) } }
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
    if (enabled.not()) {
      drawRect(colors.disabledOverlay)
    }
  }
}

private object WindowDrawer : WindowDrawerBase<RuntimeDimens, RollerShutterWindowState, RollerShutterColors>() {

  context (DrawScope)
  override fun drawShadowingElements(windowState: RollerShutterWindowState, runtimeDimens: RuntimeDimens, colors: RollerShutterColors) {
    // 0 ... 1 -> 0 ... 100%
    val position = if (windowState.markers.isEmpty()) windowState.position.value else windowState.markers.max()
    val positionCorrectedByBottomPosition = position
      .div(windowState.bottomPosition)
      .let { if (it > 1) 1f else it }

    val topCorrection = positionCorrectedByBottomPosition
      .times(runtimeDimens.movementLimit)
      .minus(runtimeDimens.slatsDistances)
      .plus(runtimeDimens.slatDistance.times(1.5f)) // Needed to align slats bottom with window bottom

    // When the roller shutter position is bigger then bottom position we need to start "closing slats".
    // Here the available space for "opened" slats is calculated
    val availableSpaceForSlatDistances = when {
      position > windowState.bottomPosition ->
        runtimeDimens.slatsDistances
          .times(100f.minus(position))
          .div(100f.minus(windowState.bottomPosition))

      else -> null
    }
    var slatsCorrection = availableSpaceForSlatDistances?.let { runtimeDimens.slatsDistances.minus(it) } ?: 0f

    runtimeDimens.slats.forEachIndexed { idx, slat ->
      if (availableSpaceForSlatDistances != null) {
        val summarizedDistance = idx.times(runtimeDimens.slatDistance)
        // When the summarized distance used between slats is bigger then available,
        // add additional slat correction, to make slats displayed next to each other (without distance)
        if (summarizedDistance > availableSpaceForSlatDistances) {
          slatsCorrection -= summarizedDistance.minus(availableSpaceForSlatDistances).let {
            // Remove max one slat distance for each slat
            if (it > runtimeDimens.slatDistance) runtimeDimens.slatDistance else it
          }
        }
      }
      drawSlat(
        topCorrection = topCorrection - runtimeDimens.movementLimit + slatsCorrection,
        rect = slat,
        runtimeDimens = runtimeDimens,
        colors = colors
      )
    }
  }

  context(DrawScope)
  override fun drawMarkers(windowState: RollerShutterWindowState, runtimeDimens: RuntimeDimens, colors: RollerShutterColors) {
    windowState.markers.forEach { position ->
      val topPosition = runtimeDimens.topLineRect.bottom
        .plus(runtimeDimens.movementLimit.minus(runtimeDimens.slatDistance).times(position).div(100f))

      drawMarker(Offset(0f, topPosition), runtimeDimens, colors)
    }
  }

  context(DrawScope)
  private fun drawSlat(topCorrection: Float, rect: Rect, runtimeDimens: RuntimeDimens, colors: RollerShutterColors) {
    val bottom = rect.bottom + topCorrection
    if (bottom < runtimeDimens.topLineRect.bottom) {
      // skip slats over screen
      return
    }
    val top = rect.top.plus(topCorrection).let {
      if (it < runtimeDimens.topLineRect.bottom) runtimeDimens.topLineRect.bottom else it
    }

    path.reset()
    path.moveTo(rect.left, top)
    path.lineTo(rect.right, top)
    path.lineTo(rect.right, bottom)
    path.lineTo(rect.left, bottom)
    path.close()

    paint.applyForSlat(colors.slatBackground, colors.shadow)
    drawContext.canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
    drawPath(path = path, color = colors.slatBorder, style = Stroke(width = 1.dp.toPx()))
  }

  context (DrawScope)
  private fun drawMarker(offset: Offset, runtimeDimens: RuntimeDimens, windowColors: RollerShutterColors) {
    runtimeDimens.markerPath.translate(offset)
    drawPath(
      path = runtimeDimens.markerPath,
      color = windowColors.markerBackground,
      style = Fill
    )
    drawPath(
      path = runtimeDimens.markerPath,
      color = windowColors.markerBorder,
      style = Stroke(width = 1.dp.toPx())
    )
    runtimeDimens.markerPath.translate(offset.times(-1f))
  }
}

private data class RuntimeDimens(
  override val canvasRect: Rect,
  override val topLineRect: Rect,
  override val windowRect: Rect,
  override val scale: Float,
  val slats: List<Rect>,
  val slatDistance: Float,
  val slatsDistances: Float,
  val markerPath: Path
) : WindowDimensBase {

  companion object {
    const val MARKER_HEIGHT = 8f
    const val MARKER_WIDTH = 28f

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
        markerPath = getMarkerPath(scale)
      )
    }

    private fun slats(scale: Float, canvasRect: Rect, topLineRect: Rect): List<Rect> {
      val slatHorizontalMargin = SlatDimens.SLAT_HORIZONTAL_MARGIN.times(scale)
      val slatSize = Size(
        canvasRect.width.minus(slatHorizontalMargin.times(2)),
        SlatDimens.SLAT_HEIGHT.times(scale)
      )

      val slatDistance = SlatDimens.SLAT_DISTANCE.times(scale)
      val slatSpace = slatSize.height.plus(slatDistance)
      val slatsCount = ceil(canvasRect.height.minus(topLineRect.height).div(slatSize.height)).toInt()
      val top = topLineRect.bottom - slatSize.height

      return mutableListOf<Rect>().also {
        for (i in 0 until slatsCount) {
          it.add(Rect(Offset(canvasRect.left.plus(slatHorizontalMargin), top.plus(slatSpace.times(i))), slatSize))
        }
      }
    }

    private fun getMarkerPath(scale: Float): Path {
      val startsAt = WindowDimens.WINDOW_HORIZONTAL_MARGIN.times(scale)

      val height = MARKER_HEIGHT.times(scale)
      val halfHeight = height.div(2f)
      val width = MARKER_WIDTH.times(scale)
      val path = Path()
      path.moveTo(startsAt, 0f)
      path.lineTo(startsAt + halfHeight, -halfHeight) // (top) -> /
      path.lineTo(startsAt + width, -halfHeight) // (top) -> /‾‾‾
      path.lineTo(startsAt + width, halfHeight) // (top) -> /‾‾‾|
      path.lineTo(startsAt + halfHeight, halfHeight) // (bottom) -> ___|
      path.close()

      return path
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
        RollerShutterWindowView(
          windowState = RollerShutterWindowState(WindowGroupedValue.Similar(75f)),
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
        RollerShutterWindowView(
          windowState = RollerShutterWindowState(WindowGroupedValue.Similar(75f), markers = listOf(0f, 10f, 50f, 100f)),
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
        RollerShutterWindowView(
          windowState = RollerShutterWindowState(WindowGroupedValue.Similar(25f)),
          modifier = Modifier
            .fillMaxSize()
            .padding(all = Distance.small)
        )
      }
    }
  }
}
