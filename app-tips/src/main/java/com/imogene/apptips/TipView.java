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

    private static final float MIN_POINTER_POSITION = 0.1F;
    private static final float MAX_POINTER_POSITION = 0.9F;

    private final int pointerSize;
    private float pointerPosition = 0.5F;
    private int pointerOffset = 0;
    private float pointerProtrusion = 1F;
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
        requestLayout();
    }

    void setPointerPosition(float position){
        pointerPosition = clampPointerPosition(position);
        pointerOffset = 0;
        updatePointerPathPoints();
        invalidate();
    }

    void setPointerOffset(int offset){
        pointerOffset = offset;
        updatePointerPathPoints();
        invalidate();
    }

    private float clampPointerPosition(float position){
        return clamp(position, MIN_POINTER_POSITION, MAX_POINTER_POSITION);
    }

    private float clamp(float value, float min, float max){
        return Math.max(min, Math.min(max, value));
    }

    private void updatePointerPathPoints(){
        int width = getWidth();
        int height = getHeight();
        updatePointerPathPoints(width, height);
    }

    private void updatePointerPathPoints(int viewWidth, int viewHeight){
        final float dynamicSize = pointerSize * pointerProtrusion;
        final float protrusion = pointerSize - dynamicSize;
        final float wh, hh, bh;

        if(pointerOffset != 0){
            wh = viewWidth / 2 + pointerOffset;
            hh = viewHeight / 2 + pointerOffset;
        } else {
            wh = viewWidth * pointerPosition;
            hh = viewHeight * pointerPosition;
        }
        final float sin60 = Util.sinDegrees(60);
        bh = dynamicSize / (2 * sin60);

        final float bx = wh - bh;
        final float cx = wh + bh;
        final float by = hh - bh;
        final float cy = hh + bh;

        switch (mode){
            case MODE_BELOW_TARGET:
                A.set(wh, protrusion);
                B.set(bx, pointerSize);
                C.set(cx, pointerSize);
                break;
            case MODE_ABOVE_TARGET:
                A.set(wh, viewHeight - protrusion);
                B.set(bx, viewHeight - pointerSize);
                C.set(cx, viewHeight - pointerSize);
                break;
            case MODE_TO_LEFT_TARGET:
                A.set(viewWidth - protrusion, hh);
                B.set(viewWidth - pointerSize, by);
                C.set(viewWidth - pointerSize, cy);
                break;
            case MODE_TO_RIGHT_TARGET:
                A.set(protrusion, hh);
                B.set(pointerSize, by);
                C.set(pointerSize, cy);
                break;
        }
    }

    float getPointerPosition(){
        if(pointerOffset != 0){
            final float position;
            if(isHorizontalMode()){
                final float width = getWidth();
                position = (width / 2 + pointerOffset) / width;
            } else {
                final float height = getHeight();
                position = (height / 2 + pointerOffset) / height;
            }
            return clampPointerPosition(position);
        }
        return pointerPosition;
    }

    void setPointerProtrusion(float protrusion){
        pointerProtrusion = protrusion;
        updatePointerPathPoints();
        invalidate();
    }

    private boolean isHorizontalMode(){
        return mode == MODE_ABOVE_TARGET || mode == MODE_BELOW_TARGET;
    }

    private boolean isVerticalMode(){
        return !isHorizontalMode();
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
        if(isHorizontalMode()){
            minHeight += pointerSize;
        }
        super.setMinHeight(minHeight);
    }

    @Override
    public void setMinWidth(int minWidth) {
        if(isVerticalMode()){
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
        updatePointerPathPoints(w, h);
        updateShapeBounds(w, h);
    }

    private void updateShapeBounds(int viewWidth, int viewHeight){
        final int left, top, right, bottom;
        switch (mode){
            case MODE_BELOW_TARGET:
                left = 0; top = pointerSize;
                right = viewWidth; bottom = viewHeight;
                break;
            case MODE_ABOVE_TARGET:
                left = 0; top = 0;
                right = viewWidth; bottom = viewHeight - pointerSize;
                break;
            case MODE_TO_LEFT_TARGET:
                left = 0; top = 0;
                right = viewWidth - pointerSize; bottom = viewHeight;
                break;
            default:
                left = pointerSize; top = 0;
                right = viewWidth; bottom = viewHeight;
                break;
        }
        drawable.setBounds(left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if(isHorizontalMode()){
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
        path.rewind();
        path.moveTo(B.x, B.y);
        path.lineTo(A.x, A.y);
        path.lineTo(C.x, C.y);
        path.close();
        canvas.drawPath(path, paint);
        drawable.draw(canvas);
        super.onDraw(canvas);
    }
}