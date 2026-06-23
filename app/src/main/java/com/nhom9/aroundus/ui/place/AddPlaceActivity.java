package com.nhom9.aroundus.ui.place;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nhom9.aroundus.R;
import com.nhom9.aroundus.model.Place;
import com.nhom9.aroundus.repository.PlaceRepository;
import com.nhom9.aroundus.utils.ImageUtils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;

public class AddPlaceActivity extends AppCompatActivity {

    private EditText edtPlaceName, edtPlaceAddress, edtPlaceDescription;
    private TextView tvPlaceCategory;
    private ImageView imgPreview1, imgPreview2, imgPreview3;
    private Button btnSelectImage, btnSavePlace;
    private ProgressBar progressBar;

    private PlaceRepository placeRepository;

    private List<Uri> selectedImageUris = new ArrayList<>();

    // Đếm số lượng ảnh đã upload thành công
    private int uploadSuccessCount = 0;

    // Xử lý kết quả trả về khi chọn nhiều hình ảnh
    private final ActivityResultLauncher<String> pickMultipleImagesLauncher = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(),
            uris -> {
                if (uris != null && !uris.isEmpty()) {
                    if (uris.size() > 3) {
                        Toast.makeText(this, "Chỉ được chọn tối đa 3 ảnh! Đã lấy 3 ảnh đầu.", Toast.LENGTH_SHORT).show();
                        selectedImageUris = uris.subList(0, 3);
                    } else {
                        selectedImageUris = uris;
                    }
                    displaySelectedImages();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        placeRepository = new PlaceRepository();

        initViews();
        setupListeners();
    }

    private void initViews() {
        edtPlaceName = findViewById(R.id.edtPlaceName);
        edtPlaceAddress = findViewById(R.id.edtPlaceAddress);
        tvPlaceCategory = findViewById(R.id.tvPlaceCategory);
        edtPlaceDescription = findViewById(R.id.edtPlaceDescription);

        imgPreview1 = findViewById(R.id.imgPreview1);
        imgPreview2 = findViewById(R.id.imgPreview2);
        imgPreview3 = findViewById(R.id.imgPreview3);

        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSavePlace = findViewById(R.id.btnSavePlace);
        progressBar = findViewById(R.id.progressBar);

        SpannableString hintName = new SpannableString("Tên địa điểm (Ví dụ: Quán phở A, Cà phê B)");
        hintName.setSpan(new AbsoluteSizeSpan(14, true), 0, hintName.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        edtPlaceName.setHint(hintName);

        SpannableString hintAddress = new SpannableString("Địa chỉ cụ thể...");
        hintAddress.setSpan(new AbsoluteSizeSpan(14, true), 0, hintAddress.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        edtPlaceAddress.setHint(hintAddress);

        SpannableString hintCategory = new SpannableString("Bấm để chọn danh mục");
        hintCategory.setSpan(new AbsoluteSizeSpan(14, true), 0, hintCategory.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tvPlaceCategory.setHint(hintCategory);

        SpannableString hintDesc = new SpannableString("Mô tả chi tiết về địa điểm này có gì thú vị...");
        hintDesc.setSpan(new AbsoluteSizeSpan(14, true), 0, hintDesc.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        edtPlaceDescription.setHint(hintDesc);
    }

    private void displaySelectedImages() {
        imgPreview1.setVisibility(View.GONE);
        imgPreview2.setVisibility(View.GONE);
        imgPreview3.setVisibility(View.GONE);

        if (selectedImageUris.size() > 0) {
            imgPreview1.setVisibility(View.VISIBLE);
            imgPreview1.setImageURI(selectedImageUris.get(0));
        }
        if (selectedImageUris.size() > 1) {
            imgPreview2.setVisibility(View.VISIBLE);
            imgPreview2.setImageURI(selectedImageUris.get(1));
        }
        if (selectedImageUris.size() > 2) {
            imgPreview3.setVisibility(View.VISIBLE);
            imgPreview3.setImageURI(selectedImageUris.get(2));
        }
    }

    private void setupListeners() {
        tvPlaceCategory.setOnClickListener(v -> {
            String[] categories = {"Quán ăn", "Cà phê", "Mua sắm", "Khu vui chơi"};
            new AlertDialog.Builder(this)
                    .setTitle("Chọn danh mục")
                    .setItems(categories, (dialog, which) -> {
                        tvPlaceCategory.setText(categories[which]);
                    })
                    .show();
        });

        // Gọi laucher lấy nhiều ảnh
        btnSelectImage.setOnClickListener(v -> {
            pickMultipleImagesLauncher.launch("image/*");
        });

        btnSavePlace.setOnClickListener(v -> {
            if (selectedImageUris.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 ảnh (tối đa 3)!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (edtPlaceName.getText().toString().trim().isEmpty()) {
                edtPlaceName.setError("Trống");
                return;
            }
            if (edtPlaceAddress.getText().toString().trim().isEmpty()) {
                edtPlaceAddress.setError("Trống");
                return;
            }
            if (tvPlaceCategory.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn danh mục!", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadImagesToCloudinary();
        });
    }

    // Duyệt mảng URI và đẩy lên Cloudinary
    private void uploadImagesToCloudinary() {
        progressBar.setVisibility(View.VISIBLE);
        btnSavePlace.setEnabled(false);
        btnSavePlace.setText("Đang tải ảnh lên (0/" + selectedImageUris.size() + ")...");

        List<String> uploadedUrls = new ArrayList<>();
        uploadSuccessCount = 0;

        for (Uri uri : selectedImageUris) {
            ImageUtils.uploadImage(this, uri, new ImageUtils.UploadListener() {
                @Override
                public void onSuccess(String imageUrl) {
                    runOnUiThread(() -> {
                        uploadSuccessCount++;
                        uploadedUrls.add(imageUrl);
                        btnSavePlace.setText("Đang tải ảnh lên (" + uploadSuccessCount + "/" + selectedImageUris.size() + ")...");

                        if (uploadSuccessCount == selectedImageUris.size()) {
                            btnSavePlace.setText("ĐANG LƯU DỮ LIỆU...");
                            savePlaceToFirestore(uploadedUrls);
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        // Nếu đang hiện Loading thì tắt đi và báo lỗi
                        if (progressBar.getVisibility() == View.VISIBLE) {
                            progressBar.setVisibility(View.GONE);
                            btnSavePlace.setEnabled(true);
                            btnSavePlace.setText("ĐĂNG ĐỊA ĐIỂM");
                            Toast.makeText(AddPlaceActivity.this, "Lỗi tải ảnh: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
    }

    private void savePlaceToFirestore(List<String> imageUrls) {
        String name = edtPlaceName.getText().toString().trim();
        String address = edtPlaceAddress.getText().toString().trim();
        String category = tvPlaceCategory.getText().toString().trim();
        String description = edtPlaceDescription.getText().toString().trim();

        Place newPlace = new Place();
        newPlace.setName(name);
        newPlace.setAddress(address);
        newPlace.setCategory(category);
        newPlace.setDescription(description);
        newPlace.setImageUrls(imageUrls);
        newPlace.setAvgRating(0.0);

        placeRepository.addPlace(newPlace, task -> {
            if (task.isSuccessful()) {
                String documentId = task.getResult().getId();
                newPlace.setPlaceId(documentId);

                Toast.makeText(AddPlaceActivity.this, "Đăng địa điểm thành công!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                progressBar.setVisibility(View.GONE);
                btnSavePlace.setEnabled(true);
                btnSavePlace.setText("ĐĂNG ĐỊA ĐIỂM");
                Toast.makeText(AddPlaceActivity.this, "Lỗi lưu dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}