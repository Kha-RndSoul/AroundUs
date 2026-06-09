package com.nhom9.aroundus.ui.place;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom9.aroundus.R;
import com.nhom9.aroundus.adapter.ReviewAdapter;
import com.nhom9.aroundus.model.Place;
import com.nhom9.aroundus.model.Review;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaceDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ImageView imgPlaceBig, imgPlaceSmall1, imgPlaceSmall2;
    private ImageButton btnBack;
    private TextView tvPlaceName, tvRatingValue, tvAddress, tvHoursStatus, tvDescription;
    private MaterialButton btnDirectionsBottom, btnWriteReview, btnSave;

    private MapView mapView;
    private GoogleMap googleMap;

    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Place currentPlace;
    private boolean isFavorite = false; // Trạng thái yêu thích

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();

        // Khởi tạo bản đồ
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        btnBack.setOnClickListener(v -> onBackPressed());

        currentPlace = (Place) getIntent().getSerializableExtra("SELECTED_PLACE");

        if (currentPlace != null) {
            displayPlaceInfo();
            setupRecyclerView();
            loadReviewsFromFirestore();
        } else {
            Toast.makeText(this, "Không tìm thấy dữ liệu địa điểm!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnDirectionsBottom.setOnClickListener(v -> openGoogleMapsByQuery());
        btnWriteReview.setOnClickListener(v -> showWriteReviewDialog());

        // Logic bấm nút trái tim
        btnSave.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            if (isFavorite) {
                btnSave.setIconResource(android.R.drawable.ic_media_ff); // Hoặc tạo icon trái tim full màu thay vào đây
                btnSave.setIconTintResource(android.R.color.holo_red_light);
                Toast.makeText(this, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                btnSave.setIconResource(R.drawable.ic_heart_outline);
                btnSave.setIconTintResource(R.color.black); // Trả về màu gốc
                Toast.makeText(this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        imgPlaceBig = findViewById(R.id.imgPlaceBig);
        imgPlaceSmall1 = findViewById(R.id.imgPlaceSmall1);
        imgPlaceSmall2 = findViewById(R.id.imgPlaceSmall2);
        btnBack = findViewById(R.id.btnBack);
        tvPlaceName = findViewById(R.id.tvPlaceName);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        tvAddress = findViewById(R.id.tvAddress);
        tvHoursStatus = findViewById(R.id.tvHoursStatus);
        tvDescription = findViewById(R.id.tvDescription);
        btnDirectionsBottom = findViewById(R.id.btnDirectionsBottom);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        btnSave = findViewById(R.id.btnSave);
        rvReviews = findViewById(R.id.rvReviews);
        mapView = findViewById(R.id.mapView);
    }

    // --- SETUP BẢN ĐỒ ---
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        // Bật thanh điều khiển Zoom (+ / -) ở góc bản đồ
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);

        if (currentPlace != null && currentPlace.getLat() != 0 && currentPlace.getLng() != 0) {
            LatLng placeLocation = new LatLng(currentPlace.getLat(), currentPlace.getLng());
            googleMap.addMarker(new MarkerOptions().position(placeLocation).title(currentPlace.getName()));
            // Zoom lại gần vị trí đó (mức zoom 15 là vừa tầm)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLocation, 15f));
        }
    }

    // Các hàm vòng đời bắt buộc để MapView không bị crash hoặc đen màn hình
    @Override
    protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override
    protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override
    protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override
    public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }

    // --- CÁC HÀM CÒN LẠI GIỮ NGUYÊN NHƯ CŨ ---
    private void displayPlaceInfo() {
        tvPlaceName.setText(currentPlace.getName());
        tvRatingValue.setText(String.format("%.1f", currentPlace.getAvgRating()));
        tvAddress.setText(currentPlace.getAddress());
        tvDescription.setText(currentPlace.getDescription() != null ? currentPlace.getDescription() : "Địa điểm này chưa có mô tả.");

        if (currentPlace.getOpeningHour() != null && currentPlace.getClosingHour() != null) {
            tvHoursStatus.setText(currentPlace.getOpeningHour() + " - " + currentPlace.getClosingHour());
        }

        List<String> images = currentPlace.getImageUrls();
        if (images != null && !images.isEmpty()) {
            if (images.size() > 0) loadWebImageToImageView(images.get(0), imgPlaceBig);
            if (images.size() > 1) loadWebImageToImageView(images.get(1), imgPlaceSmall1);
            if (images.size() > 2) loadWebImageToImageView(images.get(2), imgPlaceSmall2);
        }
    }

    private void loadWebImageToImageView(String urlString, ImageView imageView) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                InputStream in = new URL(urlString).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                handler.post(() -> imageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setupRecyclerView() { /* Như cũ */ }
    private void loadReviewsFromFirestore() { /* Như cũ */ }
    private void openGoogleMapsByQuery() { /* Như cũ */ }
    private void showWriteReviewDialog() { /* Như cũ */ }
    private void updatePlaceRatingOnFirestore(int newRating) { /* Như cũ */ }
}