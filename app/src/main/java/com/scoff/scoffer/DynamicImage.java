package com.scoff.scoffer;

import android.util.AttributeSet;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.view.View;

public class DynamicImage extends View {
    public DynamicImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DynamicImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicImage(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable bg = getBackground();
        if (bg != null) {
            int width = View.MeasureSpec.getSize(widthMeasureSpec);
            int height = width * bg.getIntrinsicHeight() / bg.getIntrinsicWidth();
            setMeasuredDimension(width,height);
        }
        else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
