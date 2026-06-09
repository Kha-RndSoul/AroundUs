package com.nhom9.aroundus.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom9.aroundus.model.User;
import com.nhom9.aroundus.utils.Constants;

public class AuthRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Đăng ký
    public void register(String name, String email, String password, MutableLiveData<User> userLiveData, MutableLiveData<String> errorLiveData) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) return;

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
    public void login(String email, String password, MutableLiveData<User> userLiveData, MutableLiveData<String> errorLiveData) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) return;

                    // Lấy thông tin User từ Firestore
                    db.collection(Constants.COLLECTION_USERS)
                            .document(firebaseUser.getUid())
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                User user = snapshot.toObject(User.class);
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

    // Lấy user hiện tại
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
}