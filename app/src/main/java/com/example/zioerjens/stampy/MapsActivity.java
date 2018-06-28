package com.example.zioerjens.stampy;

import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private MapsActivity activity;
    private Map<Marker,Integer> markerList;
    private List<MarkerData> markerData;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        markerList = new HashMap<Marker,Integer>();
        markerData = new ArrayList<MarkerData>();

        this.activity = this;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        getMarkers();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(46.80111111111111,8.226666666666667)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(7));
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        createShopPopUp(markerList.get(marker));
    }

    public void createShopPopUp(Integer key){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
        View mView = getLayoutInflater().inflate(R.layout.shop_info,null);
        TextView name = mView.findViewById(R.id.name);
        name.setText(markerData.get(key).name);
        TextView content = mView.findViewById(R.id.content);
        content.setText(markerData.get(key).content);
        mBuilder.setView(mView);
        dialog = mBuilder.create();
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    public void getMarkers(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("shop");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot grSnapshot : dataSnapshot.getChildren()) {
                    // Get Post object and use the values to update the UI
                    MarkerData marker = grSnapshot.getValue(MarkerData.class);
                    markerData.add(marker);
                }
                addMarkers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void addMarkers(){
        for (int i = 0; i < markerData.size(); i++) {
            LatLng coords = new LatLng(Double.parseDouble(markerData.get(i).latitude), Double.parseDouble(markerData.get(i).longitude));
            markerList.put(mMap.addMarker(new MarkerOptions().position(coords).title(markerData.get(i).name)), i);
        }
    }
}
