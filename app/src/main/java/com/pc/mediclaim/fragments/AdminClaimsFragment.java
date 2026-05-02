package com.pc.mediclaim.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pc.mediclaim.R;
import com.pc.mediclaim.model.Claim;
import com.pc.mediclaim.viewmodel.AdminClaimsViewModel;
import java.util.ArrayList;
import java.util.List;

public class AdminClaimsFragment extends Fragment {

    private RecyclerView rvClaims;
    private List<Claim> claimList;
    private AdminClaimsAdapter adapter;
    private AdminClaimsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_claims, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminClaimsViewModel.class);
        rvClaims = view.findViewById(R.id.rvAdminClaims);
        rvClaims.setLayoutManager(new LinearLayoutManager(getContext()));
        
        claimList = new ArrayList<>();
        adapter = new AdminClaimsAdapter(claimList);
        rvClaims.setAdapter(adapter);

        viewModel.getClaimsLiveData().observe(getViewLifecycleOwner(), claims -> {
            claimList.clear();
            claimList.addAll(claims);
            adapter.notifyDataSetChanged();
        });

        viewModel.loadAllClaims();
    }

    private class AdminClaimsAdapter extends RecyclerView.Adapter<AdminClaimsAdapter.ViewHolder> {
        private List<Claim> claims;
        public AdminClaimsAdapter(List<Claim> claims) { this.claims = claims; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_claim, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Claim claim = claims.get(position);
            holder.tvPatientName.setText(claim.getPatientName());
            holder.tvClaimNo.setText("No: " + claim.getClaimNo());
            holder.tvAmount.setText("₹ " + (int)claim.getAmount());
            holder.tvStatus.setText("Status: " + claim.getStatus());

            if (!"PENDING".equalsIgnoreCase(claim.getStatus())) {
                holder.btnApprove.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
            } else {
                holder.btnApprove.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
            }

            holder.btnApprove.setOnClickListener(v -> viewModel.updateClaimStatus(claim.getId(), "APPROVED"));
            holder.btnReject.setOnClickListener(v -> viewModel.updateClaimStatus(claim.getId(), "REJECTED"));
        }

        @Override
        public int getItemCount() { return claims.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPatientName, tvClaimNo, tvAmount, tvStatus;
            Button btnApprove, btnReject;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPatientName = itemView.findViewById(R.id.tvAdminPatientName);
                tvClaimNo = itemView.findViewById(R.id.tvAdminClaimNo);
                tvAmount = itemView.findViewById(R.id.tvAdminAmount);
                tvStatus = itemView.findViewById(R.id.tvAdminStatus);
                btnApprove = itemView.findViewById(R.id.btnApprove);
                btnReject = itemView.findViewById(R.id.btnReject);
            }
        }
    }
}
