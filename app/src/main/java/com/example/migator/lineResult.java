package com.example.migator;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class lineResult extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    String name;
    String number;
    String line_number;
    private List<String> SpellingQueue = new ArrayList<>();
    private int SpellingIndex = 0;
    private static final Map<Character, String> letterMapping = new HashMap<>();
    static {
        letterMapping.put('ą', "_a_");
        letterMapping.put('ć', "_c_");
        letterMapping.put('ę', "_e_");
        letterMapping.put('ł', "_l_");
        letterMapping.put('ń', "_n_");
        letterMapping.put('ó', "_o_");
        letterMapping.put('ś', "_s_");
        letterMapping.put('ź', "_z_");
        letterMapping.put('ż', "_z_");
    }

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

        /*Składanie animacji*/
        DeparturesResponse.Departure departure = departures.get(0);
        String direction = (String) ((TextView) findViewById(R.id.direction1)).getText();
        String time = departure.getTime();

        try {
            //Field fieldName = R.raw.class.getDeclaredField(name);
            //int videoName = fieldName.getInt(null);

            Field fieldNumber = R.raw.class.getDeclaredField("_" + lineNumber);
            int videoNumber = fieldNumber.getInt(null);
            Log.d("Sukces","Pobrano numer");

            //Field fieldDirection = R.raw.class.getDeclaredField(direction);
            //int videoDirection = fieldDirection.getInt(null);

            Field fieldTime = R.raw.class.getDeclaredField("_" + time);
            int videoTime = fieldTime.getInt(null);
            Log.d("Sukces","Pobrano czas");

            AtomicInteger flaga = new AtomicInteger(1); // flaga do wybierania filmu do odpalenia

            VideoView videoView = findViewById(R.id.videoView5);
            TextView textView = findViewById(R.id.textView6);
            AtomicReference<Uri> videoUri = new AtomicReference<>(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.najblizszy_odjazd_linii)); // ustawienie filmu

            videoView.setVideoURI(videoUri.get());
            videoView.start();
            textView.setText("Najbliższy odjazd linii");

            videoView.setOnCompletionListener(mp -> {
                if (flaga.get() == 1) {
                    videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + videoNumber)); // ustawienie drugiego filmu
                    videoView.setVideoURI(videoUri.get());
                    videoView.start();
                    textView.setText(lineNumber);
                    flaga.getAndIncrement();
                } else if (flaga.get() == 2){
                    videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.jest_za)); // ustawienie drugiego filmu
                    videoView.setVideoURI(videoUri.get());
                    videoView.start();
                    textView.setText("jest za");
                    flaga.getAndIncrement();
                } else if (flaga.get() == 3){
                    videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + videoTime));
                    videoView.setVideoURI(videoUri.get());
                    videoView.start();
                    textView.setText(time);
                    flaga.getAndIncrement();
                } else if (flaga.get() == 4){
                    videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.w_kierunku));
                    videoView.setVideoURI(videoUri.get());
                    videoView.start();
                    textView.setText("minut w kierunku");
                    flaga.getAndIncrement();
                } else if (flaga.get() == 4){
                    //videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + videoDirection));
                    //videoView.setVideoURI(videoUri.get());
                    //videoView.start();
                    textView.setText(direction);
                    flaga.getAndIncrement();
                } else if (flaga.get() == 5) {
                    videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.aby_wrocic_nacisnij_powrot));
                    videoView.setVideoURI(videoUri.get());
                    videoView.start();
                    textView.setText("Aby wrócić na stronę główną, naciśnij 'Powrót'");
                    flaga.getAndIncrement();
                } else if (flaga.get() == 6) {
                    videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stand_by_3));
                    videoView.setVideoURI(videoUri.get());
                    videoView.start();
                    textView.setText("");
                }
            });



        } catch (Exception e){
            Log.d("Bład","Wystąpił błąd podczas składania animacji");
            Log.d("Error",e.toString());
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

