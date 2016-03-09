package com.qikoo.sportscamera.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SearchRotateView extends ImageView {


    private final Paint paint;
    private final Context context;

    public SearchRotateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setStyle(Style.STROKE);
    }

    int startAngle = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        int center = getMeasuredWidth() / 2;

        int innerCircle = dip2px(context, 50); // ������Բ�뾶
        int ringWidth = dip2px(context, 1); // ����Բ�����
        int penWidth = dip2px(context, 3); // ���ñ�ˢ���

        // ������Բ
//        this.paint.setColor(android.graphics.Color.WHITE);
//        this.paint.setStrokeWidth(1);
//        canvas.drawCircle(center, center, innerCircle, this.paint);

        // ����Բ��
        // this.paint.setARGB(255, 212, 225, 233);
        this.paint.setColor(android.graphics.Color.WHITE);
        this.paint.setStrokeWidth(ringWidth);
//        canvas.drawCircle(center, center, innerCircle + 1 + ringWidth / 2,
//                this.paint);
        canvas.drawCircle(center, center, innerCircle, paint);

        // ������Բ
//        this.paint.setColor(android.graphics.Color.WHITE);
//        this.paint.setStrokeWidth(1);
//        canvas.drawCircle(center, center, innerCircle + ringWidth, this.paint);

        RectF rect2 = new RectF(center - (innerCircle + 1 + ringWidth / 2),
                center - (innerCircle + 1 + ringWidth / 2), center
                        + (innerCircle + 1 + ringWidth / 2), center
                        + (innerCircle + 1 + ringWidth / 2));

        // this.paint.setARGB(30, 127, 255, 212);
        this.paint.setAntiAlias(true);  
        this.paint.setStrokeCap(Cap.ROUND);
        this.paint.setColor(android.graphics.Color.WHITE);
        this.paint.setStrokeWidth(penWidth);
        this.paint.setDither(true);
        // ���Ʋ�͸������
        // canvas.drawArc(rect2, 180 + startAngle, 30, false, paint);
        canvas.drawArc(rect2, 0 + startAngle,(float) 0.5, false, paint);
        // ����͸������
        // this.paint.setARGB(30, 127, 255, 212);
        // this.paint.setColor(android.graphics.Color.GRAY);
        // canvas.drawArc(rect2, 90 + startAngle, 30, false, paint);
        // canvas.drawArc(rect2, 270 + startAngle, 30, false, paint);

        startAngle += 3;
        if (startAngle == 360)
            startAngle = 0;
        super.onDraw(canvas);
        if (isStart) {
            invalidate();
        }
    }

    private boolean isStart = true;
    Object mObject = new Object();

    public void Start() {
        synchronized (mObject) {
            isStart = true;
            invalidate();
        }
    }

    public void Stop() {
        synchronized (mObject) {
            isStart = false;
            invalidate();
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
