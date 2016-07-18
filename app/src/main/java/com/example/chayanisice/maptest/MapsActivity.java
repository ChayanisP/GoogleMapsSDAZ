package com.example.chayanisice.maptest;

import com.example.chayanisice.maptest.CustomFrameLayout.DragCallback;

import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

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
    private EditText constantEditText;
    private boolean isStartDrag;
    private int formula;
    private double constant;

    private LatLng prevNPos;
    private int posCounter;
    private long prevNTime;
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
        constantEditText = (EditText) findViewById(R.id.constant_text);

        Spinner spinner = (Spinner) findViewById(R.id.eqs_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.eqs_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).toString().equalsIgnoreCase("formula 1")) {
                    formula = 1;
                } else formula = 2;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
        double zoomLevel;
        mCameraTextView.setText("Formula: "+formula+" "+constant);
        if(formula == 1) zoomLevel = getZoomLevel();
        else zoomLevel = getZoomLv();
        mMap.animateCamera(CameraUpdateFactory.zoomTo((float) zoomLevel), 7, null);
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
    public void onFling(long time, double speedX, double speedY) {
        mCameraTextView.setText("Fling");
        //CameraPosition cameraPosition = CameraPosition.builder().target(mMap.getCameraPosition().target).zoom(15).bearing(mMap.getCameraPosition().bearing).build();
        //mMap.getUiSettings().setAllGesturesEnabled(false);

        LatLng currentTarget = mMap.getCameraPosition().target;
        Point screenPoint = mMap.getProjection().toScreenLocation(currentTarget);
        Point newPoint = new Point(screenPoint.x - (int)(speedX*time/1000), screenPoint.y - (int)(speedY*time/1000));
        LatLng mapNewTarget = mMap.getProjection().fromScreenLocation(newPoint);

        mCameraTextView.append(screenPoint.x + "," + screenPoint.y + " disX: " + (int) (speedX * time / 1000) + " disY: " + (int) (speedY * time / 1000) +
                " time: " + time + " speedX: " + speedX + " speedY " + speedY);

        mMap.animateCamera(CameraUpdateFactory.newLatLng(mapNewTarget), 500, new GoogleMap.CancelableCallback() {

            @Override
            public void onFinish() {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(baseZoom), 2000, null);
            }

            @Override
            public void onCancel() {

            }
        });
        //mMap.moveCamera(CameraUpdateFactory.zoomTo(baseZoom));
        mTapTextView.setText("Current1: " + mMap.getCameraPosition().zoom);
    }

    public double getZoomLevel(){
        CameraPosition currentCamera = mMap.getCameraPosition();
        long currentTime = System.currentTimeMillis();
        constant = 0.06;
        if(!constantEditText.getText().toString().equals(""))    constant = Double.parseDouble(constantEditText.getText().toString());

        if(isStartDrag){
            isStartDrag = false;
            baseZoom = currentCamera.zoom;
            prevPos = currentCamera.target;
            prevTime = currentTime;
            posCounter = 0;
            prevNPos = currentCamera.target;
            prevNTime = currentTime;
        }

        double distance = distance(prevPos.latitude, prevPos.longitude, currentCamera.target.latitude, currentCamera.target.longitude);
        double time = currentTime - prevTime;
        double zoomLevel;
        if(distance == 0)   zoomLevel = baseZoom;
        else zoomLevel = Math.min(Math.log(156.543*constant*time/distance)/Math.log(2),baseZoom);

        mTapTextView.setText("Current: " + currentCamera.zoom + " ZL: " + zoomLevel + " base: " + baseZoom);
        //mCameraTextView.setText(""+distance);
        //mMap.animateCamera(CameraUpdateFactory.zoomTo((float)zoomLevel), 10, null);

        if(posCounter % 20 == 0){
            double distanceN = distance(prevNPos.latitude, prevNPos.longitude, currentCamera.target.latitude, currentCamera.target.longitude);
            double timeN = currentTime - prevNTime;

            if(distanceN == 0)   zoomLevel = baseZoom;
            else zoomLevel = Math.min(Math.log(156.543*constant*timeN/distanceN)/Math.log(2),baseZoom);

            dragDetection.setText("zoom level for many events: "+zoomLevel);

            prevNTime = currentTime;
            prevNPos = currentCamera.target;
        }

        posCounter++;
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
        constant = 0.36;
        if(!constantEditText.getText().toString().equals(""))    constant = Double.parseDouble(constantEditText.getText().toString());

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

        //dragDetection.setText("Velocity: " + velocity+"dis: "+distance);

        temp = Math.max(velocity,constant/Math.pow(2,baseZoom));
        zoomLevel = Math.log(constant/temp)/Math.log(2);

        mTapTextView.setText("Current: " + currentCamera.zoom + " ZL: " + zoomLevel + " base: " + baseZoom);

        //mMap.animateCamera(CameraUpdateFactory.zoomTo((float)zoomLevel), 10, null);

        prevPos = currentCamera.target;
        prevTime = currentTime;

        return zoomLevel;
    }


}
