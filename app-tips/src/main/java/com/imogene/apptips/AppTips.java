package com.imogene.apptips;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 25.04.2016.
 */
public final class AppTips {

    private static final float DIM_AMOUNT = 0.3F;

    private final Activity activity;
    private final Fragment fragment;
    private final android.support.v4.app.Fragment supportFragment;
    private final List<Tip> tips = new ArrayList<>();

    private int currentIndex;
    private View currentView;

    private OnCloseListener onCloseListener;
    private OnTipChangeListener onTipChangeListener;

    public AppTips(@NonNull Activity activity){
        Util.checkNonNullParameter(activity, "activity");
        this.activity = activity;
        fragment = null;
        supportFragment = null;
    }

    public AppTips(@NonNull Fragment fragment){
        Util.checkNonNullParameter(fragment, "fragment");
        this.fragment = fragment;
        activity = null;
        supportFragment = null;
    }

    public AppTips(@NonNull android.support.v4.app.Fragment fragment){
        Util.checkNonNullParameter(fragment, "fragment");
        this.supportFragment = fragment;
        activity = null;
        this.fragment = null;
    }

    private Context getContext(){
        return activity != null ? activity
                : fragment != null ? fragment.getActivity()
                : supportFragment.getActivity();
    }

    public Tip newTip(@IdRes int targetId, @StringRes int textRes){
        return new Tip(getContext(), targetId, textRes);
    }

    public Tip newTip(@IdRes int targetId, CharSequence text){
        return new Tip(getContext(), targetId, text);
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
            showTip(0);
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
                showTip(++currentIndex);
            }
        }
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
                showTip(++currentIndex);
            }
            return handled;
        }
    };

    private void showTip(int tipIndex){
        WindowManager windowManager = getWindowManager();
        if(currentView != null){
            windowManager.removeView(currentView);
        }
        if(tipIndex == tips.size()){
            notifyClosed(false);
            reset();
            return;
        }
        Tip tip = tips.get(tipIndex);
        boolean multipleTips = tip.sibling != null;
        currentView = multipleTips ? createTipsLayout(tip) : createTipView(tip);

        int viewId = tip.targetId;
        View target = findTarget(viewId);

        WindowManager.LayoutParams lp = getLayoutParams(target, currentView, tip);
        windowManager.addView(currentView, lp);
        notifyTipChanged(tipIndex, target);
    }

    private View createTipsLayout(Tip tip){
        return null;
    }

    private TipView createTipView(Tip tip){
        Context context = getContext();
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

    private WindowManager getWindowManager(){
        return activity.getWindowManager();
    }

    private void notifyClosed(boolean cancelled){
        if(onCloseListener != null){
            onCloseListener.onClose(cancelled);
        }
    }

    private void reset(){
        currentIndex = 0;
        currentView = null;
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

    private View findTarget(int targetId){
        View target = null;
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

    private WindowManager.LayoutParams getLayoutParams(View target, View tipView, Tip tip){
        int windowType = WindowManager.LayoutParams.TYPE_APPLICATION;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(windowType);
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.windowAnimations = android.R.style.Animation_Dialog;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        if(tip.highlightingEnabled){
            lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lp.dimAmount = DIM_AMOUNT;
        }
        scheduleAdjusting(target, tipView, tip, lp);
        return lp;
    }

    private void scheduleAdjusting(final View target, final View tipView,
                                   final Tip options,
                                   final WindowManager.LayoutParams lp){
        ViewTreeObserver observer = tipView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver observer = tipView.getViewTreeObserver();
                observer.removeOnGlobalLayoutListener(this);

                final int targetX = (int) target.getX();
                final int targetY = (int) target.getY();
                final int targetHeight = target.getHeight();
                final int targetWidth = target.getWidth();
                final int tipHeight = tipView.getHeight();
                final int tipWidth = tipView.getWidth();
                final int offsetX = options.horizontalOffset;
                final int offsetY = options.verticalOffset;
                final int delta;

                lp.gravity = Gravity.TOP | Gravity.START;
                final int x, y;
                switch (options.align){
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

                lp.x = x; lp.y = y;
                WindowManager manager = getWindowManager();
                manager.updateViewLayout(tipView, lp);
            }
        });
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
            WindowManager windowManager = getWindowManager();
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
