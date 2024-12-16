package com.example.migator;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class busStopSearch extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    public static class JsonUtils {

        public static List<String> loadStopNamesFromJson(Context context) {
            try {
                // Otwórz plik JSON z folderu raw
                InputStream inputStream = context.getResources().openRawResource(R.raw.stops); // Załaduj plik JSON
                InputStreamReader reader = new InputStreamReader(inputStream);

                // Używamy Gson do sparsowania pliku JSON
                Gson gson = new Gson();
                Type type = new TypeToken<StopsResponse>(){}.getType(); // Dopasuj do swojej struktury JSON
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
        setContentView(R.layout.activity_bus_stop_search);

        /*--------------------HOOKS---------------------*/
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        /*--------------------TOOLBAR---------------------*/
        setSupportActionBar(toolbar);

        /*--------------------NAVIGATION DRAWER MENU---------------------*/

        //hide or show items
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_home).setVisible(true); //przykład

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_searchBusStop);

        // Inicjalizacja AutoCompleteTextView dla przystanku
        AutoCompleteTextView busStopView = findViewById(R.id.busStopName);

        // Ładowanie nazw przystanków z pliku JSON
        List<String> stopNames = JsonUtils.loadStopNamesFromJson(this);
        if (stopNames != null) {
            // Użycie HashSet do usunięcia duplikatów
            Set<String> uniqueStopNames = new HashSet<>(stopNames);

            // Tworzenie adaptera z niestandardowym układem
            ArrayAdapter<String> stopAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, new ArrayList<>(uniqueStopNames));
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

        // Obsługa klawisza Enter w polu "nazwa przystanku"
        busStopView.setOnEditorActionListener((v, actionId, event) -> {
            GoTo_busStopResult(v); // Wywołanie metody wyszukiwania
            return true;
        });

    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.nav_home) {
            Intent intent = new Intent(busStopSearch.this, MainActivity.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_searchBusStop) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if(menuItem.getItemId() == R.id.nav_searchLine) {
            Intent intent = new Intent(busStopSearch.this, lineSearch.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_map) {
            Intent intent = new Intent(busStopSearch.this, MapsActivity.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(busStopSearch.this, settings.class);
            startActivity(intent);
        }

        return true;
    }

    public void GoTo_busStopResult(View v) {
        Intent intent = new Intent(this, busStopResult.class);

        String busStopName = ((EditText) findViewById(R.id.busStopName)).getText().toString();
        Pair<String, String> stopInfo = findStopUtils.findStopInfo(this, busStopName);

        if (busStopName.isEmpty()) {
            Toast.makeText(this, "Nie podano nazwy przystanku.", Toast.LENGTH_SHORT).show();
        } else if (stopInfo == null) {
            Toast.makeText(this, "Nie znaleziono takiego przystanku.", Toast.LENGTH_SHORT).show();
        } else {
            intent.putExtra("BusStopName", busStopName);
            startActivity(intent);
        }
    }

    public void GoTo_Naviagtion(View v) {
        String busStopName = ((EditText) findViewById(R.id.busStopName)).getText().toString();
        busStopName = busStopName.trim();
        Pair<String, String> stopInfo = findStopUtils.findStopInfo(this, busStopName);

        if (busStopName.isEmpty()) {
            Toast.makeText(this, "Nie podano nazwy przystanku.", Toast.LENGTH_SHORT).show();
        } else if (stopInfo == null) {
            Toast.makeText(this, "Nie znaleziono takiego przystanku.", Toast.LENGTH_SHORT).show();
        } else {
            String name = stopInfo.first;
            // Alternatywne URI (żeby się włączało w trybie pieszym)
            Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" +
                    Uri.encode("Szczecin, przystanek autobusowy " + name) +
                    "&travelmode=walking");

            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
    }

    public void GoTo_MainActivity(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
