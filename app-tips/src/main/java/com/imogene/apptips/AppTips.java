package com.imogene.apptips;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.RectF;
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
public final class AppTips {

    private static final float DIM_AMOUNT = 0.3F;

    private final Context context;
    private final WindowManager windowManager;
    private final Activity activity;
    private final Fragment fragment;
    private final android.support.v4.app.Fragment supportFragment;
    private final List<Tip> tips = new ArrayList<>();

    private int currentIndex;
    private View currentView;
    private Point position = new Point();

    private OnCloseListener onCloseListener;
    private OnTipChangeListener onTipChangeListener;

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
        return currentView != null;
    }

    /**
     * Shows the first tip if there are no tips
     * shown currently.
     * <p>
     * To show the next tip, use {@link #showNextTip()}
     * method instead.
     * @see #showNextTip()
     */
    public void show(){
        if(!isShown()){
            showTips(0);
        }
    }

    /**
     * Shows the next tip if there are tips that are not
     * shown yet. If there are no tips shown currently,
     * shows the first tip.
     * @see #show()
     */
    public void showNextTip(){
        if(!isShown()){
            show();
        } else {
            int size = tips.size();
            if(currentIndex < size - 1){
                showTips(++currentIndex);
            }
        }
    }

    /**
     * Shows the next portion of tips with the given index
     * from the list. This method also removes all currently
     * shown tip views from the {@link WindowManager}.
     */
    private void showTips(int index){
        removeTipViews();
        if(index == tips.size()){
            notifyClosed(false);
            reset();
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
     * Removes all tip views from the WindowManager
     * if there are any.
     */
    private void removeTipViews(){

    }

    private void showWrapped(Tip tip){
        AbsoluteLayout wrapper = new AbsoluteLayout(context);
        do {
            View tipView = createTipView(tip);
            int size = ViewGroup.LayoutParams.WRAP_CONTENT;
            AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(size, size, 0, 0);
            wrapper.addView(tipView, lp);
            View targetView = findTargetViewForTip(tip);
            adjustPosition(targetView, tipView, tip);
            tip = tip.sibling;
        } while (tip != null);
        WindowManager.LayoutParams lp = getWrapperLayoutParams();
        windowManager.addView(wrapper, lp);
    }

    private WindowManager.LayoutParams getWrapperLayoutParams(){
        int windowType = WindowManager.LayoutParams.TYPE_APPLICATION;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(windowType);
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.windowAnimations = android.R.style.Animation_Dialog;
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
        do {
            showConcreteTip(tip);
            tip = tip.sibling;
        } while (tip != null);
    }

    /**
     * Shows a tip by adding tip view to the WindowManager.
     */
    private void showConcreteTip(Tip tip){
        View tipView = createTipView(tip);
        View targetView = findTargetViewForTip(tip);
        WindowManager.LayoutParams lp = getTipViewLayoutParams();
        windowManager.addView(tipView, lp);
        adjustPosition(targetView, tipView, tip);
    }

    /**
     * Creates new tip view according to the given tip options.
     */
    private TipView createTipView(Tip tip){
        TipView tipView = new TipView(context);
        tipView.setColor(tip.color);
        tipView.setTextColor(tip.textColor);
        tipView.setOnTouchListener(onTouchListener);
        tipView.setText(tip.text);
        int align = tip.align;
        int mode = getTipViewMode(align);
        tipView.setMode(mode);
        tipView.setPadding(tip.padding);
        tipView.setMinWidth(tip.minWidth);
        tipView.setMaxWidth(tip.maxWidth);
        tipView.setMinHeight(tip.minHeight);
        tipView.setPointerPosition(tip.pointerPosition);
        return tipView;
    }

    private WindowManager.LayoutParams getTipViewLayoutParams(){
        int windowType = WindowManager.LayoutParams.TYPE_APPLICATION;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(windowType);
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.windowAnimations = android.R.style.Animation_Dialog;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        return lp;
    }

    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        private final RectF viewBounds = new RectF();
        private boolean isPressed = false;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            boolean handled = false;
            boolean showNextTip = false;
            int action = event.getActionMasked();
            switch (action){
                case MotionEvent.ACTION_OUTSIDE:
                    handled = true;
                    showNextTip = true;
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
                            showNextTip = !view.performClick();
                        }
                    }
                    break;
            }
            if(showNextTip){
                showTips(++currentIndex);
            }
            return handled;
        }
    };

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

    private View findTargetViewForTip(Tip tip){
        View target = tip.targetView;
        if(target != null){
            return target;
        }
        int targetId = tip.targetId;
        if(activity != null){
            target = activity.findViewById(targetId);
        } else {
            View rootView;
            if(fragment != null){
                rootView = fragment.getView();
            } else {
                rootView = supportFragment.getView();
            }
            if(rootView != null){
                target = rootView.findViewById(targetId);
            }
        }
        if(target == null){
            throw new IllegalStateException(
                    "Target view is not found.");
        }
        return target;
    }

    private void notifyClosed(boolean cancelled){
        if(onCloseListener != null){
            onCloseListener.onClose(cancelled);
        }
    }

    /**
     * Listens for the first global layout event and adjusts the position
     * of the given tip view according to dimensions and position of the
     * target view and tip options.
     */
    private void adjustPosition(final View targetView, final View tipView, final Tip tip){
        // in order to adjust tip view's position we need to know the dimensions of both:
        // tip view and target view. If the tips are shown when the target view is
        // already laid out we observe tip view, otherwise we observe target view.
        final View viewToObserve = ViewCompat.isLaidOut(targetView) ? tipView : targetView;
        ViewTreeObserver observer = viewToObserve.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver observer = viewToObserve.getViewTreeObserver();
                observer.removeOnGlobalLayoutListener(this);
                getTipViewPosition(targetView, tipView, tip);
                ViewGroup.LayoutParams lp = tipView.getLayoutParams();
                if(lp instanceof WindowManager.LayoutParams){
                    WindowManager.LayoutParams wlp = (WindowManager.LayoutParams) lp;
                    wlp.x = position.x; wlp.y = position.y;
                    windowManager.updateViewLayout(tipView, lp);
                } else if(lp instanceof AbsoluteLayout.LayoutParams){
                    AbsoluteLayout.LayoutParams alp = (AbsoluteLayout.LayoutParams) lp;
                    alp.x = position.x; alp.y = position.y;
                    tipView.setLayoutParams(alp);
                }
            }
        });
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
        final int targetX = (int) targetView.getX();
        final int targetY = (int) targetView.getY();
        final int targetHeight = targetView.getHeight();
        final int targetWidth = targetView.getWidth();
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

    private void reset(){
        currentIndex = 0;
        currentView = null;
    }

    private void notifyTipChanged(int index, View target){
        if(onTipChangeListener != null){
            onTipChangeListener.onTipChanged(index, target);
        }
    }

    /**
     * Closes the currently shown tips if any.
     */
    public void close(){
        if(isShown()){
            windowManager.removeView(currentView);
            notifyClosed(true);
            reset();
        }
    }

    public void setOnCloseListener(OnCloseListener listener) {
        onCloseListener = listener;
    }

    public void setOnTipChangeListener(OnTipChangeListener listener) {
        onTipChangeListener = listener;
    }

    public interface OnCloseListener{

        void onClose(boolean cancelled);
    }

    public interface OnTipChangeListener{

        void onTipChanged(int index, View target);
    }
}
