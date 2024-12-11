package com.example.migator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class lineSearch extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    public static class JsonUtils {
        public static List<String> loadLineNumbersFromJson(Context context) {
            try {
                // Otwórz plik JSON z folderu raw
                InputStream inputStream = context.getResources().openRawResource(R.raw.lines);
                InputStreamReader reader = new InputStreamReader(inputStream);

                // Używamy Gson do sparsowania pliku JSON
                Gson gson = new Gson();
                Type type = new TypeToken<LinesResponse>(){}.getType();
                LinesResponse response = gson.fromJson(reader, type);

                // Zwróć listę numerów linii
                List<Line> lines = response.getData();
                List<String> lineNumbers = new ArrayList<>();
                for (Line line : lines) {
                    lineNumbers.add(line.getNumber());
                }

                return lineNumbers;
            } catch (Exception e) {
                Log.e("JsonUtils", "Error loading lines from JSON", e);
                return null;
            }
        }

        public static List<String> loadStopNamesFromJson(Context context) {
            try {
                // Otwórz plik JSON z folderu raw
                InputStream inputStream = context.getResources().openRawResource(R.raw.stops);
                InputStreamReader reader = new InputStreamReader(inputStream);

                // Używamy Gson do sparsowania pliku JSON
                Gson gson = new Gson();
                Type type = new TypeToken<StopsResponse>(){}.getType();
                StopsResponse response = gson.fromJson(reader, type);

                // Zwróć listę nazw przystanków
                List<Stop> stops = response.getData();
                List<String> stopNames = new ArrayList<>();
                for (Stop stop : stops) {
                    stopNames.add(stop.getName());
                }

                return stopNames;
            } catch (Exception e) {
                Log.e("JsonUtils", "Error loading stops from JSON", e);
                return null;
            }
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_search);

        /*--------------------HOOKS---------------------*/
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        /*--------------------TOOLBAR---------------------*/
        setSupportActionBar(toolbar);

        /*--------------------NAVIGATION DRAWER MENU---------------------*/
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_home).setVisible(true);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_searchLine);

        // Inicjalizacja AutoCompleteTextView dla numeru linii
        AutoCompleteTextView lineNumberView = findViewById(R.id.lineNumber);

        // Ładowanie numerów linii z pliku JSON
        List<String> lineNumbers = JsonUtils.loadLineNumbersFromJson(this);
        if (lineNumbers != null) {
            // Tworzenie adaptera dla AutoCompleteTextView
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lineNumbers);
            lineNumberView.setAdapter(adapter);

            // Wyświetlanie podpowiedzi po wpisaniu jednego znaku
            lineNumberView.setThreshold(1);

            // Obsługa kliknięcia w elementy z listy podpowiedzi
            lineNumberView.setOnItemClickListener((parent, view, position, id) -> {
                // Pobierz wybraną linię z listy
                String selectedLine = parent.getItemAtPosition(position).toString();
                Toast.makeText(this, "Wybrana linia: " + selectedLine, Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Błąd podczas ładowania danych linii", Toast.LENGTH_SHORT).show();
        }

        // Inicjalizacja AutoCompleteTextView dla przystanku
        AutoCompleteTextView busStopView = findViewById(R.id.lineBusStop);

        // Ładowanie nazw przystanków z pliku JSON
        List<String> stopNames = JsonUtils.loadStopNamesFromJson(this);
        if (stopNames != null) {
            // Tworzenie adaptera dla AutoCompleteTextView
            ArrayAdapter<String> stopAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, stopNames);
            busStopView.setAdapter(stopAdapter);

            // Wyświetlanie podpowiedzi po wpisaniu jednego znaku
            busStopView.setThreshold(1);

            // Obsługa kliknięcia w elementy z listy podpowiedzi
            busStopView.setOnItemClickListener((parent, view, position, id) -> {
                // Pobierz wybrany przystanek z listy
                String selectedStop = parent.getItemAtPosition(position).toString();
                Toast.makeText(this, "Wybrany przystanek: " + selectedStop, Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Błąd podczas ładowania danych przystanków", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.nav_home) {
            Intent intent = new Intent(lineSearch.this, MainActivity.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_searchBusStop) {
            Intent intent = new Intent(lineSearch.this, busStopSearch.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_searchLine) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if(menuItem.getItemId() == R.id.nav_map) {
            Intent intent = new Intent(lineSearch.this, MapsActivity.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(lineSearch.this, settings.class);
            startActivity(intent);
        }

        return true;
    }
    public void GoTo_LineResult(View v) {
        Intent intent = new Intent(this, lineResult.class);

        // Pobierz numer linii z AutoCompleteTextView
        String lineNumber = ((AutoCompleteTextView) findViewById(R.id.lineNumber)).getText().toString();
        String lineInfo = findStopUtils.findLineInfo(this, lineNumber);

        String busStopName = ((EditText) findViewById(R.id.lineBusStop)).getText().toString();
        Pair<String, String> stopInfo = findStopUtils.findStopInfo(this, busStopName);

        if (lineNumber.isEmpty())
        {
            Toast.makeText(this, "Nie podano linii.", Toast.LENGTH_SHORT).show();
        }
        else if (lineInfo == null)
        {
            Toast.makeText(this, "Nie znaleziono takiej linii.", Toast.LENGTH_SHORT).show();
        }
        else if (busStopName.isEmpty())
        {
            Toast.makeText(this, "Nie podano nazwy przystanku.", Toast.LENGTH_SHORT).show();
        }
        else if (stopInfo == null)
        {
            Toast.makeText(this, "Nie znaleziono takiego przystanku.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            intent.putExtra("BusLineName", lineNumber);
            intent.putExtra("BusStopName", busStopName);
            startActivity(intent);
        }
    }

    public void GoTo_MainActivity(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}