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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom9.aroundus.R;
import com.nhom9.aroundus.adapter.PlaceAdapter;
import com.nhom9.aroundus.model.Place;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvPlaces;
    private PlaceAdapter placeAdapter;

    private EditText edtSearch;
    private TextView btnFilter;

    private final List<Place> allPlaces = new ArrayList<>();
    private String currentCategory = "Tất cả";

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

        rvPlaces = view.findViewById(R.id.rvPlaces);
        edtSearch = view.findViewById(R.id.edtSearch);
        btnFilter = view.findViewById(R.id.btnFilter);

        rvPlaces.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        placeAdapter = new PlaceAdapter();
        rvPlaces.setAdapter(placeAdapter);

        createFakeData();
        placeAdapter.setPlaceList(allPlaces);

        setupSearch();
        setupFilter();

        return view;
    }

    private void createFakeData() {
        allPlaces.clear();

        Place p1 = new Place();
        p1.setPlaceId("place_001");
        p1.setName("Cơm tấm sinh viên");
        p1.setAddress("Gần Đại học Nông Lâm");
        p1.setCategory("Quán ăn");
        p1.setAvgRating(4.5);
        allPlaces.add(p1);

        Place p2 = new Place();
        p2.setPlaceId("place_002");
        p2.setName("Vincom Thủ Đức");
        p2.setAddress("TP. Thủ Đức");
        p2.setCategory("Mua sắm");
        p2.setAvgRating(4.2);
        allPlaces.add(p2);

        Place p3 = new Place();
        p2.setPlaceId("place_003");
        p3.setName("Khu vui chơi Suối Tiên");
        p3.setAddress("Xa lộ Hà Nội");
        p3.setCategory("Khu vui chơi");
        p3.setAvgRating(4.7);
        allPlaces.add(p3);

        Place p4 = new Place();
        p2.setPlaceId("place_004");
        p4.setName("Highlands Coffee");
        p4.setAddress("Khu vực Thủ Đức");
        p4.setCategory("Cà phê");
        p4.setAvgRating(4.3);
        allPlaces.add(p4);
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
            }
        });
    }

    private void setupFilter() {
        btnFilter.setOnClickListener(v -> {
            String[] categories = {
                    "Tất cả",
                    "Quán ăn",
                    "Cà phê",
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
        String keyword = edtSearch.getText().toString().trim().toLowerCase();

        List<Place> filteredList = new ArrayList<>();

        for (Place place : allPlaces) {
            boolean matchCategory =
                    currentCategory.equals("Tất cả")
                            || currentCategory.equals(place.getCategory());

            boolean matchSearch =
                    place.getName().toLowerCase().contains(keyword)
                            || place.getAddress().toLowerCase().contains(keyword)
                            || place.getCategory().toLowerCase().contains(keyword);

            if (matchCategory && matchSearch) {
                filteredList.add(place);
            }
        }

        placeAdapter.setPlaceList(filteredList);
    }
    // Refresh lại trạng thái tim trên các card mỗi khi quay về trang chủ
    @Override
    public void onResume() {
        super.onResume();
        if (placeAdapter != null) {
            placeAdapter.notifyDataSetChanged();
        }
    }
}