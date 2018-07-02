package com.example.zioerjens.stampy;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;

public class MapsFragment extends Fragment implements OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener {

    private View view;
    private Activity context;
    private ViewPager viewPager;
    private GoogleMap mMap;
    private Map<Marker,Integer> markerList = new HashMap<Marker,Integer>();
    private List<MarkerData> markerData = new ArrayList<MarkerData>();
    private Boolean firstGet = true;
    AlertDialog dialog;

    public MapsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_maps, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        context = (Activity) getContext();
        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        this.view = getView();
        setBtnBackListener();
    }

    public void setBtnBackListener(){
        FloatingActionButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (firstGet) {
            getMarkers();
        }

        Location location = null;

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        }
        if (ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = getLocation();
        }
        if (location != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(46.80111111111111, 8.226666666666667)));
        }
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        Iterator it = markerList.entrySet().iterator();
        createShopPopUp(markerList.get(marker));
    }

    public void createShopPopUp(Integer key){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = getLayoutInflater().inflate(R.layout.shop_info, null);
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

    public void getMarkers(){ //TODO gets called after doing something in another fragment -> provides error
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

        firstGet = false;
    }

    public void setViewPager(ViewPager viewPager){
        this.viewPager = viewPager;
    }

    public Location getLocation(){

            LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            Location location = null;
            Criteria criteria = new Criteria();
            String best = locationManager.getBestProvider(criteria, false);
        try {
            location = locationManager.getLastKnownLocation(best);
            return location;
        } catch (SecurityException e){
            return null;
        }
    }
}
