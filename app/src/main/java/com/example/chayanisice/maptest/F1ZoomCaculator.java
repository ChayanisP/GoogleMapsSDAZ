package com.example.chayanisice.maptest;

/**
 * Created by chayanisice on 7/25/16.
 */

interface ZoomCalculator {
    double getZoom (double speed, float baseZoom);
}

class F1ZoomCaculator implements ZoomCalculator{

    private double constant;
    public F1ZoomCaculator (double constant){
        this.constant = constant;
    }

    @Override
    public double getZoom(double speed, float baseZoom) {
        double zoomLevel;
        if(speed == 0)   zoomLevel = baseZoom;
        else zoomLevel = Math.min(Math.log(1.40625*constant/speed)/Math.log(2),baseZoom);
        return zoomLevel;
    }
}

class F2ZoomCaculator implements ZoomCalculator{

    private double constant;
    public F2ZoomCaculator (double constant){
        this.constant = constant;
    }
    @Override
    public double getZoom(double speed, float baseZoom) {
        double zoomLevel;
        if(speed == 0)   zoomLevel = baseZoom;
        else {
            zoomLevel = Math.min(Math.log(constant/speed)/Math.log(2),baseZoom);
        }
        return zoomLevel;
    }
}
