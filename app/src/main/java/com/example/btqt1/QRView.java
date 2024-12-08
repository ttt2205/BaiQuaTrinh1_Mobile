package com.example.btqt1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class QRView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrview);

        // Chuyển đổi byte[] thành bitmap
        byte[] qrCodeByte = getIntent().getByteArrayExtra("bitmap_data");
        Bitmap qrCodeBitmap = BitmapFactory.decodeByteArray(qrCodeByte, 0, qrCodeByte.length);

        // Hiển thị QRCode lên ImageView
        ImageView qrView = (ImageView) findViewById(R.id.QRCodeImageView);
        qrView.setImageBitmap(qrCodeBitmap);

        Button prevMainIntent = (Button) findViewById(R.id.btnPrevMainIntent);
        prevMainIntent.setOnClickListener(v -> {
            finish();
        });

    }
}