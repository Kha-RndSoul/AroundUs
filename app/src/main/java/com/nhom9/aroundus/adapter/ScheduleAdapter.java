package com.nhom9.aroundus.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nhom9.aroundus.R;
import com.nhom9.aroundus.model.Schedule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private final List<Schedule> scheduleList = new ArrayList<>();
    private OnScheduleActionListener listener;

    public void setScheduleList(List<Schedule> schedules) {
        scheduleList.clear();

        if (schedules != null) {
            scheduleList.addAll(schedules);
        }

        notifyDataSetChanged();
    }

    public void setOnScheduleActionListener(OnScheduleActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        Schedule schedule = scheduleList.get(position);

        holder.tvSchedulePlaceName.setText(schedule.getPlaceName());
        holder.tvScheduleAddress.setText(schedule.getPlaceAddress());

        String timeText = formatDateTime(schedule.getScheduleTimeMillis());
        holder.tvScheduleTime.setText(timeText);

        String note = schedule.getNote();
        if (note == null || note.trim().isEmpty()) {
            holder.tvScheduleNote.setVisibility(View.GONE);
        } else {
            holder.tvScheduleNote.setVisibility(View.VISIBLE);
            holder.tvScheduleNote.setText(note);
        }

        String imageUrl = schedule.getPlaceImageUrl();

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .centerCrop()
                    .into(holder.ivSchedulePlace);
        } else {
            holder.ivSchedulePlace.setImageResource(R.drawable.ic_default_avatar);
        }

        holder.tvDeleteSchedule.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(schedule);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(schedule);
            }
        });
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    private String formatDateTime(long millis) {
        if (millis <= 0) {
            return "Chưa có thời gian";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    public interface OnScheduleActionListener {
        void onDelete(Schedule schedule);
        void onClick(Schedule schedule);
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSchedulePlace;
        TextView tvSchedulePlaceName, tvScheduleTime, tvScheduleAddress, tvScheduleNote, tvDeleteSchedule;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);

            ivSchedulePlace = itemView.findViewById(R.id.ivSchedulePlace);
            tvSchedulePlaceName = itemView.findViewById(R.id.tvSchedulePlaceName);
            tvScheduleTime = itemView.findViewById(R.id.tvScheduleTime);
            tvScheduleAddress = itemView.findViewById(R.id.tvScheduleAddress);
            tvScheduleNote = itemView.findViewById(R.id.tvScheduleNote);
            tvDeleteSchedule = itemView.findViewById(R.id.tvDeleteSchedule);
        }
    }
}