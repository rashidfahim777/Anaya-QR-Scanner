package com.faheem.bouncybeak;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Bird {

    private Bitmap bitmap;
    private int x, y;
    private int velocity = 0;

    private final int gravity = 2;
    private int width, height;

    public Bird(Bitmap bitmap, int x, int y) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
    }

    public void update() {
        velocity += gravity;
        y += velocity;
    }

    public void jump() {
        velocity = -25;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap,
                x - width / 2f,
                y - height / 2f,
                null);
    }

    public Rect getRect() {
        int padding = 15;
        return new Rect(
                x - width / 2 + padding,
                y - height / 2 + padding,
                x + width / 2 - padding,
                y + height / 2 - padding
        );
    }


    public void reset(int x, int y) {
        this.x = x;
        this.y = y;
        velocity = 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
