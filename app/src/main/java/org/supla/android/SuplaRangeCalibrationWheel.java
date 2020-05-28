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

package org.supla.android;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class SuplaRangeCalibrationWheel extends View {

    private final int TOUCHED_NONE = 0;
    private final int TOUCHED_LEFT = 1;
    private final int TOUCHED_RIGHT = 2;

    private Paint paint;
    private RectF rectF;

    private float borderLineWidth;
    private float wheelCenterX;
    private float wheelCenterY;
    private float wheelRadius;
    private float wheelWidth;
    private int touched = TOUCHED_NONE;
    private PointF btnLeftCenter;
    private PointF btnRightCenter;
    private float halfBtnSize;
    private double btnRad;
    private double lastTouchedDegree;

    private int wheelColor = 0xB0ABAB;
    private int borderColor = 0x575757;
    private int btnColor = 0x575757;
    private int rangeColor = 0xFFE617;
    private int boostLineColor = 0x12A61F;
    private int insideBtnColor = 0xFFFFFF;

    private double maximumValue = 1000;
    private double minimumRange = maximumValue * 0.1;
    private double numerOfTurns = 5;
    private double minimum = 0;
    private double maximum = maximumValue;
    private double leftEdge = 0;
    private double rightEdge = maximumValue;
    private double boostLevel = 0;
    private boolean boostVisible = false;
    private double boostLineHeightFactor = 1.8;

    private OnChangeListener onChangeListener = null;


    public SuplaRangeCalibrationWheel(Context context) {
        super(context);
        init();
    }

    public SuplaRangeCalibrationWheel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SuplaRangeCalibrationWheel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SuplaRangeCalibrationWheel(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        rectF = new RectF();
        btnLeftCenter = null;
        btnRightCenter = null;
        btnRad = 0;

        Resources res = getResources();
        if (res == null) {
            borderLineWidth = (float) 1.5;
        } else {
            DisplayMetrics metrics = res.getDisplayMetrics();
            borderLineWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    1.5F, metrics);
        }

    }

    public double getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(double maximumValue) {

        if (maximumValue < getMinimumRange()) {
            maximumValue = getMinimumRange();
        }

        this.maximumValue = maximumValue;

        if (getRightEdge() > maximumValue) {
            setRightEdge(maximumValue);
        }

        invalidate();
    }

    public double getMinimumRange() {
        return this.minimumRange;
    }

    public void setMinimumRange(double minimumRange) {
        if (minimumRange < 0) {
            minimumRange = 0;
        }

        if (minimumRange > getMaximumValue()) {
            minimumRange = getMaximumValue();
        }

        this.minimumRange = minimumRange;

        if (minimumRange > getRightEdge() - getLeftEdge()) {
            double diff = (minimumRange - (getRightEdge() - getLeftEdge())) / 2.0F;
            if (diff > getLeftEdge()) {
                setLeftEdge(0);
                setRightEdge(minimumRange);
            } else if (diff + getRightEdge() > getMaximumValue()) {
                setRightEdge(getMaximumValue());
                setLeftEdge(getRightEdge() - minimumRange);
            } else {
                setLeftEdge(getLeftEdge() - diff);
            }
        }

        if (minimumRange > getMaximum() - getMinimum()) {
            double diff = (minimumRange - (getMaximum() - getMinimum())) / 2;
            if (getMinimum() - diff < getLeftEdge()) {
                setMinMax(getLeftEdge(), getMinimum() + minimumRange);
            } else if (getMaximum() + diff > getRightEdge()) {
                setMinMax(getMaximum() - minimumRange, getRightEdge());
            } else {
                setMinMax(getMinimum() - diff, getMaximum() + diff);
            }
        }
    }

    public double getNumerOfTurns() {
        return numerOfTurns;
    }

    public void setNumerOfTurns(double numerOfTurns) {
        if (numerOfTurns < 1) {
            numerOfTurns = 1;
        }
        this.numerOfTurns = numerOfTurns;
    }

    private void setMinimum(double minimum, boolean inv) {
        if (minimum + getMinimumRange() > getMaximum()) {
            minimum = getMaximum() - getMinimumRange();
        }

        if (minimum < getLeftEdge()) {
            minimum = getLeftEdge();
        }

        this.minimum = minimum;

        if (minimum > getBoostLevel()) {
            boostLevel = minimum;
        }


        if (inv) {
            invalidate();
        }
    }

    public double getMinimum() {
        return minimum;
    }

    public void setMinimum(double minimum) {
        setMinimum(minimum, true);
    }

    private void setMaximum(double maximum, boolean inv) {
        if (getMinimum() + getMinimumRange() > maximum) {
            maximum = getMinimum() + getMinimumRange();
        }

        if (maximum > getRightEdge()) {
            maximum = getRightEdge();
        }

        this.maximum = maximum;

        if (maximum < boostLevel) {
            boostLevel = maximum;
        }

        if (inv) {
            invalidate();
        }
    }

    public double getMaximum() {
        return maximum;
    }

    public void setMaximum(double maximum) {
        setMaximum(maximum, true);
    }

    public double getRightEdge() {
        return rightEdge;
    }

    public void setRightEdge(double rightEdge) {

        if (rightEdge < getMinimumRange()) {
            rightEdge = getMinimumRange();
        }

        if (rightEdge > getMaximumValue()) {
            rightEdge = getMaximumValue();
        }

        this.rightEdge = rightEdge;

        if (getLeftEdge() + getMinimumRange() > rightEdge) {
            setLeftEdge(rightEdge - getMinimumRange());
        }

        double min = getMinimum();
        setMinimum(0, false);
        setMaximum(getMaximum(), false);
        setMinimum(min);
    }

    public double getLeftEdge() {
        return leftEdge;
    }

    public void setLeftEdge(double leftEdge) {
        if (leftEdge < 0) {
            leftEdge = 0;
        }

        this.leftEdge = leftEdge;

        if (leftEdge + getMinimumRange() > getRightEdge()) {
            setRightEdge(leftEdge + getMinimumRange());
        }

        setMinimum(getMinimum());
    }

    public double getBoostLevel() {
        return boostLevel;
    }

    public void setBoostLevel(double boostLevel) {
        if (boostLevel < getMinimum()) {
            boostLevel = getMinimum();
        }

        if (boostLevel > getMaximum()) {
            boostLevel = getMaximum();
        }

        this.boostLevel = boostLevel;

        if (boostVisible) {
            invalidate();
        }
    }

    public boolean isBoostVisible() {
        return boostVisible;
    }

    public void setBoostVisible(boolean boostVisible) {
        this.boostVisible = boostVisible;
        invalidate();
    }

    private void drawBtnLines(Canvas canvas, RectF rectF) {
        final int lc = 3;

        float hMargin = rectF.height() * 0.35F;
        float wMargin = rectF.width() * 0.2F;

        rectF = new RectF(rectF);
        rectF.left += wMargin;
        rectF.right -= wMargin;
        rectF.top += hMargin;
        rectF.bottom -= hMargin;

        float step = rectF.height() / (lc - 1);
        float width = borderLineWidth * 1F;

        for (int a = 0; a < lc; a++) {
            RectF lineRectF = new RectF();
            lineRectF.set(rectF.left, rectF.top + step * a - width / 2,
                    rectF.right, rectF.top + step * a + width / 2);

            canvas.drawRoundRect(
                    lineRectF,
                    15,
                    15,
                    paint
            );
        }
    }

    private PointF drawButton(Canvas canvas, double rad, boolean visible) {

        float btnSize = wheelWidth + 4 * borderLineWidth;
        halfBtnSize = btnSize / 2;
        float x = wheelCenterX + wheelRadius - borderLineWidth;

        PointF result = new PointF();
        result.x = (float) (Math.cos(rad) * wheelRadius) + wheelCenterX;
        result.y = (float) (Math.sin(rad) * wheelRadius) + wheelCenterY;

        if (!visible) {
            return result;
        }

        canvas.save();
        canvas.rotate((float) Math.toDegrees(rad), wheelCenterX, wheelCenterY);

        paint.setColor(btnColor);
        paint.setStyle(Paint.Style.FILL);
        rectF.set(x - halfBtnSize, wheelCenterY - halfBtnSize,
                x + halfBtnSize, wheelCenterY + halfBtnSize);

        canvas.drawRoundRect(
                rectF,
                15,
                15,
                paint
        );

        paint.setColor(insideBtnColor);

        drawBtnLines(canvas, rectF);

        canvas.restore();

        return result;
    }

    private PointF drawButton(Canvas canvas, double rad) {
        return drawButton(canvas, rad, true);
    }

    private void drawValue(Canvas canvas) {

        float distanceToEdge = halfBtnSize + borderLineWidth * 2;
        float left = btnLeftCenter.x + distanceToEdge;
        float top = btnLeftCenter.y - halfBtnSize;
        float right = btnRightCenter.x - distanceToEdge;
        float bottom = btnRightCenter.y + halfBtnSize;

        float vleft = left + (float) ((right - left) * minimum / maximumValue);
        float vright = left + (float) ((right - left) * maximum / maximumValue);

        paint.setColor(rangeColor);
        paint.setStyle(Paint.Style.FILL);
        rectF.set(vleft, top, vright, bottom);

        canvas.drawRoundRect(
                rectF,
                15,
                15,
                paint);

        paint.setColor(borderColor);
        paint.setStrokeWidth(borderLineWidth);
        paint.setStyle(Paint.Style.STROKE);
        rectF.set(left, top, right, bottom);

        canvas.drawRoundRect(
                rectF,
                15,
                15,
                paint
        );

        if (boostVisible) {
            vleft = left + (float) ((right - left) * boostLevel / maximumValue);
            if (boostLevel >= (maximum - minimum) / 2) {
                vleft -= borderLineWidth;
            } else {
                vleft += borderLineWidth;
            }

            float hh = ((bottom - top) + borderLineWidth * 2.0f)
                    * (float)boostLineHeightFactor / 2.0f;

            paint.setColor(boostLineColor);
            canvas.drawLine(vleft, btnLeftCenter.y - hh ,
                    vleft, btnLeftCenter.y + hh, paint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        wheelCenterX = this.getWidth() / 2;
        wheelCenterY = this.getHeight() / 2;

        wheelRadius = wheelCenterX > wheelCenterY ? wheelCenterY : wheelCenterX;
        wheelRadius *= 0.7;
        wheelWidth = wheelRadius * 0.25F;

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(borderColor);
        paint.setStrokeWidth(wheelWidth);
        rectF.set(wheelCenterX - wheelRadius, wheelCenterY - wheelRadius,
                wheelCenterX + wheelRadius, wheelCenterY + wheelRadius);
        canvas.drawOval(rectF, paint);

        paint.setStrokeWidth(wheelWidth - borderLineWidth * 2);
        paint.setColor(wheelColor);
        canvas.drawOval(rectF, paint);

        if (touched == TOUCHED_NONE) {
            btnRightCenter = drawButton(canvas, 0);
            btnLeftCenter = drawButton(canvas, (float) Math.toRadians(180), !boostVisible);
        } else {
            if (touched == TOUCHED_RIGHT) {
                drawButton(canvas, btnRad);
            } else if (touched == TOUCHED_LEFT) {
                drawButton(canvas, btnRad, !boostVisible);
            }
        }

        drawValue(canvas);

    }

    private boolean btnTouched(PointF btnCenter, PointF touchPoint) {
        if (btnCenter != null) {
            float touchRadius = (float) Math.sqrt(Math.pow(touchPoint.x - btnCenter.x, 2)
                    + Math.pow(touchPoint.y - btnCenter.y, 2));
            return touchRadius <= halfBtnSize * 1.1;
        }
        return false;
    }

    private double touchPointToRadian(PointF touchPoint) {
        return Math.atan2(touchPoint.y - wheelCenterY,
                touchPoint.x - wheelCenterX);
    }

    private void onRangeChanged(boolean minimum) {
        if (onChangeListener != null) {
            onChangeListener.onRangeChanged(this, minimum);
        }
    }

    private void onBoostChanged() {
        if (onChangeListener != null) {
            onChangeListener.onBoostChanged(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        PointF touchPoint = new PointF();

        touchPoint.x = event.getX();
        touchPoint.y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                touched = TOUCHED_NONE;
                btnRad = 0;
                invalidate();
                break;

            case MotionEvent.ACTION_DOWN:
                if (touched == TOUCHED_NONE) {
                    if (!boostVisible && btnTouched(btnLeftCenter, touchPoint)) {
                        touched = TOUCHED_LEFT;
                        btnRad = Math.toRadians(180);
                        onRangeChanged(true);
                    } else if (btnTouched(btnRightCenter, touchPoint)) {
                        touched = TOUCHED_RIGHT;
                        btnRad = 0;
                        if (boostVisible) {
                            onBoostChanged();
                        } else {
                            onRangeChanged(false);
                        }
                    }

                    if (touched != TOUCHED_NONE) {
                        lastTouchedDegree = Math.toDegrees(touchPointToRadian(touchPoint));
                        invalidate();
                        return true;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (touched != TOUCHED_NONE) {

                    btnRad = touchPointToRadian(touchPoint);
                    double touchedDegree = Math.toDegrees(btnRad);

                    double diff = touchedDegree - lastTouchedDegree;
                    if (Math.abs(diff) > 100) {
                        diff = 360 - Math.abs(lastTouchedDegree) - Math.abs(touchedDegree);
                        if (touchedDegree > 0) {
                            diff *= -1;
                        }
                    }

                    if (Math.abs(diff) <= 20) {
                        diff = (diff * 100.0 / 360.0) * maximumValue / 100 / numerOfTurns;
                        if (touched == TOUCHED_LEFT) {
                            setMinimum(getMinimum() + diff, false);
                            onRangeChanged(true);
                        } else {
                            if (boostVisible) {
                                setBoostLevel(getBoostLevel() + diff);
                                onBoostChanged();
                            } else {
                                setMaximum(getMaximum() + diff, false);
                                onRangeChanged(false);
                            }
                        }
                    }

                    lastTouchedDegree = touchedDegree;
                    invalidate();
                    return true;
                }
                break;
        }


        return super.onTouchEvent(event);
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    public int getWheelColor() {
        return wheelColor;
    }

    public void setWheelColor(int wheelColor) {
        this.wheelColor = wheelColor;
        invalidate();
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        invalidate();
    }

    public int getBtnColor() {
        return btnColor;
    }

    public void setBtnColor(int btnColor) {
        this.btnColor = btnColor;
        invalidate();
    }

    public int getRangeColor() {
        return rangeColor;
    }

    public void setRangeColor(int rangeColor) {
        this.rangeColor = rangeColor;
        invalidate();
    }

    public int getBoostLineColor() {
        return boostLineColor;
    }

    public void setBoostLineColor(int boostLineColor) {
        this.boostLineColor = boostLineColor;
        invalidate();
    }

    public double getBoostLineHeightFactor() {
        return boostLineHeightFactor;
    }

    public void setBoostLineHeightFactor(double boostLineHeightFactor) {
        if (boostLineHeightFactor > 0 && boostLineHeightFactor <= 2) {
            this.boostLineHeightFactor = boostLineHeightFactor;
        }
    }

    public int getInsideBtnColor() {
        return insideBtnColor;
    }

    public void setInsideBtnColor(int insideBtnColor) {
        this.insideBtnColor = insideBtnColor;
        invalidate();
    }

    public float getBorderLineWidth() {
        return borderLineWidth;
    }

    public void setBorderLineWidth(float borderLineWidth) {
        if (borderLineWidth < 0.1F) {
            borderLineWidth = 0.1F;
        }
        this.borderLineWidth = borderLineWidth;
    }

    public void setMinMax(double min, double max) {
        setMinimum(0, false);
        setMaximum(getMaximumValue(), false);
        setMinimum(min, false);
        setMaximum(max);
    }

    public interface OnChangeListener {
        void onRangeChanged(SuplaRangeCalibrationWheel calibrationWheel, boolean minimum);

        void onBoostChanged(SuplaRangeCalibrationWheel calibrationWheel);
    }
}
