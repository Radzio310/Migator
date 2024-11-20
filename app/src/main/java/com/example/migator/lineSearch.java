package com.example.migator;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class lineSearch extends AppCompatActivity{

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_search);
    }


    public void GoTo_lineResult(View v){
        Intent intent = new Intent(this, lineResult.class);
        String busLineNumber = ((EditText) findViewById(R.id.lineNumber)).getText().toString();
        if (busLineNumber.isEmpty()){
            Toast.makeText(this, "Nie podano numeru linii.", Toast.LENGTH_SHORT).show();
        } else {
            intent.putExtra("BusLineNumber", busLineNumber);
            startActivity(intent);
        }
    }

    public void GoTo_MainActivity(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}