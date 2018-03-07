package com.imogene.apptips;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.IdRes;
import android.support.annotation.StyleRes;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * The object of this class holds all necessary information
 * about appearance and behavior of an tip. You create a tip
 * by using the {@link AppTips#newTip(int, CharSequence)}
 * method or it's overloads. You can further customize the
 * returned object by changing some of it's properties such
 * as color, alignment, pointer position etc.
 * <p>
 * Note that all customizable properties has default values and
 * some of these values are taken from the theme. You can change
 * the default values by creating your own style and setting
 * up it in the app theme:
 * <pre><code>
 * &lt;style name="MyOwnTipStyle" parent="TipStyle"&gt;
 *     &lt;item name="tipColor"&gt;@color/my_tip_color&lt;/item&gt;
 *     &lt;item name="tipTextAppearance"&gt;@style/MyTipTextAppearance&lt;/item&gt;
 *     &lt;item name="tipGravity"&gt;start&lt;/item&gt;
 *     &lt;item name="tipPadding"&gt;8dp&lt;/item&gt;
 *     &lt;item name="tipMinHeight"&gt;64dp&lt;/item&gt;
 *     &lt;item name="tipMinWidth"&gt;96dp&lt;/item&gt;
 *     &lt;item name="tipMaxWidth"&gt;300dp&lt;/item&gt;
 *     &lt;item name="tipAnimatePointer"&gt;false&lt;/item&gt;
 *     &lt;item name="tipHighlighting"&gt;true&lt;/item&gt;
 * &lt;/style&gt;
 *
 * &lt;style name="AppTheme" parent="..."&gt;
 *     ...
 *     &lt;item name="tipStyle"&gt;@style/MyOwnTipStyle&lt;/item&gt;
 * &lt;/style&gt;
 * </code></pre>
 */
public final class Tip {

    /** Tip gravity. Aligns the text inside the tip view to the start. */
    public static final int GRAVITY_START = 1;

    /** Tip gravity. aligns the text inside the tip view to the center. */
    public static final int GRAVITY_CENTER = 2;

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

    /**
     * Tip alignment. Aligns the right side of the tip view (with pointer) to the
     * left side of the target and aligns the top side of the tip view to the top
     * side of the target.
     */
    public static final int ALIGN_LEFT_TOP = 9;

    /**
     * Tip alignment. Aligns the right side of the tip view (with pointer) to the
     * left side of the target and aligns the bottom side of the tip view to the
     * bottom side of the target.
     */
    public static final int ALIGN_LEFT_BOTTOM = 10;

    /**
     * Tip alignment. Aligns the left side of the tip view (with pointer) to the
     * right side of the target and aligns the top side of the tip view to the
     * top side of the target.
     */
    public static final int ALIGN_RIGHT_TOP = 11;

    /**
     * Tip alignment. Aligns the left side of the tip view (with pointer) to the
     * right side of the target and aligns the bottom side of the tip view to the
     * bottom side of the target.
     */
    public static final int ALIGN_RIGHT_BOTTOM = 12;

    /**
     * Tip alignment. Places the tip view to the center of the target view. The
     * pointer is positioned at the bottom side of the tip view.
     */
    public static final int ALIGN_CENTER_INSIDE = 13;

    /**
     * Tip alignment. Indicates that the actual alignment will be chosen
     * automatically by the position and size of the target and size of
     * the tip view. This alignment is set by default for newly created
     * {@code Tip} objects.
     */
    public static final int ALIGN_AUTO = 14;

    private static final int DEFAULT_TEXT_SIZE_SP = 14;

    final int targetId;
    final View targetView;
    final Point target;
    final CharSequence text;

    int color;
    int textAppearanceRes;
    int textColor;
    int textSize;
    int gravity;
    int padding;
    int align;
    int minHeight;
    int minWidth;
    int maxWidth;
    boolean autoPointerPositionEnabled;
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
        // get the default values that appears in the style
        TypedArray array = context.obtainStyledAttributes(
                null, R.styleable.Tip, R.attr.tipStyle, R.style.TipStyle);
        try {
            color = array.getColor(R.styleable.Tip_tipColor, 0);
            textAppearanceRes = array.getResourceId(R.styleable.Tip_tipTextAppearance, 0);

            // get the default text color and size from the text appearance resource
            // in order to have the actual value to return from the appropriate getters
            final int[] attrs = new int[]{android.R.attr.textColor, android.R.attr.textSize};
            TypedArray taArray = context.obtainStyledAttributes(textAppearanceRes, attrs);
            try {
                textColor = taArray.getColor(0, Color.WHITE);
                Resources resources = context.getResources();
                DisplayMetrics metrics = resources.getDisplayMetrics();
                int defaultSize = metrics.densityDpi * DEFAULT_TEXT_SIZE_SP / 160;
                textSize = taArray.getDimensionPixelSize(1, defaultSize);
            } finally {
                taArray.recycle();
            }

            gravity = array.getInteger(R.styleable.Tip_tipGravity, 0);
            padding = array.getDimensionPixelSize(R.styleable.Tip_tipPadding, 0);
            minHeight = array.getDimensionPixelSize(R.styleable.Tip_tipMinHeight, 0);
            minWidth = array.getDimensionPixelSize(R.styleable.Tip_tipMinWidth, 0);
            maxWidth = array.getDimensionPixelSize(R.styleable.Tip_tipMaxWidth, 0);
            pointerAnimationEnabled = array.getBoolean(R.styleable.Tip_tipAnimatePointer, true);
            highlightingEnabled = array.getBoolean(R.styleable.Tip_tipHighlighting, true);
        } finally {
            array.recycle();
        }

        // set the rest defaults
        align = ALIGN_AUTO;
        autoPointerPositionEnabled = true;
        pointerPosition = 0.5f;
        pointerOffset = 0;
        verticalOffset = 0;
        horizontalOffset = 0;
    }

    /**
     * Sets the color of the tip view.
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
     * Sets the text appearance of the tip view.
     * @param styleRes the resource id of the new text appearance style
     *                 of the tip view.
     */
    public void setTextAppearance(@StyleRes int styleRes){
        textAppearanceRes = styleRes;
    }

    /**
     * Sets the text color of the tip.
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
     * Stets the text size of the tip in pixels.
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
     * Sets the gravity of the tip view. Can be either
     * {@link #GRAVITY_CENTER} or {@link #GRAVITY_START}.
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
     * Sets the padding of the tip view in pixels.
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
     * The default value is {@link #ALIGN_AUTO}.
     * @param align the desired alignment of the tip view.
     */
    public void setAlign(int align){
        if(align < ALIGN_LEFT_ABOVE || align > ALIGN_AUTO){
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
     * without taking into account the pointer triangle.
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
     * without taking into account the pointer triangle.
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
     * without taking into account the pointer triangle.
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
     * Specifies whether the position of the pointer must be determined
     * automatically or not. If this property is set to {@code true} the
     * values of {@code pointerPosition} and {@code pointerOffset} are
     * ignored and the actual position of the pointer will be determined
     * automatically by the alignment of the tip and it's size.
     * <p>
     * This feature is enabled by default.
     * @param autoPointerPosition boolean specifying whether the position
     *                            of the pointer must be determined
     *                            automatically or not.
     */
    public void setAutoPointerPositionEnabled(boolean autoPointerPosition) {
        this.autoPointerPositionEnabled = autoPointerPosition;
    }

    /**
     * Indicates whether the automatic pointer position is enabled or not.
     * @return {@code true} if the feature is enabled, {@code false} otherwise.
     */
    public boolean isAutoPointerPositionEnabled() {
        return autoPointerPositionEnabled;
    }

    /**
     * Sets the position of the pointer as a fraction from the
     * width or height of the tip view (depending on the alignment).
     * The default value is {@code 0.5}, meaning that the pointer
     * is positioned in the center of the appropriate side of the
     * rectangle of the tip view.
     * <p>
     * This property is ignored if {@link #setAutoPointerPositionEnabled(boolean)}
     * set to {@code true}.
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
     * <p>
     * This property is ignored if {@link #setAutoPointerPositionEnabled(boolean)}
     * set to {@code true}.
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
     * thus making them brighter.
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