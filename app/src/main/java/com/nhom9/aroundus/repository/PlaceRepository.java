package com.nhom9.aroundus.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.nhom9.aroundus.model.Place;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;

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
    // Nếu không có (đang dùng fake data) thì lọc từ fake data theo favIds
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
                                // Nếu Firestore không có dữ liệu thật thì dùng fake data tạm
                                if (result.isEmpty()) {
                                    result = layFakeDataTheoFavIds(favIds);
                                }
                                callback.onResult(result);
                            })
                            .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    public void addPlace(Place place, OnCompleteListener<DocumentReference> listener) {
        String uid = getCurrentUid();
        if (uid != null) {
            place.setCreatedBy(uid);
        }

        db.collection("places")
                .add(place)
                .addOnCompleteListener(listener);
    }

    // Lọc fake data theo danh sách favIds
    // Dùng tạm trong giai đoạn chưa có dữ liệu thật trên Firestore
    private List<Place> layFakeDataTheoFavIds(List<String> favIds) {
        List<Place> fakeAll = taoFakeData();
        List<Place> result = new ArrayList<>();
        for (Place p : fakeAll) {
            if (favIds.contains(p.getPlaceId())) {
                result.add(p);
            }
        }
        return result;
    }

    // Fake data tập trung một chỗ, dùng chung cho cả Repository và HomeFragment
    // Đảm bảo placeId nhất quán giữa các màn hình
    public static List<Place> taoFakeData() {
        List<Place> list = new ArrayList<>();

        Place p1 = new Place();
        p1.setPlaceId("place_001");
        p1.setName("Cơm tấm sinh viên");
        p1.setAddress("Gần Đại học Nông Lâm");
        p1.setCategory("Quán ăn");
        p1.setAvgRating(4.5);
        list.add(p1);

        Place p2 = new Place();
        p2.setPlaceId("place_002");
        p2.setName("Vincom Thủ Đức");
        p2.setAddress("TP. Thủ Đức");
        p2.setCategory("Mua sắm");
        p2.setAvgRating(4.2);
        list.add(p2);

        Place p3 = new Place();
        p3.setPlaceId("place_003");
        p3.setName("Khu vui chơi Suối Tiên");
        p3.setAddress("Xa lộ Hà Nội");
        p3.setCategory("Khu vui chơi");
        p3.setAvgRating(4.7);
        list.add(p3);

        Place p4 = new Place();
        p4.setPlaceId("place_004");
        p4.setName("Highlands Coffee");
        p4.setAddress("Khu vực Thủ Đức");
        p4.setCategory("Cà phê");
        p4.setAvgRating(4.3);
        list.add(p4);

        return list;
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
}