package com.fonekey.mainpage;
import com.fonekey.R;
import com.fonekey.searchpage.CSearchActivity;

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
import android.widget.TextView;
import android.widget.Toast;

public class CRentFragment extends Fragment {

    CClient m_client;
    TextView m_txtMessage;

    public CRentFragment() {
        m_client = new CClient();
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rent, container, false);

        Button m_btnSearch;
        m_btnSearch = (Button) view.findViewById(R.id.btnSearch);
        m_btnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(), CSearchActivity.class);
                startActivity(intent);
            }
        });

        Button btnSend = (Button) view.findViewById(R.id.btnSend);
        m_txtMessage = (TextView) view.findViewById(R.id.txtTown);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "onClick:", Toast.LENGTH_SHORT).show();
                m_client.SendData(m_txtMessage.getText().toString());
            }
        });

        return view;
    }
}