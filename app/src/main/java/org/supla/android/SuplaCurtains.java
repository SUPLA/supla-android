package com.example.curtains;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
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

public class SuplaCurtains extends View {

    //Variables
    private final DisplayMetrics Metrics = getResources() == null ? null : getResources().getDisplayMetrics();
    private final Rect MainRect = new Rect();
    private final RectF MainRoundRect = new RectF();
    private final Path MainPath = new Path();
    private final Paint paint = new Paint();
    private final Paint linePaint = new Paint();
    private final Paint greenPaint = new Paint();
    private final Paint darkgreenPaint = new Paint();
    private float mFirstClick, mCurtainWidth, mCurtainMargin;
    private float mElWidth, mLastTouchX, mLastTouchY, mPercent;
    private float tempP = 0;
    private double mThickness = 1, mLineSubtract;
    private boolean mTouchLeft;
    private int mLineColor = 0x000000;
    private int mFillColor1 = 0x05AA37;
    private int mFillColor2 = 0x049629;
    private OnPercentChangeListener mOnPercentChangeListener;
    private RectF workSpace = new RectF();

    public SuplaCurtains(Context context) {
        super(context);
    }

    public SuplaCurtains(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SuplaCurtains(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public interface OnPercentChangeListener {
        void onPercentChangeing(SuplaCurtains rs, float percent);
    }

    //Colors Getters and Setters

    public int getLineColor() { return mLineColor; }

    public void setLineColor(int lineColor) {
        mLineColor = lineColor;
        invalidate();
    }

    public int getFillColor1() { return mFillColor1; }

    public void setFillColor1(int fillColor1) {
        mFillColor1 = fillColor1;
        invalidate();
    }

    public int getFillColor2() { return mFillColor2; }

    public void setFillColor2(int fillColor2) {
        mFillColor2 = fillColor2;
        invalidate();
    }

    public void setPercent(float percent) {
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }
        mPercent = percent;
        calculateConstants();
        invalidate();
    }

    public float getPercent() { return mPercent; }

    private void drawRect(Canvas canvas, float leftPercent, float topPercent,
                          float rightPercent, float bottomPercent, Paint paint) {
        MainRect.set(
                (int)(workSpace.width() * leftPercent + workSpace.left),
                (int)(workSpace.height() * topPercent + workSpace.top),
                (int)(workSpace.width() * rightPercent + workSpace.left),
                (int)(workSpace.height() * bottomPercent + workSpace.top));
        canvas.drawRect(MainRect, paint);
    }

    private void drawRoundRect(Canvas canvas, float leftPercent, float topPercent,
                               float rightPercent, float bottomPercent, int rx, int ry, Paint paint) {
        MainRoundRect.set(
                workSpace.width() * leftPercent + workSpace.left,
                workSpace.height() * topPercent + workSpace.top,
                workSpace.width() * rightPercent + workSpace.left,
                workSpace.height() * bottomPercent + workSpace.top);
        canvas.drawRoundRect(MainRoundRect,rx,ry, paint);
    }

    private void drawCurtain(Canvas canvas, float left, float right,
                             float bottomPercent, Paint colorPaint) {
        MainRoundRect.set(
                workSpace.left + left,
                workSpace.top + workSpace.height() * 0.05f,
                workSpace.left + right,
                workSpace.top + workSpace.height() * bottomPercent);
        canvas.drawRoundRect(MainRoundRect, 7, 5, colorPaint);
        canvas.drawRoundRect(MainRoundRect, 7, 5, paint);
    }

    private void drawCurtains(Canvas canvas, boolean right) {
        float width = mCurtainWidth + mElWidth;
        int elCount;
        float l, r;

        elCount = (int)(width / mElWidth);

        if (right) {
            width = workSpace.width() - width - mCurtainMargin;
        }
        for (int i = 0; i < elCount; i++) {
            if (right) {
                l = width + (mElWidth * i);
                r = width + (mElWidth *( i + 1 ));
            } else {
                l = mCurtainMargin + width - (mElWidth * (i + 1));
                r = mCurtainMargin + width - (mElWidth * i);
            }

            if(i%2 == 0) {
                drawCurtain(canvas,  l, r, (float) (1 - mLineSubtract), greenPaint);
            } else {
                drawCurtain(canvas, l, r, 0.97f, darkgreenPaint);
            }
        }

        if (right) {
            drawCurtain(canvas,workSpace.width()-mCurtainMargin - mElWidth,
                    workSpace.width()-mCurtainMargin,(float) (1 - mLineSubtract), greenPaint);
        } else {
            drawCurtain(canvas, mCurtainMargin,mCurtainMargin + mElWidth,
                    (float) (1 - mLineSubtract), greenPaint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mFirstClick = event.getY();
                mLastTouchX = event.getX();
                mLastTouchY = event.getY();
                mTouchLeft = mLastTouchX < workSpace.width() / 2 + workSpace.left;
                tempP = getPercent();
            case MotionEvent.ACTION_MOVE:
                float X = event.getX();
                float Y = event.getY();
                float dX = X - mLastTouchX;
                float dY = Y - mLastTouchY;

                if (((workSpace.height() * 1) + ((getHeight()-workSpace.height())/2) > mFirstClick)
                        && ((workSpace.height() * 0.08 +
                        ((getHeight()-workSpace.height())/2) < mFirstClick))) {
                    if (Math.abs(dX) > Math.abs(dY)) {
                        if (mTouchLeft) {
                            dX *= -1;
                        }
                        float p = dX * 100.f / workSpace.width() * 2f;
                        setPercent(getPercent() - p);
                    }
                }
                mLastTouchX = X;
                mLastTouchY = Y;
                if (mOnPercentChangeListener != null) {
                    mOnPercentChangeListener.onPercentChangeing(this, getPercent());
                }
                return true;
            case MotionEvent.ACTION_UP:
                setPercent(tempP);
                return true;
        }
        return false;
    }

    private void setCourtainWidth(float width) {
        if (width < 0) {
            width = 0;
        } else if (width > (workSpace.width() / 2) - mElWidth) {
            width = (workSpace.width() / 2) - mElWidth;
        }
        mCurtainWidth = width;
    }

    private void calculateConstants() {
        if (getWidth() > getHeight()) {
            workSpace = new RectF(
                    (float)(getWidth()-getHeight())/2,
                    0,
                    getHeight()+ (float) ((getWidth()-getHeight())/2),
                    getHeight());
            mThickness = workSpace.height() * 0.003;
            if (mThickness < 0.5) mThickness = 0.5;
            mLineSubtract = mThickness * 2 / workSpace.height();
        } else if (getWidth() < getHeight()) {
            workSpace = new RectF(
                    0,
                    (float) (getHeight()-getWidth())/2,getWidth(),
                    getWidth() + (float) ((getHeight()-getWidth())/2));
            mThickness = workSpace.width() * 0.003;
            if (mThickness < 0.5) mThickness = 0.5;
            mLineSubtract = mThickness * 2 / workSpace.width();
        } else {
            workSpace = new RectF(0,0,getWidth(), getHeight());
            mThickness = workSpace.height() * 0.003;
            if (mThickness < 0.5) mThickness = 0.5;
            mLineSubtract = mThickness * 2 / workSpace.height();
        }
        mElWidth = workSpace.width() * 0.05f;
        mCurtainMargin = workSpace.width() * 0.05f;
        setCourtainWidth((((workSpace.width() - mCurtainMargin * 2) / 2f) - mElWidth) * mPercent / 100f);
    }

    @Override
    public void onDraw(Canvas canvas) {

        calculateConstants();

        //Variables
        float FrameLineWidth =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) mThickness, Metrics);

        //Paint Stroke Line
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(FrameLineWidth);
        paint.setColor(mLineColor);

        //Paint Fill
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(mLineColor);

        //Paint Fill Color 1
        greenPaint.setStyle(Paint.Style.FILL);
        greenPaint.setColor(mFillColor1);

        //Paint Fill Color 2
        darkgreenPaint.setStyle(Paint.Style.FILL);
        darkgreenPaint.setColor(mFillColor2);

        //Window Picture Elements

        //Window frame L
        drawRect(canvas, 0.2f, 0.16f, 0.24f, 0.88f, paint);

        //Window frame R
        drawRect(canvas, 0.76f, 0.16f, 0.80f, 0.88f, paint);

        //Glass T L
        drawRect(canvas, 0.24f, 0.16f, 0.45f, 0.45f, paint);

        //Glass T R
        drawRect(canvas, 0.55f, 0.16f, 0.76f, 0.45f, paint);

        //Glass B L
        drawRect(canvas, 0.24f, 0.49f, 0.45f, 0.88f, paint);

        //Glass B R
        drawRect(canvas, 0.55f, 0.49f, 0.76f, 0.88f, paint);

        //Windowsill
        drawRect(canvas, 0.16f, 0.88f, 0.84f, 0.95f, paint);

        //Windowsill L
        drawRoundRect(canvas, 0.145f,0.87f, 0.16f, 0.96f,
                3, 3, paint);

        //Windowsill R
        drawRoundRect(canvas, 0.84f, 0.87f, 0.855f, 0.96f,
                3, 3, paint);

        //Cornice T
        drawRoundRect(canvas, 0.15f, 0.11f, 0.85f, 0.125f,
                8, 8, paint);

        //Cornice B
        drawRoundRect(canvas, 0.18f, 0.125f, 0.82f, 0.16f,
                3, 3, paint);

        //Window line
        canvas.drawLine(
                (int) (workSpace.width() * 0.5 + workSpace.left),
                (int) (workSpace.height() * 0.16f + workSpace.top),
                (int) (workSpace.width() * 0.5 + workSpace.left),
                (int) (workSpace.height() * 0.88f + workSpace.top),
                paint);

        //Handle L
        drawRect(canvas, 0.4675f, 0.56f, 0.4825f, 0.63f, linePaint);

        //Handle R
        drawRect(canvas, 0.5175f, 0.56f, 0.5325f, 0.63f, linePaint);

        //Handle base L
        canvas.drawCircle(
                (int) (workSpace.width() * 0.475 + workSpace.left),
                (int) (workSpace.height() * 0.56 + workSpace.top),
                (int) (workSpace.width() * 0.01),
                paint);

        //Handle base R
        canvas.drawCircle(
                (int) (workSpace.width() * 0.525 + workSpace.left),
                (int) (workSpace.height() * 0.56 + workSpace.top),
                (int) (workSpace.width() * 0.01),
                paint);

        //Curtain rod
        drawRoundRect(canvas,
                (float) (0 + mLineSubtract),
                (float) (0 + mLineSubtract), (float) (1 - mLineSubtract),
                0.05f, 7, 7, paint);

        //Flowerpot
        drawRect(canvas, 0.61f, 0.8f, 0.7f, 0.87f, paint);

        //Flowerpot stand
        drawRoundRect(canvas, 0.6f, 0.87f, 0.71f, 0.88f,
                3, 3, paint);

        //Flowerpot frame
        drawRoundRect(canvas, 0.595f, 0.785f, 0.715f, 0.8f,
                7, 7, paint);

        //Leaf L L
        MainPath.reset();
        MainPath.moveTo(
                workSpace.width() * 0.62f + workSpace.left,
                workSpace.height() * 0.785f + workSpace.top);
        MainPath.quadTo(
                workSpace.width() * 0.585f + workSpace.left,
                workSpace.height() * 0.72f + workSpace.top,
                workSpace.width() * 0.565f  + workSpace.left,
                workSpace.height() * 0.7f + workSpace.top);
        MainPath.quadTo(
                workSpace.width() * 0.615f + workSpace.left,
                workSpace.height() * 0.72f + workSpace.top,
                workSpace.width() * 0.635f + workSpace.left,
                workSpace.height() * 0.785f + workSpace.top);
        MainPath.close();
        canvas.drawPath(MainPath, greenPaint);
        canvas.drawPath(MainPath, paint);

        //Leaf C L
        MainPath.moveTo(
                workSpace.width() * 0.635f + workSpace.left,
                workSpace.height() * 0.785f + workSpace.top);
        MainPath.quadTo(
                workSpace.width() * 0.62f + workSpace.left,
                workSpace.height() * 0.72f + workSpace.top,
                workSpace.width() * 0.595f + workSpace.left,
                workSpace.height() * 0.64f + workSpace.top);
        MainPath.quadTo(
                workSpace.width() * 0.635f + workSpace.left,
                workSpace.height() * 0.67f + workSpace.top,
                workSpace.width() * 0.665f + workSpace.left,
                workSpace.height() * 0.785f + workSpace.top);
        MainPath.close();
        canvas.drawPath(MainPath, greenPaint);
        canvas.drawPath(MainPath, paint);

        //Leaf C R
        MainPath.moveTo(
                workSpace.width() * 0.65f + workSpace.left,
                workSpace.height() * 0.73f + workSpace.top);
        MainPath.quadTo(
                workSpace.width() * 0.67f + workSpace.left,
                workSpace.height() * 0.65f + workSpace.top,
                workSpace.width() * 0.7f + workSpace.left,
                workSpace.height() * 0.61f + workSpace.top);
        MainPath.quadTo(
                workSpace.width() * 0.68f + workSpace.left,
                workSpace.height() * 0.7f + workSpace.top,
                workSpace.width() * 0.665f + workSpace.left,
                workSpace.height() * 0.785f + workSpace.top);
        MainPath.lineTo(
                workSpace.width() * 0.66f + workSpace.left,
                workSpace.height() * 0.785f + workSpace.top);
        MainPath.close();
        canvas.drawPath(MainPath, greenPaint);
        canvas.drawPath(MainPath, paint);

        //Leaf R R
        MainPath.moveTo(
                workSpace.width() * 0.66f + workSpace.left,
                workSpace.height() * 0.785f + workSpace.top);
        MainPath.quadTo(
                workSpace.width() * 0.695f + workSpace.left,
                workSpace.height() * 0.7192f + workSpace.top,
                workSpace.width() * 0.735f + workSpace.left,
                workSpace.height() * 0.6992f + workSpace.top);
        MainPath.quadTo(
                workSpace.width() * 0.685f + workSpace.left,
                workSpace.height() * 0.7752f + workSpace.top,
                workSpace.width() * 0.695f + workSpace.left,
                workSpace.height() * 0.785f + workSpace.top);
        MainPath.close();
        canvas.drawPath(MainPath, greenPaint);
        canvas.drawPath(MainPath, paint);

        //Square pattern T L
        drawRect(canvas, 0.625f, 0.813f, 0.645f, 0.843f, greenPaint);
        drawRect(canvas, 0.625f, 0.813f, 0.645f, 0.843f, paint);

        //Square pattern B L
        drawRect(canvas, 0.6375f, 0.828f, 0.655f, 0.858f, greenPaint);
        drawRect(canvas, 0.6375f, 0.828f, 0.655f, 0.858f, paint);

        //Square pattern T R
        drawRect(canvas, 0.655f, 0.813f, 0.6725f, 0.843f, greenPaint);
        drawRect(canvas, 0.655f, 0.813f, 0.6725f, 0.843f, paint);

        //Square pattern B R
        drawRect(canvas, 0.665f, 0.828f, 0.685f, 0.858f, greenPaint);
        drawRect(canvas, 0.665f, 0.828f, 0.685f, 0.858f, paint);

        //Curtain L Creator
        drawCurtains(canvas, false);

        //Curtain R Creator
        drawCurtains(canvas, true);
    }

    public void setOnPercentChangeListener(OnPercentChangeListener percentChangeListener) {
       mOnPercentChangeListener = percentChangeListener;
    }
}