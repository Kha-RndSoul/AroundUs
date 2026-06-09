package com.nhom9.aroundus.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.nhom9.aroundus.MainActivity;
import com.nhom9.aroundus.R;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe kết quả đăng ký
        authViewModel.userLiveData.observe(this, user -> {
            if (user != null) {
                // Đăng ký thành công → vào MainActivity
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        // Observe lỗi
        authViewModel.errorLiveData.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe loading
        authViewModel.loadingLiveData.observe(this, isLoading -> {
            btnRegister.setEnabled(!isLoading);
        });

        // Xử lý nút Register
        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!validate(name, email, password)) return;

            authViewModel.register(name, email, password);
        });

        // Quay về màn hình Login
        tvLogin.setOnClickListener(v -> finish());
    }

    private boolean validate(String name, String email, String password) {
        if (name.isEmpty()) {
            etName.setError("Vui lòng nhập họ và tên");
            return false;
        }
        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            return false;
        }
        if (password.length() < 6) {
            etPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            return false;
        }
        return true;
    }
}