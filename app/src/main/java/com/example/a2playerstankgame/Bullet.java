package com.example.a2playerstankgame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Bullet {
    private float xpos;
    private float ypos;
    private float angle;
    private int countPatt;

    boolean isAlive;
    boolean own;
    Bitmap bulletBitmap;



    Bullet(float xpos,float ypos,float angle,boolean own){
        this.xpos = xpos;
        this.ypos = ypos;
        this.angle = angle;
        this.isAlive = true;
        this.own = own;
    }

    public float getXpos(){
        return xpos;
    }
    public float getYpos(){
        return ypos;
    }
    public boolean getOwn(){
        return own;
    }

    public void setPosition(float x,float y){
        this.xpos = x;
        this.ypos = y;
    }

    public void setBulletBitmap(Bitmap bitmap){
        bulletBitmap = bitmap;
    }

    public Bitmap getBulletBitmap(){
        return bulletBitmap;
    }

    public void moveBullet(){
        this.xpos -= 0*cos(angle)-5*sin(angle);
        this.ypos -= 5*cos(angle)+0*sin(angle);
    }

    public synchronized void setAngle(float angle){
        this.angle = angle;
    }

    public synchronized float getAngle(){
        return angle;
    }

    public int getCount(){
        return countPatt;
    }

    public void incrCount(){
        countPatt++;
    }

    public void setIsAlive(boolean log){
        isAlive = log;
    }

    public boolean getIsAlive(){
        return isAlive;
    }

    public static final double cos(double angle){
        return Math.cos(Math.toRadians(angle));
    }

    public static final double sin(double angle){
        return Math.sin(Math.toRadians(angle));
    }



}
