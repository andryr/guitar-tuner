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
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by andry on 26/04/16.
 */
public class TuningView extends View {
    private int mSelectedIndex;
    private Tuning mTuning;
    private float mTuningItemWidth;
    private Paint mPaint = new Paint();
    private Rect mTempRect = new Rect();
    private int mNormalTextColor;
    private int mSelectedTextColor;
    private float mOffset = 0;
    private ValueAnimator mOffsetAnimator = null;


    public TuningView(Context context) {
        this(context, null);
    }

    public TuningView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.tuningViewStyle);
    }

    public TuningView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        final TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.TuningView, defStyleAttr,
                R.style.LightTuningView);
        mNormalTextColor = array.getColor(R.styleable.TuningView_normalTextColor, 0);
        mSelectedTextColor = array.getColor(R.styleable.TuningView_selectedTextColor, 0);
        float textSize = array.getDimension(R.styleable.TuningView_textSize, 0);
        mPaint.setTextSize(textSize);
        mTuningItemWidth = array.getDimension(R.styleable.TuningView_itemWidth, 0);
        array.recycle();
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelectedIndex(int selectedIndex, boolean animate) {
        if (selectedIndex == mSelectedIndex)
            return;

        mSelectedIndex = selectedIndex;
        float newOffset = (getWidth() - mTuningItemWidth) / 2f - mSelectedIndex * mTuningItemWidth;
        stopAnimation();
        if (animate) {
            mOffsetAnimator = ValueAnimator.ofFloat(mOffset, newOffset);
            mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mOffset = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mOffsetAnimator.start();
        } else {
            mOffset = newOffset;
        }
    }


    public void setSelectedIndex(int selectedIndex) {
        setSelectedIndex(selectedIndex, false);
    }

    private void stopAnimation() {
        if (mOffsetAnimator != null) {
            mOffsetAnimator.cancel();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        stopAnimation();
        mOffset = (w - mTuningItemWidth) / 2f - mSelectedIndex * mTuningItemWidth;
    }

    public float getTextSize() {
        return mPaint.getTextSize();
    }

    public void setTextSize(float textSize) {
        mPaint.setTextSize(textSize);
    }

    public int getNormalTextColor() {
        return mNormalTextColor;
    }

    public void setNormalTextColor(int color) {
        mNormalTextColor = color;
    }

    public int getSelectedTextColor() {
        return mSelectedTextColor;
    }

    public void setSelectedTextColor(int selectedTextColor) {
        mSelectedTextColor = selectedTextColor;
    }

    public Tuning getTuning() {
        return mTuning;
    }

    public void setTuning(Tuning tuning) {
        mTuning = tuning;
    }

    public float getTuningItemWidth() {
        return mTuningItemWidth;
    }

    public void setTuningItemWidth(float tuningItemWidth) {
        mTuningItemWidth = tuningItemWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mTuning == null)
            return;

        int height = getHeight();


        for (int i = 0; i < mTuning.pitches.length; i++) {
            if (i == mSelectedIndex) {
                mPaint.setColor(mSelectedTextColor);
            } else {
                mPaint.setColor(mNormalTextColor);
            }
            String text = mTuning.pitches[i].name;
            float textWidth = mPaint.measureText(text);
            mPaint.getTextBounds(text, 0, text.length(), mTempRect);
            canvas.drawText(text, mOffset + i * mTuningItemWidth + (mTuningItemWidth - textWidth) / 2f, (height + mTempRect.height()) / 2f, mPaint);
        }


    }
}
