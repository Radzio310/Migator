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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.util.Log;
import android.widget.VideoView;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import java.util.Map;
import java.util.stream.Collectors;

public class lineSearch extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    private List<Map<String, Object>> stops2;
    private List<Map<String, Object>> lines2;

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

        // Wczytaj stops_2.json i lines_2.json
        stops2 = loadFromFile("stops_2.json");
        lines2 = loadFromFile("lines_2.json");

        // Inicjalizacja pól AutoCompleteTextView
        AutoCompleteTextView lineNumberView = findViewById(R.id.lineNumber);
        AutoCompleteTextView busStopView = findViewById(R.id.lineBusStop);

        setupLineAutoComplete(lineNumberView, busStopView);
        setupBusStopAutoComplete(busStopView, lineNumberView);


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

    private void setupLineAutoComplete(AutoCompleteTextView lineNumberView, AutoCompleteTextView busStopView) {
        List<String> lineNumbers = JsonUtils.loadLineNumbersFromJson(this);

        if (lineNumbers != null) {
            // Usunięcie duplikatów za pomocą LinkedHashSet
            Set<String> uniqueLineNumbers = new LinkedHashSet<>(lineNumbers);

            // Ustawienie adaptera z unikalnymi wartościami
            lineNumberView.setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_item, new ArrayList<>(uniqueLineNumbers)));
            lineNumberView.setThreshold(1);
        }

        lineNumberView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLine = parent.getItemAtPosition(position).toString();
            Toast.makeText(this, "Wybrana linia: " + selectedLine, Toast.LENGTH_SHORT).show();

            // Zaktualizuj autopodpowiedzi dla przystanków na podstawie wybranej linii
            if (isLineInLines2(selectedLine)) {
                List<String> validStops = getStopsForLine(selectedLine);

                // Usunięcie duplikatów z listy przystanków
                Set<String> uniqueStops = new LinkedHashSet<>(validStops);

                busStopView.setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_item, new ArrayList<>(uniqueStops)));
            } else {
                // Resetuj do standardowych przystanków
                List<String> stopNames = JsonUtils.loadStopNamesFromJson(this);

                // Usunięcie duplikatów z listy przystanków
                Set<String> uniqueStopNames = new LinkedHashSet<>(stopNames);

                busStopView.setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_item, new ArrayList<>(uniqueStopNames)));
            }
        });
    }


    private void setupBusStopAutoComplete(AutoCompleteTextView busStopView, AutoCompleteTextView lineNumberView) {
        List<String> stopNames = JsonUtils.loadStopNamesFromJson(this);

        if (stopNames != null) {
            // Usunięcie duplikatów za pomocą LinkedHashSet
            Set<String> uniqueStopNames = new LinkedHashSet<>(stopNames);

            // Ustawienie adaptera z unikalnymi wartościami
            busStopView.setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_item, new ArrayList<>(uniqueStopNames)));
            busStopView.setThreshold(1);
        }

        busStopView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedStop = parent.getItemAtPosition(position).toString();
            Toast.makeText(this, "Wybrany przystanek: " + selectedStop, Toast.LENGTH_SHORT).show();

            // Zaktualizuj autopodpowiedzi dla linii na podstawie wybranego przystanku
            if (isStopInStops2(selectedStop)) {
                List<String> validLines = getLinesForStop(selectedStop);

                // Usunięcie duplikatów z listy linii
                Set<String> uniqueLines = new LinkedHashSet<>(validLines);

                lineNumberView.setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_item, new ArrayList<>(uniqueLines)));
            } else {
                // Resetuj do standardowych linii
                List<String> lineNumbers = JsonUtils.loadLineNumbersFromJson(this);

                // Usunięcie duplikatów z listy linii
                Set<String> uniqueLineNumbers = new LinkedHashSet<>(lineNumbers);

                lineNumberView.setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_item, new ArrayList<>(uniqueLineNumbers)));
            }
        });
    }


    private boolean isStopInStops2(String stopName) {
        // Sprawdź, czy przystanek istnieje w stops_2.json
        return stops2.stream().anyMatch(stop -> stop.get("name").equals(stopName));
    }

    private boolean isLineInLines2(String lineName) {
        // Sprawdź, czy linia istnieje w lines_2.json
        return lines2.stream().anyMatch(line -> line.get("name").equals(lineName));
    }

    private List<String> getLinesForStop(String stopName) {
        // Pobierz listę linii dla danego przystanku z stops_2.json
        return stops2.stream()
                .filter(stop -> stop.get("name").equals(stopName))
                .flatMap(stop -> ((List<String>) stop.get("lines")).stream())
                .collect(Collectors.toList());
    }

    private List<String> getStopsForLine(String lineName) {
        // Pobierz listę przystanków dla danej linii z lines_2.json
        return lines2.stream()
                .filter(line -> line.get("name").equals(lineName))
                .flatMap(line -> ((List<String>) line.get("stops")).stream())
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> loadFromFile(String fileName) {
        try (InputStreamReader reader = new InputStreamReader(openFileInput(fileName))) {
            return new Gson().fromJson(reader, new TypeToken<List<Map<String, Object>>>() {}.getType());
        } catch (Exception e) {
            Log.e("ERROR", "Błąd wczytywania pliku: " + fileName, e);
            return new ArrayList<>();
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