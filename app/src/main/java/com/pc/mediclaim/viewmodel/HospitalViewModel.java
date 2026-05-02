package com.pc.mediclaim.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pc.mediclaim.model.Hospital;
import com.pc.mediclaim.repository.MediclaimRepository;
import java.util.List;

public class HospitalViewModel extends AndroidViewModel {
    private MediclaimRepository repository;
    private MutableLiveData<List<Hospital>> hospitalLiveData = new MutableLiveData<>();
    private MutableLiveData<String> statusMessage = new MutableLiveData<>();

    public HospitalViewModel(@NonNull Application application) {
        super(application);
        repository = new MediclaimRepository(application);
    }

    public LiveData<List<Hospital>> getHospitalLiveData() { return hospitalLiveData; }
    public LiveData<String> getStatusMessage() { return statusMessage; }

    public void searchHospitals(String city) {
        List<Hospital> hospitals = repository.getHospitalsByCity(city);
        hospitalLiveData.setValue(hospitals);
    }

    public void addHospital(String name, String address, String contact, String city, String state) {
        Hospital hospital = new Hospital(0, name, address, contact, city, state);
        long result = repository.addHospital(hospital);
        if (result == -1) {
            statusMessage.setValue("Hospital already exists in this city");
        } else if (result > 0) {
            statusMessage.setValue("Hospital added successfully");
            searchHospitals(""); // Refresh
        } else {
            statusMessage.setValue("Failed to add hospital");
        }
    }

    public void updateHospital(int id, String name, String address, String contact, String city, String state) {
        Hospital hospital = new Hospital(id, name, address, contact, city, state);
        int result = repository.updateHospital(hospital);
        if (result > 0) {
            statusMessage.setValue("Hospital updated successfully");
            searchHospitals(""); // Refresh
        } else {
            statusMessage.setValue("Failed to update hospital");
        }
    }
}
