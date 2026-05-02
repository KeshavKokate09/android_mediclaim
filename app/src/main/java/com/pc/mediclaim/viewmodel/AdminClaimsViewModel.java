package com.pc.mediclaim.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pc.mediclaim.model.Claim;
import com.pc.mediclaim.repository.MediclaimRepository;
import java.util.List;

public class AdminClaimsViewModel extends AndroidViewModel {
    private MediclaimRepository repository;
    private MutableLiveData<List<Claim>> claimsLiveData = new MutableLiveData<>();

    public AdminClaimsViewModel(@NonNull Application application) {
        super(application);
        repository = new MediclaimRepository(application);
    }

    public LiveData<List<Claim>> getClaimsLiveData() { return claimsLiveData; }

    public void loadAllClaims() {
        List<Claim> claims = repository.getAllClaims();
        claimsLiveData.setValue(claims);
    }

    public void updateClaimStatus(int claimId, String status) {
        repository.updateClaimStatus(claimId, status);
        loadAllClaims();
    }
}
