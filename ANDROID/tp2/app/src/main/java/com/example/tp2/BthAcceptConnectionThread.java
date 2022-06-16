package com.example.tp2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.UUID;

public class BthAcceptConnectionThread extends Thread {
    private final BluetoothSocket mmSocket;
    private ICallback callback;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Handler mainHandler;

    public BthAcceptConnectionThread(BluetoothDevice device, ICallback callback, Looper mainLoop) {
        BluetoothSocket tmp = null;
        try {
            tmp = device.createInsecureRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.callback = callback;
        this.mmSocket = tmp;
        this.mainHandler = new Handler(mainLoop);
    }

    public void run() {
        Log.i("app_arduino","Intentando conexion de " + this.mmSocket.getRemoteDevice().getName());
        // Cancel discovery because it otherwise slows down the connection.
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e("app_arduino", "Could not close the client socket", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        manageMyConnectedSocket(mmSocket);
    }


    private void manageMyConnectedSocket(BluetoothSocket socket){
        this.callback.inititateCallback(socket);
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e("app_arduino", "Could not close the client socket", e);
        }
    }
}
