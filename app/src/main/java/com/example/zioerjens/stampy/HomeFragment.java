package com.example.zioerjens.stampy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class HomeFragment extends Fragment {

    private View view;
    private Activity context;
    private Boolean valid = false;
    private Fragment fragment;

    public HomeFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_home, container, false);
        context = (Activity) getContext();
        fragment = this;
        return rootView;
    }

    //Leitet zu der Login-Activity weiter, wenn der Benutzer nicht angemeldet ist.
    private void checkLoggedIn(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null){
            Log.e("LOGIN_REFUSED", "login failed");
            Intent intent = new Intent(context,Login.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        checkLoggedIn();
        this.view = getView();

        final Button buttonLogout = view.findViewById(R.id.btnLogout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signOut();
            }
        });
        addScannerOnclick();
        addGenerateVaucherListener();
        Button btnViewVoucher = (Button) view.findViewById(R.id.btnViewVaucher);
        btnViewVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,VoucherView.class);
                startActivity(intent);
            }
        });
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            countStamps();
        }
    }

    //Meldet den Benutzer ab
    private void signOut(){
        //Der Google-Account wird abgemeldet
        Toast.makeText(context,R.string.logoutSuccessful, Toast.LENGTH_SHORT).show();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignIn.getClient(context, gso).signOut()
                .addOnCompleteListener(context, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Nachdem der Google-Account abgemeldet wurde wird auch noch der FirebaseUser abgemeldet
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        mAuth.signOut();
                        //Es wird zum Login weitergeleitet
                        Intent intent = new Intent(context,Login.class);
                        startActivity(intent);
                        //Activity wird geschlossen
                        context.finish();
                    }
                });
    }

    private void addStamp(String code){

        int number = Character.getNumericValue(code.charAt(25));

        Log.e("NUMBER",number+"");

        for (int i = 0; i <= number; i++) {
            //Saves a stamp and adds it to the Database
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference ref = db.getReference("stamp");
            DatabaseReference ref2 = ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push();
            ref2.setValue(new Stamp());
        }
    }

    //Adds the OnClickListener to the ScannerButton
    public void addScannerOnclick(){
        final Activity activity = context;
        final Button btnScanner = view.findViewById(R.id.btnScan);
        btnScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentIntentIntegrator integrator = new FragmentIntentIntegrator(fragment);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);

        if (result != null){
            //Happens if you cancel the scanning
            if (result.getContents() == null) {
                Toast.makeText(context, R.string.scanCanceled, Toast.LENGTH_SHORT).show();
            }
            //Happens if you scan a QR-Code
            else {
                deleteCodeIfValid(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void deleteCodeIfValid(String code) {

        final String scannedCode = code;

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("freeStamp");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                valid = false;

                for(DataSnapshot grSnapshot : dataSnapshot.getChildren()) {
                    // Get Post object and use the values to update the UI
                    FreeStamp freeStamp = grSnapshot.getValue(FreeStamp.class);

                    if (freeStamp.code.equals(scannedCode)){

                        //Checking if the Stamp is older than 10 Minutes
                        if (Functions.isYoungerThanSec(freeStamp.dateTime,600)) {

                            String key = grSnapshot.getKey();
                            deleteFreeStamp(key, scannedCode);
                            valid = true;
                            break;
                        }
                    }
                }
                if (!valid){
                    showValidPopUp(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void deleteFreeStamp(String key, String code){

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("freeStamp");
        ref.child(key).removeValue();

        addStamp(code);
        showValidPopUp(true);
        countStamps();
    }

    public void addGenerateVaucherListener(){

        TextView btnGenerateVaucher = (TextView) view.findViewById(R.id.btnGenerateVoucher);
        btnGenerateVaucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference ref = db.getReference("stamp");

                ref.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for(DataSnapshot grSnapshot : dataSnapshot.getChildren()) {
                            if (grSnapshot.getKey().equals(userId)) {
                                if (grSnapshot.getChildrenCount() >= 10) {
                                    deleteStamps();
                                } else {
                                    Toast.makeText(context, "You currently don't have enough stamps", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(context, "You currently don't have enough stamps", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        });
    }

    public void deleteStamps(){
        //Delete 10 stamps of the current user
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("stamp/"+userId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int counter = 0;

                for(DataSnapshot grSnapshot : dataSnapshot.getChildren()) {

                    if (counter < 10) {
                        String key = grSnapshot.getKey();
                        DatabaseReference ref = db.getReference("stamp/" + userId + "/" + key);
                        ref.removeValue();
                        counter++;
                    }
                }
                countStamps();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        addNewVaucher();
    }

    public void addNewVaucher(){

        //Saves a stamp and adds it to the Database
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("voucher");
        DatabaseReference ref2 = ref.push();
        ref2.setValue(new Vaucher());

        Toast.makeText(context,R.string.voucherSuccessful,Toast.LENGTH_LONG).show();
    }

    public void showValidPopUp(Boolean valid){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = getLayoutInflater().inflate(R.layout.show_success,null);

        if (valid) {
            mView.findViewById(R.id.success).setBackgroundResource(R.drawable.success);
            TextView title = (TextView) mView.findViewById(R.id.successTitle);
            title.setText(R.string.stampSuccessful);
        } else {
            mView.findViewById(R.id.success).setBackgroundResource(R.drawable.failure);
            TextView title = (TextView) mView.findViewById(R.id.successTitle);
            title.setText(R.string.stampUnsuccessful);
        }

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

        new CountDownTimer(5000, 1000) { // 5000 = 5 sec

            public void onTick(long millisUntilFinished) {}

            public void onFinish() {
                dialog.dismiss();
            }
        }.start();
    }

    public void countStamps(){
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("stamp");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int stamps = 0;

                for(DataSnapshot grSnapshot : dataSnapshot.getChildren()) {
                    if (grSnapshot.getKey().equals(userId)) {
                        stamps = (int) grSnapshot.getChildrenCount();
                    }
                }

                updateHome(stamps);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void updateHome(int stamps){
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.circular_progress_bar);
        progressBar.setProgress(stamps);
        TextView textView = (TextView) view.findViewById(R.id.stampText);
        if (stamps == 1) {
            textView.setText(stamps + " " + getResources().getText(R.string.stamp));
        } else {
            textView.setText(stamps + " " + getResources().getText(R.string.stamps));
        }

        TextView btnGenerateVaucher = (TextView) view.findViewById(R.id.btnGenerateVoucher);

        if (stamps >= 10){
            btnGenerateVaucher.setVisibility(View.VISIBLE);
            textView.setPadding(0,(int)Functions.convertDpToPixel(20f,context),0,0);
            textView.setHeight((int)Functions.convertDpToPixel(70f,context));
        } else {
            btnGenerateVaucher.setVisibility(View.GONE);
            textView.setPadding(0,(int)Functions.convertDpToPixel(65f,context),0,0);
            textView.setHeight((int)Functions.convertDpToPixel(100f,context));
        }
    }
}
