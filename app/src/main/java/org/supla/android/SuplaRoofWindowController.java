package org.supla.android;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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
 */

public class SuplaRoofWindowController extends View {

    Paint paint;
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

    private int frontColor;
    private int frameColor;

    private void init() {
        paint = new Paint();
        matrix = new Matrix();

        frontColor = 0xFF61645c;
        frameColor = 0xffb49a63;
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

    private float[] getBasicFramePoints(float frameWidth, float frameHeight,
                                        float framePostWidth, float frameBarWidth,
                                        boolean bottom) {
        float[] result = new float[] {
                frameWidth/-2, frameHeight/-2,
                frameWidth/2, frameHeight/-2,
                frameWidth/2, 0,
                frameWidth/2-framePostWidth, 0,
                frameWidth/2-framePostWidth, frameHeight/-2+frameBarWidth,
                frameWidth/-2+framePostWidth, frameHeight/-2+frameBarWidth,
                frameWidth/-2+framePostWidth, 0,
                frameWidth/-2, 0
        };

        if (bottom) {
            for(int a=0;a<result.length;a++) {
                result[a] *= -1;
            }
        }

        return result;
    }

    private void addFramePointsToPath(float points[], Path path) {
        for(int a=0;a<points.length-1;a+=2) {
            if (a == 0) {
                path.moveTo(points[a], points[a+1]);
            } else {
                path.lineTo(points[a], points[a+1]);
            }
        }
    }

    private void addLateralPlanePointsToPath(float backPoints[], float frontPoints[], int offset, Path path) {
        path.moveTo(backPoints[offset], backPoints[offset+1]);
        path.lineTo(backPoints[(offset+2)%8], backPoints[(offset+3)%8]);
        path.lineTo(frontPoints[(offset+2)%8], frontPoints[(offset+3)%8]);
        path.lineTo(frontPoints[offset], frontPoints[offset+1]);
        path.lineTo(backPoints[offset], backPoints[offset+1]);
    }

    private void drawHalfFrame(Canvas canvas,
                               float centerX,
                               float centerY,
                               float windowWidth, float windowHeight,
                               float outerFramePostWidth, float outerFrameBarWidth,
                               float frameThickness,
                               float rotationXoffset,
                               boolean bottom) {

        Camera camera = new Camera();
        camera.setLocation(0,0,getWidth()*-1);
        camera.translate(centerX, centerY*-1, 0);

        camera.save();
        camera.rotateY(WINDOW_ROTATION_Y);
        camera.rotateX(WINDOW_ROTATION_X +rotationXoffset);
        camera.translate(0, 0, frameThickness);
        camera.getMatrix(matrix);
        camera.restore();

        float frontPoints[] = getBasicFramePoints(windowWidth, windowHeight,
                outerFramePostWidth, outerFrameBarWidth, bottom);
        float backPoints[] = frontPoints.clone();

        matrix.mapPoints(frontPoints);

        camera.save();
        camera.rotateY(WINDOW_ROTATION_Y);
        camera.rotateX(WINDOW_ROTATION_X +rotationXoffset);
        camera.getMatrix(matrix);
        camera.restore();

        matrix.mapPoints(backPoints);

        paint.setColor(frameColor);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(2);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        addFramePointsToPath(backPoints, path);
        canvas.drawPath(path, paint);

        path.reset();
        addLateralPlanePointsToPath(backPoints, frontPoints, 0, path);
        canvas.drawPath(path, paint);

        path.reset();
        addLateralPlanePointsToPath(backPoints, frontPoints, 2, path);
        canvas.drawPath(path, paint);

        path.reset();
        addLateralPlanePointsToPath(backPoints, frontPoints, 14, path);
        canvas.drawPath(path, paint);

        path.reset();
        addLateralPlanePointsToPath(backPoints, frontPoints, 4, path);
        canvas.drawPath(path, paint);

        path.reset();
        paint.setColor(frontColor);
        addFramePointsToPath(frontPoints, path);
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

        drawHalfFrame(canvas,
                centerX,
                centerY,
                windowWidth, windowHeight,
                outerFramePostWidth, outerFrameBarWidth, outerFramePostWidth,
                rotationXoffset, true);

        drawHalfFrame(canvas,
                centerX,
                centerY,
                windowWidth, windowHeight,
                outerFramePostWidth, outerFrameBarWidth, outerFramePostWidth,
                0, true);

        drawHalfFrame(canvas,
                centerX,
                centerY,
                windowWidth, windowHeight,
                outerFramePostWidth, outerFrameBarWidth, outerFramePostWidth/5,
                0, false);

        drawHalfFrame(canvas,
                centerX,
                centerY,
                windowWidth, windowHeight,
                outerFramePostWidth, outerFrameBarWidth, outerFramePostWidth,
                rotationXoffset, false);


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

    public int getFrontColor() {
        return frontColor;
    }

    public void setFrontColor(int frontColor) {
        if (this.frontColor != frontColor) {
            this.frontColor = frontColor;
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
