package com.example.migator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class busStopSearch extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop_search);

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
            Intent intent = new Intent(busStopSearch.this, MainActivity.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_searchBusStop) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if(menuItem.getItemId() == R.id.nav_searchLine) {
            Intent intent = new Intent(busStopSearch.this, lineSearch.class);
            startActivity(intent);
        } else if(menuItem.getItemId() == R.id.nav_map) {
            //Intent intent = new Intent(busStopSearch.this, busStopMap.class);
            //startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START); //na razie póki nie ma innych ekranów
        } else if(menuItem.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(busStopSearch.this, settings.class);
            startActivity(intent);
        }

        return true;
    }

    public void GoTo_busStopResult(View v){

        Intent intent = new Intent(this, busStopResult.class);
        String busStopName = ((EditText) findViewById(R.id.busStopName)).getText().toString();
        Pair<String, String> stopInfo = findStopUtils.findStopInfo(this, busStopName);

        if (busStopName.isEmpty())
        {
            Toast.makeText(this, "Nie podano nazwy przystanku.", Toast.LENGTH_SHORT).show();
        }
        else if (stopInfo == null)
        {
            Toast.makeText(this, "Nie znaleziono takiego przystanku.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            intent.putExtra("BusStopName", busStopName);
            startActivity(intent);
        }

    }

    public void GoTo_Naviagtion(View v) {
        String busStopName = ((EditText) findViewById(R.id.busStopName)).getText().toString();
        busStopName = busStopName.trim();
        Pair<String, String> stopInfo = findStopUtils.findStopInfo(this, busStopName);
        if (busStopName.isEmpty())
        {
            Toast.makeText(this, "Nie podano nazwy przystanku.", Toast.LENGTH_SHORT).show();
        }
        else if (stopInfo == null)
        {
            Toast.makeText(this, "Nie znaleziono takiego przystanku.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String name = stopInfo.first;
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode("Szczecin, przystanek autobusowy " + name) + "&mode=walking");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
    }

    public void GoTo_MainActivity(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}