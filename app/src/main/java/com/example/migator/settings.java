package com.example.migator;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;



import androidx.appcompat.app.AppCompatActivity;

public class settings extends AppCompatActivity{


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

    }





    public void GoTo_MainActivity(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}