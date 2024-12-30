package com.example.migator;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import android.util.Pair;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Callback;


public class findStopUtils {

    // load JSON
    public static List<Stop> loadStops(Context context) {
        String fileName = "stops.json";

        try {
            // Otwieranie pliku z pamięci wewnętrznej
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            // Wczytywanie zawartości pliku
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            Gson gson = new Gson();
            List<Stop> stops = gson.fromJson(jsonBuilder.toString(), new TypeToken<List<Stop>>() {}.getType());

            return stops;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); // Zwraca pustą listę w przypadku błędu
        }
    }

    public static List<Line> loadLines(Context context) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.lines);
            InputStreamReader reader = new InputStreamReader(inputStream);

            Gson gson = new Gson();
            LinesResponse response = gson.fromJson(reader, LinesResponse.class);

            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Pair<String, String> findStopInfo(Context context, String inputName) {
        List<Stop> stops = loadStops(context);
        if (stops == null) {
            return null;
        }

        // 1st search
        for (Stop stop : stops) {
            if (stop.getName().equalsIgnoreCase(inputName)) {
                return new Pair<>(stop.getName(), stop.getNumber());
            }
        }

        // 2nd search
        for (Stop stop : stops) {
            if (stop.getName().toLowerCase().contains(inputName.toLowerCase())) {
                return new Pair<>(stop.getName(), stop.getNumber());
            }
        }

        // not found
        return null;
    }

    public static String findLineInfo(Context context, String inputName) {
        List<Line> lines = loadLines(context);
        if (lines == null) {
            return null;
        }

        // 1st search
        for (Line line : lines) {
            if (line.getNumber().equalsIgnoreCase(inputName)) {
                return line.getNumber();
            }
        }

        // not found
        return null;
    }

    public static void getDepartures(String stopNumber, Context context, DepartureCallback callback) {
        ApiClient.getApiService().getDepartures(stopNumber).enqueue(new Callback<DeparturesResponse>() {
            @Override
            public void onResponse(Call<DeparturesResponse> call, Response<DeparturesResponse> response) {
                if (response.isSuccessful()) {
                    DeparturesResponse departuresResponse = response.body();
                    if (departuresResponse != null) {
                        List<DeparturesResponse.Departure> departures = departuresResponse.getDepartures();
                        callback.onDeparturesLoaded(departures);
                    } else {
                        callback.onError("Pusta odpowiedź z serwera");
                    }
                } else {
                    callback.onError("Błąd odpowiedzi: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<DeparturesResponse> call, Throwable t) {
                callback.onError("Błąd sieci: " + t.getMessage());
            }
        });
    }

    public interface DepartureCallback {
        void onDeparturesLoaded(List<DeparturesResponse.Departure> departures);
        void onError(String error);
    }
}
