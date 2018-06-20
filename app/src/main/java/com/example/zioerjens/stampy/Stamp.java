package com.example.zioerjens.stampy;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Stamp {

    public String userId;
    //public String dateTime;

    public Stamp(){}

    public Stamp(String userId) {
        this.userId = userId;
        //this.dateTime = new SimpleDateFormat("dd-MM-yyyy  HH:mm:ss").format(new Date());
    }
}
