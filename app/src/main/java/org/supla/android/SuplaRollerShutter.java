package org.supla.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

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


public class SuplaRollerShutter extends View {

    Path markerPath = new Path();
    DisplayMetrics metrics = getResources().getDisplayMetrics();
    private float FrameLineWidth = 1;
    private float Percent = 0;
    private float virtPercent = 0;
    private int GlassColor = 0xFFbed9f1;
    private int WindowFrameColor = Color.WHITE;
    private int WindowFrameLineColor = Color.BLACK;
    private int MarkerColor = Color.RED;
    private int RollerShutterColor = Color.WHITE;
    private int RollerShutterLineColor = Color.BLACK;
    private int RollerShutterBackgroundColor = 0xffEDEDED;
    private int SunColor = Color.WHITE;
    private float Spaceing;
    private int LouverCount = 10;
    private float LouverSpaceing;
    private boolean Moving = false;
    private float MoveX;
    private float MoveY;
    private Paint paint = new Paint();
    private OnTouchListener mOnTouchListener;
    private RectF rectf = new RectF();
    private ArrayList<Float> Markers = null;
    private float bottomPosition;

    public SuplaRollerShutter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SuplaRollerShutter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SuplaRollerShutter(Context context) {
        super(context);
        init();
    }

    private void init() {

        FrameLineWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 2, metrics);

        Spaceing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 3, metrics);

        LouverSpaceing = Spaceing/2f;
    }

    public float getFrameLineWidth() {
        return FrameLineWidth;
    }

    public void setFrameLineWidth(float frameLineWidth) {
        FrameLineWidth = frameLineWidth;
        invalidate();
    }

    public float getSpaceing() {
        return Spaceing;
    }

    public void setSpaceing(float spaceing) {
        Spaceing = spaceing;
        invalidate();
    }

    public int getWindowFrameLineColor() {
        return WindowFrameLineColor;
    }

    void setWindowFrameLineColor(int windowFrameLineColor) {
        WindowFrameLineColor = windowFrameLineColor;
        invalidate();
    }

    int getGlassColor() {
        return GlassColor;
    }

    public void setGlassColor(int  glassColor) {
        GlassColor = glassColor;
        invalidate();
    }

    public int getMarkerColor() {
        return MarkerColor;
    }

    public void setMarkerColor(int markerColor) {
        MarkerColor = markerColor;
        invalidate();
    }

    public void setMarkers(ArrayList<Float> markers) {
        if (markers == null) {
            Markers = null;
        } else {
            Markers = new ArrayList<>(markers);
        }
        invalidate();
    }

    public int getSunColor() {
        return SunColor;
    }

    public void setSunColor(int sunColor) {
        SunColor = sunColor;
        invalidate();
    }

    public int getRollerShutterColor() {
        return RollerShutterColor;
    }

    public void setRollerShutterColor(int rollerShutterColor) {
        RollerShutterColor = rollerShutterColor;
        invalidate();
    }

    public int getRollerShutterLineColor() {
        return RollerShutterLineColor;
    }

    public void setRollerShutterLineColor(int rollerShutterLineColor) {
        RollerShutterLineColor = rollerShutterLineColor;
        invalidate();
    }

    public int getRollerBackgroundShutterColor() {
        return RollerShutterBackgroundColor;
    }

    public void setRollerShutterBackgroundColor(int rollerShutterBackgroundColor) {
        RollerShutterBackgroundColor = rollerShutterBackgroundColor;
        invalidate();
    }

    public float getPercent() {
        return Percent;
    }

    public void setPercent(float percent) {

        if (percent < 0)
            percent = 0;
        else if (percent > 100)
            percent = 100;

        Percent = percent;
        invalidate();
    }

    public float getBottomPosition() {
        return bottomPosition;
    }

    public void setBottomPosition(float bottomPosition) {
        if (bottomPosition < 0) {
            bottomPosition = 0;
        } else if (bottomPosition > 100) {
            bottomPosition = 100;
        }
        this.bottomPosition = bottomPosition;
    }

    public int getLouverCount() {
        return LouverCount;
    }

    public void setLouverCount(int louverCount) {
        LouverCount = louverCount;
        invalidate();
    }

    public float getLouverSpaceing() {
        return LouverSpaceing;
    }

    public void setLouverSpaceing(float louverSpaceing) {
        LouverSpaceing = louverSpaceing;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
/*
        int bgColor = Color.TRANSPARENT;

        if (getBackground() instanceof ColorDrawable) {
            bgColor = ((ColorDrawable) getBackground().mutate()).getColor();
        }

        if (bgColor == Color.TRANSPARENT)
            bgColor = Color.WHITE;

        canvas.drawColor(bgColor);
*/
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(WindowFrameLineColor);
        paint.setAntiAlias(true);

        paint.setStrokeWidth(FrameLineWidth);
        float hFLW = FrameLineWidth / 2;

        float lrMargin = FrameLineWidth * (float) 0.5;

        float left = hFLW + lrMargin;
        float top = hFLW;
        float right = getWidth() - hFLW - lrMargin;
        float bottom = getHeight() - hFLW;

        rectf.set(left, top, right, bottom);

        paint.setColor(WindowFrameColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRoundRect(rectf, 1, 1, paint);

        paint.setColor(WindowFrameLineColor);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(rectf, 1, 1, paint);

        left = lrMargin + FrameLineWidth + Spaceing;
        top = FrameLineWidth + Spaceing;
        right = getWidth() - lrMargin - FrameLineWidth - Spaceing;
        bottom = getHeight() - FrameLineWidth - Spaceing;

        hFLW = Spaceing / 2;

        float w = (right - left) / 2 - Spaceing / 2;
        float h = (bottom - top) / 2 - Spaceing / 2;

        for(int a=0;a<2;a++) {
            if (a == 0) {
                paint.setColor(GlassColor);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
            } else {
                paint.setColor(WindowFrameLineColor);
                paint.setStyle(Paint.Style.STROKE);
            }

            rectf.set(left + hFLW, top + hFLW, left + w - hFLW, top - hFLW + h);
            canvas.drawRect(rectf, paint);
            rectf.set(right - w + hFLW, top + hFLW, right - hFLW, top - hFLW + h);
            canvas.drawRect(rectf, paint);
            rectf.set(left + hFLW, bottom - h + hFLW, left + w - hFLW, bottom - hFLW);
            canvas.drawRect(rectf, paint);
            rectf.set(right - w + hFLW, bottom - h + hFLW, right - hFLW, bottom - hFLW);
            canvas.drawRect(rectf, paint);
        }


        left = right - w + Spaceing;
        top = top + Spaceing;
        right = right - Spaceing;
        bottom = top - Spaceing + h;

        w = (right - left) / 6;
        h = (bottom - top) / 6;

        if (w < h)
            h = w;

        paint.setStrokeWidth(FrameLineWidth / (float) 1.5);
        paint.setColor(SunColor);

        float x = right - h * (float) 2;
        float y = top + h * (float) 2;

        canvas.drawCircle(x, y, h, paint);

        float ray_n = h + (h * (float) 0.2);
        float ray_m = h * (float) 0.4;

        for (int a = 0; a < 360; a += 30) {

            double r = Math.toRadians(a);

            float ray_x = (float) (x + Math.cos(r) * (ray_n));
            float ray_y = (float) (y + Math.sin(r) * (ray_n));

            canvas.drawLine(ray_x,
                    ray_y,
                    (float) (ray_x + Math.cos(r) * (ray_m)),
                    (float) (ray_y + Math.sin(r) * (ray_m)),
                    paint);

        }

        float percent = Moving ? virtPercent : Percent;

        if (!Moving && percent == 0 && Markers != null && !Markers.isEmpty()) {

            float markerHalfHeight = Spaceing + FrameLineWidth / 2;
            float markerArrowWidth = Spaceing * 2;
            float markerWidth = getWidth() / 20 + markerArrowWidth;
            float markerMargin = FrameLineWidth / 2;

            paint.setStrokeWidth(metrics.density);

            float pos;

            for (int a = 0; a < Markers.size(); a++) {
                pos = (float) ((getHeight() - markerHalfHeight * 2) * Markers.get(a) / 100.00) + markerHalfHeight;

                for (short b = 0; b < 2; b++) {

                    if (b == 0) {
                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(MarkerColor);
                    } else {
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setColor(Color.WHITE);
                    }

                    markerPath.reset();

                    markerPath.moveTo(markerMargin, pos);
                    markerPath.lineTo(markerMargin + markerArrowWidth, pos - markerHalfHeight);
                    markerPath.lineTo(markerMargin + markerWidth, pos - markerHalfHeight);
                    markerPath.lineTo(markerMargin + markerWidth, pos + markerHalfHeight);
                    markerPath.lineTo(markerMargin + markerArrowWidth, pos + markerHalfHeight);
                    markerPath.lineTo(markerMargin, pos);

                    canvas.drawPath(markerPath, paint);
                }


            }
        }

        if (percent == 0 || LouverCount <= 0)
            return;

        if (percent > 100)
            percent = 100;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(RollerShutterBackgroundColor);

        h = getHeight();

        if (bottomPosition > 0) {
            if (percent <= bottomPosition) {
                h *= (percent * 100 / bottomPosition) / 100;
            }
        } else {
            h *= percent / 100;
        }

        rectf.set(0, 0, getWidth(), h);
        canvas.drawRect(rectf, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Moving ? (WindowFrameLineColor & 0x00ffffff) | (50 << 24) : WindowFrameLineColor);
        paint.setStrokeWidth(FrameLineWidth);

        hFLW = FrameLineWidth / 2;

        float LouverHeight = (getHeight() - LouverSpaceing * (LouverCount - 1)) / LouverCount - FrameLineWidth;
        h -= hFLW;

        int _LouverCount = LouverCount;

        if (bottomPosition > 0 && percent > bottomPosition) {
            _LouverCount = (int)(getHeight() / LouverHeight)+1;
        }

        for (int a = 0; a < _LouverCount; a++) {
            if (h < 0)
                break;

            float _LouverSpaceing = LouverSpaceing;

            if (bottomPosition > 0 && percent > bottomPosition) {
                float n = (_LouverCount-1)-((100-percent)/((100- bottomPosition) / (_LouverCount-1)));

                if (n-a > 0) {
                    n = 1 - (n-a);
                    _LouverSpaceing = LouverSpaceing * n;
                    if (_LouverSpaceing < 0) {
                        _LouverSpaceing = 0;
                    }
                }
            }

            rectf.set(hFLW, h - LouverHeight, getWidth() - hFLW, h);

            paint.setColor(RollerShutterColor);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawRect(rectf, paint);

            paint.setColor(RollerShutterLineColor);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rectf, paint);
            h = h - LouverHeight - _LouverSpaceing - FrameLineWidth;

        }

        h = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 0.5, metrics);

        paint.setStrokeWidth(h);

        h /= 2;

        canvas.drawLine(h, 0, getWidth(), 0, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        float x = event.getX();
        float y = event.getY();
        boolean result = false;

        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                if (mOnTouchListener != null) {
                    mOnTouchListener.onPercentChanged(this, virtPercent);
                }

                Moving = false;
                invalidate();
                break;
            case MotionEvent.ACTION_DOWN:
                virtPercent = Percent;
                result = true;
                break;
            case MotionEvent.ACTION_MOVE:
                Moving = true;
                float delta = y - MoveY;

                if (Math.abs(x - MoveX) < Math.abs(delta)) {

                    float p = Math.abs(delta) * 100 / getHeight();

                    if (delta > 0)
                        virtPercent += p;
                    else
                        virtPercent -= p;

                    if (virtPercent < 0)
                        virtPercent = 0;
                    else if (virtPercent > 100)
                        virtPercent = 100;

                    if (mOnTouchListener != null) {
                        mOnTouchListener.onPercentChangeing(this, virtPercent);
                    }

                    result = true;
                }

                invalidate();
                break;
        }

        MoveX = x;
        MoveY = y;

        return result;
    }

    public void setOnPercentTouchListener(OnTouchListener l) {
        mOnTouchListener = l;
    }

    public interface OnTouchListener {
        void onPercentChanged(SuplaRollerShutter rs, float percent);

        void onPercentChangeing(SuplaRollerShutter rs, float percent);
    }
}
