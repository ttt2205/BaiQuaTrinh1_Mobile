package com.example.btqt1;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerViewQRCode extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_view_qrcode); // Ensure this layout has ZXingScannerView
        mScannerView = findViewById(R.id.scannerView);
        ImageView cancelButton = (ImageView) findViewById(R.id.ImageViewCancel);
        cancelButton.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if camera permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            mScannerView.setResultHandler(this); // Set this activity as the result handler
            mScannerView.startCamera();          // Start the camera
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera(); // Stop the camera on pause
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mScannerView.startCamera();
        } else {
            Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void handleResult(Result rawResult) {
        String qrData = rawResult.getText();

        // Kiểm tra nếu dữ liệu là URL
        if (Patterns.WEB_URL.matcher(qrData).matches()) {
            // Mở URL trong trình duyệt
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(qrData));
            startActivity(browserIntent);
        }
        // Nếu không phải URL, xử lý dữ liệu khác
        else {
            Toast.makeText(this, "Dữ liệu QR: " + qrData, Toast.LENGTH_SHORT).show();

            // Thực hiện hành động khác với qrData, ví dụ lưu vào cơ sở dữ liệu hoặc điều hướng
            switch (qrData) {
                case "activityUser": {
                    Intent intent = new Intent(ScannerViewQRCode.this, ViewActivityContact.class);
//                    intent.putExtra("contactList", (Serializable) contactList);
                    startActivity(intent);
                    break;
                }
                case "email": {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"webmaster@website.com"});  // Địa chỉ email người nhận
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "my subject");  // Tiêu đề email
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "body text");  // Nội dung email

                    if (emailIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(emailIntent);  // Mở ứng dụng email (như Gmail hoặc bất kỳ ứng dụng email hỗ trợ)
                    } else {
                        Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
                    }
                }
                default: {
                    Toast.makeText(ScannerViewQRCode.this, "Không có data hợp lệ:", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // Bắt đầu lại camera để quét mã mới nếu cần
        mScannerView.resumeCameraPreview(this);
    }
}