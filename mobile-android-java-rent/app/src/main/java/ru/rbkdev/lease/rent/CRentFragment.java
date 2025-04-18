package ru.rbkdev.lease.rent;

import ru.rbkdev.lease.main.CClient;
import ru.rbkdev.lease.main.CException;
import ru.rbkdev.lease.R;
import ru.rbkdev.lease.main.CMainActivity;
import ru.rbkdev.lease.rent.CFarmActivity;
import ru.rbkdev.lease.search.result.pageradapter.listfarm.CRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.view.View;
import android.os.Bundle;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class CRentFragment extends Fragment {

    SwipeRefreshLayout m_swipeRefreshLayout;
    RecyclerView m_recyclerViewRent;
    TextView m_textPlugRent;

    Context m_context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rent, container, false);

        m_context = getContext();

        m_swipeRefreshLayout = view.findViewById(R.id.swipeRefreshRent);
        m_recyclerViewRent = view.findViewById(R.id.recyclerViewRent);
        m_textPlugRent = view.findViewById(R.id.txtPlugRent);

        m_recyclerViewRent.setHasFixedSize(true);
        m_recyclerViewRent.setLayoutManager(new LinearLayoutManager(getActivity()));
        m_recyclerViewRent.setAdapter(new CRecyclerAdapter(true, getActivity(), this));

        FloatingActionButton btnAddFarm = view.findViewById(R.id.btnAddFerm);
        btnAddFarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(CRentFragment.this.getActivity(), CFarmActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        GetListFarms();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null) {
            if(resultCode == 1889) {

                ArrayList<byte[]> data_send = new ArrayList<>();

                ByteArrayOutputStream message = new ByteArrayOutputStream();
                try {
                    message.write(data.getByteArrayExtra("message"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                m_textPlugRent.setText(R.string.not_connection);
                m_recyclerViewRent.setVisibility(View.INVISIBLE);
                CRecyclerAdapter adapter = (CRecyclerAdapter)m_recyclerViewRent.getAdapter();
                if(adapter != null) {
                    adapter.onClear();

                    ArrayList<ArrayList<ByteArrayOutputStream>> answer = CClient.DataExchange(data_send, (byte)0x53, m_context);
                    if(answer != null) {
                        try {
                            if(!answer.isEmpty()) {
                                ArrayList<ByteArrayOutputStream> listValue = answer.get(0);
                                if(listValue.size() == 1) {
                                    if(listValue.get(0).toByteArray()[0] == '0')
                                        throw new CException("Квартира создана.\nОбновите список");
                                    else
                                        throw new CException("Квартира не создана");
                                } else
                                    throw new CException("Ошибка парсера");
                            } else
                                throw new CException("Ошибка парсера");
                        } catch (CException error) {
                            m_textPlugRent.setText(error.getMessage());
                        }
                    }
                }
            }
        }
    }

    // Получения списка квартир
    private void GetListFarms() {
        m_textPlugRent.setText(R.string.not_connection);
        m_recyclerViewRent.setVisibility(View.INVISIBLE);
        CRecyclerAdapter adapter = (CRecyclerAdapter)m_recyclerViewRent.getAdapter();
        if(adapter != null) {
            adapter.onClear();
            ArrayList<byte[]> data = new ArrayList<>();
            data.add(CMainActivity.g_userId.getBytes());
            data.add("111".getBytes());

            // Command "O"
            ArrayList<ArrayList<ByteArrayOutputStream>> answer = CClient.DataExchange(data, (byte) 0x4F, m_context);
            if (answer != null) {
                try {
                    if (!answer.isEmpty()) {
                        Iterator<ArrayList<ByteArrayOutputStream>> itr = answer.iterator();
                        int countFarm = 0;
                        int numberFarm = 0;
                        while (itr.hasNext()) {
                            ArrayList<ByteArrayOutputStream> listValue = itr.next();
                            if (listValue.size() == 2) {
                                if (listValue.get(0).toByteArray()[0] != '0')
                                    throw new CException("Ошибка сервера");

                                numberFarm = Integer.parseInt(listValue.get(1).toString());
                                if (numberFarm == 0) {
                                    m_textPlugRent.setText("У вас нет сдаваемых квартир");
                                    return;
                                }
                            } else {
                                if (listValue.size() == 9) {
                                    adapter.onItemArray(listValue);
                                    countFarm++;
                                } else
                                    throw new CException("Ошибка парсера");
                            }
                        }

                        if (countFarm != numberFarm)
                            throw new CException("Ошибка парсера");

                        m_recyclerViewRent.setVisibility(View.VISIBLE);
                        m_textPlugRent.setText("");
                    } else {
                        throw new CException("Ошибка парсера");
                    }
                } catch (CException error) {
                    m_textPlugRent.setText(error.getMessage());
                    // Toast.makeText(m_context, "Ошибка парсера", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        m_swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetListFarms();
                m_swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
