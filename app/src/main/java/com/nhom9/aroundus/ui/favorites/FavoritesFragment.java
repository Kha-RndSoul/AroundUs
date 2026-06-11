package com.nhom9.aroundus.ui.favorites;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom9.aroundus.R;
import com.nhom9.aroundus.adapter.PlaceAdapter;
import com.nhom9.aroundus.repository.PlaceRepository;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private PlaceAdapter placeAdapter;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    // Handler để đảm bảo cập nhật UI luôn chạy trên Main Thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final PlaceRepository placeRepository = new PlaceRepository();

    public FavoritesFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        rvFavorites = view.findViewById(R.id.rvFavorites);
        tvEmpty     = view.findViewById(R.id.tvEmpty);
        progressBar = view.findViewById(R.id.progressBar);

        rvFavorites.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        placeAdapter = new PlaceAdapter();
        rvFavorites.setAdapter(placeAdapter);

        taiDanhSachYeuThich();

        return view;
    }

    // Gọi Firestore lấy danh sách địa điểm yêu thích rồi đổ vào RecyclerView
    // Dùng mainHandler để đảm bảo thao tác UI chạy đúng trên Main Thread
    private void taiDanhSachYeuThich() {
        mainHandler.post(() -> {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        });

        placeRepository.getFavoritePlaces(places -> mainHandler.post(() -> {
            // Kiểm tra fragment còn gắn vào activity không trước khi cập nhật UI
            if (!isAdded()) return;

            progressBar.setVisibility(View.GONE);

            if (places == null || places.isEmpty()) {
                // Không có địa điểm yêu thích nào thì hiện thông báo trống
                tvEmpty.setVisibility(View.VISIBLE);
                rvFavorites.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvFavorites.setVisibility(View.VISIBLE);
                placeAdapter.setPlaceList(places);
            }
        }));
    }

    // Reload lại danh sách mỗi khi quay lại tab Yêu thích
    @Override
    public void onResume() {
        super.onResume();
        taiDanhSachYeuThich();
    }
}