package ru.rbkdev.rent.key;
import ru.rbkdev.rent.main.CClient;
import ru.rbkdev.rent.R;
import ru.rbkdev.rent.main.CMainActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.io.ByteArrayOutputStream;

public class CSurrenderFragment extends Fragment {

    TextView m_textPlugSurrender;
    RecyclerView m_recyclerViewSurrender;
    SwipeRefreshLayout m_swipeRefreshLayout;
    Context m_context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_surrender, container, false);

        m_context = getContext();

        m_textPlugSurrender = view.findViewById(R.id.txtPlugSurrender);
        m_swipeRefreshLayout = view.findViewById(R.id.swipeRefreshSurrender);
        m_recyclerViewSurrender = view.findViewById(R.id.recyclerViewSurrender);

        m_recyclerViewSurrender.setHasFixedSize(true);
        m_recyclerViewSurrender.setLayoutManager(new LinearLayoutManager(getActivity()));
        m_recyclerViewSurrender.setAdapter(new CSliderFarmRecyclerAdapter());

        GetListFarms();

        return view;
    }

    // Получения списка ключей
    private void GetListFarms() {
        m_textPlugSurrender.setText("Ошибка программы.\nПерезагрузите её");
        m_recyclerViewSurrender.setVisibility(View.INVISIBLE);
        CSliderFarmRecyclerAdapter adapter = (CSliderFarmRecyclerAdapter)m_recyclerViewSurrender.getAdapter();
        if(adapter != null) {
            adapter.onClear();
            m_textPlugSurrender.setText(R.string.not_connection);

            ArrayList<byte[]> data = new ArrayList<>();
            data.add(CMainActivity.g_userId.getBytes());

            // Command "K"
            ArrayList<ArrayList<ByteArrayOutputStream>> answer = CClient.DataExchange(data, (byte) 0x4B, m_context);
            if(answer != null) {

                ArrayList<ByteArrayOutputStream> ar = answer.get(1);
                if(ar != null) {
                    if(!ar.get(1).toString().equals("0")) {

                        if(ar.get(1).toString().equals("A")) {
                            m_textPlugSurrender.setText("Вы не зарегистрированны");
                            return;
                        }

                        for(int i = 2; i < answer.size(); i++)
                            adapter.onItemAdd(answer.get(i));
                        m_recyclerViewSurrender.setVisibility(View.VISIBLE);
                        m_textPlugSurrender.setText("");
                    } else
                        m_textPlugSurrender.setText("У вас нет квартир");
                }
            }

            /*{
                try {
                    if(!answer.isEmpty()) {
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
                                    m_textPlugSurrender.setText("У вас нет ключей от квартир");
                                    return;
                                }
                            } else {
                                if (listValue.size() == 4) {
                                    adapter.onItemAdd(listValue);
                                    countFarm++;
                                } else
                                    throw new CException("Ошибка парсера");
                            }
                        }

                        if (countFarm != numberFarm)
                            throw new CException("Ошибка парсера");

                        m_recyclerViewSurrender.setVisibility(View.VISIBLE);
                        m_textPlugSurrender.setText("");

                    } else {
                        throw new CException("Ошибка парсера");
                    }
                } catch (CException error) {
                    m_textPlugSurrender.setText(error.getMessage());
                }
            }*/
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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