package com.example.migator;

public class Stop {
    private int id;
    private String number;
    private String name;
    public double latitude;
    public double longitude;
    private boolean request_stop;
    private boolean park_and_ride;
    private String railway_station_name;
    private String updated_at;

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

}
