package com.imogene.apptips;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;

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

    int color;
    int textColor;
    int padding;
    int align;
    int minHeight;
    int minWidth;
    int maxWidth;
    float pointerPosition;
    int verticalMargin;
    int horizontalMargin;
    CharSequence text;
    int viewId;

    private TipOptions(){}

    public static TipOptions create(Context context){
        TipOptions options = new TipOptions();
        options.color = obtainDefaultColor(context);
        options.textColor = Color.WHITE;
        options.padding = Util.convertDpInPixels(context, DEFAULT_PADDING);
        options.align = ALIGN_LEFT_BELOW;
        options.minHeight = Util.convertDpInPixels(context, DEFAULT_MIN_HEIGHT);
        options.minWidth = Util.convertDpInPixels(context, DEFAULT_MIN_WIDTH);
        options.maxWidth = Util.convertDpInPixels(context, DEFAULT_MAX_WIDTH);
        options.pointerPosition = 0.5f;
        options.verticalMargin = 0;
        options.horizontalMargin = 0;
        return options;
    }

    @SuppressWarnings("ResourceType")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static int obtainDefaultColor(Context context){
        boolean isPostLollipop = Util.checkApiVersion(Build.VERSION_CODES.LOLLIPOP);

        int[] attrs = !isPostLollipop ? new int[]{R.attr.colorAccent} :
                new int[]{android.R.attr.colorAccent, R.attr.colorAccent};

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs);
        try {
            int color = array.getColor(0, 0);

            if(color == 0){
                if(!isPostLollipop){
                    return getPredefinedColor(context);
                }else {
                    color = array.getColor(1,0);
                }
            }else {
                return color;
            }

            if(color == 0){
                color = getPredefinedColor(context);
            }

            return color;
        }finally {
            array.recycle();
        }
    }

    @SuppressWarnings("deprecation")
    private static int getPredefinedColor(Context context){
        return context.getResources().getColor(R.color.color_tip_view_default);
    }

    static TipOptions from(TipOptions src){
        TipOptions options = new TipOptions();
        options.color = src.color;
        options.textColor = src.textColor;
        options.padding = src.padding;
        options.align = src.align;
        options.minHeight = src.minHeight;
        options.minWidth = src.minWidth;
        options.maxWidth = src.maxWidth;
        options.pointerPosition = src.pointerPosition;
        options.verticalMargin = src.verticalMargin;
        options.horizontalMargin = src.horizontalMargin;
        return options;
    }

    public TipOptions color(int color){
        this.color = color;
        return this;
    }

    public TipOptions textColor(int textColor){
        this.textColor = textColor;
        return this;
    }

    public TipOptions padding(int padding){
        this.padding = padding;
        return this;
    }

    public TipOptions align(int align){
        if(align < ALIGN_LEFT_ABOVE || align > ALIGN_RIGHT){
            throw new IllegalArgumentException("Unsupported align constant.");
        }
        this.align = align;
        return this;
    }

    public TipOptions minHeight(int minHeight){
        this.minHeight = minHeight;
        return this;
    }

    public TipOptions minWidth(int minWidth){
        this.minWidth = minWidth;
        return this;
    }

    public TipOptions maxWidth(int maxWidth){
        this.maxWidth = maxWidth;
        return this;
    }

    public TipOptions pointerPosition(float position){
        pointerPosition = position;
        return this;
    }

    public TipOptions verticalMargin(int verticalMargin){
        this.verticalMargin = verticalMargin;
        return this;
    }

    public TipOptions horizontalMargin(int horizontalMargin){
        this.horizontalMargin = horizontalMargin;
        return this;
    }

    public TipOptions text(CharSequence text){
        this.text = text;
        return this;
    }

    public TipOptions target(int viewId){
        this.viewId = viewId;
        return this;
    }
}