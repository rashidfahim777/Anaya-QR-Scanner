package com.faheem.anayapaint;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    MyCanvasView canvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout container = findViewById(R.id.canvasContainer);
        canvasView = new MyCanvasView(this);
        container.addView(canvasView);

        // Color buttons
        findViewById(R.id.btnRed).setOnClickListener(v -> canvasView.setColor(Color.RED));
        findViewById(R.id.btnBlue).setOnClickListener(v -> canvasView.setColor(Color.BLUE));
        findViewById(R.id.btnGreen).setOnClickListener(v -> canvasView.setColor(Color.GREEN));
        findViewById(R.id.btnPurple).setOnClickListener(v -> canvasView.setColor(Color.MAGENTA));
        findViewById(R.id.btnPink).setOnClickListener(v -> canvasView.setColor(Color.parseColor("#FFC0CB")));

        // Brush buttons
        findViewById(R.id.btnSmall).setOnClickListener(v -> canvasView.setBrushSize(5f));
        findViewById(R.id.btnMedium).setOnClickListener(v -> canvasView.setBrushSize(10f));
        findViewById(R.id.btnLarge).setOnClickListener(v -> canvasView.setBrushSize(20f));

        // Clear button
        findViewById(R.id.btnClear).setOnClickListener(v -> canvasView.clearCanvas());
    }
}
