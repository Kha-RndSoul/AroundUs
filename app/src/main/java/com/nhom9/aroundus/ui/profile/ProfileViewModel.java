package com.nhom9.aroundus.ui.profile;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom9.aroundus.model.User;
import com.nhom9.aroundus.repository.AuthRepository;

public class ProfileViewModel extends ViewModel {
    private final AuthRepository repo = new AuthRepository();

    public final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);

    public void loadProfile() {
        loadingLiveData.setValue(true);
        repo.getMyProfile(new AuthRepository.ProfileCallback() {
            @Override
            public void onSuccess(User user) {
                loadingLiveData.postValue(false);
                userLiveData.postValue(user);
            }

            @Override
            public void onError(String msg) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(msg);
            }
        });
    }

    public void updateProfile(String displayName, String avatarUrl) {
        loadingLiveData.setValue(true);
        repo.updateProfile(displayName, avatarUrl, new AuthRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                loadProfile();
            }

            @Override
            public void onError(String msg) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(msg);
            }
        });
    }

    public void logout() {
        repo.logout();
    }
}