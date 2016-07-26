package com.example.chayanisice.maptest;

/**
 * Created by chayanisice on 7/25/16.
 */

interface ZoomCalculator {
    double getZoom (double speed, double constant, float baseZoom);
}

class F1ZoomCaculator implements ZoomCalculator{
    @Override
    public double getZoom(double speed, double constant, float baseZoom) {
        double zoomLevel;
        if(speed == 0)   zoomLevel = baseZoom;
        else zoomLevel = Math.min(Math.log(156.543*constant/speed)/Math.log(2),baseZoom);
        return zoomLevel;
    }
}

class F2ZoomCaculator implements ZoomCalculator{
    @Override
    public double getZoom(double speed, double constant, float baseZoom) {
        double zoomLevel;
        if(speed == 0)   zoomLevel = baseZoom;
        else {
            double temp = Math.max(speed,constant/Math.pow(2,baseZoom));
            zoomLevel = Math.log(constant/temp)/Math.log(2);
        }
        return zoomLevel;
    }
}
