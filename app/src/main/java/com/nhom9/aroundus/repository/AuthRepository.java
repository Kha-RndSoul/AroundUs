package com.nhom9.aroundus.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.nhom9.aroundus.model.User;
import com.nhom9.aroundus.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // ===== CALLBACKS =====
    public interface ProfileCallback {
        void onSuccess(User user);
        void onError(String msg);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String msg);
    }

    // ===== AUTH =====

    // Đăng ký
    public void register(String name, String email, String password,
                         MutableLiveData<User> userLiveData,
                         MutableLiveData<String> errorLiveData) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        errorLiveData.setValue("Không tạo được tài khoản");
                        return;
                    }

                    // Tạo User object và lưu vào Firestore
                    User user = new User(firebaseUser.getUid(), name, email);

                    db.collection(Constants.COLLECTION_USERS)
                            .document(firebaseUser.getUid())
                            .set(user)
                            .addOnSuccessListener(unused -> userLiveData.setValue(user))
                            .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    // Đăng nhập
    public void login(String email, String password,
                      MutableLiveData<User> userLiveData,
                      MutableLiveData<String> errorLiveData) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        errorLiveData.setValue("Không lấy được thông tin người dùng");
                        return;
                    }

                    // Lấy thông tin User từ Firestore
                    db.collection(Constants.COLLECTION_USERS)
                            .document(firebaseUser.getUid())
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                User user = snapshot.toObject(User.class);

                                // Fallback nếu user doc chưa tồn tại/đọc null
                                if (user == null) {
                                    user = new User(
                                            firebaseUser.getUid(),
                                            firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Người dùng",
                                            firebaseUser.getEmail() != null ? firebaseUser.getEmail() : email
                                    );
                                }

                                userLiveData.setValue(user);
                            })
                            .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    // Đăng xuất
    public void logout() {
        auth.signOut();
    }

    // Kiểm tra đã đăng nhập chưa
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    // Lấy user Firebase hiện tại
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // ===== PROFILE =====

    // Lấy profile của user hiện tại từ Firestore
    public void getMyProfile(ProfileCallback callback) {
        FirebaseUser current = auth.getCurrentUser();
        if (current == null) {
            callback.onError("Chưa đăng nhập");
            return;
        }

        db.collection(Constants.COLLECTION_USERS)
                .document(current.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);

                    // Fallback nếu doc chưa có
                    if (user == null) {
                        user = new User(
                                current.getUid(),
                                current.getDisplayName() != null ? current.getDisplayName() : "Người dùng",
                                current.getEmail() != null ? current.getEmail() : ""
                        );
                    }

                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Cập nhật thông tin profile cơ bản
    public void updateProfile(String displayName, String avatarUrl, ActionCallback callback) {
        FirebaseUser current = auth.getCurrentUser();
        if (current == null) {
            callback.onError("Chưa đăng nhập");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", displayName);

        // Nếu avatarUrl null/empty thì không ghi đè trường avatar
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            updates.put("avatarUrl", avatarUrl.trim());
        }

        db.collection(Constants.COLLECTION_USERS)
                .document(current.getUid())
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}