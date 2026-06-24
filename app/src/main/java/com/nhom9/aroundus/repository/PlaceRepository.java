package com.nhom9.aroundus.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.nhom9.aroundus.model.Place;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.Timestamp;
import com.nhom9.aroundus.model.Review;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Lấy uid của người dùng đang đăng nhập
    private String getCurrentUid() {
        if (auth.getCurrentUser() == null) return null;
        return auth.getCurrentUser().getUid();
    }

    // Kiểm tra một địa điểm có đang được yêu thích không
    // Kết quả trả về qua callback boolean
    public void isFavorite(String placeId, FavoriteCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) {
            callback.onResult(false);
            return;
        }
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        List<String> favs = (List<String>) snapshot.get("favoriteIds");
                        boolean result = favs != null && favs.contains(placeId);
                        callback.onResult(result);
                    } else {
                        callback.onResult(false);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(false));
    }

    // Thêm địa điểm vào danh sách yêu thích
    // Dùng set+merge để tự tạo document user nếu chưa tồn tại trên Firestore
    public void addFavorite(String placeId, ActionCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) { callback.onError("Chưa đăng nhập"); return; }

        Map<String, Object> data = new HashMap<>();
        data.put("favoriteIds", FieldValue.arrayUnion(placeId));

        db.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Xóa địa điểm khỏi danh sách yêu thích
    // Dùng set+merge để tránh lỗi nếu document user chưa tồn tại
    public void removeFavorite(String placeId, ActionCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) { callback.onError("Chưa đăng nhập"); return; }

        Map<String, Object> data = new HashMap<>();
        data.put("favoriteIds", FieldValue.arrayRemove(placeId));

        db.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Lấy toàn bộ danh sách địa điểm yêu thích của user
    // Ưu tiên lấy từ Firestore collection "places"
    public void getFavoritePlaces(PlaceListCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) { callback.onResult(new ArrayList<>()); return; }

        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }
                    List<String> favIds = (List<String>) snapshot.get("favoriteIds");
                    if (favIds == null || favIds.isEmpty()) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }
                    // Thử lấy từ Firestore collection "places" trước
                    db.collection("places")
                            .whereIn("placeId", favIds)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                List<Place> result = new ArrayList<>();
                                for (var doc : querySnapshot.getDocuments()) {
                                    Place p = doc.toObject(Place.class);
                                    if (p != null) result.add(p);
                                }
                                callback.onResult(result);
                            })
                            .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    public void addPlace(Place place, OnCompleteListener<Void> listener) {
        // DocumentReference trống để lấy ID ngẫu nhiên
        DocumentReference newDocRef = db.collection("places").document();

        // Lấy ID vừa sinh ra bỏ vào field placeId của object Place
        place.setPlaceId(newDocRef.getId());

        // Gán thêm người tạo (createdBy)
        String uid = getCurrentUid();
        if (uid != null) {
            place.setCreatedBy(uid);
        }

        // Lưu object đã đầy đủ ID lên Document
        newDocRef.set(place).addOnCompleteListener(listener);
    }
    public void getAllPlaces(PlaceListCallback callback) {
        db.collection("places")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Place> places = new ArrayList<>();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Place place = doc.toObject(Place.class);

                        if (place != null) {
                            if (place.getPlaceId() == null || place.getPlaceId().isEmpty()) {
                                place.setPlaceId(doc.getId());
                            }

                            places.add(place);
                        }
                    }

                    callback.onResult(places);
                });
    }
    // Thêm đánh giá mới
    public void addReview(Review review, ActionCallback callback) {
        String uid = getCurrentUid();

        if (uid == null) {
            callback.onError("Bạn cần đăng nhập để viết đánh giá");
            return;
        }

        DocumentReference docRef = db.collection("reviews").document();

        review.setReviewId(docRef.getId());
        review.setUserId(uid);
        review.setCreatedAt(Timestamp.now());

        docRef.set(review)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Lấy danh sách đánh giá theo placeId
    public void getReviewsByPlaceId(String placeId, ReviewListCallback callback) {
        db.collection("reviews")
                .whereEqualTo("placeId", placeId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Review review = doc.toObject(Review.class);

                        if (review != null) {
                            if (review.getReviewId() == null || review.getReviewId().isEmpty()) {
                                review.setReviewId(doc.getId());
                            }

                            reviews.add(review);
                        }
                    }

                    callback.onResult(reviews);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    // Cập nhật điểm trung bình của địa điểm
    public void updatePlaceRating(String placeId, double avgRating) {
        if (placeId == null || placeId.isEmpty()) return;

        db.collection("places")
                .document(placeId)
                .update("avgRating", avgRating);
    }
    // Interface callback trả về boolean (dùng cho isFavorite)
    public interface FavoriteCallback {
        void onResult(boolean isFavorite);
    }

    // Interface callback cho các thao tác thêm/xóa
    public interface ActionCallback {
        void onSuccess();
        void onError(String errorMsg);
    }

    // Interface callback trả về danh sách Place
    public interface PlaceListCallback {
        void onResult(List<Place> places);
    }
    public interface ReviewListCallback {
        void onResult(List<Review> reviews);
    }
}