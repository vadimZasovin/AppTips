package com.imogene.apptips;

import android.app.Activity;
import android.graphics.PixelFormat;
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
    private final TipOptions defaultOptions;
    private final float dimAmount;

    private View.OnTouchListener onTouchListener;
    private boolean shown;
    private int currentTip;
    private TipView currentTipView;

    private OnShowListener onShowListener;
    private OnCloseListener onCloseListener;
    private OnTipChangeListener onTipChangeListener;

    private AppTips(Builder builder){
        activity = builder.activity;
        view = builder.view;
        tips = builder.tips;
        defaultOptions = builder.defaultOptions;
        dimAmount = builder.dimAmount;
    }

    public void show(){
        if(!shown){
            showTips();
        }
    }

    private void showTips(){
        ensureOnTouchListener();
        showTip(0);
        shown = true;
        if(onShowListener != null){
            onShowListener.onShow();
        }
    }

    private void ensureOnTouchListener(){
        if(onTouchListener == null){
            onTouchListener = createOnTouchListener();
        }
    }

    private View.OnTouchListener createOnTouchListener(){
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean handled = false;
                if(event.getActionMasked() == MotionEvent.ACTION_OUTSIDE){
                    showTip(++currentTip);
                    handled = true;
                }
                return handled;
            }
        };
    }

    private void showTip(int tipIndex){
        WindowManager windowManager = activity.getWindowManager();

        if(tipIndex != 0){
            windowManager.removeView(currentTipView);
        }

        if(tipIndex == tips.size()){
            if(onCloseListener != null){
                onCloseListener.onClose(false);
            }

            shown = false;
            currentTip = 0;
            currentTipView = null;
            return;
        }

        TipOptions options = tips.get(tipIndex);

        currentTipView = new TipView(activity);
        currentTipView.setColor(options.color);
        currentTipView.setTextColor(options.textColor);
        currentTipView.setOnTouchListener(onTouchListener);
        currentTipView.setText(options.text);
        int align = options.align;
        int mode = getTipViewMode(align);
        currentTipView.setMode(mode);
        currentTipView.setPaddingWithRespectToPointerSize(options.padding);
        currentTipView.setMinWidth(options.minWidth);
        currentTipView.setMaxWidth(options.maxWidth);
        currentTipView.setMinHeight(options.minHeight);
        currentTipView.setPointerPosition(options.pointerPosition);

        int viewId = options.viewId;
        View target = view != null ?
                view.findViewById(viewId) :
                activity.findViewById(viewId);
        if(target == null){
            throw new IllegalStateException("View with id " + viewId + " is not found.");
        }

        WindowManager.LayoutParams layoutParams =
                generateLayoutParams(currentTipView, target, options);
        windowManager.addView(currentTipView, layoutParams);

        if(onTipChangeListener != null){
            onTipChangeListener.onTipChanged(tipIndex, target);
        }
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

    private WindowManager.LayoutParams generateLayoutParams(final View tipView, View target,
                                                            TipOptions options){
        WindowManager.LayoutParams layoutParams = new WindowManager
                .LayoutParams(WindowManager.LayoutParams.TYPE_APPLICATION);
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.windowAnimations = android.R.style.Animation_Dialog;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        if(dimAmount > 0){
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            layoutParams.dimAmount = dimAmount;
        }

        final float targetX = target.getX();
        final float targetY = target.getY();

        final int verticalMargin = options.verticalMargin;
        final int horizontalMargin = options.horizontalMargin;

        switch (options.align){
            case TipOptions.ALIGN_LEFT_BELOW:
                layoutParams.gravity = Gravity.TOP | Gravity.START;
                layoutParams.x = (int) targetX + horizontalMargin;
                layoutParams.y = (int) (targetY + target.getHeight() + verticalMargin);
                break;
            case TipOptions.ALIGN_RIGHT_BELOW:
                layoutParams.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, layoutParams){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        int delta = target.getWidth() - tipView.getWidth();
                        layoutParams.x = (int) (targetX + delta + horizontalMargin);
                    }
                }.scheduleAdjusting();
                layoutParams.y = (int) (targetY + target.getHeight() + verticalMargin);
                break;
            case TipOptions.ALIGN_CENTER_BELOW:
                layoutParams.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, layoutParams){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        int delta = (target.getWidth() - tipView.getWidth()) / 2;
                        layoutParams.x = (int) (targetX + delta + horizontalMargin);
                    }
                }.scheduleAdjusting();
                layoutParams.y = (int) (targetY + target.getHeight() + verticalMargin);
                break;
            case TipOptions.ALIGN_LEFT_ABOVE:
                layoutParams.gravity = Gravity.TOP | Gravity.START;
                layoutParams.x = (int) (targetX + horizontalMargin);
                new LayoutAdjuster(target, layoutParams){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        layoutParams.y = (int) (targetY - tipView.getHeight() - verticalMargin);
                    }
                }.scheduleAdjusting();
                break;
            case TipOptions.ALIGN_RIGHT_ABOVE:
                layoutParams.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, layoutParams){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        int delta = target.getWidth() - tipView.getWidth();
                        layoutParams.x = (int) (targetX + delta + horizontalMargin);
                        layoutParams.y = (int) (targetY - tipView.getHeight() - verticalMargin);
                    }
                }.scheduleAdjusting();
                break;
            case TipOptions.ALIGN_CENTER_ABOVE:
                layoutParams.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, layoutParams){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        int delta = (target.getWidth() - tipView.getWidth()) / 2;
                        layoutParams.x = (int) (targetX + delta + horizontalMargin);
                        layoutParams.y = (int) (targetY - tipView.getHeight() - verticalMargin);
                    }
                }.scheduleAdjusting();
                break;
            case TipOptions.ALIGN_LEFT:
                layoutParams.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, layoutParams){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        layoutParams.x = (int) (targetX - tipView.getWidth() - horizontalMargin);
                        int delta = (target.getHeight() - tipView.getHeight()) / 2;
                        layoutParams.y = (int) (targetY + delta + verticalMargin);
                    }
                }.scheduleAdjusting();
                break;
            case TipOptions.ALIGN_RIGHT:
                layoutParams.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, layoutParams){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        layoutParams.x = (int) (targetX + target.getWidth() + horizontalMargin);
                        int delta = (target.getHeight() - tipView.getHeight()) / 2;
                        layoutParams.y = (int) (targetY + delta + verticalMargin);
                    }
                }.scheduleAdjusting();
        }

        return layoutParams;
    }

    public void close(){
        if(currentTipView != null){
            WindowManager windowManager = activity.getWindowManager();
            windowManager.removeView(currentTipView);

            if(onCloseListener != null) {
                onCloseListener.onClose(true);
            }

            shown = false;
            currentTip = 0;
            currentTipView = null;
        }
    }

    public void setOnShowListener(OnShowListener listener) {
        onShowListener = listener;
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
        TipOptions defaultOptions;
        float dimAmount = -1f;

        private Builder(Activity activity, View view){
            tips = new ArrayList<>();
            this.activity = activity;
            this.view = null;
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
                throw new IllegalStateException("Default tip options must be set");
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
            return new AppTips(this);
        }

        public AppTips show() {
            AppTips appTips = new AppTips(this);
            appTips.show();
            return appTips;
        }
    }

    private abstract class LayoutAdjuster implements ViewTreeObserver.OnGlobalLayoutListener{

        private View target;
        private WindowManager.LayoutParams layoutParams;

        LayoutAdjuster(View target, WindowManager.LayoutParams layoutParams){
            this.target = target;
            this.layoutParams = layoutParams;
        }

        @Override
        public void onGlobalLayout() {
            onAdjustLayout(target, layoutParams);
            activity.getWindowManager().updateViewLayout(currentTipView, layoutParams);
            currentTipView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }

        void scheduleAdjusting(){
            currentTipView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }

        abstract void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams);
    }

    public interface OnShowListener{

        void onShow();
    }

    public interface OnCloseListener{

        void onClose(boolean cancelled);
    }

    public interface OnTipChangeListener{

        void onTipChanged(int index, View target);
    }
}
