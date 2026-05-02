package com.pc.mediclaim.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pc.mediclaim.model.User;
import com.pc.mediclaim.repository.MediclaimRepository;

public class LoginViewModel extends AndroidViewModel {
    private MediclaimRepository repository;
    private MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new MediclaimRepository(application);
    }

    public LiveData<User> getUserLiveData() { return userLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            errorLiveData.setValue("Please enter all fields");
            return;
        }

        User user = repository.login(email, password);
        if (user != null) {
            userLiveData.setValue(user);
        } else {
            errorLiveData.setValue("Invalid credentials");
        }
    }
}
