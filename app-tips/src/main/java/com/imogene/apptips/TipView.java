package com.imogene.apptips;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;

/**
 * Created by Admin on 25.04.2016.
 */
class TipView extends AppCompatTextView {

    static final int MODE_BELOW_TARGET = 0;
    static final int MODE_ABOVE_TARGET = 1;
    static final int MODE_TO_LEFT_TARGET = 2;
    static final int MODE_TO_RIGHT_TARGET = 3;

    private final int pointerSize;
    private float pointerPosition = 0.5f;
    private int mode;
    private final Paint paint;
    private final Path path;
    private final ShapeDrawable drawable;

    private final PointF A;
    private final PointF B;
    private final PointF C;

    TipView(Context context) {
        super(context);
        Resources resources = getResources();

        pointerSize = resources.getDimensionPixelSize(R.dimen.tip_view_pointer_size);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        A = new PointF();
        B = new PointF();
        C = new PointF();

        float radius = resources.getDimensionPixelSize(R.dimen.tip_view_corner_radius);
        float[] radii = new float[]{
                radius, radius, radius, radius,
                radius, radius, radius, radius
        };
        Shape shape = new RoundRectShape(radii, null, null);
        drawable = new ShapeDrawable(shape);

        setGravity(Gravity.CENTER);
    }

    void setColor(int color) {
        paint.setColor(color);
        drawable.getPaint().setColor(color);
        invalidate();
    }

    void setMode(int mode) {
        if(mode < MODE_BELOW_TARGET || mode > MODE_TO_RIGHT_TARGET){
            throw new IllegalArgumentException("Unsupported mode: " + mode);
        }
        this.mode = mode;
        invalidate();
        requestLayout();
    }

    void setPointerPosition(float position){
        pointerPosition = clamp(position, .1f, .9f);
        invalidate();
    }

    @SuppressWarnings("SameParameterValue")
    private float clamp(float value, float min, float max){
        return Math.max(min, Math.min(max, value));
    }

    void setPadding(int padding){
        int left = padding, top = padding, right = padding, bottom = padding;
        switch (mode){
            case MODE_BELOW_TARGET:
                top += pointerSize;
                break;
            case MODE_ABOVE_TARGET:
                bottom += pointerSize;
                break;
            case MODE_TO_LEFT_TARGET:
                right += pointerSize;
                break;
            case MODE_TO_RIGHT_TARGET:
                left += pointerSize;
                break;
        }
        setPadding(left, top, right, bottom);
    }

    @Override
    public void setMinHeight(int minHeight) {
        if(mode == MODE_ABOVE_TARGET || mode == MODE_BELOW_TARGET){
            minHeight += pointerSize;
        }
        super.setMinHeight(minHeight);
    }

    @Override
    public void setMinWidth(int minWidth) {
        if(mode == MODE_TO_LEFT_TARGET || mode == MODE_TO_RIGHT_TARGET){
            minWidth += pointerSize;
        }
        super.setMinWidth(minWidth);
    }

    @Override
    public boolean performClick() {
        // override this method in order to
        // disable IDE warnings about it.
        return super.performClick();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float wh = w * pointerPosition;
        float hh = h * pointerPosition;
        float sin60 = Util.sinDegrees(60);
        float bh = pointerSize / (2 * sin60);

        float bxv = wh - bh;
        float cxv = wh + bh;

        float byh = hh - bh;
        float cyh = hh + bh;

        if(mode == MODE_BELOW_TARGET){
            A.set(wh, 0);
            B.set(bxv, pointerSize);
            C.set(cxv, pointerSize);
            drawable.setBounds(0, pointerSize, w, h);
        }else if (mode == MODE_ABOVE_TARGET){
            A.set(wh, h);
            B.set(bxv, h - pointerSize);
            C.set(cxv, h - pointerSize);
            drawable.setBounds(0, 0, w, h - pointerSize);
        }else if(mode == MODE_TO_LEFT_TARGET){
            A.set(w, hh);
            B.set(w - pointerSize, byh);
            C.set(w - pointerSize, cyh);
            drawable.setBounds(0, 0, w - pointerSize, h);
        }else if(mode == MODE_TO_RIGHT_TARGET){
            A.set(0, hh);
            B.set(pointerSize, byh);
            C.set(pointerSize, cyh);
            drawable.setBounds(pointerSize, 0, w, h);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if(mode == MODE_ABOVE_TARGET || mode == MODE_BELOW_TARGET){
            height += pointerSize;
        } else {
            width += pointerSize;
        }

        int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.UNSPECIFIED);
        int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED);
        super.onMeasure(widthSpec, heightSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        path.moveTo(B.x, B.y);
        path.lineTo(A.x, A.y);
        path.lineTo(C.x, C.y);
        path.close();
        canvas.drawPath(path, paint);
        drawable.draw(canvas);
        super.onDraw(canvas);
    }
}