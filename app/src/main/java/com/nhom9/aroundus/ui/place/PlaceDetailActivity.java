package com.nhom9.aroundus.ui.place;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom9.aroundus.R;
import com.nhom9.aroundus.adapter.ReviewAdapter;
import com.nhom9.aroundus.model.Place;
import com.nhom9.aroundus.model.Review;
import com.nhom9.aroundus.model.Schedule;
import com.nhom9.aroundus.repository.PlaceRepository;
import com.nhom9.aroundus.repository.ScheduleRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlaceDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ImageView imgPlaceBig;
    private ImageView imgPlaceSmall1;
    private ImageView imgPlaceSmall2;
    private ImageButton btnBack;
    private TextView tvPlaceName;
    private TextView tvRatingValue;
    private TextView tvAddress;
    private TextView tvHoursStatus;
    private TextView tvDescription;
    private MaterialButton btnSaveFavorite;
    private MaterialButton btnAddToSchedule;
    private MapView mapViewLocation;

    private GoogleMap googleMapInstance;
    private Place currentPlace;

    // Trạng thái yêu thích hiện tại, được đồng bộ từ Firestore khi màn hình mở
    private boolean isFavoriteSelected = false;

    private final PlaceRepository placeRepository = new PlaceRepository();
    private final ScheduleRepository scheduleRepository = new ScheduleRepository();

    private TextView tvWriteReview;
    private RecyclerView rvReviewsList;

    private ReviewAdapter reviewAdapter;
    private final List<Review> reviewList = new ArrayList<>();

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        anhXaView();

        mapViewLocation.onCreate(savedInstanceState);
        mapViewLocation.getMapAsync(this);

        // Nhận dữ liệu địa điểm từ màn hình trước gửi sang
        if (getIntent() != null && getIntent().hasExtra("SELECTED_PLACE")) {
            currentPlace = (Place) getIntent().getSerializableExtra("SELECTED_PLACE");
            hienThiThongTinDiaDiem();
            // Sau khi có placeId, kiểm tra xem địa điểm này đã được yêu thích chưa
            kiemTraTrangThaiYeuThich();
        }

        // Xử lý nút quay lại
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Khi nhấn nút trái tim: toggle yêu thích và đồng bộ lên Firestore
        btnSaveFavorite.setOnClickListener(v -> xuLyToggleYeuThich());

        // Khi nhấn nút "Thêm vào lịch trình": mở form lịch trình, gán sẵn địa điểm hiện tại
        btnAddToSchedule.setOnClickListener(v -> hienThiDialogThemLichTrinh());

        cauHinhDanhSachReview();

        if (currentPlace != null && currentPlace.getPlaceId() != null) {
            loadReviews();
        }

        tvWriteReview.setOnClickListener(v -> hienThiDialogVietDanhGia());
    }

    // Ánh xạ toàn bộ view từ layout
    private void anhXaView() {
        mapViewLocation   = findViewById(R.id.mapViewLocation);
        imgPlaceBig       = findViewById(R.id.imgPlaceBig);
        imgPlaceSmall1    = findViewById(R.id.imgPlaceSmall1);
        imgPlaceSmall2    = findViewById(R.id.imgPlaceSmall2);
        btnBack           = findViewById(R.id.btnBack);
        tvPlaceName       = findViewById(R.id.tvPlaceName);
        tvRatingValue     = findViewById(R.id.tvRatingValue);
        tvAddress         = findViewById(R.id.tvAddress);
        tvHoursStatus     = findViewById(R.id.tvAddress);
        tvDescription     = findViewById(R.id.tvDescription);
        btnSaveFavorite   = findViewById(R.id.btnSaveFavorite);
        btnAddToSchedule  = findViewById(R.id.btnAddToSchedule);
        tvWriteReview     = findViewById(R.id.tvWriteReview);
        rvReviewsList     = findViewById(R.id.rvReviewsList);
    }

    // Hiển thị thông tin địa điểm lên các TextView và ImageView
    private void hienThiThongTinDiaDiem() {
        if (currentPlace == null) return;

        tvPlaceName.setText(currentPlace.getName());
        tvAddress.setText(currentPlace.getAddress());
        tvRatingValue.setText(String.valueOf(currentPlace.getAvgRating()));

        // Hiển thị giờ mở cửa nếu có đủ 2 trường
        if (currentPlace.getOpeningHour() != null && currentPlace.getClosingHour() != null) {
            tvHoursStatus.setText(currentPlace.getOpeningHour() + " - " + currentPlace.getClosingHour());
        }

        // Ưu tiên hiển thị mô tả, nếu không có thì hiển thị danh mục
        if (currentPlace.getDescription() != null && !currentPlace.getDescription().isEmpty()) {
            tvDescription.setText(currentPlace.getDescription());
        } else if (currentPlace.getCategory() != null) {
            tvDescription.setText("Thể loại: " + currentPlace.getCategory());
        }

        // Tải ảnh từ Cloudinary qua Glide, lần lượt ảnh lớn rồi 2 ảnh nhỏ
        List<String> urls = currentPlace.getImageUrls();
        if (urls != null && !urls.isEmpty()) {
            Glide.with(this).load(urls.get(0)).centerCrop().into(imgPlaceBig);
            if (urls.size() > 1) Glide.with(this).load(urls.get(1)).centerCrop().into(imgPlaceSmall1);
            if (urls.size() > 2) Glide.with(this).load(urls.get(2)).centerCrop().into(imgPlaceSmall2);

            // Bấm vào từng ảnh để mở xem full màn hình, đúng vị trí ảnh đã bấm
            imgPlaceBig.setOnClickListener(v -> ImageViewerActivity.open(this, urls, 0));
            if (urls.size() > 1) imgPlaceSmall1.setOnClickListener(v -> ImageViewerActivity.open(this, urls, 1));
            if (urls.size() > 2) imgPlaceSmall2.setOnClickListener(v -> ImageViewerActivity.open(this, urls, 2));
        }
    }

    // Hỏi Firestore xem placeId này có trong favoriteIds của user chưa
    // Rồi cập nhật icon trái tim tương ứng
    private void kiemTraTrangThaiYeuThich() {
        if (currentPlace == null || currentPlace.getPlaceId() == null) return;
        placeRepository.isFavorite(currentPlace.getPlaceId(), isFav -> {
            isFavoriteSelected = isFav;
            capNhatIconTraiTim();
        });
    }

    // Đổi icon và gọi Firestore thêm/xóa tùy theo trạng thái hiện tại
    private void xuLyToggleYeuThich() {
        if (currentPlace == null || currentPlace.getPlaceId() == null) {
            Toast.makeText(this, "Không thể lưu địa điểm này", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isFavoriteSelected) {
            // Chưa yêu thích → thêm vào Firestore
            placeRepository.addFavorite(currentPlace.getPlaceId(), new PlaceRepository.ActionCallback() {
                @Override
                public void onSuccess() {
                    isFavoriteSelected = true;
                    capNhatIconTraiTim();
                    Toast.makeText(PlaceDetailActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String errorMsg) {
                    Toast.makeText(PlaceDetailActivity.this, "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Đã yêu thích → xóa khỏi Firestore
            placeRepository.removeFavorite(currentPlace.getPlaceId(), new PlaceRepository.ActionCallback() {
                @Override
                public void onSuccess() {
                    isFavoriteSelected = false;
                    capNhatIconTraiTim();
                    Toast.makeText(PlaceDetailActivity.this, "Đã bỏ khỏi yêu thích", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String errorMsg) {
                    Toast.makeText(PlaceDetailActivity.this, "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Cập nhật icon và text nút trái tim dựa theo trạng thái isFavoriteSelected
    private void capNhatIconTraiTim() {
        if (isFavoriteSelected) {
            // Trái tim đặc = đã yêu thích
            btnSaveFavorite.setIconResource(R.drawable.ic_heart_filled);
            btnSaveFavorite.setText("Đã thêm");
        } else {
            // Trái tim rỗng = chưa yêu thích
            btnSaveFavorite.setIconResource(R.drawable.ic_heart_outline);
            btnSaveFavorite.setText("Thêm vào yêu thích");
        }
    }

    // Mở dialog "Thêm lịch trình" với địa điểm hiện tại đã gán sẵn, không cho đổi địa điểm
    private void hienThiDialogThemLichTrinh() {
        if (currentPlace == null) {
            Toast.makeText(this, "Không tìm thấy địa điểm để thêm lịch trình", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_schedule, null);

        TextView tvSelectedPlace = dialogView.findViewById(R.id.tvSelectedPlace);
        TextView tvSelectedDateTime = dialogView.findViewById(R.id.tvSelectedDateTime);
        android.widget.Button btnChoosePlace = dialogView.findViewById(R.id.btnChoosePlace);
        android.widget.Button btnChooseDateTime = dialogView.findViewById(R.id.btnChooseDateTime);
        EditText edtScheduleNote = dialogView.findViewById(R.id.edtScheduleNote);

        // Gán sẵn tên địa điểm hiện tại, ẩn nút chọn địa điểm vì đã biết trước
        tvSelectedPlace.setText(currentPlace.getName());
        btnChoosePlace.setVisibility(View.GONE);

        final Calendar[] selectedCalendar = new Calendar[1];

        btnChooseDateTime.setOnClickListener(v -> hienThiDateTimePickerChoLichTrinh(selectedCalendar, tvSelectedDateTime));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thêm vào lịch trình")
                .setView(dialogView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (selectedCalendar[0] == null) {
                    Toast.makeText(this, "Vui lòng chọn ngày giờ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedCalendar[0].getTimeInMillis() < System.currentTimeMillis()) {
                    Toast.makeText(this, "Không thể chọn thời gian trong quá khứ", Toast.LENGTH_SHORT).show();
                    return;
                }

                Schedule schedule = new Schedule();
                schedule.setPlaceId(currentPlace.getPlaceId());
                schedule.setPlaceName(currentPlace.getName());
                schedule.setPlaceAddress(currentPlace.getAddress());
                schedule.setScheduleTimeMillis(selectedCalendar[0].getTimeInMillis());
                schedule.setNote(edtScheduleNote.getText().toString().trim());

                if (currentPlace.getImageUrls() != null && !currentPlace.getImageUrls().isEmpty()) {
                    schedule.setPlaceImageUrl(currentPlace.getImageUrls().get(0));
                }

                scheduleRepository.addSchedule(schedule, new ScheduleRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(PlaceDetailActivity.this, "Đã thêm vào lịch trình", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Toast.makeText(PlaceDetailActivity.this, "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        dialog.show();
    }

    // Hiện DatePicker rồi TimePicker, giống y logic chọn ngày giờ ở màn Lịch trình
    private void hienThiDateTimePickerChoLichTrinh(Calendar[] selectedCalendar, TextView tvSelectedDateTime) {
        Calendar now = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);
                                calendar.set(Calendar.MILLISECOND, 0);

                                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                                    Toast.makeText(this, "Không thể chọn thời gian trong quá khứ", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                selectedCalendar[0] = calendar;
                                tvSelectedDateTime.setText(formatDateTimeLichTrinh(calendar.getTimeInMillis()));
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true
                    );

                    timePickerDialog.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private String formatDateTimeLichTrinh(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    // Cấu hình RecyclerView hiển thị danh sách đánh giá
    private void cauHinhDanhSachReview() {
        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviewsList.setLayoutManager(new LinearLayoutManager(this));
        rvReviewsList.setAdapter(reviewAdapter);
        rvReviewsList.setNestedScrollingEnabled(false);
    }

    // Load danh sách review từ Firestore theo placeId
    private void loadReviews() {
        if (currentPlace == null || currentPlace.getPlaceId() == null || currentPlace.getPlaceId().isEmpty()) {
            return;
        }

        placeRepository.getReviewsByPlaceId(currentPlace.getPlaceId(), reviews -> {
            reviewList.clear();
            reviewList.addAll(reviews);
            reviewAdapter.notifyDataSetChanged();

            capNhatDiemTrungBinhTuReviews();
        });
    }

    // Mở dialog cho người dùng chọn sao và nhập bình luận
    private void hienThiDialogVietDanhGia() {
        if (currentPlace == null || currentPlace.getPlaceId() == null || currentPlace.getPlaceId().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy địa điểm để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để viết đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_write_review, null);

        RatingBar rbInputStar = dialogView.findViewById(R.id.rbInputStar);
        EditText edtCommentInput = dialogView.findViewById(R.id.edtCommentInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Viết đánh giá")
                .setView(dialogView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Gửi", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                int rating = (int) rbInputStar.getRating();
                String comment = edtCommentInput.getText().toString().trim();

                if (rating <= 0) {
                    Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (comment.isEmpty()) {
                    edtCommentInput.setError("Vui lòng nhập nội dung đánh giá");
                    return;
                }

                Review review = new Review();
                review.setPlaceId(currentPlace.getPlaceId());
                review.setRating(rating);
                review.setComment(comment);

                String uid = currentUser.getUid();

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String userName = documentSnapshot.getString("displayName");

                            if (userName == null || userName.trim().isEmpty()) {
                                userName = currentUser.getDisplayName();
                            }

                            if (userName == null || userName.trim().isEmpty()) {
                                userName = "Người dùng AroundUs";
                            }

                            review.setUserName(userName);
                            luuReviewLenFirestore(review, dialog);
                        })
                        .addOnFailureListener(e -> {
                            review.setUserName("Người dùng AroundUs");
                            luuReviewLenFirestore(review, dialog);
                        });
            });
        });

        dialog.show();
    }

    // Lưu review lên Firestore
    private void luuReviewLenFirestore(Review review, AlertDialog dialog) {
        placeRepository.addReview(review, new PlaceRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(PlaceDetailActivity.this, "Đã gửi đánh giá", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadReviews();
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(PlaceDetailActivity.this, "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Tính điểm trung bình từ danh sách review
    private void capNhatDiemTrungBinhTuReviews() {
        if (reviewList == null || reviewList.isEmpty()) {
            return;
        }

        double total = 0;

        for (Review review : reviewList) {
            total += review.getRating();
        }

        double avg = total / reviewList.size();

        // Làm tròn 1 chữ số thập phân
        avg = Math.round(avg * 10.0) / 10.0;

        tvRatingValue.setText(String.valueOf(avg));

        if (currentPlace != null) {
            currentPlace.setAvgRating(avg);

            if (currentPlace.getPlaceId() != null) {
                placeRepository.updatePlaceRating(currentPlace.getPlaceId(), avg);
            }
        }
    }

    // Cấu hình bản đồ sau khi GoogleMap đã sẵn sàng
    @Override
    public void onMapReady(GoogleMap map) {
        googleMapInstance = map;
        googleMapInstance.getUiSettings().setZoomControlsEnabled(true);
        googleMapInstance.getUiSettings().setZoomGesturesEnabled(true);
        googleMapInstance.getUiSettings().setScrollGesturesEnabled(true);

        if (currentPlace != null) {
            // Dùng đúng lat/lng từ model thay vì tọa độ cứng
            LatLng viTriDiaDiem = new LatLng(currentPlace.getLat(), currentPlace.getLng());
            googleMapInstance.addMarker(new MarkerOptions()
                    .position(viTriDiaDiem)
                    .title(currentPlace.getName()));
            googleMapInstance.moveCamera(CameraUpdateFactory.newLatLngZoom(viTriDiaDiem, 15f));
        }
    }

    @Override
    protected void onResume() { super.onResume(); mapViewLocation.onResume(); }

    @Override
    protected void onPause() { super.onPause(); mapViewLocation.onPause(); }

    @Override
    protected void onDestroy() { super.onDestroy(); mapViewLocation.onDestroy(); }

    @Override
    public void onLowMemory() { super.onLowMemory(); mapViewLocation.onLowMemory(); }
}