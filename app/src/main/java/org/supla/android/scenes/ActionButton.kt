package org.supla.android.scenes

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import org.supla.android.R
import org.supla.android.SuplaApp

class ActionButton(context: Context, private val position: Position, private val text: String) {

  private val background = ColorDrawable(ContextCompat.getColor(context, R.color.channel_btn))
  private val buttonWidth =
    context.resources.getDimensionPixelSize(R.dimen.channel_layout_button_width)

  private val textPaint = Paint()
  private val textRect = Rect()

  init {
    textPaint.color = ContextCompat.getColor(context, R.color.channel_btn_text)
    textPaint.textSize = context.resources.getDimension(R.dimen.channel_btn_text_size)
    textPaint.typeface = SuplaApp.getApp().typefaceQuicksandRegular
    textPaint.textAlign = Paint.Align.LEFT

    textPaint.getTextBounds(text, 0, text.length, textRect)
  }

  fun getButtonWidth(): Int = buttonWidth

  fun draw(canvas: Canvas, area: Rect) {
    drawBackground(canvas, area)
    drawText(canvas, area)
  }

  private fun drawBackground(canvas: Canvas, area: Rect) {
    if (position == Position.RIGHT) {
      var left = area.left
      if (area.right - left > buttonWidth) {
        left = area.right - buttonWidth
      }
      background.setBounds(left, area.top, area.right, area.bottom)
    } else if (position == Position.LEFT) {
      var right = area.right
      if (right - area.left > buttonWidth) {
        right = area.left + buttonWidth
      }
      background.setBounds(area.left, area.top, right, area.bottom)
    }

    background.draw(canvas)
  }

  private fun drawText(canvas: Canvas, area: Rect) {
    val textMargin = ((buttonWidth - textRect.width()) / 2)

    val textX = when {
      position == Position.RIGHT && area.width() < buttonWidth -> area.left + textMargin
      position == Position.RIGHT -> area.right - buttonWidth + textMargin
      // position is LEFT
      area.width() < buttonWidth -> area.right - (textMargin + textRect.width())
      else -> textMargin
    }

    val textY = area.height() / 2 + textRect.height() / 2 - textRect.bottom
    canvas.drawText(text, textX.toFloat(), (area.top + textY).toFloat(), textPaint)
  }

  enum class Position {
    LEFT, RIGHT
  }
}