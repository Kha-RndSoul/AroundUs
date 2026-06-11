package com.nhom9.aroundus;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.cloudinary.android.MediaManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nhom9.aroundus.ui.favorites.FavoritesFragment;
import com.nhom9.aroundus.ui.home.HomeFragment;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo Cloudinary
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dfbijq8ur");
        config.put("api_key", "418197113655413");
        config.put("api_secret", "t871XwlMIj_N066Z-UbDvoQxfYI");
        MediaManager.init(this, config);

        // Hiển thị HomeFragment mặc định khi app khởi động
        if (savedInstanceState == null) {
            chuyenFragment(new HomeFragment());
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Tab Khám phá — màn hình danh sách địa điểm
                chuyenFragment(new HomeFragment());
                return true;

            } else if (id == R.id.nav_favorites) {
                // Tab Yêu thích — màn hình danh sách địa điểm đã lưu
                chuyenFragment(new FavoritesFragment());
                return true;

            } else if (id == R.id.nav_schedule
                    || id == R.id.nav_contribute
                    || id == R.id.nav_account) {
                // Các tab Lịch trình, Đóng góp, Tài khoản chưa implement
                Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });
    }

    // Hàm chung để thay fragment vào fragmentContainer
    private void chuyenFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}