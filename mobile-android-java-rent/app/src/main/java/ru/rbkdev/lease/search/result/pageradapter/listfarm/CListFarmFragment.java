package ru.rbkdev.lease.search.result.pageradapter.listfarm;
import ru.rbkdev.lease.R;

import ru.rbkdev.lease.main.CClient;
import ru.rbkdev.lease.main.CException;
import ru.rbkdev.lease.main.CMainActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;

// import ru.yandex.money.android.sdk.Checkout;
import ru.yoomoney.sdk.kassa.payments.Checkout;

public class CListFarmFragment extends Fragment {

    SwipeRefreshLayout m_swipeRefreshLayout;
    RecyclerView m_recyclerViewList;
    TextView m_textPlugList;

    static public final int REQUEST_CODE_TOKENIZE = 33;

    public String m_dateBegin;
    public String m_dateEnd;

    private String m_town;
    private String m_numberPerson;
    private String m_dateBeginSec;
    private String m_dateEndSec;

    Context m_context;

    // Debug
    long d_time_begin;
    long d_time_end;
    // Debug

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        m_context = getContext();

        Intent intent;
        FragmentActivity act = this.getActivity();
        if(act != null) {
            intent = act.getIntent();
            m_town = intent.getStringExtra("town");
            m_numberPerson = intent.getStringExtra("number_person");
            m_dateBeginSec = intent.getStringExtra("data_begin_sec");
            m_dateEndSec = intent.getStringExtra("data_end_sec");

            m_dateBegin = intent.getStringExtra("data_begin");
            m_dateEnd = intent.getStringExtra("data_end");

            m_swipeRefreshLayout = view.findViewById(R.id.swipeRefreshList);
            m_recyclerViewList = view.findViewById(R.id.recycle_view);
            m_textPlugList = view.findViewById(R.id.txtPlugList);

            m_recyclerViewList.setHasFixedSize(true);
            m_recyclerViewList.setLayoutManager(new LinearLayoutManager(getActivity()));
            m_recyclerViewList.setAdapter(new CRecyclerAdapter(false, getContext(), this));

            // Debug
            Date date = Calendar.getInstance().getTime();

            d_time_begin = date.getTime() / 1000;
            m_dateBeginSec = d_time_begin + "";

            d_time_end = d_time_begin + 300;
            m_dateEndSec = d_time_end + 300  + "";
            // Debug

            CommandListFarms();
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Intent in = getActivity().getIntent();
        if(requestCode == REQUEST_CODE_TOKENIZE && resultCode == Activity.RESULT_OK) {

            byte[] farm_id = in.getByteArrayExtra("farm_id");
            if(farm_id == null) {
                Toast.makeText(getContext(), "Error program", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<byte[]> data = new ArrayList<>();

            data.add(m_town.getBytes());
            data.add(farm_id);
            data.add(m_dateBeginSec.getBytes());
            data.add(m_dateEndSec.getBytes());
            data.add(CMainActivity.g_userId.getBytes());
            data.add(Checkout.createTokenizationResult(intent).getPaymentToken().getBytes());

            // CClient.SetTimeout(5000);

            // Command "P"
            ArrayList<ArrayList<ByteArrayOutputStream>> answer = CClient.DataExchange(data, (byte) 0x50, m_context);
            if(answer != null) {
                try {
                    ArrayList<ByteArrayOutputStream> listValue = answer.get(1);
                    if(listValue.size() == 3) {
                        if(!listValue.get(1).toString().equals("0"))
                            throw new CException("Оплата не прошла");

                        if(!listValue.get(2).toString().equals("0"))
                            throw new CException("Нет соединения с квартирой");

                        throw new CException("Квартира куплена");
                    } else
                        throw new CException("Квартира не куплена");

                } catch (CException exc) {
                    Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else
                Toast.makeText(getContext(), R.string.not_connection, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Токен не создан", Toast.LENGTH_SHORT).show();
            if(resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(getContext(), "RESULT_CANCELED", Toast.LENGTH_SHORT).show();
        }
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        m_swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                CommandListFarms();
                m_swipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    // Получения списка квартир
    private void CommandListFarms() {

        m_textPlugList.setText(R.string.not_connection);
        m_recyclerViewList.setVisibility(View.INVISIBLE);
        CRecyclerAdapter adapter = (CRecyclerAdapter)m_recyclerViewList.getAdapter();
        if(adapter != null) {
            adapter.onClear();

            ArrayList<byte[]> data = new ArrayList<>();
            data.add(m_town.getBytes());
            data.add(m_numberPerson.getBytes());
            data.add(m_dateBeginSec.getBytes());
            data.add(m_dateEndSec.getBytes());

            // Command "L"
            ArrayList<ArrayList<ByteArrayOutputStream>> answer = CClient.DataExchange(data, (byte) 0x4C, m_context);
            if(answer != null) {

                for(int i = 1; i < answer.size(); i++)
                    adapter.onItemArray(answer.get(i));

                m_textPlugList.setText("");
                m_recyclerViewList.setVisibility(View.VISIBLE);
            }
        }
    }
}
