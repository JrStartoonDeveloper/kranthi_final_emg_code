package com.startemg.apps.pheezee.activities;

import static startemg.apps.pheezee.R.id.iv_back_app_info;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import startemg.apps.pheezee.R;

public class AppInfo extends AppCompatActivity {


    LinearLayout ll_1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        ImageView button1 = findViewById(iv_back_app_info);
        ll_1 = findViewById(R.id.testing);


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });
    }
}

