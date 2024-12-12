package com.example.migator;

import android.content.Intent;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.w3c.dom.Text;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}