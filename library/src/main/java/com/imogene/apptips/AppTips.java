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

    private Builder mBuilder;
    private Activity mActivity;
    private View.OnTouchListener mOnTouchListener;
    private boolean mShown;
    private int mCurrentTip;
    private TipView mCurrentTipView;

    OnShowListener mOnShowListener;
    OnCloseListener mOnCloseListener;
    OnTipChangeListener mOnTipChangeListener;

    AppTips(Builder builder){
        mBuilder = builder;
    }

    public void show(Activity activity){
        if(mShown || mCurrentTip != 0 || mBuilder.mTips == null || mBuilder.mTips.size() == 0){
            return;
        }
        mActivity = activity;
        mOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean handled = false;
                if(event.getActionMasked() == MotionEvent.ACTION_OUTSIDE){
                    showTip(++mCurrentTip);
                    handled = true;
                }
                return handled;
            }
        };
        showTip(0);
        mShown = true;
        if(mOnShowListener != null){
            mOnShowListener.onShow();
        }
    }

    private void showTip(int tipIndex){
        WindowManager windowManager = mActivity.getWindowManager();

        if(tipIndex != 0){
            windowManager.removeView(mCurrentTipView);
        }

        if(tipIndex == mBuilder.mTips.size()){
            if(mOnCloseListener != null){
                mOnCloseListener.onClose(false);
            }

            mShown = false;
            mCurrentTip = 0;
            mCurrentTipView = null;
            // AppTips object is prepared for the next showing
            return;
        }

        TipOptions options = mBuilder.mTips.get(tipIndex);

        mCurrentTipView = new TipView(mActivity);
        mCurrentTipView.setColor(options.mColor);
        mCurrentTipView.setTextColor(options.mTextColor);
        mCurrentTipView.setOnTouchListener(mOnTouchListener);
        mCurrentTipView.setText(options.mText);
        int align = options.mAlign;
        if(align == TipOptions.ALIGN_CENTER_ABOVE ||
                align == TipOptions.ALIGN_LEFT_ABOVE ||
                align == TipOptions.ALIGN_RIGHT_ABOVE){
            mCurrentTipView.setMode(TipView.MODE_ABOVE_TARGET);
        }else if(align == TipOptions.ALIGN_LEFT){
            mCurrentTipView.setMode(TipView.MODE_TO_LEFT_TARGET);
        }else if(align == TipOptions.ALIGN_RIGHT){
            mCurrentTipView.setMode(TipView.MODE_TO_RIGHT_TARGET);
        }else {
            mCurrentTipView.setMode(TipView.MODE_BELOW_TARGET);
        }
        int padding = options.mPadding;
        mCurrentTipView.setPadding(padding, padding, padding, padding);
        mCurrentTipView.setMinWidth(options.mMinWidth);
        mCurrentTipView.setMaxWidth(options.mMaxWidth);
        mCurrentTipView.setMinHeight(options.mMinHeight);
        mCurrentTipView.setPointerPosition(options.mPointerPosition);

        View target = mActivity.findViewById(options.mViewId);

        WindowManager.LayoutParams layoutParams =
                generateLayoutParams(mCurrentTipView, target, options);
        windowManager.addView(mCurrentTipView, layoutParams);

        if(mOnTipChangeListener != null){
            mOnTipChangeListener.onTipChanged(tipIndex, target);
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
        float dimAmount = mBuilder.mDimAmount;
        if(dimAmount > 0){
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            layoutParams.dimAmount = dimAmount;
        }

        // calculate position for the window
        final float targetX = target.getX();
        final float targetY = target.getY();

        switch (options.mAlign){
            case TipOptions.ALIGN_LEFT_BELOW:
                layoutParams.gravity = Gravity.TOP | Gravity.START;
                layoutParams.x = (int) targetX;
                layoutParams.y = (int) (targetY + target.getHeight());
                break;
            case TipOptions.ALIGN_RIGHT_BELOW:
                layoutParams.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, layoutParams){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        int delta = target.getWidth() - tipView.getWidth();
                        layoutParams.x = (int) (targetX + delta);
                    }
                }.scheduleAdjusting();
                layoutParams.y = (int) (targetY + target.getHeight());
                break;
            case TipOptions.ALIGN_CENTER_BELOW:
                layoutParams.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, layoutParams){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        int delta = (target.getWidth() - tipView.getWidth()) / 2;
                        layoutParams.x = (int) (targetX + delta);
                    }
                }.scheduleAdjusting();
                layoutParams.y = (int) (targetY + target.getHeight());
                break;
            case TipOptions.ALIGN_LEFT_ABOVE:
                layoutParams.gravity = Gravity.BOTTOM | Gravity.START;
                layoutParams.x = (int) targetX;
                layoutParams.y = (int) (targetY + target.getHeight());
                break;
            case TipOptions.ALIGN_RIGHT_ABOVE:
                layoutParams.gravity = Gravity.BOTTOM | Gravity.START;
                new LayoutAdjuster(target, layoutParams){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        int delta = target.getWidth() - tipView.getWidth();
                        layoutParams.x = (int) (targetX + delta);
                    }
                }.scheduleAdjusting();
                layoutParams.y = (int) (targetY + target.getHeight());
                break;
            case TipOptions.ALIGN_CENTER_ABOVE:
                layoutParams.gravity = Gravity.BOTTOM | Gravity.START;
                new LayoutAdjuster(target, layoutParams){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        int delta = (target.getWidth() - tipView.getWidth()) / 2;
                        layoutParams.x = (int) (targetX + delta);
                    }
                }.scheduleAdjusting();
                layoutParams.y = (int) (targetY + target.getHeight());
                break;
            case TipOptions.ALIGN_LEFT:
                layoutParams.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, layoutParams){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        layoutParams.x = (int) (targetX - tipView.getWidth());
                        int delta = target.getHeight() - tipView.getHeight();
                        layoutParams.y = (int) (targetY + delta);
                    }
                }.scheduleAdjusting();
                break;
            case TipOptions.ALIGN_RIGHT:
                layoutParams.gravity = Gravity.TOP | Gravity.START;
                new LayoutAdjuster(target, layoutParams){
                    @Override
                    void onAdjustLayout(View target, WindowManager.LayoutParams layoutParams) {
                        layoutParams.x = (int) (targetX + target.getWidth());
                        int delta = target.getHeight() - tipView.getHeight();
                        layoutParams.y = (int) (targetY + delta);
                    }
                }.scheduleAdjusting();
        }

        return layoutParams;
    }

    public void close(){
        if(mCurrentTipView != null){
            WindowManager windowManager = mActivity.getWindowManager();
            windowManager.removeView(mCurrentTipView);

            if(mOnCloseListener != null) {
                mOnCloseListener.onClose(true);
            }

            mShown = false;
            mCurrentTip = 0;
            mCurrentTipView = null;
        }
    }

    public void setOnShowListener(OnShowListener listener) {
        mOnShowListener = listener;
    }

    public void setOnCloseListener(OnCloseListener listener) {
        mOnCloseListener = listener;
    }

    public void setOnChangeTipListener(OnTipChangeListener listener) {
        mOnTipChangeListener = listener;
    }

    public static class Builder implements IBuilder {

        List<TipOptions> mTips;
        TipOptions mDefaultOptions;
        float mDimAmount = -1f;

        public Builder(){
            mTips = new ArrayList<>();
        }

        @Override
        public IBuilder setDimAmount(float dimAmount) {
            mDimAmount = dimAmount;
            return this;
        }

        @Override
        public IBuilder addTip(TipOptions options) {
            mTips.add(options);
            return this;
        }

        @Override
        public IBuilder addTip(int viewId, CharSequence text) {
            if(mDefaultOptions == null){
                throw new IllegalStateException("This method can be used only if the" +
                        "default TipOptions has been set.");
            }

            TipOptions options = TipOptions.from(mDefaultOptions);
            options.mViewId = viewId;
            options.mText = text;
            mTips.add(options);
            return this;
        }

        @Override
        public IBuilder setDefaultOptions(TipOptions options) {
            mDefaultOptions = options;
            return this;
        }

        @Override
        public AppTips build() {
            return new AppTips(this);
        }

        @Override
        public AppTips show(Activity activity) {
            AppTips appTips = new AppTips(this);
            appTips.show(activity);
            return appTips;
        }
    }

    private abstract class LayoutAdjuster implements ViewTreeObserver.OnGlobalLayoutListener{

        private View mTarget;
        private WindowManager.LayoutParams mLayoutParams;

        LayoutAdjuster(View target, WindowManager.LayoutParams layoutParams){
            mTarget = target;
            mLayoutParams = layoutParams;
        }

        @Override
        public void onGlobalLayout() {
            onAdjustLayout(mTarget, mLayoutParams);
            mActivity.getWindowManager().updateViewLayout(mCurrentTipView, mLayoutParams);
            mCurrentTipView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }

        void scheduleAdjusting(){
            mCurrentTipView.getViewTreeObserver().addOnGlobalLayoutListener(this);
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
