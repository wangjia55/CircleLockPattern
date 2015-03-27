package com.jacob.circle.lock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Package : com.jacob.circle.lock
 * Author : jacob
 * Date : 15-3-27
 * Description : 圆形锁
 */
public class LockPatternView extends View {

    private Bitmap mBitmapNormal;
    private Bitmap mBitmapSelected;
    private Bitmap mBitmapError;

    //12个点组成一个圆形
    public static final int COUNT = 9;

    //圆形半径
    private int RADIUS = dpToPx(120);

    //屏幕中心点的坐标
    private int mCenterX;
    private int mCenterY;

    //View的尺寸
    private int mLayoutSize;

    private boolean hasInit = false;

    //用于记录12个点的信息
    private Point[] mPointList = new Point[COUNT];

    public LockPatternView(Context context) {
        super(context);
        initView();
    }

    public LockPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LockPatternView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mBitmapNormal = BitmapFactory.decodeResource(getResources(), R.mipmap.gesture_pattern_normal);
        mBitmapSelected = BitmapFactory.decodeResource(getResources(), R.mipmap.gesture_pattern_selected);
        mBitmapError = BitmapFactory.decodeResource(getResources(), R.mipmap.gesture_pattern_selected_wrong);
    }


    private void initPoint() {
        double angle = Math.PI * 2 / COUNT;
        for (int i = 0; i < COUNT; i++) {
            float x = (float) (mCenterX + RADIUS * Math.sin(angle * i) - mBitmapNormal.getWidth() / 2);
            float y = (float) (mCenterY - RADIUS * Math.cos(angle * i) - mBitmapNormal.getWidth() / 2);
            Point point = new Point(x, y);
            point.displayMode = DisplayMode.Normal;
            mPointList[i] = point;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mLayoutSize = RADIUS * 2 + mBitmapNormal.getHeight() * 2;
        mCenterX = mLayoutSize / 2;
        mCenterY = mLayoutSize / 2;
        setMeasuredDimension(mLayoutSize, mLayoutSize);
    }


    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);

        if (!hasInit) {
            initPoint();
            hasInit = true;
        }
        canvas.drawColor(Color.GRAY);
        drawPoint(canvas);

    }

    /**
     * 绘制点
     */
    private void drawPoint(Canvas canvas) {
        for (Point point : mPointList) {
            canvas.drawBitmap(mBitmapNormal, point.x, point.y, null);
        }
    }


    private static class Point {
        private Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        float x;
        float y;

        DisplayMode displayMode;
    }


    public enum DisplayMode {
        Normal,
        Select,
        Error;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
