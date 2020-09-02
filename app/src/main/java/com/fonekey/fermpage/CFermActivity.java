package com.fonekey.fermpage;
import com.fonekey.R;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CFermActivity extends AppCompatActivity {

    CFermActivity(){};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_ferm);
    }
}
