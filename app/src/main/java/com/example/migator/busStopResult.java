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
import android.widget.LinearLayout;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class busStopResult extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    String name;
    String number;

    private List<String> SpellingQueue = new ArrayList<>();
    private int SpellingIndex = 0;
    private static final Map<Character, String> letterMapping = new HashMap<>();
    static {
        letterMapping.put('ą', "_a_");
        letterMapping.put('ć', "_c_");
        letterMapping.put('ę', "_e_");
        letterMapping.put('ł', "_l");
        letterMapping.put('ń', "_n_");
        letterMapping.put('ó', "_o_");
        letterMapping.put('ś', "_s_");
        letterMapping.put('ź', "_z_");
        letterMapping.put('ż', "_z__");
    }

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
        Pair<String, String> stopInfo = findStopUtils.findStopInfoSingle(this, getIntent().getStringExtra("BusStopName"));
        String stopNumber = getIntent().getStringExtra("stopNumber");

        if (stopInfo != null) {
            name = stopInfo.first;
            if (stopNumber != null)
            {
                number = stopNumber;
            }
            else
            {
                number = stopInfo.second;
            }

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
            Log.d("Info",departure.getDirection());
            resultLayout.addView(directionView);


            // Add the result layout to the container
            resultsContainer.addView(resultLayout);
        }

        // If no departures are found
        if (departures.isEmpty()) {
            AtomicInteger flaga = new AtomicInteger(1);
            VideoView videoView = findViewById(R.id.videoView6);
            TextView textView = findViewById(R.id.textView6);
            AtomicReference<Uri> videoUri = new AtomicReference<>(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.blad_podczas_wyszukiwania_przystanku));

            videoView.setVideoURI(videoUri.get());
            videoView.start();
            textView.setText("Wystąpił bład podczas wyszukiwania przystanku");

            videoView.setOnCompletionListener(mp -> {
                if (flaga.get() == 1){
                    videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.aby_wrocic_nacisnij_powrot));
                    videoView.setVideoURI(videoUri.get());
                    videoView.start();
                    textView.setText("Aby wrócić naciśnij powrót");
                    flaga.getAndIncrement();
                } else if (flaga.get() == 2){
                    videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stand_by_3));
                    videoView.setVideoURI(videoUri.get());
                    videoView.start();
                    textView.setText("");
                }

            });

            TextView emptyView = new TextView(this);
            emptyView.setText("Brak odjazdów w najbliższym czasie");
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setTypeface(ResourcesCompat.getFont(this, R.font.baloo), Typeface.ITALIC); // Czcionka Baloo i pochyłość
            emptyView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark)); // Kolor czerwony
            emptyView.setTextSize(16); // Opcjonalnie dostosuj rozmiar tekstu
            resultsContainer.addView(emptyView);
        } else {

            DeparturesResponse.Departure departure = departures.get(0);
            String direction = departure.getDirection();
            String time = departure.getTime();
            Log.d("Info", direction);

            try {
                Field fieldNumber = R.raw.class.getDeclaredField("_" + departure.getLineNumber());
                int videoNumber = fieldNumber.getInt(null);

                String directionNameWithPolishChars = direction;
                String directionName = removeDiacritics(directionNameWithPolishChars.toLowerCase());

                int videoDirection = 0;
                try {
                    Field fieldDirection = R.raw.class.getDeclaredField(directionName);
                    videoDirection = fieldDirection.getInt(null);
                } catch (Exception e) {

                }


                Field fieldTime = R.raw.class.getDeclaredField("_" + time);
                int videoTime = fieldTime.getInt(null);

                AtomicInteger flaga = new AtomicInteger(1);

                VideoView videoView = findViewById(R.id.videoView6);
                TextView textView = findViewById(R.id.textView6);
                AtomicReference<Uri> videoUri = new AtomicReference<>(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.najblizszy_odjazd_linii)); // ustawienie filmu

                videoView.setVideoURI(videoUri.get());
                videoView.start();
                textView.setText("Najbliższy odjazd linii");


                prepareLetterVideos(directionNameWithPolishChars); // przygotuj nagrania do literowania

                int finalVideoDirection = videoDirection;
                videoView.setOnCompletionListener(mp -> {
                    if (flaga.get() == 1) {
                        videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + videoNumber));
                        videoView.setVideoURI(videoUri.get());
                        videoView.start();
                        textView.setText(departure.getLineNumber());
                        flaga.getAndIncrement();
                    } else if (flaga.get() == 2) {
                        videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.jest_za));
                        videoView.setVideoURI(videoUri.get());
                        videoView.start();
                        textView.setText("jest za");
                        flaga.getAndIncrement();
                    } else if (flaga.get() == 3) {
                        videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + videoTime));
                        videoView.setVideoURI(videoUri.get());
                        videoView.start();
                        textView.setText(time);
                        flaga.getAndIncrement();
                    } else if (flaga.get() == 4) {
                        videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.w_kierunku));
                        videoView.setVideoURI(videoUri.get());
                        videoView.start();
                        textView.setText("minut w kierunku");
                        flaga.getAndIncrement();
                    } else if (flaga.get() == 5) {
                        textView.setText(directionNameWithPolishChars);

                        if (finalVideoDirection != 0) {
                            videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + finalVideoDirection));
                            videoView.setVideoURI(videoUri.get());
                            videoView.start();
                            flaga.getAndIncrement();
                        } else {
                            if (!playNextVideo(videoView)) {
                                videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.aby_wrocic_nacisnij_powrot));
                                videoView.setVideoURI(videoUri.get());
                                videoView.start();
                                textView.setText("Aby wrócić na stronę główną, naciśnij 'Powrót'");
                                flaga.getAndIncrement();
                            }
                        }

                    } else if (flaga.get() == 6) {
                        videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stand_by_3));
                        videoView.setVideoURI(videoUri.get());
                        videoView.start();
                        textView.setText("");
                    }
                });


            } catch (Exception e) {
                TextView textView = findViewById(R.id.textView6);
                textView.setText("Wystąpił problem podczas tworzenia animacji");
                VideoView videoView = findViewById(R.id.videoView6);
                AtomicReference<Uri> videoUri = new AtomicReference<>(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.blad_podczas_wyszukiwania_przystanku));
                videoView.setVideoURI(videoUri.get());
                videoView.start();
                Log.d("Bład", "Wystąpił błąd podczas składania animacji");
                Log.d("Error", e.toString());
                AtomicInteger flaga = new AtomicInteger(1);

                videoView.setOnCompletionListener(mp -> {
                    if (flaga.get() == 1) {
                        videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.aby_wrocic_nacisnij_powrot));
                        videoView.setVideoURI(videoUri.get());
                        videoView.start();
                        textView.setText("Aby wrócić na stronę głowną, naciśnij 'Powrót'");
                        flaga.getAndIncrement();
                    } else if (flaga.get() == 2){
                        videoUri.set(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stand_by_4));
                        videoView.setVideoURI(videoUri.get());
                        videoView.start();
                        textView.setText("");
                    }

                });
            }





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

    public void go_back(View v) {
        super.onBackPressed();
    }

    /* FUNKCJE POMOCNICZE DO GENEROWANIA ANIMACJI*/

    private String removeDiacritics(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case 'ą': result.append('a'); break;
                case 'ć': result.append('c'); break;
                case 'ę': result.append('e'); break;
                case 'ł': result.append('l'); break;
                case 'ń': result.append('n'); break;
                case 'ó': result.append('o'); break;
                case 'ś': result.append('s'); break;
                case 'ź': result.append('z'); break;
                case 'ż': result.append('z'); break;
                case ' ': result.append(""); break;
                case '(': result.append(""); break;
                case ')': result.append(""); break;
                default: result.append(c); break;
            }
        }
        return result.toString();
    }


    private void prepareLetterVideos(String stopName) {
        SpellingQueue.clear(); // Wyczyść kolejkę na wszelki wypadek
        SpellingIndex = 0;
        for (char letter : stopName.toLowerCase().toCharArray()) {
            if (Character.isLetter(letter)) {
                String fileName;
                if (letterMapping.containsKey(letter)) {
                    fileName = letterMapping.get(letter);
                } else {
                    fileName = "_" + letter; // np. "_b"
                }
                // Pobierz identyfikator zasobu z folderu raw
                int videoResId = getResources().getIdentifier(fileName, "raw", getPackageName());
                if (videoResId != 0) {
                    String videoUri = "android.resource://" + getPackageName() + "/" + videoResId;
                    SpellingQueue.add(videoUri);
                } else {
                    Log.d("Resource Error", "Nie znaleziono zasobu dla: " + fileName);
                }
            }
        }
    }


    private boolean playNextVideo(VideoView videoView) {
        if (SpellingIndex <= SpellingQueue.size() - 1) {
            String videoUri = SpellingQueue.get(SpellingIndex);
            videoView.setVideoURI(Uri.parse(videoUri));
            videoView.start();
            SpellingIndex++;
            return true;
        } else {
            return false;
        }
    }

}