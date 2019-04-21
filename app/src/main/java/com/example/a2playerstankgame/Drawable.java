package com.example.a2playerstankgame;

import android.graphics.Bitmap;


public class Drawable {


    private float xpos;
    private float ypos;


    private Bitmap tankBitmap;

    public Drawable(){
        this.xpos = 0;
        this.ypos = 0;
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


    public void setPos(float x,float y){
        this.xpos = x;
        this.ypos = y;
    }

}
