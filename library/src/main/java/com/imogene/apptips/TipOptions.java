package com.imogene.apptips;

import android.content.Context;

/**
 * Created by Admin on 25.04.2016.
 */
public final class TipOptions {

    public static final int ALIGN_LEFT_ABOVE = 1;
    public static final int ALIGN_LEFT_BELOW = 2;
    public static final int ALIGN_RIGHT_ABOVE = 3;
    public static final int ALIGN_RIGHT_BELOW = 4;
    public static final int ALIGN_CENTER_ABOVE = 5;
    public static final int ALIGN_CENTER_BELOW = 6;
    public static final int ALIGN_LEFT = 7;
    public static final int ALIGN_RIGHT = 8;


    private static final int DEFAULT_PADDING = 4;
    private static final int DEFAULT_MIN_HEIGHT = 36;
    private static final int DEFAULT_MIN_WIDTH = 140;
    private static final int DEFAULT_MAX_WIDTH = 250;

    int mColor;
    int mTextColor;
    int mPadding;
    int mAlign;
    int mMinHeight;
    int mMinWidth;
    int mMaxWidth;
    float mPointerPosition;
    int mVerticalMargin;
    int mHorizontalMargin;
    CharSequence mText; // required option
    int mViewId; // required option

    TipOptions(){}

    @SuppressWarnings("deprecation")
    public static TipOptions create(Context context){
        TipOptions options = new TipOptions();
        options.mColor = context.getResources().getColor(R.color.color_tip_view_default);
        options.mTextColor = context.getResources().getColor(com.imogene.apptips.R.color.color_text_default);
        options.mPadding = Util.convertDpInPixels(context, DEFAULT_PADDING);
        options.mAlign = ALIGN_LEFT_BELOW;
        options.mMinHeight = Util.convertDpInPixels(context, DEFAULT_MIN_HEIGHT);
        options.mMinWidth = Util.convertDpInPixels(context, DEFAULT_MIN_WIDTH);
        options.mMaxWidth = Util.convertDpInPixels(context, DEFAULT_MAX_WIDTH);
        options.mPointerPosition = 0.5f;
        options.mVerticalMargin = 0;
        options.mHorizontalMargin = 0;
        return options;
    }

    static TipOptions from(TipOptions src){
        TipOptions options = new TipOptions();
        options.mColor = src.mColor;
        options.mTextColor = src.mTextColor;
        options.mPadding = src.mPadding;
        options.mAlign = src.mAlign;
        options.mMinHeight = src.mMinHeight;
        options.mMinWidth = src.mMinWidth;
        options.mMaxWidth = src.mMaxWidth;
        options.mPointerPosition = src.mPointerPosition;
        options.mVerticalMargin = src.mVerticalMargin;
        options.mHorizontalMargin = src.mHorizontalMargin;
        return options;
    }

    public TipOptions setColor(int color){
        mColor = color;
        return this;
    }

    public TipOptions setTextColor(int textColor){
        mTextColor = textColor;
        return this;
    }

    public TipOptions setPadding(int padding){
        mPadding = padding;
        return this;
    }

    public TipOptions setAlign(int align){
        if(align < ALIGN_LEFT_ABOVE || align > ALIGN_RIGHT){
            throw new IllegalArgumentException("Unsupported align constant.");
        }
        mAlign = align;
        return this;
    }

    public TipOptions setMinHeight(int minHeight){
        mMinHeight = minHeight;
        return this;
    }

    public TipOptions setMinWidth(int minWidth){
        mMinWidth = minWidth;
        return this;
    }

    public TipOptions setMaxWidth(int maxWidth){
        mMaxWidth = maxWidth;
        return this;
    }

    public TipOptions setPointerPosition(float position){
        mPointerPosition = position;
        return this;
    }

    public TipOptions setVerticalMargin(int verticalMargin){
        mVerticalMargin = verticalMargin;
        return this;
    }

    public TipOptions setHorizontalMargin(int horizontalMargin){
        mHorizontalMargin = horizontalMargin;
        return this;
    }

    public TipOptions setText(CharSequence text){
        mText = text;
        return this;
    }

    public TipOptions setTarget(int viewId){
        mViewId = viewId;
        return this;
    }
}

