package com.nhom9.aroundus.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom9.aroundus.R;
import com.nhom9.aroundus.adapter.PlaceAdapter;
import com.nhom9.aroundus.model.Place;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvPlaces;
    private PlaceAdapter placeAdapter;

    private List<Place> allPlaces = new ArrayList<>();

    public HomeFragment() {
        // Bắt buộc cần constructor rỗng cho Fragment
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
        rvPlaces.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        placeAdapter = new PlaceAdapter();
        rvPlaces.setAdapter(placeAdapter);

        Button btnAll = view.findViewById(R.id.btnAll);
        Button btnFood = view.findViewById(R.id.btnFood);
        Button btnShopping = view.findViewById(R.id.btnShopping);
        Button btnEntertainment = view.findViewById(R.id.btnEntertainment);

        createFakeData();
        placeAdapter.setPlaceList(allPlaces);

        btnAll.setOnClickListener(v -> placeAdapter.setPlaceList(allPlaces));
        btnFood.setOnClickListener(v -> filterByCategory("Quán ăn"));
        btnShopping.setOnClickListener(v -> filterByCategory("Mua sắm"));
        btnEntertainment.setOnClickListener(v -> filterByCategory("Khu vui chơi"));

        return view;
    }

    private void createFakeData() {
        allPlaces.clear();

        Place p1 = new Place();
        p1.setName("Cơm tấm sinh viên");
        p1.setAddress("Gần Đại học Nông Lâm");
        p1.setCategory("Quán ăn");
        p1.setAvgRating(4.5);
        allPlaces.add(p1);

        Place p2 = new Place();
        p2.setName("Vincom Thủ Đức");
        p2.setAddress("TP. Thủ Đức");
        p2.setCategory("Mua sắm");
        p2.setAvgRating(4.2);
        allPlaces.add(p2);

        Place p3 = new Place();
        p3.setName("Khu vui chơi Suối Tiên");
        p3.setAddress("Xa lộ Hà Nội");
        p3.setCategory("Khu vui chơi");
        p3.setAvgRating(4.7);
        allPlaces.add(p3);
    }

    private void filterByCategory(String category) {
        List<Place> filteredList = new ArrayList<>();

        for (Place place : allPlaces) {
            if (category.equals(place.getCategory())) {
                filteredList.add(place);
            }
        }

        placeAdapter.setPlaceList(filteredList);
    }
}