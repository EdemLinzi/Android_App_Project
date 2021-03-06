package com.example.a2playerstankgame;

import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_DEVICE_NAME;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_READ;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_STATE_CHANGE;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_WRITE;

public class MainActivity extends AppCompatActivity {

    Button btnNewGame;
    Button btnConnection;
    Button btnExitGame;
    TextView textView;
    public static SetUpBluetooth bluetoothConnection;
    public static MyBluetoothService myBluetoothService;

    Intent intent;

     int first = 0;

    public static final int CONNECT_TO_DEVICE=111;
    public static final int GAME_OVER=100;

    boolean ready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        first = 0;

        btnNewGame = findViewById(R.id.btn_new_game);
        btnConnection = findViewById(R.id.btn_conncetion);
        btnExitGame = findViewById(R.id.btn_exit_game);
        textView = findViewById(R.id.textView);
        myBluetoothService = new MyBluetoothService(mHandler);
        bluetoothConnection = new SetUpBluetooth(myBluetoothService,MainActivity.this);

        intent  =  new Intent(MainActivity.this,GamePlace.class);

        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent  =  new Intent(MainActivity.this,GamePlace.class);

                if(first!=-1)first = 1;
                Log.i("MainActivity","First? "+first);
                intent.putExtra("First",first);
                //startActivity(intent);

                if(ready) {
                    bluetoothConnection.sendMessage("Start");
                    startActivityForResult(intent,1);
                }
                else{
                    bluetoothConnection.sendMessage("Ready");
                    btnNewGame.setBackgroundColor(Color.RED);
                    btnNewGame.setText("Ready");
                }
            }
        });

    }

    public void connection(View v){
        startActivityForResult(new Intent(this,SetUpBluetooth.class),0);
    }

    public void exitGame(View v){
        finish();
    }

    @SuppressLint("HandlerLeak")
    public final Handler mHandler = new Handler() {
        @Override
        public  void  handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case MyBluetoothService.STATE_CONNECTED:
                                Log.i("MainActivity", "Connected");
                                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();
                                break;
                            case MyBluetoothService.STATE_CONNECTING:
                                Log.i("MainActivity", "Connecting");
                                Toast.makeText(MainActivity.this, "Connecting", Toast.LENGTH_LONG).show();
                                break;
                            case MyBluetoothService.STATE_LISTEN:
                                Toast.makeText(MainActivity.this, "Listening", Toast.LENGTH_LONG).show();
                                break;
                            case MyBluetoothService.STATE_NONE:
                                Log.i("MainActivity", "Not connected");
                                Toast.makeText(MainActivity.this, "Not connected", Toast.LENGTH_LONG).show();
                                break;
                        }
                        break;
                    case MESSAGE_WRITE:
                        break;
                    case MESSAGE_READ:
                            byte[] readBuf = (byte[]) msg.obj;
                            String readMessage = new String(readBuf, 0, msg.arg1);
                            //Log.i("MainActivity","message_read" + readMessage);
                            if (readMessage.equals("Ready")) {
                                Toast.makeText(MainActivity.this, "The other player is " + readMessage, Toast.LENGTH_LONG).show();
                                btnNewGame.setText("Start");
                                btnNewGame.setBackgroundColor(Color.GREEN);
                                if (first != 1) first = -1;
                                ready = true;
                            }
                            if (readMessage.equals("Start")) {
                                Toast.makeText(MainActivity.this, "Start", Toast.LENGTH_LONG).show();
                                startActivityForResult(intent,1);

                            }
                            if(readMessage.equals("Shoot")){
                                Intent intentGyro = new Intent();
                                intentGyro.putExtra("Shoot",true);
                                intentGyro.setAction("com.example.a2playerstankgame.MainActivity");
                                sendBroadcast(intentGyro);

                            }

                            String[] str = readMessage.split(" ");

                            //Log.i("MainActivity", "" + readMessage);
                            if (str[0].equals("Pos") && str.length == 5) {
                                Intent intentGyro = new Intent();
                                //intentGyro.putExtra("Arrived",true);
                                intentGyro.putExtra("Pos", "Position");
                                intentGyro.putExtra("Xpos", Float.parseFloat(str[1]));
                                intentGyro.putExtra("Ypos", Float.parseFloat(str[2]));
                                intentGyro.putExtra("Angle", Float.parseFloat(str[3]));
                                intentGyro.putExtra("HullAngle", Float.parseFloat(str[4]));
                                intentGyro.setAction("com.example.a2playerstankgame.MainActivity");
                                sendBroadcast(intentGyro);
                                //Log.i("MainActivity","Position was sended ("+Float.parseFloat(str[1])+" "+Float.parseFloat(str[2]));
                            }

                        break;

                    case MESSAGE_DEVICE_NAME:
                        break;
                }

            }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
            if (resultCode == CONNECT_TO_DEVICE) {
                Log.i("MainActivity", "Connecting");
                BluetoothDevice device = data.getParcelableExtra("connectTo");
                if(device != null)myBluetoothService.connect(device, true);
            }
            if( resultCode == SetUpBluetooth.LISTENING){
                myBluetoothService.start();
            }
        }
        if(requestCode == 1) {
            if (resultCode == GAME_OVER) {
                if (data.getIntExtra("Winner",-1) == 1) {
                    textView.setText("You win");
                    Log.i("MainActivity", "You win");

                }

                if (data.getIntExtra("Winner",-1) == 2) {
                    textView.setText("The other player win");
                    Log.i("MainActivity", "The other player win");

                }

                btnNewGame.setText("New Game");
                btnNewGame.setBackgroundColor(Color.LTGRAY);
                ready = false;
                first = 0;
                Log.i("MainActivity", "GameOver");

            }
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        Log.i("MainActivity", "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("MainActivity", "onDestroy");

        myBluetoothService.stop();

    }
}
