package com.nhom9.aroundus.ui.auth;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom9.aroundus.model.User;
import com.nhom9.aroundus.repository.AuthRepository;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    public final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    public AuthViewModel() {
        authRepository = new AuthRepository();
    }

    // Đăng ký
    public void register(String name, String email, String password) {
        loadingLiveData.setValue(true);
        authRepository.register(name, email, password, userLiveData, errorLiveData);
        loadingLiveData.setValue(false);
    }

    // Đăng nhập
    public void login(String email, String password) {
        loadingLiveData.setValue(true);
        authRepository.login(email, password, userLiveData, errorLiveData);
        loadingLiveData.setValue(false);
    }

    // Đăng xuất
    public void logout() {
        authRepository.logout();
    }

    // Kiểm tra đã đăng nhập chưa
    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }
}