package com.faheem.anayapaint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.MotionEvent;
import android.graphics.Path;
import java.util.ArrayList;
import java.util.List;

public class MyCanvasView extends View {
    private float currentBrushSize = 10f; // default
    private Paint paint;
    private int currentColor = Color.BLUE;  // default color

    // Each stroke is a Path + color
    private class Stroke {
        Path path;
        int color;
        float strokeWidth;

        Stroke(Path path, int color, float strokeWidth) {
            this.path = path;
            this.color = color;
            this.strokeWidth = strokeWidth;
        }
    }

    private List<Stroke> strokes;
    private Path currentPath;

    public MyCanvasView(Context context) {
        super(context);

        paint = new Paint();
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        strokes = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Stroke stroke : strokes) {
            paint.setColor(stroke.color);
            paint.setStrokeWidth(stroke.strokeWidth);
            canvas.drawPath(stroke.path, paint);
        }

// Draw current path
        if (currentPath != null) {
            paint.setColor(currentColor);
            paint.setStrokeWidth(currentBrushSize);
            canvas.drawPath(currentPath, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new Path();
                currentPath.moveTo(x, y);
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (currentPath != null) {
                    currentPath.lineTo(x, y);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (currentPath != null) {
                    strokes.add(new Stroke(currentPath, currentColor, currentBrushSize));
                    currentPath = null;
                    invalidate();
                }
                break;
        }

        return true;
    }

    // Change color
    public void setColor(int color) {
        currentColor = color;
    }

    // Clear canvas
    public void clearCanvas() {
        strokes.clear();
        currentPath = null;
        invalidate();
    }
    public void setBrushSize(float size) {
        currentBrushSize = size;
    }
}
