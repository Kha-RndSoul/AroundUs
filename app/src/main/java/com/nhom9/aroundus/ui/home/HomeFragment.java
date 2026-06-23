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
import com.nhom9.aroundus.repository.PlaceRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvPlaces;
    private PlaceAdapter placeAdapter;

    private EditText edtSearch;
    private TextView btnFilter;

    private PlaceRepository placeRepository;

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

            if (allPlaces.isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        "Chưa có địa điểm nào để hiển thị",
                        Toast.LENGTH_SHORT
                ).show();
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
            String name = safeLower(place.getName());
            String address = safeLower(place.getAddress());
            String category = safeLower(place.getCategory());

            boolean matchCategory =
                    currentCategory.equals("Tất cả")
                            || currentCategory.equals(place.getCategory());

            boolean matchSearch =
                    name.contains(keyword)
                            || address.contains(keyword)
                            || category.contains(keyword);

            if (matchCategory && matchSearch) {
                filteredList.add(place);
            }
        }

        placeAdapter.setPlaceList(filteredList);
    }

    private String safeLower(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (placeAdapter != null) {
            placeAdapter.notifyDataSetChanged();
        }
    }
}