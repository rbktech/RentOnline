package ru.rbkdev.lease.account.login;

import ru.rbkdev.lease.R;

import ru.rbkdev.lease.main.CID;
import ru.rbkdev.lease.main.CMainActivity;
import ru.rbkdev.lease.account.login.sign.CSignActivity;
import ru.rbkdev.lease.account.login.create.CCreateActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class CLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_login_activity);

        Button btnCreateNow = findViewById(R.id.btnCreateNow);
        Button btnSignAccount = findViewById(R.id.btnSignAccount);
        TextView txtMessage = findViewById(R.id.txtAccountLoginMessage);
        txtMessage.setText("");

        Intent intent = getIntent();
        if(intent != null) {

            int result = intent.getIntExtra(CID.INTENT_RESULT, 0);
            if(result != 0) {

                String message = intent.getStringExtra(CID.INTENT_MESSAGE);
                if(message != null)
                    txtMessage.setText(message);

                if(result == -1 || result == 2) {
                    btnCreateNow.setVisibility(View.INVISIBLE);
                    btnSignAccount.setVisibility(View.INVISIBLE);
                }
            }
        }

        btnSignAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CLoginActivity.this, CSignActivity.class);
                startActivityForResult(intent, CID.RESULT_RETURN_SIGN);
            }
        });

        btnCreateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CLoginActivity.this, CCreateActivity.class);
                startActivityForResult(intent, CID.RESULT_RETURN_CREATE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        String valueIntent;

        if(resultCode == RESULT_OK) {

            File file = new File(getExternalFilesDir(null), CID.FILE_NAME);
            if(file.delete() || !file.delete()) {
                if(requestCode == CID.RESULT_RETURN_CREATE) {
                    valueIntent = intent.getStringExtra(CID.INTENT_USER_MAIL);
                    CMainActivity.RefreshUserId(file, "AM" + valueIntent);
                    setResult(RESULT_OK);
                    finish();
                }

                if(requestCode == CID.RESULT_RETURN_SIGN) {
                    valueIntent = intent.getStringExtra(CID.INTENT_USER_ID);

                    CMainActivity.RefreshUserId(file, valueIntent);
                    finish();
                }
            }
        }
    }
}
