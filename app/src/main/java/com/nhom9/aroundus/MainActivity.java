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
import com.nhom9.aroundus.ui.profile.ProfileFragment;

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

        // Khởi tạo Cloudinary (không dùng api_secret ở client)
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dfbijq8ur");
            config.put("api_key", "418197113655413");
            MediaManager.init(this, config);
        } catch (Exception ignored) {
            // Tránh crash nếu MediaManager đã init trước đó
        }

        // Mặc định mở Home
        if (savedInstanceState == null) {
            chuyenFragment(new HomeFragment());
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                chuyenFragment(new HomeFragment());
                return true;

            } else if (id == R.id.nav_favorites) {
                chuyenFragment(new FavoritesFragment());
                return true;

            } else if (id == R.id.nav_account) {
                chuyenFragment(new ProfileFragment());
                return true;

            } else if (id == R.id.nav_contribute) {
                android.content.Intent intent = new android.content.Intent(MainActivity.this, com.nhom9.aroundus.ui.place.AddPlaceActivity.class);
                startActivity(intent);
                return true;

            } else if (id == R.id.nav_schedule) {
                Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });
    }

    private void chuyenFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}