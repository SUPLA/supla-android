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
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import java.text.DateFormatSymbols;

public class SuplaThermostatCalendar extends View {

  private Paint mPaint;
  private float mSpacing;
  private float mTextSize;

  private float mBoxHeight;
  private float mBoxWidth;

  private String[] mDayNames;

  private String mProgram0Label;
  private String mProgram1Label;
  private int mHourProgram0Color;
  private int mHourProgram1Color;
  private int mFontColor;
  private int mFirtsDay = 1;

  private boolean[][] mHourProgramGrid = new boolean[7][24];
  private boolean mSetHourProgramTo1;
  private int mLastHour = -1;
  private int mLastDay = -1;
  private boolean mTouched = false;
  private OnCalendarTouchListener mOnCalendarTouchListener;

  public SuplaThermostatCalendar(Context context) {
    super(context);
    init(context);
  }

  public SuplaThermostatCalendar(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public SuplaThermostatCalendar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  public SuplaThermostatCalendar(
      Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context);
  }

  private void init(Context context) {

    Resources res = getResources();
    if (res == null) {
      mSpacing = 5;
      mTextSize = 20;
    } else {
      DisplayMetrics mMetrics = res.getDisplayMetrics();
      mSpacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 3, mMetrics);
      mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, (float) 10, mMetrics);
    }

    DateFormatSymbols symbols = new DateFormatSymbols();
    mDayNames = symbols.getShortWeekdays();
    mHourProgram0Color = Color.parseColor("#b0e0a8");
    mHourProgram1Color = Color.parseColor("#ffd19a");
    mProgram0Label = "P0";
    mProgram1Label = "P1";
    mFontColor = ResourcesCompat.getColor(context.getResources(), R.color.on_background, null);

    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setColor(Color.RED);
    mPaint.setStyle(Paint.Style.FILL);
    mPaint.setTextSize(mTextSize);
  }

  protected void calculateGridArea() {
    float mGridHeight = getHeight();
    float mGridWidth = getWidth();

    mBoxHeight = (mGridHeight - mSpacing * 25f) / 26f;
    mBoxWidth = (mGridWidth - mSpacing * 7f) / 8f;
  }

  protected RectF getRectangle(int day, int hour) {
    float leftOffset = day * (mBoxWidth + mSpacing);
    float topOffset = (hour + 1) * (mBoxHeight + mSpacing);

    return new RectF(leftOffset, topOffset, leftOffset + mBoxWidth, topOffset + mBoxHeight);
  }

  protected Rect drawText(Canvas canvas, String label, RectF frm, boolean alignLeft) {
    Rect bounds = new Rect();
    mPaint.getTextBounds(label, 0, label.length(), bounds);
    canvas.drawText(
        label,
        frm.left + (alignLeft ? 0 : (frm.width() / 2 - bounds.width() / 2)),
        frm.top + bounds.height() + (frm.height() / 2 - bounds.height() / 2),
        mPaint);
    return bounds;
  }

  protected short drawLabel(Canvas canvas, short offset, String label, int color) {
    if (label != null) {
      RectF frm = getRectangle(offset + 1, 24);
      frm.right = getRectangle(offset + 3, 24).right;

      mPaint.setColor(color);
      canvas.drawRoundRect(frm, 6, 6, mPaint);

      mPaint.setColor(Color.BLACK);
      drawText(canvas, label, frm, false);
      offset += 3;
    }

    return offset;
  }

  public short dayWithOffset(short day) {

    day = (short) (day + mFirtsDay - 1);
    if (day > 7) {
      day -= 7;
    }

    return day;
  }

  @Override
  protected void onDraw(Canvas canvas) {

    calculateGridArea();
    RectF frm;

    for (short d = 0; d <= 7; d++) {
      for (short h = -1; h < 24; h++) {

        frm = getRectangle(d, h);
        short dayIdx = (short) (dayWithOffset(d) - 1);

        if (h == -1 || d == 0) {
          String label = "";

          if (h == -1 && d > 0) {
            label = mDayNames[dayIdx + 1];
          } else if (h > -1) {
            label = String.format("%02d", h);
          }

          mPaint.setColor(mFontColor);
          drawText(canvas, label, frm, false);

        } else {
          mPaint.setColor(mHourProgramGrid[dayIdx][h] ? mHourProgram1Color : mHourProgram0Color);
          canvas.drawRoundRect(frm, 6, 6, mPaint);
        }
      }
    }

    short offset = drawLabel(canvas, (short) 0, mProgram0Label, mHourProgram0Color);
    drawLabel(canvas, offset, mProgram1Label, mHourProgram1Color);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if ((event.getAction() != MotionEvent.ACTION_DOWN
        && event.getAction() != MotionEvent.ACTION_MOVE)) {
      mLastHour = -1;
      mLastDay = -1;
      mTouched = false;
      return false;
    }

    float X = event.getX();
    float Y = event.getY();
    mTouched = true;

    if (X < mBoxWidth || Y < mBoxHeight || Y > getHeight() - mBoxHeight) {
      return false;
    }

    for (short d = 1; d <= 7; d++) {
      for (short h = 0; h < 24; h++) {
        RectF frm = getRectangle(d, h);
        short dayIdx = (short) (dayWithOffset(d) - 1);
        if (X >= frm.left && Y >= frm.top && X <= frm.right && Y <= frm.bottom) {

          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mSetHourProgramTo1 = !mHourProgramGrid[dayIdx][h];
          }

          if (mLastDay != d || mLastHour != h) {
            mHourProgramGrid[dayIdx][h] = mSetHourProgramTo1;
            mLastDay = d;
            mLastHour = h;

            if (mOnCalendarTouchListener != null) {
              mOnCalendarTouchListener.onHourValueChanged(this, d, h, mSetHourProgramTo1);
            }

            invalidate();
          }

          break;
        }
      }
    }

    return true;
  }

  private boolean areTheDayAndHourCorrect(short day, short hour) {
    return !(day < 1 || day > 7 || hour < 0 || hour > 23);
  }

  public void setHourProgramTo1(short day, short hour, boolean one) {
    if (areTheDayAndHourCorrect(day, hour)) {
      mHourProgramGrid[day - 1][hour] = one;
    }
  }

  public boolean isHourProgramIsSetTo1(short day, short hour) {
    return areTheDayAndHourCorrect(day, hour) && mHourProgramGrid[day - 1][hour];
  }

  public float getTextSize() {
    return mTextSize;
  }

  public void setTextSize(float textSize) {
    this.mTextSize = textSize;
    invalidate();
  }

  public void setProgram0Label(String program0Label) {
    this.mProgram0Label = program0Label;
    invalidate();
  }

  public void setProgram1Label(String program1Label) {
    this.mProgram1Label = program1Label;
    invalidate();
  }

  public int getFirtsDay() {
    return mFirtsDay;
  }

  public void setFirtsDay(int firtsDay) {
    if (firtsDay >= 1 && firtsDay <= 7) {
      this.mFirtsDay = firtsDay;
      invalidate();
    }
  }

  public void setOnCalendarTouchListener(OnCalendarTouchListener mOnCalendarTouchListener) {
    this.mOnCalendarTouchListener = mOnCalendarTouchListener;
  }

  public boolean isTouched() {
    return mTouched;
  }

  public void clear() {
    mHourProgramGrid = new boolean[7][24];
    invalidate();
  }

  public interface OnCalendarTouchListener {
    void onHourValueChanged(
        SuplaThermostatCalendar calendar, short day, short hour, boolean program1);
  }
}
