package com.example.migator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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

import org.w3c.dom.Text;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Call;

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
            fetchStops();  // Pobierz stops.json
            fetchLines();  // Pobierz lines.json
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
                    saveStopsToFile(stops);
                } else {
                    System.out.println("Błąd w odpowiedzi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<StopsResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void saveStopsToFile(List<Stop> stops) {
        String json = new com.google.gson.Gson().toJson(stops);

        try (FileOutputStream fos = openFileOutput("stops.json", Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
            System.out.println("Dane zapisane w pliku: " + "stops.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchLines() {
        ApiClient.ApiService apiService = ApiClient.getApiService();

        Call<LinesResponse> call = apiService.getLines();
        call.enqueue(new Callback<LinesResponse>() {
            @Override
            public void onResponse(Call<LinesResponse> call, Response<LinesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Line> lines = response.body().getData();
                    saveLinesToFile(lines);
                } else {
                    System.out.println("Błąd w odpowiedzi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LinesResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void saveLinesToFile(List<Line> lines) {
        String json = new com.google.gson.Gson().toJson(lines);

        try (FileOutputStream fos = openFileOutput("lines.json", Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
            System.out.println("Dane zapisane w pliku: " + "lines.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}