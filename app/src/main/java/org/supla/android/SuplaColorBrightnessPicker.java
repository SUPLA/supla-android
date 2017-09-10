package org.supla.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.util.AttributeSet;
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

 Fragments of code based on:
    https://github.com/chiralcode/Android-Color-Picker/blob/master/src/com/chiralcode/colorpicker/ColorPicker.java
    https://github.com/LarsWerkman/HoloColorPicker/blob/master/libary/src/main/java/com/larswerkman/holocolorpicker/ColorPicker.java
 */

public class SuplaColorBrightnessPicker extends View {

    static private final int[] Colors = new int[] {
            0xFFFF0000, 0xFFFF00FF,
            0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000
    };

    private int[] BW = new int[] {
            Color.BLACK,
            Color.WHITE,
            Color.WHITE

    };

    private class PointerTop {
        double X;
        double Y;
        double Height;
    }

    private PointerTop outerTop;
    private PointerTop innerTop;


    final static private double m160d = Math.toRadians(-160);
    final static private double m90d = Math.toRadians(-90);
    final static private double m90_01d = Math.toRadians(-90.01);
    final static private double m20d = Math.toRadians(-20);

    private float centerX;
    private float centerY;
    private float wheelWidth;
    private float arrowHeight;
    private float outerWheelWidth;
    private double outerArrowHeight_a;
    private double outerArrowHeight_b;
    private float outerWheelRadius;
    private double outerWheelPointerAngle;
    private double innerArrowHeight_a;
    private double innerArrowHeight_b;
    private float innerWheelWidth;
    private float innerWheelRadius;
    private double innerWheelPointerAngle;
    private int selectedColor;
    private double selectedBrightness;
    private int selectedBrightnessColor;
    private Path outerArrowPath;
    private Paint outerArrowPaint;
    private Path innerArrowPath;
    private Paint innerArrowPaint;

    private Paint cwPaint;   // color wheel paint
    private Shader cwShader; // color wheel shader

    private Paint bwPaint;   // brightness wheel paint
    private Shader bwShader; // brightness wheel shader

    private Matrix gradientRotationMatrix;

    private boolean colorWheelVisible;
    private boolean bwBrightnessWheelVisible;
    private boolean colorBrightnessWheelVisible;
    private boolean percentVisible;

    private boolean colorWheelMove;
    private boolean brightnessWheelMove;

    private double lastTouchedAngle;
    private OnColorBrightnessChangeListener mOnChangeListener;

    private Rect bounds;
    private Paint textPaint;

    public interface OnColorBrightnessChangeListener {
        void onColorChanged(SuplaColorBrightnessPicker scbPicker, int color);
        void onBrightnessChanged(SuplaColorBrightnessPicker scbPicker, double brightness);
        void onChangeFinished();
    }

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
        bwBrightnessWheelVisible = false;
        colorBrightnessWheelVisible = false;

        cwShader = new SweepGradient(0, 0, Colors, null);
        cwPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cwPaint.setStyle(Paint.Style.STROKE);

        bwShader = new SweepGradient(0, 0, BW, null);
        bwPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bwPaint.setStyle(Paint.Style.STROKE);

        outerArrowPath = new Path();
        outerArrowPaint = new Paint();

        outerTop = new PointerTop();
        innerTop = new PointerTop();

        outerWheelPointerAngle = Math.toRadians(-90);
        selectedColor = calculateColor((float)outerWheelPointerAngle, Colors);

        innerArrowPath = new Path();
        innerArrowPaint = new Paint();

        innerWheelPointerAngle = Math.toRadians(-90);
        selectedBrightnessColor = calculateColor((float)(innerWheelPointerAngle-m90d), BW);
        selectedBrightness = 0;

        gradientRotationMatrix = new Matrix();
        gradientRotationMatrix.preRotate(-90);
        bwShader.setLocalMatrix(gradientRotationMatrix);

        colorWheelMove = false;
        brightnessWheelMove = false;

        percentVisible = true;

        bounds = new Rect();
        textPaint = new Paint();

        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);

        setWheelWidth(100);
        setArrowHeight(100);

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

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.translate(centerX, centerY);


        if ( colorWheelVisible ) {

            cwPaint.setShader(cwShader);
            canvas.drawOval(new RectF(-outerWheelRadius, -outerWheelRadius, outerWheelRadius, outerWheelRadius), cwPaint);
            drawOuterPointerArrow(canvas, outerTop,
                    outerWheelPointerAngle,
                    outerWheelRadius,
                    outerWheelWidth,
                    -(outerArrowHeight_a/4),
                    outerArrowHeight_a,
                    outerArrowHeight_b,
                    selectedColor,
                    outerArrowPath,
                    outerArrowPaint);

        }

        if ( bwBrightnessWheelVisible
                || colorBrightnessWheelVisible ) {

            bwPaint.setShader(bwShader);
            canvas.drawOval(new RectF(-innerWheelRadius, -innerWheelRadius, innerWheelRadius, innerWheelRadius), bwPaint);

            int negative;
            double arrowOffset;

            if ( colorWheelVisible ) {
                negative = -1;
                arrowOffset = innerArrowHeight_a / 4 - innerWheelWidth;
            } else {
                negative = 1;
                arrowOffset = -(innerArrowHeight_a / 4);
            }

            drawOuterPointerArrow(canvas, innerTop,
                    innerWheelPointerAngle,
                    innerWheelRadius,
                    innerWheelWidth,
                    arrowOffset,
                    negative*innerArrowHeight_a,
                    negative*innerArrowHeight_b,
                    selectedBrightnessColor,
                    innerArrowPath,
                    innerArrowPaint);

            if ( percentVisible ) {

                String text = Integer.toString((int)selectedBrightness) + "%";

                textPaint.getTextBounds(text, 0, text.length(), bounds);
                canvas.drawText(text, -(bounds.width()/2), bounds.height()/2, textPaint);
            }


        }


    }


    private void drawOuterPointerArrow(Canvas canvas, PointerTop top, double topAngle, float wheelRadius, float wheelWidth, double arrowOffset, double arrowHeight_a, double arrowHeight_b, int color, Path arrowPath, Paint arrowPaint) {

        top.X = Math.cos(topAngle) * (wheelRadius+wheelWidth/2+arrowOffset);
        top.Y = Math.sin(topAngle) * (wheelRadius+wheelWidth/2+arrowOffset);

        top.Height =  Math.abs(arrowHeight_a+ arrowHeight_b);

        double arrowRad = Math.toRadians(40);

        double leftX = top.X + Math.cos(topAngle+arrowRad)*arrowHeight_a;
        double leftY = top.Y + Math.sin(topAngle+arrowRad)*arrowHeight_a;

        double rightX = top.X + Math.cos(topAngle-arrowRad)*arrowHeight_a;
        double rightY = top.Y + Math.sin(topAngle-arrowRad)*arrowHeight_a;

        double backRad = topAngle;

        double backLeftX = leftX + Math.cos(backRad)*arrowHeight_b;
        double backLeftY = leftY + Math.sin(backRad)*arrowHeight_b;

        double backRightX = rightX + Math.cos(backRad)*arrowHeight_b;
        double backRightY = rightY + Math.sin(backRad)*arrowHeight_b;

        arrowPath.reset();
        arrowPath.moveTo((float) top.X, (float) top.Y);
        arrowPath.lineTo((float) leftX, (float) leftY);
        arrowPath.lineTo((float) backLeftX, (float) backLeftY);

        arrowPath.moveTo((float) top.X, (float) top.Y);
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
        int color = colorBrightnessWheelVisible ? selectedColor : Color.WHITE;

        if ( BW[1] != color ) {
            BW[1] = color;
            BW[2] = color;

            bwShader = new SweepGradient(0, 0, BW, null);
            bwShader.setLocalMatrix(gradientRotationMatrix);
        }
    }

    private void _onSizeChanged() {

        outerWheelWidth = wheelWidth / 2;
        outerArrowHeight_a = arrowHeight;
        outerArrowHeight_b = outerArrowHeight_a * 0.6;
        outerArrowHeight_a -= outerArrowHeight_b;

        innerWheelWidth = outerWheelWidth;
        innerArrowHeight_a = arrowHeight;
        innerArrowHeight_b = innerArrowHeight_a * 0.6;
        innerArrowHeight_a -= innerArrowHeight_b;

        centerX = this.getWidth() / 2;
        centerY = this.getHeight() / 2;

        if ( colorWheelVisible
                && ( bwBrightnessWheelVisible || colorBrightnessWheelVisible ) ) {
            outerWheelWidth = wheelWidth / 2;
        } else {
            outerWheelWidth = wheelWidth;
        }

        innerWheelWidth = outerWheelWidth;
        outerWheelRadius = Math.min(centerX, centerY) - outerWheelWidth / 2 - (int)(arrowHeight);

        if ( colorWheelVisible ) {
            innerWheelRadius = outerWheelRadius - innerWheelWidth;
        } else {
            innerWheelRadius = outerWheelRadius;
        }

        textPaint.setTextSize((int)(innerWheelRadius*0.4));
        cwPaint.setStrokeWidth(outerWheelWidth);
        bwPaint.setStrokeWidth(innerWheelWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        _onSizeChanged();
        super.onSizeChanged(w,h,oldw,oldh);
    }

    private double calculateAngle(double pointerAngle, double inRads) {

        double delta = 0;

        if ( Math.abs(lastTouchedAngle - inRads) > Math.PI ) {

            delta = 2*Math.PI - Math.abs(lastTouchedAngle) - Math.abs(inRads);

            if ( lastTouchedAngle > 0 && inRads < 0 ) {
                delta*=-1;
            }

        } else {
            delta = lastTouchedAngle - inRads;
        }

        double result = (pointerAngle - delta);

        if ( Math.abs(result) > Math.PI ) {

            result = Math.PI - (Math.abs(result) % Math.PI);

            if ( lastTouchedAngle < inRads ) {
                result *= -1;
            }
        }


        return result;
    }

    private void calculateBrightness() {

        double d = Math.toDegrees(innerWheelPointerAngle) + 90;

        if ( d < 0 )
            d = d + 360;

        if ( d >= 359.99 )
            d = 360;

        selectedBrightness = ( d / 360 ) * 100;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX() - centerX;
        float y = event.getY() - centerY;

        double inRads = (float) Math.atan2(y, x);

        int action = event.getAction();

        switch (action) {

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                colorWheelMove = false;
                brightnessWheelMove = false;

                if ( mOnChangeListener != null )
                    mOnChangeListener.onChangeFinished();

                break;

            case MotionEvent.ACTION_DOWN:

                double sqrt = Math.sqrt(x*x + y*y);


                if ( colorWheelVisible
                        && Math.abs(outerTop.X - x) <= outerTop.Height
                        && Math.abs(outerTop.Y - y) <= outerTop.Height
                        && sqrt >= outerWheelRadius-outerWheelWidth/2
                        && sqrt <= outerWheelRadius+(outerArrowHeight_a+outerArrowHeight_b)*2  ) {

                    colorWheelMove = true;
                    brightnessWheelMove = false;

                } else if ( ( bwBrightnessWheelVisible || colorBrightnessWheelVisible )
                            && Math.abs(innerTop.X - x) <= innerTop.Height
                            && Math.abs(innerTop.Y - y) <= innerTop.Height
                            && (( colorWheelVisible
                                && sqrt <= innerWheelRadius-innerWheelWidth/2
                                && sqrt >= innerWheelRadius-(innerArrowHeight_a+innerArrowHeight_b)*2 )
                                || (!colorWheelVisible
                                && sqrt <= innerWheelRadius+(innerArrowHeight_a+innerArrowHeight_b)*2
                                && sqrt >= innerWheelRadius-innerWheelWidth/2 ))  ) {

                    colorWheelMove = false;
                    brightnessWheelMove = true;
                }


                lastTouchedAngle = inRads;

                if ( !getMoving() )
                    return super.onTouchEvent(event);

                break;

            case MotionEvent.ACTION_MOVE:

                if ( colorWheelMove ) {

                    outerWheelPointerAngle = calculateAngle(outerWheelPointerAngle, inRads);

                    int newColor = calculateColor((float)outerWheelPointerAngle, Colors);

                    if ( newColor != selectedColor ) {

                        selectedColor = newColor;
                        setBWcolor();
                        invalidate();

                        if ( mOnChangeListener != null )
                            mOnChangeListener.onColorChanged(this, selectedColor);

                    }

                } else if ( brightnessWheelMove ) {

                    double newAngle = calculateAngle(innerWheelPointerAngle, inRads);

                    if ( newAngle >= m160d
                            && newAngle <= m20d ) {

                        if ( innerWheelPointerAngle > newAngle ) {

                            if ( innerWheelPointerAngle >= m90d
                                    && newAngle < m90d ) {
                                newAngle = m90d;
                            }

                        } else if ( innerWheelPointerAngle < newAngle ) {

                            if ( innerWheelPointerAngle <= m90_01d
                                    && newAngle > m90_01d ) {
                                newAngle = m90_01d;
                            }

                        }

                    }

                    if ( innerWheelPointerAngle != newAngle ) {

                        innerWheelPointerAngle = newAngle;

                        calculateBrightness();
                        invalidate();

                        if ( mOnChangeListener != null )
                            mOnChangeListener.onBrightnessChanged(this, selectedBrightness);

                    }

                }

                if ( (colorWheelMove || brightnessWheelMove)
                        && ( bwBrightnessWheelVisible || colorBrightnessWheelVisible )  ) {
                    selectedBrightnessColor = calculateColor((float)(innerWheelPointerAngle-m90d), BW);
                }

                lastTouchedAngle = inRads;

                break;
        }

        return true;
    }

    public void setOnChangeListener(OnColorBrightnessChangeListener l) {
        mOnChangeListener = l;
    }

    public void setColor(int color) {

        if ( (color & 0xFFFFFF) == 0xFFFFFF )
            color = 0xFFFFFFFF;

        if ( selectedColor != color ) {

            selectedColor = color;
            setBWcolor();
            outerWheelPointerAngle = colorToAngle(color);

            if ( color == Color.WHITE )
                selectedColor = color;
            else
                selectedColor = calculateColor((float)outerWheelPointerAngle, Colors);

            setBWcolor();
            setBrightnessValue(selectedBrightness);
        }

    }

    public int getColor() {
        return selectedColor;
    }

    public void setColorWheelVisible(boolean visible) {

        if ( visible != colorWheelVisible ) {

            if ( visible ) {
                bwBrightnessWheelVisible = false;
                colorBrightnessWheelVisible = false;
            } else {
                bwBrightnessWheelVisible = true;
                colorBrightnessWheelVisible = false;
            }

            colorWheelVisible = visible;
            _onSizeChanged();
            invalidate();
        }

    }

    public boolean getColorWheelVisible() {
        return colorWheelVisible;
    }

    public void setBWBrightnessWheelVisible(boolean visible) {

        if ( visible != bwBrightnessWheelVisible ) {

            if ( visible ) {
                colorBrightnessWheelVisible = false;
                colorWheelVisible = false;
            } else {
                colorBrightnessWheelVisible = false;
                colorWheelVisible = true;
            }

            bwBrightnessWheelVisible = visible;

            setBWcolor();
            selectedBrightnessColor = calculateColor((float)(innerWheelPointerAngle-m90d), BW);

            _onSizeChanged();
            invalidate();
        }

    }

    public void setColorBrightnessWheelVisible(boolean visible) {

        if ( visible != colorBrightnessWheelVisible ) {

            if ( visible ) {
                colorWheelVisible = true;
                bwBrightnessWheelVisible = false;
            }

            colorBrightnessWheelVisible = visible;

            setBWcolor();
            selectedBrightnessColor = calculateColor((float)(innerWheelPointerAngle-m90d), BW);

            _onSizeChanged();
            invalidate();
        }

    }

    public void setPercentVisible(boolean visible) {
        if ( percentVisible != visible ) {
            percentVisible = visible;
            invalidate();
        }
    }

    public double getBrightnessValue() {
        return selectedBrightness;
    }

    public void setBrightnessValue(double value) {

        if ( value < 0 )
            value = 0;
        else if ( value > 100 )
            value = 100;


        if ( value == 100 ) {
            innerWheelPointerAngle = m90_01d;
        } else {

            double a = 360*value/100;

            if ( a > 180 )
                a-=360;

            innerWheelPointerAngle = Math.toRadians(a)+m90d;
        }


        selectedBrightnessColor = calculateColor((float)(innerWheelPointerAngle-m90d), BW);
        selectedBrightness = value;
        invalidate();


    }

    public float getWheelWidth() {
        return wheelWidth;
    }

    public void setWheelWidth(float wheelWidth) {
        this.wheelWidth = wheelWidth;
        _onSizeChanged();
        invalidate();
    }

    public float getArrowHeight() {
        return arrowHeight;
    }

    public void setArrowHeight(float arrowHeight) {
        this.arrowHeight = arrowHeight;
    }

    public void setTextTypeface(Typeface typeface) {

        textPaint.setTypeface(typeface);
        invalidate();
    }

    public boolean getColorBrightnessWheelVisible() {
        return colorBrightnessWheelVisible;
    }

    public boolean getBWBrightnessWheelVisible() {
        return bwBrightnessWheelVisible;
    }

    public boolean getMoving() {
        return colorWheelMove || brightnessWheelMove;
    }

}
