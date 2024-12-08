package com.example.btqt1;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
    private static final int PICK_CONTACT_REQUEST = 1;
    private static final int REQUEST_READ_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Xử lý tạo QRCode
        Button buttonCreateQR = (Button) findViewById(R.id.btnCallQRScanner);
        buttonCreateQR.setOnClickListener(v -> {
//            String data = "https://www.google.com/search?q=your_search_query";
            String data = "email";
            Bitmap qrCode = generateCode(data,BarcodeFormat.QR_CODE, 300, 300);
            compressBitmapInBackground(qrCode);
        });

        // Open camera to Scan QRCode
        Button buttonScanQR = (Button) findViewById(R.id.btnCamera);
        buttonScanQR.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScannerViewQRCode.class);
            startActivity(intent);
        });


        // Kiểm tra quyền READ_CONTACTS
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // Yêu cầu quyền nếu chưa được cấp
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
        } else {
            // Quyền đã được cấp, tiếp tục với chức năng lấy danh bạ
            //       Call danh ba cho user chon
            Log.d("test", "Khoi dong thanh cong");
            Button btnCallUserActivity = findViewById(R.id.btnCallActivityUser);
            btnCallUserActivity.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                pickContactLauncher.launch(intent);
            });
        }

//       Hiển thị contact đã chọn
        Button btnCallViewActivity = (Button) findViewById(R.id.btnCallContact);
        btnCallViewActivity.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViewActivityContact.class);
//            intent.putExtra("contactList", (Serializable) contactList);
            startActivity(intent);
        });

        // Call email
        Button btnCallEmail = (Button) findViewById(R.id.btnCallEmail);
        btnCallEmail.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"webmaster@website.com"});  // Địa chỉ email người nhận
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "my subject");  // Tiêu đề email
            emailIntent.putExtra(Intent.EXTRA_TEXT, "body text");  // Nội dung email

            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(emailIntent, "Send email..."));  // Mở ứng dụng email (như Gmail hoặc bất kỳ ứng dụng email hỗ trợ)
            } else {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private ActivityResultLauncher<Intent> pickContactLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri contactUri = result.getData().getData();
                // Tiến hành xử lý để lấy thông tin danh bạ
                getContactInfo(contactUri);
            }
        });

    // Xử lý khi người dùng chọn contact
    //    projection là mảng các trường dữ liệu bạn muốn lấy từ danh bạ, ở đây là tên liên hệ (DISPLAY_NAME).
    //    getContentResolver().query(...) thực hiện truy vấn danh bạ để lấy thông tin của liên hệ.
    //    contactName là tên của liên hệ mà bạn đã chọn
    private void getContactInfo(Uri contactUri) {
        String[] projection = {ContactsContract.Contacts.DISPLAY_NAME};
        try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Log.d("Contact Info", "Tên liên hệ: " + contactName);
                Toast.makeText(this, "Tên liên hệ: " + contactName, Toast.LENGTH_SHORT).show();            }
        }
    }

    // Xử lý truyền QRCode qua intent
    public void compressBitmapInBackground(Bitmap bitmap) {
        // Xử dụng thread để khởi chạy QRCode không gây ảnh hưởng tới quá trình chạy app
        ExecutorService executorService = Executors.newSingleThreadExecutor();  // Sử dụng ExecutorService để xử lý tác vụ nền
        executorService.submit(() -> {
            // Nén Bitmap trong nền
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Truyền byteArray qua Intent sau khi nén hoàn thành compress (nén)
            runOnUiThread(() -> {
                // Chạy trên main thread khi truyền qua Intent
                Intent intent = new Intent(MainActivity.this, QRView.class);
                intent.putExtra("bitmap_data", byteArray);
                startActivity(intent);
            });
        });
    }

    public Bitmap generateCode(String data, BarcodeFormat format, int width, int height) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(data, format, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF); // Black for 1, white for 0
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

}