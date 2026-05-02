package com.pc.mediclaim.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pc.mediclaim.R;
import com.pc.mediclaim.adapters.ClaimAdapter;
import com.pc.mediclaim.model.Claim;
import com.pc.mediclaim.utils.Constants;
import com.pc.mediclaim.viewmodel.AdminClaimsViewModel;
import com.pc.mediclaim.viewmodel.ClaimsViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays medical claims in a list.
 * <p>
 * This fragment serves two roles:
 * <ul>
 *     <li><b>User Mode:</b> Shows personal claims and allows submitting a new claim.</li>
 *     <li><b>Admin Mode:</b> Shows all submitted claims and provides administrative actions
 *     like updating claim status and viewing attached documentation.</li>
 * </ul>
 */
public class ClaimsFragment extends Fragment implements ClaimAdapter.OnClaimActionListener {

    private static final String TAG = "ClaimsFragment";
    private RecyclerView rvClaims;
    private List<Claim> claimList;
    private ClaimAdapter adapter;
    private MaterialCardView cardNewClaim;
    private TextView tvTitle, tvSubtitle;
    private ClaimsViewModel userViewModel;
    private AdminClaimsViewModel adminViewModel;
    private boolean isAdmin;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_claims, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Fetch user session information
        SharedPreferences pref = requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        userId = pref.getInt(Constants.KEY_USER_ID, -1);
        String role = pref.getString(Constants.KEY_USER_ROLE, Constants.ROLE_USER);
        isAdmin = Constants.ROLE_ADMIN.equalsIgnoreCase(role);

        // Initialize UI components
        rvClaims = view.findViewById(R.id.rvClaims);
        cardNewClaim = view.findViewById(R.id.cardNewClaim);
        tvTitle = view.findViewById(R.id.tvClaimsTitle);
        tvSubtitle = view.findViewById(R.id.tvClaimsSubtitle);

        rvClaims.setLayoutManager(new LinearLayoutManager(getContext()));
        claimList = new ArrayList<>();
        adapter = new ClaimAdapter(claimList, isAdmin, this);
        rvClaims.setAdapter(adapter);

        // Setup role-specific logic
        if (isAdmin) {
            setupAdminUI();
        } else {
            setupUserUI();
        }
    }

    /**
     * Configures the UI for a standard user.
     * Sets appropriate titles and enables the "New Claim" action.
     */
    private void setupUserUI() {
        tvTitle.setText("Claim Endorsements");
        tvSubtitle.setText("Track and manage your reimbursement requests");
        
        if (cardNewClaim != null) {
            cardNewClaim.setVisibility(View.VISIBLE);
            cardNewClaim.setClickable(true);
            cardNewClaim.setFocusable(true);
            cardNewClaim.setOnClickListener(v -> {
                Log.d(TAG, "Navigating to NewClaimFragment");
                Navigation.findNavController(v).navigate(R.id.action_claimsFragment_to_newClaimFragment);
            });
        }

        userViewModel = new ViewModelProvider(this).get(ClaimsViewModel.class);
        userViewModel.getClaimsLiveData().observe(getViewLifecycleOwner(), claims -> {
            claimList.clear();
            claimList.addAll(claims);
            adapter.notifyDataSetChanged();
        });
        userViewModel.loadClaims(userId);
    }

    /**
     * Configures the UI for an administrator.
     * Sets appropriate titles, hides "New Claim" button, and initializes administrative data sync.
     */
    private void setupAdminUI() {
        tvTitle.setText("Claim Approvals");
        tvSubtitle.setText("Review and process pending reimbursement requests");
        if (cardNewClaim != null) {
            cardNewClaim.setVisibility(View.GONE);
        }

        adminViewModel = new ViewModelProvider(this).get(AdminClaimsViewModel.class);
        adminViewModel.getClaimsLiveData().observe(getViewLifecycleOwner(), claims -> {
            claimList.clear();
            claimList.addAll(claims);
            adapter.notifyDataSetChanged();
        });
        adminViewModel.loadAllClaims();
    }

    /**
     * Interface implementation to handle status update requests from the adapter.
     * Shows a dialog to select a new status for the claim.
     *
     * @param claim The claim to update.
     */
    @Override
    public void onUpdateStatus(Claim claim) {
        String[] statuses = {"PENDING", "APPROVED", "REJECTED"};
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Update Claim Status")
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];
                    adminViewModel.updateClaimStatus(claim.getId(), newStatus);
                    Toast.makeText(getContext(), "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Interface implementation to handle document viewing requests from the adapter.
     * Triggered by the "View Doc" button, visible only to admins.
     *
     * @param claim The claim containing the document URI.
     */
    @Override
    public void onViewDocument(Claim claim) {
        if (claim.getDocumentUri() != null && !claim.getDocumentUri().isEmpty()) {
            showDocumentPreview(claim.getDocumentUri());
        } else {
            Toast.makeText(getContext(), "No document uploaded for this claim", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays a Material dialog containing a preview of the claim's document image.
     *
     * @param uriString The URI string of the image to preview.
     */
    private void showDocumentPreview(String uriString) {
        try {
            ImageView imageView = new ImageView(getContext());
            imageView.setImageURI(Uri.parse(uriString));
            imageView.setAdjustViewBounds(true);
            
            // Add some padding around the image preview
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            imageView.setPadding(padding, padding, padding, padding);
            
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Claim Document Preview")
                    .setView(imageView)
                    .setPositiveButton("Close", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error displaying document preview: " + e.getMessage());
            Toast.makeText(getContext(), "Error loading document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
