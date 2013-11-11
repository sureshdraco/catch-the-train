package com.draco.catchthetrain;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suresh on 04/11/13.
 */
public class Response {
    @SerializedName("HafasResponse")
    public HafasResponse hafasResponse;

    @SerializedName("SubTrip")
    public SubTrip subTrip;

    class HafasResponse {
        @SerializedName("Trip")
        List<Trip> tripList;
    }

    class Trip {
        @SerializedName("Summary")
        public Summary summary;
    }

    class Summary {
        @SerializedName("DepartureDate")
        public String departureDate;

        @SerializedName("Destination")
        public Place destination;

        @SerializedName("DepartureTime")
        public Time departureTime;
    }

    class Time {
        @SerializedName("#text")
        public String time;
    }

    public String getTime() {
        return hafasResponse.tripList.get(1).summary.departureTime.time;
    }

    public List<TrainTime> getTrainTimes() {
        List<TrainTime> trainTimeList = new ArrayList<TrainTime>();
        for (Trip trip : hafasResponse.tripList) {
            trainTimeList.add(new TrainTime(trip.summary.destination.place, trip.summary.departureTime.time));
        }
        return trainTimeList;
    }

    private class Place {
        @SerializedName("#text")
        public String place;
    }
}
