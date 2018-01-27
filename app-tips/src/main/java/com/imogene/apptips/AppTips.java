package com.imogene.apptips;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.RectF;
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
public class AppTips {

    private final Activity activity;
    private final View view;
    private final List<TipOptions> tips;
    private final float dimAmount;

    private int currentIndex;
    private TipView currentView;

    private OnCloseListener onCloseListener;
    private OnTipChangeListener onTipChangeListener;

    private AppTips(Builder builder){
        activity = builder.activity;
        view = builder.view;
        tips = builder.tips;
        dimAmount = builder.dimAmount;
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

        TipOptions options = tips.get(tipIndex);

        currentView = new TipView(activity);
        currentView.setColor(options.color);
        currentView.setTextColor(options.textColor);
        currentView.setOnTouchListener(onTouchListener);
        currentView.setText(options.text);
        int align = options.align;
        int mode = getTipViewMode(align);
        currentView.setMode(mode);
        currentView.setPaddingWithRespectToPointerSize(options.padding);
        currentView.setMinWidth(options.minWidth);
        currentView.setMaxWidth(options.maxWidth);
        currentView.setMinHeight(options.minHeight);
        currentView.setPointerPosition(options.pointerPosition);

        int viewId = options.viewId;
        View target = getTarget(viewId);

        WindowManager.LayoutParams lp = getLayoutParams(target, currentView, options);
        windowManager.addView(currentView, lp);
        notifyTipChanged(tipIndex, target);
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
            case TipOptions.ALIGN_CENTER_ABOVE:
            case TipOptions.ALIGN_LEFT_ABOVE:
            case TipOptions.ALIGN_RIGHT_ABOVE:
                return TipView.MODE_ABOVE_TARGET;
            case TipOptions.ALIGN_LEFT:
                return TipView.MODE_TO_LEFT_TARGET;
            case TipOptions.ALIGN_RIGHT:
                return TipView.MODE_TO_RIGHT_TARGET;
            default:
                return TipView.MODE_BELOW_TARGET;
        }
    }

    private View getTarget(int targetId){
        View target = view != null ?
                view.findViewById(targetId) :
                activity.findViewById(targetId);
        if(target == null){
            throw new IllegalStateException(
                    "Target view is not found.");
        }
        return target;
    }

    private WindowManager.LayoutParams getLayoutParams(View target, View tipView,
                                                       TipOptions options){
        int windowType = WindowManager.LayoutParams.TYPE_APPLICATION;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(windowType);
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.windowAnimations = android.R.style.Animation_Dialog;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        if(dimAmount > 0){
            lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lp.dimAmount = dimAmount;
        }
        scheduleAdjusting(target, tipView, options, lp);
        return lp;
    }

    private void scheduleAdjusting(final View target, final View tipView,
                                   final TipOptions options,
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
                final int marginX = options.horizontalMargin;
                final int marginY = options.verticalMargin;
                final int delta;

                lp.gravity = Gravity.TOP | Gravity.START;
                final int x, y;
                switch (options.align){
                    case TipOptions.ALIGN_LEFT_BELOW:
                        x = targetX + marginX;
                        y = targetY + targetHeight + marginY;
                        break;
                    case TipOptions.ALIGN_RIGHT_BELOW:
                        delta = targetWidth - tipWidth;
                        x = targetX + delta + marginX;
                        y = targetY + targetHeight + marginY;
                        break;
                    case TipOptions.ALIGN_CENTER_BELOW:
                        delta = (targetWidth - tipWidth) / 2;
                        x = targetX + delta + marginX;
                        y = targetY + targetHeight + marginY;
                        break;
                    case TipOptions.ALIGN_LEFT_ABOVE:
                        x = targetX + marginX;
                        y = targetY - tipHeight - marginY;
                        break;
                    case TipOptions.ALIGN_RIGHT_ABOVE:
                        delta = targetWidth - tipWidth;
                        x = targetX + delta + marginX;
                        y = targetY - tipHeight - marginY;
                        break;
                    case TipOptions.ALIGN_CENTER_ABOVE:
                        delta = (targetWidth - tipWidth) / 2;
                        x = targetX + delta + marginX;
                        y = targetY - tipHeight - marginY;
                        break;
                    case TipOptions.ALIGN_LEFT:
                        x = targetX - tipWidth - marginX;
                        delta = (targetHeight - tipHeight) / 2;
                        y = targetY + delta + marginY;
                        break;
                    default:
                        x = targetX + targetWidth + marginX;
                        delta = (targetHeight - tipHeight) / 2;
                        y = targetY + delta + marginY;
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

    public static class Builder{

        final Activity activity;
        final View view;
        final List<TipOptions> tips;
        float dimAmount = -1f;
        private TipOptions defaultOptions;

        private Builder(Activity activity, View view){
            tips = new ArrayList<>();
            this.activity = activity;
            this.view = view;
        }

        public Builder(Activity activity){
            this(activity, null);
        }

        public Builder(View view){
            this((Activity) view.getContext(), view);
        }

        public Builder dimAmount(float dimAmount) {
            this.dimAmount = dimAmount;
            return this;
        }

        public Builder tip(TipOptions options) {
            tips.add(options);
            return this;
        }

        public Builder tip(int viewId, CharSequence text) {
            if(defaultOptions == null){
                throw new IllegalStateException(
                        "Default options must be set.");
            }
            TipOptions options = TipOptions.from(defaultOptions);
            options.viewId = viewId;
            options.text = text;
            tips.add(options);
            return this;
        }

        public Builder defaultOptions(TipOptions options) {
            defaultOptions = options;
            return this;
        }

        public AppTips build() {
            if(tips.isEmpty()){
                throw new IllegalStateException(
                        "At least one tip must be specified.");
            }
            return new AppTips(this);
        }

        public AppTips show() {
            AppTips appTips = build();
            appTips.show();
            return appTips;
        }
    }

    public interface OnCloseListener{

        void onClose(boolean cancelled);
    }

    public interface OnTipChangeListener{

        void onTipChanged(int index, View target);
    }
}
