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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class CRentFragment extends Fragment {

    TextView m_txtMessage;
    private Socket m_socket;
    private static final int SERVERPORT = 3500;
    private static final String SERVER_IP = "45.134.60.232";

    public CRentFragment() {
    };

    class ClientThread implements Runnable {

        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                m_socket = new Socket(serverAddr, SERVERPORT);
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

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
                SendData(m_txtMessage.getText().toString());
            }
        });

        new Thread(new ClientThread()).start();

        return view;
    }

    public void SendData(String message) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(m_socket.getOutputStream())),
                    true);
            out.println(message);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}