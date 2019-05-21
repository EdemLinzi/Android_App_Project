package com.example.a2playerstankgame;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_DEVICE_NAME;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_READ;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_STATE_CHANGE;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_WRITE;
import static java.lang.Float.parseFloat;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class GamePlace extends AppCompatActivity {

    //TextView textView;
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;


    SetUpBluetooth setUpBluetooth;

    MyReciver myReciver;

    GameView gameView;
    Drawable[] tanks = new Drawable[3];

    Handler handler;


    Integer move = 0;
    Integer moveOwn = 0;
    int own;
    int other;


    private float previousX = 0;
    private float previousY = 0;

    Point size ;
    Display display;
    int width = 0;
    int height = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:

                    float dx = x - previousX;
                    float dy = y - previousY;

                    // reverse direction of rotation above the mid-line
                    if(x>width/4 && x< width/2+width/4 ) {
                        if (y > height / 2) {
                            dx = dx * -1;
                        }

                        // reverse direction of rotation to left of the mid-line
                        if (x < width / 2) {
                            dy = dy * -1;
                        }

                        tanks[own].setHullAngle(tanks[own].getHullAngle() + ((dx + dy) * TOUCH_SCALE_FACTOR));
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    if(x > width/2+width/4 && height/2<y) {
                        moveOwn = 1;
                    }
                    if(x < width/4){
                        moveOwn = -1;
                    }
                    if(x > width/2+width/4 && height/2>y ){
                        Log.i("GameView","Shoot");

                        gameView.addBullet(new Bullet(tanks[own].getXpos(),tanks[own].getYpos(),tanks[own].getHullAngle(),true));
                        setUpBluetooth.sendMessage("Shoot");
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    moveOwn = 0;
                    break;
            }

            previousX = x;
            previousY = y;
            return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("GamePlace","Is create");

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(R.layout.activity_gyro_test);
        handler =  new Handler();

        tanks[0] = new Drawable(150,1000, BitmapFactory.decodeResource(getResources(),R.drawable.tank1));
        tanks[1] = new Drawable(1500,500, BitmapFactory.decodeResource(getResources(),R.drawable.tank2));

        Intent intent = getIntent();
        int tmp = intent.getIntExtra("First",0);
        if(tmp == 1){ own = 0; other = 1;}
        if(tmp == -1){ own = 1; other = 0; }


        display = getWindowManager().getDefaultDisplay();
        size = new Point();

        display.getSize(size);
        width = size.x;
        height = size.y;

        gameView = new GameView(this,tanks[0],tanks[1],own);
        setContentView(gameView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        setUpBluetooth = MainActivity.bluetoothConnection;

        new Thread(new Runnable() {
            @Override
            public void run() {
                sensorEventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        float[] rotationMatrix = new float[16];
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

                        // Remap coordinate system
                        float[] remappedRotationMatrix = new float[16];
                        SensorManager.remapCoordinateSystem(rotationMatrix,
                                SensorManager.AXIS_X,
                                SensorManager.AXIS_Z,
                                remappedRotationMatrix);

                        // Convert to orientations
                        float[] orientations = new float[3];
                        SensorManager.getOrientation(remappedRotationMatrix, orientations);

                        for(int i = 0; i < 3; i++) {
                            orientations[i] = (float)(Math.toDegrees(orientations[i]));
                        }
                        //Log.i("GamePlace","Angle "+tanks[own].getAngle());
                        if(orientations[2] > -110 && orientations[2] < -70) {
                            tanks[own].incrAngle(0);
                        }
                        else if(orientations[2] > -70) {
                            tanks[own].incrAngle(5);
                        }
                        else if(orientations[2] < -110) {
                            tanks[own].incrAngle(-5);
                        }

                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
            }
        }).start();


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        switch (moveOwn) {
                            case -1:
                                //TODO hátrameneten még dolgozni kell
                                if (gameView.canIMove(0, -5)) {
                                tanks[own].move(0, -5,tanks[own].getAngle());
                                }else moveOwn = 0;
                                break;
                            case 1:

                                if (gameView.canIMove(0, 5)){
                                    tanks[own].move(0, 5,tanks[own].getAngle());
                                    //tanks[own].move2(xpos, -ypos);
                                }else moveOwn = 0;
                                break;

                        }
                        gameView.invalidate();
                        gameView.moveBullets();
                        gameView.hit();

                        /*synchronized (this) {
                            if (setUpBluetooth.getState() != 0) {
                                //Log.i("GamePlace","Pos " + tanks[own].getXpos() + " " + tanks[own].getYpos()+" "+ tanks[own].getAngle());
                                setUpBluetooth.sendMessage("Pos " + tanks[own].getXpos() + " " + tanks[own].getYpos()+" " + tanks[own].getAngle()+" "+tanks[own].getHullAngle());
                                //sended = false;
                            }
                        }*/
                    }
                });
            }
        },30,30);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(setUpBluetooth.getState() != 0){
                    setUpBluetooth.sendMessage("Pos " + tanks[own].getXpos() + " " + tanks[own].getYpos()+" " + tanks[own].getAngle()+" "+tanks[own].getHullAngle()+" ");
                    try {
                        Thread.sleep(90);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        myReciver = new MyReciver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.a2playerstankgame.MainActivity");
        registerReceiver(myReciver,intentFilter);
        sensorManager.registerListener(sensorEventListener,sensor,SensorManager.SENSOR_DELAY_NORMAL);


    }



    @Override
    protected void onPause() {
        super.onPause();
        Log.i("GamePlace","onPause");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("GamePlace","onDestroy");
        unregisterReceiver(myReciver);
        finish();
    }

    class MyReciver extends BroadcastReceiver{
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            //Log.i("GamePlace","Incoming intent");
            String str = intent.getStringExtra("State");
            //Log.i("GamePlace","Incoming intent "+str);
            if(intent.getBooleanExtra("Shoot",false)){
                gameView.addBullet(new Bullet(tanks[other].getXpos(),tanks[other].getYpos(),tanks[other].getHullAngle(),false));
            }else if(intent.getStringExtra("Pos").equals("Position")) {
                float xpos = intent.getFloatExtra("Xpos", 0.0f);
                float ypos = intent.getFloatExtra("Ypos", 0.0f);
                float angle = intent.getFloatExtra("Angle", 0.0f);
                float hullAngle = intent.getFloatExtra("HullAngle", 0.0f);
                //Log.i("GamePlace","Incoming position & angle" + xpos +" "+ypos+" "+angle);

                tanks[other].setPos(xpos,ypos);
                tanks[other].setAngle(angle);
                tanks[other].setHullAngle(hullAngle);
            }

            if (intent.getBooleanExtra("Arrived",false)){
               // sended = true;
            }

            }
    }



}
