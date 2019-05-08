package com.example.a2playerstankgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.view.View;

public class GameView extends View {

    Drawable tank1;
    Drawable tankhull;
    Drawable tank2;
    Bitmap map ;


    public GameView(Context context,Drawable tank1,Drawable tank2,Drawable tankhull,int own) {
        super(context);
        if(own == 0) {
            this.tank1 = tank1;
            this.tank2 = tank2;
        }
        if(own == 1){
            this.tank1 = tank2;
            this.tank2 = tank1;
        }
        this.tankhull = tankhull;
        this.map = BitmapFactory.decodeResource(getResources(),R.drawable.map);
    }


    public synchronized boolean canIMove(float px,float py){
        float xpos = tank1.getXpos()+px;
        float ypos = tank1.getYpos()+py;
        float[] tankImageSize = {tank1.getTankBitmap().getWidth()/10,tank1.getTankBitmap().getHeight()/10};

        if((xpos - tankImageSize[0]) >= 0 && (xpos) <= (map.getWidth()-tankImageSize[0]) && (ypos - tankImageSize[1]) >= 0 && (ypos) <= (map.getHeight() - tankImageSize[1])){
        float lr = 1;
            if(px != 0) lr = px/5;
        float ud = 1;
            if(py!=0) ud = py/5;
        Log.i("GameView","1 ok px =" + lr);
            if (map.getPixel(Math.round(xpos + (lr * tankImageSize[0])), Math.round(ypos + (ud * tankImageSize[1]))) == Color.BLACK) {
                Log.i("GameView","2 ok");
                //return false;
                return true;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float canvasWidth = getWidth();
        float canvasHeight = getHeight();

        float tankPosX = canvasWidth/2;
        float tankPosY = canvasHeight/2;

       // Log.i("GameView","Xpos "+tank1.getXpos()+" Ypos "+tank1.getYpos());

        float[] tankImageSize = {tank1.getTankBitmap().getWidth()/10,tank1.getTankBitmap().getHeight()/10};
        if(tank1.getXpos() - tankImageSize[0] < 0 ){tank1.setPos(tankImageSize[0],tank1.getYpos());}
        if(tank1.getXpos() + tankImageSize[0] > map.getWidth()){tank1.setPos(map.getWidth()-tankImageSize[0],tank1.getYpos());}
        if(tank1.getYpos() - tankImageSize[1] < 0){tank1.setPos(tank1.getXpos(),tankImageSize[1]);}
        if(tank1.getYpos() + tankImageSize[1] > map.getHeight()){tank1.setPos(tank1.getXpos(),map.getHeight()-tankImageSize[1]);}



        canvas.drawBitmap(map,tankPosX-tank1.getXpos(),tankPosY-tank1.getYpos(),null);


        //Log.i("GameView","Xpos "+tank1.getXpos()+" Xpos "+tank1.getYpos());

        canvas.scale(0.2f,0.2f);

        //Log.i("GameView","HeightScale2 "+canvasHeight+" WidhtScale2 "+canvasWidth);
        canvas.rotate(tank1.getAngle(),canvasWidth*2.5f,canvasHeight*2.5f);
        canvas.drawBitmap(tank1.getTankBitmap(),canvasWidth*2.5f-tank1.getTankBitmap().getWidth()/2,canvasHeight*2.5f-tank1.getTankBitmap().getHeight()/2,null);
        canvas.rotate(-tank1.getAngle(),canvasWidth*2.5f,canvasHeight*2.5f);

        //Log.i("GameView","HeightScale1 "+(canvasWidth*2.5f-tank1.getTankBitmap().getWidth()/2)+" WidhtScale1 "+(canvasHeight*2.5f-tank1.getTankBitmap().getHeight()/2));

        canvas.rotate(tank1.getHullAngle(),canvasWidth*2.5f,canvasHeight*2.5f);
        canvas.drawBitmap(tankhull.getTankBitmap(),canvasWidth*2.5f-tankhull.getTankBitmap().getWidth()/2,canvasHeight*2.5f-tankhull.getTankBitmap().getHeight()/2,null);
        canvas.rotate(-tank1.getHullAngle(),canvasWidth*2.5f,canvasHeight*2.5f);

        float tank2PosX = tankPosX*5-tank1.getXpos()*5+tank2.getXpos()*5-tank2.getTankBitmap().getWidth()/2;
        float tank2PosY = tankPosY*5-tank1.getYpos()*5+tank2.getYpos()*5-tank2.getTankBitmap().getHeight()/2;


        //Log.i("GameView","Xpos2 "+tank2.getXpos()+" Ypos2 "+tank2.getYpos());
        canvas.rotate(tank2.getAngle(),tank2PosX+tank2.getTankBitmap().getWidth()/2,tank2PosY+tank2.getTankBitmap().getHeight()/2);
        canvas.drawBitmap(tank2.getTankBitmap(),tank2PosX,tank2PosY,null);
        canvas.rotate(-tank2.getAngle(),tank2PosX+tank2.getTankBitmap().getWidth()/2,tank2PosY+tank2.getTankBitmap().getHeight()/2);


        canvas.rotate(tank2.getHullAngle(),tank2PosX+tank2.getTankBitmap().getWidth()/2,tank2PosY+tank2.getTankBitmap().getHeight()/2);
        canvas.drawBitmap(tankhull.getTankBitmap(),tank2PosX,tank2PosY,null);
        canvas.rotate(-tank2.getHullAngle(),tank2PosX+tank2.getTankBitmap().getWidth()/2,tank2PosY+tank2.getTankBitmap().getHeight()/2);


        canvas.scale(-1.0f,-1.0f);

    }
}
