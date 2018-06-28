package com.example.zioerjens.stampy;

public class MarkerData {

    public String longitude;
    public String latitude;
    public String name;
    public String content;

    public MarkerData(){}

    public MarkerData(String longitude, String latitude, String name, String content) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.name = name;
        this.content = content;
    }
}
