package com.example.a2playerstankgame;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class SetUpBluetooth extends AppCompatActivity {

    Button btnBluetooth;
    Button btnDiscoverable;
    Button btnShowDevices;
    Button btnSend;
    EditText editText;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final String DEVICE_NAME = "Name: ";
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;

    ListView listViewDevices;
    ArrayAdapter adapter ;

    MyBluetoothService myBluetoothService;

    BluetoothAdapter bluetoothAdapter;
    public static final Integer REQUEST_ENABLE_BT = 1;
    public static final Integer DISCOVERABLE_DURATION= 300;


    ArrayList<String> deviceDetails;
    ArrayList<BluetoothDevice> devices;


    @Override
    protected void onResume() {
        super.onResume();
        if(myBluetoothService != null){
            if(myBluetoothService.getState() == MyBluetoothService.STATE_NONE && bluetoothAdapter.isEnabled()){
                myBluetoothService.start();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_bluetooth);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        deviceDetails = new ArrayList<>();
        devices = new ArrayList<>();
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,deviceDetails);

        btnBluetooth = findViewById(R.id.btn_setup_enable_bluetooth);
        btnDiscoverable = findViewById(R.id.btn_setup_enable_discover);
        btnShowDevices = findViewById(R.id.btn_setup_show_devices);
        btnSend = findViewById(R.id.btn_setup_send);
        editText = findViewById(R.id.edit_text_setup);

        listViewDevices = findViewById(R.id.list_view_setup_devices);

        listViewDevices.setAdapter(adapter);

        listViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("SetUpBluetooth","Connect to "+devices.get(position).getName());
                myBluetoothService.connect(devices.get(position),true);
            }
        });

        if(bluetoothAdapter == null){
            Toast.makeText(this,"Sorry your device dosen't support Bluetooth",Toast.LENGTH_LONG).show();
        }

        myBluetoothService = new MyBluetoothService(getApplicationContext(),mHandler);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,filter);

    }

    public void enableBluetooth(View v){
        if(!bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
        }
        else {
            Toast.makeText(this,"Your Bluetooth is alredy enabled",Toast.LENGTH_LONG).show();
        }
    }

    public void enableDiscoverability(View v){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,DISCOVERABLE_DURATION);
        startActivity(discoverableIntent);
    }


    public void showDevices(View v){
        Log.i("SetUpBluetooth","showDevices");
        deviceDetails.clear();
        devices.clear();
        if(!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
        }
    }

    public void sendMessageButton(View v){

        if(myBluetoothService.getState() != MyBluetoothService.STATE_CONNECTED){
            Toast.makeText(this,"not connected device",Toast.LENGTH_LONG).show();
            return;
        }

        Log.i("SetUpBluetooth","Message sending");
        String message = editText.getText().toString();

        if(message.length()>0) {
            byte[] send = message.getBytes();
            myBluetoothService.write(send);
            editText.setText("");

        }
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceDetails.add(device.getName());
                devices.add(device);
                Log.i("SetUpBluetooth","Devices found "+device.getName());
                adapter.notifyDataSetChanged();
            }

        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ENABLE_BT) {
                Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_LONG).show();
            }
        }
        if(resultCode == DISCOVERABLE_DURATION){
            Toast.makeText(this,"Device is discoverable",Toast.LENGTH_LONG).show();
        }

    }


    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1){
                        case MyBluetoothService.STATE_CONNECTED:
                            Toast.makeText(SetUpBluetooth.this,"Connected",Toast.LENGTH_LONG).show();
                            deviceDetails.clear();
                            break;
                        case MyBluetoothService.STATE_CONNECTING:
                            Toast.makeText(SetUpBluetooth.this,"Connecting",Toast.LENGTH_LONG).show();
                            break;
                        case MyBluetoothService.STATE_LISTEN:
                        case MyBluetoothService.STATE_NONE:
                            Toast.makeText(SetUpBluetooth.this,"Not connected",Toast.LENGTH_LONG).show();
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String writeMessage = new String(writeBuf);
                    deviceDetails.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf,0,msg.arg1);
                    Log.i("SetUpBluetooth","message_read" + readMessage);

                    deviceDetails.add("Connected device: " + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    break;
            }
            adapter.notifyDataSetChanged();
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(receiver);
    }

}
