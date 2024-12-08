package com.example.migator;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Odbierz mapę z fragmentu
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*--------------------HOOKS---------------------*/
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        /*--------------------TOOLBAR---------------------*/
        //setSupportActionBar(toolbar);

        /*--------------------NAVIGATION DRAWER MENU---------------------*/

        //hide or show items
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_home).setVisible(true); //przykład


        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_map);
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
            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_searchBusStop) {
            Intent intent = new Intent(MapsActivity.this, busStopSearch.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_searchLine) {
            Intent intent = new Intent(MapsActivity.this, lineSearch.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_map) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if(menuItem.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(MapsActivity.this, settings.class);
            startActivity(intent);
        }

        return true;
    }

    private void loadBusStopsFromJSON() {
        try {
            // Wczytaj plik JSON z katalogu assets
            InputStream is = getAssets().open("stops.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            // Sparsuj JSON do obiektu StopsResponse
            Gson gson = new Gson();
            StopsResponse stopsResponse = gson.fromJson(stringBuilder.toString(), StopsResponse.class);

            // Dodaj markery dla każdego przystanku
            for (Stop stop : stopsResponse.data) {
                // Używamy getterów, aby uzyskać dane
                LatLng stopLocation = new LatLng(stop.latitude, stop.longitude);
                mMap.addMarker(new MarkerOptions()
                        .position(stopLocation)
                        .title(stop.getName()) // Korzystamy z getName()
                        .snippet("Numer: " + stop.getNumber())); // Korzystamy z getNumber()
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Ładowanie przystanków z pliku JSON
        loadBusStopsFromJSON();

        // Przykładowe punkty (przystanki autobusowe w Szczecinie)
        LatLng stop1 = new LatLng(53.42894, 14.55302); // Plac Grunwaldzki
        LatLng stop2 = new LatLng(53.43078, 14.55147); // Brama Portowa

        // Dodaj markery
        mMap.addMarker(new MarkerOptions().position(stop1).title("Plac Grunwaldzki").snippet("Popularny przystanek w centrum."));
        mMap.addMarker(new MarkerOptions().position(stop2).title("Brama Portowa").snippet("Główne miejsce przesiadkowe."));

        // Ustaw kamerę na pierwszy przystanek
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stop1, 14f));

        // Obsługa kliknięcia markerów
        mMap.setOnMarkerClickListener(marker -> {
            String title = marker.getTitle();
            String snippet = marker.getSnippet();
            Toast.makeText(this, title + "\n" + snippet, Toast.LENGTH_SHORT).show();
            return false;
        });

        // Dostosowanie mapy
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
    }
}

