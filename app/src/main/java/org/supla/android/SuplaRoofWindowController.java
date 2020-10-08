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
    private float openingPercentage;
    private Float openingPercentageWhileMoving;
    private OnOpeningPercentageChangeListener onOpeningPercentageChangeListener;

    private final float MAXIMUM_OPENING_ANGLE = 40f;
    private final float WINDOW_ROTATION_X = 210;
    private final float WINDOW_ROTATION_Y = 210;
    private final float WINDOW_HEIGHT_RATIO = 0.9f;
    private final float WINDOW_WIDTH_RATIO = 1.79f;

    private int lineColor;
    private int frameColor;
    private int glassColor;

    private void init() {
        float px = 1;

        Resources r = getResources();
        if (r!=null) {
            px = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    2,
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
                                        float framePostWidth,
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

        float[] mirrorPoints = new float[] {
                frameWidth/-2+framePostWidth, frameHeight/-2+frameBarWidth,
                frameWidth/2-framePostWidth, frameHeight/-2+frameBarWidth,
                frameWidth/2-framePostWidth, frameHeight/2-frameBarWidth,
                frameWidth/-2+framePostWidth, frameHeight/2-frameBarWidth,
                frameWidth/-2+framePostWidth, frameHeight/-2+frameBarWidth,
        };

        matrix.mapPoints(mirrorPoints);
        path.reset();

        for(int a=0;a<mirrorPoints.length-1;a+=2) {
            if (a==0) {
                path.moveTo(mirrorPoints[a], mirrorPoints[a+1]);
            } else {
                path.lineTo(mirrorPoints[a], mirrorPoints[a+1]);
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;

        float windowHeight = getWidth() * WINDOW_HEIGHT_RATIO;
        float windowWidth = windowHeight/ WINDOW_WIDTH_RATIO;
        float outerFramePostWidth = windowWidth * 0.1f;
        float outerFrameBarWidth = outerFramePostWidth * 0.8f;

        float rotationXoffset = MAXIMUM_OPENING_ANGLE
                * (openingPercentageWhileMoving == null
                ? openingPercentage : openingPercentageWhileMoving.floatValue()) / 100f;

        drawOuterFrameRemainPart(canvas,
                centerX,
                centerY,
                windowWidth, windowHeight,
                outerFramePostWidth,
                outerFramePostWidth / 2,
                outerFrameBarWidth / 2,
                openingPercentage == 0);

        drawInnerFrame(canvas,
                centerX,
                centerY,
                windowWidth-outerFramePostWidth, windowHeight-outerFrameBarWidth,
                outerFramePostWidth/2,
                outerFrameBarWidth/2,
                rotationXoffset,
                openingPercentage == 0);

        drawOuterFrameMainPart(canvas,
                centerX,
                centerY,
                windowWidth, windowHeight,
                outerFramePostWidth,
                outerFramePostWidth / 2,
                outerFrameBarWidth,
                outerFrameBarWidth / 2,
                openingPercentage == 0);

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

                if (onOpeningPercentageChangeListener != null) {
                    onOpeningPercentageChangeListener.onOpeningPercentageChanged(this,
                            openingPercentageWhileMoving);
                }

                openingPercentageWhileMoving = null;
                invalidate();
                break;
            case MotionEvent.ACTION_DOWN:
                openingPercentageWhileMoving = openingPercentage;
                result = true;
                break;
            case MotionEvent.ACTION_MOVE:

                float delta = y - lastTouchY;

                if (Math.abs(x - lastTouchX) < Math.abs(delta)) {

                    float p = Math.abs(delta) * 100 / (getWidth() * WINDOW_HEIGHT_RATIO / 2);

                    openingPercentageWhileMoving += p * (delta > 0 ? -1 : 1);

                    if (openingPercentageWhileMoving < 0)
                        openingPercentageWhileMoving = 0f;
                    else if (openingPercentageWhileMoving > 100)
                        openingPercentageWhileMoving = 100f;

                    if (onOpeningPercentageChangeListener != null) {
                        onOpeningPercentageChangeListener.onOpeningPercentageChangeing(
                                this, openingPercentageWhileMoving);
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

    public float getOpeningPercentage() {
        return openingPercentage;
    }

    public void setOpeningPercentage(float openingPercentage) {
        if (openingPercentage < 0) {
            openingPercentage = 0;
        } else if (openingPercentage > 100) {
            openingPercentage = 100;
        }

        if (this.openingPercentage != openingPercentage) {
            this.openingPercentage = openingPercentage;
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

    public OnOpeningPercentageChangeListener getOnOpeningPercentageChangeListener() {
        return onOpeningPercentageChangeListener;
    }

    public void setOnOpeningPercentageChangeListener(OnOpeningPercentageChangeListener
                                                             onOpeningPercentageChangeListener) {
        this.onOpeningPercentageChangeListener = onOpeningPercentageChangeListener;
    }

    public interface OnOpeningPercentageChangeListener {
        void onOpeningPercentageChanged(SuplaRoofWindowController controller, float openingPercentage);
        void onOpeningPercentageChangeing(SuplaRoofWindowController controller, float openingPercentage);
    }
}
