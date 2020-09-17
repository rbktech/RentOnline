package com.fonekey.fermpage;
import com.fonekey.R;
import com.fonekey.searchpage.CSearchActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class CFermActivity extends AppCompatActivity {

    public CFermActivity() {};

    TextView m_street;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_ferm);
        Button btnOk = (Button) findViewById(R.id.btnSaveFerm);
        m_street = (TextView) findViewById(R.id.txtStreetFerm);
        btnOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                intent.putExtra("street", m_street.getText());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }
}
