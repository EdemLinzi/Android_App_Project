package com.example.a2playerstankgame;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
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

import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_DEVICE_NAME;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_READ;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_STATE_CHANGE;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_WRITE;
import static java.lang.Float.parseFloat;

public class GyroTest extends AppCompatActivity {

    TextView textView;
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;

    SetUpBluetooth setUpBluetooth;

    Handler mHandler;

    public GyroTest(Handler handler){
        this.mHandler = handler;
    }

    public GyroTest(){}

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyro_test);

        textView = findViewById(R.id.gyrotest_text_view);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        setUpBluetooth = MainActivity.bluetoothConnection;

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(event.values[1] > 0.5f) { // anticlockwise
                    getWindow().getDecorView().setBackgroundColor(Color.BLUE);
                    setUpBluetooth.sendMessage("State "+Float.toString(event.values[1]));

                } else if(event.values[1] < -0.5f) { // clockwise
                    setUpBluetooth.sendMessage("State "+Float.toString(event.values[1]));

                    getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(sensorEventListener,sensor,SensorManager.SENSOR_DELAY_NORMAL);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:
                        switch (msg.arg1){
                            case MyBluetoothService.STATE_CONNECTED:
                                Toast.makeText(GyroTest.this,"Connected",Toast.LENGTH_LONG).show();

                                break;
                            case MyBluetoothService.STATE_CONNECTING:
                                Toast.makeText(GyroTest.this,"Connecting",Toast.LENGTH_LONG).show();
                                break;
                            case MyBluetoothService.STATE_LISTEN:
                            case MyBluetoothService.STATE_NONE:
                                Toast.makeText(GyroTest.this,"Not connected",Toast.LENGTH_LONG).show();
                                break;
                        }
                        break;
                    case MESSAGE_WRITE:
                        byte[] writeBuf = (byte[]) msg.obj;

                        break;
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        String readMessage = new String(readBuf,0,msg.arg1);
                        Log.i("Main","message_read" + readMessage);
                        if(readMessage.equals("Ready")){
                            Toast.makeText(GyroTest.this,"The other player is "+readMessage,Toast.LENGTH_LONG).show();
                        }
                        String[] str = readMessage.split(" ");
                        if(str[0].equals("State")){
                            Log.i("GyroTest",str[0]+"-->"+str[1]);

                            Float tmp = Float.parseFloat(str[1]);
                            if(tmp>0.5){
                                getWindow().getDecorView().setBackgroundColor(Color.GREEN);
                            }
                            if(tmp<-0.5){
                                getWindow().getDecorView().setBackgroundColor(Color.MAGENTA);
                            }
                        } break;
                    case MESSAGE_DEVICE_NAME:
                        break;
                }

            }
        };
    }





}
