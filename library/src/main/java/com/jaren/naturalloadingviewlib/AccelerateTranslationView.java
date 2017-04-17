package com.jaren.naturalloadingviewlib;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;


/**
 * Created by
 *          jaren on 2017/3/16.
 */


public class AccelerateTranslationView extends View {


    private float mDotRadius;
    private int mDotNum;
    private Paint mPaint;
    private int mDotColor;

    /**
     * 动画周期 ms
     */
    private int mPeriod;
    /**
     * 小圆点总位移
     */
    private float mDisplacement;
    /**
     * 小圆点X轴坐标集
     */
    float[] cxArr;
    /**
     * 小圆点进入边界的初速度
     */
    private float v0;
    /**
     * 加速度大小
     */
    private float a;
    /**
     * 小圆点进入边界时间间隔
     */
    private  float timeInterval  ;

    private ValueAnimator animator;
    private int mHeight;


    public AccelerateTranslationView(Context context) {
        this(context, null);
    }

    public AccelerateTranslationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AccelerateTranslationView(Context context, AttributeSet attrs,
                                     int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.AccelerateTranslationView, defStyle, 0);
        mDotColor= a.getColor(
                R.styleable.AccelerateTranslationView_dotColor, Color.BLUE);
        mDotNum = a.getInteger(
                R.styleable.AccelerateTranslationView_dotNum, 5);
        mDotRadius = a.getDimension(
                R.styleable.AccelerateTranslationView_dotRadius, TypedValue
                        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3,
                                getResources().getDisplayMetrics()));
        mPeriod = a.getInteger(
                R.styleable.AccelerateTranslationView_period, 3000);
        a.recycle();
        mPaint = new Paint();
        mPaint.setColor(mDotColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        cxArr=new float[mDotNum];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        //测量高度，宽度不做处理
        if (hMode == MeasureSpec.EXACTLY) {
            mHeight = hSize;
        } else {
            mHeight = (int) (mDotRadius * 2 + 2);
            if (hMode == MeasureSpec.AT_MOST) {
                mHeight = Math.min(mHeight, hSize);
            }
        }
        setMeasuredDimension(wSize, mHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight=h;
        mDisplacement=w;
        v0 = 2 * mDisplacement / mPeriod;
        a = 2 * v0 / mPeriod;//加速度
        timeInterval= (float) Math.sqrt(mDisplacement*0.2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (animator == null) {
            startMotion();
        }
        drawDot(canvas);

    }
    /**
     * 开启动画
     */
    private void startMotion() {
        animator = ValueAnimator.ofFloat(0f, 140f);
        animator.setDuration(mPeriod).setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());//使用线性插值器
        animator.start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                                       @Override
                                       public void onAnimationUpdate(ValueAnimator animation) {
                                           float fraction = (float) animation.getAnimatedValue();
                                           float t;
                                           float cx;//当前位移(X轴坐标)
                                           for (int i = 0; i < mDotNum; i++) {
                                               t = (fraction - i * timeInterval) * mPeriod / 100;//依次延迟不同小圆点的出发时间
                                               if (t < 0) {
                                                   cx = -1;
                                               } else if (t <= mPeriod * 0.5) {//类竖直上抛运动（恒加速度减速运动阶段）
                                                   cx = (float) (v0 * t - 0.5 * a * Math.pow(t, 2.0));
                                               } else {//类竖直上抛运动（恒加速度加速运动阶段）与上抛不同的是，这里改变了位移的方向
                                                   cx = mDisplacement - (float) (v0 * t - 0.5 * a * Math.pow(t, 2.0));
                                               }
                                               cxArr[i] = cx;
                                           }
                                           invalidate();
                                       }
                                   }

        );
    }

    /**
     * 绘制圆点
     */
    private void drawDot(Canvas canvas) {
        float cx;
        for (int i = 0; i < mDotNum; i++) {
            cx = cxArr[i];
            if (cx <= mDisplacement && cx > mDotRadius) {
                canvas.drawCircle(cx, mHeight / 2 , mDotRadius, mPaint);
            }
        }
    }

}
