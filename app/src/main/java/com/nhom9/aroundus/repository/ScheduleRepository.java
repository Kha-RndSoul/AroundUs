package com.nhom9.aroundus.repository;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom9.aroundus.model.Schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ScheduleRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private String getCurrentUid() {
        if (auth.getCurrentUser() == null) return null;
        return auth.getCurrentUser().getUid();
    }

    public void addSchedule(Schedule schedule, ActionCallback callback) {
        String uid = getCurrentUid();

        if (uid == null) {
            callback.onError("Bạn cần đăng nhập để tạo lịch trình");
            return;
        }

        DocumentReference docRef = db.collection("schedules").document();

        schedule.setScheduleId(docRef.getId());
        schedule.setUserId(uid);
        schedule.setCreatedAt(Timestamp.now());

        docRef.set(schedule)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getMySchedules(ScheduleListCallback callback) {
        String uid = getCurrentUid();

        if (uid == null) {
            callback.onResult(new ArrayList<>());
            return;
        }

        db.collection("schedules")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Schedule> schedules = new ArrayList<>();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Schedule schedule = doc.toObject(Schedule.class);

                        if (schedule != null) {
                            if (schedule.getScheduleId() == null || schedule.getScheduleId().isEmpty()) {
                                schedule.setScheduleId(doc.getId());
                            }

                            schedules.add(schedule);
                        }
                    }

                    Collections.sort(schedules, Comparator.comparingLong(Schedule::getScheduleTimeMillis));

                    callback.onResult(schedules);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    public void deleteSchedule(String scheduleId, ActionCallback callback) {
        if (scheduleId == null || scheduleId.isEmpty()) {
            callback.onError("Không tìm thấy lịch trình");
            return;
        }

        db.collection("schedules")
                .document(scheduleId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
    public void updateSchedule(Schedule schedule, ActionCallback callback) {
        if (schedule == null || schedule.getScheduleId() == null || schedule.getScheduleId().isEmpty()) {
            callback.onError("Không tìm thấy lịch trình");
            return;
        }

        String uid = getCurrentUid();

        if (uid == null) {
            callback.onError("Bạn cần đăng nhập");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("placeId", schedule.getPlaceId());
        updates.put("placeName", schedule.getPlaceName());
        updates.put("placeAddress", schedule.getPlaceAddress());
        updates.put("placeImageUrl", schedule.getPlaceImageUrl());
        updates.put("scheduleTimeMillis", schedule.getScheduleTimeMillis());
        updates.put("note", schedule.getNote());

        db.collection("schedules")
                .document(schedule.getScheduleId())
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String errorMsg);
    }

    public interface ScheduleListCallback {
        void onResult(List<Schedule> schedules);
    }
}