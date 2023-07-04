package com.startemg.apps.pheezee.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import startemg.apps.pheezee.R;


public class MyAccountPannel extends AppCompatActivity {



        @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account_pannel);




        //Back Button
        ImageView button = findViewById(R.id.iv_back_phizio_profile);
        button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        finish();
        }
        });
        //Change Password button

        LinearLayout app_layer = (LinearLayout) findViewById (R.id.lchange_password_btr);
        app_layer.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), ChangePassword.class);
        startActivity(intent);
        }
        });
        //Delete Account Button
                LinearLayout app_layer2 = (LinearLayout) findViewById (R.id.ldelete_btr);
                app_layer2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), DeleteAccountOne.class);
                                startActivity(intent);
                        }
                });



        // App_Info
                LinearLayout app_layer3 = (LinearLayout) findViewById (R.id.lapp_info);
                app_layer3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                Intent intent = new Intent(getApplicationContext(), AppInfo.class);
                                startActivity(intent);
                        }
                });

        //Term and Conditions
//        ImageView button3 = findViewById(iv_go3);
//        button3.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//        Intent intent = new Intent(getApplicationContext(), TeamsAndConditions.class);
//        startActivity(intent);
//        }
//        });
//       Rate Us Button
//        ImageView button3 = findViewById(iv_go3);
//        button3.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//        Intent intent = new Intent(getApplicationContext(), TeamsAndConditions.class);
//        startActivity(intent);
//        }
//        });
        //Warrenty Details
//        ImageView button4 = findViewById(iv_go4);
//        button4.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//
//        }
//        });

        }
        }

