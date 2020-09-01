package com.fonekey.mainpage;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fonekey.R;

public class CSearchActivity extends AppCompatActivity {

    public CSearchActivity() {};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_search);
    }
}
