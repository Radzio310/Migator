package com.example.migator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class busStopResult extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

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

        //hide or show items
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_home).setVisible(true); //przykład


        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_searchBusStop);

        // get number and name of busstop
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

            // API callbackiem
            findStopUtils.getDepartures(number, this, new findStopUtils.DepartureCallback() {
                @Override
                public void onDeparturesLoaded(List<DeparturesResponse.Departure> departures) {
                    runOnUiThread(() -> updateResults(departures));
                }

                @Override
                public void onError(String error) {
                    Log.e("Departure Error", error);
                    runOnUiThread(() -> {
                        Toast.makeText(busStopResult.this, "Błąd: " + error, Toast.LENGTH_LONG).show();
                    });
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

        // Arrays for the time, line, and direction IDs
        int[] timeIds = {R.id.timeRemaining1, R.id.timeRemaining2, R.id.timeRemaining3};
        int[] lineIds = {R.id.lineNumber1, R.id.lineNumber2, R.id.lineNumber3};
        int[] directionIds = {R.id.direction1, R.id.direction2, R.id.direction3};

        // Loop through the list of departures, processing up to 3 departures
        for (int i = 0; i < departures.size() && i < 3; i++) {
            // Get the departure details
            DeparturesResponse.Departure departure = departures.get(i);

            // Set the time text
            String timeText = departure.getTime().contains(":") ?
                    "Odjazd " + departure.getTime() :
                    "Odjazd za " + departure.getTime() + "min";
            ((TextView) findViewById(timeIds[i])).setText(timeText);

            // Set the line number text
            ((TextView) findViewById(lineIds[i])).setText("Linia " + departure.getLineNumber());

            // Set the direction text
            ((TextView) findViewById(directionIds[i])).setText("Kierunek: " + departure.getDirection());
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
            Intent intent = new Intent(busStopResult.this, MainActivity.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_searchBusStop) {
            Intent intent = new Intent(busStopResult.this, busStopSearch.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_searchLine) {
            Intent intent = new Intent(busStopResult.this, lineSearch.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_map) {
            //Intent intent = new Intent(busStopResult.this, busStopMap.class);
            //startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START); //na razie póki nie ma innych ekranów
        } else if(menuItem.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(busStopResult.this, settings.class);
            startActivity(intent);
        }

        return true;
    }

    public void GoTo_busStopSearch(View v){
        Intent intent = new Intent(this, busStopSearch.class);
        startActivity(intent);
    }
}