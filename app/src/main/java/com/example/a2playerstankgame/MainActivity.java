package com.example.a2playerstankgame;

import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_DEVICE_NAME;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_READ;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_STATE_CHANGE;
import static com.example.a2playerstankgame.SetUpBluetooth.MESSAGE_WRITE;

public class MainActivity extends AppCompatActivity {

    Button btnNewGame;
    Button btnConnection;
    Button btnExitGame;
    public static SetUpBluetooth bluetoothConnection;
    GyroTest gyroTest;
    MyBluetoothService myBluetoothService;

    public static final int CONNECT_TO_DEVICE=111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNewGame = findViewById(R.id.btn_new_game);
        btnConnection = findViewById(R.id.btn_conncetion);
        btnExitGame = findViewById(R.id.btn_exit_game);
        myBluetoothService = new MyBluetoothService(this,mHandler);
        gyroTest = new GyroTest(mHandler);
        bluetoothConnection = new SetUpBluetooth(myBluetoothService,MainActivity.this);
        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothConnection.sendMessage("Ready");
                btnNewGame.setBackgroundColor(Color.RED);
                btnNewGame.setText("Ready");
                Intent intent  =  new Intent(MainActivity.this,GyroTest.class);
                startActivity(intent);
            }
        });
        myBluetoothService.start();

    }

    public void connection(View v){
        startActivityForResult(new Intent(this,SetUpBluetooth.class),0);
    }

    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1){
                        case MyBluetoothService.STATE_CONNECTED:
                            Toast.makeText(MainActivity.this,"Connected",Toast.LENGTH_LONG).show();
                            //unregisterReceiver(receiver);
                            //deviceDetails.clear();
                            break;
                        case MyBluetoothService.STATE_CONNECTING:
                            Toast.makeText(MainActivity.this,"Connecting",Toast.LENGTH_LONG).show();
                            break;
                        case MyBluetoothService.STATE_LISTEN:
                        case MyBluetoothService.STATE_NONE:
                            Toast.makeText(MainActivity.this,"Not connected",Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this,"The other player is "+readMessage,Toast.LENGTH_LONG).show();
                    }
                    String[] str = readMessage.split(" ");
                    if(str[0].equals("State")){
                        Log.i("MainActivity",str[0]+"-->"+str[1]);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
            if (resultCode == CONNECT_TO_DEVICE) {
                Log.i("MainActivity", "Connecting");
                BluetoothDevice device = data.getParcelableExtra("connectTo");
                if(device != null)myBluetoothService.connect(device, true);
            }
        }
    }
}
