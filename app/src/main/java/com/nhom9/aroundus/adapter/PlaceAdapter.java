package com.nhom9.aroundus.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom9.aroundus.R;
import com.nhom9.aroundus.model.Place;

import java.util.ArrayList;
import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<Place> placeList = new ArrayList<>();

    public void setPlaceList(List<Place> placeList) {
        this.placeList = placeList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);

        holder.tvPlaceName.setText(place.getName());
        holder.tvPlaceAddress.setText(place.getAddress());
        holder.tvPlaceCategory.setText(place.getCategory());
        holder.tvPlaceRating.setText("Đánh giá: " + place.getAvgRating());

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(
                    v.getContext(),
                    "Bạn chọn: " + place.getName(),
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {

        TextView tvPlaceName;
        TextView tvPlaceAddress;
        TextView tvPlaceCategory;
        TextView tvPlaceRating;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvPlaceAddress = itemView.findViewById(R.id.tvPlaceAddress);
            tvPlaceCategory = itemView.findViewById(R.id.tvPlaceCategory);
            tvPlaceRating = itemView.findViewById(R.id.tvPlaceRating);
        }
    }
}