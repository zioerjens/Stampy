package com.example.zioerjens.stampy;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Vaucher {

    public String dateTime;
    public String code;
    public String userId;

    public Vaucher() {
        this.dateTime = new SimpleDateFormat("dd-MM-yyyy  HH:mm:ss").format(new Date());
        this.code = Functions.generateNewCode(25);
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}