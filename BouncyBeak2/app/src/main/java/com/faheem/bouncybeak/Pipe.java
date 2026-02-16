package com.faheem.bouncybeak;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.Random;

public class Pipe {

    private int x;
    private int width;
    private int topHeight;
    private static final int COLLISION_PADDING = 25;

    private static final int GAP = 350;
    private static final int MIN_PIPE_HEIGHT = 150;

    private int screenHeight;
    private int speed = 8;

    private Bitmap bitmap;
    private boolean passed = false;

    public Pipe(int startX, int screenHeight, Bitmap bitmap) {
        this.screenHeight = screenHeight;
        this.bitmap = bitmap;
        this.width = bitmap.getWidth();
        this.x = startX;

        Random random = new Random();

        int maxTopHeight =
                screenHeight - GAP - MIN_PIPE_HEIGHT;

        if (maxTopHeight < MIN_PIPE_HEIGHT) {
            maxTopHeight = MIN_PIPE_HEIGHT + 1;
        }

        topHeight =
                MIN_PIPE_HEIGHT +
                        random.nextInt(maxTopHeight - MIN_PIPE_HEIGHT);
    }

    public void update() {
        x -= speed;
    }

    public void draw(Canvas canvas) {
        // Top pipe
        canvas.drawBitmap(bitmap, null,
                new Rect(x, 0, x + width, topHeight),
                null);

        // Bottom pipe
        canvas.drawBitmap(bitmap, null,
                new Rect(x, topHeight + GAP, x + width, screenHeight),
                null);
    }

    public boolean isOffScreen() {
        return x + width < 0;
    }

    public Rect getTopRect() {
        return new Rect(
                x + COLLISION_PADDING,
                0,
                x + width - COLLISION_PADDING,
                topHeight - COLLISION_PADDING
        );
    }

    public Rect getBottomRect() {
        return new Rect(
                x + COLLISION_PADDING,
                topHeight + GAP + COLLISION_PADDING,
                x + width - COLLISION_PADDING,
                screenHeight
        );
    }


    public int getX() { return x; }
    public int getWidth() { return width; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
}
