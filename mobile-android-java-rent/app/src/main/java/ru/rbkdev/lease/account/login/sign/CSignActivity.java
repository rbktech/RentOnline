package ru.rbkdev.lease.account.login.sign;
import ru.rbkdev.lease.main.CID;
import ru.rbkdev.lease.R;
import ru.rbkdev.lease.main.CClient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class CSignActivity extends AppCompatActivity {

    EditText m_txtMail;
    EditText m_txtPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_login_sign_activity);

        m_txtMail = findViewById(R.id.txtAccountLoginSignMail);
        m_txtPassword = findViewById(R.id.txtAccountLoginSignPassword);

        Button btnOk = findViewById(R.id.btnAccountLoginSignOk);
        btnOk.setOnClickListener(CommandSignIn);
    }

    View.OnClickListener CommandSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayList<byte[]> data = new ArrayList<>();
            data.add(m_txtMail.getText().toString().getBytes());
            data.add(m_txtPassword.getText().toString().getBytes());

            // Command - 'E'
            ArrayList<ArrayList<ByteArrayOutputStream>> bundle = CClient.DataExchange(data, (byte)0x45, CSignActivity.this);
            if(bundle != null) {
                Intent intent = new Intent();
                intent.putExtra(CID.INTENT_USER_ID, bundle.get(1).get(1).toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    };
}