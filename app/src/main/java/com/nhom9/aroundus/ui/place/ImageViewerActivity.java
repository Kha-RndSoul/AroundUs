package com.nhom9.aroundus.ui.place;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.nhom9.aroundus.R;
import com.nhom9.aroundus.adapter.ImagePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ImageViewerActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URLS = "EXTRA_IMAGE_URLS";
    public static final String EXTRA_START_POSITION = "EXTRA_START_POSITION";

    // Hàm tiện ích để mở Activity này từ bất kỳ đâu, truyền sẵn danh sách ảnh + ảnh bắt đầu
    public static void open(Context context, List<String> imageUrls, int startPosition) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putStringArrayListExtra(EXTRA_IMAGE_URLS, new ArrayList<>(imageUrls));
        intent.putExtra(EXTRA_START_POSITION, startPosition);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        ViewPager2 viewPager = findViewById(R.id.viewPagerImages);
        TextView tvCounter = findViewById(R.id.tvImageCounter);
        ImageButton btnClose = findViewById(R.id.btnCloseViewer);

        List<String> imageUrls = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
        int startPosition = getIntent().getIntExtra(EXTRA_START_POSITION, 0);

        if (imageUrls == null || imageUrls.isEmpty()) {
            finish();
            return;
        }

        ImagePagerAdapter adapter = new ImagePagerAdapter(imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startPosition, false);

        tvCounter.setText((startPosition + 1) + " / " + imageUrls.size());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tvCounter.setText((position + 1) + " / " + imageUrls.size());
            }
        });

        btnClose.setOnClickListener(v -> finish());
    }
}