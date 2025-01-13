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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

public class SuplaColorListPicker extends View {

  private final Paint p = new Paint();
  private final RectF rectF = new RectF();
  private ArrayList<ListItem> Items = null;
  private float Space = 0;
  private float BorderWidth = 0;
  private int BorderColor = Color.WHITE;
  private int BrightnessLevelColor = Color.BLACK;
  private int BorderColorSelected = Color.YELLOW;
  private ListItem TouchedItem = null;
  private OnColorListTouchListener mOnTouchListener;
  private final Handler handler = new Handler();

  public SuplaColorListPicker(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public SuplaColorListPicker(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public SuplaColorListPicker(Context context) {
    super(context);
    init();
  }

  private void init() {
    Items = new ArrayList<>();

    Space =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, (float) 10, getResources().getDisplayMetrics());

    BorderWidth =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, (float) 1, getResources().getDisplayMetrics());
  }

  public int addItem(int color, short percent) {
    TouchedItem = null;
    ListItem i = new ListItem();
    i.setColor(color);
    i.setPercent(percent);
    Items.add(i);
    return Items.size() - 1;
  }

  public int addItem() {

    return addItem(Color.TRANSPARENT, (short) 0);
  }

  public void removeItem(int idx) {

    if (idx >= 0 && idx < Items.size()) {
      TouchedItem = null;
      Items.remove(idx);
      invalidate();
    }
  }

  public int count() {
    return Items.size();
  }

  public int getItemColor(int idx) {

    if (idx >= 0 && idx < Items.size()) return Items.get(idx).getColor();

    return Color.TRANSPARENT;
  }

  public void setItemColor(int idx, int color) {

    if (idx >= 0 && idx < Items.size()) {
      Items.get(idx).setColor(color);
      invalidate();
    }
  }

  public short getItemPercent(int idx) {

    if (idx >= 0 && idx < Items.size()) return Items.get(idx).getPercent();

    return 0;
  }

  public void setItemPercent(int idx, short percent) {

    if (idx >= 0 && idx < Items.size()) {
      Items.get(idx).setPercent(percent);
      invalidate();
    }
  }

  public Object getExtraParam1(int idx) {

    if (idx >= 0 && idx < Items.size()) return Items.get(idx).getExtraParam1();

    return null;
  }

  public void setExtraParam1(int idx, Object param1) {

    if (idx >= 0 && idx < Items.size()) Items.get(idx).setExtraParam1(param1);
  }

  public Object getExtraParam2(int idx) {

    if (idx >= 0 && idx < Items.size()) return Items.get(idx).getExtraParam2();

    return null;
  }

  public void setExtraParam2(int idx, Object param2) {

    if (idx >= 0 && idx < Items.size()) Items.get(idx).setExtraParam1(param2);
  }

  public float getSpace() {
    return Space;
  }

  public void setSpace(float space) {
    Space = space;
    invalidate();
  }

  public float getBorderWidth() {
    return BorderWidth;
  }

  public void setBorderWidth(float borderWidth) {
    BorderWidth = borderWidth;
    invalidate();
  }

  public int getBorderColor() {
    return BorderColor;
  }

  public void setBorderColor(int borderColor) {
    BorderColor = borderColor;
    invalidate();
  }

  public int getBorderColorSelected() {
    return BorderColorSelected;
  }

  public void setBorderColorSelected(int borderColorSelected) {
    BorderColorSelected = borderColorSelected;
    invalidate();
  }

  public int getBrightnessLevelColor() {
    return BrightnessLevelColor;
  }

  public void setBrightnessLevelColor(int brightnessLevelColor) {
    BrightnessLevelColor = brightnessLevelColor;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {

    if (Items.size() == 0) return;

    p.setAntiAlias(true);

    int bw = (int) (BorderWidth / 2);
    int width = getWidth() - 1;
    int height = getHeight() - 1;
    width =
        (int)
            ((width - Space * (Items.size() - 1) - BorderWidth * (Items.size() - 1))
                / Items.size());
    int left = 0;

    for (int a = 0; a < Items.size(); a++) {
      ListItem i = Items.get(a);

      p.setColor(i.getColor());
      p.setStyle(Paint.Style.FILL);

      int l = left + bw;
      int r = left + width - bw;
      int b = height - bw;

      rectF.set(l, bw, r, b);
      canvas.drawRoundRect(rectF, 20, 20, p);

      p.setColor(i == TouchedItem ? BorderColorSelected : BorderColor);
      p.setStyle(Paint.Style.STROKE);
      p.setStrokeWidth(BorderWidth);

      canvas.drawRoundRect(rectF, 20, 20, p);

      i.setRect(rectF);

      if (i.getPercent() > 0) {

        p.setColor(BrightnessLevelColor);

        double rl_margin = width * 0.05;
        double b_margin = width * 0.1;

        int p_width = (int) (width - rl_margin);

        l = (int) (left + bw + rl_margin);
        r = left + p_width - bw;

        b = r - l;
        b *= i.getPercent() / 100.00;
        b = (r - l) - b;

        l += b / 2;
        r -= b / 2;

        // canvas.drawLine(l, (int)(height-bw-b_margin-BorderWidth), r,
        // (int)(height-bw-b_margin-BorderWidth), p);
        canvas.drawLine(
            l, (int) (bw + b_margin + BorderWidth), r, (int) (bw + b_margin + BorderWidth), p);
      }

      left += width + Space + BorderWidth;
    }
  }

  private void onEdit() {

    if (mOnTouchListener != null && TouchedItem != null) {
      mOnTouchListener.onEdit(this, Items.indexOf(TouchedItem));

      TouchedItem = null;
      invalidate();
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {

    switch (event.getAction()) {
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        if (TouchedItem != null && mOnTouchListener != null) {
          mOnTouchListener.onColorTouched(this, TouchedItem.getColor(), TouchedItem.getPercent());
        }

        handler.removeCallbacks(this::onEdit);

        TouchedItem = null;

        invalidate();

        break;

      case MotionEvent.ACTION_DOWN:
        float x = event.getX();
        float y = event.getY();

        handler.removeCallbacks(this::onEdit);

        for (int a = 0; a < Items.size(); a++) {
          ListItem i = Items.get(a);
          RectF rect = i.getRect();

          if (rect != null && rect.contains(x, y)) {
            TouchedItem = i;
            invalidate();

            handler.postDelayed(this::onEdit, 1500);
          }
        }

        break;
    }

    return true;
  }

  void setOnTouchListener(OnColorListTouchListener l) {
    mOnTouchListener = l;
  }

  public interface OnColorListTouchListener {
    void onColorTouched(SuplaColorListPicker sclPicker, int color, short percent);

    void onEdit(SuplaColorListPicker sclPicker, int idx);
  }

  private class ListItem {

    private int mColor = Color.TRANSPARENT;
    private short Percent = 0;
    private Object ExtraParam1 = null;
    private Object ExtraParam2 = null;
    private RectF Rect = null;

    public ListItem() {}

    public int getColor() {
      return mColor;
    }

    public void setColor(int color) {
      mColor = color;
    }

    public short getPercent() {
      return Percent;
    }

    public void setPercent(short percent) {

      if (percent < 0) percent = (short) 0;
      else if (percent > 100) percent = (short) 100;

      Percent = percent;
    }

    public RectF getRect() {
      return Rect;
    }

    public void setRect(RectF rect) {
      Rect = rect == null ? null : new RectF(rect);
    }

    public Object getExtraParam1() {
      return ExtraParam1;
    }

    public void setExtraParam1(Object extraParam1) {
      ExtraParam1 = extraParam1;
    }

    public Object getExtraParam2() {
      return ExtraParam2;
    }

    public void setExtraParam2(Object extraParam2) {
      ExtraParam2 = extraParam2;
    }
  }
}
