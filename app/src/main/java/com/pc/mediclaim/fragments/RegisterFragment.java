package com.pc.mediclaim.fragments;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pc.mediclaim.R;
import com.pc.mediclaim.repository.MediclaimRepository;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private TextInputEditText etName, etEmail, etMobile, etPassword;
    private TextInputLayout tilName, tilEmail, tilMobile, tilPassword;
    private AutoCompleteTextView spinnerSumInsured;
    private ChipGroup chipGroupPolicyType;
    private TextView tvPremium;
    private MaterialButton btnRegister;
    private MediclaimRepository repository;

    private final String[] sumInsuredOptions = {"3,00,000", "5,00,000", "10,00,000", "20,00,000", "50,00,000"};
    private final Map<String, Double> baseRates = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseRates.put("Individual", 0.012);
        baseRates.put("Family Floater", 0.022);
        baseRates.put("Senior Citizen", 0.042);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new MediclaimRepository(getContext());
        initViews(view);
        setupUI();

        btnRegister.setOnClickListener(v -> handleRegistration());
        
        calculatePremium();
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.etRegName);
        etEmail = view.findViewById(R.id.etRegEmail);
        etMobile = view.findViewById(R.id.etRegMobile);
        etPassword = view.findViewById(R.id.etRegPassword);
        
        tilName = (TextInputLayout) etName.getParent().getParent();
        tilEmail = (TextInputLayout) etEmail.getParent().getParent();
        tilMobile = (TextInputLayout) etMobile.getParent().getParent();
        tilPassword = (TextInputLayout) etPassword.getParent().getParent();

        chipGroupPolicyType = view.findViewById(R.id.chipGroupPolicyType);
        spinnerSumInsured = view.findViewById(R.id.spinnerSumInsured);
        tvPremium = view.findViewById(R.id.tvPremium);
        btnRegister = view.findViewById(R.id.btnRegister);
    }

    private void setupUI() {
        ArrayAdapter<String> sumAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, sumInsuredOptions);
        spinnerSumInsured.setAdapter(sumAdapter);
        spinnerSumInsured.setOnItemClickListener((parent, view, position, id) -> calculatePremium());
        chipGroupPolicyType.setOnCheckedStateChangeListener((group, checkedIds) -> calculatePremium());
    }

    private void calculatePremium() {
        String type = "Individual";
        int checkedId = chipGroupPolicyType.getCheckedChipId();
        if (checkedId != View.NO_ID) {
            Chip chip = chipGroupPolicyType.findViewById(checkedId);
            if (chip != null) type = chip.getText().toString();
        }

        String sumStr = spinnerSumInsured.getText().toString().replace(",", "");
        if (!sumStr.isEmpty()) {
            try {
                double sumInsured = Double.parseDouble(sumStr);
                double rate = baseRates.getOrDefault(type, 0.02);
                double premium = sumInsured * rate;
                if (type.equals("Family Floater")) premium *= 1.1;
                tvPremium.setText(String.format("₹ %,.0f", premium));
            } catch (NumberFormatException e) {
                tvPremium.setText("₹ 0");
            }
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;
        
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (name.isEmpty()) {
            tilName.setError("Name is required");
            isValid = false;
        } else tilName.setError(null);

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Valid email is required");
            isValid = false;
        } else tilEmail.setError(null);

        if (mobile.length() != 10) {
            tilMobile.setError("10-digit mobile number required");
            isValid = false;
        } else tilMobile.setError(null);

        if (pass.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else tilPassword.setError(null);

        return isValid;
    }

    private void handleRegistration() {
        if (!validateInputs()) return;

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        long id = repository.registerUser(name, email, mobile, pass);
        if (id == -1) {
            Toast.makeText(getContext(), "Email or Mobile already registered", Toast.LENGTH_LONG).show();
        } else if (id > 0) {
            Toast.makeText(getContext(), "Application submitted! Quotes saved to your profile.", Toast.LENGTH_LONG).show();
            Navigation.findNavController(requireView()).navigateUp();
        } else {
            Toast.makeText(getContext(), "Failed to submit application", Toast.LENGTH_SHORT).show();
        }
    }
}
