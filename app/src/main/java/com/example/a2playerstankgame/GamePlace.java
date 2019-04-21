package com.example.a2playerstankgame;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_DEVICE_NAME;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_READ;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_STATE_CHANGE;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_WRITE;
import static java.lang.Float.parseFloat;

public class GamePlace extends AppCompatActivity {

    //TextView textView;
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;

    SetUpBluetooth setUpBluetooth;

    MyReciver myReciver;

    GameView gameView;
    Drawable[] tanks = new Drawable[2];

    Handler handler;

    Integer move = 0;
    Integer moveOwn = 0;
    int own;
    int other;

    boolean sensorChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("GamePlace","Is create");
        //setContentView(R.layout.activity_gyro_test);
        handler =  new Handler();

        tanks[0] = new Drawable(0,0, BitmapFactory.decodeResource(getResources(),R.drawable.tank1));
        tanks[1] = new Drawable(0,0, BitmapFactory.decodeResource(getResources(),R.drawable.tank2));

        Intent intent = getIntent();
        int tmp = intent.getIntExtra("First",0);
        if(tmp == 1){ own = 0; other = 1;}
        if(tmp == -1){ own = 1; other = 0; }

        gameView = new GameView(this,tanks[0],tanks[1],own);
        setContentView(gameView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        setUpBluetooth = MainActivity.bluetoothConnection;

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                    if(event.values[1] > 1.0f) {
                       // Log.i("GamePlace","message was sended (up)1");
                       // setUpBluetooth.sendMessage("State up");
                        //Log.i("GamePlace","message was sended (up)2");

                        if(moveOwn == 0)moveOwn = 1;
                        else moveOwn = 0;
                        sensorChanged = true;

                    } else if(event.values[1] < -1.0f) {
                        //setUpBluetooth.sendMessage("State down");
                        //Log.i("GamePlace","message was sended (down)");

                        if(moveOwn == 0)moveOwn = -1;
                        else moveOwn = 0;

                        sensorChanged = true;

                        //getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
                    } else if(event.values[0] > 1.0f){
                        //setUpBluetooth.sendMessage("State right");
                        //Log.i("GamePlace","message was sended (right)");

                        if(moveOwn == 0) moveOwn = 2;
                        else moveOwn = 0;

                        sensorChanged = true;

                        // getWindow().getDecorView().setBackgroundColor(Color.YELLOW);

                    } else if(event.values[0] < -1.0f){
                        //setUpBluetooth.sendMessage("State left");
                        //Log.i("GamePlace","message was sended (left)");

                        if(moveOwn == 0) moveOwn = 3;
                        else  moveOwn = 0;

                        sensorChanged = true;

                        //getWindow().getDecorView().setBackgroundColor(Color.GREEN);
                    }
                    if(sensorChanged){
                        //Log.i("GamePlace","Sensor is changed");

                        sensorChanged = false;
                    }

                }


            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        /*switch (move){
                            case -1:
                                tanks[other].move(0,10);
                                break;
                            case 1:
                                tanks[other].move(0,-10);
                                break;
                            case 2:
                                tanks[other].move(10,0);
                                break;
                            case 3:
                                tanks[other].move(-10,0);
                                break;
                        }*/
                        switch (moveOwn){
                            case -1:
                                tanks[own].move(0,10);
                                break;
                            case 1:
                                tanks[own].move(0,-10);
                                break;
                            case 2:
                                tanks[own].move(10,0);
                                break;
                            case 3:
                                tanks[own].move(-10,0);
                                break;
                        }
                        gameView.invalidate();
                        synchronized (GamePlace.this) {
                            if(setUpBluetooth.getState()!=0){
                            //Log.i("GamePlace","Pos " + tanks[own].getXpos() + " " + tanks[own].getYpos());
                                setUpBluetooth.sendMessage("Pos " + tanks[own].getXpos() + " " + tanks[own].getYpos());
                            }
                        }
                    }
                });
            }
        },0,30);

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
    }

    class MyReciver extends BroadcastReceiver{
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            //Log.i("GamePlace","Incoming intent");
            String str = intent.getStringExtra("State");
            //Log.i("GamePlace","Incoming intent "+str);
            if(str!=null){
                if(str.equals("up")){
                    if(move == 0)move = 1;
                    else move = 0;
                }
                if(str.equals("down")){
                    if(move == 0)move = -1;
                    else move = 0;
                }
                if(str.equals("right")){
                    if(move == 0)move = 2;
                    else move = 0;
                }
                if(str.equals("left")){
                    if(move == 0)move = 3;
                    else move = 0;
                }
            }
            if(intent.getStringExtra("Pos").equals("Position")) {
                float xpos = intent.getFloatExtra("Xpos", 0.0f);
                float ypos = intent.getFloatExtra("Ypos", 0.0f);
                Log.i("GamePlace","Incoming position " + xpos +" "+ypos);

                tanks[other].setPos(xpos,ypos);
            }

            }
    }



}
