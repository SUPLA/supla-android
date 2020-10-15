package org.supla.android;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
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

public class SuplaRoofWindowController extends View {

    Paint paint;
    Path path;
    Matrix matrix;
    private float lastTouchX;
    private float lastTouchY;
    private float closingPercentage;
    private Float closingPercentageWhileMoving;
    private OnClosingPercentageChangeListener onClosingPercentageChangeListener;
    private ArrayList<Float> markers = null;

    private final float MAXIMUM_OPENING_ANGLE = 40f;
    private final float WINDOW_ROTATION_X = 210;
    private final float WINDOW_ROTATION_Y = 210;
    private final float WINDOW_HEIGHT_RATIO = 1.0f;
    private final float WINDOW_WIDTH_RATIO = 0.69f;

    private int lineColor;
    private int frameColor;
    private int glassColor;
    private int markerColor;

    private void init() {
        float px = 1;

        Resources r = getResources();
        if (r!=null) {
            px = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1.5f,
                    r.getDisplayMetrics()
            );
        }

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(px);

        matrix = new Matrix();
        path = new Path();

        lineColor = Color.BLACK;
        frameColor = Color.WHITE;
        glassColor = 0xFFbed9f1;
        markerColor = 0xFFbed9f1;
    }

    public SuplaRoofWindowController(Context context) {
        super(context);
        init();
    }

    public SuplaRoofWindowController(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SuplaRoofWindowController(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SuplaRoofWindowController(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void getMatrix(float centerX, float centerY, float rotationXoffset) {
        Camera camera = new Camera();
        camera.setLocation(0,0,getWidth()*-1);
        camera.translate(centerX, centerY*-1, 0);

        camera.rotateZ(180);
        camera.rotateY(WINDOW_ROTATION_Y);
        camera.rotateX(WINDOW_ROTATION_X+rotationXoffset);
        camera.getMatrix(matrix);
    }

    private void drawOuterFrameMainPart(Canvas canvas,
                                        float centerX,
                                        float centerY,
                                        float frameWidth, float frameHeight,
                                        float framePostWidth,
                                        float bottomFramePostWidth,
                                        float frameBarWidth,
                                        float bottomFrameBarWidth,
                                        boolean excludeInnerLines) {

        float[] points = new float[] {
                frameWidth/-2, 0,
                frameWidth/-2, frameHeight/-2,
                frameWidth/2, frameHeight/-2,
                frameWidth/2, frameHeight/2,
                frameWidth/-2, frameHeight/2,
                frameWidth/-2+bottomFramePostWidth, frameHeight/2-bottomFrameBarWidth,
                frameWidth/2-bottomFramePostWidth, frameHeight/2-bottomFrameBarWidth,
                frameWidth/2-bottomFramePostWidth, 0,
                frameWidth/2-framePostWidth, 0,
                frameWidth/2-framePostWidth, frameHeight/-2+frameBarWidth,
                frameWidth/-2+framePostWidth, frameHeight/-2+frameBarWidth,
                frameWidth/-2+framePostWidth, 0,
                frameWidth/-2+bottomFramePostWidth, 0,
        };

        path.reset();
        path.setFillType(Path.FillType.EVEN_ODD);

        paint.setColor(frameColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        getMatrix(centerX, centerY, 0);
        matrix.mapPoints(points);

        for(int a=0;a<points.length-1;a+=2) {
            if (a==0) {
                path.moveTo(points[a], points[a+1]);
            } else {
                path.lineTo(points[a], points[a+1]);
            }
        }

        canvas.drawPath(path, paint);

        paint.setColor(lineColor);
        paint.setStyle(Paint.Style.STROKE);

        path.reset();

        for(int a=0;a<points.length-1;a+=2) {
            if (a==0 || a==10
                || (excludeInnerLines && ((a >= 12 && a <= 16) || a == 24))) {
                path.moveTo(points[a], points[a+1]);
            } else {
                path.lineTo(points[a], points[a+1]);
            }
        }

        canvas.drawPath(path, paint);
    }

    private void drawOuterFrameRemainPart(Canvas canvas,
                                        float centerX,
                                        float centerY,
                                        float frameWidth, float frameHeight,
                                        float bottomFramePostWidth,
                                        float bottomFrameBarWidth, boolean excludeInnerLines) {

        float[] points = new float[] {
                frameWidth/-2+bottomFramePostWidth, 0,
                frameWidth/-2+bottomFramePostWidth, frameHeight/2-bottomFrameBarWidth,
                frameWidth/-2, frameHeight/2,
                frameWidth/-2, 0
        };

        path.reset();
        path.setFillType(Path.FillType.EVEN_ODD);
        paint.setColor(frameColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        getMatrix(centerX, centerY, 0);
        matrix.mapPoints(points);

        for(int a=0;a<points.length-1;a+=2) {
            if (a==0) {
                path.moveTo(points[a], points[a+1]);
            } else {
                path.lineTo(points[a], points[a+1]);
            }
        }

        canvas.drawPath(path, paint);

        paint.setColor(lineColor);
        paint.setStyle(Paint.Style.STROKE);

        path.reset();

        // TODO: All loops of this type should be transferred to a method
        //  with a parameter as an arrow function (after switching to Java 8)
        for(int a=0;a<points.length-1;a+=2) {
            if (a==0 || a==4
                || (excludeInnerLines && a == 2)) {
                path.moveTo(points[a], points[a+1]);
            } else {
                path.lineTo(points[a], points[a+1]);
            }
        }

        canvas.drawPath(path, paint);
    }

    private void drawInnerFrame(Canvas canvas,
                                        float centerX,
                                        float centerY,
                                        float frameWidth, float frameHeight,
                                        float framePostWidth,
                                        float frameBarWidth, float rotationXoffset,
                                boolean excludeOuterLines) {

        float[] framePoints = new float[] {
                frameWidth/-2, frameHeight/-2,
                frameWidth/2, frameHeight/-2,
                frameWidth/2, 0,
                frameWidth/2, frameHeight/2,
                frameWidth/-2, frameHeight/2,
                frameWidth/-2, 0,
                frameWidth/-2, frameHeight/-2,

                frameWidth/-2+framePostWidth, frameHeight/-2+frameBarWidth,
                frameWidth/2-framePostWidth, frameHeight/-2+frameBarWidth,
                frameWidth/2-framePostWidth, frameHeight/2-frameBarWidth,
                frameWidth/-2+framePostWidth, frameHeight/2-frameBarWidth,
                frameWidth/-2+framePostWidth, frameHeight/-2+frameBarWidth,
        };

        path.reset();
        path.setFillType(Path.FillType.EVEN_ODD);

        getMatrix(centerX, centerY, rotationXoffset);
        matrix.mapPoints(framePoints);

        paint.setColor(frameColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        for(int a=0;a<framePoints.length-1;a+=2) {
            if (a==0 || a == 14) {
                path.moveTo(framePoints[a], framePoints[a+1]);
            } else {
                path.lineTo(framePoints[a], framePoints[a+1]);
            }
        }

        canvas.drawPath(path, paint);

        float[] glassPoints = new float[] {
                frameWidth/-2+framePostWidth, frameHeight/-2+frameBarWidth,
                frameWidth/2-framePostWidth, frameHeight/-2+frameBarWidth,
                frameWidth/2-framePostWidth, frameHeight/2-frameBarWidth,
                frameWidth/-2+framePostWidth, frameHeight/2-frameBarWidth,
                frameWidth/-2+framePostWidth, frameHeight/-2+frameBarWidth,
        };

        matrix.mapPoints(glassPoints);
        path.reset();

        for(int a=0;a<glassPoints.length-1;a+=2) {
            if (a==0) {
                path.moveTo(glassPoints[a], glassPoints[a+1]);
            } else {
                path.lineTo(glassPoints[a], glassPoints[a+1]);
            }
        }

        paint.setColor(glassColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);

        paint.setColor(lineColor);
        paint.setStyle(Paint.Style.STROKE);

        path.reset();
        for(int a=0;a<framePoints.length-1;a+=2) {
            if (a==0 || a == 14 || (excludeOuterLines && a>=6 && a<=10)) {
                path.moveTo(framePoints[a], framePoints[a+1]);
            } else {
                path.lineTo(framePoints[a], framePoints[a+1]);
            }
        }

        canvas.drawPath(path, paint);
    }

    private void drawMarkers(Canvas canvas,
                                float centerX,
                                float centerY,
                                float frameWidth, float frameHeight,
                                float framePostWidth,
                                float frameBarWidth) {


        if (markers == null || markers.size() == 0) {
            return;
        }

        for(int m=0;m<markers.size();m++) {
            path.reset();
            getMatrix(centerX, centerY, closingPercentageToXoffset(markers.get(m)));

            float[] glassPoints = new float[] {
                    frameWidth/-2+framePostWidth, frameHeight/-2+frameBarWidth,
                    frameWidth/2-framePostWidth, frameHeight/-2+frameBarWidth,
                    frameWidth/2-framePostWidth, frameHeight/2-frameBarWidth,
                    frameWidth/-2+framePostWidth, frameHeight/2-frameBarWidth,
                    frameWidth/-2+framePostWidth, frameHeight/-2+frameBarWidth,
            };

            matrix.mapPoints(glassPoints);

            for(int a=0;a<glassPoints.length-1;a+=2) {
                if (a==0) {
                    path.moveTo(glassPoints[a], glassPoints[a+1]);
                } else {
                    path.lineTo(glassPoints[a], glassPoints[a+1]);
                }
            }

            paint.setColor(markerColor);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(path, paint);
        }
    }

    private float closingPercentageToXoffset(float closingPercentage) {
        return MAXIMUM_OPENING_ANGLE * (100f - closingPercentage / 100f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;

        float windowHeight = getHeight() * WINDOW_HEIGHT_RATIO;
        float windowWidth = windowHeight * WINDOW_WIDTH_RATIO;

        if (windowWidth > getWidth()) {
            windowWidth = getWidth();
        }

        float outerFramePostWidth = windowWidth * 0.1f;
        float outerFrameBarWidth = outerFramePostWidth * 0.8f;

        float closingPercentage = closingPercentageWhileMoving == null
                ? this.closingPercentage : closingPercentageWhileMoving.floatValue();

        float rotationXoffset = closingPercentageToXoffset(closingPercentage);

        drawOuterFrameRemainPart(canvas,
                centerX,
                centerY,
                windowWidth, windowHeight,
                outerFramePostWidth / 2,
                outerFrameBarWidth / 2,
                closingPercentage >= 100);

        if (markers != null && !markers.isEmpty()
                && closingPercentageWhileMoving == null && closingPercentage == 0) {
            drawMarkers(canvas,
                    centerX,
                    centerY,
                    windowWidth-outerFramePostWidth,
                    windowHeight-outerFrameBarWidth,
                    outerFramePostWidth/2,
                    outerFrameBarWidth/2);
        } else {
            drawInnerFrame(canvas,
                    centerX,
                    centerY,
                    windowWidth-outerFramePostWidth,
                    windowHeight-outerFrameBarWidth,
                    outerFramePostWidth/2,
                    outerFrameBarWidth/2,
                    rotationXoffset,
                    closingPercentage >= 100);
        }

        drawOuterFrameMainPart(canvas,
                centerX,
                centerY,
                windowWidth, windowHeight,
                outerFramePostWidth,
                outerFramePostWidth / 2,
                outerFrameBarWidth,
                outerFrameBarWidth / 2,
                closingPercentage >= 100);

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

                if (onClosingPercentageChangeListener != null) {
                    onClosingPercentageChangeListener.onClosingPercentageChanged(this,
                            closingPercentageWhileMoving);
                }

                closingPercentageWhileMoving = null;
                invalidate();
                break;
            case MotionEvent.ACTION_DOWN:
                closingPercentageWhileMoving = closingPercentage;
                result = true;
                break;
            case MotionEvent.ACTION_MOVE:

                float delta = y - lastTouchY;

                if (Math.abs(x - lastTouchX) < Math.abs(delta)) {

                    float p = Math.abs(delta) * 100 / (getHeight() * WINDOW_HEIGHT_RATIO / 2);

                    closingPercentageWhileMoving += p * (delta > 0 ? 1 : -1);

                    if (closingPercentageWhileMoving < 0)
                        closingPercentageWhileMoving = 0f;
                    else if (closingPercentageWhileMoving > 100)
                        closingPercentageWhileMoving = 100f;

                    if (onClosingPercentageChangeListener != null) {
                        onClosingPercentageChangeListener.onClosingPercentageChangeing(
                                this, closingPercentageWhileMoving);
                    }

                    result = true;
                }

                invalidate();
                break;
        }

        lastTouchX = x;
        lastTouchY = y;

        return result;
    }

    public float getClosingPercentage() {
        return closingPercentage;
    }

    public void setClosingPercentage(float closingPercentage) {
        if (closingPercentage < 0) {
            closingPercentage = 0;
        } else if (closingPercentage > 100) {
            closingPercentage = 100;
        }

        if (this.closingPercentage != closingPercentage) {
            this.closingPercentage = closingPercentage;
            invalidate();
        }
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        if (this.lineColor != lineColor) {
            this.lineColor = lineColor;
            invalidate();
        }
    }

    public int getFrameColor() {
        return frameColor;
    }

    public void setFrameColor(int frameColor) {
        if (this.frameColor != frameColor) {
            this.frameColor = frameColor;
            invalidate();
        }
    }

    public int getGlassColor() {
        return glassColor;
    }

    public void setGlassColor(int glassColor) {
        if (this.glassColor != glassColor) {
            this.glassColor = glassColor;
            invalidate();
        }
    }

    public int getMarkerColor() {
        return markerColor;
    }

    public void setMarkerColor(int markerColor) {
        if (this.markerColor != markerColor) {
            this.markerColor = markerColor;
            invalidate();
        }
    }

    public void setMarkers(ArrayList<Float> markers) {
        if (markers == null) {
            this.markers = null;
        } else {
            this.markers = new ArrayList<>();
            for(int a=0;a<markers.size();a++) {
                Float value = markers.get(a);
                if (value < 0) {
                    value = 0f;
                } else if (value > 100) {
                    value = 100f;
                }
                this.markers.add(value);
            }
        }
        invalidate();
    }

    public ArrayList<Float>getMarkers() {
        return markers;
    }

    public OnClosingPercentageChangeListener getOnClosingPercentageChangeListener() {
        return onClosingPercentageChangeListener;
    }

    public void setOnClosingPercentageChangeListener(OnClosingPercentageChangeListener
                                                             onClosingPercentageChangeListener) {
        this.onClosingPercentageChangeListener = onClosingPercentageChangeListener;
    }

    public interface OnClosingPercentageChangeListener {
        void onClosingPercentageChanged(SuplaRoofWindowController controller, float closingPercentage);
        void onClosingPercentageChangeing(SuplaRoofWindowController controller, float closingPercentage);
    }
}
