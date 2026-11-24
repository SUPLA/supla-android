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

import android.graphics.Paint
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.defaultFont
import org.supla.android.extensions.drawPercentage
import org.supla.android.extensions.toPx
import org.supla.android.features.details.windowdetail.base.data.ShadingBlindMarker
import org.supla.android.features.details.windowdetail.base.data.ValueDestination
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.facadeblinds.FacadeBlindWindowState
import org.supla.android.features.details.windowdetail.base.ui.MoveState
import org.supla.android.features.details.windowdetail.base.ui.windowview.SlatDimens
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDimens
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDimensBase
import org.supla.android.features.details.windowdetail.base.ui.windowview.WindowDrawerBase
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueUnit
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import org.supla.android.extensions.drawText as drawCenteredText

val slatShadowRadius = 1.dp
private const val HORIZONTAL_MOVE_HYSTERESIS = 20

@Composable
fun FacadeBlindsWindowView(
  windowState: FacadeBlindWindowState,
  modifier: Modifier = Modifier,
  enabled: Boolean = false,
  onPositionChanging: ((tilt: Float, position: Float) -> Unit)? = null,
  onPositionChanged: ((tilt: Float, position: Float) -> Unit)? = null
) {
  val (windowDimens, updateDimens) = remember { mutableStateOf<RuntimeDimens?>(null) }
  val moveState = remember { mutableStateOf(MoveState()) }
  val colors = FacadeBlindColors.standard()
  val textMeasurer = rememberTextMeasurer()

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
                  initialVerticalPercentage = windowState.position.value(ValueDestination.WINDOW) ?: 0f,
                  initialHorizontalPercentage = windowState.windowTilt?.value(ValueDestination.WINDOW) ?: 0f,
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
              onPositionChanged?.invoke(
                windowState.windowTilt?.value(ValueDestination.WINDOW) ?: 0f,
                windowState.position.value(ValueDestination.WINDOW) ?: 0f
              )
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

    WindowDrawer.drawWindow(
      scope = this@Canvas,
      runtimeDimens = windowDimens,
      colors = colors,
      windowState = windowState,
      textMeasurer = textMeasurer
    )
    if (enabled.not()) {
      drawRect(colors.disabledOverlay)
    }
  }
}

private object WindowDrawer : WindowDrawerBase<RuntimeDimens, FacadeBlindWindowState, FacadeBlindColors>() {

  private val maxTilt = 8.dp.toPx()
  private val tiltInitialCorrection = 1.dp.toPx()
  private const val TILT_RANGE = 180f
  private const val TILT_HALF_RANGE = 90f

  private val markerLinesOffsets = listOf(2.5.dp, 0.dp, (-2.5).dp)
  private val markerCluster = MarkerCluster()

  override fun DrawScope.drawShadowingElements(
    windowState: FacadeBlindWindowState,
    runtimeDimens: RuntimeDimens,
    colors: FacadeBlindColors
  ) {
    val topCorrection = (windowState.position.value(ValueDestination.WINDOW)?.div(100f) ?: 0f)
      .times(runtimeDimens.movementLimit)
      .plus(runtimeDimens.slatDistance.times(1.5f)) // Needed to align slats bottom with window bottom

    // When the roller shutter position is bigger than bottom position we need to start "closing slats".
    // Here the available space for "opened" slats is calculated
    val slatsCount = runtimeDimens.slats.size
    val progression = windowState.windowTiltDegrees?.let { if (it > 50) 1..slatsCount else null } ?: (slatsCount downTo 1)
    for (i in progression) {
      drawSlat(
        topCorrection = topCorrection - runtimeDimens.movementLimit,
        rect = runtimeDimens.slats[i - 1],
        runtimeDimens = runtimeDimens,
        colors = colors,
        tilt = windowState.windowTiltDegrees
      )
    }
  }

  override fun DrawScope.drawMarkers(
    windowState: FacadeBlindWindowState,
    runtimeDimens: RuntimeDimens,
    colors: FacadeBlindColors,
    textMeasurer: TextMeasurer
  ) {
    markerCluster.clean()

    windowState.markers.forEach { marker ->
      val topPosition = runtimeDimens.topLineRect.bottom
        .plus(runtimeDimens.movementLimit.minus(runtimeDimens.slatDistance).times(marker.position).div(100f))

      if (markerCluster.y == null) {
        markerCluster.first(topPosition, marker)
      } else if (markerCluster.covers(topPosition, runtimeDimens.markerInfoRadius)) {
        markerCluster.append(marker)
      } else if (markerCluster.shouldDrawAsSingleMarker) {
        drawMarker(
          offset = Offset(runtimeDimens.windowRect.left, markerCluster.safeY),
          value = markerCluster.minValue,
          tilt = markerCluster.minTilt?.let { SlatTiltSliderDimens.trimAngle(it) },
          runtimeDimens = runtimeDimens,
          windowColors = colors,
          textMeasurer = textMeasurer
        )

        // Clear and take next marker
        markerCluster.first(topPosition, marker)
      } else if (markerCluster.hasTilt) {
        drawMergedMarkers(
          offset = Offset(runtimeDimens.windowRect.left, markerCluster.safeY),
          minValue = markerCluster.minTilt ?: 0f,
          maxValue = markerCluster.maxTilt ?: 0f,
          unit = ValueUnit.TEMPERATURE_DEGREE.toString(),
          runtimeDimens = runtimeDimens,
          windowColors = colors,
          textMeasurer = textMeasurer
        )

        // Clear and take next marker
        markerCluster.first(topPosition, marker)
      } else {
        drawMergedMarkers(
          offset = Offset(runtimeDimens.windowRect.left, markerCluster.safeY),
          text = markerCluster.percentageText,
          runtimeDimens = runtimeDimens,
          windowColors = colors,
          textMeasurer = textMeasurer
        )

        // Clear and take next marker
        markerCluster.first(topPosition, marker)
      }
    }

    val (topPosition) = guardLet(markerCluster.y) { return }

    if (markerCluster.shouldDrawAsSingleMarker) {
      drawMarker(
        offset = Offset(runtimeDimens.windowRect.left, topPosition),
        value = markerCluster.minValue,
        tilt = markerCluster.minTilt?.let { SlatTiltSliderDimens.trimAngle(it) },
        runtimeDimens = runtimeDimens,
        windowColors = colors,
        textMeasurer = textMeasurer
      )
    } else if (markerCluster.hasTilt) {
      drawMergedMarkers(
        offset = Offset(runtimeDimens.windowRect.left, topPosition),
        minValue = markerCluster.minTilt ?: 0f,
        maxValue = markerCluster.maxTilt ?: 0f,
        unit = ValueUnit.TEMPERATURE_DEGREE.toString(),
        runtimeDimens = runtimeDimens,
        windowColors = colors,
        textMeasurer = textMeasurer
      )
    } else if (markerCluster.count > 1) {
      drawMergedMarkers(
        offset = Offset(runtimeDimens.windowRect.left, topPosition),
        text = markerCluster.percentageText,
        runtimeDimens = runtimeDimens,
        windowColors = colors,
        textMeasurer = textMeasurer
      )
    }
  }

  private fun DrawScope.drawSlat(
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

    applyForSlat(paint, colors)
    drawContext.canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
    drawPath(path = path, color = colors.slatBorder, style = Stroke(width = 1.dp.toPx()))
  }

  private fun DrawScope.drawMarker(
    offset: Offset,
    value: Float?,
    tilt: Float?,
    runtimeDimens: RuntimeDimens,
    windowColors: FacadeBlindColors,
    textMeasurer: TextMeasurer
  ) {
    drawCircle(color = windowColors.slatBackground, radius = runtimeDimens.markerInfoRadius, center = offset)
    drawCircle(
      color = windowColors.slatBorder,
      radius = runtimeDimens.markerInfoRadius,
      center = offset,
      style = Stroke(width = 1.5.dp.toPx())
    )
    val lineOffset = runtimeDimens.markerInfoRadius / 2

    if (tilt != null) {
      markerLinesOffsets.forEach {
        val y = offset.y - it.toPx()
        rotate(tilt, Offset(x = offset.x, y = y)) {
          drawLine(
            color = windowColors.markerBorder,
            start = Offset(offset.x - lineOffset, y),
            end = Offset(offset.x + lineOffset, y),
            strokeWidth = 1.dp.toPx()
          )
        }
      }
    } else if (value != null) {
      drawPercentage(
        value = value.div(100f),
        textMeasurer = textMeasurer,
        color = windowColors.markerBorder,
        center = offset,
        textSize = 6.dp
      )
    } else {
      drawCenteredText(
        text = "?",
        textMeasurer = textMeasurer,
        color = windowColors.markerBorder,
        center = offset,
        textSize = 6.dp
      )
    }
  }

  private fun DrawScope.drawMergedMarkers(
    offset: Offset,
    minValue: Float,
    maxValue: Float,
    unit: String,
    runtimeDimens: RuntimeDimens,
    windowColors: FacadeBlindColors,
    textMeasurer: TextMeasurer
  ) {
    drawMergedMarkers(
      offset = offset,
      text = "${minValue.roundToInt()}$unit - ${maxValue.roundToInt()}$unit",
      runtimeDimens = runtimeDimens,
      windowColors = windowColors,
      textMeasurer = textMeasurer
    )
  }

  private fun DrawScope.drawMergedMarkers(
    offset: Offset,
    text: String,
    runtimeDimens: RuntimeDimens,
    windowColors: FacadeBlindColors,
    textMeasurer: TextMeasurer
  ) {
    val padding = 4.dp.toPx()
    val textLayout = textMeasurer.measure(
      text = text,
      style = TextStyle(
        fontSize = (12.dp.toPx() / density / fontScale).sp,
        fontFamily = defaultFont
      )
    )

    drawRoundRect(
      color = windowColors.slatBackground,
      topLeft = Offset(offset.x - runtimeDimens.markerInfoRadius, offset.y - runtimeDimens.markerInfoRadius),
      size = Size(textLayout.size.width.toFloat() + padding.times(2), runtimeDimens.markerInfoRadius.times(2)),
      cornerRadius = CornerRadius(runtimeDimens.markerInfoRadius, runtimeDimens.markerInfoRadius)
    )
    drawRoundRect(
      color = windowColors.slatBorder,
      topLeft = Offset(offset.x - runtimeDimens.markerInfoRadius, offset.y - runtimeDimens.markerInfoRadius),
      size = Size(textLayout.size.width.toFloat() + padding.times(2), runtimeDimens.markerInfoRadius.times(2)),
      cornerRadius = CornerRadius(runtimeDimens.markerInfoRadius, runtimeDimens.markerInfoRadius),
      style = Stroke(width = 1.5.dp.toPx())
    )
    drawText(
      textLayoutResult = textLayout,
      color = windowColors.markerBorder,
      topLeft = Offset(offset.x - runtimeDimens.markerInfoRadius + padding, offset.y - textLayout.size.height.div(2))
    )
  }
}

private fun DrawScope.applyForSlat(paint: Paint, colors: FacadeBlindColors) {
  with(paint) {
    style = Paint.Style.FILL
    strokeCap = Paint.Cap.SQUARE
    color = colors.slatBackground.toArgb()
    setShadowLayer(slatShadowRadius.toPx(), 0f, 1.5.dp.toPx(), colors.shadow.toArgb())
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

internal data class MarkerCluster(
  var y: Float? = null,
  var minTilt: Float? = null,
  var maxTilt: Float? = null,
  var minValue: Float? = null,
  var maxValue: Float? = null,
  var count: Int = 0
) {
  val safeY: Float
    get() = y ?: 0f

  val shouldDrawAsSingleMarker: Boolean
    get() = count == 1 ||
      (hasTilt && minTilt?.roundToInt() == maxTilt?.roundToInt()) ||
      (!hasTilt && minValue != null && minValue?.roundToInt() == maxValue?.roundToInt())

  val hasTilt: Boolean
    get() = minTilt != null && maxTilt != null

  val percentageText: String
    get() {
      val unit = ValueUnit.PERCENTAGE
      val (minValue, maxValue) = guardLet(minValue, maxValue) { return "?" }
      return if (minValue == maxValue) "$minValue" else "$minValue$unit - $maxValue$unit"
    }

  fun first(y: Float, marker: ShadingBlindMarker) {
    this.y = y
    this.minTilt = marker.tilt
    this.maxTilt = marker.tilt
    this.minValue = marker.tiltValue
    this.maxValue = marker.tiltValue
    this.count = 1
  }

  fun append(marker: ShadingBlindMarker) {
    minTilt = minOfNullable(minTilt, marker.tilt)
    maxTilt = maxOfNullable(maxTilt, marker.tilt)
    minValue = minOfNullable(minValue, marker.tiltValue)
    maxValue = maxOfNullable(maxValue, marker.tiltValue)
    count++
  }

  fun covers(y: Float, radius: Float): Boolean =
    this.y?.let { abs(y - it) < radius.times(2) } ?: false

  fun clean() {
    y = null
    minTilt = null
    maxTilt = null
    minValue = null
    maxValue = null
    count = 0
  }
}

private fun minOfNullable(first: Float?, second: Float?): Float? {
  return if (first == null && second == null) {
    null
  } else if (first == null) {
    second
  } else if (second == null) {
    first
  } else {
    min(first, second)
  }
}

private fun maxOfNullable(first: Float?, second: Float?): Float? {
  return if (first == null && second == null) {
    null
  } else if (first == null) {
    second
  } else if (second == null) {
    first
  } else {
    max(first, second)
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
        val markers = listOf(
          ShadingBlindMarker(24f, 25f, tilt0Angle = 0f, tilt100Angle = 90f),
          ShadingBlindMarker(25f, 75f, tilt0Angle = 0f, tilt100Angle = 90f),
          ShadingBlindMarker(60f, 25f, tilt0Angle = 0f, tilt100Angle = 90f),
          ShadingBlindMarker(100f, 25f)
        )
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
          enabled = true,
          modifier = Modifier
            .fillMaxSize()
            .padding(all = Distance.small)
        )
      }
    }
  }
}
