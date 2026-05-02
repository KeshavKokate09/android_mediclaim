package com.pc.mediclaim.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pc.mediclaim.R;
import com.pc.mediclaim.model.Dependent;
import java.util.List;

public class DependentAdapter extends RecyclerView.Adapter<DependentAdapter.ViewHolder> {
    private List<Dependent> dependents;

    public DependentAdapter(List<Dependent> dependents) {
        this.dependents = dependents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dependent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dependent dependent = dependents.get(position);
        holder.tvName.setText(dependent.getName());
        holder.tvRelation.setText(dependent.getRelation());
    }

    @Override
    public int getItemCount() {
        return dependents.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRelation;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDepName);
            tvRelation = itemView.findViewById(R.id.tvDepRelation);
        }
    }
}
