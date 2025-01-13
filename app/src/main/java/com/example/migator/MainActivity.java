package com.example.migator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    private static boolean dataFetched = false; // Flaga sesji

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sprawdź, czy dane zostały już pobrane w tej sesji
        if (!dataFetched) {
            fetchStops(); // Pobierz stops.json
            fetchLines(); // Pobierz lines.json
            generateFiles(); // Generuj lines_2.json i stops_2.json
            dataFetched = true; // Ustaw flagę na true
        }

        // Załaduj preferencje o trybie (ciemnym lub jasnym)
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false); // Pobierz zapisany stan

        // Ustaw tryb aplikacji (ciemny/jasny)
        setAppTheme(isDarkMode);

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

        navigationView.setCheckedItem(R.id.nav_home);


        /*-----URUCHAMIANIE WIDEO-----*/
        AtomicInteger flaga = new AtomicInteger(1); // flaga do wybierania filmu do odpalenia

        VideoView videoView = findViewById(R.id.videoView);
        TextView textView = findViewById(R.id.textView2);
        AtomicReference<Uri> videoUri = new AtomicReference<>(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.witaj_w_migator)); // ustawienie filmu

        videoView.setVideoURI(videoUri.get());
        videoView.start(); // uruchomienie filmu
        textView.setText("Witaj w aplikacji Migator!");

        videoView.setOnCompletionListener(mp -> { // czekanie az sie zakonczy obecny film
            if (flaga.get() == 1) { // sprawdzenie czy zakonczyl sie pierwszy film
                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.jestes_na_glownej)); // ustawienie drugiego filmu
                videoView.setVideoURI(videoUri.get());
                videoView.start();
                textView.setText("Jesteś teraz na stronie głównej aplikacji");
                flaga.getAndIncrement(); // zwiekszenie flagi
            } else if (flaga.get() == 2) {
                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.na_gorze_menu)); // ustawienie drugiego filmu
                videoView.setVideoURI(videoUri.get());
                videoView.start();
                textView.setText("Na górnym pasku znajdziesz rozwijane menu, które pozwala poruszać się po całej aplikacji");
                flaga.getAndIncrement();
            } else if (flaga.get() == 3){
                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.kliknij_aby_wybrac_przystanek)); // ustawienie drugiego filmu
                videoView.setVideoURI(videoUri.get());
                videoView.start();
                textView.setText("Kliknij w przycisk poniżej aby wybrać przystanek");
                flaga.getAndIncrement();
            } else if (flaga.get() == 4){
                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stand_by_5));
                videoView.setVideoURI(videoUri.get());
                videoView.start();
                textView.setText("");
            }
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

    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.nav_home) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if(menuItem.getItemId() == R.id.nav_searchBusStop) {
            Intent intent = new Intent(MainActivity.this, busStopSearch.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_searchLine) {
            Intent intent = new Intent(MainActivity.this, lineSearch.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_map) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this, settings.class);
            startActivity(intent);
        }

        return true;
    }

    public void GoTo_busStopSearch(View v){
        Intent intent = new Intent(this, busStopSearch.class);
        startActivity(intent);
    }

    public void GoTo_busLineSearch(View v){
        Intent intent = new Intent(this, lineSearch.class);
        startActivity(intent);
    }

    public void GoTo_Map(View v){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    // Metoda do ustawiania motywu aplikacji
    private void setAppTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    // dynamiczne pobieranie stops.json
    private void fetchStops() {
        ApiClient.ApiService apiService = ApiClient.getApiService();

        Call<StopsResponse> call = apiService.getStops();
        call.enqueue(new Callback<StopsResponse>() {
            @Override
            public void onResponse(Call<StopsResponse> call, Response<StopsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Stop> stops = response.body().getData();
                    saveToFile("stops.json", stops);
                } else {
                    Log.e("API", "Błąd w odpowiedzi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<StopsResponse> call, Throwable t) {
                Log.e("API", "Błąd pobierania stops.json", t);
            }
        });
    }

    private void fetchLines() {
        ApiClient.ApiService apiService = ApiClient.getApiService();

        Call<LinesResponse> call = apiService.getLines();
        call.enqueue(new Callback<LinesResponse>() {
            @Override
            public void onResponse(Call<LinesResponse> call, Response<LinesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Line> lines = response.body().getData();
                    saveToFile("lines.json", lines);
                } else {
                    Log.e("API", "Błąd w odpowiedzi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LinesResponse> call, Throwable t) {
                Log.e("API", "Błąd pobierania lines.json", t);
            }
        });
    }

    private void generateFiles() {
        try {
            // Wczytaj dane z pamięci wewnętrznej
            List<Stop> stops = loadFromFile("stops.json", new TypeToken<List<Stop>>() {}.getType());
            List<Line> lines = loadFromFile("lines.json", new TypeToken<List<Line>>() {}.getType());

            // Mapy do przechowywania przystanków i linii
            Map<String, Set<String>> lineToStopsMap = new HashMap<>();
            Map<String, Set<String>> stopToLinesMap = new HashMap<>();

            for (Line line : lines) {
                String lineId = String.valueOf(line.getId());
                Trajectory trajectory = fetchTrajectory(lineId);

                if (trajectory != null) {
                    for (Stop stop : stops) {
                        if (isStopOnTrajectory(stop, trajectory)) {
                            lineToStopsMap.computeIfAbsent(line.getNumber(), k -> new HashSet<>()).add(stop.getName());
                            stopToLinesMap.computeIfAbsent(stop.getName(), k -> new HashSet<>()).add(line.getNumber());
                        }
                    }
                }
            }

            // Generuj dane wyjściowe
            List<Map<String, Object>> stopsJson = stopToLinesMap.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", entry.getKey());
                        map.put("lines", new ArrayList<>(entry.getValue()));
                        return map;
                    })
                    .collect(Collectors.toList());


            List<Map<String, Object>> linesJson = lineToStopsMap.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", entry.getKey());
                        map.put("stops", new ArrayList<>(entry.getValue()));
                        return map;
                    })
                    .collect(Collectors.toList());


            saveToFile("stops_2.json", stopsJson);
            saveToFile("lines_2.json", linesJson);

        } catch (Exception e) {
            Log.e("ERROR", "Błąd generowania plików lines_2.json i stops_2.json", e);
        }
    }

    private Trajectory fetchTrajectory(String lineId) {
        ApiClient.ApiService apiService = ApiClient.getApiService();
        try {
            Response<Trajectory> response = apiService.getTrajectory(lineId).execute();
            return response.isSuccessful() ? response.body() : null;
        } catch (IOException e) {
            Log.e("API", "Błąd pobierania trajektorii dla linii " + lineId, e);
            return null;
        }
    }

    private boolean isStopOnTrajectory(Stop stop, Trajectory trajectory) {
        for (Trajectory.Feature feature : trajectory.getFeatures()) {
            for (List<Double> coordinate : feature.getGeometry().getCoordinates()) {
                double lat = coordinate.get(1);
                double lon = coordinate.get(0);
                if (Math.abs(stop.getLatitude() - lat) < 0.0005 && Math.abs(stop.getLongitude() - lon) < 0.0005) {
                    return true;
                }
            }
        }
        return false;
    }

    private <T> T loadFromFile(String fileName, Type type) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(openFileInput(fileName))) {
            return new Gson().fromJson(reader, type);
        }
    }

    private void saveToFile(String fileName, Object data) {
        String json = new Gson().toJson(data);
        try (FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
            Log.d("FILE", "Zapisano dane do pliku: " + fileName);
        } catch (IOException e) {
            Log.e("FILE", "Błąd zapisu do pliku: " + fileName, e);
        }
    }
}