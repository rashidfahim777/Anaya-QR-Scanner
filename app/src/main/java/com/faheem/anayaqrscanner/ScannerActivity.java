package com.faheem.anayaqrscanner;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@OptIn(markerClass = ExperimentalGetImage.class)
public class ScannerActivity extends AppCompatActivity  {

    private static final String TAG = "ScannerActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int GALLERY_REQUEST_CODE = 101;

    private PreviewView previewView;
    private ImageButton btnFlash, btnBack;
    private MaterialButton btnGallery, btnKeyboard;
    private View scanLine;
    private View scanFrameContainer;
    private TextView tvInstruction;
    private Camera camera;
    private boolean isFlashOn = false;
    private ExecutorService cameraExecutor;
    private boolean isScanned = false;
    private ObjectAnimator scanAnimator;
    private MediaPlayer scanSoundPlayer;
    private SharedPrefManager prefManager;  // ADDED: SharedPrefManager instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // ADDED: Initialize SharedPrefManager
        prefManager = new SharedPrefManager(this);

        // Initialize sound player
        initializeSoundPlayer();

        initializeViews();
        setupClickListeners();
        checkCameraPermission();
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void initializeSoundPlayer() {
        try {
            // Create a simple beep sound or use a sound file from raw folder
            // If you have a sound file in res/raw/scan_sound.mp3
            scanSoundPlayer = MediaPlayer.create(this, R.raw.scan_sound);

            // If you don't have a sound file, you can create a simple tone programmatically
            // or use Android's built-in notification sound
            if (scanSoundPlayer == null) {
                // Fallback to system default notification sound
                scanSoundPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
            }

            if (scanSoundPlayer != null) {
                scanSoundPlayer.setVolume(1.0f, 1.0f);
                scanSoundPlayer.setOnCompletionListener(mp -> {
                    // Reset player when done
                    mp.seekTo(0);
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing sound player", e);
            scanSoundPlayer = null;
        }
    }

    private void initializeViews() {
        previewView = findViewById(R.id.preview_view);
        btnFlash = findViewById(R.id.btn_flash);
        btnBack = findViewById(R.id.btn_back);
        btnGallery = findViewById(R.id.btn_gallery);
        btnKeyboard = findViewById(R.id.btn_keyboard);
        scanLine = findViewById(R.id.scan_line);
        scanFrameContainer = findViewById(R.id.scan_frame_container);
    }

    private void setupClickListeners() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Gallery button
        if (btnGallery != null) {
            btnGallery.setOnClickListener(v -> {
                Log.d(TAG, "Gallery button clicked");
                openGallery();
            });
        } else {
            Log.e(TAG, "Gallery button not found in layout");
        }

        // Keyboard button
        if (btnKeyboard != null) {
            btnKeyboard.setOnClickListener(v -> {
                Log.d(TAG, "Keyboard button clicked");
                showManualInputDialog();
            });
        } else {
            Log.e(TAG, "Keyboard button not found in layout");
        }

        // Flash button
        if (btnFlash != null) {
            btnFlash.setOnClickListener(v -> toggleFlash());
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (Exception e) {
                Log.e(TAG, "Camera start failed", e);
                Toast.makeText(this, "Camera failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        // Preview
        Preview preview = new Preview.Builder()
                .setTargetRotation(previewView.getDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image analysis
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            processBarcode(imageProxy);
        });

        try {
            camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
            );

            // Start scan animation after camera is ready
            startScanAnimation();

        } catch (Exception e) {
            Log.e(TAG, "Camera binding failed", e);
            Toast.makeText(this, "Camera setup failed", Toast.LENGTH_LONG).show();
        }
    }

    private void startScanAnimation() {
        if (scanFrameContainer == null || scanLine == null) {
            Log.e(TAG, "Scan animation views not initialized");
            return;
        }

        // Wait for layout to be measured
        scanFrameContainer.post(() -> {
            // Create up and down animation
            float startY = 0f;
            float endY = scanFrameContainer.getHeight() - scanLine.getHeight();

            scanAnimator = ObjectAnimator.ofFloat(
                    scanLine,
                    "translationY",
                    startY,
                    endY
            );

            scanAnimator.setDuration(2000); // 2 seconds for one cycle
            scanAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            scanAnimator.setRepeatMode(ObjectAnimator.REVERSE);
            scanAnimator.setInterpolator(new LinearInterpolator());
            scanAnimator.start();

            Log.d(TAG, "Scan animation started. Height: " + scanFrameContainer.getHeight());
        });
    }

    private void stopScanAnimation() {
        if (scanAnimator != null && scanAnimator.isRunning()) {
            scanAnimator.cancel();
            scanAnimator = null;
        }
    }

    @ExperimentalGetImage
    private void processBarcode(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null || isScanned) {
            imageProxy.close();
            return;
        }

        try {
            InputImage image = InputImage.fromMediaImage(
                    imageProxy.getImage(),
                    imageProxy.getImageInfo().getRotationDegrees()
            );

            BarcodeScanner barcodeScanner = BarcodeScanning.getClient();
            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        if (!barcodes.isEmpty() && !isScanned) {
                            Barcode barcode = barcodes.get(0);
                            String rawValue = barcode.getRawValue();
                            if (rawValue != null && !rawValue.trim().isEmpty()) {
                                handleScanResult(rawValue);
                                isScanned = true;
                            }
                        }
                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Barcode scan failed", e);
                        imageProxy.close();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Image processing failed", e);
            imageProxy.close();
        }
    }

    private void handleScanResult(String rawValue) {
        Log.d(TAG, "Scanned: " + rawValue);

        runOnUiThread(() -> {
            // Stop scan animation when result is found
            stopScanAnimation();

            // Hide scan line
            if (scanLine != null) {
                scanLine.setVisibility(View.GONE);
            }

            // Hide instruction text
            if (tvInstruction != null) {
                tvInstruction.setVisibility(View.GONE);
            }

            // ADDED: Check settings and provide feedback based on user preferences
            provideScanFeedback();

            // Navigate to result screen
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("type", detectBarcodeType(rawValue));
            intent.putExtra("content", rawValue);
            startActivity(intent);
            finish();
        });
    }

    // ADDED: New method to check settings and provide appropriate feedback
    private void provideScanFeedback() {
        // Check vibration setting
        if (prefManager.getVibrationEnabled()) {
            vibrateOnScan();
        }

        // Check sound setting
        if (prefManager.getSoundEnabled()) {
            playScanSound();
        }
    }

    // ADDED: Updated vibration method to be more reliable
    private void vibrateOnScan() {
        try {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // For Android 8.0 (Oreo) and above
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    // For older versions
                    vibrator.vibrate(200);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Vibration failed", e);
        }
    }

    // ADDED: Method to play scan sound
    private void playScanSound() {
        if (scanSoundPlayer != null) {
            try {
                // Reset if already playing
                if (scanSoundPlayer.isPlaying()) {
                    scanSoundPlayer.seekTo(0);
                }
                scanSoundPlayer.start();
            } catch (Exception e) {
                Log.e(TAG, "Error playing sound", e);
            }
        } else {
            // Fallback to system beep
            try {
                MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                        .start();
            } catch (Exception e) {
                Log.e(TAG, "Error playing fallback sound", e);
            }
        }
    }

    private int detectBarcodeType(String rawValue) {
        if (rawValue == null) return 256; // Default to QR Code

        // Check if it's a standard barcode (numeric only, specific lengths)
        if (rawValue.matches("^[0-9]+$")) {
            int length = rawValue.length();

            // UPC-A: 12 digits
            if (length == 12) return 128;

            // UPC-E: 6, 7, or 8 digits
            if (length >= 6 && length <= 8) return 512;

            // EAN-13: 13 digits
            if (length == 13) return 64;

            // EAN-8: 8 digits
            if (length == 8) return 32;

            // CODE 128: variable length
            if (length >= 4 && length <= 30) return 1;

            return 1; // Default to CODE_128
        }

        // If contains letters and numbers, might be CODE_39
        if (rawValue.matches("^[A-Za-z0-9]+$")) {
            return 2; // CODE_39
        }

        // Check for URLs
        if (rawValue.startsWith("http://") || rawValue.startsWith("https://")) {
            return 256; // QR Code
        }

        // Check for other types
        if (rawValue.startsWith("tel:")) return 257; // Phone
        if (rawValue.startsWith("mailto:")) return 258; // Email
        if (rawValue.startsWith("WIFI:")) return 259; // WiFi
        if (rawValue.startsWith("BEGIN:VCARD")) return 260; // Contact
        if (rawValue.startsWith("SMSTO:")) return 261; // SMS

        return 256; // Default to QR Code
    }

    private void openGallery() {
        Log.d(TAG, "Opening gallery...");

        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png", "image/jpg"});

        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
        } else {
            Toast.makeText(this, "No gallery app found", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No app found to handle gallery intent");

            // Try alternative approach
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_CODE);
        }
    }

    private void showManualInputDialog() {
        Log.d(TAG, "Showing manual input dialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter QR/Barcode Data");
        builder.setMessage("Type or paste the code below:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(3);
        input.setMaxLines(6);
        input.setHint("Enter URL, text, or barcode number...");
        builder.setView(input);

        builder.setPositiveButton("Scan", (dialog, which) -> {
            String manualInput = input.getText().toString().trim();
            if (!manualInput.isEmpty()) {
                processManualInput(manualInput);
            } else {
                Toast.makeText(this, "Please enter some text", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
        input.requestFocus();
    }

    private void processManualInput(String input) {
        Log.d(TAG, "Processing manual input: " + input);

        // ADDED: Provide feedback for manual input too
        provideScanFeedback();

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("content", input);
        intent.putExtra("type", detectInputType(input));
        startActivity(intent);
    }

    private int detectInputType(String input) {
        if (input.startsWith("http://") || input.startsWith("https://")) {
            return 256; // QR Code
        } else if (input.startsWith("tel:")) {
            return 257; // Phone
        } else if (input.startsWith("mailto:")) {
            return 258; // Email
        } else if (input.startsWith("WIFI:")) {
            return 259; // WiFi
        } else if (input.matches("^[0-9]+$")) {
            if (input.length() == 13) return 64;  // EAN-13
            if (input.length() == 12) return 128; // UPC-A
            if (input.length() == 8) return 32;   // EAN-8
            return 1; // Default to CODE_128
        } else {
            return 256; // Default to QR Code
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (resultCode == RESULT_OK && requestCode == GALLERY_REQUEST_CODE && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                Log.d(TAG, "Image selected from gallery: " + imageUri.toString());
                processImageFromGallery(imageUri);
            } else {
                Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processImageFromGallery(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            // ADDED: Show loading indicator
            Toast.makeText(this, "Scanning image...", Toast.LENGTH_SHORT).show();

            // Using ML Kit for image scanning (same as camera)
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            BarcodeScanner barcodeScanner = BarcodeScanning.getClient();

            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        if (!barcodes.isEmpty()) {
                            Barcode barcode = barcodes.get(0);
                            String rawValue = barcode.getRawValue();
                            if (rawValue != null) {
                                // ADDED: Provide feedback for gallery scan too
                                provideScanFeedback();

                                // Navigate to result screen
                                Intent intent = new Intent(this, ResultActivity.class);
                                intent.putExtra("content", rawValue);
                                intent.putExtra("type", 256);
                                startActivity(intent);
                            }
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(this, "No QR code or barcode found", Toast.LENGTH_LONG).show());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Image scan failed", e);
                        runOnUiThread(() ->
                                Toast.makeText(this, "Scan failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    });

        } catch (IOException e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFlash() {
        if (camera == null) {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            isFlashOn = !isFlashOn;
            camera.getCameraControl().enableTorch(isFlashOn);

            int flashIcon = isFlashOn ?
                    R.drawable.ic_flash_on : R.drawable.ic_flash_off;
            if (btnFlash != null) {
                btnFlash.setImageResource(flashIcon);
            }

            String message = isFlashOn ? "Flash ON" : "Flash OFF";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Flash not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScanAnimation();

        // ADDED: Release MediaPlayer resources
        if (scanSoundPlayer != null) {
            try {
                if (scanSoundPlayer.isPlaying()) {
                    scanSoundPlayer.stop();
                }
                scanSoundPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing sound player", e);
            }
            scanSoundPlayer = null;
        }

        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScanAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isScanned = false; // Reset scan state when returning to scanner

        // Restore animation and visibility
        if (scanLine != null) {
            scanLine.setVisibility(View.VISIBLE);
        }
        if (tvInstruction != null) {
            tvInstruction.setVisibility(View.VISIBLE);
        }

        // Start animation again if camera is ready
        if (camera != null) {
            startScanAnimation();
        }
    }
}