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
import android.view.View;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.nhom9.aroundus.R;
import com.nhom9.aroundus.model.Place;
import com.nhom9.aroundus.repository.PlaceRepository;

public class AddPlaceActivity extends AppCompatActivity {

    private EditText edtPlaceName, edtPlaceCategory, edtPlaceDescription;
    private ImageView imgPlacePreview;
    private Button btnSelectImage, btnSavePlace;
    private ProgressBar progressBar;
    private PlaceRepository placeRepository;

    // Biến lưu trữ đường dẫn URI của ảnh sau khi chọn
    private Uri selectedImageUri = null;

    // Xử lý kết quả trả về từ Intent
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

        placeRepository = new PlaceRepository();

        initViews();
        setupListeners();

        // Khởi tạo Cloudinary
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dfbijq8ur");
            config.put("api_key", "418197113655413");
            config.put("api_secret", "t871XwlMIj_N066Z-UbDvoQxfYI");
            MediaManager.init(this, config);
        } catch (Exception e) {
            // MediaManager đã được khởi tạo, bỏ qua
        }
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

        // Sự kiện bấm nút Lưu
        btnSavePlace.setOnClickListener(v -> {
            // Validate: Bắt buộc phải chọn ảnh
            if (selectedImageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh trước khi đăng!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate: Bắt buộc phải nhập tên địa điểm
            if (edtPlaceName.getText().toString().trim().isEmpty()) {
                edtPlaceName.setError("Không được để trống tên địa điểm");
                return;
            }

            // Tiến hành upload ảnh trước
            uploadImageToCloudinary(selectedImageUri);
        });
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        // Hiện vòng xoay Loading và khóa nút bấm
        progressBar.setVisibility(View.VISIBLE);
        btnSavePlace.setEnabled(false);
        btnSavePlace.setText("Đang tải ảnh lên...");

        MediaManager.get().upload(imageUri).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                // Quá trình upload bắt đầu
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                // Lấy URL an toàn (https) do Cloudinary trả về
                String imageUrl = (String) resultData.get("secure_url");

                // Đảm bảo cập nhật UI trên Main Thread
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddPlaceActivity.this, "Tải ảnh thành công!", Toast.LENGTH_SHORT).show();
                    btnSavePlace.setText("ĐANG LƯU DỮ LIỆU...");

                    // Lưu lên FireStore
                    savePlaceToFirestore(imageUrl);
                });
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                // Xử lý Edge Case: Lỗi mạng hoặc lỗi file
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSavePlace.setEnabled(true);
                    btnSavePlace.setText("ĐĂNG ĐỊA ĐIỂM");
                    Toast.makeText(AddPlaceActivity.this, "Lỗi tải ảnh: " + error.getDescription(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                // Xử lý khi tác vụ bị hoãn và lên lịch lại
            }
        }).dispatch();
    }

    private void savePlaceToFirestore(String imageUrl) {
        String name = edtPlaceName.getText().toString().trim();
        String category = edtPlaceCategory.getText().toString().trim();
        String description = edtPlaceDescription.getText().toString().trim();

        // 1. Khởi tạo object Place bằng constructor rỗng
        Place newPlace = new Place();

        // 2. Dùng các hàm Setter để gán dữ liệu
        newPlace.setName(name);
        newPlace.setCategory(category);
        newPlace.setDescription(description);

        // Do model của bạn lưu danh sách ảnh (List<String>), nên ta cần tạo một List và add ảnh vừa upload vào
        List<String> images = new ArrayList<>();
        images.add(imageUrl);
        newPlace.setImageUrls(images);

        // (Tùy chọn) Gán thêm các dữ liệu mặc định khác nếu nhóm bạn yêu cầu
        newPlace.setAvgRating(0.0);

        // 3. Gọi Repository để lưu lên Firebase
        placeRepository.addPlace(newPlace, task -> {
            if (task.isSuccessful()) {
                // Sử dụng setPlaceId theo đúng tên hàm trong model của bạn
                String documentId = task.getResult().getId();
                newPlace.setPlaceId(documentId);

                // Optional: Nếu bạn muốn cập nhật lại chính document đó trên Firestore để lưu placeId vào trong field
                // placeRepository.updatePlaceId(documentId);

                Toast.makeText(AddPlaceActivity.this, "Đăng địa điểm thành công!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                progressBar.setVisibility(View.GONE);
                btnSavePlace.setEnabled(true);
                btnSavePlace.setText("ĐĂNG ĐỊA ĐIỂM");
                Toast.makeText(AddPlaceActivity.this, "Lỗi khi lưu dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}