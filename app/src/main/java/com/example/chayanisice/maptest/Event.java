package com.example.chayanisice.maptest;

/**
 * Created by chayanisice on 7/26/16.
 */
public class Event {
    private double posX;
    private double posY;
    private double time;

    public Event(double posX, double posY, double time){
        this.posX = posX;
        this.posY = posY;
        this.time = time;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }


}
