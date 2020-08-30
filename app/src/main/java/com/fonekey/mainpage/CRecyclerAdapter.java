package com.fonekey.mainpage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fonekey.R;

import java.util.List;

public class CRecyclerAdapter extends RecyclerView.Adapter<CRecyclerAdapter.CFermViewHolder> {

    private List<CFerm> m_lstFerm;

    public CRecyclerAdapter(List<CFerm> lstFerm) {
        m_lstFerm = lstFerm;
    }

    @Override
    public CFermViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ferm, viewGroup, false);
        return new CFermViewHolder(v);
    }

    public static class CFermViewHolder extends RecyclerView.ViewHolder {

        private TextView m_cost;

        public CFermViewHolder(View itemView) {
            super (itemView);

            m_cost = (TextView) itemView.findViewById(R.id.txtCost);
        }
    }

    @Override
    public void onBindViewHolder(CFermViewHolder viewHolder, final int position) {
        viewHolder.m_cost.setText(m_lstFerm.get(position).m_cost);
    }

    @Override
    public int getItemCount() { return m_lstFerm.size(); }

    /*Context m_context;
    List<CFerm> m_firmList;


    public CRecyclerAdapter (Context context, List<CFerm> list) {
        this.m_context = context;
        this.m_firmList = list;
    }

    @NonNull
    @Override
    public CFermViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(m_context).inflate(R.layout.ferm, parent, false);
        return new CFermViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CFermViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return m_firmList.size();
    }*/
}