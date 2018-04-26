package org.supla.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

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

public class SuplaLinearOnlineStatus extends View {

    private boolean Horizontal = false;
    private float Percent = 50;
    private int OnlineColor = Color.GREEN;
    private int OfflineColor = Color.RED;
    private int BorderlineColor = Color.BLACK;
    private RectF rectf = new RectF();
    private Paint paint = new Paint();
    private DisplayMetrics metrics = getResources().getDisplayMetrics();
    private float FrameLineWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            (float) 1, metrics);

    public SuplaLinearOnlineStatus(Context context) {
        super(context);
    }

    public SuplaLinearOnlineStatus(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SuplaLinearOnlineStatus(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SuplaLinearOnlineStatus(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setHorizontal(boolean horizontal) {
        Horizontal = horizontal;
        invalidate();
    }

    public boolean getHorizontal() {
        return Horizontal;
    }

    public void setPercent(float percent) {
        Percent = percent;
        invalidate();
    }

    public float getPercent() {
        return Percent;
    }

    public void setOnlineColor(int onlineColor) {
        OnlineColor = onlineColor;
        invalidate();
    }

    public int getOnlineColor() {
        return OnlineColor;
    }

    public void setOfflineColor(int offlineColor) {
        OfflineColor = offlineColor;
        invalidate();
    }

    public int getOfflineColor() {
        return OfflineColor;
    }

    public void setBorderLineColor(int borderLineColor) {
        BorderlineColor = borderLineColor;
        invalidate();
    }

    public int getBorderLineColor() {
        return BorderlineColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        paint.setColor(getOnlineColor());
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(FrameLineWidth);

        float percentPoint;

        if (getHorizontal()) {
            percentPoint = getWidth() * Percent / 100;

            rectf.set(0, 0, percentPoint, getHeight());
            canvas.drawRect(rectf, paint);

            paint.setColor(getOfflineColor());
            rectf.set(percentPoint, 0, getWidth(), getHeight());
            canvas.drawRect(rectf, paint);
        } else {
            percentPoint = getHeight() * Percent / 100;

            rectf.set(0, 0, getWidth(), percentPoint);
            canvas.drawRect(rectf, paint);

            paint.setColor(getOfflineColor());
            rectf.set(0, percentPoint, getWidth(), getHeight());
            canvas.drawRect(rectf, paint);
        }


        paint.setColor(getBorderLineColor());
        paint.setStyle(Paint.Style.STROKE);
        rectf.set(0, 0, getWidth(), getHeight());
        canvas.drawRect(rectf, paint);
    }
}
