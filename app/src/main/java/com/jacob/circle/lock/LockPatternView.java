package com.jacob.circle.lock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

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
    public static final int COUNT = 12;

    public static final int MIN_SIZE = 5;

    //圆形半径，默认120dp
    private int RADIUS = dpToPx(120);

    //屏幕中心点的坐标
    private int mCenterX;
    private int mCenterY;

    private boolean hasInit = false;
    private boolean isSelect, isFinish, movingNoPoint;

    //用于记录12个点的信息
    private Point[] mPointList = new Point[COUNT];

    //用于记录选择的点
    private List<Point> mSelectPoints = new ArrayList<>();

    //触摸的xy左边
    private float moveX;
    private float moveY;

    //用于绘制触摸过程中的线
    private Paint mPathPaint = new Paint();

    private static final int COLOR_NORMAL = Color.parseColor("#7fb400");
    private static final int COLOR_ERROR = Color.parseColor("#7ffb6760");

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

        mPathPaint.setAntiAlias(true);
        mPathPaint.setStrokeWidth(10);
        mPathPaint.setColor(COLOR_NORMAL);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
    }


    /**
     * 初始化所有的点
     */
    private void initPoint() {
        double angle = Math.PI * 2 / COUNT;
        for (int i = 0; i < COUNT; i++) {
            //先确定每个点的xy坐标,默认让xy的坐标在图片的中心点
            float x = (float) (mCenterX + RADIUS * Math.sin(angle * i));
            float y = (float) (mCenterY - RADIUS * Math.cos(angle * i));
            Point point = new Point(x, y);
            point.index = i;  //设置点的编号，后续作为密码使用
            point.displayMode = DisplayMode.Normal;  //设置起始的状态
            mPointList[i] = point;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int mLayoutSize = RADIUS * 2 + mBitmapNormal.getHeight() * 2;
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
        //画点
        drawPointOnCanvas(canvas);

        //画线
        if (mSelectPoints.size() > 0) {
            Point a = mSelectPoints.get(0);
            //绘制选中点之间的线
            for (int i = 0; i < mSelectPoints.size(); i++) {
                Point b = mSelectPoints.get(i);
                canvas.drawLine(a.x, a.y, b.x, b.y, mPathPaint);
                a = b;
            }
            //绘制当前触摸的位置的连线
            if (movingNoPoint) {
                canvas.drawLine(a.x, a.y, moveX, moveY, mPathPaint);
            }
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        movingNoPoint = false;
        isFinish = false;

        moveX = event.getX();
        moveY = event.getY();

        Point point = null;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                resetPoints();
                point = checkSelectPoint();
                if (point != null) {
                    isSelect = true;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (isSelect) {
                    point = checkSelectPoint();
                    if (point == null) {
                        movingNoPoint = true;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                isFinish = true;
                isSelect = false;
                break;

        }

        //选中重复检查
        if (!isFinish && isSelect && point != null) {
            //交叉点
            if (crossPointCheck(point)) {
                movingNoPoint = true;
            } else {
                //新点
                point.displayMode = DisplayMode.Select;
                mSelectPoints.add(point);
            }
        }

        //绘制结束
        if (isFinish) {
            if (mSelectPoints.size() == 1) {
                //绘制点数不够
                resetPoints();
            } else if (mSelectPoints.size() < MIN_SIZE && mSelectPoints.size() > 0) {
                //绘制错误
                errorPoint();
            }else{
                //绘制成功
            }
        }

        //update ui
        postInvalidate();
        return true;
    }

    /**
     * 交叉点的检查
     */
    public boolean crossPointCheck(Point point) {
        if (mSelectPoints.contains(point)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 绘制不成立，重置
     */
    public void resetPoints() {
        for (Point point : mPointList) {
            point.displayMode = DisplayMode.Normal;
        }
        mSelectPoints.clear();
        mPathPaint.setColor(COLOR_NORMAL);
    }


    /**
     * 绘制错误
     */
    public void errorPoint() {
        for (Point point : mSelectPoints) {
            point.displayMode = DisplayMode.Error;
        }
        mPathPaint.setColor(COLOR_ERROR);
    }

    /**
     * 判断触摸的点是否在宫格以内
     */
    private Point checkSelectPoint() {
        for (Point point : mPointList) {
            if (Point.withInPoint(point.x, point.y, mBitmapNormal.getWidth() / 2, moveX, moveY)) {
                return point;
            }
        }
        return null;
    }

    /**
     * 在画布上绘制点
     */
    private void drawPointOnCanvas(Canvas canvas) {
        int bitmapWidth = mBitmapNormal.getWidth();
        for (Point point : mPointList) {
            if (point.displayMode == DisplayMode.Normal) {
                canvas.drawBitmap(mBitmapNormal, point.x - bitmapWidth / 2, point.y - bitmapWidth / 2, null);
            } else if (point.displayMode == DisplayMode.Select) {
                canvas.drawBitmap(mBitmapSelected, point.x - bitmapWidth / 2, point.y - bitmapWidth / 2, null);
            } else {
                canvas.drawBitmap(mBitmapError, point.x - bitmapWidth / 2, point.y - bitmapWidth / 2, null);
            }
        }
    }


    /**
     * 将每个解锁的图案抽象成一个point
     */
    private static class Point {
        private Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        float x;
        float y;
        int index;

        DisplayMode displayMode;


        /**
         * 判断触摸的位置是否在固定的点以内
         */
        public static boolean withInPoint(float pointX, float pointY, float radius, float moveX, float moveY) {
            return Math.hypot((pointX - moveX), (pointY - moveY)) < radius;
        }
    }


    /**
     * 每个点的状态显示：正常，选中，错误
     */
    public enum DisplayMode {
        Normal,
        Select,
        Error
    }

    /**
     * 将dp 转换成px
     */
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
