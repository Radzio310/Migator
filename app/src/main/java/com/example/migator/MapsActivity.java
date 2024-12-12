package com.example.migator;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private List<Marker> markersList = new ArrayList<>();
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
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        /*--------------------HOOKS---------------------*/
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        /*--------------------NAVIGATION DRAWER MENU---------------------*/
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_map);

        AutoCompleteTextView searchView = findViewById(R.id.bus_stop_search);
        List<String> stopNames = busStopSearch.JsonUtils.loadStopNamesFromJson(this);
        Set<String> uniqueStopNames = new HashSet<>(stopNames);
        ArrayAdapter<String> stopAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, new ArrayList<>(uniqueStopNames));
        searchView.setAdapter(stopAdapter);

        searchView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedStop = parent.getItemAtPosition(position).toString();

            for (Marker marker : markersList) {
                if (marker.getTitle().equals(selectedStop)) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14f));
                    marker.showInfoWindow();

                    // Symulacja zdarzenia kliknięcia
                    mMap.setOnMarkerClickListener(m -> {
                        if (m.equals(marker)) {
                            // Obsługa wybranego markera
                            return true;
                        }
                        return false;
                    });

                    break;
                }
            }
        });
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
            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.nav_searchBusStop) {
            Intent intent = new Intent(MapsActivity.this, busStopSearch.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.nav_searchLine) {
            Intent intent = new Intent(MapsActivity.this, lineSearch.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.nav_map) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (menuItem.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(MapsActivity.this, settings.class);
            startActivity(intent);
        }

        return true;
    }

    private void loadBusStopsFromJSON() {
        try {
            // Wczytaj plik JSON z katalogu raw
            InputStream is = getResources().openRawResource(R.raw.stops);
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
                LatLng stopLocation = new LatLng(stop.latitude, stop.longitude);
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(stopLocation)
                        .title(stop.getName())
                        .snippet("Numer: " + stop.getNumber()));
                markersList.add(marker);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enableUserLocation() {
        if (mMap != null && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void requestLocationPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {
                Toast.makeText(this, "Brak uprawnień do lokalizacji. Nie można włączyć funkcji lokalizacji.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestLocationPermission();
        }

        loadBusStopsFromJSON();

        mMap.setOnMarkerClickListener(marker -> {
            String stopName = marker.getTitle();
            LatLng stopPosition = marker.getPosition();

            TextView stopNameView = findViewById(R.id.stop_name);
            stopNameView.setText(stopName);

            findViewById(R.id.bottom_panel).setVisibility(View.VISIBLE);

            findViewById(R.id.view_schedule_button).setOnClickListener(v -> {
                Intent intent = new Intent(MapsActivity.this, busStopResult.class);
                intent.putExtra("BusStopName", stopName);
                startActivity(intent);
            });

            findViewById(R.id.navigate_button).setOnClickListener(v -> {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + stopPosition.latitude + "," + stopPosition.longitude + "&mode=w");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            });

            return true;
        });

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
    }
}
