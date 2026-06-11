package com.nhom9.aroundus.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom9.aroundus.R;
import com.nhom9.aroundus.model.Place;
import com.nhom9.aroundus.repository.PlaceRepository;
import com.nhom9.aroundus.ui.place.PlaceDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<Place> placeList = new ArrayList<>();
    private final PlaceRepository placeRepository = new PlaceRepository();

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
        holder.tvPlaceCategory.setText(place.getCategory());
        holder.tvPlaceMeta.setText("⭐ " + place.getAvgRating() + " · " + place.getAddress());

        // Kiểm tra trạng thái yêu thích từ Firestore và cập nhật icon tim
        if (place.getPlaceId() != null) {
            placeRepository.isFavorite(place.getPlaceId(), isFav -> {
                holder.btnFavorite.setText(isFav ? "♥" : "♡");
            });
        }

        // Nhấn icon tim trên card → toggle yêu thích không cần vào trang chi tiết
        holder.btnFavorite.setOnClickListener(v -> {
            if (place.getPlaceId() == null) {
                Toast.makeText(v.getContext(), "Không thể lưu địa điểm này", Toast.LENGTH_SHORT).show();
                return;
            }
            placeRepository.isFavorite(place.getPlaceId(), isFav -> {
                if (!isFav) {
                    // Chưa yêu thích → thêm vào
                    placeRepository.addFavorite(place.getPlaceId(), new PlaceRepository.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            holder.btnFavorite.setText("♥");
                            Toast.makeText(v.getContext(), "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(String errorMsg) {
                            Toast.makeText(v.getContext(), "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Đã yêu thích → xóa khỏi
                    placeRepository.removeFavorite(place.getPlaceId(), new PlaceRepository.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            holder.btnFavorite.setText("♡");
                            Toast.makeText(v.getContext(), "Đã bỏ khỏi yêu thích", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(String errorMsg) {
                            Toast.makeText(v.getContext(), "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });

        // Nhấn vào card → mở trang chi tiết
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, PlaceDetailActivity.class);
            intent.putExtra("SELECTED_PLACE", place);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {

        TextView tvPlaceName;
        TextView tvPlaceCategory;
        TextView tvPlaceMeta;
        TextView btnFavorite;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaceName     = itemView.findViewById(R.id.tvPlaceName);
            tvPlaceCategory = itemView.findViewById(R.id.tvPlaceCategory);
            tvPlaceMeta     = itemView.findViewById(R.id.tvPlaceMeta);
            btnFavorite     = itemView.findViewById(R.id.btnFavorite);
        }
    }
}