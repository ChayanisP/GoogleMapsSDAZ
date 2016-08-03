package com.example.chayanisice.maptest;

/**
 * Created by chayanisice on 7/25/16.
 */

interface ZoomCalculator {
    double getZoomGround (double speed, float baseZoom, float currentZoom, double zoomGearing);
    double getZoomScreen (double speed, float baseZoom, float currentZoom, double zoomGearing);
}

class F1ZoomCalculator implements ZoomCalculator{

    private double constant;

    public F1ZoomCalculator(double constant){
        this.constant = constant;
    }

    @Override
    public double getZoomGround(double speed, float baseZoom, float currentZoom, double zoomGearing) {
        double zoomLevel;
        if(speed == 0)   zoomLevel = baseZoom;
        else zoomLevel = Math.min((Math.log(constant)+Math.log(360)-Math.log(speed)-Math.log(256))/Math.log(2),baseZoom);
        zoomLevel = Math.min(currentZoom+(zoomGearing*(zoomLevel - currentZoom)),baseZoom);
        return zoomLevel;
    }

    @Override
    public double getZoomScreen(double speed, float baseZoom, float currentZoom, double zoomGearing) {
        double zoomLevel;
        if(speed == 0)   zoomLevel = baseZoom;
        else zoomLevel = Math.min(currentZoom+(zoomGearing*((Math.log(constant)-Math.log(speed))/Math.log(2))),baseZoom);
        return zoomLevel;
    }
}
