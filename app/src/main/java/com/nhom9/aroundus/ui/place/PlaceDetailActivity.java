package com.nhom9.aroundus.ui.place;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.CollapsingToolbarLayout;
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

public class PlaceDetailActivity extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView imgPlaceDetail;
    private TextView tvPlaceName, tvCategory, tvRatingValue, tvAddress;
    private RatingBar ratingBarPlace;
    private MaterialButton btnDirections, btnWriteReview;

    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Place currentPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Nhận đối tượng Place từ Intent chuyển qua
        currentPlace = (Place) getIntent().getSerializableExtra("SELECTED_PLACE");

        if (currentPlace != null) {
            displayPlaceInfo();
            setupRecyclerView();
            loadReviewsFromFirestore();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin địa điểm!", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnDirections.setOnClickListener(v -> openGoogleMapsByQuery());
        btnWriteReview.setOnClickListener(v -> showWriteReviewDialog());
    }

    private void initViews() {
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        imgPlaceDetail = findViewById(R.id.imgPlaceDetail);
        tvPlaceName = findViewById(R.id.tvPlaceName);
        tvCategory = findViewById(R.id.tvCategory);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        tvAddress = findViewById(R.id.tvAddress);
        ratingBarPlace = findViewById(R.id.ratingBarPlace);
        btnDirections = findViewById(R.id.btnDirections);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        rvReviews = findViewById(R.id.rvReviews);
    }

    private void displayPlaceInfo() {
        collapsingToolbar.setTitle(currentPlace.getName());
        tvPlaceName.setText(currentPlace.getName());
        tvCategory.setText(currentPlace.getCategory());
        tvAddress.setText("Địa chỉ: " + currentPlace.getAddress());
        ratingBarPlace.setRating((float) currentPlace.getAvgRating());
        tvRatingValue.setText(String.format("(%.1f)", currentPlace.getAvgRating()));

        // Render ảnh từ URL Cloudinary
        if (currentPlace.getImageUrl() != null && !currentPlace.getImageUrl().isEmpty()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                try {
                    InputStream in = new URL(currentPlace.getImageUrl()).openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    handler.post(() -> imgPlaceDetail.setImageBitmap(bitmap));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void setupRecyclerView() {
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
    }

    private void loadReviewsFromFirestore() {
        db.collection("reviews")
                .whereEqualTo("placeId", currentPlace.getPlaceId())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        reviewList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Review review = doc.toObject(Review.class);
                            reviewList.add(review);
                        }
                        reviewAdapter.notifyDataSetChanged();
                    }
                });
    }

    // Cơ chế chỉ đường không cần tọa độ lat lng: Tìm kiếm bằng tên kết hợp địa chỉ trên Maps
    private void openGoogleMapsByQuery() {
        String query = currentPlace.getName() + " " + currentPlace.getAddress();
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Intent webMapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            startActivity(webMapIntent);
        }
    }

    private void showWriteReviewDialog() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để viết đánh giá!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_write_review, null);
        builder.setView(dialogView);

        RatingBar rbInput = dialogView.findViewById(R.id.rbInputStar);
        EditText edtComment = dialogView.findViewById(R.id.edtCommentInput);

        builder.setTitle("Viết đánh giá của bạn")
                .setPositiveButton("Gửi", (dialog, id) -> {
                    int rating = (int) rbInput.getRating();
                    String comment = edtComment.getText().toString().trim();

                    if (comment.isEmpty()) {
                        Toast.makeText(PlaceDetailActivity.this, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Review newReview = new Review();
                    newReview.setReviewId(UUID.randomUUID().toString());
                    newReview.setPlaceId(currentPlace.getPlaceId());
                    newReview.setUserId(currentUser.getUid());
                    newReview.setUserName(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail());
                    newReview.setRating(rating);
                    newReview.setComment(comment);
                    newReview.setCreatedAt(Timestamp.now());

                    db.collection("reviews").document(newReview.getReviewId())
                            .set(newReview)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(PlaceDetailActivity.this, "Đăng thành công!", Toast.LENGTH_SHORT).show();
                                updatePlaceRatingOnFirestore(rating);
                            })
                            .addOnFailureListener(e -> Toast.makeText(PlaceDetailActivity.this, "Lỗi khi gửi!", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", (dialog, id) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void updatePlaceRatingOnFirestore(int newRating) {
        db.collection("reviews").whereEqualTo("placeId", currentPlace.getPlaceId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    double total = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Review r = doc.toObject(Review.class);
                        total += r.getRating();
                    }
                    double newAvg = count > 0 ? total / count : newRating;
                    db.collection("places").document(currentPlace.getPlaceId())
                            .update("avgRating", newAvg);
                });
    }
}