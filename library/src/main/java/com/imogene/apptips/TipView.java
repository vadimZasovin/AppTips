package com.imogene.apptips;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Created by Admin on 25.04.2016.
 */
public class TipView extends TextView {

    public static final int MODE_BELOW_TARGET = 0;
    public static final int MODE_ABOVE_TARGET = 1;
    public static final int MODE_TO_LEFT_TARGET = 2;
    public static final int MODE_TO_RIGHT_TARGET = 3;

    private static final int POINTER_SIZE_DP = 12;
    private static final int PADDING_DP = 4;

    private int mPointerSize;
    private float mPointerPosition = 0.5f;
    private int mMode;
    private Paint mPaint;
    private Path mPath;
    private ShapeDrawable mShapeDrawable;

    private PointF A;
    private PointF B;
    private PointF C;

    public TipView(Context context) {
        this(context, null);
    }

    public TipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPointerSize = Util.convertDpInPixels(context, POINTER_SIZE_DP);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPath = new Path();
        mPath.setFillType(Path.FillType.EVEN_ODD);
        float cornerRadius = Util.convertDpInPixels(context, PADDING_DP);
        mShapeDrawable = new ShapeDrawable(new RoundRectShape(
                new float[]{
                        cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                        cornerRadius, cornerRadius, cornerRadius, cornerRadius},
                null, null));
        mShapeDrawable.getPaint().set(mPaint);

        A = new PointF();
        B = new PointF();
        C = new PointF();

        setGravity(Gravity.CENTER);
        setPadding(0, 0, 0, 0);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        mShapeDrawable.getPaint().set(mPaint);
        invalidate();
    }

    public void setMode(int mode) {
        if(mode < MODE_BELOW_TARGET || mode > MODE_TO_RIGHT_TARGET){
            throw new IllegalArgumentException("Unsupported mode constant");
        }
        mMode = mode;
        invalidate();
        requestLayout();
    }

    public void setPointerPosition(float position){
        if(position < 0.1){
            mPointerPosition = 0.1f;
        }else if(position > 0.9){
            mPointerPosition = 0.9f;
        }else {
            mPointerPosition = position;
        }
        invalidate();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        switch (mMode){
            case MODE_BELOW_TARGET:
                super.setPadding(left, top + mPointerSize, right, bottom);
                break;
            case MODE_ABOVE_TARGET:
                super.setPadding(left, top, right, bottom + mPointerSize);
                break;
            case MODE_TO_LEFT_TARGET:
                super.setPadding(left, top, right + mPointerSize, bottom);
                break;
            case MODE_TO_RIGHT_TARGET:
                super.setPadding(left + mPointerSize, top, right, bottom);
                break;
        }
    }

    @Override
    public void setMinHeight(int minHeight) {
        if(mMode == MODE_ABOVE_TARGET || mMode == MODE_BELOW_TARGET){
            minHeight += mPointerSize;
        }
        super.setMinHeight(minHeight);
    }

    @Override
    public void setMinWidth(int minWidth) {
        if(mMode == MODE_TO_LEFT_TARGET || mMode == MODE_TO_RIGHT_TARGET){
            minWidth += mPointerSize;
        }
        super.setMinWidth(minWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPath.moveTo(B.x, B.y);
        mPath.lineTo(A.x, A.y);
        mPath.lineTo(C.x, C.y);
        mPath.close();
        canvas.drawPath(mPath, mPaint);
        mShapeDrawable.draw(canvas);
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if(mMode == MODE_ABOVE_TARGET || mMode == MODE_BELOW_TARGET){
            height += mPointerSize;
        }else if(mMode == MODE_TO_LEFT_TARGET || mMode == MODE_TO_RIGHT_TARGET){
            width += mPointerSize;
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float wh = w * mPointerPosition;
        float hh = h * mPointerPosition;
        float bh = mPointerSize / (2 * Util.sinDegrees(60));

        float bxv = wh - bh;
        float cxv = wh + bh;

        float byh = hh - bh;
        float cyh = hh + bh;

        if(mMode == MODE_BELOW_TARGET){
            A.set(wh, 0);
            B.set(bxv, mPointerSize);
            C.set(cxv, mPointerSize);
            mShapeDrawable.setBounds(0, mPointerSize, w, h);
        }else if (mMode == MODE_ABOVE_TARGET){
            A.set(wh, h);
            B.set(bxv, h - mPointerSize);
            C.set(cxv, h - mPointerSize);
            mShapeDrawable.setBounds(0, 0, w, h - mPointerSize);
        }else if(mMode == MODE_TO_LEFT_TARGET){
            A.set(w, hh);
            B.set(w - mPointerSize, byh);
            C.set(w - mPointerSize, cyh);
            mShapeDrawable.setBounds(0, 0, w - mPointerSize, h);
        }else if(mMode == MODE_TO_RIGHT_TARGET){
            A.set(0, hh);
            B.set(mPointerSize, byh);
            C.set(mPointerSize, cyh);
            mShapeDrawable.setBounds(mPointerSize, 0, w, h);
        }

    }
}
