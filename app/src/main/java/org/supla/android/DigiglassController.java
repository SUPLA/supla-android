package org.supla.android;

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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class DigiglassController extends View {

  private static final int SECTION_BUTTON_MAX_SIZE_PX = 150;
  private static final int BTN_PICTOGRAM_MAX_WIDTH_PX = 150;

  Paint paint;
  RectF rect;
  float pxPerDP;
  float lineWidth;
  int barColor;
  int lineColor;
  int glassColor;
  int dotColor;
  int sectionCount;
  int btnBackgroundColor;
  int btnDotColor;
  boolean vertical;
  int transparentSections;
  OnSectionClickListener onSectionClickListener;

  private void init() {
    paint = new Paint();
    paint.setAntiAlias(true);
    rect = new RectF();

    pxPerDP = 1;
    Resources r = getResources();
    if (r != null) {
      pxPerDP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, r.getDisplayMetrics());
    }

    lineWidth = 2f * pxPerDP;
    barColor = Color.WHITE;
    lineColor = Color.BLACK;
    dotColor = Color.BLACK;
    glassColor = 0xffbdd8f2;
    btnBackgroundColor = Color.WHITE;
    btnDotColor = Color.BLACK;
    sectionCount = 7;
  }

  public DigiglassController(Context context) {
    super(context);
    init();
  }

  public DigiglassController(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public DigiglassController(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public DigiglassController(
      Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  public float getLineWidth() {
    return lineWidth;
  }

  public void setLineWidth(float lineWidth) {
    if (lineWidth <= 0.1) {
      lineWidth = 0.1f;
    } else if (lineWidth > 20) {
      lineWidth = 20;
    }
    this.lineWidth = lineWidth;
    invalidate();
  }

  public int getBarColor() {
    return barColor;
  }

  public void setBarColor(int barColor) {
    this.barColor = barColor;
    invalidate();
  }

  public int getLineColor() {
    return lineColor;
  }

  public void setLineColor(int lineColor) {
    this.lineColor = lineColor;
    invalidate();
  }

  public int getGlassColor() {
    return glassColor;
  }

  public void setGlassColor(int glassColor) {
    this.glassColor = glassColor;
    invalidate();
  }

  public int getDotColor() {
    return dotColor;
  }

  public void setDotColor(int dotColor) {
    this.dotColor = dotColor;
    invalidate();
  }

  public int getBtnBackgroundColor() {
    return btnBackgroundColor;
  }

  public void setBtnBackgroundColor(int btnBackgroundColor) {
    this.btnBackgroundColor = btnBackgroundColor;
    invalidate();
  }

  public int getBtnDotColor() {
    return btnDotColor;
  }

  public void setBtnDotColor(int btnDotColor) {
    this.btnDotColor = btnDotColor;
    invalidate();
  }

  public int getSectionCount() {
    return sectionCount;
  }

  public void setSectionCount(int sectionCount) {
    if (sectionCount < 1) {
      sectionCount = 1;
    } else if (sectionCount > 7) {
      sectionCount = 7;
    }
    this.sectionCount = sectionCount;
    this.transparentSections &= (short) (Math.pow(2, sectionCount) - 1);
    invalidate();
  }

  public OnSectionClickListener getOnSectionClickListener() {
    return onSectionClickListener;
  }

  public void setOnSectionClickListener(OnSectionClickListener onSectionClickListener) {
    this.onSectionClickListener = onSectionClickListener;
  }

  public int getTransparentSections() {
    return transparentSections;
  }

  public void setTransparentSections(int transparentSections) {
    this.transparentSections = transparentSections & (short) (Math.pow(2, sectionCount) - 1);
    invalidate();
  }

  public void setAllTransparent() {
    this.transparentSections = (short) (Math.pow(2, sectionCount) - 1);
    invalidate();
  }

  public void setAllOpaque() {
    this.transparentSections = 0;
    invalidate();
  }

  public boolean isVertical() {
    return vertical;
  }

  public void setVertical(boolean vertical) {
    this.vertical = vertical;
    invalidate();
  }

  private float getBarHeight() {
    return getHeight() * 0.05f;
  }

  private void getSectionRect(int sectionNumber, RectF rect) {
    float barHeight = getBarHeight();
    float height = getHeight();
    float width = getWidth();

    float sectionSize = (vertical ? width : (height - 2 * barHeight)) / sectionCount;
    if (vertical) {
      rect.left = sectionSize * sectionNumber;
      rect.right = rect.left + sectionSize + 1;
      rect.top = barHeight;
      rect.bottom = height - barHeight;
    } else {
      rect.left = lineWidth;
      rect.right = width - lineWidth;
      rect.top = barHeight + sectionSize * sectionNumber;
      rect.bottom = rect.top + sectionSize + 1;
    }
  }

  private void getSectionBtnRect(RectF sectionRect, RectF btnRect) {
    float width = sectionRect.width();
    if (sectionRect.height() < width) {
      width = sectionRect.height();
    }

    width *= 0.6;
    float left, right, top, bottom;

    if (width > SECTION_BUTTON_MAX_SIZE_PX) {
      width = SECTION_BUTTON_MAX_SIZE_PX;
    }
    if (vertical) {
      left = sectionRect.left + sectionRect.width() / 2 - width / 2;
      right = left + width;
      bottom = sectionRect.bottom - sectionRect.height() * 0.05f;
      top = bottom - width;
    } else {
      right = sectionRect.right - sectionRect.width() * 0.05f;
      left = right - width;
      top = sectionRect.top + sectionRect.height() / 2 - width / 2;
      bottom = top + width;
    }

    btnRect.left = left;
    btnRect.right = right;
    btnRect.top = top;
    btnRect.bottom = bottom;
  }

  private void drawDots(Canvas canvas, RectF rect) {
    paint.setColor(glassColor);
    canvas.drawRect(rect, paint);

    paint.setColor(dotColor);
    paint.setStrokeWidth(1);
    paint.setStyle(Paint.Style.FILL);
    float fieldRadius = pxPerDP * 1.0f;
    float pointRadius = pxPerDP * 0.5f;

    float diameter = fieldRadius * 2;
    int cw = (int) (rect.width() / diameter);
    int ch = (int) ((rect.height()) / diameter);

    float wmargin = (rect.width() - (cw * diameter)) / 2;
    float hmargin = (rect.height() - (ch * diameter)) / 2;

    for (int a = 0; a < ch; a++) {
      for (int b = 0; b < cw; b++) {
        if (b % 2 != a % 2) {
          canvas.drawCircle(
              rect.left + wmargin + fieldRadius + b * diameter,
              rect.top + hmargin + fieldRadius + a * diameter,
              pointRadius,
              paint);
        }
      }
    }
  }

  private void drawButton(Canvas canvas, RectF rect, boolean lines) {
    float radius = rect.width();
    if (rect.height() < radius) {
      radius = rect.height();
    }

    radius /= 2;

    paint.setStyle(Paint.Style.FILL);
    paint.setColor(btnBackgroundColor);

    float centerx = rect.left + rect.width() / 2;
    float centery = rect.top + rect.height() / 2;

    canvas.drawCircle(centerx, centery, radius, paint);

    paint.setColor(btnDotColor);

    float p = radius * 1.20f;

    if (p > BTN_PICTOGRAM_MAX_WIDTH_PX) {
      p = BTN_PICTOGRAM_MAX_WIDTH_PX;
    }

    float distance = p / 5f;
    float r = distance * 0.35f;
    float m, cx;
    int a;

    if (lines) {
      r *= 0.8f;

      canvas.save();
      canvas.rotate(-45, centerx, centery);

      canvas.drawRect(centerx - p / 2f, centery - r / 2, centerx + p / 2f, centery + r / 2, paint);

      p *= 0.6f;
      canvas.drawRect(
          centerx - p / 2f,
          centery - distance - r / 2,
          centerx + p / 2f,
          centery - distance + r / 2,
          paint);

      canvas.drawRect(
          centerx - p / 2f,
          centery + distance - r / 2,
          centerx + p / 2f,
          centery + distance + r / 2,
          paint);

      canvas.restore();
      return;
    }

    for (a = 0; a < 6; a++) {
      m = r;
      if (a == 0 || a == 5) {
        m *= 0.5f;
      } else if (a == 1 || a == 4) {
        m *= 0.8f;
      }

      cx = centerx + p / 2f - a * distance;

      canvas.drawCircle(cx, centery - distance / 2f, m, paint);

      canvas.drawCircle(cx, centery + distance / 2f, m, paint);
    }

    m = r * 0.8f;

    for (a = 1; a < 5; a++) {
      cx = centerx + p / 2f - a * distance;

      canvas.drawCircle(cx, centery - (distance / 2f + distance), m, paint);

      canvas.drawCircle(cx, centery + (distance / 2f + distance), m, paint);
    }

    m = r * 0.5f;

    for (a = 2; a < 4; a++) {
      cx = centerx + p / 2f - a * distance;

      canvas.drawCircle(cx, centery - (distance / 2f + distance * 2), m, paint);

      canvas.drawCircle(cx, centery + (distance / 2f + distance * 2), m, paint);
    }
  }

  public boolean isSectionTransparent(int num) {
    return (transparentSections & (1 << num)) > 0;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    float barHeight = getBarHeight();
    float height = getHeight();
    float width = getWidth();

    float lineHalfWidth = lineWidth / 2;
    paint.setStrokeWidth(1);

    rect.left = lineWidth;
    rect.right = width - lineWidth;
    rect.top = barHeight;
    rect.bottom = height - barHeight;

    drawDots(canvas, rect);

    for (int a = 0; a < sectionCount; a++) {
      getSectionRect(a, rect);

      boolean transparent = isSectionTransparent(a);

      if (transparent) {
        paint.setColor(glassColor);
        canvas.drawRect(rect, paint);
      }

      getSectionBtnRect(rect, rect);
      drawButton(canvas, rect, transparent);
    }

    paint.setColor(barColor);
    canvas.drawRect(0, 0, width, barHeight, paint);
    canvas.drawRect(0, height, width, height - barHeight, paint);

    paint.setStrokeWidth(lineWidth);
    paint.setColor(lineColor);

    canvas.drawLine(0, lineHalfWidth, width, lineHalfWidth, paint);
    canvas.drawLine(0, height - lineHalfWidth, width, height - lineHalfWidth, paint);

    canvas.drawLine(0, barHeight - lineHalfWidth, width, barHeight - lineHalfWidth, paint);
    canvas.drawLine(
        0, height - barHeight + lineHalfWidth, width, height - barHeight + lineHalfWidth, paint);

    canvas.drawLine(
        lineHalfWidth,
        barHeight - lineHalfWidth,
        lineHalfWidth,
        height - barHeight + lineHalfWidth,
        paint);
    canvas.drawLine(
        width - lineHalfWidth,
        barHeight - lineHalfWidth,
        width - lineHalfWidth,
        height - barHeight + lineHalfWidth,
        paint);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      RectF rectF = new RectF();
      for (int a = 0; a < sectionCount; a++) {
        getSectionRect(a, rectF);
        if (rectF.contains(event.getX(), event.getY())) {
          int transparentSections = getTransparentSections();
          transparentSections ^= 1 << a;
          setTransparentSections(transparentSections);
          if (onSectionClickListener != null) {
            onSectionClickListener.onGlassSectionClick(this, a, isSectionTransparent(a));
          }
          break;
        }
      }
    }

    return true;
  }

  public interface OnSectionClickListener {
    void onGlassSectionClick(DigiglassController controller, int section, boolean transparent);
  }
}
