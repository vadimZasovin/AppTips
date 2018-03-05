package com.imogene.apptips;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * The object of this class is used to prepare and show tips
 * for different parts of the UI in {@code Activity} or
 * {@code Fragment}.
 * <p>
 * Simple example:
 * <pre><code>
 * // 'this' references to the current activity
 * AppTips appTips = new AppTips(this);
 *
 * Tip tip1 = appTips.newTip(R.id.targetView1, "tip1 text");
 *
 * View targetView2 = findViewById(R.id.targetView2);
 * Tip tip2 = appTips.newTip(targetView2, "tip 2 text");
 *
 * int tip3X = getXCoordinateForTip3();
 * int tip3Y = getYCoordinateForTip3();
 * Tip tip3 = appTips.newTip(tip3X, tip3Y, R.string.tip_3_text);
 *
 * appTips.addTips(false, tip1, tip2);
 * appTips.addTip(tip3);
 * appTips.show();
 * </code></pre>
 */
@SuppressWarnings("deprecation")
public final class AppTips {

    private static final float DIM_AMOUNT = 0.3F;

    private final Context context;
    private final WindowManager windowManager;
    private final Activity activity;
    private final Fragment fragment;
    private final android.support.v4.app.Fragment supportFragment;

    private final List<Tip> tips = new ArrayList<>();
    private int currentIndex;
    private ViewGroup wrapper;

    private final int[] position = new int[2];
    private final Rect activityVisibleFrame = new Rect();

    private OnCloseListener onCloseListener;

    /**
     * Creates new {@code AppTips} object for the given activity.
     * The target views for tips will be searched in the specified
     * activity.
     * @param activity the activity in which the target views will
     *                 be searched.
     */
    public AppTips(@NonNull Activity activity){
        Util.checkNonNullParameter(activity, "activity");
        context = activity;
        windowManager = activity.getWindowManager();
        this.activity = activity;
        this.fragment = null;
        this.supportFragment = null;
    }

    /**
     * Creates new {@code AppTips} object for the given fragment.
     * The target views for tips will be searched in the root
     * view of the specified fragment (that is returned by the
     * {@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * callback method). The fragment must be attached to activity
     * at this point.
     * @param fragment the fragment in which the target views will
     *                 be searched.
     */
    public AppTips(@NonNull Fragment fragment){
        Util.checkNonNullParameter(fragment, "fragment");
        Activity activity = fragment.getActivity();
        if(activity == null){
            throw new IllegalStateException(
                    "Fragment must be attached to activity.");
        }
        context = activity;
        windowManager = activity.getWindowManager();
        this.activity = null;
        this.fragment = fragment;
        supportFragment = null;
    }

    /**
     * Creates new {@code AppTips} object for the given fragment.
     * The target views for tips will be searched in the root
     * view of the specified fragment (that is returned by the
     * {@link android.support.v4.app.Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * callback method). The fragment must be attached to activity
     * at this point.
     * @param fragment the fragment in which the target views will
     *                 be searched.
     */
    public AppTips(@NonNull android.support.v4.app.Fragment fragment){
        Util.checkNonNullParameter(fragment, "fragment");
        Activity activity = fragment.getActivity();
        if(activity == null){
            throw new IllegalStateException(
                    "Fragment must be attached to activity.");
        }
        context = activity;
        windowManager = activity.getWindowManager();
        this.activity = null;
        this.fragment = null;
        supportFragment = fragment;
    }

    /**
     * Creates and returns new {@code Tip} object for the given target
     * and with the specified text.
     * @param targetId {@code id} of the target view of the tip.
     * @param text text message of the tip.
     * @return new {@code Tip} object.
     * @see #newTip(int, int)
     * @see #newTip(View, CharSequence)
     * @see #newTip(View, int)
     */
    public Tip newTip(@IdRes int targetId, CharSequence text){
        return new Tip(context, targetId, text);
    }

    /**
     * Creates and returns new {@code Tip} object for the given target
     * and with the specified text.
     * @param targetId {@code id} of the target view of the tip.
     * @param textRes resource id of the text message of the tip.
     * @return new {@code Tip} object.
     * @see #newTip(int, CharSequence)
     * @see #newTip(View, CharSequence)
     * @see #newTip(View, int)
     */
    public Tip newTip(@IdRes int targetId, @StringRes int textRes){
        String text = context.getString(textRes);
        return newTip(targetId, text);
    }

    /**
     * Creates and returns new {@code Tip} object for the given target
     * and with the specified text.
     * @param targetView target view of the tip.
     * @param text text message of the tip.
     * @return new {@code Tip} object.
     * @see #newTip(int, CharSequence)
     * @see #newTip(int, int)
     * @see #newTip(View, int)
     */
    public Tip newTip(View targetView, CharSequence text){
        return new Tip(context, targetView, text);
    }

    /**
     * Creates and returns new {@code Tip} object for the given target
     * and with the specified text.
     * @param targetView target view of the tip.
     * @param textRes resource id of the text message of the tip.
     * @return new {@code Tip} object.
     * @see #newTip(int, CharSequence)
     * @see #newTip(int, int)
     * @see #newTip(View, CharSequence)
     */
    public Tip newTip(View targetView, @StringRes int textRes){
        String text = context.getString(textRes);
        return newTip(targetView, text);
    }

    /**
     * Creates and returns new {@code Tip} object for the given target
     * coordinates and with the specified text. The coordinates of the
     * target are specified as screen pixels without taking into account
     * such systems windows as status bar.
     * <p>
     * You can use this method when it is not so trivial to provide a
     * target view for the tip, for example when the tip needs to be
     * associated with some point of the image on the screen and you
     * know the positions and sizes of the image. Or when the tip is
     * created for an option's icon in the {@code Toolbar}.
     * <p>
     * Highlighting is disabled by default for the returned {@code Tip},
     * because there is no target view to highlight, but you can manually
     * enable it to highlight only tip view itself.
     * @param targetX X coordinate in screen pixels of the target.
     * @param targetY Y coordinate in screen pixels of the target.
     * @param text text message of the tip.
     * @return new {@code Tip} object.
     * @see #newTip(int, int, int)
     */
    public Tip newTip(int targetX, int targetY, CharSequence text){
        Point target = new Point(targetX, targetY);
        Tip tip = new Tip(context, target, text);
        tip.setHighlightingEnabled(false);
        return tip;
    }

    /**
     * Creates and returns new {@code Tip} object for the given target
     * coordinates and with the specified text. The coordinates of the
     * target are specified as screen pixels without taking into account
     * such systems windows as status bar.
     * <p>
     * You can use this method when it is not so trivial to provide a
     * target view for the tip, for example when the tip needs to be
     * associated with some point of the image on the screen and you
     * know the positions and sizes of the image. Or when the tip is
     * created for an option's icon in the {@code Toolbar}.
     * <p>
     * Highlighting is disabled by default for the returned {@code Tip},
     * because there is no target view to highlight, but you can manually
     * enable it to highlight only tip view itself.
     * @param targetX X coordinate in screen pixels of the target.
     * @param targetY Y coordinate in screen pixels of the target.
     * @param textRes resource id of the text message of the tip.
     * @return new {@code Tip} object.
     * @see #newTip(int, int, CharSequence)
     */
    public Tip newTip(int targetX, int targetY, @StringRes int textRes){
        String text = context.getString(textRes);
        return newTip(targetX, targetY, text);
    }

    /**
     * Adds the specified {@code Tip} to this object. The tips are shown
     * then with the {@link #show()} or {@link #showNext()} methods in the
     * order in which they were added.
     * @param tip the tip to add.
     * @see #addTips(boolean, Tip...)
     */
    public void addTip(Tip tip){
        Util.checkNonNullParameter(tip, "tip");
        tips.add(tip);
    }

    /**
     * Adds the specified {@code Tip}s to this object. These tips are
     * displayed simultaneously. The tips are shown with the {@link #show()}
     * or {@link #showNext()} methods in the order in which they were added.
     * @param highlightingEnabled indicates whether the highlighting is enabled
     *                            for the specified group of tips or not. The
     *                            value of the similar property for each
     *                            specified tip is ignored.
     * @param tips the tips to add.
     */
    public void addTips(boolean highlightingEnabled, Tip... tips){
        Util.checkNonNullParameter(tips, "tips");
        int length = tips.length;
        if(length > 0){
            Tip tip = tips[0];
            tip.highlightingEnabled = highlightingEnabled;
            addTip(tip);
            for (int i = 1; i < length; i++){
                Tip sibling = tips[i];
                tip.sibling = sibling;
                tip = sibling;
            }
        }
    }

    /**
     * Adds the specified {@code Tip}s to this object. Calls the
     * {@link #addTips(boolean, Tip...)} method with
     * {@code highlightingEnabled} parameter set to {@code true}.
     * @param tips the tips to add.
     */
    public void addTips(Tip... tips){
        addTips(true, tips);
    }

    /**
     * Checks whether there are tips shown currently or not.
     * @return {@code true} if there are tips shown currently,
     * {@code false} otherwise.
     */
    public boolean isShown(){
        if(tips.size() > 0){
            Tip tip = tips.get(currentIndex);
            do {
                if(tip.tipView != null){
                    return true;
                }
                tip = tip.sibling;
            } while (tip != null);
        }
        return false;
    }

    /**
     * Shows the current portion of tips if there are no tips
     * shown currently. This method can be used to show tips for
     * the first time or to resume showing the tips that was
     * closed before.
     * <p>
     * To show the next tips, use {@link #showNext()} method instead.
     * @see #showNext()
     */
    public void show(){
        if(!isShown()){
            showTips(currentIndex);
        }
    }

    /**
     * Shows the next portion of tips if there are tips that are not
     * show yet or shows the first portion of tips if no tips has been
     * shown yet.
     * <p>
     * Next portion of tips is always shown automatically when the user
     * touches the screen outside of tip views or if all tip views are
     * removed one by one (by clicking a tip view itself).
     * @see #show()
     */
    public void showNext(){
        if(!isShown() && currentIndex == 0){
            showTips(0);
        } else if(currentIndex < tips.size() - 1) {
            showNextPortion();
        }
    }

    /**
     * Removes the currently shown tips if any. The {@link OnCloseListener}
     * will be also notified with {@code cancelled} parameter set to true
     * from within this method.
     * @see #reset()
     */
    public void close(){
        if(isShown()){
            removeTipViews();
            notifyClosed(true);
        }
    }

    /**
     * Closes the currently shown tips if any and resets the tips counter
     * so that the next call to {@link #showNext()} method will show the
     * first portion of tips.
     * @see #close()
     */
    public void reset(){
        close();
        currentIndex = 0;
    }

    /**
     * Registers a callback to be invoked when the tips are closed either
     * as a result of calling the {@link #close()} or {@link #reset()}
     * methods or after all the tips are shown by the user.
     * @param listener the close listener.
     */
    public void setOnCloseListener(OnCloseListener listener) {
        onCloseListener = listener;
    }

    /**
     * Removes the currently shown tip views from the screen and shows
     * the next portion.
     */
    private void showNextPortion(){
        removeTipViews();
        showTips(++currentIndex);
    }

    /**
     * Removes all tip views from the WindowManager
     * if there are any.
     */
    private void removeTipViews(){
        final boolean wrapped = wrapper != null;
        if(wrapped){
            windowManager.removeView(wrapper);
            wrapper = null;
        }
        if(tips.size() > 0) {
            Tip tip = tips.get(currentIndex);
            do {
                View tipView = tip.tipView;
                if(tipView != null){
                    tip.tipView = null;
                    if(!wrapped){
                        windowManager.removeView(tipView);
                    } else if(tip.highlightingView != null){
                        tip.highlightingView = null;
                    }
                }
                tip = tip.sibling;
            } while (tip != null);
        }
    }

    /**
     * Shows the portion of tips with the given index from
     * the list.
     */
    private void showTips(int index){
        if(index == tips.size()){
            notifyClosed(false);
            currentIndex = 0;
            return;
        }
        Tip tip = tips.get(index);
        if(tip.highlightingEnabled){
            showWrapped(tip);
        } else {
            showSeparately(tip);
        }
    }

    /**
     * Show the portion of tips (as specified by the given root tip)
     * by adding tip view to an special wrapper (AbsoluteLayout is
     * most convenient variant in this case despite this class is
     * deprecated) and then adding this wrapper ViewGroup to the
     * WindowManager. This method is used when highlighting is enabled.
     */
    private void showWrapped(Tip tip){
        final Tip firstSibling = tip;
        wrapper = new AbsoluteLayout(context);
        wrapper.setOnTouchListener(wrapperTouchListener);
        // add highlighting views to the wrapper first
        do {
            if(tip.target != null){
                tip = tip.sibling;
                continue;
            }
            final View highlightingView = new View(context);
            tip.highlightingView = highlightingView;
            highlightingView.setTag(R.id.tag_id_tip, tip);
            highlightingView.setOnClickListener(highlightingViewClickListener);
            AbsoluteLayout.LayoutParams lp = getLayoutParamsForWrapper();
            wrapper.addView(highlightingView, lp);
            tip = tip.sibling;
        } while (tip != null);
        tip = firstSibling;
        // add tip views to the wrapper
        do {
            View tipView = createTipView(tip);
            AbsoluteLayout.LayoutParams lp = getLayoutParamsForWrapper();
            wrapper.addView(tipView, lp);
            tip = tip.sibling;
        } while (tip != null);
        // and finally add the wrapper to the WindowManager
        WindowManager.LayoutParams lp = getWrapperLayoutParams();
        windowManager.addView(wrapper, lp);
        adjustPositions();
    }

    private AbsoluteLayout.LayoutParams getLayoutParamsForWrapper(){
        int size = ViewGroup.LayoutParams.WRAP_CONTENT;
        return new AbsoluteLayout.LayoutParams(size, size, 0, 0);
    }

    private WindowManager.LayoutParams getWrapperLayoutParams(){
        int windowType = WindowManager.LayoutParams.TYPE_APPLICATION;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(windowType);
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                WindowManager.LayoutParams.FLAG_DIM_BEHIND |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        lp.dimAmount = DIM_AMOUNT;
        return lp;
    }

    /**
     * Shows tips by adding them directly into WindowManager
     * without wrapping them into additional ViewGroup. This
     * method is used if highlighting is disabled.
     */
    private void showSeparately(Tip tip){
        boolean first = true;
        do {
            showConcreteTip(tip, first);
            tip = tip.sibling;
            first = false;
        } while (tip != null);
        adjustPositions();
    }

    /**
     * Shows a tip by adding tip view to the WindowManager.
     */
    private void showConcreteTip(Tip tip, boolean watchOutsideTouch){
        View tipView = createTipView(tip);
        WindowManager.LayoutParams lp = getTipViewLayoutParams(watchOutsideTouch);
        windowManager.addView(tipView, lp);
    }

    /**
     * Creates new tip view according to the given tip options.
     */
    private TipView createTipView(Tip tip){
        TipView tipView = new TipView(context);
        tipView.setTag(R.id.tag_id_tip, tip);
        tip.tipView = tipView;
        tipView.setColor(tip.color);
        tipView.setTextAppearance(context, tip.textAppearanceRes);
        tipView.setTextColor(tip.textColor);
        tipView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tip.textSize);
        int gravity = tip.gravity == Tip.GRAVITY_START ?
                Gravity.START | Gravity.CENTER_VERTICAL :
                Gravity.CENTER;
        tipView.setGravity(gravity);
        tipView.setOnTouchListener(tipViewTouchListener);
        tipView.setText(tip.text);
        int align = tip.align;
        if(align != Tip.ALIGN_AUTO){
            int mode = getTipViewMode(align);
            tipView.setMode(mode);
        }
        tipView.setPadding(tip.padding);
        tipView.setMinWidth(tip.minWidth);
        tipView.setMaxWidth(tip.maxWidth);
        tipView.setMinHeight(tip.minHeight);
        tipView.setPointerPosition(tip.pointerPosition);
        tipView.setPointerOffset(tip.pointerOffset);
        tipView.setPointerProtrusion(1);
        return tipView;
    }

    private WindowManager.LayoutParams getTipViewLayoutParams(boolean watchOutsideTouch){
        int windowType = WindowManager.LayoutParams.TYPE_APPLICATION;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(windowType);
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.windowAnimations = android.R.style.Animation_Dialog;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        if(watchOutsideTouch){
            lp.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        }
        return lp;
    }

    private final View.OnTouchListener tipViewTouchListener = new View.OnTouchListener() {

        private final RectF viewBounds = new RectF();
        private boolean isPressed = false;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            boolean handled = false;
            int action = event.getActionMasked();
            switch (action){
                case MotionEvent.ACTION_OUTSIDE:
                    showNextPortion();
                    handled = true;
                    break;
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_MOVE:
                    final int left = view.getLeft();
                    final int top = view.getTop();
                    final int right = view.getRight();
                    final int bottom = view.getBottom();
                    viewBounds.set(left, top, right, bottom);
                    final float eventX = left + event.getX();
                    final float eventY = top + event.getY();
                    if(action == MotionEvent.ACTION_MOVE){
                        if(isPressed && !viewBounds.contains(eventX, eventY)){
                            isPressed = false;
                        }
                        break;
                    }
                    if(viewBounds.contains(eventX, eventY)){
                        handled = true;
                        if(action == MotionEvent.ACTION_DOWN){
                            isPressed = true;
                        } else if(isPressed) {
                            isPressed = false;
                            if(!view.performClick()){
                                if(removeTipView(view)){
                                    showNextPortion();
                                } else if(wrapper == null){
                                    updateWatchingOutsideTouchesWindow();
                                }
                            }
                        }
                    }
                    break;
            }
            return handled;
        }
    };

    /**
     * Removes the specified tip view from the WindowManager
     * or from the current wrapper ViewGroup. Returns true if
     * all the tips from the current portion is removed from
     * the screen and hence the next portion must be shown.
     */
    private boolean removeTipView(View tipView){
        Tip tip = (Tip) tipView.getTag(R.id.tag_id_tip);
        tip.tipView = null;
        boolean showNextPortion = false;
        if(wrapper != null){
            wrapper.removeView(tipView);
            View highlightingView = tip.highlightingView;
            if(highlightingView != null){
                wrapper.removeView(highlightingView);
                tip.highlightingView = null;
            }
            int childCount = wrapper.getChildCount();
            if(childCount == 0){
                windowManager.removeView(wrapper);
                wrapper = null;
                showNextPortion = true;
            }
        } else {
            windowManager.removeView(tipView);
            showNextPortion = !isShown();
        }
        return showNextPortion;
    }

    /**
     * Finds first currently shown tip view and updates
     * it's layout params such that the corresponding
     * window starts to listen for 'outside touches'.
     * First tip view is a view that was added to the
     * WindowManager before others.
     */
    private void updateWatchingOutsideTouchesWindow(){
        Tip tip = tips.get(currentIndex);
        View firstTipView = null;
        do {
            View tipView = tip.tipView;
            if(tipView != null){
                firstTipView = tipView;
                break;
            }
            tip = tip.sibling;
        } while (tip != null);
        if(firstTipView != null){
            WindowManager.LayoutParams lp =
                    (WindowManager.LayoutParams) firstTipView.getLayoutParams();
            lp.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            windowManager.updateViewLayout(firstTipView, lp);
        }
    }

    private final View.OnTouchListener wrapperTouchListener = new View.OnTouchListener() {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = motionEvent.getActionMasked();
            if(action == MotionEvent.ACTION_OUTSIDE || action == MotionEvent.ACTION_DOWN){
                showNextPortion();
                return true;
            }
            return false;
        }
    };

    private final View.OnClickListener highlightingViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Tip tip = (Tip) view.getTag(R.id.tag_id_tip);
            if(removeTipView(tip.tipView)){
                showNextPortion();
            }
        }
    };

    private void notifyClosed(boolean cancelled){
        if(onCloseListener != null){
            onCloseListener.onClose(cancelled);
        }
    }

    /**
     * Adjusts positions of the tip views and highlighting views
     * (if they presented) when first layout event occurred.
     */
    private void adjustPositions(){
        Tip tip = tips.get(currentIndex);
        // find view to observe for the first global layout event
        final View viewToObserve;
        View firstTargetView = null;
        View lastTipView = tip.tipView;
        // if we find a target view that is not laid out yet
        // it will be the view to observe for the first global
        // layout event
        do {
            if(tip.target == null){
                View targetView = getTargetView(tip);
                if(!ViewCompat.isLaidOut(targetView)){
                    firstTargetView = targetView;
                    break;
                }
            }
            lastTipView = tip.tipView;
            tip = tip.sibling;
        } while (tip != null);

        viewToObserve = firstTargetView != null ? firstTargetView :
                wrapper != null ? wrapper : lastTipView;

        final ViewTreeObserver observer = viewToObserve.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(observer.isAlive()){
                    observer.removeOnGlobalLayoutListener(this);
                } else {
                    ViewTreeObserver observer = viewToObserve.getViewTreeObserver();
                    observer.removeOnGlobalLayoutListener(this);
                }

                Tip tip = tips.get(currentIndex);
                do {
                    final TipView tipView = tip.tipView;
                    final Point target = tip.target;
                    final View targetView;
                    final int targetX, targetY;
                    final int targetWidth, targetHeight;

                    if(target != null){
                        targetView = null;
                        targetX = target.x; targetY = target.y;
                        targetWidth = 0; targetHeight = 0;
                    } else {
                        targetView = getTargetView(tip);
                        targetView.getLocationOnScreen(position);
                        targetX = position[0]; targetY = position[1];
                        targetWidth = targetView.getWidth();
                        targetHeight = targetView.getHeight();
                    }

                    // adjust tip view position and size
                    int align = tip.align;
                    if(align == Tip.ALIGN_AUTO){
                        align = determineTipAlignment(targetX, targetY,
                                targetWidth, targetHeight, tip);
                        int mode = getTipViewMode(align);
                        tipView.setMode(mode);
                    }
                    getTipViewPosition(targetX, targetY, targetWidth, targetHeight, tip, align);
                    final int x = position[0], y = position[1];
                    updateTipViewLayoutParams(tipView, x, y);

                    // adjust highlighting view size and position
                    // and also it's background
                    View highlightingView = tip.highlightingView;
                    if(targetView != null && highlightingView != null){
                        AbsoluteLayout.LayoutParams hlp = (AbsoluteLayout.LayoutParams)
                                highlightingView.getLayoutParams();
                        hlp.x = targetX; hlp.y = targetY;
                        hlp.width = targetWidth;
                        hlp.height = targetHeight;
                        highlightingView.setLayoutParams(hlp);
                        setupHighlighting(targetView, highlightingView);
                    }

                    // animate pointer position if this feature is enabled
                    if(tip.pointerAnimationEnabled){
                        animateTipViewPointer(tipView);
                    }

                    tip = tip.sibling;
                } while (tip != null);
            }
        });
    }

    private View getTargetView(Tip tip){
        if(tip.targetViewCache != null){
            return tip.targetViewCache;
        } else {
            View targetView = findTargetViewForTip(tip);
            tip.targetViewCache = targetView;
            return targetView;
        }
    }

    private View findTargetViewForTip(Tip tip){
        View targetView = tip.targetView;
        if(targetView != null){
            return targetView;
        }
        int targetId = tip.targetId;
        if(activity != null){
            targetView = activity.findViewById(targetId);
        } else {
            View rootView;
            if(fragment != null){
                rootView = fragment.getView();
            } else {
                rootView = supportFragment.getView();
            }
            if(rootView != null){
                targetView = rootView.findViewById(targetId);
            }
        }
        if(targetView == null){
            throw new IllegalStateException(
                    "Target view is not found.");
        }
        return targetView;
    }

    /**
     * Determines the most appropriate for the given tip by the
     * specified target position and size. This method is used when
     * the initial alignment (set by the user) has value {@link Tip#ALIGN_AUTO}.
     */
    private int determineTipAlignment(int targetX, int targetY, int targetWidth,
                                      int targetHeight, Tip tip){
        Window window = getActivityWindow();
        View decorView = window.getDecorView();
        decorView.getWindowVisibleDisplayFrame(activityVisibleFrame);
        final int availableWidth = activityVisibleFrame.width();
        final int availableHeight = activityVisibleFrame.height();
        final int statusBarHeight = activityVisibleFrame.top;
        targetY -= statusBarHeight;

        final int offsetX = tip.horizontalOffset;
        final int offsetY = tip.verticalOffset;

        final int availableToLeft = targetX - offsetX;
        final int availableToRight = availableWidth - (targetX + targetWidth + offsetX);
        final int availableAbove = targetY - offsetY;
        final int availableBelow = availableHeight - (targetY + targetHeight + offsetY);

        final View tipView = tip.tipView;
        final int tipWidth = tipView.getWidth();
        final int tipHeight = tipView.getHeight();

        boolean alignToRight = availableToRight > tipWidth;
        boolean alignToLeft = availableToLeft > tipWidth;
        boolean alignBelow = availableBelow > tipHeight;
        boolean alignAbove = availableAbove > tipHeight;

        if(alignToLeft || alignToRight){
            if(tipHeight <= targetHeight){
                return alignToRight ? Tip.ALIGN_RIGHT : Tip.ALIGN_LEFT;
            } else {
                boolean moreBelow = availableBelow > availableAbove;
                boolean moreAbove = availableAbove > availableBelow;
                if(alignToRight){
                    return moreBelow ? Tip.ALIGN_RIGHT_TOP :
                            moreAbove ? Tip.ALIGN_RIGHT_BOTTOM : Tip.ALIGN_RIGHT;
                } else {
                    return moreBelow ? Tip.ALIGN_LEFT_TOP :
                            moreAbove ? Tip.ALIGN_LEFT_BOTTOM : Tip.ALIGN_LEFT;
                }
            }
        } else if(alignBelow || alignAbove){
            if(tipWidth <= targetWidth){
                return alignAbove ? Tip.ALIGN_CENTER_ABOVE : Tip.ALIGN_CENTER_BELOW;
            } else {
                boolean moreToLeft = availableToLeft > availableToRight;
                boolean moreToRight = availableToRight > availableToLeft;
                if(alignBelow){
                    return moreToLeft ? Tip.ALIGN_LEFT_BELOW :
                            moreToRight ? Tip.ALIGN_RIGHT_BELOW : Tip.ALIGN_CENTER_BELOW;
                } else {
                    return moreToLeft ? Tip.ALIGN_LEFT_ABOVE :
                            moreToRight ? Tip.ALIGN_RIGHT_ABOVE : Tip.ALIGN_CENTER_ABOVE;
                }
            }
        } else {
            return Tip.ALIGN_CENTER_INSIDE;
        }
    }

    private Window getActivityWindow(){
        final Window window;
        if(activity != null){
            window = activity.getWindow();
        } else {
            Activity activity;
            if(fragment != null){
                activity = fragment.getActivity();
            } else {
                activity = supportFragment.getActivity();
            }
            window = activity != null ? activity.getWindow() : null;
        }
        if(window == null){
            throw new IllegalStateException(
                    "Activity is not visual, " +
                    "could not retrieve window.");
        }
        return window;
    }

    /**
     * Returns mode for TipView by the specified align of the tip.
     * This method must be used only if the alignment is determined,
     * i.e. if the align is not {@link Tip#ALIGN_AUTO}, otherwise
     * this method throws an exception.
     */
    private int getTipViewMode(int align){
        switch (align){
            case Tip.ALIGN_CENTER_ABOVE:
            case Tip.ALIGN_LEFT_ABOVE:
            case Tip.ALIGN_RIGHT_ABOVE:
            case Tip.ALIGN_CENTER_INSIDE:
                return TipView.MODE_ABOVE_TARGET;
            case Tip.ALIGN_LEFT:
            case Tip.ALIGN_LEFT_TOP:
            case Tip.ALIGN_LEFT_BOTTOM:
                return TipView.MODE_TO_LEFT_TARGET;
            case Tip.ALIGN_RIGHT:
            case Tip.ALIGN_RIGHT_TOP:
            case Tip.ALIGN_RIGHT_BOTTOM:
                return TipView.MODE_TO_RIGHT_TARGET;
            case Tip.ALIGN_CENTER_BELOW:
            case Tip.ALIGN_LEFT_BELOW:
            case Tip.ALIGN_RIGHT_BELOW:
                return TipView.MODE_BELOW_TARGET;
            default:
                throw new IllegalArgumentException(
                        "Could not determine " +
                        "TipView mode for auto align.");
        }
    }

    /**
     * Calculates the absolute position for the given tip and
     * alignment according to the absolute position and size of
     * the target. This method must be called only if the tip
     * view is laid out and alignment is determined.
     */
    private void getTipViewPosition(int targetX, int targetY, int targetWidth,
                                    int targetHeight, Tip tip, int align){
        final TipView tipView = tip.tipView;
        final int tipHeight = tipView.getHeight();
        final int tipWidth = tipView.getWidth();
        final int offsetX = tip.horizontalOffset;
        final int offsetY = tip.verticalOffset;
        int delta;
        final int x, y;
        switch (align){
            case Tip.ALIGN_LEFT_BELOW:
                x = targetX + offsetX;
                y = targetY + targetHeight + offsetY;
                break;
            case Tip.ALIGN_RIGHT_BELOW:
                delta = targetWidth - tipWidth;
                x = targetX + delta + offsetX;
                y = targetY + targetHeight + offsetY;
                break;
            case Tip.ALIGN_CENTER_BELOW:
                delta = (targetWidth - tipWidth) / 2;
                x = targetX + delta + offsetX;
                y = targetY + targetHeight + offsetY;
                break;
            case Tip.ALIGN_LEFT_ABOVE:
                x = targetX + offsetX;
                y = targetY - tipHeight - offsetY;
                break;
            case Tip.ALIGN_RIGHT_ABOVE:
                delta = targetWidth - tipWidth;
                x = targetX + delta + offsetX;
                y = targetY - tipHeight - offsetY;
                break;
            case Tip.ALIGN_CENTER_ABOVE:
                delta = (targetWidth - tipWidth) / 2;
                x = targetX + delta + offsetX;
                y = targetY - tipHeight - offsetY;
                break;
            case Tip.ALIGN_LEFT:
                x = targetX - tipWidth - offsetX;
                delta = (targetHeight - tipHeight) / 2;
                y = targetY + delta + offsetY;
                break;
            case Tip.ALIGN_LEFT_TOP:
                x = targetX - tipWidth - offsetX;
                y = targetY + offsetY;
                break;
            case Tip.ALIGN_LEFT_BOTTOM:
                x = targetX - tipWidth - offsetX;
                delta = targetHeight - tipHeight;
                y = targetY + delta - offsetY;
                break;
            case Tip.ALIGN_RIGHT:
                x = targetX + targetWidth + offsetX;
                delta = (targetHeight - tipHeight) / 2;
                y = targetY + delta + offsetY;
                break;
            case Tip.ALIGN_RIGHT_TOP:
                x = targetX + targetWidth + offsetX;
                y = targetY + offsetY;
                break;
            case Tip.ALIGN_RIGHT_BOTTOM:
                x = targetX + targetWidth + offsetX;
                delta = targetHeight - tipHeight;
                y = targetY + delta - offsetY;
                break;
            case Tip.ALIGN_CENTER_INSIDE:
                delta = (targetWidth - tipWidth) / 2;
                x = targetX + delta + offsetX;
                int tipBodyHeight = tipHeight - tipView.pointerSize;
                delta = (targetHeight - tipBodyHeight) / 2;
                y = targetY + delta + offsetY;
                break;
            default:
                x = 0; y = 0;
                break;
        }
        position[0] = x; position[1] = y;
    }

    private void updateTipViewLayoutParams(View tipView, int x, int y){
        ViewGroup.LayoutParams lp = tipView.getLayoutParams();
        if(lp instanceof WindowManager.LayoutParams){
            WindowManager.LayoutParams wlp = (WindowManager.LayoutParams) lp;
            wlp.x = x; wlp.y = y;
            windowManager.updateViewLayout(tipView, wlp);
        } else {
            AbsoluteLayout.LayoutParams alp = (AbsoluteLayout.LayoutParams) lp;
            alp.x = x; alp.y = y;
            tipView.setLayoutParams(alp);
        }
    }

    private void setupHighlighting(View targetView, View highlightingView){
        final int width = targetView.getWidth();
        final int height = targetView.getHeight();
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        targetView.draw(canvas);
        Resources resources = context.getResources();
        Drawable background = new BitmapDrawable(resources, bitmap);
        highlightingView.setBackground(background);
    }

    private void animateTipViewPointer(TipView tipView){
        final float initialPosition = 0.5F;
        final float finalPosition = tipView.getPointerPosition();
        if(initialPosition != finalPosition){
            Animator animator = ObjectAnimator.ofFloat(
                    tipView, "pointerPosition",
                    initialPosition, finalPosition);
            animator.start();
        }

        Animator animator = ObjectAnimator.ofFloat(
                tipView, "pointerProtrusion", 0F, 1F);
        animator.start();
    }

    /**
     * Interface definition for the callback to be invoked when the
     * tips are closed either by calling the {@link #close()} or
     * {@link #reset()} methods or when all tips are shown by the user.
     */
    public interface OnCloseListener {

        /**
         * Called when the tips are closed.
         * @param cancelled indicates whether tips are closed as a result
         *                  of calling the {@link #close()} or {@link #reset()}
         *                  methods (then this value is true) or when all the
         *                  tips are shown by the user (then false.)
         */
        void onClose(boolean cancelled);
    }
}
