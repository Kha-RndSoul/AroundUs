package com.nhom9.aroundus.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom9.aroundus.R;
import com.nhom9.aroundus.adapter.PlaceAdapter;
import com.nhom9.aroundus.model.Place;
import com.nhom9.aroundus.repository.PlaceRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nhom9.aroundus.ui.place.PlaceDetailActivity;
import java.text.Normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private RecyclerView rvPlaces;
    private PlaceAdapter placeAdapter;

    private EditText edtSearch;
    private TextView btnFilter;

    private PlaceRepository placeRepository;

    private final List<Place> allPlaces = new ArrayList<>();
    private String currentCategory = "Tất cả";
    private ImageView ivAvatar;
    private TextView tvUserName;
    private TextView tvGreeting;
    private MapView homeMapView;
    private GoogleMap googleMap;
    private TextView btnZoomIn;
    private TextView btnZoomOut;
    private LatLngBounds currentMapBounds;
    //private boolean hasMovedCameraToFirstPlace = false;
    private static final LatLng HCM_CENTER = new LatLng(10.7769, 106.7009);
    //private boolean hasAdjustedInitialCamera = false;

    private final Map<Marker, Place> markerPlaceMap = new HashMap<>();

    public HomeFragment() {
        // Constructor rỗng bắt buộc cho Fragment
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        homeMapView = view.findViewById(R.id.homeMapView);
        homeMapView.onCreate(savedInstanceState);
        homeMapView.getMapAsync(this);
        btnZoomIn = view.findViewById(R.id.btnZoomIn);
        btnZoomOut = view.findViewById(R.id.btnZoomOut);
        rvPlaces = view.findViewById(R.id.rvPlaces);
        edtSearch = view.findViewById(R.id.edtSearch);
        btnFilter = view.findViewById(R.id.btnFilter);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvGreeting = view.findViewById(R.id.tvGreeting);

        loadCurrentUserInfo();

        rvPlaces.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        placeAdapter = new PlaceAdapter();
        rvPlaces.setAdapter(placeAdapter);

        placeRepository = new PlaceRepository();

        setupSearch();
        setupFilter();

        loadPlaces();

        return view;
    }

    private void loadPlaces() {
        placeRepository.getAllPlaces(places -> {
            allPlaces.clear();

            if (places != null) {
                allPlaces.addAll(places);
            }

            applyFilter();
    /*
            if (!hasAdjustedInitialCamera) {
                hasAdjustedInitialCamera = true;
                moveCameraToPlaces(allPlaces);
            }
    */
            if (allPlaces.isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        "Chưa có địa điểm nào để hiển thị",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
    private void setupZoomButtons() {
        btnZoomIn.setOnClickListener(v -> {
            if (googleMap != null) {
                googleMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        btnZoomOut.setOnClickListener(v -> {
            if (googleMap != null) {
                googleMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý
            }
        });
    }

    private void setupFilter() {
        btnFilter.setOnClickListener(v -> {
            String[] categories = {
                    "Tất cả",
                    "Quán ăn",
                    "Quán cf",
                    "Mua sắm",
                    "Khu vui chơi"
            };

            new AlertDialog.Builder(requireContext())
                    .setTitle("Chọn danh mục")
                    .setItems(categories, (dialog, which) -> {
                        currentCategory = categories[which];
                        applyFilter();

                        Toast.makeText(
                                requireContext(),
                                "Đang lọc: " + currentCategory,
                                Toast.LENGTH_SHORT
                        ).show();
                    })
                    .show();
        });
    }

    private void applyFilter() {
        String keyword = normalizeSearchText(edtSearch.getText().toString());

        List<Place> filteredList = new ArrayList<>();

        for (Place place : allPlaces) {
            String category = safeString(place.getCategory());

            boolean matchCategory =
                    currentCategory.equals("Tất cả")
                            || currentCategory.equals(category);

            String searchContent = buildSearchContent(place);

            boolean matchSearch =
                    keyword.isEmpty()
                            || searchContent.contains(keyword);

            boolean matchMapBounds = isPlaceInCurrentMapBounds(place);

            if (matchCategory && matchSearch && matchMapBounds) {
                filteredList.add(place);
            }
        }

        placeAdapter.setPlaceList(filteredList);
        updateMapMarkers(filteredList);
    }

    private void loadCurrentUserInfo() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            updateHeaderUser("Khách", null);
            return;
        }

        String uid = currentUser.getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    String displayName = null;
                    String avatarUrl = null;

                    if (snapshot.exists()) {
                        displayName = snapshot.getString("displayName");

                        if (isBlank(displayName)) {
                            displayName = snapshot.getString("name");
                        }

                        if (isBlank(displayName)) {
                            displayName = snapshot.getString("username");
                        }

                        avatarUrl = snapshot.getString("avatarUrl");

                        if (isBlank(avatarUrl)) {
                            avatarUrl = snapshot.getString("photoUrl");
                        }

                        if (isBlank(avatarUrl)) {
                            avatarUrl = snapshot.getString("avatar");
                        }
                    }

                    if (isBlank(displayName)) {
                        displayName = currentUser.getDisplayName();
                    }

                    if (isBlank(displayName) && currentUser.getEmail() != null) {
                        displayName = currentUser.getEmail().split("@")[0];
                    }

                    if (isBlank(displayName)) {
                        displayName = "Người dùng";
                    }

                    if (isBlank(avatarUrl) && currentUser.getPhotoUrl() != null) {
                        avatarUrl = currentUser.getPhotoUrl().toString();
                    }

                    updateHeaderUser(displayName, avatarUrl);
                })
                .addOnFailureListener(e -> {
                    String fallbackName = currentUser.getDisplayName();
                    String fallbackAvatarUrl = null;

                    if (isBlank(fallbackName) && currentUser.getEmail() != null) {
                        fallbackName = currentUser.getEmail().split("@")[0];
                    }

                    if (isBlank(fallbackName)) {
                        fallbackName = "Người dùng";
                    }

                    if (currentUser.getPhotoUrl() != null) {
                        fallbackAvatarUrl = currentUser.getPhotoUrl().toString();
                    }

                    updateHeaderUser(fallbackName, fallbackAvatarUrl);
                });
    }

    private void updateHeaderUser(String displayName, String avatarUrl) {
        tvUserName.setText("Xin chào, " + displayName);
        tvGreeting.setText("Hôm nay bạn muốn khám phá nơi nào?");

        if (!isBlank(avatarUrl)) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeSearchText(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replace("đ", "d").replace("Đ", "D");

        return normalized.toLowerCase().trim();
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }
    private String buildSearchContent(Place place) {
        if (place == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        builder.append(safeString(place.getName())).append(" ");
        builder.append(safeString(place.getAddress())).append(" ");
        builder.append(safeString(place.getCategory())).append(" ");
        builder.append(safeString(place.getDescription())).append(" ");
        builder.append(safeString(place.getOpeningHour())).append(" ");
        builder.append(safeString(place.getClosingHour())).append(" ");
        builder.append(safeString(place.getPlaceId())).append(" ");
        builder.append(place.getAvgRating());

        return normalizeSearchText(builder.toString());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HCM_CENTER, 11.5f));
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        setupZoomButtons();

        googleMap.setOnCameraIdleListener(() -> {
            if (googleMap == null) {
                return;
            }

            currentMapBounds = googleMap.getProjection()
                    .getVisibleRegion()
                    .latLngBounds;

            applyFilter();
        });

        googleMap.setOnMarkerClickListener(marker -> {
            Place selectedPlace = markerPlaceMap.get(marker);

            if (selectedPlace != null) {
                Intent intent = new Intent(requireContext(), PlaceDetailActivity.class);
                intent.putExtra("SELECTED_PLACE", selectedPlace);
                startActivity(intent);
            }

            return true;
        });

        updateMapMarkers(allPlaces);
    }

    private void updateMapMarkers(List<Place> places) {
        if (googleMap == null || places == null) {
            return;
        }

        googleMap.clear();
        markerPlaceMap.clear();

        int index = 1;

        for (Place place : places) {
            if (!isValidLatLng(place)) {
                continue;
            }

            LatLng position = new LatLng(place.getLat(), place.getLng());

            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(place.getName())
                    .snippet(place.getCategory())
                    .icon(BitmapDescriptorFactory.fromBitmap(
                            createCircleMarker(String.valueOf(index))
                    )));

            if (marker != null) {
                markerPlaceMap.put(marker, place);
            }

            index++;
        }
    }


    private Bitmap createCircleMarker(String text) {
        int size = 72;

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.rgb(46, 125, 50));

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        canvas.drawCircle(size / 2f, size / 2f, size / 2.4f, circlePaint);

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float textY = size / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f;

        canvas.drawText(text, size / 2f, textY, textPaint);

        return bitmap;
    }

    private boolean isPlaceInCurrentMapBounds(Place place) {
        if (!isValidLatLng(place)) {
            return false;
        }

        if (currentMapBounds == null) {
            return true;
        }

        LatLng position = new LatLng(place.getLat(), place.getLng());
        return currentMapBounds.contains(position);
    }

    private boolean isValidLatLng(Place place) {
        if (place == null) {
            return false;
        }

        double lat = place.getLat();
        double lng = place.getLng();

        return lat >= -90 && lat <= 90
                && lng >= -180 && lng <= 180
                && !(lat == 0 && lng == 0);
    }
    private void moveCameraToPlaces(List<Place> places) {
        if (googleMap == null || places == null || places.isEmpty()) {
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int validPlaceCount = 0;

        for (Place place : places) {
            if (!isValidLatLng(place)) {
                continue;
            }

            LatLng position = new LatLng(place.getLat(), place.getLng());
            builder.include(position);
            validPlaceCount++;
        }

        if (validPlaceCount == 0) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HCM_CENTER, 11.5f));
            return;
        }

        if (validPlaceCount == 1) {
            for (Place place : places) {
                if (isValidLatLng(place)) {
                    LatLng position = new LatLng(place.getLat(), place.getLng());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14f));
                    return;
                }
            }
        }

        LatLngBounds bounds = builder.build();

        homeMapView.post(() -> {
            int padding = 80;
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (homeMapView != null) {
            homeMapView.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (homeMapView != null) {
            homeMapView.onResume();
        }

        if (placeRepository != null) {
            loadPlaces();
        }
    }

    @Override
    public void onPause() {
        if (homeMapView != null) {
            homeMapView.onPause();
        }

        super.onPause();
    }

    @Override
    public void onStop() {
        if (homeMapView != null) {
            homeMapView.onStop();
        }

        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (homeMapView != null) {
            homeMapView.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (homeMapView != null) {
            homeMapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (homeMapView != null) {
            homeMapView.onSaveInstanceState(outState);
        }
    }
}