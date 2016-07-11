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
        //mTapTextView.setText("tapped, point=" + point);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        //mTapTextView.setText("long pressed, point=" + point);
    }

    @Override
    public void onCameraChange(final CameraPosition position) {
        //mCameraTextView.setText(position.target.toString());
    }

    @Override
    public void onDrag() {
        double zoomLevel = getZoomLevel();
        //double zoomLevel = getZoomLv();
        mMap.animateCamera(CameraUpdateFactory.zoomTo((float)zoomLevel), 7, null);
    }

    @Override
    public void noDrag() {
        //mMap.moveCamera(CameraUpdateFactory.zoomTo(baseZoom));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(baseZoom), 500, null);
        isStartDrag = true;
        mTapTextView.setText("Current: " + mMap.getCameraPosition().zoom);
        //mCameraTextView.append("Finger up: " + System.currentTimeMillis());
    }

    @Override
    public void onFling() {
        mCameraTextView.setText("Fling");
        //CameraPosition cameraPosition = CameraPosition.builder().target(mMap.getCameraPosition().target).zoom(15).bearing(mMap.getCameraPosition().bearing).build();
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(baseZoom), 5000, new GoogleMap.CancelableCallback() {

            @Override
            public void onFinish() {
                mMap.getUiSettings().setAllGesturesEnabled(true);
            }

            @Override
            public void onCancel() {
                mMap.getUiSettings().setAllGesturesEnabled(true);
            }
        });
        //mMap.moveCamera(CameraUpdateFactory.zoomTo(baseZoom));
        mTapTextView.setText("Current1: " + mMap.getCameraPosition().zoom);
    }

    public double getZoomLevel(){
        CameraPosition currentCamera = mMap.getCameraPosition();
        long currentTime = System.currentTimeMillis();

        if(isStartDrag){
            isStartDrag = false;
            baseZoom = currentCamera.zoom;
            prevPos = currentCamera.target;
            prevTime = currentTime;
        }

        double distance = distance(prevPos.latitude, prevPos.longitude, currentCamera.target.latitude, currentCamera.target.longitude);
        double time = currentTime - prevTime;
        double zoomLevel;
        if(distance == 0)   zoomLevel = baseZoom;
        else zoomLevel = Math.min(Math.log(156.543*0.06*time/distance)/Math.log(2),baseZoom);

        mTapTextView.setText("Current: " + currentCamera.zoom + " ZL: " + zoomLevel + " base: " + baseZoom);
        mCameraTextView.setText(""+distance);
        //mMap.animateCamera(CameraUpdateFactory.zoomTo((float)zoomLevel), 10, null);

        prevPos = currentCamera.target;
        prevTime = currentTime;

        return zoomLevel;
    }

    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = (earthRadius * c)/1000; //km

        return dist;
    }

    public double getZoomLv() {
        long timeDiff;
        double distance, LatDiff, LngDiff, velocity, zoomLevel, temp;
        CameraPosition currentCamera = mMap.getCameraPosition();
        long currentTime = System.currentTimeMillis();

        if (isStartDrag){
            isStartDrag = false;
            baseZoom = currentCamera.zoom;
            prevPos = currentCamera.target;
            prevTime = currentTime;
        }
        timeDiff = currentTime - prevTime;
        LatDiff = Math.abs(prevPos.latitude - currentCamera.target.latitude);
        LngDiff = Math.abs(prevPos.longitude - currentCamera.target.longitude);

        distance = Math.sqrt(Math.pow(LatDiff, 2) + Math.pow(LngDiff, 2));
        if(timeDiff == 0)   velocity = 0;
        else velocity = distance/timeDiff;

        dragDetection.setText("Velocity: " + velocity+"dis: "+distance);

        temp = Math.max(velocity,0.36/Math.pow(2,baseZoom));
        zoomLevel = Math.log(0.36/temp)/Math.log(2);

        mTapTextView.setText("Current: " + currentCamera.zoom + " ZL: " + zoomLevel + " base: " + baseZoom);

        //mMap.animateCamera(CameraUpdateFactory.zoomTo((float)zoomLevel), 10, null);

        prevPos = currentCamera.target;
        prevTime = currentTime;

        return zoomLevel;
    }


}
