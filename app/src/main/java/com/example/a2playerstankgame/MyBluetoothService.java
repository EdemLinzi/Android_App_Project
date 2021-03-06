package com.example.a2playerstankgame;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MyBluetoothService {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private BluetoothAdapter bluetoothAdapter;
    public static final UUID MY_UUID_SECURE = UUID.fromString("058041fe-9936-4e36-86e9-fcda2be5560d");
    public static final UUID MY_UUID_INSECURE = UUID.fromString("dd846754-f3c8-47da-b764-bbfddbdaa396");

    private ConnectedThread connectedThread;
    private ClientThread clientThread;
    private ServerThread serverThreadSecure;
    //ServerThread serverThreadInsecure;

    private int mState;
    private int newState;
    private final Handler mHandler;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private Context context;

    public MyBluetoothService(Handler handler){
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        newState = mState;
        mHandler = handler;
    }

    private synchronized void updateUserInterface(){
        mState = getState();
        newState = mState;
        mHandler.obtainMessage(SetUpBluetooth.MESSAGE_STATE_CHANGE,newState,-1).sendToTarget();

    }

    public synchronized int getState(){
        return mState;
    }

    public synchronized void start(){
        Log.i("MyBluetoothService","start");

        if(clientThread != null){
            clientThread.cancel();
            clientThread = null;
        }

        if(connectedThread!=null){
            connectedThread.cancel();
            connectedThread = null;
        }

        if(serverThreadSecure == null){
            serverThreadSecure = new ServerThread(true);
            serverThreadSecure.start();
        }

        /*if(serverThreadInsecure  ==null){
            serverThreadInsecure = new ServerThread(false);
            serverThreadInsecure.start();
        }*/

        updateUserInterface();

    }


    public synchronized void connect(BluetoothDevice bluetoothDevice,boolean secure){
        Log.i("MyBluetoothService","connect to "+bluetoothDevice.getName());

        if(mState == STATE_CONNECTING){
            if(clientThread!=null){
                clientThread.cancel();
                clientThread = null;
            }
        }

        if(connectedThread!=null){
            connectedThread.cancel();
            connectedThread = null;
        }

        clientThread = new ClientThread(bluetoothDevice,secure);
        clientThread.start();

        updateUserInterface();

    }

    public synchronized void connected(BluetoothSocket socket,BluetoothDevice device){
        Log.i("MyBluetoothService","connected to "+device.getName());


        if(connectedThread!=null){
            connectedThread.cancel();
            connectedThread = null;
        }

        if(clientThread != null){
            clientThread.cancel();
            clientThread = null;
        }

        /*if(serverThreadSecure!=null){
            serverThreadSecure.cancel();
            serverThreadSecure = null;
        }*/

       /* if(serverThreadInsecure!=null){
            serverThreadInsecure.cancel();
            serverThreadInsecure = null;
        }*/

        connectedThread = new ConnectedThread(socket);
        connectedThread.start();


        Message msg = mHandler.obtainMessage(SetUpBluetooth.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(SetUpBluetooth.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);


        updateUserInterface();

    }

    public synchronized void stop(){
        Log.i("MyBluetoothService","stop");
        if(clientThread != null){
            clientThread.cancel();
            clientThread = null;
        }

        if(connectedThread!=null){
            connectedThread.cancel();
            connectedThread = null;
        }

        if(serverThreadSecure!=null){
            serverThreadSecure.cancel();
            serverThreadSecure = null;
        }

        /*if(serverThreadInsecure!=null){
            serverThreadInsecure.cancel();
            serverThreadInsecure = null;
        }*/


        mState = STATE_NONE;
        updateUserInterface();

    }

    public void write(byte[] out){
        ConnectedThread r;
        synchronized (this){
            if(mState != STATE_CONNECTED) return;
            r=connectedThread;
        }
        r.write(out);
    }

    private void connectionFailed(){
        //Toast.makeText(context,"Connection Failed",Toast.LENGTH_LONG).show();
        Log.i("MyBluetoothService","connectionFailed");

        mState = STATE_NONE;
        stop();
        updateUserInterface();

        //MyBluetoothService.this.start();
    }

    public void connectionLost(){
        //Toast.makeText(context,"Connection Lost",Toast.LENGTH_LONG).show();
        Log.i("MyBluetoothService","connectionLost");

        mState = STATE_NONE;
        stop();
        updateUserInterface();

        //MyBluetoothService.this.start();
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = mmSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run() {
            Log.i("MyBluetoothService","BEGIN connectedThread");
            byte[] buffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.

                while (mState == STATE_CONNECTED) {
                    try {
                        // Read from the InputStream.
                        numBytes = mmInStream.read(buffer);
                        // Send the obtained bytes to the UI activity.
                        mHandler.obtainMessage(SetUpBluetooth.MESSAGE_READ, numBytes, -1, buffer)
                                .sendToTarget();

                    } catch (IOException e) {
                        Log.d(TAG, "Input stream was disconnected", e);
                        connectionLost();
                        break;
                    }
                }

        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                mHandler.obtainMessage(
                        SetUpBluetooth.MESSAGE_WRITE, -1, -1, bytes)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                Log.i("MySetUpBluetooth", "ConnectedThread close");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ServerThread extends Thread {

        private static final String NAME = "My_Application";
        private final BluetoothServerSocket mmServerSocket;

        public ServerThread(boolean secure) {

            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                    tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME,MY_UUID_SECURE);
                }
            catch (IOException e) {
                Log.e("SetUpBluetooth Server", "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
            mState = STATE_LISTEN;

        }

        public void run() {
            Log.i("MyBluetoothService","Start ServerThread");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (mState != STATE_CONNECTED ) {
                try {
                    if(mmServerSocket!=null)
                    socket = mmServerSocket.accept();
                    Log.i("MyBluetoothService","Server's accept");
                } catch (IOException e) {
                    Log.e("MyBluetoothService", "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                        switch (mState){
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket,socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    socket.close();
                                    Log.i("MyBluetoothService", "Socket closed");

                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                Log.i("MySetUpBluetooth", "ServerThread close");

                mmServerSocket.close();
            } catch (IOException e) {

                Log.e("SetUpBluetooth Server", "Could not close the connect socket", e);
            }
        }
    }

    public class ClientThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ClientThread(BluetoothDevice device,boolean secure) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                if(secure) {
                    tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                }
                else{
                    tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e("SetUpBluetooth Client", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
            //updateUserInterface();
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.

            Log.i("MyBluetoothService","Client run");

            if(bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("SetUpBluetooth Client", "Could not close the client socket", closeException);
                }
                connectionFailed();
                return;
            }

            synchronized (MyBluetoothService.this){
                clientThread = null;
            }

            connected(mmSocket,mmDevice);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                Log.i("MySetUpBluetooth", "ClientThread close");
                mmSocket.close();
            } catch (IOException e) {
                Log.e("MySetUpBluetooth Client", "Could not close the client socket", e);
            }
        }
    }



}