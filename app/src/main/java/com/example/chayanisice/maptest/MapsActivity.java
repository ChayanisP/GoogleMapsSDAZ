package com.example.chayanisice.maptest;

import com.example.chayanisice.maptest.CustomFrameLayout.DragCallback;

import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.MotionEvent;
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

import java.util.LinkedList;
import java.util.Queue;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnMapClickListener, OnMapLongClickListener, OnCameraChangeListener, DragCallback {

    private GoogleMap mMap;
    private TextView mTapTextView;
    private TextView mCameraTextView;
    private TextView dragDetection;
    private EditText constantEditText;
    private boolean isStartDrag;
    private int formula=1;
    private double constant;

    private float baseZoom;
    private LatLng prevPos;
    private long prevTime;
    private double startTime;

    private Queue<Event> queue = new LinkedList<Event>();

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
                if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("formula 1")) {
                    formula = 1;
                } else if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("formula 2")) {
                    formula = 2;
                }
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
    public void onDrag(MotionEvent e1, MotionEvent e2) {
        mCameraTextView.setText("Formula: "+formula+" "+constant);

        if(isStartDrag){
            Event event1 = new Event();
            event1.setPosX(e1.getX());
            event1.setPosY(e1.getY());
            event1.setTime(e1.getEventTime());
            queue.add(event1);
        }
        Event event = new Event();
        event.setPosX(e2.getX());
        event.setPosY(e2.getY());
        event.setTime(e2.getEventTime());
        queue.add(event);

        while(event.getTime() - queue.peek().getTime() > 1000){
            queue.remove();
        }

        float x = queue.peek().getPosX() - event.getPosX();
        float y = queue.peek().getPosY() - event.getPosY();
        double dist = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        double speed = dist/1000*(event.getTime() - queue.peek().getTime());

        dragDetection.setText("one sec speed: "+speed+" size: "+queue.size());

        double zoomLevel = getZoomLevel();
        mMap.animateCamera(CameraUpdateFactory.zoomTo((float) zoomLevel), 7, null);
    }

    @Override
    public void noDrag() {
        mMap.animateCamera(CameraUpdateFactory.zoomTo(baseZoom), 500, null);
        isStartDrag = true;
        queue.clear();
        mTapTextView.setText("Current: " + mMap.getCameraPosition().zoom);
    }

    @Override
    public void onFling(double speedX, double speedY) {
        mCameraTextView.setText("Fling");

        LatLng currentTarget = mMap.getCameraPosition().target;
        Point screenPoint = mMap.getProjection().toScreenLocation(currentTarget);
        Point newPoint = new Point(screenPoint.x - (int)(speedX/4), screenPoint.y - (int)(speedY/4));
        LatLng mapNewTarget = mMap.getProjection().fromScreenLocation(newPoint);

        mCameraTextView.append(screenPoint.x + "," + screenPoint.y + " speedX: " + speedX + " speedY " + speedY);

        float curZoom = mMap.getCameraPosition().zoom;
        float tempZoom = curZoom + (baseZoom - curZoom)/4;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapNewTarget,tempZoom), 500, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(baseZoom), 4000, null);
            }
            @Override
            public void onCancel() {
            }
        });

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
            startTime = currentTime;
        }

        double distance1 = distance(prevPos.latitude, prevPos.longitude, currentCamera.target.latitude, currentCamera.target.longitude);

        double LatDiff = Math.abs(prevPos.latitude - currentCamera.target.latitude);
        double LngDiff = Math.abs(prevPos.longitude - currentCamera.target.longitude);
        double distance2 = Math.sqrt(Math.pow(LatDiff, 2) + Math.pow(LngDiff, 2));

        double time = currentTime - prevTime;

        double speed1, speed2;
        if(time == 0) { speed1 = 0; speed2 = 0; }
        else {
            speed1 = distance1/time;
            speed2 = distance2/time;
        }

        double zoomLevel;

        switch (formula){
            case 1:
                constant = 0.06;
                if(!constantEditText.getText().toString().equals(""))    constant = Double.parseDouble(constantEditText.getText().toString());
                F1ZoomCaculator f1 = new F1ZoomCaculator();
                zoomLevel = f1.getZoom(speed1, constant, baseZoom);
                break;
            case 2:
                constant = 0.36;
                if(!constantEditText.getText().toString().equals(""))    constant = Double.parseDouble(constantEditText.getText().toString());
                F2ZoomCaculator f2 = new F2ZoomCaculator();
                zoomLevel = f2.getZoom(speed2, constant, baseZoom);
                break;
            default:
                constant = 0.06;
                if(!constantEditText.getText().toString().equals(""))    constant = Double.parseDouble(constantEditText.getText().toString());
                F1ZoomCaculator fdef = new F1ZoomCaculator();
                zoomLevel = fdef.getZoom(speed1, constant, baseZoom);
                break;
        }

        mTapTextView.setText("Current: " + currentCamera.zoom + " ZL: " + zoomLevel + " base: " + baseZoom);

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


}
