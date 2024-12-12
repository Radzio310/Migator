package com.example.migator;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DeparturesResponse {
    @SerializedName("stop_name")
    private String stopName;

    @SerializedName("stop_number")
    private String stopNumber;

    @SerializedName("departures")
    private List<Departure> departures;

    public String getStopName() {
        return stopName;
    }

    public String getStopNumber() {
        return stopNumber;
    }

    public List<Departure> getDepartures() {
        return departures;
    }

    public static class Departure {
        @SerializedName("line_number")
        private String lineNumber;

        @SerializedName("direction")
        private String direction;

        @SerializedName("time_real")
        private Integer timeReal;

        @SerializedName("time_scheduled")
        private String timeScheduled;

        public String getLineNumber() {
            return lineNumber;
        }

        public String getDirection() {
            return direction;
        }

        public Integer getTimeReal() {
            return timeReal;
        }

        public String getTimeScheduled() {
            return timeScheduled;
        }

        public String getTime() {
            return timeReal != null ? timeReal.toString() : timeScheduled;
        }
    }
}
