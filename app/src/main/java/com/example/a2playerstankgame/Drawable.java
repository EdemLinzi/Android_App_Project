package com.example.a2playerstankgame;

import android.graphics.Bitmap;


public class Drawable {


    private float xpos;
    private float ypos;

    private float angle;
    private float hullAngle;


    private Bitmap tankBitmap;


    public Drawable(){
        this.xpos = 0;
        this.ypos = 0;
        this.angle = 0;
    }

    public Drawable(float xpos,float ypos,Bitmap bitmap){
        this.xpos = xpos;
        this.ypos = ypos;
        this.tankBitmap = bitmap;
    }


    public void move(float x, float y){
            xpos += x;
            ypos += y;
    }

    public float getXpos() {
        return xpos;
    }

    public float getYpos() {
        return ypos;
    }

    public Bitmap getTankBitmap() {
        return tankBitmap;
    }

    public void setTankBitmap(Bitmap bitmap) {
        this.tankBitmap  = bitmap;
    }



    public void setPos(float x,float y){
        this.xpos = x;
        this.ypos = y;
    }

    public synchronized float getAngle() {
        return angle;
    }

    public synchronized void setAngle(float angle) {
        this.angle = angle;
    }

    public  void incrAngle(float incr) {
        this.angle+=incr;
        if(angle>=360){
            angle=0;
        }
        else if(angle<0){
            angle=360;
        }
    }

    public synchronized float getHullAngle() {
        return hullAngle;
    }

    public synchronized void setHullAngle(float hullAngle) {
        this.hullAngle = hullAngle;
    }


}
