package com.pc.mediclaim.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pc.mediclaim.model.User;
import com.pc.mediclaim.repository.MediclaimRepository;
import java.util.List;

public class AdminUsersViewModel extends AndroidViewModel {
    private MediclaimRepository repository;
    private MutableLiveData<List<User>> pendingUsersLiveData = new MutableLiveData<>();
    private MutableLiveData<String> statusMessage = new MutableLiveData<>();

    public AdminUsersViewModel(@NonNull Application application) {
        super(application);
        repository = new MediclaimRepository(application);
    }

    public LiveData<List<User>> getPendingUsersLiveData() { return pendingUsersLiveData; }
    public LiveData<String> getStatusMessage() { return statusMessage; }

    public void loadPendingUsers() {
        pendingUsersLiveData.setValue(repository.getPendingUsers());
    }

    public void approveUser(int userId, String memberId, String policyType, double sumInsured) {
        int result = repository.approveUser(userId, memberId, policyType, sumInsured);
        if (result == -2) {
            statusMessage.setValue("Member ID " + memberId + " already exists!");
        } else if (result > 0) {
            statusMessage.setValue("User approved successfully");
            loadPendingUsers();
        } else {
            statusMessage.setValue("Approval failed");
        }
    }
}
