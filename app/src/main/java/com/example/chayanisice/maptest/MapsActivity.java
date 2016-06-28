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
        mMap.addMarker(new MarkerOptions().position(edinburgh).title("Marker in Edinburgh"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(edinburgh));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12));
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
    public void onDrag(double distance, long time, float prevX, float prevY, float curX, float curY) {
        double velocity = distance/time;
        dragDetection.setText("dragging" + " Velocity: " + velocity + " prevPos: " + prevX + ", " + prevY + " curPos: " + curX + ", " + curY);
        drawLine.setPositions(prevX, prevY, curX, curY);
        mCameraTextView.setText(mMap.getCameraPosition().target.toString());
        if(velocity > 1)    mMap.moveCamera(CameraUpdateFactory.zoomBy((float)-0.1));
        //else mMap.moveCamera(CameraUpdateFactory.zoomBy((float)0.1));
    }

    @Override
    public void noDrag() {
        //dragDetection.setText("no dragging");
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12));
    }
}
