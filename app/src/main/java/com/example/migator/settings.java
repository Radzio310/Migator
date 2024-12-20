package com.example.migator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class settings extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

        navigationView.setCheckedItem(R.id.nav_settings);

        // Załaduj zapisane dane
        loadSavedPreferences();

        // Ładowanie preferencji
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        boolean initialDarkMode = sharedPreferences.getBoolean("darkMode", false); // Śledzenie pierwotnego stanu
        setAppTheme(initialDarkMode);

        Switch themeSwitch = findViewById(R.id.switchTheme);
        themeSwitch.setChecked(initialDarkMode);

        /*-----URUCHAMIANIE WIDEO-----*/
        AtomicInteger flaga = new AtomicInteger(1); // flaga do wybierania filmu do odpalenia

        VideoView videoView = findViewById(R.id.videoView2);
        TextView textView = findViewById(R.id.textView3);
        AtomicReference<Uri> videoUri = new AtomicReference<>(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ponizej_znajdziesz_ustawienia)); // ustawienie filmu

        videoView.setVideoURI(videoUri.get());
        videoView.start(); // uruchomienie filmu
        textView.setText("Poniżej znajdziesz ustawienia aplikacji");

        videoView.setOnCompletionListener(mp -> { // czekanie az sie zakonczy obecny film
            if (flaga.get() == 1) { // sprawdzenie czy zakonczyl sie pierwszy film
                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mozesz_tu_dostosowac_preferencje)); // ustawienie drugiego filmu
                videoView.setVideoURI(videoUri.get());
                videoView.start();
                textView.setText("Możesz tu dostosować swoje preferencje i wygląd aplikacji");
                flaga.getAndIncrement(); // zwiekszenie flagi
            } else if (flaga.get() == 2){
                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.aby_wrocic_nacisnij_powrot)); // ustawienie drugiego filmu
                videoView.setVideoURI(videoUri.get());
                videoView.start();
                textView.setText("Aby wrócić na stronę główną, naciśnij przycisk 'Powrót'");
                flaga.getAndIncrement();
            } else if (flaga.get() == 3){
                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stand_by_5));
                videoView.setVideoURI(videoUri.get());
                videoView.start();
                textView.setText("");
            }
        });
    }



    private void loadSavedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);

        // Załaduj stan Switch
        Switch switch2 = findViewById(R.id.switchTheme);
        switch2.setChecked(sharedPreferences.getBoolean("switch2", false)); // default: false

        // Załaduj tekst z EditText
        EditText editText2 = findViewById(R.id.editTextText2);
        EditText editText3 = findViewById(R.id.editTextText3);
        editText2.setText(sharedPreferences.getString("option3", ""));
        editText3.setText(sharedPreferences.getString("option4", ""));
    }


    public void saveChanges(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Pobierz aktualny stan przełącznika
        Switch themeSwitch = findViewById(R.id.switchTheme);
        boolean newDarkMode = themeSwitch.isChecked();
        boolean initialDarkMode = sharedPreferences.getBoolean("darkMode", false);

        // Zapisz stan przełączników
        editor.putBoolean("darkMode", newDarkMode);

        // Zapisz tekst z EditText
        EditText editText2 = findViewById(R.id.editTextText2);
        EditText editText3 = findViewById(R.id.editTextText3);
        editor.putString("option3", editText2.getText().toString());
        editor.putString("option4", editText3.getText().toString());

        // Zapisz zmiany
        editor.apply();

        // Jeśli stan trybu ciemnego się zmienił, zrestartuj aktywność
        if (initialDarkMode != newDarkMode) {
            setAppTheme(newDarkMode); // Użyj newDarkMode zamiast isChecked

            // Zapisz stan motywu
            editor = sharedPreferences.edit();
            editor.putBoolean("darkMode", newDarkMode); // Użyj newDarkMode zamiast isChecked
            editor.apply();
            restartActivity();
        } else {
            Toast.makeText(this, "Zmiany zostały zapisane", Toast.LENGTH_SHORT).show();
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

    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.nav_home) {
            Intent intent = new Intent(settings.this, MainActivity.class);
            startActivity(intent);

        } else if(menuItem.getItemId() == R.id.nav_searchBusStop) {
            Intent intent = new Intent(settings.this, busStopSearch.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_searchLine) {
            Intent intent = new Intent(settings.this, lineSearch.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_map) {
            Intent intent = new Intent(settings.this, MapsActivity.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_settings) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        return true;
    }



    public void GoTo_MainActivity(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void setAppTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

}