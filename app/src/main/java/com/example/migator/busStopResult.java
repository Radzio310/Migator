package com.example.migator;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class busStopResult extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    String name;
    String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop_result);

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

        // Get number and name of the bus stop
        Pair<String, String> stopInfo = findStopUtils.findStopInfo(this, getIntent().getStringExtra("BusStopName"));

        if (stopInfo != null) {
            name = stopInfo.first;
            number = stopInfo.second;

            Log.d("Stop Info", "Stop Name: " + name + ", Stop Number: " + number);
        } else {
            Log.d("Stop Info", "Nie znaleziono przystanku.");
        }

        if (number != null) {
            Log.d("Stop Number", "Numer przystanku: " + number);

            // API callback
            findStopUtils.getDepartures(number, this, new findStopUtils.DepartureCallback() {
                @Override
                public void onDeparturesLoaded(List<DeparturesResponse.Departure> departures) {
                    runOnUiThread(() -> updateResults(departures));
                }

                @Override
                public void onError(String error) {
                    Log.e("Departure Error", error);
                    runOnUiThread(() -> Toast.makeText(busStopResult.this, "Błąd: " + error, Toast.LENGTH_LONG).show());
                }
            });
        } else {
            Log.d("Stop Number", "Nie znaleziono przystanku.");
            Toast.makeText(this, "Nie znaleziono przystanku.", Toast.LENGTH_LONG).show();
        }
    }

    private void updateResults(List<DeparturesResponse.Departure> departures) {
        // Set the current bus stop name
        ((TextView) findViewById(R.id.currentBusStopName)).setText(name);

        // Get the container for results
        LinearLayout resultsContainer = findViewById(R.id.resultsContainer);
        resultsContainer.removeAllViews(); // Clear existing results

        // Add up to 5 departures
        int maxResults = 5;
        for (int i = 0; i < departures.size() && i < maxResults; i++) {
            DeparturesResponse.Departure departure = departures.get(i);

            // Create a new horizontal layout for each result
            LinearLayout resultLayout = new LinearLayout(this);
            resultLayout.setOrientation(LinearLayout.HORIZONTAL);
            resultLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            resultLayout.setPadding(0, 8, 0, 8);

            // Time TextView
            TextView timeView = new TextView(this);
            timeView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            timeView.setGravity(Gravity.CENTER); // Ustawienie wyśrodkowania
            String timeText = departure.getTime().contains(":") ? departure.getTime() : "za " + departure.getTime() + "min";
            timeView.setText(timeText);
            resultLayout.addView(timeView);

// Line Number TextView
            TextView lineView = new TextView(this);
            lineView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            lineView.setGravity(Gravity.CENTER); // Ustawienie wyśrodkowania
            lineView.setText("Linia " + departure.getLineNumber());
            resultLayout.addView(lineView);

// Direction TextView
            TextView directionView = new TextView(this);
            directionView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
            directionView.setGravity(Gravity.CENTER); // Ustawienie wyśrodkowania
            directionView.setText(departure.getDirection());
            resultLayout.addView(directionView);


            // Add the result layout to the container
            resultsContainer.addView(resultLayout);
        }

        // If no departures are found
        if (departures.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Brak odjazdów w najbliższym czasie");
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setTypeface(ResourcesCompat.getFont(this, R.font.baloo), Typeface.ITALIC); // Czcionka Baloo i pochyłość
            emptyView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark)); // Kolor czerwony
            emptyView.setTextSize(16); // Opcjonalnie dostosuj rozmiar tekstu
            resultsContainer.addView(emptyView);
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
            Intent intent = new Intent(busStopResult.this, MainActivity.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.nav_searchBusStop) {
            Intent intent = new Intent(busStopResult.this, busStopSearch.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.nav_searchLine) {
            Intent intent = new Intent(busStopResult.this, lineSearch.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.nav_map) {
            Intent intent = new Intent(busStopResult.this, MapsActivity.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(busStopResult.this, settings.class);
            startActivity(intent);
        }
        return true;
    }

    public void GoTo_busStopSearch(View v) {
        Intent intent = new Intent(this, busStopSearch.class);
        startActivity(intent);
    }
}