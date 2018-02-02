package com.imogene.apptips;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.view.View;

/**
 * Created by Admin on 25.04.2016.
 */
public final class Tip {

    public static final int ALIGN_LEFT_ABOVE = 1;
    public static final int ALIGN_LEFT_BELOW = 2;
    public static final int ALIGN_RIGHT_ABOVE = 3;
    public static final int ALIGN_RIGHT_BELOW = 4;
    public static final int ALIGN_CENTER_ABOVE = 5;
    public static final int ALIGN_CENTER_BELOW = 6;
    public static final int ALIGN_LEFT = 7;
    public static final int ALIGN_RIGHT = 8;

    final int targetId;
    final View targetView;
    final CharSequence text;

    int color;
    int textColor;
    int padding;
    int align;
    int minHeight;
    int minWidth;
    int maxWidth;
    float pointerPosition;
    int verticalOffset;
    int horizontalOffset;
    boolean highlightingEnabled;

    Tip sibling;

    Tip(Context context, @IdRes int targetId, CharSequence text){
        this.targetId = targetId;
        this.targetView = null;
        this.text = text;
        initializeDefaults(context);
    }

    Tip(Context context, View targetView, CharSequence text){
        this.targetId = View.NO_ID;
        this.targetView = targetView;
        this.text = text;
        initializeDefaults(context);
    }

    private void initializeDefaults(Context context){
        Resources resources = context.getResources();
        color = obtainDefaultColor(context);
        textColor = Color.WHITE;
        padding = resources.getDimensionPixelSize(R.dimen.tip_view_default_padding);
        minHeight = resources.getDimensionPixelSize(R.dimen.tip_view_default_min_height);
        minWidth = resources.getDimensionPixelSize(R.dimen.tip_view_default_min_width);
        maxWidth = resources.getDimensionPixelSize(R.dimen.tip_view_default_max_width);
        align = ALIGN_LEFT_BELOW;
        pointerPosition = 0.5f;
        verticalOffset = 0;
        horizontalOffset = 0;
        highlightingEnabled = true;
    }

    @SuppressWarnings("ResourceType")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static int obtainDefaultColor(Context context){
        final boolean isLollipop = Util.checkApiVersion(Build.VERSION_CODES.LOLLIPOP);
        final int[] attrs = !isLollipop ? new int[]{R.attr.colorAccent} :
                new int[]{android.R.attr.colorAccent, R.attr.colorAccent};
        final int fallbackColor = Color.DKGRAY;
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs);
        try {
            int defaultColor = isLollipop ? array.getColor(1, fallbackColor) : fallbackColor;
            return array.getColor(0, defaultColor);
        }finally {
            array.recycle();
        }
    }

    public void setColor(int color){
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setTextColor(int textColor){
        this.textColor = textColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setPadding(int padding){
        this.padding = padding;
    }

    public int getPadding() {
        return padding;
    }

    public void setAlign(int align){
        if(align < ALIGN_LEFT_ABOVE || align > ALIGN_RIGHT){
            throw new IllegalArgumentException(
                    "Unsupported align: " + align + ".");
        }
        this.align = align;
    }

    public int getAlign() {
        return align;
    }

    public void setMinHeight(int minHeight){
        this.minHeight = minHeight;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public void setMinWidth(int minWidth){
        this.minWidth = minWidth;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMaxWidth(int maxWidth){
        this.maxWidth = maxWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setPointerPosition(float position){
        pointerPosition = position;
    }

    public float getPointerPosition() {
        return pointerPosition;
    }

    public void setVerticalOffset(int verticalOffset){
        this.verticalOffset = verticalOffset;
    }

    public int getVerticalOffset() {
        return verticalOffset;
    }

    public void setHorizontalOffset(int horizontalOffset){
        this.horizontalOffset = horizontalOffset;
    }

    public int getHorizontalOffset() {
        return horizontalOffset;
    }

    public void setHighlightingEnabled(boolean highlightingEnabled) {
        this.highlightingEnabled = highlightingEnabled;
    }

    public boolean isHighlightingEnabled() {
        return highlightingEnabled;
    }
}