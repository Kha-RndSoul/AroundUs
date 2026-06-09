package com.nhom9.aroundus.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.nhom9.aroundus.MainActivity;
import com.nhom9.aroundus.repository.AuthRepository;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthRepository authRepository = new AuthRepository();

        if (authRepository.isLoggedIn()) {
            // Đã đăng nhập → vào MainActivity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // Chưa đăng nhập → vào LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}