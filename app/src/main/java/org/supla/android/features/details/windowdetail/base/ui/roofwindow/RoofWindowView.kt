package org.supla.android.features.details.windowdetail.base.ui.roofwindow
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

import android.graphics.Camera
import android.graphics.Matrix
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
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.asAndroidPath
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
import org.supla.android.extensions.guardLet
import org.supla.android.features.details.windowdetail.base.data.RoofWindowState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.ui.MoveState

private const val MAX_OPENED_OFFSET = 35f

private val WINDOW_DIMENS = object {
  val width = 231f
  val height = 336f
  val ratio = width / height
  val widthCorrection = 0.69f

  val windowFrameWidth = 10f
  val windowTopCoverWidth = 10f
}

private val windowShadowRadius = 4.dp
private val windowPaint = Paint().asFrameworkPaint().apply {
  style = android.graphics.Paint.Style.FILL
  strokeCap = android.graphics.Paint.Cap.SQUARE
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RoofWindowView(
  windowState: RoofWindowState,
  modifier: Modifier = Modifier,
  colors: RoofWindowColors = RoofWindowColors.standard(),
  onPositionChanging: ((Float) -> Unit)? = null,
  onPositionChanged: ((Float) -> Unit)? = null
) {
  val (windowDimens, updateDimens) = remember { mutableStateOf<RoofWindowDimens?>(null) }
  val staticCamera = remember(windowDimens) { windowDimens?.let { Camera().asStatic(windowDimens) } }
  val dynamicCamera = remember(windowDimens, windowState.position) {
    windowDimens?.let { Camera().asDynamic(windowDimens, windowState) }
  }
  val moveState = remember { mutableStateOf(MoveState()) }

  Canvas(
    modifier = modifier
      .onSizeChanged { updateDimens(RoofWindowDimens.make(it)) }
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
    if (windowDimens == null || staticCamera == null || dynamicCamera == null) {
      return@Canvas // Skip drawing when view size is not set yet
    }

    windowPaint.apply {
      color = colors.window.toArgb()
      setShadowLayer(windowShadowRadius.toPx(), 0f, 2.dp.toPx(), colors.shadow.toArgb())
    }
    drawContext.canvas.nativeCanvas.drawPath(
      RoofWindowDrawer.windowShadowPath(windowDimens, staticCamera).asAndroidPath(),
      windowPaint
    )

    drawPath(RoofWindowDrawer.windowCoverableJambPath(windowDimens, staticCamera), color = colors.window)
    drawContext.canvas.nativeCanvas.drawPath(
      RoofWindowDrawer.sashPath(windowDimens, dynamicCamera).asAndroidPath(),
      windowPaint
    )
    drawPath(RoofWindowDrawer.sashGlassPath(windowDimens, dynamicCamera), color = colors.glassTop.copy(alpha = 0.4f))
    drawPath(RoofWindowDrawer.windowJambPath(windowDimens, staticCamera), color = colors.window)
  }
}

private object RoofWindowDrawer {

  private val path = Path()
  private val secondPath = Path()
  private val operationalPath = Path()

  private val matrix = Matrix()

  private val pointsBuffer26 = FloatArray(26)
  private val pointsBuffer8 = FloatArray(8)
  private val secondPointsBuffer8 = FloatArray(8)

  fun windowJambPath(windowDimens: RoofWindowDimens, staticCamera: Camera): Path {
    windowDimens.windowJambPoints.copyTo(pointsBuffer26)

    matrix.reset()
    staticCamera.getMatrix(matrix)
    matrix.mapPoints(pointsBuffer26)

    path.reset()
    pointsBuffer26.applyToPath(path)
    return path
  }

  fun windowCoverableJambPath(windowDimens: RoofWindowDimens, staticCamera: Camera): Path {
    windowDimens.windowCoverableJambPoints.copyTo(pointsBuffer8)

    matrix.reset()
    staticCamera.getMatrix(matrix)
    matrix.mapPoints(pointsBuffer8)

    path.reset()
    pointsBuffer8.applyToPath(path)
    return path
  }

  fun sashPath(windowDimens: RoofWindowDimens, dynamicCamera: Camera): Path {
    windowDimens.sashOutsidePoints.copyTo(pointsBuffer8)
    windowDimens.sashInsidePoints.copyTo(secondPointsBuffer8)

    matrix.reset()
    dynamicCamera.getMatrix(matrix)

    matrix.mapPoints(pointsBuffer8)
    matrix.mapPoints(secondPointsBuffer8)

    path.reset()
    pointsBuffer8.applyToPath(path)
    secondPointsBuffer8.applyToPath(secondPath)
    operationalPath.op(path, secondPath, PathOperation.Difference)

    return operationalPath
  }

  fun sashGlassPath(windowDimens: RoofWindowDimens, staticCamera: Camera): Path {
    windowDimens.sashInsidePoints.copyTo(secondPointsBuffer8)

    matrix.reset()
    staticCamera.getMatrix(matrix)
    matrix.mapPoints(secondPointsBuffer8)

    path.reset()
    secondPointsBuffer8.applyToPath(path)
    return path
  }

  fun windowShadowPath(windowDimens: RoofWindowDimens, dynamicCamera: Camera): Path {
    windowDimens.framePoints.copyTo(pointsBuffer8)
    windowDimens.sashOutsidePoints.copyTo(secondPointsBuffer8)

    matrix.reset()
    dynamicCamera.getMatrix(matrix)

    matrix.mapPoints(pointsBuffer8)
    matrix.mapPoints(secondPointsBuffer8)

    path.reset()
    pointsBuffer8.applyToPath(path)
    secondPointsBuffer8.applyToPath(secondPath)
    operationalPath.op(path, secondPath, PathOperation.Difference)

    return operationalPath
  }
}

private data class RoofWindowDimens(
  val canvasRect: Rect,

  val framePoints: List<Offset>,
  val windowJambPoints: List<Offset>,
  val windowCoverableJambPoints: List<Offset>,
  val sashOutsidePoints: List<Offset>,
  val sashInsidePoints: List<Offset>,
) {

  fun getPositionFromState(state: MoveState): Float? {
    val (initialY) = guardLet(state.initialPoint?.y) { return null }

    val positionDiffAsPercentage =
      state.lastPoint.y.minus(initialY)
        .div(canvasRect.height.div(2))
        .times(100f)

    return state.initialVerticalPercentage.plus(positionDiffAsPercentage).let {
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

    fun make(viewSize: IntSize): RoofWindowDimens {
      val canvasRect = canvasRect(viewSize)

      val scale = canvasRect.width / WINDOW_DIMENS.width
      val windowFrameWidth = WINDOW_DIMENS.windowFrameWidth.times(scale)
      val windowTopCoverWidth = WINDOW_DIMENS.windowTopCoverWidth.times(scale)

      return RoofWindowDimens(
        canvasRect = canvasRect,
        framePoints = RoofWindowDimensBuilder.framePoints(canvasRect),
        windowJambPoints = RoofWindowDimensBuilder.windowJambPoints(canvasRect, windowFrameWidth, windowTopCoverWidth),
        windowCoverableJambPoints = RoofWindowDimensBuilder.windowCoverableJambPoints(canvasRect, windowFrameWidth),
        sashOutsidePoints = RoofWindowDimensBuilder.windowSashOutsidePoints(canvasRect, windowFrameWidth),
        sashInsidePoints = RoofWindowDimensBuilder.windowSashInsidePoints(canvasRect, windowFrameWidth, windowTopCoverWidth),
      )
    }

    private fun canvasRect(viewSize: IntSize): Rect {
      val size = getSize(viewSize = viewSize)
      return Rect(
        Offset(viewSize.width.minus(size.width).div(2), 0f),
        size
      )
    }

    private fun getSize(viewSize: IntSize): Size {
      val canvasRatio = viewSize.width / viewSize.height.toFloat()
      return if (canvasRatio > WINDOW_DIMENS.ratio) {
        Size(viewSize.height.times(WINDOW_DIMENS.ratio), viewSize.height.toFloat())
      } else {
        Size(viewSize.width.toFloat(), viewSize.width.div(WINDOW_DIMENS.ratio)).times(WINDOW_DIMENS.widthCorrection)
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
        RoofWindowView(
          windowState = RoofWindowState(WindowGroupedValue.Similar(75f)),
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
        RoofWindowView(
          windowState = RoofWindowState(WindowGroupedValue.Similar(50f), markers = listOf(0f, 10f, 50f, 100f)),
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
        RoofWindowView(
          windowState = RoofWindowState(WindowGroupedValue.Similar(25f)),
          modifier = Modifier
            .fillMaxSize()
            .padding(all = Distance.small)
        )
      }
    }
  }
}

private fun FloatArray.applyToPath(path: Path) {
  path.reset()
  for (i in 0 until (size - 1) step 2) {
    if (i == 0) {
      path.moveTo(this[0], this[1])
    } else {
      path.lineTo(this[i], this[i + 1])
    }
  }
}

private fun List<Offset>.copyTo(destination: FloatArray) {
  if (size.times(2) != destination.size) {
    throw IllegalStateException("Destination has different size than source")
  }

  var i = 0
  forEach {
    destination[i++] = it.x
    destination[i++] = it.y
  }
}

private fun Camera.asStatic(windowDimens: RoofWindowDimens): Camera {
  setLocation(0f, 0f, windowDimens.canvasRect.size.width.times(-1))
  translate(windowDimens.canvasRect.size.center.x, windowDimens.canvasRect.size.center.y * -1, 0f)
  rotateZ(180f)
  rotateY(210f)
  rotateX(210f)

  return this
}

private fun Camera.asDynamic(windowDimens: RoofWindowDimens, roofWindowState: RoofWindowState): Camera {
  setLocation(0f, 0f, windowDimens.canvasRect.size.width.times(-1))
  translate(windowDimens.canvasRect.size.center.x, windowDimens.canvasRect.size.center.y * -1, 0f)
  rotateZ(180f)
  rotateY(210f)
  rotateX(210f + roofWindowState.openedOffset)

  return this
}

val RoofWindowState.openedOffset: Float
  get() = MAX_OPENED_OFFSET.times(100f.minus(position.value)).div(100f)
