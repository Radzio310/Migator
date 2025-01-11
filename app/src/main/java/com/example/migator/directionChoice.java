package com.example.migator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class directionChoice extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_direction_choice);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        List<Pair<String, String>> stopInfo = findStopUtils.findStopInfo(this, getIntent().getStringExtra("BusStopName"));

        if (stopInfo != null && !stopInfo.isEmpty()) {
            // Wyświetl nazwę przystanku (zakładamy, że wszystkie przystanki mają tę samą nazwę)
            String stopName = stopInfo.get(0).first; // Pobieramy nazwę z pierwszego przystanku
            ((TextView) findViewById(R.id.currentBusStopName)).setText(stopName); // Ustawiamy nazwę przystanku w widoku

            LinearLayout resultsContainer = findViewById(R.id.resultsContainer);
            resultsContainer.removeAllViews(); // Usuń istniejące widoki, aby uniknąć duplikacji

            for (Pair<String, String> stop : stopInfo) {
                String number = stop.second;

                if (number != null) {
                    Log.d("Stop Number", "Numer przystanku: " + number);

                    // API callback
                    findStopUtils.getDepartures(number, this, new findStopUtils.DepartureCallback() {
                        @Override
                        public void onDeparturesLoaded(List<DeparturesResponse.Departure> departures) {
                            runOnUiThread(() -> {
                                // Zbierz unikalne kierunki dla danego przystanku
                                Set<String> uniqueDirections = new LinkedHashSet<>();
                                for (DeparturesResponse.Departure departure : departures) {
                                    String direction = departure.getDirection();
                                    if (direction != null && !direction.isEmpty()) {
                                        uniqueDirections.add(direction);
                                    }
                                }

                                // Utwórz przycisk dla unikalnych kierunków danego przystanku
                                Button stopButton = new Button(directionChoice.this);
                                stopButton.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT));
                                stopButton.setText("Kierunek: " + String.join(", ", uniqueDirections));
                                stopButton.setPadding(16, 16, 16, 16);

                                // Obsługa kliknięcia przycisku - przejście do nowej aktywności z numerem przystanku
                                stopButton.setOnClickListener(v -> GoTo_busStopResult(v, stopName, number));

                                // Dodaj przycisk do kontenera
                                resultsContainer.addView(stopButton);
                            });
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("Departure Error", error);
                            runOnUiThread(() -> Toast.makeText(directionChoice.this, "Błąd: " + error, Toast.LENGTH_LONG).show());
                        }
                    });
                } else {
                    Log.d("Stop Number", "Nie znaleziono przystanku.");
                    Toast.makeText(this, "Nie znaleziono przystanku.", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Log.d("Stop Info", "Nie znaleziono przystanku.");
        }
    }

    public void GoTo_busStopResult(View v, String stopName, String stopNumber) {
        Intent intent = new Intent(this, busStopResult.class);

        intent.putExtra("BusStopName", stopName);
        intent.putExtra("stopNumber", stopNumber);
        startActivity(intent);

    }
}
