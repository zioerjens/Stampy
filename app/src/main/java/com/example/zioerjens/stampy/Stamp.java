package com.example.zioerjens.stampy;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Stamp {

    public String dateTime;

    public Stamp() {
        this.dateTime = new SimpleDateFormat("dd-MM-yyyy  HH:mm:ss").format(new Date());
    }
}
