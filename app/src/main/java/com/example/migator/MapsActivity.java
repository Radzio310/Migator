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
import android.widget.VideoView;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private List<Marker> markersList = new ArrayList<>();
    private Marker activeMarker = null;
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

            // Zamknij klawiaturę
            closeKeyboard();

            for (Marker marker : markersList) {
                if (marker.getTitle().equals(selectedStop)) {
                    if (activeMarker != null) {
                        // Resetuj wygląd poprzedniego aktywnego markera
                        resetMarkerAppearance(activeMarker);
                    }

                    // Ustaw wybrany marker jako aktywny
                    activeMarker = marker;
                    highlightMarker(marker);

                    // Wyświetl szczegóły przystanku
                    displayStopDetails(marker);
                    break;
                }
            }
        });

        /*-----URUCHAMIANIE WIDEO-----*/
        AtomicInteger flaga = new AtomicInteger(1);

        VideoView videoView = findViewById(R.id.avatarMaps);
        AtomicReference<Uri> videoUri = new AtomicReference<>(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ponizej_widzisz_mape));

        videoView.setVideoURI(videoUri.get());
        videoView.start();

        videoView.setOnCompletionListener(mp -> {
            if (flaga.get() == 1) {
                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.zaznacz_przystanek_aby_zobaczyc_rozklad));
                videoView.setVideoURI(videoUri.get());
                videoView.start();
                flaga.getAndIncrement();
            } else if (flaga.get() == 2){
                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stand_by_5));
                videoView.setVideoURI(videoUri.get());
                videoView.start();
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
            // Użycie metody pomocniczej loadStops do wczytania przystanków z pamięci wewnętrznej
            List<Stop> stops = findStopUtils.loadStops(this);

            // Dodaj markery dla każdego przystanku
            for (Stop stop : stops) {
                LatLng stopLocation = new LatLng(stop.latitude, stop.longitude);
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(stopLocation)
                        .title(stop.getName())
                        .snippet("Numer: " + stop.getNumber()));
                markersList.add(marker);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Błąd podczas ładowania przystanków z JSON");
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
            centerMapBasedOnLocation();
        } else {
            requestLocationPermission();
        }

        loadBusStopsFromJSON();

        // Obsługa kliknięcia na marker
        mMap.setOnMarkerClickListener(marker -> {
            if (activeMarker != null) {
                // Resetuj poprzedni aktywny marker
                resetMarkerAppearance(activeMarker);
            }

            // Ustaw nowy aktywny marker
            activeMarker = marker;
            highlightMarker(marker); // Zmień wygląd markera
            displayStopDetails(marker);
            VideoView videoView = findViewById(R.id.avatarMaps);
            AtomicReference<Uri> videoUri = new AtomicReference<>(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.nacisnij_nawiguj));

            videoView.setVideoURI(videoUri.get());
            videoView.start();


            return true;
        });

        // Obsługa kliknięcia w puste miejsce
        mMap.setOnMapClickListener(latLng -> {
            if (activeMarker != null) {
                // Resetuj aktywny marker
                resetMarkerAppearance(activeMarker);
                activeMarker = null;
            }

            // Ukryj panel dolny
            findViewById(R.id.bottom_panel).setVisibility(View.GONE);
        });

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
    }

    // Zmiana wyglądu aktywnego markera
    private void highlightMarker(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)); // Kolor: niebieski
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15f));
    }

    // Resetowanie wyglądu markera do domyślnego
    private void resetMarkerAppearance(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)); // Kolor: czerwony
    }


    private void centerMapBasedOnLocation() {
        // Szczecin center coordinates
        LatLng szczecinCenter = new LatLng(53.4285, 14.5528);
        float radiusKm = 20.0f;

        // Check user location once
        if (mMap.isMyLocationEnabled() && mMap.getMyLocation() != null) {
            android.location.Location location = mMap.getMyLocation();
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            float[] distance = new float[1];
            android.location.Location.distanceBetween(userLocation.latitude, userLocation.longitude,
                    szczecinCenter.latitude, szczecinCenter.longitude, distance);

            if (distance[0] / 1000 > radiusKm) {
                // Outside Szczecin - center on the first marker without showing details
                if (!markersList.isEmpty()) {
                    Marker firstMarker = markersList.get(0);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstMarker.getPosition(), 14f));
                }
            } else {
                // Inside Szczecin - center on user location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14f));
            }
        } else {
            // Location unavailable, center on Szczecin
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(szczecinCenter, 14f));
        }
    }


    private void displayStopDetails(Marker marker) {
        String stopName = marker.getTitle();
        LatLng stopPosition = marker.getPosition();
        String number = marker.getSnippet();
        String stopNumber = number.replace("Numer: ", "");

        TextView stopNameView = findViewById(R.id.stop_name);
        stopNameView.setText(stopName);

        findViewById(R.id.bottom_panel).setVisibility(View.VISIBLE);

        findViewById(R.id.view_schedule_button).setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, busStopResult.class);
            intent.putExtra("BusStopName", stopName);
            intent.putExtra("stopNumber", stopNumber);
            startActivity(intent);
        });

        findViewById(R.id.navigate_button).setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + stopPosition.latitude + "," + stopPosition.longitude + "&mode=w");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        // Animacja przesunięcia i przybliżenia mapy na marker
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16f));
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


}
