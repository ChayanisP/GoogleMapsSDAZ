package com.example.chayanisice.maptest;

import com.example.chayanisice.maptest.CustomFrameLayout.DragCallback;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CameraPosition;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnMapClickListener, OnMapLongClickListener, OnCameraChangeListener, DragCallback {

    private GoogleMap mMap;
    private TextView mTapTextView;
    private TextView mCameraTextView;
    private TextView dragDetection;
    private DrawView drawLine;
    private boolean isStartDrag;
    private float baseZoom;
    private LatLng prevPos;
    private long prevTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mTapTextView = (TextView) findViewById(R.id.tap_text);
        mCameraTextView = (TextView) findViewById(R.id.camera_text);
        dragDetection = (TextView) findViewById(R.id.drag_text);
        drawLine = (DrawView) findViewById(R.id.draw_line);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        CustomFrameLayout mapRoot = (CustomFrameLayout) findViewById(R.id.map1);
        mapRoot.setOnDragListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);

        // Add a marker in Sydney and move the camera
        LatLng edinburgh = new LatLng(55.95, -3.19);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        //mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.addMarker(new MarkerOptions().position(edinburgh).title("Marker in Edinburgh"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(edinburgh));
        //mMap.moveCamera(CameraUpdateFactory.zoomTo(12));

        isStartDrag = true;
        baseZoom = mMap.getCameraPosition().zoom;
    }

    @Override
    public void onMapClick(LatLng point) {
        mTapTextView.setText("tapped, point=" + point);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        mTapTextView.setText("long pressed, point=" + point);
    }

    @Override
    public void onCameraChange(final CameraPosition position) {
        //mCameraTextView.setText(position.target.toString());
    }

    @Override
    public void onDrag() {
        long timeDiff;
        double distance, LatDiff, LngDiff, velocity, zoomLevel, temp;

        if(isStartDrag){
            isStartDrag = false;
            baseZoom = mMap.getCameraPosition().zoom;
            prevPos = mMap.getCameraPosition().target;
            prevTime = System.currentTimeMillis();
        }
        timeDiff = System.currentTimeMillis() - prevTime;
        LatDiff = Math.abs(prevPos.latitude - mMap.getCameraPosition().target.latitude);
        LngDiff = Math.abs(prevPos.longitude - mMap.getCameraPosition().target.longitude);

        distance = Math.sqrt(Math.pow(LatDiff, 2) + Math.pow(LngDiff, 2));
        velocity = distance/timeDiff;

        //dragDetection.setText("Velocity: " + velocity + " PrevPos: " + prevPos.toString() + " CurPos: " + mMap.getCameraPosition().target.toString());
        //dragDetection.setText("Velocity: " + velocity + " Distance: " + distance + " Time: "+ timeDiff);
        dragDetection.setText("Velocity: " + velocity);

        if(velocity > 0.36/Math.pow(2,baseZoom))    temp = velocity;
        else temp = 0.36/Math.pow(2,baseZoom);
        zoomLevel = Math.log(0.36/temp)/Math.log(2);

        mTapTextView.setText("Current: " +mMap.getCameraPosition().zoom+" ZL: "+zoomLevel+" base: "+baseZoom );

        if(mMap.getCameraPosition().zoom > zoomLevel)
            mMap.moveCamera(CameraUpdateFactory.zoomBy((float) -0.1));
            //mMap.moveCamera(CameraUpdateFactory.zoomTo((float)zoomLevel));
        else if(mMap.getCameraPosition().zoom < baseZoom)   mMap.moveCamera(CameraUpdateFactory.zoomBy((float)0.1));
        //else    mMap.animateCamera(CameraUpdateFactory.zoomTo((float)zoomLevel), 5000, null);

        prevPos = mMap.getCameraPosition().target;
        prevTime = System.currentTimeMillis();
        mCameraTextView.setText("dragging: "+(float)zoomLevel+", "+temp+", "+0.36/Math.pow(2,baseZoom));
    }

    @Override
    public void noDrag() {
        //mMap.moveCamera(CameraUpdateFactory.zoomTo(baseZoom));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(baseZoom), 2000, null);
        isStartDrag = true;
        mTapTextView.setText("Current: " +mMap.getCameraPosition().zoom);
        mCameraTextView.append("Finger up: " + System.currentTimeMillis());
    }

    @Override
    public void onFling() {
        mCameraTextView.setText("Fling: "+System.currentTimeMillis());
        //CameraPosition cameraPosition = CameraPosition.builder().target(mMap.getCameraPosition().target).zoom(15).bearing(mMap.getCameraPosition().bearing).build();
        mMap.animateCamera(CameraUpdateFactory.zoomTo(baseZoom), 2000, null);
        mTapTextView.setText("Current1: " +mMap.getCameraPosition().zoom);
    }
}
