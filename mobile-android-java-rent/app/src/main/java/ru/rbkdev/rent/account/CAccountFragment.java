package ru.rbkdev.rent.account;
import ru.rbkdev.rent.main.CClient;
import ru.rbkdev.rent.main.CID;
import ru.rbkdev.rent.R;
import ru.rbkdev.rent.account.login.CLoginActivity;
import ru.rbkdev.rent.main.CMainActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import static ru.rbkdev.rent.main.CMainActivity.g_userId;

public class CAccountFragment extends Fragment {

    CMainActivity m_activity;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        m_activity = (CMainActivity)getActivity();
        if(m_activity == null)
            return null;

        if(!g_userId.isEmpty()) {
            if(g_userId.charAt(0) == 'A') {

                Intent intent = new Intent(m_activity, CLoginActivity.class);

                if(CMainActivity.g_userId.length() > 1 && CMainActivity.g_userId.charAt(1) == 'M') {

                    int result = CommandCheckAccount(m_activity);
                    intent.putExtra(CID.INTENT_RESULT, result);

                    if(result == -1) {

                        intent.putExtra(CID.INTENT_MESSAGE, "Нет связи сервером");
                        m_activity.startActivityForResult(intent, CID.RESULT_RETURN_LOGIN);
                    } else if(result == 1) {

                        intent.putExtra(CID.INTENT_MESSAGE, "Отказано в регистрации");
                        m_activity.startActivityForResult(intent, CID.RESULT_RETURN_LOGIN);
                    } else if(result == 2) {

                        intent.putExtra(CID.INTENT_MESSAGE, "Ваш аккаунт\nеще не создан");
                        m_activity.startActivityForResult(intent, CID.RESULT_RETURN_LOGIN);
                    }

                } else
                    m_activity.startActivityForResult(intent, CID.RESULT_RETURN_LOGIN);
            }
        }

        View view = inflater.inflate(R.layout.account_fragment, container, false);

        Button btnExit = view.findViewById(R.id.btnAccountExit);

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_activity.RefreshAccount("A");
            }
        });

        return view;
    }

    int CommandCheckAccount(Context context) {

        ArrayList<byte[]> data = new ArrayList<>();
        data.add(CMainActivity.g_userId.getBytes());

        // Command "С"
        ArrayList<ArrayList<ByteArrayOutputStream>> bundle = CClient.DataExchange(data, (byte) 0x43, context);
        if(bundle != null) {

            byte[] result = bundle.get(1).get(1).toByteArray();

            if(result[0] == 0) {
                g_userId = bundle.get(1).get(2).toString();
                m_activity.RefreshAccount(g_userId);
            }

            return result[0];
        } else
            return -1;

        // return 0; // Успех

        // return 1; // Отказано

        // return 2; // Проверка

        // return -1; // Ошибка соединения
    }
}
