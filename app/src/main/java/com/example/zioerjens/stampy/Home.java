package com.example.zioerjens.stampy;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        checkLoggedIn();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    //Leitet zu der Login-Activity weiter, wenn der Benutzer nicht angemeldet ist.
    private void checkLoggedIn(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null){
            Log.e("LOGIN_REFUSED", "login failed");
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Button buttonLogout = findViewById(R.id.btnLogout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signOut();
            }
        });
        addScannerOnclick();
    }

    //Adds the OnClickListener to the ScannerButton
    public void addScannerOnclick(){
        final Activity activity = this;
        final Button btnScanner = findViewById(R.id.btnScan);
        btnScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.setOrientationLocked(true);
                integrator.setCaptureActivity(CaptureActivityPortrait.class);
                integrator.initiateScan();
            }
        });
    }

    //Meldet den Benutzer ab
    private void signOut(){
        //Der Google-Account wird abgemeldet
        Toast.makeText(this,R.string.logoutSuccessful, Toast.LENGTH_SHORT).show();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignIn.getClient(this, gso).signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Nachdem der Google-Account abgemeldet wurde wird auch noch der FirebaseUser abgemeldet
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        mAuth.signOut();
                        //Es wird zum Login weitergeleitet
                        Intent intent = new Intent(getApplicationContext(),Login.class);
                        startActivity(intent);
                        //Activity wird geschlossen
                        finish();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);

        if (result != null){
            //Happens if you cancel the scanning
            if (result.getContents() == null) {
                Toast.makeText(this, R.string.scanCanceled, Toast.LENGTH_SHORT).show();
            }
            //Happens if you scan a QR-Code
            else {
                //TODO check if QR-Code is valid
                addStamp();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addStamp(){

        //Saves a stamp and adds it to the Database
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("stamp");
        DatabaseReference ref2 = ref.push();
        ref2.setValue(new Stamp("jvkhgjhc"));

        Log.e("ADDED DATABASE", db.toString());
        Log.e("ADDED DATABASE", "");

        Toast.makeText(this, R.string.stampSuccessful, Toast.LENGTH_LONG).show();
    }
}
