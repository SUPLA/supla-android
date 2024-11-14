package org.supla.android.features.details.windowdetail.base.ui.projectorscreen
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
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
import org.supla.android.core.branding.Configuration
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.guardLet
import org.supla.android.features.details.windowdetail.base.data.ProjectorScreenState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.ui.MoveState
import org.supla.android.features.details.windowdetail.base.ui.applyForWindow

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProjectorScreenView(
  windowState: ProjectorScreenState,
  modifier: Modifier = Modifier,
  enabled: Boolean = false,
  onPositionChanging: ((Float) -> Unit)? = null,
  onPositionChanged: ((Float) -> Unit)? = null
) {
  val (windowDimens, updateDimens) = remember { mutableStateOf<RuntimeDimens?>(null) }
  val moveState = remember { mutableStateOf(MoveState()) }
  val logoPainter = painterResource(id = Configuration.ProjectorScreen.LOGO_RESOURCE)
  val logoColor = Configuration.ProjectorScreen.COLOR?.let { colorResource(id = it) }
  val colors = ProjectorScreenColors.standard()

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
    val screenHeight = windowDimens.maxScreenHeight.times(position).div(100)
    val bottomRect = windowDimens.bottomRect.translate(0f, screenHeight)
    val radius = bottomRect.height.div(2)
    val cornerRadius = CornerRadius(radius, radius)

    // screen
    drawRect(colors.screen, topLeft = windowDimens.screenTopLeft, size = Size(windowDimens.screenWidth, screenHeight))
    // Top part
    ProjectorScreenDrawer.drawTopRectWithShadow(windowDimens, colors)
    // Logo
    drawLogo(logoPainter, logoColor, windowDimens, screenHeight, bottomRect)
    // Bottom part
    drawRoundRect(colors.bottomRect, topLeft = bottomRect.topLeft, size = bottomRect.size, cornerRadius = cornerRadius)
    drawHandle(colors.bottomRect, bottomRect)

    if (windowState.markers.isNotEmpty()) {
      val markerColor = colors.bottomRect.copy(alpha = 0.5f)
      windowState.markers.sortedDescending().forEachIndexed { index, markerPosition ->
        if (index > 0f) {
          val markerScreenHeight = windowDimens.maxScreenHeight.times(markerPosition).div(100)
          val markerBottomRect = windowDimens.bottomRect.translate(0f, markerScreenHeight)
          drawRoundRect(markerColor, topLeft = markerBottomRect.topLeft, size = markerBottomRect.size, cornerRadius = cornerRadius)
          drawHandle(markerColor, markerBottomRect)
        }
      }
    }

    if (enabled.not()) {
      drawRect(colors.disabledOverlay)
    }
  }
}

context(DrawScope)
private fun drawHandle(color: Color, bottomRect: Rect) {
  drawLine(
    color = color,
    start = bottomRect.bottomCenter,
    end = Offset(bottomRect.bottomCenter.x, bottomRect.bottomCenter.y + 6.dp.toPx()),
    strokeWidth = 2.dp.toPx()
  )
  drawCircle(
    color = color,
    center = Offset(bottomRect.bottomCenter.x, bottomRect.bottomCenter.y + 10.dp.toPx()),
    radius = 4.dp.toPx(),
    style = Stroke(width = 2.dp.toPx())
  )
}

context(DrawScope)
private fun drawLogo(logoPainter: Painter, logoColor: Color?, windowDimens: RuntimeDimens, screenHeight: Float, bottomRect: Rect) {
  with(logoPainter) {
    val verticalCorrection = windowDimens.maxScreenHeight.minus(screenHeight)
    clipRect(
      left = windowDimens.logoRect.left,
      top = windowDimens.topRect.bottom,
      right = windowDimens.logoRect.right,
      bottom = bottomRect.top
    ) {
      translate(left = windowDimens.logoRect.left, top = windowDimens.logoRect.top - verticalCorrection) {
        draw(size = windowDimens.logoRect.size, colorFilter = logoColor?.let { ColorFilter.tint(it) }, alpha = 0.2f)
      }
    }
  }
}

private object ProjectorScreenDrawer {
  private val paint = Paint().asFrameworkPaint()

  context(DrawScope)
  fun drawTopRectWithShadow(windowDimens: RuntimeDimens, colors: ProjectorScreenColors) {
    paint.applyForWindow(colors.topRect, colors.shadow)
    drawContext.canvas.nativeCanvas.drawRect(
      windowDimens.topRect.left,
      windowDimens.topRect.top,
      windowDimens.topRect.right,
      windowDimens.topRect.bottom,
      paint
    )
  }
}

object DefaultDimens {
  const val WIDTH = 320f
  const val HEIGHT = 260f
  const val RATIO = WIDTH / HEIGHT

  const val TOP_RECT_HEIGHT = 16f
  const val BOTTOM_RECT_HEIGHT = 8f
  const val BOTTOM_RECT_WIDTH = 304f
  const val SCREEN_WIDTH = 288f

  const val LOGO_WIDTH = Configuration.ProjectorScreen.LOGO_WIDTH
  const val LOGO_HEIGHT = Configuration.ProjectorScreen.LOGO_HEIGHT
  const val LOGO_TOP_MARGIN = 50f
}

data class RuntimeDimens(
  val canvasRect: Rect,
  val scale: Float,
  val topRect: Rect,
  val bottomRect: Rect,
  val screenTopLeft: Offset,
  val screenWidth: Float,
  val maxScreenHeight: Float,
  val logoRect: Rect
) {

  fun getPositionFromState(state: MoveState): Float? {
    val (initial) = guardLet(state.initialPoint) { return null }

    val verticalDiffAsPercentage =
      state.lastPoint.y.minus(initial.y)
        .div(maxScreenHeight)
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
      val scale = canvasRect.width / DefaultDimens.WIDTH
      val topRect = topRect(scale, canvasRect)
      val bottomRect = bottomRect(scale, topRect)

      return RuntimeDimens(
        canvasRect = canvasRect,
        scale = scale,
        topRect = topRect,
        bottomRect = bottomRect,
        screenWidth = DefaultDimens.SCREEN_WIDTH.times(scale),
        screenTopLeft = screenTopOffset(scale, topRect),
        maxScreenHeight = canvasRect.height.minus(topRect.height).minus(bottomRect.height),
        logoRect = logoRect(scale, topRect)
      )
    }

    private fun canvasRect(viewSize: IntSize): Rect {
      val size = getSize(viewSize = viewSize)
      return Rect(
        Offset(viewSize.width.minus(size.width).div(2), viewSize.height.minus(size.height).div(2)),
        size
      )
    }

    private fun topRect(scale: Float, canvasRect: Rect): Rect {
      val size = Size(canvasRect.width, DefaultDimens.TOP_RECT_HEIGHT.times(scale))
      return Rect(canvasRect.topLeft, size)
    }

    private fun bottomRect(scale: Float, topRect: Rect): Rect {
      val size = Size(DefaultDimens.BOTTOM_RECT_WIDTH.times(scale), DefaultDimens.BOTTOM_RECT_HEIGHT.times(scale))
      val left = topRect.left.plus(topRect.width.minus(size.width).div(2))
      val topLeft = Offset(left, topRect.bottom)
      return Rect(topLeft, size)
    }

    private fun logoRect(scale: Float, topRect: Rect): Rect {
      val logoWidth = DefaultDimens.LOGO_WIDTH.times(scale)
      val logoHeight = DefaultDimens.LOGO_HEIGHT.times(scale)
      val left = topRect.left.plus(topRect.width.minus(logoWidth).div(2))
      val topLeft = Offset(left, topRect.bottom.plus(DefaultDimens.LOGO_TOP_MARGIN.times(scale)))
      return Rect(topLeft, Size(logoWidth, logoHeight))
    }

    private fun screenTopOffset(scale: Float, topRect: Rect): Offset {
      val screenWidth = DefaultDimens.SCREEN_WIDTH.times(scale)
      return Offset(topRect.left.plus(topRect.width.minus(screenWidth).div(2)), topRect.bottom)
    }

    private fun getSize(viewSize: IntSize): Size {
      val canvasRatio = viewSize.width / viewSize.height.toFloat()
      return if (canvasRatio > DefaultDimens.RATIO) {
        Size(viewSize.height.times(DefaultDimens.RATIO), viewSize.height.toFloat())
      } else {
        Size(viewSize.width.toFloat(), viewSize.width.div(DefaultDimens.RATIO))
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
        ProjectorScreenView(
          windowState = ProjectorScreenState(WindowGroupedValue.Similar(75f)),
          enabled = true,
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
        ProjectorScreenView(
          windowState = ProjectorScreenState(WindowGroupedValue.Similar(50f), markers = listOf(0f, 10f, 50f, 100f)),
          modifier = Modifier
            .fillMaxSize()
        )
      }
      Box(
        modifier = Modifier
          .width(200.dp)
          .height(100.dp)
          .background(color = colorResource(id = R.color.background))
      ) {
        ProjectorScreenView(
          windowState = ProjectorScreenState(WindowGroupedValue.Similar(45f)),
          enabled = true,
          modifier = Modifier
            .fillMaxSize()
            .padding(all = Distance.small)
        )
      }
    }
  }
}
