package com.nhom9.aroundus.ui.place;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private TextView tvPlaceCategory, tvLatLng;
    private ImageView imgPreview1, imgPreview2, imgPreview3, btnBack;
    private Button btnSelectImage, btnSavePlace;
    private ProgressBar progressBar;

    private PlaceRepository placeRepository;

    private List<Uri> selectedImageUris = new ArrayList<>();
    private int uploadSuccessCount = 0;

    // Tọa độ lấy được từ Geocoder
    private double resolvedLat = 0.0;
    private double resolvedLng = 0.0;

    // Launcher chọn nhiều ảnh từ thư viện
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
        edtPlaceName        = findViewById(R.id.edtPlaceName);
        edtPlaceAddress     = findViewById(R.id.edtPlaceAddress);
        tvPlaceCategory     = findViewById(R.id.tvPlaceCategory);
        edtPlaceDescription = findViewById(R.id.edtPlaceDescription);
        imgPreview1         = findViewById(R.id.imgPreview1);
        imgPreview2         = findViewById(R.id.imgPreview2);
        imgPreview3         = findViewById(R.id.imgPreview3);
        btnBack             = findViewById(R.id.btnBack);
        btnSelectImage      = findViewById(R.id.btnSelectImage);
        btnSavePlace        = findViewById(R.id.btnSavePlace);
        progressBar         = findViewById(R.id.progressBar);
        tvLatLng            = findViewById(R.id.tvLatLng);

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

    // Hiển thị ảnh preview theo số lượng đã chọn
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
        // Chọn danh mục từ dialog
        tvPlaceCategory.setOnClickListener(v -> {
            String[] categories = {"Quán ăn", "Quán cf", "Mua sắm", "Khu vui chơi"};
            new AlertDialog.Builder(this)
                    .setTitle("Chọn danh mục")
                    .setItems(categories, (dialog, which) ->
                            tvPlaceCategory.setText(categories[which]))
                    .show();
        });

        // Mở thư viện chọn nhiều ảnh
        btnSelectImage.setOnClickListener(v -> pickMultipleImagesLauncher.launch("image/*"));

        // Validate rồi lấy tọa độ → upload ảnh → lưu Firestore
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

            // Bước 1: lấy tọa độ từ địa chỉ
            layToaDoTuDiaChi();
        });

        btnBack.setOnClickListener(v -> finish());
    }

    // Dùng Geocoder chuyển địa chỉ → lat/lng, chạy trên thread phụ tránh block UI
    private void layToaDoTuDiaChi() {
        String address = edtPlaceAddress.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        btnSavePlace.setEnabled(false);
        btnSavePlace.setText("Đang lấy tọa độ...");

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> results = geocoder.getFromLocationName(address, 1);

                runOnUiThread(() -> {
                    if (results != null && !results.isEmpty()) {
                        // Lấy tọa độ đầu tiên trả về
                        resolvedLat = results.get(0).getLatitude();
                        resolvedLng = results.get(0).getLongitude();

                        tvLatLng.setVisibility(View.VISIBLE);
                        tvLatLng.setTextColor(0xFF4CAF50);
                        tvLatLng.setText("📍 " + resolvedLat + ", " + resolvedLng);
                    } else {
                        // Không tìm được tọa độ, vẫn cho đăng nhưng cảnh báo
                        resolvedLat = 0.0;
                        resolvedLng = 0.0;
                        tvLatLng.setVisibility(View.VISIBLE);
                        tvLatLng.setTextColor(0xFFFF5722);
                        tvLatLng.setText("⚠️ Không tìm thấy tọa độ, bản đồ sẽ không hiển thị đúng");
                    }

                    // Bước 2: upload ảnh
                    uploadImagesToCloudinary();
                });

            } catch (IOException e) {
                runOnUiThread(() -> {
                    resolvedLat = 0.0;
                    resolvedLng = 0.0;
                    Toast.makeText(this, "Không thể lấy tọa độ, vẫn tiếp tục đăng.", Toast.LENGTH_SHORT).show();
                    // Vẫn tiếp tục dù không có tọa độ
                    uploadImagesToCloudinary();
                });
            }
        }).start();
    }

    // Upload từng ảnh lên Cloudinary, đếm đủ thì lưu Firestore
    private void uploadImagesToCloudinary() {
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

                        // Đủ số ảnh thì lưu Firestore
                        if (uploadSuccessCount == selectedImageUris.size()) {
                            btnSavePlace.setText("ĐANG LƯU DỮ LIỆU...");
                            savePlaceToFirestore(uploadedUrls);
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
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
        String name        = edtPlaceName.getText().toString().trim();
        String address     = edtPlaceAddress.getText().toString().trim();
        String category    = tvPlaceCategory.getText().toString().trim();
        String description = edtPlaceDescription.getText().toString().trim();

        Place newPlace = new Place();
        newPlace.setName(name);
        newPlace.setAddress(address);
        newPlace.setCategory(category);
        newPlace.setDescription(description);
        newPlace.setImageUrls(imageUrls);
        newPlace.setAvgRating(0.0);

        // Gán tọa độ đã lấy từ Geocoder
        newPlace.setLat(resolvedLat);
        newPlace.setLng(resolvedLng);

        placeRepository.addPlace(newPlace, task -> {
            if (task.isSuccessful()) {
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