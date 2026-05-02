package com.pc.mediclaim.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pc.mediclaim.model.Policy;
import com.pc.mediclaim.repository.MediclaimRepository;

public class HomeViewModel extends AndroidViewModel {
    private MediclaimRepository repository;
    private MutableLiveData<Policy> policyLiveData = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new MediclaimRepository(application);
    }

    public LiveData<Policy> getPolicyLiveData() { return policyLiveData; }

    public void loadPolicy(int userId) {
        Policy policy = repository.getPolicyByUserId(userId);
        policyLiveData.setValue(policy);
    }
}
