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
    private EditText timeEditText;
    private boolean isStartDrag=true;
    private int formula=1;
    private double constant;
    private double inputTime;

    private float baseZoom;
    private LatLng prevPos;
    private long prevTime;
    private long ptime;

    private F1ZoomCalculator currentZoomCalculator1 = new F1ZoomCalculator(0.06);
    private F2ZoomCalculator currentZoomCalculator2 = new F2ZoomCalculator(0.36);

    private Queue<Event> squeue = new LinkedList<Event>();
    private Queue<Event> gqueue = new LinkedList<Event>();
    private double timeBasedScreenSpeed = 0.0;
    private double timeBasedGroundSpeed = 0.0;
    private double screenSpeed = 0.0;
    private double groundSpeed = 0.0;

    private int choice=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mTapTextView = (TextView) findViewById(R.id.tap_text);
        mCameraTextView = (TextView) findViewById(R.id.camera_text);
        dragDetection = (TextView) findViewById(R.id.drag_text);
        constantEditText = (EditText) findViewById(R.id.constant_text);
        timeEditText = (EditText) findViewById(R.id.time_text);

        Spinner spinner = (Spinner) findViewById(R.id.eqs_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.eqs_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("ground speed 1")) {
                    formula = 1;
                    constant = 0.06;
                    if (!constantEditText.getText().toString().equals(""))
                        constant = Double.parseDouble(constantEditText.getText().toString());
                    currentZoomCalculator1 = new F1ZoomCalculator(constant);
                } else if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("ground speed 2")) {
                    formula = 2;
                    constant = 0.36;
                    if (!constantEditText.getText().toString().equals(""))
                        constant = Double.parseDouble(constantEditText.getText().toString());
                    currentZoomCalculator2 = new F2ZoomCalculator(constant);
                } else if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("screen speed")) {
                    formula = 3;
                    constant = 0.256;
                    if (!constantEditText.getText().toString().equals(""))
                        constant = Double.parseDouble(constantEditText.getText().toString());
                    currentZoomCalculator2 = new F2ZoomCalculator(constant);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner spinner1 = (Spinner) findViewById(R.id.tspd_spinner);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.tspd_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("default")) {
                    choice = 1;
                } else if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("time based speed")) {
                    choice = 2;
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
    public void onDrag(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mCameraTextView.setText("Formula: " + formula + " " + constant);

        screenSpeedCalculation(e1, e2, distanceX, distanceY);
        groundSpeedCalculation();

        double zoomLevel = getZoomLevel();
        mMap.animateCamera(CameraUpdateFactory.zoomTo((float) zoomLevel), 7, null);

    }

    @Override
    public void noDrag() {
        double tempZoom = baseZoom - mMap.getCameraPosition().zoom;
        int time = (int) (tempZoom * 500);
        int isZero = Double.compare(tempZoom,0.0);
        if(time > 0) {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(baseZoom), time, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    isStartDrag = true;
                    System.out.println("Finset");
                }

                @Override
                public void onCancel() {
                }
            });
        } else if(isZero == 0)    {
            isStartDrag = true;
        }
        squeue.clear();
        gqueue.clear();
        mTapTextView.setText("Current: " + mMap.getCameraPosition().zoom + " time: " + time + " zoom: " + tempZoom);
        System.out.println("Current: " + mMap.getCameraPosition().zoom + " time: " + time + " zoom: " + tempZoom);
    }

    @Override
    public void onFling(double speedX, double speedY) {
        mCameraTextView.setText("Fling");
        squeue.clear();
        gqueue.clear();

        LatLng currentTarget = mMap.getCameraPosition().target;
        Point screenPoint = mMap.getProjection().toScreenLocation(currentTarget);
        Point newPoint = new Point(screenPoint.x - (int)(speedX/4), screenPoint.y - (int)(speedY/4));
        LatLng mapNewTarget = mMap.getProjection().fromScreenLocation(newPoint);

        mCameraTextView.append(screenPoint.x + "," + screenPoint.y + " speedX: " + speedX + " speedY " + speedY);

        float curZoom = mMap.getCameraPosition().zoom;

        double speed = Math.sqrt(Math.pow(speedX, 2) + Math.pow(speedY, 2))*0.001;
        /*speed = speed/Math.pow(2,curZoom);
        F2ZoomCalculator f2 = new F2ZoomCalculator(16);
        double zoomLevel = f2.getZoom(speed, baseZoom);*/

        /*float tempZoom = baseZoom - curZoom;
        if(tempZoom > 0.25) tempZoom = (float) 0.25;*/
        final float restZoom = baseZoom - curZoom + (float) (speed/16);// - tempZoom;
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapNewTarget, (curZoom + tempZoom)), 250, new GoogleMap.CancelableCallback() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapNewTarget, (float) (curZoom - (speed/16))), 250, new GoogleMap.CancelableCallback() {
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapNewTarget, (float) zoomLevel), 250, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                if(restZoom > 0)
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(baseZoom), (int) (restZoom*4000) , null);
            }

            @Override
            public void onCancel() {
            }
        });

        mTapTextView.setText("Current1: " + mMap.getCameraPosition().zoom);
    }

    public double getZoomLevel(){
        double zoomLevel;
        CameraPosition currentCamera = mMap.getCameraPosition();

        switch (formula){
            case 1:
                if(choice==1)   zoomLevel = currentZoomCalculator1.getZoom(groundSpeed, baseZoom);
                else zoomLevel = currentZoomCalculator1.getZoom(timeBasedGroundSpeed, baseZoom);
                break;
            case 2:
                if(choice==1)   zoomLevel = currentZoomCalculator2.getZoom(groundSpeed, baseZoom);
                else zoomLevel = currentZoomCalculator2.getZoom(timeBasedGroundSpeed, baseZoom);
                break;
            case 3:
                double ssspeed;
                if(choice==1)   ssspeed = screenSpeed/Math.pow(2,currentCamera.zoom);
                else ssspeed = timeBasedScreenSpeed/Math.pow(2,currentCamera.zoom);
                zoomLevel = currentZoomCalculator2.getZoom(ssspeed, baseZoom);
                break;
            default:
                if(choice==1)   zoomLevel = currentZoomCalculator1.getZoom(groundSpeed, baseZoom);
                else zoomLevel = currentZoomCalculator1.getZoom(timeBasedGroundSpeed, baseZoom);
                break;
        }

        //-------print------

        mTapTextView.setText("Current: " + currentCamera.zoom + " ZL: " + zoomLevel + " base: " + baseZoom);

        F2ZoomCalculator fo2 = new F2ZoomCalculator(0.36);
        double zoomLevel1 = fo2.getZoom(groundSpeed, baseZoom);
        String string = "Gspeed: "+groundSpeed+" Sspeed: " + screenSpeed + " zoomLv1: " + zoomLevel + " zoomLv2: " + zoomLevel1 + " base: " + baseZoom;
        System.out.println(string);

        //------------------

        return zoomLevel;
    }

    public void screenSpeedCalculation(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY){
        if(isStartDrag){
            Event event1 = new Event(e1.getX(), e1.getY(),e1.getEventTime());
            squeue.add(event1);
            ptime = e1.getEventTime();
        }

        Event event = new Event(e2.getX(), e2.getY(), e2.getEventTime());
        squeue.add(event);

        inputTime = 1000;
        if (!timeEditText.getText().toString().equals(""))
            inputTime = (Double.parseDouble(timeEditText.getText().toString()))*1000;

        while(event.getTime() - squeue.peek().getTime() > inputTime){
            squeue.remove();
        }

        double x = squeue.peek().getPosX() - event.getPosX();
        double y = squeue.peek().getPosY() - event.getPosY();
        double dist = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        if((event.getTime() - squeue.peek().getTime())==0)  timeBasedScreenSpeed = 0;
        else timeBasedScreenSpeed = dist/(event.getTime() - squeue.peek().getTime());

        dragDetection.setText("one sec speed: " + timeBasedScreenSpeed + " size: " + squeue.size()+" time: "+inputTime);

        double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
        if((ptime - e2.getEventTime())==0)   screenSpeed = 0;
        else screenSpeed = distance/Math.abs(ptime - e2.getEventTime());

        ptime = e2.getEventTime();
    }

    public void groundSpeedCalculation(){
        CameraPosition currentCamera = mMap.getCameraPosition();
        long currentTime = System.currentTimeMillis();

        if(!constantEditText.getText().toString().equals(""))
            if(constant != Double.parseDouble(constantEditText.getText().toString())){
                constant = Double.parseDouble(constantEditText.getText().toString());
                if(formula == 1)    currentZoomCalculator1 = new F1ZoomCalculator(constant);
                else if(formula == 2 || formula == 3)   currentZoomCalculator2 = new F2ZoomCalculator(constant);
            }

        if(isStartDrag){
            isStartDrag = false;
            baseZoom = currentCamera.zoom;
            prevPos = currentCamera.target;
            prevTime = currentTime;
            Event e1 = new Event(currentCamera.target.latitude, currentCamera.target.longitude, currentTime);
            gqueue.add(e1);
        }

        Event e2 = new Event(currentCamera.target.latitude, currentCamera.target.longitude, currentTime);
        gqueue.add(e2);

        inputTime = 1000;
        if (!timeEditText.getText().toString().equals(""))
            inputTime = (Double.parseDouble(timeEditText.getText().toString()))*1000;

        while(e2.getTime() - gqueue.peek().getTime() > inputTime){
            gqueue.remove();
        }

        double x = gqueue.peek().getPosX() - e2.getPosX();
        double y = gqueue.peek().getPosY() - e2.getPosY();
        double dist = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        if((e2.getTime() - gqueue.peek().getTime())==0)  timeBasedGroundSpeed = 0;
        else timeBasedGroundSpeed = dist/(e2.getTime() - gqueue.peek().getTime());

        dragDetection.append("one sec speed: " + timeBasedGroundSpeed + " size: " + gqueue.size()+" time: "+inputTime);

        //double distance = distance(prevPos.latitude, prevPos.longitude, currentCamera.target.latitude, currentCamera.target.longitude);

        double LatDiff = Math.abs(prevPos.latitude - currentCamera.target.latitude);
        double LngDiff = Math.abs(prevPos.longitude - currentCamera.target.longitude);
        double distance = Math.sqrt(Math.pow(LatDiff, 2) + Math.pow(LngDiff, 2));

        double time = currentTime - prevTime;

        if(time == 0) { groundSpeed = 0;}
        else {
            groundSpeed = distance/time;
        }
        prevPos = currentCamera.target;
        prevTime = currentTime;
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
