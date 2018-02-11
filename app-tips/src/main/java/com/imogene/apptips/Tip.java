package com.imogene.apptips;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.IdRes;
import android.view.Gravity;
import android.view.View;

/**
 * The object of this class holds all necessary information
 * about appearance and behavior of an tip. You create a tip
 * by using the {@link AppTips#newTip(int, CharSequence)}
 * method or it's overloads. You can further customize the
 * returned object by changing some of it's properties such
 * as color, alignment, pointer position etc. Note that all
 * customizable properties has default values which are
 * appropriate for most cases. The default values are noted
 * for each concrete property.
 */
public final class Tip {

    /** Tip alignment. Aligns the tip view above and to the left of the target. */
    public static final int ALIGN_LEFT_ABOVE = 1;

    /** Tip alignment. Aligns the tip view below and to the left of the target. */
    public static final int ALIGN_LEFT_BELOW = 2;

    /** Tip alignment. Aligns the tip view above and to the right of the target. */
    public static final int ALIGN_RIGHT_ABOVE = 3;

    /** Tip alignment. Aligns the tip view below and to the right of the target. */
    public static final int ALIGN_RIGHT_BELOW = 4;

    /** Tip alignment. Aligns the tip view above and to the center of the target. */
    public static final int ALIGN_CENTER_ABOVE = 5;

    /** Tip alignment. Aligns the tip view below and to the center of the target. */
    public static final int ALIGN_CENTER_BELOW = 6;

    /**
     * Tip alignment. Aligns the right side of the tip view (with pointer) to the
     * left side of the target and aligns to the center of the target vertically.
     */
    public static final int ALIGN_LEFT = 7;
    /**
     * Tip alignment. Aligns the left side of the tip view (with pointer) to the
     * right side of the target and aligns to the center of the target vertically.
     */
    public static final int ALIGN_RIGHT = 8;

    final int targetId;
    final View targetView;
    final Point target;
    final CharSequence text;

    int color;
    int textColor;
    int textSize;
    int gravity;
    int padding;
    int align;
    int minHeight;
    int minWidth;
    int maxWidth;
    float pointerPosition;
    int pointerOffset;
    boolean pointerAnimationEnabled;
    int verticalOffset;
    int horizontalOffset;
    boolean highlightingEnabled;

    Tip sibling;
    TipView tipView;
    View highlightingView;
    View targetViewCache;

    Tip(Context context, @IdRes int targetId, CharSequence text){
        this.targetId = targetId;
        this.targetView = null;
        this.target = null;
        this.text = text;
        initializeDefaults(context);
    }

    Tip(Context context, View targetView, CharSequence text){
        this.targetId = View.NO_ID;
        this.targetView = targetView;
        this.target = null;
        this.text = text;
        initializeDefaults(context);
    }

    Tip(Context context, Point target, CharSequence text){
        this.targetId = View.NO_ID;
        this.targetView = null;
        this.target = target;
        this.text = text;
        initializeDefaults(context);
    }

    private void initializeDefaults(Context context){
        Resources resources = context.getResources();
        color = obtainDefaultColor(context);
        textColor = Color.WHITE;
        textSize = resources.getDimensionPixelSize(R.dimen.tip_view_default_text_size);
        gravity = Gravity.CENTER;
        padding = resources.getDimensionPixelSize(R.dimen.tip_view_default_padding);
        minHeight = resources.getDimensionPixelSize(R.dimen.tip_view_default_min_height);
        minWidth = resources.getDimensionPixelSize(R.dimen.tip_view_default_min_width);
        maxWidth = resources.getDimensionPixelSize(R.dimen.tip_view_default_max_width);
        align = ALIGN_LEFT_BELOW;
        pointerPosition = 0.5f;
        pointerOffset = 0;
        pointerAnimationEnabled = true;
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

    /**
     * Sets the color of the tip view. The default color is the
     * {@code R.attr.colorAccent} or {@code android.R.attr.colorAccent}
     * from the application theme. If such a color is not defined in the
     * theme the default value is {@link Color#DKGRAY}.
     * @param color new color of the tip.
     */
    public void setColor(int color){
        this.color = color;
    }

    /**
     * Returns the current color of the tip.
     * @return color of the tip.
     */
    public int getColor() {
        return color;
    }

    /**
     * Sets the text color of the tip. The default text color is
     * white ({@link Color#WHITE}).
     * @param textColor new text color of the tip.
     */
    public void setTextColor(int textColor){
        this.textColor = textColor;
    }

    /**
     * Returns the current text color of the tip.
     * @return text color of the tip.
     */
    public int getTextColor() {
        return textColor;
    }

    /**
     * Stets the text size of the tip in pixels. The default value is
     * {@code 14sp}.
     * @param textSize size of the text of the tip.
     */
    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    /**
     * Returns the current text size of the tip.
     * @return text size of the tip.
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * Sets the gravity of the tip view. The default gravity
     * is {@link Gravity#CENTER}.
     * @param gravity gravity of the tip view.
     */
    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    /**
     * Returns the current gravity of the tip view.
     * @return gravity of the tip view.
     */
    public int getGravity() {
        return gravity;
    }

    /**
     * Sets the padding of the tip view in pixels. The default value
     * is {@code 4dp}.
     * @param padding new padding of the tip view in pixels.
     */
    public void setPadding(int padding){
        this.padding = padding;
    }

    /**
     * Returns the current padding of the tip view.
     * @return padding of the tip view.
     */
    public int getPadding() {
        return padding;
    }

    /**
     * Sets the alignment of the tip view. There are 8 possible
     * values and they are listed as constants in this class.
     * The default value is {@link #ALIGN_LEFT_BELOW}.
     * @param align the desired alignment of the tip view.
     */
    public void setAlign(int align){
        if(align < ALIGN_LEFT_ABOVE || align > ALIGN_RIGHT){
            throw new IllegalArgumentException(
                    "Unsupported align: " + align + ".");
        }
        this.align = align;
    }

    /**
     * Returns the current alignment value.
     * @return alignment.
     */
    public int getAlign() {
        return align;
    }

    /**
     * Sets the minimum height of the tip view in pixels. This value
     * specifies the minimum height of the rectangle with a text,
     * without taking into account the pointer triangle. The default
     * value is {@code 36dp}.
     * @param minHeight minimum height of the tip view in pixels.
     */
    public void setMinHeight(int minHeight){
        this.minHeight = minHeight;
    }

    /**
     * Returns the current minimum height of the tip view.
     * @return minimum height of the tip view.
     */
    public int getMinHeight() {
        return minHeight;
    }

    /**
     * Sets the minimum width of the tip view in pixels. This value
     * specifies the minimum width of the rectangle with a text,
     * without taking into account the pointer triangle. The default
     * value is {@code 144dp}.
     * @param minWidth minimum width of the tip view in pixels.
     */
    public void setMinWidth(int minWidth){
        this.minWidth = minWidth;
    }

    /**
     * Returns the current minimum width of the tip view.
     * @return minimum width of the tip view.
     */
    public int getMinWidth() {
        return minWidth;
    }

    /**
     * Sets the maximum width of the tip view in pixels. This value
     * specifies the maximum width of the rectangle with a text,
     * without taking into account the pointer triangle. The default
     * value is {@code 248dp}.
     * @param maxWidth maximum width of the tip view in pixels.
     */
    public void setMaxWidth(int maxWidth){
        this.maxWidth = maxWidth;
    }

    /**
     * Returns the current maximum width of the tip view.
     * @return maximum width of the tip view.
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    /**
     * Sets the position of the pointer as a fraction from the
     * width or height of the tip view (depending on the alignment).
     * The default value is {@code 0.5}, meaning that the pointer
     * is positioned in the center of the appropriate side of the
     * rectangle of the tip view.
     * @param position position of the pointer as fraction from the
     *                 size of the appropriate side. Must be in range
     *                 [0, 1].
     * @see #setPointerOffset(int)
     */
    public void setPointerPosition(float position){
        pointerPosition = position;
    }

    /**
     * Returns the current position of the pointer.
     * @return position of the pointer.
     */
    public float getPointerPosition() {
        return pointerPosition;
    }

    /**
     * Sets the offset in pixels of the pointer from it's default
     * position (centered along the appropriate side). The negative
     * values are applicable and means that the pointer must be moved
     * to the left/top (depending on the alignment). The default value
     * is {@code 0}.
     * <p>
     * This option takes precedence over the {@link #setPointerPosition(float)}
     * property when the position of the pointer is calculated, i.e. if the
     * pointer offset is not {@code 0} the value set with the
     * {@link #setPointerPosition(float)} method is ignored.
     * @param pointerOffset offset of the pointer in pixels.
     * @see #setPointerPosition(float)
     */
    public void setPointerOffset(int pointerOffset) {
        this.pointerOffset = pointerOffset;
    }

    /**
     * Returns the current offset of the pointer.
     * @return offset of the pointer.
     */
    public int getPointerOffset() {
        return pointerOffset;
    }

    /**
     * Specifies whether the pointer must be animated or not. If the default
     * position of the pointer is overridden either by the
     * {@link #setPointerPosition(float)} or {@link #setPointerOffset(int)}
     * methods and the property is enabled then when this tip's corresponding
     * view is shown the pointer position will be animated from the default
     * value (centered along the side) to the desired position.
     * <p>
     * In addition even if the position of the pointer is not overridden
     * the appearing of the pointer will be animated if this property is
     * enabled.
     * <p>
     * The default value is {@code true}.
     * @param pointerAnimationEnabled boolean specifying whether the pointer
     *                                should be animated or not.
     */
    public void setPointerAnimationEnabled(boolean pointerAnimationEnabled) {
        this.pointerAnimationEnabled = pointerAnimationEnabled;
    }

    /**
     * Indicates whether the animation of the pointer is enabled for this
     * tip or not.
     * @return {@code true} if the animation of the pointer is enabled,
     * {@code false} otherwise.
     */
    public boolean isPointerAnimationEnabled() {
        return pointerAnimationEnabled;
    }

    /**
     * Sets the vertical offset of the tip view in pixels. Negative values
     * are applicable. The default value is {@code 0}.
     * @param verticalOffset vertical offset of the tip view in pixels.
     * @see #setHorizontalOffset(int)
     */
    public void setVerticalOffset(int verticalOffset){
        this.verticalOffset = verticalOffset;
    }

    /**
     * Returns the current vertical offset of the tip view.
     * @return vertical offset of the tip view.
     */
    public int getVerticalOffset() {
        return verticalOffset;
    }

    /**
     * Sets the horizontal offset of the tip view in pixels. Negative values
     * are applicable. The default value is {@code 0}.
     * @param horizontalOffset horizontal offset of the tip view in pixels.
     * @see #setVerticalOffset(int)
     */
    public void setHorizontalOffset(int horizontalOffset){
        this.horizontalOffset = horizontalOffset;
    }

    /**
     * Returns the current horizontal offset of the tip view.
     * @return horizontal offset of the tip view.
     */
    public int getHorizontalOffset() {
        return horizontalOffset;
    }

    /**
     * Specifies whether the highlighting option is enabled for
     * this tip. The highlighting means that the tip view and
     * the corresponding target view (if it is specified) will
     * be visually highlighted by dimming the screen behind, and
     * thus making them brighter. The default value is {@code true}.
     * @param highlightingEnabled boolean specifying whether the
     *                            highlighting is enabled or not.
     */
    public void setHighlightingEnabled(boolean highlightingEnabled) {
        this.highlightingEnabled = highlightingEnabled;
    }

    /**
     * Indicates whether the highlighting is enabled for this tip
     * or not.
     * @return {@code true} if highlighting is enabled, {@code false}
     * otherwise.
     */
    public boolean isHighlightingEnabled() {
        return highlightingEnabled;
    }
}