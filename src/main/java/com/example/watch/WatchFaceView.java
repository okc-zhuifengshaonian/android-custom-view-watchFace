package com.example.watch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.watch.utils.App;
import com.example.watch.utils.SizeUtils;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.TimeZone;

public class WatchFaceView extends View {

    private static final String TAG = "WatchFace";
    private int mSecondColor;
    private int mMinColor;
    private int mHourColor;
    private int mScaleColor;
    private int mBgResId;
    private boolean mIsScaleShow;
    private Paint mSecondPaint;
    private Paint mMinPaint;
    private Paint mHourPaint;
    private Paint mScalePaint;
    private Bitmap mBackgroundImage = null;
    private int mWidth;
    private int mHeight;
    private Rect mSrcRect;
    private Rect mDesRect;
    private final int mInnerCircleRadius = SizeUtils.dip2px(5);
    private Calendar mCalendar;

    public WatchFaceView(Context context) {
        this(context, null);
    }

    public WatchFaceView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WatchFaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取相关属性
        initAttrs(context, attrs);
        //拿到日历实例
        mCalendar = Calendar.getInstance();
        mCalendar.setTimeZone(TimeZone.getDefault());
        //创建画笔
        initPaints();
    }

    //初始化 Rect     Rect 是 Rectangle 的缩写，矩形的意思
    private void initRect() {
        if(mBackgroundImage == null) {
            return;
        }
        //源坑 - 从图片中截取，如果跟图片大小一样，则截取图片所有内容
        mSrcRect = new Rect();
        mSrcRect.left = 0;
        mSrcRect.top = 0;
        mSrcRect.right = mBackgroundImage.getWidth() * 3 / 4;
        mSrcRect.bottom = mBackgroundImage.getHeight() * 3 / 4;
        //目标坑 - 要填放源内容的地方
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mDesRect = new Rect();
        mDesRect.left = 0;
        mDesRect.top = 0;
        mDesRect.right = mWidth;
        mDesRect.bottom = mHeight;
    }

    //创建相关画笔
    private void initPaints() {
        //秒针
        mSecondPaint = new Paint();
        mSecondPaint.setColor(mSecondColor);
        mSecondPaint.setStyle(Paint.Style.STROKE);
        mSecondPaint.setStrokeCap(Paint.Cap.ROUND);
        mSecondPaint.setStrokeWidth(5f);
        mSecondPaint.setAntiAlias(true);
        //分针
        mMinPaint = new Paint();
        mMinPaint.setColor(mMinColor);
        mMinPaint.setStyle(Paint.Style.STROKE);
        mMinPaint.setStrokeCap(Paint.Cap.ROUND);
        mMinPaint.setStrokeWidth(5f);
        mMinPaint.setAntiAlias(true);
        //时针
        mHourPaint = new Paint();
        mHourPaint.setColor(mHourColor);
        mHourPaint.setStyle(Paint.Style.STROKE);
        mHourPaint.setStrokeWidth(5f);
        mHourPaint.setStrokeCap(Paint.Cap.ROUND);
        mHourPaint.setAntiAlias(true);
        //刻度
        mScalePaint = new Paint();
        mScalePaint.setColor(mScaleColor);
        mScalePaint.setStyle(Paint.Style.STROKE);
        mScalePaint.setStrokeCap(Paint.Cap.ROUND);
        mScalePaint.setStrokeWidth(10f);
        mScalePaint.setAntiAlias(true);
    }

    private void initAttrs(Context context, @Nullable AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WatchFace);
        mSecondColor = a.getColor(R.styleable.WatchFace_secondColor, getResources().getColor(R.color.secondDefaultColor));
        mMinColor = a.getColor(R.styleable.WatchFace_minColor, getResources().getColor(R.color.minDefaultColor));
        mHourColor = a.getColor(R.styleable.WatchFace_hourColor, getResources().getColor(R.color.hourDefaultColor));
        mScaleColor = a.getColor(R.styleable.WatchFace_scaleColor, getResources().getColor(R.color.scaleDefaultColor));
        mBgResId = a.getResourceId(R.styleable.WatchFace_watchFaceBackground, -1);
        mIsScaleShow = a.getBoolean(R.styleable.WatchFace_scaleShow, true);
        if(mBgResId != -1) {
            mBackgroundImage = BitmapFactory.decodeResource(getResources(), mBgResId);
        }
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测量自己
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //减去内边距
        int widthTargetSize = widthSize - getPaddingLeft() - getPaddingRight();
        int heightTargetSize = heightSize - getPaddingTop() - getPaddingBottom();
        //取小值
        int targetSize = widthTargetSize < heightTargetSize ? widthTargetSize : heightTargetSize;
        setMeasuredDimension(targetSize, targetSize);
        //onMeasure() 方法中在 setMeasuredDimension() 之后再调用 initRect() 方法
        initRect();
    }

    private boolean isUpdate = false;
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isUpdate = true;
        post(new Runnable() {
            @Override
            public void run() {
                if(isUpdate) {
                    invalidate();     //让当前 UI 重新绘制
                    postDelayed(this, 1000);   //刷新时间是 1 秒钟
                } else {
                    removeCallbacks(this);
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.isUpdate = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long currentMills = System.currentTimeMillis();
        mCalendar.setTimeInMillis(currentMills);
        int radius = (int)(mWidth / 2f);

        //绘制刻度
        drawScale(canvas);
        //绘制时针
        drawHourLine(canvas, radius);
        //绘制分针
        drawMinLine(canvas, radius);
        //绘制秒针
        drawSecondLine(canvas, radius);
    }

    //绘制刻度
    private void drawScale(Canvas canvas) {
        mBackgroundImage = null;
        if(mBackgroundImage != null) {
            canvas.drawBitmap(mBackgroundImage, mSrcRect, mDesRect, mScalePaint);
        } else {
            //这个 WatchFaceView 真正的半径
            int radius = (int)(mWidth / 2f);
            //外环半径
            int outerC = (int)(mWidth / 2 * 0.9f);
            //内环半径
            int innerC = (int)(mWidth / 2 * 0.8f);

            canvas.drawCircle(radius, radius , mInnerCircleRadius, mScalePaint);
            canvas.save();
            for(int i = 0; i < 12; i++ ) {
                canvas.drawLine(radius, radius - outerC, radius, radius - innerC, mScalePaint);
                //坐标系围绕(radius, radius) 这个点，每次顺时针旋转 30°
                //调用 canvas.rotate(angle) 方法时，实际上是将绘图上下文的坐标系沿着画布的原点
                //旋转了指定的角度，以后所有的绘图操作都将基于这个新的坐标系进行
                canvas.rotate(30, radius, radius);
            }
            canvas.restore();
        }
    }

    private void drawHourLine(Canvas canvas, int radius) {
        int hourValue = mCalendar.get(Calendar.HOUR);
        int minValue = mCalendar.get(Calendar.MINUTE);
        int hourRadius = (int)(radius * 0.7f);
        float hourRotate = hourValue * 30 + (minValue / 2);
        canvas.save();
        //坐标系围绕(radius, radius) 这个点，顺时针旋转 hourRotate度
        //调用 canvas.rotate(angle) 方法时，实际上是将绘图上下文的坐标系沿着画布的原点
        //旋转了指定的角度，以后所有的绘图操作都将基于这个新的坐标系进行
        canvas.rotate(hourRotate, radius, radius);
        canvas.drawLine(radius, radius - hourRadius, radius, radius - mInnerCircleRadius, mHourPaint);
        canvas.restore();
    }

    private void drawMinLine(Canvas canvas, int radius) {
        int minValue = mCalendar.get(Calendar.MINUTE);
        int miniRadius = (int)(radius * 0.6f);
        canvas.save();
        float minRotate = minValue * 6f;
        canvas.rotate(minRotate, radius, radius);
        canvas.drawLine(radius, radius - miniRadius, radius, radius - mInnerCircleRadius, mMinPaint);
        canvas.restore();
    }

    private void drawSecondLine(Canvas canvas, int radius) {
        int secondValue = mCalendar.get(Calendar.SECOND);
        int secondRadius = (int)(radius * 0.6f);
        canvas.save();
        float secondRotate = secondValue * 6f;
        canvas.rotate(secondRotate, radius, radius);
        canvas.drawLine(radius, radius - secondRadius, radius, radius - mInnerCircleRadius, mSecondPaint);
        canvas.restore();
    }
}




