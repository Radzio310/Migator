package com.example.migator;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class ApiClient {

    private static final String BASE_URL = "https://www.zditm.szczecin.pl/api/v1/";

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static ApiService getApiService() {
        return retrofit.create(ApiService.class);
    }

    public interface ApiService {

        @GET("stops")
        Call<StopsResponse> getStops();

        @GET("lines")
        Call<LinesResponse> getLines();

        @GET("displays/{stopNumber}")
        Call<DeparturesResponse> getDepartures(@Path("stopNumber") String stopNumber);
    }
}
