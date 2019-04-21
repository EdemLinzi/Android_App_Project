package com.example.a2playerstankgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

public class GameView extends View {

    Drawable tank1;
    Drawable tank2;
    Bitmap map ;

    public GameView(Context context,Drawable tank1,Drawable tank2,int own) {
        super(context);
        if(own == 0) {
            this.tank1 = tank1;
            this.tank2 = tank2;
        }
        if(own == 1){
            this.tank1 = tank2;
            this.tank2 = tank1;
        }
        this.map = BitmapFactory.decodeResource(getResources(),R.drawable.map);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float canvasWidth = getWidth();
        float canvasHeight = getHeight();

        float tankPosX = canvasWidth/2;
        float tankPosY = canvasHeight/2;

       // Log.i("GameView","Xpos "+tank1.getXpos()+" Ypos "+tank1.getYpos());


        if(tank1.getXpos()<0){tank1.setPos(0,tank1.getYpos());}
        if(tank1.getXpos()>map.getWidth()){tank1.setPos(map.getWidth(),tank1.getYpos());}
        if(tank1.getYpos()<0){tank1.setPos(tank1.getXpos(),0);}
        if(tank1.getYpos()>map.getHeight()){tank1.setPos(tank1.getXpos(),map.getHeight());}


        canvas.drawBitmap(map,tankPosX-tank1.getXpos(),tankPosY-tank1.getYpos(),null);


        //Log.i("GameView","Xpos "+tank1.getXpos()+" Xpos "+tank1.getYpos());

        canvas.scale(0.2f,0.2f);
        //Log.i("GameView","HeightScale2 "+canvasHeight+" WidhtScale2 "+canvasWidth);
        canvas.drawBitmap(tank1.getTankBitmap(),canvasWidth*2.5f-tank1.getTankBitmap().getWidth()/2,canvasHeight*2.5f-tank1.getTankBitmap().getHeight()/2,null);
        //Log.i("GameView","HeightScale1 "+(canvasWidth*2.5f-tank1.getTankBitmap().getWidth()/2)+" WidhtScale1 "+(canvasHeight*2.5f-tank1.getTankBitmap().getHeight()/2));

        float tank2PosX =tankPosX*5-tank1.getXpos()*5+tank2.getXpos()*5-tank2.getTankBitmap().getWidth()/2;
        float tank2PosY =tankPosY*5-tank1.getYpos()*5+tank2.getYpos()*5-tank2.getTankBitmap().getHeight()/2;


        //Log.i("GameView","Xpos2 "+tank2.getXpos()+" Ypos2 "+tank2.getYpos());

        canvas.drawBitmap(tank2.getTankBitmap(),tank2PosX,tank2PosY,null);

        canvas.scale(-1.0f,-1.0f);

    }
}
