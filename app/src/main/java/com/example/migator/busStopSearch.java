package com.example.migator;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.example.migator.findStopUtils;

public class busStopSearch extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    public static class JsonUtils {

        public static List<String> loadStopNamesFromJson(Context context) {

            try {
                List<Stop> stops = findStopUtils.loadStops(context);

                // Zwróć listę nazw przystanków

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

        /*-----URUCHAMIANIE WIDEO-----*/
        AtomicInteger flaga = new AtomicInteger(1);

        VideoView videoView = findViewById(R.id.videoView3);
        TextView textView = findViewById(R.id.textView7);
        AtomicReference<Uri> videoUri = new AtomicReference<>(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.wpisz_nazwe_przystanku));
        textView.setText("Wpisz nazwę przystanku, następnie naciśnij 'Wyszukaj'");
        videoView.setVideoURI(videoUri.get());
        videoView.start();

        videoView.setOnCompletionListener(mp -> {
            if (flaga.get() == 1) {
                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.nacisnij_nawiguj));
                videoView.setVideoURI(videoUri.get());
                videoView.start();
                textView.setText("Jeśli chcesz dotrzeć na przystanek, naciśnij przycisk 'Nawiguj'");
                flaga.getAndIncrement();
            } else if (flaga.get() == 2){
                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.aby_wrocic_nacisnij_powrot));
                videoView.setVideoURI(videoUri.get());
                videoView.start();
                textView.setText("Aby wrócić na stronę główną, naciśnij przycisk 'Powrót'");
                flaga.getAndIncrement();
            } else if (flaga.get() == 3){
                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stand_by_3));
                videoView.setVideoURI(videoUri.get());
                videoView.start();
                textView.setText("");
            }
        });




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
        List<Pair<String, String>> stopInfo = findStopUtils.findStopInfo(this, busStopName);



        if (busStopName.isEmpty()) {
            Toast.makeText(this, "Nie podano nazwy przystanku.", Toast.LENGTH_SHORT).show();
        } else if (stopInfo == null) {
            Toast.makeText(this, "Nie znaleziono takiego przystanku.", Toast.LENGTH_SHORT).show();
        } else if (stopInfo.size() >= 2) {
             GoTo_directionChoice(v);
        } else {
            intent.putExtra("BusStopName", busStopName);
            startActivity(intent);
        }
    }

    public void GoTo_directionChoice(View v) {
        Intent intent = new Intent(this, directionChoice.class);

        String busStopName = ((EditText) findViewById(R.id.busStopName)).getText().toString();
        List<Pair<String, String>> stopInfo = findStopUtils.findStopInfo(this, busStopName);

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

        // Znajdujemy informacje o przystankach
        List<Pair<Double, Double>> geoInfoList = findStopUtils.findGeoInfo(this, busStopName);

        if (busStopName.isEmpty()) {
            Toast.makeText(this, "Nie podano nazwy przystanku.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (geoInfoList.isEmpty()) {
            Toast.makeText(this, "Nie znaleziono takiego przystanku.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Jeśli tylko 1 przystanek, od razu przekierowujemy do nawigacji
        if (geoInfoList.size() == 1) {
            Pair<Double, Double> geoInfo = geoInfoList.get(0);
            String latitude = geoInfo.first.toString();
            String longitude = geoInfo.second.toString();

            // Budowanie URL do Google Maps w trybie pieszym
            Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" +
                    Uri.encode(latitude + ", " + longitude) +
                    "&travelmode=walking");

            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } else {
            // Jeśli więcej niż jeden przystanek, przekierowujemy użytkownika do wyboru kierunku
            Intent intent = new Intent(this, directionChoice.class);
            intent.putExtra("BusStopName", busStopName);
            intent.putExtra("navigationFlag", true);  // Flaga informująca o nawigacji
            startActivity(intent);
        }
    }

    public void GoTo_MainActivity(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
