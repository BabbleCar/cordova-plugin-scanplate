package com.tnc.alpr;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

class DrawView extends View {

    private Bitmap bitmap;
    private boolean isSelect = true;
    private boolean isTouch = false;
    private float circleX;
    private float circleR;
    private float circleY;
    private Paint mCerclePaint;

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        mCerclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCerclePaint.setStyle(Paint.Style.STROKE);
        mCerclePaint.setColor(Color.WHITE);
        mCerclePaint.setStrokeWidth(5);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isTouch) getCircle(canvas);
        createWindowFrame(canvas);
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        bitmap = null;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    public void isSelect(Boolean select) {
        isSelect = select;
        postInvalidate();
    }

    public void isTouch(float x, float y) {
        isTouch = true;
        circleX = x;
        circleY = y;
        circleR = 100;
        new Thread(new Runnable() {
            @Override
            public void run() {
                long starttime = System.currentTimeMillis();
                while (true) {
                    long millis = System.currentTimeMillis() - starttime;
                    if(millis >= 5) {
                        circleR = circleR - 2;
                        if (circleR <= 0) {
                            break;
                        }
                        postInvalidate();
                        starttime = System.currentTimeMillis();
                    }
                }
                isTouch = false;
            }
        }).start();
    }

    protected Canvas createWindowFrame(Canvas canvas) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas osCanvas = new Canvas(bitmap);
        RectF outerRectangle = new RectF(0, 0, getWidth(), getHeight());
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0XFF000000);
        paint.setAlpha(180);
        osCanvas.drawRect(outerRectangle, paint);


        if(isSelect) {
            RectF inRectangle = new RectF(convertDpToPixel(15), (getHeight() / 2) - convertDpToPixel(65), getWidth() - (convertDpToPixel(15)), (getHeight() / 2) + convertDpToPixel(65));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
            osCanvas.drawRoundRect(inRectangle, 10, 10, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.TRANSPARENT);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
            osCanvas.drawRoundRect(inRectangle, 10, 10, paint);
        }

        canvas.drawBitmap(bitmap, 0, 0, null);

        return canvas;
    }

    public static float convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);

        return Math.round(px);
    }

    protected Canvas getCircle(Canvas canvas) {
        canvas.drawCircle(circleX, circleY, circleR, mCerclePaint);

        return canvas;
    }

}
