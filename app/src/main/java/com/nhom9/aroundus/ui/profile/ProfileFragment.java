package com.nhom9.aroundus.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.nhom9.aroundus.R;
import com.nhom9.aroundus.ui.auth.LoginActivity;
import com.nhom9.aroundus.utils.ImageUtils;

public class ProfileFragment extends Fragment {

    private ProfileViewModel vm;
    private ImageView ivAvatar;
    private TextView tvEmail;
    private EditText edtDisplayName, edtAvatarUrl;
    private Button btnPickAvatar, btnSave, btnLogout;
    private ProgressBar progressBar;

    private Uri selectedAvatarUri = null;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    selectedAvatarUri = result.getData().getData();
                    ivAvatar.setImageURI(selectedAvatarUri); // preview local
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvEmail = view.findViewById(R.id.tvEmail);
        edtDisplayName = view.findViewById(R.id.edtDisplayName);
        edtAvatarUrl = view.findViewById(R.id.edtAvatarUrl);
        btnPickAvatar = view.findViewById(R.id.btnPickAvatar);
        btnSave = view.findViewById(R.id.btnSaveProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        progressBar = view.findViewById(R.id.progressBarProfile);

        vm = new ViewModelProvider(this).get(ProfileViewModel.class);

        vm.userLiveData.observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            edtDisplayName.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());
            edtAvatarUrl.setText(user.getAvatarUrl());

            if (!TextUtils.isEmpty(user.getAvatarUrl())) {
                Glide.with(this).load(user.getAvatarUrl()).circleCrop().into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_account_placeholder);
            }
        });

        vm.errorLiveData.observe(getViewLifecycleOwner(),
                msg -> { if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show(); });

        vm.loadingLiveData.observe(getViewLifecycleOwner(),
                isLoading -> {
                    boolean loading = Boolean.TRUE.equals(isLoading);
                    progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
                    btnSave.setEnabled(!loading);
                    btnPickAvatar.setEnabled(!loading);
                });

        btnPickAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> saveProfile());

        btnLogout.setOnClickListener(v -> {
            vm.logout();
            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        vm.loadProfile();
        return view;
    }

    private void saveProfile() {
        String displayName = edtDisplayName.getText().toString().trim();
        if (displayName.isEmpty()) {
            edtDisplayName.setError("Vui lòng nhập tên hiển thị");
            return;
        }

        // Nếu đã chọn ảnh mới => upload trước, rồi update profile
        if (selectedAvatarUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);

            ImageUtils.uploadImage(requireContext(), selectedAvatarUri, new ImageUtils.UploadListener() {
                @Override
                public void onSuccess(String imageUrl) {
                    vm.updateProfile(displayName, imageUrl);
                    selectedAvatarUri = null;
                }

                @Override
                public void onError(String error) {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(requireContext(), "Upload ảnh lỗi: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Không chọn ảnh mới => dùng URL hiện tại trong ô nhập (nếu có)
            String avatarUrl = edtAvatarUrl.getText().toString().trim();
            vm.updateProfile(displayName, avatarUrl.isEmpty() ? null : avatarUrl);
        }
    }
}