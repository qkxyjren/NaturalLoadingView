package com.jaren.naturalloadingviewlib;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * 模拟竖直平面内小球以一定初速度在重力作用下绕圆环做变速圆周运动 （从最低点减速到0上升到最高点再加速到初始值回到最低点）。
 *
 * @author jaren
 *
 */
public class AccelerateCircularView extends View {
	/**
	 * 圆环的颜色
	 */
	private int mRingColor;
	/**
	 * 小球的颜色
	 */
	private int mGlobuleColor;
	/**
	 * 圆环半径、小球的旋转半径
	 */
	private float mRingRadius;
	/**
	 * 圆环宽度
	 */
	private float mRingWidth;
	/**
	 * 小球的半径
	 */
	private float mGlobuleRadius;
	protected double currentAngle = -1;
	/**
	 * 小球运动的周期
	 */
	private float mCycleTime;
	private Paint mPaint;

	public AccelerateCircularView(Context context) {
		this(context, null);
	}

	public AccelerateCircularView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AccelerateCircularView(Context context, AttributeSet attrs,
								  int defStyle) {
		super(context, attrs, defStyle);
		TypedArray attrsArray = context.getTheme().obtainStyledAttributes(
				attrs, R.styleable.AccelerateCircularView, defStyle, 0);
		mRingColor = attrsArray.getColor(
				R.styleable.AccelerateCircularView_ringColor, Color.GRAY);
		mGlobuleColor = attrsArray.getColor(
				R.styleable.AccelerateCircularView_globuleColor, Color.BLUE);
		mRingWidth = attrsArray.getDimension(
				R.styleable.AccelerateCircularView_ringWidth, TypedValue
						.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
								getResources().getDisplayMetrics()));
		mGlobuleRadius = attrsArray.getDimension(
				R.styleable.AccelerateCircularView_globuleRadius, TypedValue
						.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6,
								getResources().getDisplayMetrics()));
		mCycleTime = attrsArray.getFloat(
				R.styleable.AccelerateCircularView_cycleTime, 3000);
		attrsArray.recycle();
		mPaint = new Paint();

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int mWidth = 0, mHeight = 0;
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		if (widthMode == MeasureSpec.EXACTLY) {
			mWidth = widthSize;
		} else {
			mWidth = 169;
			if (widthMode == MeasureSpec.AT_MOST) {
				mWidth = Math.min(mWidth, widthSize);
			}

		}
		if (heightMode == MeasureSpec.EXACTLY) {
			mHeight = heightSize;
		} else {
			mHeight = 169;
			if (heightMode == MeasureSpec.AT_MOST) {
				mHeight = Math.min(mWidth, heightSize);
			}

		}

		setMeasuredDimension(mWidth, mHeight);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int central = Math.min(getWidth(), getHeight()) / 2;

		mRingRadius = central - mGlobuleRadius;

		if (mGlobuleRadius < mRingWidth / 2) {// 小球嵌在环里
			mRingRadius = central - mRingWidth / 2;
		}
		mPaint.setStrokeWidth(mRingWidth);
		mPaint.setStyle(Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setColor(mRingColor);
		canvas.drawCircle(central, central, mRingRadius, mPaint);// 绘制圆环
		mPaint.setStyle(Style.FILL);
		mPaint.setAntiAlias(true);
		mPaint.setColor(mGlobuleColor);

		if (currentAngle == -1) {
			startCirMotion();
		}
		drawGlobule(canvas, central);// 绘制小球
	}

	/**
	 * 绘制小球,起始位置为圆环最低点
	 *
	 * @param central
	 */
	private void drawGlobule(Canvas canvas, float central) {

		float cx = central + (float) (mRingRadius * Math.cos(currentAngle));
		float cy = (float) (central + mRingRadius * Math.sin(currentAngle));
		canvas.drawCircle(cx, cy, mGlobuleRadius, mPaint);

	}

	/**
	 * 旋转小球
	 */
	private void startCirMotion() {
		ValueAnimator animator = ValueAnimator.ofFloat(90f, 450f);
		animator.setDuration((long) mCycleTime).setRepeatCount(
				ValueAnimator.INFINITE);
		animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				Float angle = (Float) animation.getAnimatedValue();
				currentAngle = angle * Math.PI / 180;
				invalidate();
			}
		});
		// animator.setInterpolator(new LinearInterpolator());// 匀速旋转
		// 自定义开始减速到0后加速到初始值的Interpolator
		animator.setInterpolator(new TimeInterpolator() {

			@Override
			public float getInterpolation(float input) {
				float output;
				if (input < 0.5) {
					output = (float) Math.sin(input * Math.PI) / 2;// 先加速
				} else {
					output = 1 - (float) Math.sin(input * Math.PI) / 2;// 后减速，最高点（中间）速度为0
				}
				return output;
			}
		});
		animator.start();

	}

}
