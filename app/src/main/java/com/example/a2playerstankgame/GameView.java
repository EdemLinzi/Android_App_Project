package com.example.a2playerstankgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class GameView extends View {

    Drawable tank1;
    Drawable tankhull;
    Drawable tank2;
    Bitmap map ;
    Bitmap bulletImage ;

    ArrayList<PointF> imagePoins;
    ArrayList<Bullet> bullets;



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
        imagePoins = new ArrayList<>();
        this.tankhull = new Drawable();
        tankhull.setTankBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.tankhull));
        bulletImage = BitmapFactory.decodeResource(getResources(),R.drawable.bullet);
        this.map = BitmapFactory.decodeResource(getResources(),R.drawable.map);
        bullets = new ArrayList<>();


    }

    public void addBullet(Bullet bullet){
        bullet.setBulletBitmap(bulletImage);
        bullets.add(bullet);
        Log.i("GameView","Bullet position x= "+bullet.getXpos()+" y= "+bullet.getYpos()+" own "+bullet.getOwn());
    }

    public synchronized void moveBullets(){
        float[] bulletImagePoints={
                0,-bulletImage.getHeight()/10,
                0,+bulletImage.getHeight()/10,

                bulletImage.getWidth()/10,0,
                -bulletImage.getWidth()/10,0,
        };

        for(int i = 0;i<bullets.size();i++){
            bullets.get(i).moveBullet();
                //Log.i("GameView", "Bullet position x= " + bullets.get(i).getXpos() + " y= " + bullets.get(i).getYpos() + " own " + bullets.get(i).getOwn());
                if (bullets.get(i).getCount()>=3 || bullets.get(i).getXpos()-bulletImagePoints[4] < 0 || bullets.get(i).getXpos()+bulletImagePoints[4] > map.getWidth() || bullets.get(i).getYpos()-bulletImagePoints[3] < 0 || (bullets.get(i).getYpos()+bulletImagePoints[3]) > map.getHeight()) {
                    bullets.get(i).setIsAlive(false);
                    bullets.remove(i);
                    Log.i("GameView", "Bullet i " + i + " torolve Bullets.size " + bullets.size());
                }
        }
        for(int i = 0;i<bullets.size();i++){
            for(int j = 0;j<bulletImagePoints.length;j+=2){
                if(map.getPixel((int)(bullets.get(i).getXpos()-bulletImagePoints[j]),(int)(bullets.get(i).getYpos()-bulletImagePoints[j+1]))==Color.BLACK){
                    if(j>3){
                        Log.i("GameView", "Bullet angle3 " + bullets.get(i).getAngle());
                        float tmpang = 360 - (bullets.get(i).getAngle());
                        bullets.get(i).setAngle(tmpang);

                    }
                    if(j<=3) {
                        Log.i("GameView", "Bullet angle1 " + bullets.get(i).getAngle());

                        float tmpang = 180 - bullets.get(i).getAngle();
                        bullets.get(i).setAngle(tmpang);
                    }
                    bullets.get(i).incrCount();
                }
            }
        }
    }



    private PointF rotatePoint(PointF point,float deg){
        PointF tmp = new PointF();

                tmp.x =(float)(point.x*cos(deg)-point.y*sin(deg));
                tmp.y =(float)(point.y*cos(deg)+point.x*sin(deg));

        return tmp;
    }

    private synchronized ArrayList<PointF> calcPoints(float xpos, float ypos, float deg){
        ArrayList<PointF> arrayPoints = new ArrayList<>();
        PointF tanksImagePoints = new PointF();
        //BF
        tanksImagePoints.set(-tank1.getTankBitmap().getWidth()/10,-tank1.getTankBitmap().getHeight()/10);
        arrayPoints.add(rotatePoint(tanksImagePoints,deg));
        //JF
        tanksImagePoints.set(tank1.getTankBitmap().getWidth()/10,-tank1.getTankBitmap().getHeight()/10);
        arrayPoints.add(rotatePoint(tanksImagePoints,deg));
        //JA
        tanksImagePoints.set(tank1.getTankBitmap().getWidth()/10,tank1.getTankBitmap().getHeight()/10);
        arrayPoints.add(rotatePoint(tanksImagePoints,deg));
        //BA
        tanksImagePoints.set(-tank1.getTankBitmap().getWidth()/10,tank1.getTankBitmap().getHeight()/10);
        arrayPoints.add(rotatePoint(tanksImagePoints,deg));


        return arrayPoints;
    }

    public boolean canIMove(float px,float py){
        float x1,y1,x2,y2 ;
        x1=x2=y1=y2=0;
        float[] tankImageSize = {tank1.getTankBitmap().getWidth()/10,tank1.getTankBitmap().getHeight()/10};

        int elore = 0;
        if(py<0){ elore = 1; }
        if(py>0){ elore = -1; }

        x1 += (-tankImageSize[0])*cos(tank1.getAngle())-(+elore*tankImageSize[1])*sin(tank1.getAngle());
        y1 += (+elore*tankImageSize[1])*cos(tank1.getAngle())+(-tankImageSize[0])*sin(tank1.getAngle());

        x2 += (+tankImageSize[0])*cos(tank1.getAngle())-(+elore*tankImageSize[1])*sin(tank1.getAngle());
        y2 += (+elore*tankImageSize[1])*cos(tank1.getAngle())+(+tankImageSize[0])*sin(tank1.getAngle());

        imagePoins = calcPoints(tank1.getXpos(),tank1.getYpos(),tank1.getAngle());
        for(int i = 0;i<imagePoins.size();i++){
            //Log.i("Gameview","ImagePoint "+i+" pos "+imagePoins.get(i).x+" "+imagePoins.get(i).y);
            if((imagePoins.get(i).x+tank1.getXpos()) <= 0){ tank1.setPos(Math.abs(imagePoins.get(i).x),tank1.getYpos()); }
            if(imagePoins.get(i).y+tank1.getYpos() <= 0 ){ tank1.setPos(tank1.getXpos(),Math.abs(imagePoins.get(i).y)); }
            if(imagePoins.get(i).x+tank1.getXpos() >=  map.getWidth() ){ tank1.setPos(map.getWidth()-Math.abs(imagePoins.get(i).x)-1,tank1.getYpos()); }
            if(imagePoins.get(i).y+tank1.getYpos() >=  map.getHeight() ){ tank1.setPos(tank1.getXpos(), map.getHeight()-Math.abs(imagePoins.get(i).y)-1);}
        }


        float m = (y1 - y2)/(x2 - x1);
        /*Log.i("GameView","M= "+m);
        Log.i("GameView","x1= "+x1+" x2="+x2);
        Log.i("GameView","xpos= "+tank1.getXpos()+" ypos="+tank1.getYpos());
        Log.i("GameView","y1= "+(y1)+" y2="+(y2));
        Log.i("GameView","y1+y= "+(y1+tank1.getYpos())+" y2+y="+(y2+tank1.getYpos()));
        Log.i("GameView","x2-x1 "+(x2-x1));
        Log.i("GameView","y2-y1 "+(y1-y2));
        Log.i("GameView","mapWidth "+map.getWidth()+" mapHeight "+map.getHeight());*/


       /* for(int i = 0;i<imagePoins.size();i++){
            Log.i("Gameview","ImagePoint "+i+" pos "+imagePoins.get(i).x+" "+imagePoins.get(i).y);
        }*/

        float ford = 0;
        if(tank1.getAngle()<90 || tank1.getAngle()>270){
            ford = 1;
        }else if(tank1.getAngle()>90 && tank1.getAngle()<270){
            ford = -1;
        }
        //Log.i("GameView","ford "+ford);
        //Log.i("GameView","elore "+elore);

        for(int i=0;i<Math.abs(x2-x1);i++){
            //Log.i("GameView", "X sarok=" + (int)(x1+ford*i+tank1.getXpos()) + " Y magassag=" + (int)(-(ford*i)*m+tank1.getYpos()+y1));
            if (map.getPixel((int)(x1+ford*i+tank1.getXpos()),(int)(-(ford*i)*m+tank1.getYpos()+y1)) == Color.BLACK) {
                    return false;
                }
        }
        if(tank1.getAngle() == 90 || tank1.getAngle() == 270){
            for(int i = 0;i<Math.abs(y1-y2);i++){
                if (map.getPixel((int)(x1+tank1.getXpos()),(int)(-(ford*i)+tank1.getYpos()+y1)) == Color.BLACK) {
                    //Log.i("GameView", "X sarok=" + (x1+i+tank1.getXpos()) + " Y magassag=" + (-(i)*m+tank1.getYpos()+y1));
                    return false;
                }
            }
        }


        return true;
    }

    public static final double cos(double angle){
        return Math.cos(Math.toRadians(angle));
    }

    public static final double sin(double angle){
        return Math.sin(Math.toRadians(angle));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float canvasHeight = getHeight();
        float canvasWidth = getWidth();
        float tankPosX = canvasWidth/2;
        float tankPosY = canvasHeight/2;

       // Log.i("GameView","Xpos "+tank1.getXpos()+" Ypos "+tank1.getYpos());

        /*float[] tankImageSize = {tank1.getTankBitmap().getWidth()/10,tank1.getTankBitmap().getHeight()/10};
        if(tank1.getXpos() - tankImageSize[0] < 0 ){tank1.setPos(tankImageSize[0],tank1.getYpos());}
        if(tank1.getXpos() + tankImageSize[0] > map.getWidth()){tank1.setPos(map.getWidth()-tankImageSize[0],tank1.getYpos());}
        if(tank1.getYpos() - tankImageSize[1] < 0){tank1.setPos(tank1.getXpos(),tankImageSize[1]);}
        if(tank1.getYpos() + tankImageSize[1] > map.getHeight()){tank1.setPos(tank1.getXpos(),map.getHeight()-tankImageSize[1]);}
*/


        imagePoins = calcPoints(tank1.getXpos(),tank1.getYpos(),tank1.getAngle());
        for(int i = 0;i<imagePoins.size();i++){
            //Log.i("Gameview","ImagePoint "+i+" pos "+imagePoins.get(i).x+" "+imagePoins.get(i).y);
            if((imagePoins.get(i).x+tank1.getXpos()) < 0){ tank1.setPos(Math.abs(imagePoins.get(i).x),tank1.getYpos()); }
            if(imagePoins.get(i).y+tank1.getYpos() < 0 ){ tank1.setPos(tank1.getXpos(),Math.abs(imagePoins.get(i).y)); }
            if(imagePoins.get(i).x+tank1.getXpos() >  map.getWidth() ){ tank1.setPos(map.getWidth()-Math.abs(imagePoins.get(i).x),tank1.getYpos()); }
            if(imagePoins.get(i).y+tank1.getYpos() >  map.getHeight() ){ tank1.setPos(tank1.getXpos(), map.getHeight()-Math.abs(imagePoins.get(i).y));}
        }




        canvas.drawBitmap(map,tankPosX-tank1.getXpos(),tankPosY-tank1.getYpos(),null);



        //Log.i("GameView","Xpos "+tank1.getXpos()+" Xpos "+tank1.getYpos());



        canvas.scale(0.2f,0.2f);

        for(Bullet b : bullets) {
            canvas.drawBitmap(b.getBulletBitmap(), tankPosX * 5 - tank1.getXpos() * 5 + b.getXpos() * 5 -bulletImage.getWidth()/2, tankPosY * 5 - tank1.getYpos() * 5 + b.getYpos() * 5 - bulletImage.getHeight()/2, null);
        }

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
        float tank2HullPosX = tankPosX*5-tank1.getXpos()*5+tank2.getXpos()*5-tankhull.getTankBitmap().getWidth()/2;
        float tank2HullPosY = tankPosY*5-tank1.getYpos()*5+tank2.getYpos()*5-tankhull.getTankBitmap().getHeight()/2;

        //Log.i("GameView","Xpos2 "+tank2.getXpos()+" Ypos2 "+tank2.getYpos());
        canvas.rotate(tank2.getAngle(),tank2PosX+tank2.getTankBitmap().getWidth()/2,tank2PosY+tank2.getTankBitmap().getHeight()/2);
        canvas.drawBitmap(tank2.getTankBitmap(),tank2PosX,tank2PosY,null);
        canvas.rotate(-tank2.getAngle(),tank2PosX+tank2.getTankBitmap().getWidth()/2,tank2PosY+tank2.getTankBitmap().getHeight()/2);


        canvas.rotate(tank2.getHullAngle(),tank2PosX+tank2.getTankBitmap().getWidth()/2,tank2PosY+tank2.getTankBitmap().getHeight()/2);
        canvas.drawBitmap(tankhull.getTankBitmap(),tank2HullPosX,tank2HullPosY,null);
        canvas.rotate(-tank2.getHullAngle(),tank2PosX+tank2.getTankBitmap().getWidth()/2,tank2PosY+tank2.getTankBitmap().getHeight()/2);


        canvas.scale(-1.0f,-1.0f);

    }
}
