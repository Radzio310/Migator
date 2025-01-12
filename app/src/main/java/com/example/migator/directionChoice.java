package com.example.migator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class directionChoice extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_direction_choice);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Zainicjalizuj VideoView
        VideoView videoView = findViewById(R.id.videoView);

        // Ustaw URI wideo "standby"
        Uri standbyVideoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stand_by_3);
        videoView.setVideoURI(standbyVideoUri);

        // Rozpocznij odtwarzanie
        videoView.start();

        // Po zakończeniu, automatycznie odtwórz ponownie wideo "standby"
        videoView.setOnCompletionListener(mp -> {
            videoView.setVideoURI(standbyVideoUri);
            videoView.start();
        });

        List<Pair<String, String>> stopInfo = findStopUtils.findStopInfo(this, getIntent().getStringExtra("BusStopName"));

        /*--------------------HOOKS---------------------*/
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        /*--------------------TOOLBAR---------------------*/
        setSupportActionBar(toolbar);

        /*--------------------NAVIGATION DRAWER MENU---------------------*/

        // Hide or show items
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_home).setVisible(true); // Example

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_searchBusStop);

        if (stopInfo != null && !stopInfo.isEmpty()) {
            // Wyświetl nazwę przystanku (zakładamy, że wszystkie przystanki mają tę samą nazwę)
            String stopName = stopInfo.get(0).first; // Pobieramy nazwę z pierwszego przystanku
            ((TextView) findViewById(R.id.currentBusStopName)).setText(stopName); // Ustawiamy nazwę przystanku w widoku

            LinearLayout resultsContainer = findViewById(R.id.resultsContainer);
            resultsContainer.removeAllViews(); // Usuń istniejące widoki, aby uniknąć duplikacji

            for (Pair<String, String> stop : stopInfo) {
                String number = stop.second;

                if (number != null) {
                    Log.d("Stop Number", "Numer przystanku: " + number);

                    // API callback
                    findStopUtils.getDepartures(number, this, new findStopUtils.DepartureCallback() {
                        @Override
                        public void onDeparturesLoaded(List<DeparturesResponse.Departure> departures) {
                            runOnUiThread(() -> {
                                // Zbierz unikalne kierunki dla danego przystanku
                                Set<String> uniqueDirections = new LinkedHashSet<>();
                                for (DeparturesResponse.Departure departure : departures) {
                                    String direction = departure.getDirection();
                                    if (direction != null && !direction.isEmpty()) {
                                        uniqueDirections.add(direction);
                                    }
                                }

                                // Utwórz ramkę dla unikalnych kierunków danego przystanku
                                TextView stopDirectionView = new TextView(directionChoice.this);

                                // Ustaw układ i marginesy
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                layoutParams.setMargins(16, 10, 16, 10); // Marginesy: lewy, góra, prawy, dół
                                stopDirectionView.setLayoutParams(layoutParams);

                                // Konfiguracja widoku
                                stopDirectionView.setText(String.join(", ", uniqueDirections));
                                stopDirectionView.setPadding(16, 16, 16, 16);
                                stopDirectionView.setTextColor(getResources().getColor(R.color.white));
                                stopDirectionView.setTextSize(18f);
                                stopDirectionView.setBackgroundResource(R.drawable.rounded_blue_background);
                                stopDirectionView.setGravity(View.TEXT_ALIGNMENT_CENTER);

                                // Wyśrodkowanie tekstu
                                stopDirectionView.setGravity(android.view.Gravity.CENTER);

                                // Obsługa kliknięcia - przejście do results lub navigation
                                if (getIntent().getBooleanExtra("navigationFlag", false)) {
                                    stopDirectionView.setOnClickListener(v -> GoTo_Naviagtion(v, number));
                                } else {
                                    stopDirectionView.setOnClickListener(v -> GoTo_busStopResult(v, stopName, number));
                                }

                                // Dodaj ramkę do kontenera
                                resultsContainer.addView(stopDirectionView);
                            });

                        }

                        @Override
                        public void onError(String error) {
                            Log.e("Departure Error", error);
                            runOnUiThread(() -> Toast.makeText(directionChoice.this, "Błąd: " + error, Toast.LENGTH_LONG).show());
                        }
                    });
                } else {
                    Log.d("Stop Number", "Nie znaleziono przystanku.");
                    Toast.makeText(this, "Nie znaleziono przystanku.", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Log.d("Stop Info", "Nie znaleziono przystanku.");
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.nav_home) {
            Intent intent = new Intent(directionChoice.this, MainActivity.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.nav_searchBusStop) {
            Intent intent = new Intent(directionChoice.this, busStopSearch.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.nav_searchLine) {
            Intent intent = new Intent(directionChoice.this, lineSearch.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.nav_map) {
            Intent intent = new Intent(directionChoice.this, MapsActivity.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(directionChoice.this, settings.class);
            startActivity(intent);
        }
        return true;
    }

    public void GoTo_busStopResult(View v, String stopName, String stopNumber) {
        Intent intent = new Intent(this, busStopResult.class);

        intent.putExtra("BusStopName", stopName);
        intent.putExtra("stopNumber", stopNumber);
        startActivity(intent);

    }

    public void GoTo_Naviagtion(View v, String number) {
        Pair<Double, Double> geoInfo = findStopUtils.findGeoInfoByNumber(this, number);
        String latitude = geoInfo.first.toString();
        String longitude = geoInfo.second.toString();
        // Alternatywne URI (żeby się włączało w trybie pieszym)
        Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" +
                Uri.encode(latitude + ", " + longitude) +
                "&travelmode=walking");

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    public void go_back(View v) {
        super.onBackPressed();
    }
}
