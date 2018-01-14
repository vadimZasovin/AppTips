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
    private final float dimAmount;

    private int currentTip;
    private TipView currentTipView;

    private OnShowListener onShowListener;
    private OnCloseListener onCloseListener;
    private OnTipChangeListener onTipChangeListener;

    private AppTips(Builder builder){
        activity = builder.activity;
        view = builder.view;
        tips = builder.tips;
        dimAmount = builder.dimAmount;
    }

    public boolean isShown(){
        return currentTipView != null;
    }

    public void show(){
        if(!isShown()){
            showTip(0);
            notifyShown();
        }
    }

    private void notifyShown(){
        if(onShowListener != null){
            onShowListener.onShow();
        }
    }

    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {
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

    private void showTip(int tipIndex){
        WindowManager windowManager = activity.getWindowManager();

        if(currentTipView != null){
            windowManager.removeView(currentTipView);
        }

        if(tipIndex == tips.size()){
            notifyClosed(false);
            reset();
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
        View target = getTarget(viewId);

        WindowManager.LayoutParams lp = getLayoutParams(currentTipView, target, options);
        windowManager.addView(currentTipView, lp);
        notifyTipChanged(tipIndex, target);
    }

    private void notifyClosed(boolean cancelled){
        if(onCloseListener != null){
            onCloseListener.onClose(cancelled);
        }
    }

    private void reset(){
        currentTip = 0;
        currentTipView = null;
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
            throw new IllegalStateException("Target is not found.");
        }
        return target;
    }

    private WindowManager.LayoutParams getLayoutParams(final View tipView, View target,
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

        final float targetX = target.getX();
        final float targetY = target.getY();

        final int verticalMargin = options.verticalMargin;
        final int horizontalMargin = options.horizontalMargin;

        switch (options.align){
            case TipOptions.ALIGN_LEFT_BELOW:
                lp.gravity = Gravity.TOP | Gravity.START;
                lp.x = (int) targetX + horizontalMargin;
                lp.y = (int) (targetY + target.getHeight() + verticalMargin);
                break;
            case TipOptions.ALIGN_RIGHT_BELOW:
                lp.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, lp){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        int delta = target.getWidth() - tipView.getWidth();
                        layoutParams.x = (int) (targetX + delta + horizontalMargin);
                    }
                }.schedule();
                lp.y = (int) (targetY + target.getHeight() + verticalMargin);
                break;
            case TipOptions.ALIGN_CENTER_BELOW:
                lp.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, lp){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        int delta = (target.getWidth() - tipView.getWidth()) / 2;
                        layoutParams.x = (int) (targetX + delta + horizontalMargin);
                    }
                }.schedule();
                lp.y = (int) (targetY + target.getHeight() + verticalMargin);
                break;
            case TipOptions.ALIGN_LEFT_ABOVE:
                lp.gravity = Gravity.TOP | Gravity.START;
                lp.x = (int) (targetX + horizontalMargin);
                new LayoutAdjuster(target, lp){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        layoutParams.y = (int) (targetY - tipView.getHeight() - verticalMargin);
                    }
                }.schedule();
                break;
            case TipOptions.ALIGN_RIGHT_ABOVE:
                lp.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, lp){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        int delta = target.getWidth() - tipView.getWidth();
                        layoutParams.x = (int) (targetX + delta + horizontalMargin);
                        layoutParams.y = (int) (targetY - tipView.getHeight() - verticalMargin);
                    }
                }.schedule();
                break;
            case TipOptions.ALIGN_CENTER_ABOVE:
                lp.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, lp){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        int delta = (target.getWidth() - tipView.getWidth()) / 2;
                        layoutParams.x = (int) (targetX + delta + horizontalMargin);
                        layoutParams.y = (int) (targetY - tipView.getHeight() - verticalMargin);
                    }
                }.schedule();
                break;
            case TipOptions.ALIGN_LEFT:
                lp.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, lp){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        layoutParams.x = (int) (targetX - tipView.getWidth() - horizontalMargin);
                        int delta = (target.getHeight() - tipView.getHeight()) / 2;
                        layoutParams.y = (int) (targetY + delta + verticalMargin);
                    }
                }.schedule();
                break;
            case TipOptions.ALIGN_RIGHT:
                lp.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, lp){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        layoutParams.x = (int) (targetX + target.getWidth() + horizontalMargin);
                        int delta = (target.getHeight() - tipView.getHeight()) / 2;
                        layoutParams.y = (int) (targetY + delta + verticalMargin);
                    }
                }.schedule();
        }

        return lp;
    }

    private void notifyTipChanged(int index, View target){
        if(onTipChangeListener != null){
            onTipChangeListener.onTipChanged(index, target);
        }
    }

    public void close(){
        if(isShown()){
            WindowManager windowManager = activity.getWindowManager();
            windowManager.removeView(currentTipView);
            notifyClosed(true);
            reset();
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
                throw new IllegalStateException("Default options must be set");
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
                throw new IllegalStateException("At least one tip must be specified.");
            }
            return new AppTips(this);
        }

        public AppTips show() {
            AppTips appTips = build();
            appTips.show();
            return appTips;
        }
    }

    private abstract class LayoutAdjuster implements ViewTreeObserver.OnGlobalLayoutListener{

        private final View target;
        private final WindowManager.LayoutParams layoutParams;

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

        void schedule(){
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
