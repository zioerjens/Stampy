package com.example.zioerjens.stampy;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VoucherViewFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private VoucherViewFragment voucherView;
    private ProgressBar pb;
    private int clickCounter;
    private Activity context;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_voucher_view, container, false);
        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();

        context = (Activity) getContext();
        view = getView();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);

        this.voucherView = this;

        this.pb = view.findViewById(R.id.indeterminateBar);
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            getVouchersFromUser();
        }
    }

    public void getVouchersFromUser(){

        pb.setVisibility(ProgressBar.VISIBLE);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("voucher");
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final List<Vaucher> vauchers = new ArrayList<Vaucher>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot grSnapshot : dataSnapshot.getChildren()) {
                    // Get Post object and use the values to update the UI
                    Vaucher vaucher = grSnapshot.getValue(Vaucher.class);

                    if (vaucher.userId.equals(userId)){

                        vauchers.add(vaucher);
                    }
                }

                // specify an adapter (see also next example)
                mAdapter = new MyAdapter(vauchers,voucherView);
                mRecyclerView.setAdapter(mAdapter);
                pb.setVisibility(ProgressBar.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void createPopUp(String code){

        if (clickCounter == 1) {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
            View mView = context.getLayoutInflater().inflate(R.layout.show_code, null);
            final ImageView codeImg = (ImageView) mView.findViewById(R.id.qrCode);

            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(code, BarcodeFormat.QR_CODE, 1000, 1000);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                codeImg.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
            mBuilder.setView(mView);
            final AlertDialog dialog = mBuilder.create();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    getVouchersFromUser();
                    clickCounter = 0;
                }
            });
            dialog.show();
        }
    }

    public int getClickCounter() {
        return clickCounter;
    }

    public void setClickCounter(int clickCounter) {
        this.clickCounter = clickCounter;
    }
}
