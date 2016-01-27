package io.ipoli.android.app.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ReplacementSpan;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/27/16.
 */
public class ImageWithBackgroundColorSpan extends ReplacementSpan {

    private final Drawable drawable;
    private final Paint backgroundPaint;

    public ImageWithBackgroundColorSpan(Drawable drawable, int backgroundColor) {
        this.drawable = drawable;
        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        int textWidth = (int) Math.ceil(paint.measureText(text, start, end));
        int drawableWidth = drawable.getIntrinsicWidth();
        return Math.max(textWidth, drawableWidth);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        canvas.drawRect((int) x, top, (int) x + drawable.getIntrinsicWidth(), bottom, backgroundPaint);

        // set bounds relative to the upper left corner

        int width = (int) (drawable.getIntrinsicWidth() / 1.25f);
        int startX = getStartX(x, width);
        int height = (int) (drawable.getIntrinsicHeight() / 1.25f);
        int startY = getStartY(top, height);
        drawable.setBounds(startX, startY, startX + width, startY + height);
        drawable.draw(canvas);

    }

    private int getStartY(int top, int height) {
        int heightEmptySpace = drawable.getIntrinsicHeight() - height;
        return top + heightEmptySpace / 2;
    }

    private int getStartX(float x, int width) {
        int widthEmptySpace = drawable.getIntrinsicWidth() - width;
        return (int) (x + widthEmptySpace / 2);
    }
}
