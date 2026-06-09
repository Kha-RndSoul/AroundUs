package com.nhom9.aroundus.adapter;

import android.content.Context; // Bắt buộc thêm để dùng Intent
import android.content.Intent;  // Bắt buộc thêm để dùng Intent
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom9.aroundus.R;
import com.nhom9.aroundus.model.Place;
import com.nhom9.aroundus.ui.place.PlaceDetailActivity; // Thêm import màn hình chi tiết

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
        holder.tvPlaceCategory.setText(place.getCategory());
        holder.tvPlaceMeta.setText("⭐ " + place.getAvgRating() + " · " + place.getAddress());

        // CHỈNH SỬA Ở ĐÂY: Thay Toast bằng lệnh chuyển trang
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();

            // 1. Tạo Intent trỏ tới màn hình PlaceDetailActivity
            Intent intent = new Intent(context, PlaceDetailActivity.class);

            // 2. Gửi nguyên cái object Place mà bạn vừa click vào sang màn chi tiết
            // (Đảm bảo class Place của bạn đã có chữ "implements Serializable" như các bước trước)
            intent.putExtra("SELECTED_PLACE", place);

            // 3. Thực hiện mở màn hình
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

            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvPlaceCategory = itemView.findViewById(R.id.tvPlaceCategory);
            tvPlaceMeta = itemView.findViewById(R.id.tvPlaceMeta);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}