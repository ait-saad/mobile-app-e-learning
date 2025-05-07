package com.projet.skilllearn.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.projet.skilllearn.model.User;
import com.projet.skilllearn.repository.UserRepository;

public class AuthViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AuthViewModel() {
        userRepository = new UserRepository();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void registerUser(String name, String email, String password) {
        isLoading.setValue(true);

        userRepository.registerUser(name, email, password, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser.setValue(user);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void loginUser(String email, String password) {
        isLoading.setValue(true);

        userRepository.loginUser(email, password, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser.setValue(user);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void logoutUser() {
        userRepository.logoutUser();
        currentUser.setValue(null);
    }
}