package org.supla.android.features.details.windowdetail.base.ui.garagedoor
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.guardLet
import org.supla.android.features.details.windowdetail.base.data.GarageDoorState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.ui.MoveState
import org.supla.android.features.details.windowdetail.base.ui.applyForWindow
import kotlin.math.ceil

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GarageDoorScreenView(
  windowState: GarageDoorState,
  modifier: Modifier = Modifier,
  colors: GarageDoorScreenColors = GarageDoorScreenColors.standard(),
  onPositionChanging: ((Float) -> Unit)? = null,
  onPositionChanged: ((Float) -> Unit)? = null
) {
  val (windowDimens, updateDimens) = remember { mutableStateOf<RuntimeDimens?>(null) }
  val moveState = remember { mutableStateOf(MoveState()) }
  val garageContentPainter = painterResource(id = R.drawable.garage_content)

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

    val position = if (windowState.markers.isEmpty()) windowState.position.value else windowState.markers.max()
    val topCorrection = windowDimens.movementMaxHeight.times(100f.minus(position)).div(100)

    GarageDoorDrawer.drawGarageBuilding(windowDimens, colors)
    GarageDoorDrawer.drawGarageContent(garageContentPainter, windowDimens)
    GarageDoorDrawer.drawSlats(windowDimens, colors, topCorrection)

    for (marker in windowState.markers) {
      val markerCorrection = windowDimens.movementMaxHeight.times(marker).div(100)
      translate(top = markerCorrection) {
        drawPath(windowDimens.markerPath, colors.markerBackground)
        drawPath(windowDimens.markerPath, colors.markerBorder, style = Stroke(1.dp.toPx()))
      }
    }
  }
}

private object GarageDoorDrawer {

  private val path: Path = Path()
  private val paint = Paint().asFrameworkPaint()

  context(DrawScope)
  fun drawGarageBuilding(dimens: RuntimeDimens, colors: GarageDoorScreenColors) {
    path.reset()
    path.moveTo(dimens.canvasRect.left, dimens.canvasRect.bottom)
    path.lineTo(dimens.canvasRect.left, dimens.canvasRect.bottom.minus(dimens.wallHeight))
    path.lineTo(dimens.canvasRect.center.x, dimens.canvasRect.top)
    path.lineTo(dimens.canvasRect.right, dimens.canvasRect.bottom.minus(dimens.wallHeight))
    path.lineTo(dimens.canvasRect.right, dimens.canvasRect.bottom)
    path.close()

    paint.applyForWindow(colors.building, colors.shadow)
    drawContext.canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
  }

  context(DrawScope)
  fun drawGarageContent(painter: Painter, dimens: RuntimeDimens) {
    path.reset()
    path.addRoundRect(RoundRect(dimens.doorRect, dimens.doorRadius, dimens.doorRadius))
    path.close()

    with(painter) {
      clipPath(path) {
        translate(left = dimens.doorRect.left, top = dimens.doorRect.top) {
          draw(size = dimens.doorRect.size)
        }
      }
    }
  }

  context(DrawScope)
  fun drawSlats(dimens: RuntimeDimens, colors: GarageDoorScreenColors, topCorrection: Float) {
    path.reset()
    path.addRoundRect(RoundRect(dimens.doorRect, dimens.doorRadius, dimens.doorRadius))
    path.close()

    clipPath(path) {
      for (slat in dimens.slats) {
        val topLeft = Offset(slat.left, slat.top.minus(topCorrection))
        drawRect(colors.slatBackground, topLeft, slat.size)
        drawRect(color = colors.slatBorder, topLeft, slat.size, style = Stroke(1.dp.toPx()))
      }
    }
  }
}

data class RuntimeDimens(
  val canvasRect: Rect,
  val scale: Float,
  val wallHeight: Float,
  val slats: List<Rect>,
  val doorRect: Rect,
  val doorRadius: CornerRadius,
  val movementMaxHeight: Float,
  val markerPath: Path
) {

  fun getPositionFromState(state: MoveState): Float? {
    val (initial) = guardLet(state.initialPoint) { return null }

    val verticalDiffAsPercentage =
      state.lastPoint.y.minus(initial.y)
        .div(movementMaxHeight)
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
    private const val WIDTH = 304f
    private const val HEIGHT = 304f
    private const val RATIO = WIDTH / HEIGHT

    private const val WALL_HEIGHT = 216f
    private const val DOOR_WIDTH = 256f
    private const val DOOR_HEIGHT = 196f
    private const val SLAT_HEIGHT = 24f
    private const val DOOR_RADIUS = 4f

    private const val MARKER_HEIGHT = 8f
    private const val MARKER_WIDTH = 28f

    fun make(viewSize: IntSize): RuntimeDimens {
      val canvasRect = canvasRect(viewSize)
      val scale = canvasRect.width / WIDTH
      val doorRect = doorRect(scale, canvasRect)

      return RuntimeDimens(
        canvasRect = canvasRect,
        scale = scale,
        wallHeight = WALL_HEIGHT.times(scale),
        slats = slats(scale, doorRect),
        doorRect = doorRect,
        doorRadius = CornerRadius(DOOR_RADIUS.times(scale)),
        movementMaxHeight = doorRect.height.minus(SLAT_HEIGHT.times(scale)),
        markerPath = getMarkerPath(scale, doorRect)
      )
    }

    private fun canvasRect(viewSize: IntSize): Rect {
      val size = getSize(viewSize = viewSize)
      return Rect(
        Offset(viewSize.width.minus(size.width).div(2), viewSize.height.minus(size.height).div(2)),
        size
      )
    }

    private fun doorRect(scale: Float, canvasRect: Rect): Rect {
      val doorWidth = DOOR_WIDTH.times(scale)
      val doorHeight = DOOR_HEIGHT.times(scale)
      val left = canvasRect.center.x.minus(doorWidth.div(2))
      val top = canvasRect.bottom.minus(doorHeight)
      val topLeft = Offset(left, top)
      return Rect(topLeft, Size(doorWidth, doorHeight))
    }

    private fun slats(scale: Float, doorRect: Rect): List<Rect> {
      val slatWidth = doorRect.width
      val slatHeight = SLAT_HEIGHT.times(scale)
      val slatsCount = ceil(doorRect.height.div(slatHeight)).toInt()

      val top = doorRect.bottom.minus(slatHeight)
      val left = doorRect.left

      return mutableListOf<Rect>().apply {
        for (i in 0 until slatsCount) {
          add(Rect(Offset(left, top.minus(slatHeight.times(i))), Size(slatWidth, slatHeight)))
        }
      }
    }

    private fun getMarkerPath(scale: Float, doorRect: Rect): Path {
      val top = doorRect.top.plus(SLAT_HEIGHT.times(scale))
      val left = doorRect.left

      val height = MARKER_HEIGHT.times(scale)
      val halfHeight = height.div(2f)
      val width = MARKER_WIDTH.times(scale)
      val path = Path()
      path.moveTo(left, top)
      path.lineTo(left + halfHeight, top - halfHeight) // (top) -> /
      path.lineTo(left + width, top - halfHeight) // (top) -> /‾‾‾
      path.lineTo(left + width, top + halfHeight) // (top) -> /‾‾‾|
      path.lineTo(left + halfHeight, top + halfHeight) // (bottom) -> ___|
      path.close()

      return path
    }

    private fun getSize(viewSize: IntSize): Size {
      val canvasRatio = viewSize.width / viewSize.height.toFloat()
      return if (canvasRatio > RATIO) {
        Size(viewSize.height.times(RATIO), viewSize.height.toFloat())
      } else {
        Size(viewSize.width.toFloat(), viewSize.width.div(RATIO))
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
        GarageDoorScreenView(
          windowState = GarageDoorState(WindowGroupedValue.Similar(75f)),
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
        GarageDoorScreenView(
          windowState = GarageDoorState(WindowGroupedValue.Similar(50f), markers = listOf(0f, 25f, 50f, 75f, 100f)),
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
        GarageDoorScreenView(
          windowState = GarageDoorState(WindowGroupedValue.Similar(25f)),
          modifier = Modifier
            .fillMaxSize()
            .padding(all = Distance.small)
        )
      }
    }
  }
}
