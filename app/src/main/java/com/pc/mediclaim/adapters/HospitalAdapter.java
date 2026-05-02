package com.pc.mediclaim.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.pc.mediclaim.R;
import com.pc.mediclaim.model.Hospital;
import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.ViewHolder> {
    private List<Hospital> hospitals;
    private boolean isAdmin;
    private OnHospitalActionListener listener;

    public interface OnHospitalActionListener {
        void onEdit(Hospital hospital);
        void onNavigate(Hospital hospital);
    }

    public HospitalAdapter(List<Hospital> hospitals, boolean isAdmin, OnHospitalActionListener listener) {
        this.hospitals = hospitals;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospital, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hospital hospital = hospitals.get(position);
        holder.tvName.setText(hospital.getName());
        holder.tvCity.setText(hospital.getCity());
        holder.tvAddress.setText(hospital.getAddress());
        holder.tvContact.setText(hospital.getContact());

        holder.btnEdit.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(hospital);
        });

        holder.btnNavigate.setOnClickListener(v -> {
            if (listener != null) listener.onNavigate(hospital);
        });
    }

    @Override
    public int getItemCount() {
        return hospitals.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCity, tvAddress, tvContact;
        MaterialButton btnNavigate, btnEdit;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHospName);
            tvCity = itemView.findViewById(R.id.tvHospCity);
            tvAddress = itemView.findViewById(R.id.tvHospAddress);
            tvContact = itemView.findViewById(R.id.tvHospContact);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
            btnEdit = itemView.findViewById(R.id.btnEditHospital);
        }
    }
}
