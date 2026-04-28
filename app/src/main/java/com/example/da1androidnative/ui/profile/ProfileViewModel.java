package com.example.da1androidnative.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.da1androidnative.data.ProfileRepository;
import com.example.da1androidnative.data.model.UpdateUserProfileRequest;
import com.example.da1androidnative.data.model.UserProfileResponse;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final ProfileRepository profileRepository;

    private final MutableLiveData<UserProfileResponse> profileData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();

    @Inject
    public ProfileViewModel(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public LiveData<UserProfileResponse> getProfileData() {
        return profileData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public void loadProfile() {
        isLoading.setValue(true);

        profileRepository.getMyProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(
                    Call<UserProfileResponse> call,
                    Response<UserProfileResponse> response
            ) {
                isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    profileData.setValue(response.body());
                } else {
                    errorMessage.setValue("Error al cargar el perfil");
                }
            }

            @Override
            public void onFailure(
                    Call<UserProfileResponse> call,
                    Throwable t
            ) {
                isLoading.setValue(false);
                errorMessage.setValue("Error de conexión");
            }
        });
    }

    public void updateProfile(UpdateUserProfileRequest request) {
        isLoading.setValue(true);

        profileRepository.updateMyProfile(request)
                .enqueue(new Callback<UserProfileResponse>() {

                    @Override
                    public void onResponse(
                            Call<UserProfileResponse> call,
                            Response<UserProfileResponse> response
                    ) {
                        isLoading.setValue(false);

                        if (response.isSuccessful() && response.body() != null) {
                            profileData.setValue(response.body());
                            updateSuccess.setValue(true);
                        } else {
                            errorMessage.setValue("Error al actualizar el perfil");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<UserProfileResponse> call,
                            Throwable t
                    ) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Error de conexión");
                    }
                });
    }
}