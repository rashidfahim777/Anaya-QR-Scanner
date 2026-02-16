package com.faheem.bouncybeak;

import android.content.Context;
import android.graphics.*;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;
    private Bird bird;
    private List<Pipe> pipes;
    private long lastPipeSpawnTime = 0;
    private static final long PIPE_SPAWN_INTERVAL = 1800; // ms
    private int screenWidth, screenHeight;
    private boolean gameOver = false;
    private int score = 0;

    private Paint textPaint;
    private SoundPool soundPool;
    private int flapSound, hitSound, scoreSound;

    private static final int PIPE_INTERVAL = 600;
    private static final long PIPE_DELAY = 1500;

    private long gameStartTime;

    private Bitmap pipeBitmap;
    private Bitmap birdBitmap;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setFocusable(true);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(80);

        soundPool = new SoundPool.Builder().setMaxStreams(3).build();
        flapSound = soundPool.load(context, R.raw.flap, 1);
        hitSound = soundPool.load(context, R.raw.hit, 1);
        scoreSound = soundPool.load(context, R.raw.score, 1);
    }

    private void initGame() {
        gameStartTime = System.currentTimeMillis();
        gameOver = false;
        score = 0;
        lastPipeSpawnTime = 0;
        birdBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bird);
        pipeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pipe);

        bird = new Bird(birdBitmap, 300, screenHeight / 2);
        pipes = new ArrayList<>();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenWidth = getWidth();
        screenHeight = getHeight();

        initGame();

        gameThread = new GameThread(holder, this);
        gameThread.setRunning(true);
        gameThread.start();
    }

    public void update() {
        if (gameOver) return;

        bird.update();

        long elapsed = System.currentTimeMillis() - gameStartTime;

        // Spawn pipes ONLY after delay
        if (elapsed > PIPE_DELAY) {
            long now = System.currentTimeMillis();

            if (!gameOver && now - gameStartTime > PIPE_DELAY) {

                if (now - lastPipeSpawnTime > PIPE_SPAWN_INTERVAL) {

                    pipes.add(new Pipe(
                            screenWidth + 200,
                            screenHeight,
                            pipeBitmap
                    ));

                    lastPipeSpawnTime = now;
                }
            }
        }

        Iterator<Pipe> iterator = pipes.iterator();
        while (iterator.hasNext()) {
            Pipe pipe = iterator.next();
            pipe.update();

            if (!pipe.isPassed() &&
                    pipe.getX() + pipe.getWidth() < bird.getX()) {
                pipe.setPassed(true);
                score++;
                soundPool.play(scoreSound, 1, 1, 1, 0, 1);
            }

            if (pipe.isOffScreen()) {
                iterator.remove();
            }

            if (Rect.intersects(bird.getRect(), pipe.getTopRect()) ||
                    Rect.intersects(bird.getRect(), pipe.getBottomRect())) {
                gameOver = true;
                soundPool.play(hitSound, 1, 1, 1, 0, 1);
            }
        }

        if (bird.getY() < 0 || bird.getY() > screenHeight) {
            gameOver = true;
            soundPool.play(hitSound, 1, 1, 1, 0, 1);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas == null) return;

        canvas.drawColor(Color.CYAN);

        bird.draw(canvas);

        for (Pipe pipe : pipes) {
            pipe.draw(canvas);
        }

        canvas.drawText("Score: " + score, 50, 100, textPaint);

        if (gameOver) {
            canvas.drawText("GAME OVER",
                    screenWidth / 4f,
                    screenHeight / 2f,
                    textPaint);

            canvas.drawText("Tap to Restart",
                    screenWidth / 5f,
                    screenHeight / 2f + 100,
                    textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (gameOver) {
                initGame();
            } else {
                bird.jump();
                soundPool.play(flapSound, 1, 1, 1, 0, 1);
            }
        }
        return true;
    }

    public void pause() {
        if (gameThread != null) {
            gameThread.setRunning(false);
            try {
                gameThread.join();
            } catch (InterruptedException ignored) {}
        }
    }

    public void resume() {
        // surfaceCreated handles everything
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {}
    @Override public void surfaceDestroyed(SurfaceHolder holder) {}
}
