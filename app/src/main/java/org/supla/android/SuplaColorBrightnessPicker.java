package org.supla.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
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

public class SuplaColorBrightnessPicker extends View {

    static private final int[] Colors = new int[]{
            0xFFFF0000, 0xFFFF00FF,
            0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000
    };
    final static private double m160d = Math.toRadians(-160);
    final static private double m90d = Math.toRadians(-90);
    final static private double m90_01d = Math.toRadians(-90.01);
    final static private double m20d = Math.toRadians(-20);
    final static private double p40d = Math.toRadians(40);
    private int[] BW = new int[]{
            Color.BLACK,
            Color.WHITE,
            Color.WHITE

    };
    private PointF colorPointerCenter;
    private PointF brightnessPointerCenter;
    private RectF rectF = new RectF();
    private float centerX;
    private float centerY;
    private float colorWheelWidth;
    private double pointerHeight;
    private double arrowHeight_a;
    private double arrowHeight_b;
    private float colorWheelRadius;
    private double colorWheelPointerAngle;
    private float brightnessWheelWidth;
    private float brightnessWheelRadius;
    private double brightnessWheelPointerAngle;
    private int selectedColor;
    private double selectedBrightness;
    private Path colorArrowPath;
    private Paint colorArrowPaint;
    private Path brightnessArrowPath;
    private Paint brightnessArrowPaint;
    private Paint paint;
    private Paint cwPaint;   // color wheel paint
    private Shader cwShader; // color wheel shader
    private Paint bwPaint;   // brightness wheel paint
    private Shader bwShader; // brightness wheel shader
    private boolean colorWheelVisible;
    private boolean colorPointerMoving;
    private boolean brightnessWheelPointerMoving;
    private boolean colorfulBrightnessWheel;
    private boolean circleInsteadArrow;
    private double touchDiff;
    private OnColorBrightnessChangeListener mOnChangeListener;
    private ArrayList<Double> colorMarkers;
    private ArrayList<Double> brightnessMarkers;
    private boolean sliderVisible;
    private RectF sliderRect;
    private boolean powerButtonVisible;
    private boolean powerButtonEnabled;
    private boolean powerButtonOn;
    private int powerButtonColorOn;
    private int powerButtonColorOff;
    private float powerButtonRadius;
    private boolean powerButtonTouched;

    public SuplaColorBrightnessPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SuplaColorBrightnessPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SuplaColorBrightnessPicker(Context context) {
        super(context);
        init();
    }

    private void init() {

        colorWheelVisible = true;

        paint = new Paint();

        cwShader = new SweepGradient(0, 0, Colors, null);
        cwPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cwPaint.setStyle(Paint.Style.STROKE);

        bwPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bwPaint.setStyle(Paint.Style.STROKE);

        colorArrowPath = new Path();
        colorArrowPaint = new Paint();

        colorPointerCenter = new PointF();
        brightnessPointerCenter = new PointF();

        brightnessArrowPath = new Path();
        brightnessArrowPaint = new Paint();

        colorPointerMoving = false;
        brightnessWheelPointerMoving = false;
        colorfulBrightnessWheel = true;
        circleInsteadArrow = true;
        powerButtonColorOn = 0xffffffff;
        powerButtonColorOff = 0xff404040;

        powerButtonVisible = true;
        powerButtonEnabled = true;

        setBrightnessValue(0);
        setColor(0xff00ff00);
    }

    private int ave(int s, int d, float p) {
        return s + Math.round(p * (d - s));
    }

    private int calculateColor(float angle, int[] Colors) {
        float unit = (float) (angle / (2 * Math.PI));
        if (unit < 0) {
            unit += 1;
        }

        if (unit <= 0) {
            return Colors[0];
        }
        if (unit >= 1) {
            return Colors[Colors.length - 1];
        }

        float p = unit * (Colors.length - 1);
        int i = (int) p;
        p -= i;

        int c0 = Colors[i];
        int c1 = Colors[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);

        return Color.argb(a, r, g, b);
    }

    private float colorToAngle(int color) {
        float[] colors = new float[3];
        Color.colorToHSV(color, colors);

        return (float) Math.toRadians(-colors[0]);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(widthSize, heightSize);
        setMeasuredDimension(size, size);
    }

    private void drawMarker(Canvas canvas, float x, float y, float markerSize) {
        paint.setAntiAlias(true);
        paint.setStrokeWidth(markerSize / 5);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x, y, markerSize, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);

        canvas.drawCircle(x, y, markerSize, paint);
    }

    private void drawWheelMarkers(Canvas canvas, float radius, float markerSize,
                                  ArrayList<Double> markers, boolean brightness) {
        if (markers == null) {
            return;
        }

        double angle;

        for (int a = 0; a < markers.size(); a++) {

            double v = markers.get(a);
            if (brightness) {
                if (v < 0.5) {
                    v = 0.5;
                } else if (v > 99.5) {
                    v = 99.5;
                }

                angle = brightnessToAngle(v);
            } else {
                angle = colorToAngle((int) v);
            }

            drawMarker(canvas,
                    (float) Math.cos(angle) * radius,
                    (float) Math.sin(angle) * radius,
                    markerSize);


        }

    }

    private void drawSliderMarkers(Canvas canvas, float markerSize) {
        if (brightnessMarkers == null) {
            return;
        }

        float h = sliderRect.height() - (float) pointerHeight;

        for (int a = 0; a < brightnessMarkers.size(); a++) {
            drawMarker(canvas,
                    0,
                    h / 2 - h * brightnessMarkers.get(a).floatValue() / 100f,
                    markerSize);
        }
    }

    private void drawCirclePointer(Canvas canvas, int color, PointF center) {
        float lw = (float) pointerHeight * 0.05f;

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(center.x, center.y, (float) pointerHeight / 2 - lw / 2, paint);

        paint.setStrokeWidth(lw);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);

        canvas.drawCircle(center.x, center.y, (float) pointerHeight / 2 - lw * 1.5f, paint);
    }

    private void drawCirclePointer(Canvas canvas, double angle,
                                   float wheelRadius, int color, PointF center) {

        center.x = (float) Math.cos(angle) * wheelRadius;
        center.y = (float) Math.sin(angle) * wheelRadius;

        drawCirclePointer(canvas, color, center);
    }

    private float trimBrightnessColorAngle(float rad) {
        if (rad >= 0 && rad <= 0.4f) {
            rad = 0.4f;
        } else if (rad >= 2.7 || rad < 0) {
            rad = 2.7f;
        }
        return rad;
    }

    private void drawPowerButton(Canvas canvas, float wheelRadius) {
        powerButtonRadius = wheelRadius * 0.3f;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(powerButtonOn ? powerButtonColorOn : powerButtonColorOff);
        paint.setStrokeWidth(powerButtonRadius * 0.2f);

        Path path = new Path();
        paint.setStrokeCap(Paint.Cap.ROUND);
        RectF rect = new RectF(-powerButtonRadius, -powerButtonRadius,
                powerButtonRadius, powerButtonRadius);
        path.addArc(rect, -60f, 300f);
        path.moveTo(0f, powerButtonRadius * -1f - powerButtonRadius * 0.15f);
        path.lineTo(0f, powerButtonRadius * -1f + powerButtonRadius * 0.6f);

        canvas.drawPath(path, paint);

    }

    private void drawWheel(Canvas canvas) {

        if (colorWheelVisible) {

            cwPaint.setShader(cwShader);
            rectF.set(-colorWheelRadius, -colorWheelRadius, colorWheelRadius, colorWheelRadius);
            canvas.drawOval(rectF, cwPaint);

            if (circleInsteadArrow) {
                drawCirclePointer(canvas, colorWheelPointerAngle,
                        colorWheelRadius, selectedColor, colorPointerCenter);
            } else {
                drawArrow(canvas, colorPointerCenter,
                        colorWheelPointerAngle,
                        colorWheelRadius,
                        colorWheelWidth,
                        -(arrowHeight_a / 4),
                        arrowHeight_a,
                        arrowHeight_b,
                        selectedColor,
                        colorArrowPath,
                        colorArrowPaint);
            }

            drawWheelMarkers(canvas, colorWheelRadius,
                    colorWheelWidth / (circleInsteadArrow ? 9 : 6),
                    colorMarkers, false);
        }

        bwPaint.setStyle(Paint.Style.STROKE);
        bwPaint.setShader(bwShader);
        rectF.set(-brightnessWheelRadius, -brightnessWheelRadius, brightnessWheelRadius, brightnessWheelRadius);
        canvas.drawOval(rectF, bwPaint);

        int negative;
        double arrowOffset;

        if (colorWheelVisible) {
            negative = -1;
            arrowOffset = arrowHeight_a / 4 - brightnessWheelWidth;
        } else {
            negative = 1;
            arrowOffset = -(arrowHeight_a / 4);
        }


        if (circleInsteadArrow) {
            float angle = (float) (brightnessWheelPointerAngle - m90d);
            if (!colorfulBrightnessWheel || !colorWheelVisible
                    || (colorWheelVisible && (selectedColor & 0xffffff) == 0xffffff)) {
                angle = trimBrightnessColorAngle(angle);
            }
            drawCirclePointer(canvas, brightnessWheelPointerAngle,
                    brightnessWheelRadius, calculateColor(angle, BW), brightnessPointerCenter);
        } else {
            drawArrow(canvas, brightnessPointerCenter,
                    brightnessWheelPointerAngle,
                    brightnessWheelRadius,
                    brightnessWheelWidth,
                    arrowOffset,
                    negative * arrowHeight_a,
                    negative * arrowHeight_b,
                    calculateColor((float) (brightnessWheelPointerAngle - m90d), BW),
                    brightnessArrowPath,
                    brightnessArrowPaint);
        }

        drawWheelMarkers(canvas, brightnessWheelRadius,
                brightnessWheelWidth / (circleInsteadArrow ? 9 : 6),
                brightnessMarkers, true);

        if (powerButtonVisible) {
            drawPowerButton(canvas, brightnessWheelRadius * 0.8f);
        }
    }

    private void drawSlider(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        float height = getHeight() - (float) pointerHeight / 2;
        float x = (float) pointerHeight / -2;
        float y = height / -2;

        Path path = new Path();
        sliderRect = new RectF(x, y, x + (float) pointerHeight, y + height);
        path.addRoundRect(sliderRect, 90, 90, Path.Direction.CW);
        canvas.clipPath(path);

        float[] hsv = new float[3];
        Color.colorToHSV(Color.WHITE, hsv);

        for (float a = 0; a < height; a++) {
            hsv[2] = 1 - 1 * (a * 100f / height) / 100f;
            paint.setColor(Color.HSVToColor(hsv));
            canvas.drawLine(x, y + a, x + (float) pointerHeight, y + a, paint);
        }


        height -= pointerHeight;

        brightnessPointerCenter.x = 0;
        brightnessPointerCenter.y = height / 2 - height * (float) selectedBrightness / 100f;

        float percent = (float) selectedBrightness;

        if (percent > 85f) {
            percent = 85f;
        } else if (percent < 15f) {
            percent = 15f;
        }

        hsv[2] = 1 * percent / 100f;
        drawCirclePointer(canvas, Color.HSVToColor(hsv), brightnessPointerCenter);
        drawSliderMarkers(canvas, (float) pointerHeight / 10f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(centerX, centerY);
        if (sliderVisible) {
            drawSlider(canvas);
        } else {
            drawWheel(canvas);
        }
    }

    private void drawArrow(Canvas canvas, PointF center, double topAngle,
                           float wheelRadius, float wheelWidth, double arrowOffset,
                           double arrowHeight_a, double arrowHeight_b,
                           int color, Path arrowPath, Paint arrowPaint) {

        float hh = (float) (arrowHeight_a + arrowHeight_b) / 2.0f;
        double radius = wheelRadius + wheelWidth / 2 + arrowOffset;
        float x = (float) (Math.cos(topAngle) * radius);
        float y = (float) (Math.sin(topAngle) * radius);

        center.x = (float) (Math.cos(topAngle) * (radius + hh));
        center.y = (float) (Math.sin(topAngle) * (radius + hh));

        double arrowRad = Math.toRadians(40);

        double leftX = x + Math.cos(topAngle + arrowRad) * arrowHeight_a;
        double leftY = y + Math.sin(topAngle + arrowRad) * arrowHeight_a;

        double rightX = x + Math.cos(topAngle - arrowRad) * arrowHeight_a;
        double rightY = y + Math.sin(topAngle - arrowRad) * arrowHeight_a;

        double backLeftX = leftX + Math.cos(topAngle) * arrowHeight_b;
        double backLeftY = leftY + Math.sin(topAngle) * arrowHeight_b;

        double backRightX = rightX + Math.cos(topAngle) * arrowHeight_b;
        double backRightY = rightY + Math.sin(topAngle) * arrowHeight_b;

        arrowPath.reset();
        arrowPath.moveTo(x, y);
        arrowPath.lineTo((float) leftX, (float) leftY);
        arrowPath.lineTo((float) backLeftX, (float) backLeftY);

        arrowPath.moveTo(x, y);
        arrowPath.lineTo((float) rightX, (float) rightY);
        arrowPath.lineTo((float) backRightX, (float) backRightY);
        arrowPath.lineTo((float) backLeftX, (float) backLeftY);

        arrowPaint.setColor(color);
        arrowPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(arrowPath, arrowPaint);

        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeJoin(Paint.Join.ROUND);
        arrowPaint.setColor(Color.BLACK);
        canvas.drawPath(arrowPath, arrowPaint);


    }

    private void setBWcolor() {
        int color = colorWheelVisible
                && colorfulBrightnessWheel && !sliderVisible ? selectedColor : Color.WHITE;

        if (BW[1] != color) {
            BW[1] = color;
            BW[2] = color;

            bwShader = new SweepGradient(0, 0, BW, null);
            Matrix gradientRotationMatrix = new Matrix();
            gradientRotationMatrix.preRotate(-90);
            bwShader.setLocalMatrix(gradientRotationMatrix);

        }
    }

    private void _onSizeChanged() {

        float w = this.getWidth() > this.getHeight() ? this.getHeight() : this.getWidth();

        if (sliderVisible) {
            pointerHeight = w / 6.5f;
        } else if (circleInsteadArrow) {
            w /= 7.0f;
            pointerHeight = w;
        } else {
            w /= 10.0f;
            pointerHeight = w * 0.9f;
        }

        colorWheelWidth = w / 2f;
        arrowHeight_a = pointerHeight;
        arrowHeight_b = arrowHeight_a * 0.6;
        arrowHeight_a -= arrowHeight_b;

        brightnessWheelWidth = colorWheelWidth;

        centerX = this.getWidth() / 2f;
        centerY = this.getHeight() / 2f;

        if (colorWheelVisible && !circleInsteadArrow) {
            colorWheelWidth = w / 2f;
        } else {
            colorWheelWidth = w;
        }

        brightnessWheelWidth = colorWheelWidth;
        int margin = circleInsteadArrow ? 0 : (int) (pointerHeight);
        colorWheelRadius = Math.min(centerX, centerY) - colorWheelWidth / 2 - margin;

        if (colorWheelVisible) {
            brightnessWheelRadius = colorWheelRadius - brightnessWheelWidth;
        } else {
            brightnessWheelRadius = colorWheelRadius;
        }

        cwPaint.setStrokeWidth(colorWheelWidth);
        bwPaint.setStrokeWidth(brightnessWheelWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        _onSizeChanged();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private boolean touchOverPointer(PointF touchPoint, PointF pointerCenter,
                                     double pointerHeight) {
        return Math.sqrt(Math.pow(pointerCenter.x - touchPoint.x, 2)
                + Math.pow(pointerCenter.y - touchPoint.y, 2)) <= pointerHeight / 2;
    }

    public double pointToRadians(PointF point) {
        return Math.atan2(point.y, point.x);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        PointF touchPoint = new PointF(event.getX() - centerX, event.getY() - centerY);
        double touchAngle = pointToRadians(touchPoint);

        int action = event.getAction();

        switch (action) {

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                colorPointerMoving = false;
                brightnessWheelPointerMoving = false;

                if (powerButtonTouched) {
                    setPowerButtonOn(!isPowerButtonOn());

                    if (mOnChangeListener != null)
                        mOnChangeListener.onPowerButtonClick(this);
                }

                if (mOnChangeListener != null)
                    mOnChangeListener.onChangeFinished(this);

                powerButtonTouched = false;
                break;

            case MotionEvent.ACTION_DOWN:

                colorPointerMoving = false;
                brightnessWheelPointerMoving = false;
                powerButtonTouched = false;

                if (!sliderVisible
                        && colorWheelVisible
                        && touchOverPointer(touchPoint, colorPointerCenter, pointerHeight)) {
                    colorPointerMoving = true;
                    touchDiff = pointToRadians(colorPointerCenter) - touchAngle;
                } else if (touchOverPointer(touchPoint, brightnessPointerCenter, pointerHeight)) {
                    brightnessWheelPointerMoving = true;
                    if (sliderVisible) {
                        touchDiff = brightnessPointerCenter.y - touchPoint.y;
                    } else {
                        touchDiff = pointToRadians(brightnessPointerCenter) - touchAngle;
                    }
                } else if (powerButtonVisible
                        && powerButtonEnabled
                        && touchOverPointer(touchPoint, new PointF(0, 0),
                        powerButtonRadius * 2.2)) {
                    powerButtonTouched = true;
                }

                if (!isMoving() && !powerButtonTouched) {
                    return super.onTouchEvent(event);
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (sliderVisible) {
                    if (brightnessWheelPointerMoving) {
                        float h = sliderRect.height() - (float) pointerHeight;
                        float brightness = 100 - ((h / 2) + (float) touchDiff + touchPoint.y) * 100 / h;

                        if (brightness > 100) {
                            brightness = 100;
                        } else if (brightness < 0) {
                            brightness = 0;
                        }

                        if (selectedBrightness != brightness) {
                            setBrightnessValue(brightness);

                            if (mOnChangeListener != null)
                                mOnChangeListener.onBrightnessChanged(this, selectedBrightness);
                        }

                    }
                } else if (colorPointerMoving) {

                    colorWheelPointerAngle = touchAngle + touchDiff;

                    int newColor = calculateColor((float) colorWheelPointerAngle, Colors);

                    if (newColor != selectedColor) {

                        setBWcolor();
                        selectedColor = newColor;
                        invalidate();

                        if (mOnChangeListener != null)
                            mOnChangeListener.onColorChanged(this, selectedColor);

                    }

                } else if (brightnessWheelPointerMoving) {

                    double newAngle = touchAngle + touchDiff;
                    if (brightnessWheelPointerAngle >= m160d
                            && brightnessWheelPointerAngle <= m20d) {

                        if (Math.abs(brightnessWheelPointerAngle - newAngle) > p40d) {
                            newAngle = brightnessWheelPointerAngle;
                        }

                        if (brightnessWheelPointerAngle > newAngle) {
                            if (brightnessWheelPointerAngle >= m90d
                                    && newAngle < m90d) {
                                newAngle = m90d;
                            }
                        } else if (brightnessWheelPointerAngle < newAngle) {
                            if (brightnessWheelPointerAngle <= m90_01d
                                    && newAngle > m90_01d) {
                                newAngle = m90_01d;
                            }
                        }
                    }

                    if (brightnessWheelPointerAngle != newAngle) {
                        double d = Math.toDegrees(newAngle) + 90;

                        if (d < 0) {
                            d += 360;
                        }

                        if (d >= 359.99) {
                            d = 360;
                        }

                        setBrightnessValue((d / 360) * 100);

                        if (mOnChangeListener != null)
                            mOnChangeListener.onBrightnessChanged(this, selectedBrightness);
                    }
                }

                break;
        }

        return true;
    }

    public void setOnChangeListener(OnColorBrightnessChangeListener l) {
        mOnChangeListener = l;
    }

    public int getColor() {
        return selectedColor;
    }

    public void setColor(int color) {
        if (selectedColor != color) {
            selectedColor = color;
            colorWheelPointerAngle = colorToAngle(color);
            setBWcolor();
            invalidate();
        }
    }

    public boolean isColorWheelVisible() {
        return colorWheelVisible;
    }

    public void setColorWheelVisible(boolean visible) {
        if (visible != colorWheelVisible) {
            colorWheelVisible = visible;
            setBWcolor();
            _onSizeChanged();
            invalidate();
        }
    }

    public double getBrightnessValue() {
        return selectedBrightness;
    }

    public void setBrightnessValue(double value) {

        if (value > 100) {
            value = 100;
        } else if (value < 0) {
            value = 0;
        }

        brightnessWheelPointerAngle = brightnessToAngle(value);
        selectedBrightness = value;
        invalidate();

    }

    private double brightnessToAngle(double value) {

        double result;

        if (value < 0)
            value = 0;
        else if (value > 100)
            value = 100;


        if (value == 100) {
            result = m90_01d;
        } else {

            double a = 360 * value / 100;

            if (a > 180)
                a -= 360;

            result = Math.toRadians(a) + m90d;
        }

        return result;
    }

    public boolean isMoving() {
        return colorPointerMoving || brightnessWheelPointerMoving;
    }

    public ArrayList<Double> getColorMarkers() {
        return colorMarkers == null ? null : new ArrayList<>(colorMarkers);
    }

    public void setColorMarkers(ArrayList<Double> colorMarkers) {
        this.colorMarkers = colorMarkers == null ?
                null : new ArrayList<>(colorMarkers);

        invalidate();
    }

    public ArrayList<Double> getBrightnessMarkers() {
        return brightnessMarkers == null ? null : new ArrayList<>(brightnessMarkers);
    }

    public void setBrightnessMarkers(ArrayList<Double> brightnessMarkers) {
        this.brightnessMarkers = brightnessMarkers == null ?
                null : new ArrayList<>(brightnessMarkers);
        invalidate();
    }

    public boolean isColorfulBrightnessWheel() {
        return colorfulBrightnessWheel;
    }

    public void setColorfulBrightnessWheel(boolean colorfulBrightnessWheel) {
        this.colorfulBrightnessWheel = colorfulBrightnessWheel;
        setBWcolor();
        invalidate();
    }

    public boolean isCircleInsteadArrow() {
        return circleInsteadArrow;
    }

    public void setCircleInsteadArrow(boolean circleInsteadArrow) {
        this.circleInsteadArrow = circleInsteadArrow;
        _onSizeChanged();
        invalidate();
    }

    public boolean isSliderVisible() {
        return sliderVisible;
    }

    public void setSliderVisible(boolean sliderVisible) {
        this.sliderVisible = sliderVisible;
        _onSizeChanged();
        setBWcolor();
        invalidate();
    }

    public boolean isPowerButtonVisible() {
        return powerButtonVisible;
    }

    public void setPowerButtonVisible(boolean powerButtonVisible) {
        this.powerButtonVisible = powerButtonVisible;
        invalidate();
    }

    public boolean isPowerButtonEnabled() {
        return powerButtonEnabled;
    }

    public void setPowerButtonEnabled(boolean powerButtonEnabled) {
        this.powerButtonEnabled = powerButtonEnabled;
    }

    public boolean isPowerButtonOn() {
        return powerButtonOn;
    }

    public void setPowerButtonOn(boolean powerButtonOn) {
        this.powerButtonOn = powerButtonOn;
        invalidate();
    }

    public int getPowerButtonColorOn() {
        return powerButtonColorOn;
    }

    public void setPowerButtonColorOn(int powerButtonColorOn) {
        this.powerButtonColorOn = powerButtonColorOn;
        invalidate();
    }

    public int getPowerButtonColorOff() {
        return powerButtonColorOff;
    }

    public void setPowerButtonColorOff(int powerButtonColorOff) {
        this.powerButtonColorOff = powerButtonColorOff;
        invalidate();
    }

    public interface OnColorBrightnessChangeListener {
        void onColorChanged(SuplaColorBrightnessPicker scbPicker, int color);

        void onBrightnessChanged(SuplaColorBrightnessPicker scbPicker, double brightness);

        void onChangeFinished(SuplaColorBrightnessPicker scbPicker);

        void onPowerButtonClick(SuplaColorBrightnessPicker scbPicker);
    }

}
