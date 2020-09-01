package com.fonekey.mainpage;
import com.fonekey.R;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import android.view.View.OnClickListener;

public class CRentFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_rent, container, false);

        Button m_btnSearch;
        m_btnSearch = (Button) v.findViewById(R.id.btnSearch);
        m_btnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(), CSearchActivity.class);
                startActivity(intent);
            }
        });

        return v;
    }
}