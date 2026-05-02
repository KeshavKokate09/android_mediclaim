package com.pc.mediclaim.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.pc.mediclaim.R;
import com.pc.mediclaim.adapters.HospitalAdapter;
import com.pc.mediclaim.model.Hospital;
import com.pc.mediclaim.utils.Constants;
import com.pc.mediclaim.viewmodel.HospitalViewModel;

import java.util.ArrayList;
import java.util.List;

public class HospitalFragment extends Fragment implements HospitalAdapter.OnHospitalActionListener {

    private RecyclerView rvHospitals;
    private List<Hospital> hospitalList;
    private HospitalAdapter adapter;
    private EditText etSearchCity;
    private ExtendedFloatingActionButton fabAddHospital;
    private HospitalViewModel viewModel;
    private boolean isAdmin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hospital, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences pref = requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        String role = pref.getString(Constants.KEY_USER_ROLE, Constants.ROLE_USER);
        isAdmin = Constants.ROLE_ADMIN.equalsIgnoreCase(role);

        viewModel = new ViewModelProvider(this).get(HospitalViewModel.class);
        rvHospitals = view.findViewById(R.id.rvHospitals);
        etSearchCity = view.findViewById(R.id.etSearchCity);
        fabAddHospital = view.findViewById(R.id.fabAddHospital);

        if (isAdmin) {
            fabAddHospital.setVisibility(View.VISIBLE);
            fabAddHospital.setOnClickListener(v -> showHospitalDialog(null));
        } else {
            fabAddHospital.setVisibility(View.GONE);
        }

        setupRecyclerView();
        setupSearch();
        setupObservers();

        viewModel.searchHospitals("");
    }

    private void setupRecyclerView() {
        rvHospitals.setLayoutManager(new LinearLayoutManager(getContext()));
        hospitalList = new ArrayList<>();
        adapter = new HospitalAdapter(hospitalList, isAdmin, this);
        rvHospitals.setAdapter(adapter);

        rvHospitals.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (isAdmin && fabAddHospital != null) {
                    if (dy > 0 && fabAddHospital.isExtended()) {
                        fabAddHospital.shrink();
                    } else if (dy < 0 && !fabAddHospital.isExtended()) {
                        fabAddHospital.extend();
                    }
                }
            }
        });
    }

    private void setupSearch() {
        etSearchCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchHospitals(s.toString().trim());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        viewModel.getHospitalLiveData().observe(getViewLifecycleOwner(), hospitals -> {
            hospitalList.clear();
            hospitalList.addAll(hospitals);
            adapter.notifyDataSetChanged();
        });

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEdit(Hospital hospital) {
        showHospitalDialog(hospital);
    }

    @Override
    public void onNavigate(Hospital hospital) {
        try {
            String query = hospital.getName() + ", " + hospital.getAddress() + ", " + hospital.getCity();
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, gmmIntentUri));
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Could not open map", Toast.LENGTH_SHORT).show();
        }
    }

    private void showHospitalDialog(@Nullable Hospital hospital) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        
        View view = getLayoutInflater().inflate(R.layout.dialog_add_hospital, null);
        EditText etName = view.findViewById(R.id.etHospName);
        EditText etAddress = view.findViewById(R.id.etHospAddress);
        EditText etCity = view.findViewById(R.id.etHospCity);
        EditText etState = view.findViewById(R.id.etHospState);
        EditText etContact = view.findViewById(R.id.etHospContact);

        if (hospital != null) {
            builder.setTitle("Update Hospital");
            etName.setText(hospital.getName());
            etAddress.setText(hospital.getAddress());
            etCity.setText(hospital.getCity());
            etState.setText(hospital.getState());
            etContact.setText(hospital.getContact());
        } else {
            builder.setTitle("New Network Hospital");
        }

        builder.setView(view);
        builder.setPositiveButton(hospital == null ? "Save" : "Update", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String state = etState.getText().toString().trim();
            String contact = etContact.getText().toString().trim();

            if (validate(name, city, address, contact)) {
                if (hospital == null) {
                    viewModel.addHospital(name, address, contact, city, state);
                } else {
                    viewModel.updateHospital(hospital.getId(), name, address, contact, city, state);
                }
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private boolean validate(String name, String city, String address, String contact) {
        if (name.isEmpty() || city.isEmpty() || address.isEmpty() || contact.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (contact.length() < 10) {
            Toast.makeText(getContext(), "Enter a valid contact number", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
