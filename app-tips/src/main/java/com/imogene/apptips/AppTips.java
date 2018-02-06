package com.imogene.apptips;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 25.04.2016.
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
    private Point position = new Point();
    private ViewGroup wrapper;

    private OnCloseListener onCloseListener;

    public AppTips(@NonNull Activity activity){
        Util.checkNonNullParameter(activity, "activity");
        context = activity;
        windowManager = activity.getWindowManager();
        this.activity = activity;
        this.fragment = null;
        this.supportFragment = null;
    }

    public AppTips(@NonNull Fragment fragment){
        Util.checkNonNullParameter(fragment, "fragment");
        Activity activity = fragment.getActivity();
        if(activity == null){
            throw new IllegalArgumentException(
                    "Fragment must be attached to activity.");
        }
        context = activity;
        windowManager = activity.getWindowManager();
        this.activity = null;
        this.fragment = fragment;
        supportFragment = null;
    }

    public AppTips(@NonNull android.support.v4.app.Fragment fragment){
        Util.checkNonNullParameter(fragment, "fragment");
        Activity activity = fragment.getActivity();
        if(activity == null){
            throw new IllegalArgumentException(
                    "Fragment must be attached to activity.");
        }
        context = activity;
        windowManager = activity.getWindowManager();
        this.activity = null;
        this.fragment = null;
        supportFragment = fragment;
    }

    public Tip newTip(@IdRes int targetId, CharSequence text){
        return new Tip(context, targetId, text);
    }

    public Tip newTip(@IdRes int targetId, @StringRes int textRes){
        String text = context.getString(textRes);
        return newTip(targetId, text);
    }

    public Tip newTip(View targetView, CharSequence text){
        return new Tip(context, targetView, text);
    }

    public Tip newTip(View targetView, @StringRes int textRes){
        String text = context.getString(textRes);
        return newTip(targetView, text);
    }

    public Tip newTip(int targetX, int targetY, CharSequence text){
        Point target = new Point(targetX, targetY);
        Tip tip = new Tip(context, target, text);
        tip.setHighlightingEnabled(false);
        return tip;
    }

    public Tip newTip(int targetX, int targetY, @StringRes int textRes){
        String text = context.getString(textRes);
        return newTip(targetX, targetY, text);
    }

    public void addTip(Tip tip){
        tips.add(tip);
    }

    public void addTips(boolean highlightingEnabled, Tip... tips){
        Util.checkNonNullParameter(tips, "tips");
        int length = tips.length;
        if(length == 0){
            throw new IllegalStateException("The array of tips must not be empty.");
        }
        Tip tip = tips[0];
        tip.highlightingEnabled = highlightingEnabled;
        addTip(tip);
        for (int i = 1; i < length; i++){
            Tip sibling = tips[i];
            tip.sibling = sibling;
            tip = sibling;
        }
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
     * Shows the first portion of tips if there are no tips
     * shown currently.
     * <p>
     * To show the next tips, use {@link #showNext()} method instead.
     * @see #showNext()
     */
    public void show(){
        if(!isShown()){
            currentIndex = 0;
            showTips(0);
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
     *
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
     * Registers a callback to be invoked when a tips are closed either
     * as a result of calling the {@link #close()} method or when all
     * tips are shown by the user.
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
                        // destroy drawing cache of the target view
                        View targetView = getTargetView(tip);
                        targetView.destroyDrawingCache();
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
            View highlightingView = new View(context);
            View targetView = getTargetView(tip);
            setupHighlighting(targetView, highlightingView);
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

    /**
     * Setup highlighting for the view by using target view's drawing
     * cache bitmap as the background. If the drawing cache is not
     * prepared yet this method registers an onPreDrawListener to
     * wait for the target view to be drawn to obtain it's drawing
     * cache.
     */
    private void setupHighlighting(final View targetView, final View viewToHighlight){
        if(!highlightView(targetView, viewToHighlight)){
            ViewTreeObserver observer = targetView.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if(highlightView(targetView, viewToHighlight)){
                        ViewTreeObserver observer = targetView.getViewTreeObserver();
                        observer.removeOnPreDrawListener(this);
                    }
                    return true;
                }
            });
        }
    }

    private boolean highlightView(View targetView, View viewToHighlight){
        targetView.buildDrawingCache();
        Bitmap drawingCache = targetView.getDrawingCache();
        if(drawingCache != null){
            Resources resources = context.getResources();
            Drawable background = new BitmapDrawable(resources, drawingCache);
            viewToHighlight.setBackground(background);
            return true;
        }
        return false;
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
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                WindowManager.LayoutParams.FLAG_DIM_BEHIND;
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
        tipView.setTextColor(tip.textColor);
        tipView.setOnTouchListener(tipViewTouchListener);
        tipView.setText(tip.text);
        int align = tip.align;
        int mode = getTipViewMode(align);
        tipView.setMode(mode);
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
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
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
                // destroy drawing cache of the target view
                View targetView = getTargetView(tip);
                targetView.destroyDrawingCache();
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

    private int getTipViewMode(int align){
        switch (align){
            case Tip.ALIGN_CENTER_ABOVE:
            case Tip.ALIGN_LEFT_ABOVE:
            case Tip.ALIGN_RIGHT_ABOVE:
                return TipView.MODE_ABOVE_TARGET;
            case Tip.ALIGN_LEFT:
                return TipView.MODE_TO_LEFT_TARGET;
            case Tip.ALIGN_RIGHT:
                return TipView.MODE_TO_RIGHT_TARGET;
            default:
                return TipView.MODE_BELOW_TARGET;
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
            if(tip.target != null){
                tip = tip.sibling;
                continue;
            }
            View targetView = getTargetView(tip);
            if(!ViewCompat.isLaidOut(targetView)){
                firstTargetView = targetView;
                break;
            }
            lastTipView = tip.tipView;
            tip = tip.sibling;
        } while (tip != null);
        viewToObserve = firstTargetView != null ? firstTargetView :
                wrapper != null ? wrapper : lastTipView;

        ViewTreeObserver observer = viewToObserve.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver observer = viewToObserve.getViewTreeObserver();
                observer.removeOnGlobalLayoutListener(this);

                Tip tip = tips.get(currentIndex);
                do {
                    TipView tipView = tip.tipView;
                    Point target = tip.target;
                    if(target != null){
                        int x = target.x, y = target.y;
                        getTipViewPosition(x, y, tipView, tip);
                    } else {
                        View targetView = getTargetView(tip);
                        getTipViewPosition(targetView, tipView, tip);

                        // adjust highlighting view size and position
                        View highlightingView = tip.highlightingView;
                        if(highlightingView != null){
                            AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams)
                                    highlightingView.getLayoutParams();
                            lp.x = (int) targetView.getX();
                            lp.y = (int) targetView.getY();
                            lp.width = targetView.getWidth();
                            lp.height = targetView.getHeight();
                            highlightingView.setLayoutParams(lp);
                        }
                    }

                    // adjust tip view position
                    int x = position.x, y = position.y;
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

                    // animate pointer position if this feature is enabled
                    if(tip.pointerAnimationEnabled){
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
     * Calculates the absolute position for the given tip view
     * according to the position of the target view and tip
     * options. The calculated coordinates are written to the
     * {@code position} field. This function can only be used
     * if the target view and tip view are laid out and has
     * their dimensions set.
     */
    private void getTipViewPosition(View targetView, View tipView, Tip tip){
        int targetX = (int) targetView.getX();
        int targetY = (int) targetView.getY();
        int targetHeight = targetView.getHeight();
        int targetWidth = targetView.getWidth();
        getTipViewPosition(targetX, targetY, targetWidth, targetHeight, tipView, tip);
    }

    /**
     * Calculates the absolute position for the given tip view
     * according to the absolute position of the target,
     * specified as first two arguments. This method must be
     * called only if the tip view is laid out.
     */
    private void getTipViewPosition(int targetX, int targetY, View tipView, Tip tip){
        getTipViewPosition(targetX, targetY, 0, 0, tipView, tip);
    }

    /**
     * Calculates the absolute position for the given tip view
     * according to the absolute position and size of the target.
     * This method must be called only if the tip view is laid out.
     */
    private void getTipViewPosition(int targetX, int targetY,
                                    int targetWidth, int targetHeight,
                                    View tipView, Tip tip){
        final int tipHeight = tipView.getHeight();
        final int tipWidth = tipView.getWidth();
        final int offsetX = tip.horizontalOffset;
        final int offsetY = tip.verticalOffset;
        final int delta;
        final int x, y;
        switch (tip.align){
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
            default:
                x = targetX + targetWidth + offsetX;
                delta = (targetHeight - tipHeight) / 2;
                y = targetY + delta + offsetY;
                break;
        }
        position.set(x, y);
    }

    /**
     * Interface definition for the callback to be invoked when
     * tips are closed either by calling the {@link #close()}
     * method or when all tips are shown by the user.
     */
    public interface OnCloseListener {

        /**
         * Called when a tips are closed.
         * @param cancelled indicates whether tips are closed as a result
         *                  of calling the {@link #close()} method (then this
         *                  value is true) or when all tips are shown by the
         *                  user (then false.)
         */
        void onClose(boolean cancelled);
    }
}
