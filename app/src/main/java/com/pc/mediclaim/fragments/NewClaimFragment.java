package com.pc.mediclaim.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;
import com.pc.mediclaim.R;
import com.pc.mediclaim.model.Claim;
import com.pc.mediclaim.model.Dependent;
import com.pc.mediclaim.model.Policy;
import com.pc.mediclaim.repository.MediclaimRepository;
import com.pc.mediclaim.utils.Constants;
import com.pc.mediclaim.viewmodel.ClaimsViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Fragment for submitting a new medical reimbursement claim.
 * Handles patient selection, hospitalization details, and document scanning.
 */
public class NewClaimFragment extends Fragment {

    private TextInputEditText etPolicyNo, etHospitalName, etDoa, etDod, etAmount;
    private AutoCompleteTextView spinnerPatient, spinnerDocType;
    private MaterialButton btnScanDoc, btnSubmit;
    private RecyclerView rvUploadedDocs;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;

    private ClaimsViewModel userViewModel;
    private int userId;
    private ActivityResultLauncher<IntentSenderRequest> scannerLauncher;
    
    private List<UploadedDoc> uploadedDocsList = new ArrayList<>();
    private DocAdapter docAdapter;
    private String selectedDocType = "";

    private final String[] documentTypes = {
            "Claim Form", "Aadhar Card", "Discharge Certificate", 
            "Hospital Bill", "Pharmacy Bills", "Lab Reports"
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        scannerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        GmsDocumentScanningResult scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.getData());
                        if (scanResult != null && scanResult.getPages() != null && !scanResult.getPages().isEmpty()) {
                            Uri uri = scanResult.getPages().get(0).getImageUri();
                            addDocument(selectedDocType, uri);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_claim, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences pref = requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        userId = pref.getInt(Constants.KEY_USER_ID, -1);

        initViews(view);
        setupWindowInsets(view);
        setupToolbar();
        setupPatientSpinner();
        setupDocTypeSpinner();
        setupDatePickers();
        loadPolicyInfo();
        
        userViewModel = new ViewModelProvider(this).get(ClaimsViewModel.class);

        btnScanDoc.setOnClickListener(v -> startScanning());
        btnSubmit.setOnClickListener(v -> submitClaim());

        rvUploadedDocs.setLayoutManager(new LinearLayoutManager(getContext()));
        docAdapter = new DocAdapter();
        rvUploadedDocs.setAdapter(docAdapter);
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        appBarLayout = view.findViewById(R.id.appBarLayout); 
        etPolicyNo = view.findViewById(R.id.etPolicyNo);
        spinnerPatient = view.findViewById(R.id.spinnerPatient);
        etHospitalName = view.findViewById(R.id.etHospitalName);
        etDoa = view.findViewById(R.id.etDoa);
        etDod = view.findViewById(R.id.etDod);
        etAmount = view.findViewById(R.id.etAmount);
        spinnerDocType = view.findViewById(R.id.spinnerDocType);
        btnScanDoc = view.findViewById(R.id.btnScanDoc);
        btnSubmit = view.findViewById(R.id.btnSubmitClaim);
        rvUploadedDocs = view.findViewById(R.id.rvUploadedDocs);
    }

    /**
     * Handles window insets for edge-to-edge display.
     * Ensures the toolbar is not hidden behind the status bar.
     */
    private void setupWindowInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Apply top padding to AppBarLayout to account for status bar
            if (appBarLayout != null) {
                appBarLayout.setPadding(0, systemBars.top, 0, 0);
            }
            
            // Apply bottom padding to the root view to account for navigation bar
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setupToolbar() {
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void loadPolicyInfo() {
        MediclaimRepository repo = new MediclaimRepository(requireContext());
        Policy policy = repo.getPolicyByUserId(userId);
        if (policy != null) {
            etPolicyNo.setText(policy.getPolicyNo());
        }
    }

    private void setupPatientSpinner() {
        MediclaimRepository repo = new MediclaimRepository(requireContext());
        List<Dependent> dependents = repo.getDependentsByUserId(userId);
        List<String> names = new ArrayList<>();
        SharedPreferences pref = requireContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        names.add("Self (" + pref.getString(Constants.KEY_USER_NAME, "User") + ")");
        for (Dependent d : dependents) names.add(d.getName() + " (" + d.getRelation() + ")");
        spinnerPatient.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names));
    }

    private void setupDocTypeSpinner() {
        spinnerDocType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, documentTypes));
        spinnerDocType.setOnItemClickListener((parent, view, position, id) -> selectedDocType = documentTypes[position]);
    }

    private void setupDatePickers() {
        etDoa.setOnClickListener(v -> showDatePicker(etDoa));
        etDod.setOnClickListener(v -> showDatePicker(etDod));
    }

    private void showDatePicker(TextInputEditText et) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            et.setText(day + "/" + (month + 1) + "/" + year);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void startScanning() {
        if (selectedDocType.isEmpty()) {
            Toast.makeText(getContext(), "Please select document type first", Toast.LENGTH_SHORT).show();
            return;
        }

        for (UploadedDoc doc : uploadedDocsList) {
            if (doc.type.equals(selectedDocType)) {
                Toast.makeText(getContext(), selectedDocType + " is already uploaded.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            GmsDocumentScannerOptions options = new GmsDocumentScannerOptions.Builder()
                    .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                    .setGalleryImportAllowed(true)
                    .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                    .build();

            GmsDocumentScanning.getClient(options).getStartScanIntent(requireActivity())
                    .addOnSuccessListener(intentSender -> scannerLauncher.launch(new IntentSenderRequest.Builder(intentSender).build()))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            Toast.makeText(getContext(), "Scanner error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addDocument(String type, Uri uri) {
        uploadedDocsList.add(new UploadedDoc(type, uri));
        docAdapter.notifyDataSetChanged();
    }

    private void submitClaim() {
        String patient = spinnerPatient.getText().toString();
        String hospital = etHospitalName.getText().toString();
        String doa = etDoa.getText().toString();
        String dod = etDod.getText().toString();
        String amountStr = etAmount.getText().toString();

        if (patient.isEmpty() || hospital.isEmpty() || doa.isEmpty() || dod.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all details", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasForm = false;
        boolean hasDischarge = false;
        for (UploadedDoc doc : uploadedDocsList) {
            if (doc.type.equals("Claim Form")) hasForm = true;
            if (doc.type.equals("Discharge Certificate")) hasDischarge = true;
        }

        if (!hasForm || !hasDischarge) {
            Toast.makeText(getContext(), "Claim Form and Discharge Certificate are mandatory", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String claimNo = "CLM-" + (System.currentTimeMillis() % 1000000);
            String mainDoc = uploadedDocsList.get(0).uri.toString();
            
            Claim newClaim = new Claim(0, userId, patient, doa, dod, amount, claimNo, "PENDING", mainDoc);
            userViewModel.uploadClaim(newClaim);
            
            Toast.makeText(getContext(), "Claim submitted successfully", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Submission error", Toast.LENGTH_SHORT).show();
        }
    }

    private static class UploadedDoc {
        String type;
        Uri uri;
        UploadedDoc(String type, Uri uri) { this.type = type; this.uri = uri; }
    }

    private class DocAdapter extends RecyclerView.Adapter<DocAdapter.ViewHolder> {
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_uploaded_doc, p, false));
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) {
            UploadedDoc doc = uploadedDocsList.get(p);
            h.type.setText(doc.type);
            h.thumb.setImageURI(doc.uri);
            h.delete.setOnClickListener(v -> {
                uploadedDocsList.remove(p);
                notifyDataSetChanged();
            });
        }
        @Override public int getItemCount() { return uploadedDocsList.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView type; ImageView thumb; View delete;
            ViewHolder(View v) {
                super(v);
                type = v.findViewById(R.id.tvDocType);
                thumb = v.findViewById(R.id.ivDocThumbnail);
                delete = v.findViewById(R.id.btnDeleteDoc);
            }
        }
    }
}
