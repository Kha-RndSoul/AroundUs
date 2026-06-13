package com.nhom9.aroundus.ui.place;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.nhom9.aroundus.R;

public class AddPlaceActivity extends AppCompatActivity {

    private EditText edtPlaceName, edtPlaceCategory, edtPlaceDescription;
    private ImageView imgPlacePreview;
    private Button btnSelectImage, btnSavePlace;
    private ProgressBar progressBar;

    // Biến lưu trữ đường dẫn URI của ảnh sau khi chọn
    private Uri selectedImageUri = null;

    // Cách mới của Android để xử lý kết quả trả về từ Intent (thay thế cho onActivityResult)
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    // Hiển thị ảnh vừa chọn lên ImageView
                    imgPlacePreview.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place); // Liên kết với file XML giao diện

        initViews();
        setupListeners();
    }

    private void initViews() {
        edtPlaceName = findViewById(R.id.edtPlaceName);
        edtPlaceCategory = findViewById(R.id.edtPlaceCategory);
        edtPlaceDescription = findViewById(R.id.edtPlaceDescription);
        imgPlacePreview = findViewById(R.id.imgPlacePreview);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSavePlace = findViewById(R.id.btnSavePlace);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        // Sự kiện bấm nút Chọn ảnh
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        // Sự kiện bấm nút Lưu (Tạm thời test việc chọn ảnh trước khi code Cloudinary)
        btnSavePlace.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh trước!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã chọn ảnh thành công! Chuẩn bị upload.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}