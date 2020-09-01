package com.fonekey.mainpage;
import com.fonekey.R;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class CSurrenderFragment extends Fragment {

    private List<CFerm> m_lstFerm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_surrender, container, false);

        RecyclerView m_recyclerViewFerm = (RecyclerView) view.findViewById(R.id.recycle_view);
        m_recyclerViewFerm.setHasFixedSize(true);
        m_recyclerViewFerm.setLayoutManager(new LinearLayoutManager(getActivity()));

        m_recyclerViewFerm.setAdapter(new CRecyclerAdapter(m_lstFerm));

        //m_recyclerViewFerm.setAdapter(new CRecyclerAdapter(getContext(), m_lstFerm));

         return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CFerm ferm1 = new CFerm();
        ferm1.m_cost = "10000";

        CFerm ferm2 = new CFerm();
        ferm2.m_cost = "20000";

        CFerm ferm3 = new CFerm();
        ferm3.m_cost = "30000";

        CFerm ferm4 = new CFerm();
        ferm4.m_cost = "40000";

        CFerm ferm5 = new CFerm();
        ferm5.m_cost = "50000";

        CFerm ferm6 = new CFerm();
        ferm6.m_cost = "60000";

        CFerm ferm7 = new CFerm();
        ferm7.m_cost = "70000";

        CFerm ferm8 = new CFerm();
        ferm8.m_cost = "80000";

        m_lstFerm = new ArrayList<>();
        m_lstFerm.add(ferm1);
        m_lstFerm.add(ferm2);
        m_lstFerm.add(ferm3);
        m_lstFerm.add(ferm4);
        m_lstFerm.add(ferm5);
        m_lstFerm.add(ferm6);
        m_lstFerm.add(ferm7);
        m_lstFerm.add(ferm8);

        // Заполняем лист домов
    }
}