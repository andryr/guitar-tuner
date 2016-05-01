/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andryr.guitartuner;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andry on 22/04/16.
 */
public class NeedleView extends View {


    private double mAngle;
    private Paint mPaint;
    private float mStrokeWidth;
    private float mTextStrokeWidth;
    private float mTickLabelTextSize;
    private Matrix mRotateMatrix;
    private float mArcOffset;
    private float mTickLength;
    private int mNeedleColor;
    private int mSmallTicksColor;
    private int mBigTicksColor;
    private int mTextColor;
    private Map<Float, String> mTickLabels = new HashMap<>();
    private float mTipPosition;
    private float mTipPos;


    public NeedleView(Context context) {
        this(context, null);
    }

    public NeedleView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.needleViewStyle);
    }


    public NeedleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mTextStrokeWidth = mPaint.getStrokeWidth();
        mStrokeWidth = getResources().getDimension(R.dimen.needle_view_stroke_width);
        mArcOffset = getResources().getDimension(R.dimen.needle_view_ticks_margin_top);
        mTickLabelTextSize = getResources().getDimension(R.dimen.needle_view_tick_label_text_size);
        mTickLength = Utils.dpToPixels(context, 5);
        setTipPos(0);

        final TypedArray array = context.obtainStyledAttributes( attrs,
                R.styleable.NeedleView, defStyleAttr,
                R.style.LightNeedleView);
        mNeedleColor = array.getColor(R.styleable.NeedleView_needleColor, 0);
        mSmallTicksColor = array.getColor(R.styleable.NeedleView_smallTicksColor, 0);
        mBigTicksColor = array.getColor(R.styleable.NeedleView_bigTicksColor, 0);
        mTextColor = array.getColor(R.styleable.NeedleView_textColor, 0);
        array.recycle();

    }

    public double getAngle() {
        return mAngle;
    }


    public void animateTip(float toPos) {

        toPos = Math.min(1.0F, Math.max(-1.0F, toPos));
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();

        double toAngle;
        if(height > width/2f)
            toAngle = 90 + toPos * (90 - Math.toDegrees(Math.acos(((width) / 2.0F - mStrokeWidth) / height)));
        else
            toAngle = 90 + toPos * 90;

        ValueAnimator animator = ValueAnimator.ofFloat((float) mAngle, (float) toAngle);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.setDuration(200);
        animator.start();
    }

    public float getTipPos() {
        return mTipPos;
    }

    public void setTipPos(float pos) {
        mTipPosition = Math.min(1.0F, Math.max(-1.0F, pos));
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        if(height > width/2f)
            mAngle = 90 + pos * (90 - Math.toDegrees(Math.acos(((width) / 2.0F - mStrokeWidth) / height)));
        else
            mAngle = 90 + pos * 90;

    }

    public void setTickLabel(float pos, String label) {
        mTickLabels.put(pos, label);
    }

    public void removeTickLabel(float pos) {
        mTickLabels.remove(pos);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.BUTT);

        drawTickLabels(canvas, width, height);
        float tickLabelHeight = mPaint.descent() - mPaint.ascent();

        // drawArc(canvas, needleLength, width, height);
        drawTicks(canvas, width, height, tickLabelHeight);
        drawNeedle(canvas, width, height, tickLabelHeight);
    }

    private void drawTickLabels(Canvas canvas, int width, int height) {
        float cx = width / 2.0F + getPaddingLeft();
        float cy = height + getPaddingTop();
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(mTickLabelTextSize);
        mPaint.setStrokeWidth(mTextStrokeWidth);
        for (Map.Entry<Float, String> entry : mTickLabels.entrySet()) {
            String text = entry.getValue();
            float textWidth = mPaint.measureText(text);
            float pos = entry.getKey();
            if (pos == 0) {
                canvas.drawText(text, (width - textWidth) / 2.0F + getPaddingLeft(), getPaddingTop() - mPaint.ascent() / 2.0F, mPaint);
            } else {
                float angle = (float) (pos * (90 - Math.toDegrees(Math.acos((width / 2.0F) / height))));
                canvas.save();
                canvas.rotate(angle, cx, cy);
                if (pos > 0) {
                    canvas.drawText(text, width / 2.0F - textWidth + getPaddingLeft(), getPaddingTop(), mPaint);
                } else {
                    canvas.drawText(text, width / 2.0F + getPaddingLeft(), getPaddingTop(), mPaint);
                }
                canvas.restore();
            }

        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void drawArc(Canvas canvas, float needleLength, int width, int height) {
        int hPadding = getPaddingLeft() + getPaddingRight();
        float arcLeft = -(needleLength - width / 2.0F);
        float arcRight = width + (needleLength - width / 2.0F);
        float arcTop = height - needleLength - getPaddingBottom() - mArcOffset;
        float arcBottom = arcTop + (needleLength * 2);
        float offsetAngle = (float) Math.toDegrees(Math.acos((width - hPadding) / 2.0F - mStrokeWidth) / needleLength);

        canvas.drawArc(arcLeft, arcTop, arcRight, arcBottom, 180 + offsetAngle, 180 - 2 * offsetAngle, false, mPaint);
    }

    private void drawNeedle(Canvas canvas, int width, int height, float tickLabelHeight) {
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        mPaint.setColor(mNeedleColor);
        double angleRad = Math.toRadians(mAngle);
        float needleLength = height - mArcOffset - tickLabelHeight;
        float cx = width / 2.0F + getPaddingLeft();
        float cy = height + getPaddingTop();
        float tipX = (float) (-needleLength * Math.cos(angleRad) + cx);
        float tipY = (float) (-needleLength * Math.sin(angleRad) + cy);

        canvas.drawLine(cx, cy, tipX, tipY, mPaint);
        mPaint.setColor(mTextColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, mStrokeWidth, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth/2f);
        canvas.drawCircle(cx, cy, mStrokeWidth*1.5f, mPaint);

    }

    private void drawTicks(Canvas canvas, int width, int height, float tickLabelHeight) {
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        float cx = width / 2.0F + getPaddingLeft();
        float cy = height + getPaddingTop();
        float startAngle;
        if(height > width /2f)
            startAngle = (float) Math.toDegrees(Math.acos((width / 2.0F - mStrokeWidth) / height));
        else
            startAngle = 0;
        float currentAngle = startAngle;
        float endAngle = 180 - startAngle;
        float midAngle = startAngle + (endAngle - startAngle) / 2.0F;
        float step = (endAngle - startAngle) / (2.0F * 10);

        drawBigTick(canvas, height, tickLabelHeight, cx, cy, currentAngle);
        currentAngle += step;

        while (currentAngle < midAngle) {
            drawSmallTick(canvas, height, tickLabelHeight, cx, cy, currentAngle);
            currentAngle += step;
        }
        currentAngle = midAngle;
        drawBigTick(canvas, height, tickLabelHeight, cx, cy, currentAngle);
        currentAngle += step;

        while (currentAngle < endAngle) {
            drawSmallTick(canvas, height, tickLabelHeight, cx, cy, currentAngle);
            currentAngle += step;
        }
        currentAngle = endAngle;
        drawBigTick(canvas, height, tickLabelHeight, cx, cy, currentAngle);


    }

    private void drawSmallTick(Canvas canvas, float height, float tickLabelHeight, float cx, float cy, float angle) {
        mPaint.setColor(mSmallTicksColor);
        mPaint.setStrokeWidth(mStrokeWidth / 2.0F);

        double angleRad = Math.toRadians(angle);
        float tipX = (float) (-(height - mArcOffset - tickLabelHeight) * Math.cos(angleRad) + cx);
        float tipY = (float) (-(height - mArcOffset - tickLabelHeight) * Math.sin(angleRad) + cy);

        float tickLength = mTickLength;


        canvas.drawLine((float) (tipX + Math.cos(angleRad) * tickLength), (float) (tipY + Math.sin(angleRad) * tickLength), tipX, tipY, mPaint);
    }

    private void drawBigTick(Canvas canvas, float height, float tickLabelHeight, float cx, float cy, float angle) {
        mPaint.setColor(mBigTicksColor);
        mPaint.setStrokeWidth(mStrokeWidth);

        double angleRad = Math.toRadians(angle);
        float tipX = (float) (-(height - mArcOffset - tickLabelHeight) * Math.cos(angleRad) + cx);
        float tipY = (float) (-(height - mArcOffset - tickLabelHeight) * Math.sin(angleRad) + cy);

        float tickLength = mTickLength * 2;


        canvas.drawLine((float) (tipX + Math.cos(angleRad) * tickLength), (float) (tipY + Math.sin(angleRad) * tickLength), tipX, tipY, mPaint);
    }

}
