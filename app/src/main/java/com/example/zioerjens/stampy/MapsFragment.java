package com.example.zioerjens.stampy;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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
import java.util.List;
import java.util.Map;

public class MapsFragment extends Fragment implements OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener {

    private View view;
    private Activity context;
    private ViewPager viewPager;
    private GoogleMap mMap;
    private Map<Marker,Integer> markerList;
    private List<MarkerData> markerData;
    AlertDialog dialog;

    public MapsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_maps, container, false);//TODO create fragment maps, edit main.java...
        //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        markerList = new HashMap<Marker,Integer>();
        markerData = new ArrayList<MarkerData>();
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

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
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

    public void setViewPager(ViewPager viewPager){
        this.viewPager = viewPager;
    }
}
