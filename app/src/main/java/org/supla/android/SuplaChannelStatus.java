package org.supla.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.Nullable;

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

public class SuplaChannelStatus extends View {

  private ShapeType shapeType = ShapeType.LinearVertical;
  private float Percent = 50;
  private int OnlineColor = Color.GREEN;
  private int OfflineColor = Color.RED;
  private boolean mSingleColor = false;
  private int BorderlineColor = Color.BLACK;
  private final RectF rectf = new RectF();
  private final Paint paint = new Paint();
  private final DisplayMetrics metrics = getResources().getDisplayMetrics();
  private final float FrameLineWidth =
      TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 1, metrics);

  public SuplaChannelStatus(Context context) {
    super(context);
  }

  public SuplaChannelStatus(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public SuplaChannelStatus(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public ShapeType getShapeType() {
    return shapeType;
  }

  public void setShapeType(ShapeType shapeType) {
    this.shapeType = shapeType;
    invalidate();
  }

  public float getPercent() {
    return Percent;
  }

  public void setPercent(float percent) {
    Percent = percent;
    invalidate();
  }

  public int getOnlineColor() {
    return OnlineColor;
  }

  public void setOnlineColor(int onlineColor) {
    OnlineColor = onlineColor;
    invalidate();
  }

  public int getOfflineColor() {
    return OfflineColor;
  }

  public void setOfflineColor(int offlineColor) {
    OfflineColor = offlineColor;
    invalidate();
  }

  public boolean getSingleColor() {
    return mSingleColor;
  }

  public void setSingleColor(boolean singleColor) {
    mSingleColor = singleColor;
    invalidate();
  }

  public int getBorderLineColor() {
    return BorderlineColor;
  }

  public void setBorderLineColor(int borderLineColor) {
    BorderlineColor = borderLineColor;
    invalidate();
  }

  public float getBorderLineWidth() {
    return FrameLineWidth;
  }

  @Override
  protected void onDraw(Canvas canvas) {

    paint.setColor(getOfflineColor());
    paint.setStyle(Paint.Style.FILL);
    paint.setStrokeWidth(FrameLineWidth);
    paint.setFlags(Paint.ANTI_ALIAS_FLAG);

    float percentPoint;

    if (shapeType == ShapeType.LinearHorizontal) {
      percentPoint = getWidth() * (100 - Percent) / 100;

      if (!getSingleColor()) {
        rectf.set(0, 0, percentPoint, getHeight());
        canvas.drawRect(rectf, paint);
      }

      paint.setColor(getOnlineColor());
      rectf.set(percentPoint, 0, getWidth(), getHeight());
      canvas.drawRect(rectf, paint);
    } else if (shapeType == ShapeType.LinearVertical) {
      percentPoint = getHeight() * (100 - Percent) / 100;

      if (!getSingleColor()) {
        rectf.set(0, 0, getWidth(), percentPoint);
        canvas.drawRect(rectf, paint);
      }

      paint.setColor(getOnlineColor());
      rectf.set(0, percentPoint, getWidth(), getHeight());
      canvas.drawRect(rectf, paint);
    } else {
      float size = getWidth();
      if (size > getHeight()) {
        size = getHeight();
      }
      size = size / 2;

      paint.setColor(Percent > 0 ? getOnlineColor() : getOfflineColor());
      paint.setStyle(shapeType == ShapeType.Ring ? Paint.Style.STROKE : Paint.Style.FILL);

      canvas.drawColor(Color.TRANSPARENT);
      canvas.drawCircle(
          getWidth() / 2,
          getHeight() / 2,
          size - (shapeType == ShapeType.Ring ? FrameLineWidth / 2 : 0),
          paint);
      return;
    }

    paint.setColor(getBorderLineColor());
    paint.setStyle(Paint.Style.STROKE);
    rectf.set(0, 0, getWidth(), getHeight());
    canvas.drawRect(rectf, paint);
  }

  public enum ShapeType {
    LinearVertical,
    LinearHorizontal,
    Dot,
    Ring,
  }
}
