package com.rzm.downloadmodle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class RingProgress extends View {

    private float paintStrokeWidth;

    public RingProgress(Context context) {
        super(context, null);
    }

    public RingProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private Paint redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint bluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint grayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private void init() {
        paintStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());

        grayPaint.setColor(Color.GRAY);
        grayPaint.setStyle(Paint.Style.STROKE);
        grayPaint.setStrokeWidth(paintStrokeWidth);

        redPaint.setColor(Color.RED);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeWidth(paintStrokeWidth);

        bluePaint.setColor(Color.BLUE);
        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics());
        bluePaint.setTextSize(textSize);
    }

    float percent = 0;

    public void setPercent(float percent) {
        this.percent = percent;
        text = String.format("%.1f%%", percent * 100);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        computeValues();
        drawGrayRing(canvas);
        drawRedArc(canvas);
        drawBlueText(canvas);
    }

    RectF arcRectF = new RectF();
    PointF center = new PointF();
    float radius;

    private void computeValues() {
        center.x = getWidth() / 2;
        center.y = getHeight() / 2;
        radius = Math.min(getWidth(), getHeight()) / 2 - paintStrokeWidth / 2;
        arcRectF.set(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
    }

    private void drawGrayRing(Canvas canvas) {
        canvas.drawCircle(center.x, center.y, radius, grayPaint);
    }

    private void drawRedArc(Canvas canvas) {
        /**
         * 360度进制， 0度是3点钟方向， 顺时针是正的
         * @param oval          弧的外切矩形
         * @param startAngle    弧的起始角度
         * @param sweepAngle    弧扫过的角度
         * @param useCenter     如果是true就会把弧的两端与圆心相连，如果是false只画弧
         * @param paint         画笔
         */
        canvas.drawArc(arcRectF, -90, percent * 360, false, redPaint);
    }

    private void drawBlueText(Canvas canvas) {
        Rect textBounds = new Rect();
        bluePaint.getTextBounds(text, 0, text.length(), textBounds);

        canvas.drawText(text, center.x - textBounds.width() / 2, center.y + textBounds.height() / 2, bluePaint);
    }

    private String text="";

    public void setText(String text) {
        this.text = text;
        invalidate();
    }
}
