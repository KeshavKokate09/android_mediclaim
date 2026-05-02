package com.pc.mediclaim.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.pc.mediclaim.R;
import com.pc.mediclaim.model.Claim;
import java.util.List;

/**
 * Adapter for displaying a list of claims in a RecyclerView.
 * Provides functionality for admins to update claim status and view uploaded documents.
 */
public class ClaimAdapter extends RecyclerView.Adapter<ClaimAdapter.ViewHolder> {
    private List<Claim> claims;
    private boolean isAdmin;
    private OnClaimActionListener listener;

    /**
     * Interface for handling claim-related actions.
     */
    public interface OnClaimActionListener {
        /**
         * Called when the admin clicks to update the status of a claim.
         * @param claim The claim to be updated.
         */
        void onUpdateStatus(Claim claim);

        /**
         * Called when the admin clicks to view the document associated with a claim.
         * @param claim The claim whose document is to be viewed.
         */
        void onViewDocument(Claim claim);
    }

    /**
     * Constructs a new ClaimAdapter.
     * @param claims The list of claims to display.
     * @param isAdmin True if the current user is an admin.
     * @param listener The listener for claim actions.
     */
    public ClaimAdapter(List<Claim> claims, boolean isAdmin, OnClaimActionListener listener) {
        this.claims = claims;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_claim, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Claim claim = claims.get(position);
        holder.tvPatientName.setText(claim.getPatientName());
        holder.tvClaimNo.setText(claim.getClaimNo());
        holder.tvDates.setText(claim.getDoa() + " - " + claim.getDod());
        holder.tvAmount.setText("₹ " + String.format("%,.0f", claim.getAmount()));
        
        String status = claim.getStatus().toUpperCase();
        holder.tvClaimStatus.setText(status);

        // Apply Status Colors
        int bgColor, textColor;
        if (status.equals("APPROVED")) {
            bgColor = R.color.status_active;
            textColor = R.color.on_status_active;
        } else if (status.equals("REJECTED")) {
            bgColor = R.color.status_utilized;
            textColor = R.color.on_status_utilized;
        } else {
            bgColor = R.color.status_pending;
            textColor = R.color.on_status_pending;
        }

        holder.tvClaimStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), bgColor)));
        holder.tvClaimStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), textColor));

        // Admin Actions
        if (isAdmin) {
            holder.adminActionLayout.setVisibility(View.VISIBLE);
            
            holder.btnUpdate.setOnClickListener(v -> {
                if (listener != null) listener.onUpdateStatus(claim);
            });

            holder.btnViewDoc.setOnClickListener(v -> {
                if (listener != null) listener.onViewDocument(claim);
            });
        } else {
            holder.adminActionLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return claims.size();
    }

    /**
     * ViewHolder class for ClaimAdapter.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvClaimStatus, tvClaimNo, tvDates, tvAmount;
        MaterialButton btnUpdate, btnViewDoc;
        LinearLayout adminActionLayout;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvClaimStatus = itemView.findViewById(R.id.tvClaimStatus);
            tvClaimNo = itemView.findViewById(R.id.tvClaimNo);
            tvDates = itemView.findViewById(R.id.tvDates);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            btnUpdate = itemView.findViewById(R.id.btnUpdateStatus);
            btnViewDoc = itemView.findViewById(R.id.btnViewDoc);
            adminActionLayout = itemView.findViewById(R.id.adminActionLayout);
        }
    }
}
