package com.example.migator;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import java.util.Iterator;
import java.util.List;

public class lineResult extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    String name;
    String number;
    String line_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_result);
        ((TextView) findViewById(R.id.textView6)).setText(getIntent().getStringExtra("BusLineName"));

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

        navigationView.setCheckedItem(R.id.nav_searchLine);

        // get number and name of busstop
        Pair<String, String> stopInfo = findStopUtils.findStopInfo(this, getIntent().getStringExtra("BusStopName"));
        line_number = getIntent().getStringExtra("BusLineName");

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
                    runOnUiThread(() -> updateResults(departures, line_number));
                }

                @Override
                public void onError(String error) {
                    Log.e("Departure Error", error);
                    runOnUiThread(() -> {
                        Toast.makeText(lineResult.this, "Błąd: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } else {
            Log.d("Stop Number", "Nie znaleziono przystanku.");
            Toast.makeText(this, "Nie znaleziono przystanku.", Toast.LENGTH_LONG).show();
        }


    }

    private void updateResults(List<DeparturesResponse.Departure> departures, String lineNumber) {
        // leave only the stops that have the provided lineNumber
        Iterator<DeparturesResponse.Departure> iterator = departures.iterator();
        while (iterator.hasNext()) {
            DeparturesResponse.Departure departure = iterator.next();
            if (!departure.getLineNumber().equals(lineNumber)) {
                Log.d("test", "usunieto." + departure.getLineNumber()+" "+lineNumber );
                iterator.remove();
            }
        }
        // Set the current bus stop name
        ((TextView) findViewById(R.id.currentBusLine)).setText(name);

        // Arrays for the time, line, and direction IDs
        int[] timeIds = {R.id.timeRemaining1, R.id.timeRemaining2, R.id.timeRemaining3};
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

            // Set the direction text
            ((TextView) findViewById(directionIds[i])).setText("Kierunek: " + departure.getDirection());
        }
        if (departures.isEmpty()) {
            TextView emptyView = ((TextView) findViewById(directionIds[0]));
            emptyView.setText("Brak odjazdów tej linii");
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setTypeface(ResourcesCompat.getFont(this, R.font.baloo), Typeface.ITALIC); // Czcionka Baloo i pochyłość
            emptyView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark)); // Kolor czerwony
            emptyView.setTextSize(16); // Opcjonalnie dostosuj rozmiar tekstu
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
            Intent intent = new Intent(lineResult.this, MainActivity.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_searchBusStop) {
            Intent intent = new Intent(lineResult.this, busStopSearch.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_searchLine) {
            Intent intent = new Intent(lineResult.this, lineSearch.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_map) {
            Intent intent = new Intent(lineResult.this, MapsActivity.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(lineResult.this, settings.class);
            startActivity(intent);
        }

        return true;
    }

    public void GoTo_lineSearch(View v){
        Intent intent = new Intent(this, lineSearch.class);
        startActivity(intent);
    }

    public void GoTo_MainActivity(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }



}

