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
import android.widget.TextView;
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
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class lineSearch extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    public static class JsonUtils {
        public static List<String> loadLineNumbersFromJson(Context context) {
            try {
                // Użycie metody loadLines do wczytania danych z pliku w pamięci wewnętrznej
                List<Line> lines = findStopUtils.loadLines(context);
                if (lines == null) {
                    Log.e("JsonUtils", "Nie udało się wczytać danych");
                    return null; // Jeśli nie udało się wczytać danych, zwróć null
                }

                // Zwróć listę numerów linii
                List<String> lineNumbers = new ArrayList<>();
                for (Line line : lines) {
                    lineNumbers.add(line.getNumber());
                }

                return lineNumbers;
            } catch (Exception e) {
                Log.e("JsonUtils", "Error loading line numbers from JSON", e);
                return null;
            }
        }

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
        AutoCompleteTextView busStopView = findViewById(R.id.lineBusStop);

        // Ładowanie danych do pól AutoComplete
        List<String> lineNumbers = JsonUtils.loadLineNumbersFromJson(this);
        if (lineNumbers != null) {
            Set<String> uniqueLineNumbers = new HashSet<>(lineNumbers);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, new ArrayList<>(uniqueLineNumbers));
            lineNumberView.setAdapter(adapter);
            lineNumberView.setThreshold(1);
        } else {
            Toast.makeText(this, "Błąd podczas ładowania danych linii", Toast.LENGTH_SHORT).show();
        }

        List<String> stopNames = JsonUtils.loadStopNamesFromJson(this);
        if (stopNames != null) {
            Set<String> uniqueStopNames = new HashSet<>(stopNames);
            ArrayAdapter<String> stopAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, new ArrayList<>(uniqueStopNames));
            busStopView.setAdapter(stopAdapter);
            busStopView.setThreshold(1);
        } else {
            Toast.makeText(this, "Błąd podczas ładowania danych przystanków", Toast.LENGTH_SHORT).show();
        }


        /*-----URUCHAMIANIE WIDEO-----*/
        AtomicInteger flaga = new AtomicInteger(1);

        VideoView videoView = findViewById(R.id.videoView4);
        TextView textView = findViewById(R.id.textView7);
        AtomicReference<Uri> videoUri = new AtomicReference<>(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.wpisz_numer_linii));
        textView.setText("Wpisz numer linii, następnie naciśnij 'Wyszukaj'");
        videoView.setVideoURI(videoUri.get());
        videoView.start();

        videoView.setOnCompletionListener(mp -> {
            if (flaga.get() == 1) {
                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.aby_wrocic_nacisnij_powrot));
                videoView.setVideoURI(videoUri.get());
                videoView.start();
                textView.setText("Aby wrócić na stronę główną, naciśnij przycisk 'Powrót'");
                flaga.getAndIncrement();
            } else if (flaga.get() == 2){
                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stand_by_5));
                videoView.setVideoURI(videoUri.get());
                videoView.start();
                textView.setText("");
            }
        });


        // Obsługa klawisza Enter w polu "numer linii"
        lineNumberView.setOnEditorActionListener((v, actionId, event) -> {
            busStopView.requestFocus(); // Przeniesienie kursora do następnego pola
            return true;
        });

        // Obsługa klawisza Enter w polu "nazwa przystanku"
        busStopView.setOnEditorActionListener((v, actionId, event) -> {
            GoTo_LineResult(v); // Wywołanie metody wyszukiwania
            return true;
        });
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
        Pair<String, String> stopInfo = findStopUtils.findStopInfoSingle(this, busStopName);

        if (lineNumber.isEmpty())
        {
            Toast.makeText(this, "Nie podano linii.", Toast.LENGTH_SHORT).show();
            VideoView videoView = findViewById(R.id.videoView4);
            TextView textView = findViewById(R.id.textView7);
            AtomicReference<Uri> videoUri = new AtomicReference<>(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.brak_pojazdu_o_takim_numerze));
            textView.setText("Brak pojazdu o takim numerze");
            videoView.setVideoURI(videoUri.get());
            videoView.start();
        }
        else if (lineInfo == null)
        {
            Toast.makeText(this, "Nie znaleziono takiej linii.", Toast.LENGTH_SHORT).show();
            VideoView videoView = findViewById(R.id.videoView4);
            TextView textView = findViewById(R.id.textView7);
            AtomicReference<Uri> videoUri = new AtomicReference<>(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.brak_pojazdu_o_takim_numerze));
            textView.setText("Brak pojazdu o takim numerze");
            videoView.setVideoURI(videoUri.get());
            videoView.start();
        }
        else if (busStopName.isEmpty())
        {
            Toast.makeText(this, "Nie podano nazwy przystanku.", Toast.LENGTH_SHORT).show();
            VideoView videoView = findViewById(R.id.videoView4);
            TextView textView = findViewById(R.id.textView7);
            AtomicReference<Uri> videoUri = new AtomicReference<>(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.blad_podczas_wyszukiwania_przystanku));
            textView.setText("Wystąpił błąd");
            videoView.setVideoURI(videoUri.get());
            videoView.start();
        }
        else if (stopInfo == null)
        {
            Toast.makeText(this, "Nie znaleziono takiego przystanku.", Toast.LENGTH_SHORT).show();
            VideoView videoView = findViewById(R.id.videoView4);
            TextView textView = findViewById(R.id.textView7);
            AtomicReference<Uri> videoUri = new AtomicReference<>(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.blad_podczas_wyszukiwania_przystanku));
            textView.setText("Wystąpił błąd");
            videoView.setVideoURI(videoUri.get());
            videoView.start();
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