package com.example.tp2;

import static android.bluetooth.BluetoothProfile.GATT_SERVER;

import android.Manifest;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BthModel implements ICallback {

    private Presenter presenter;
    private BluetoothDevice device;
    private BthAcceptConnectionThread bthConnectionThread;
    private BluetoothSocket socket;
    private BthHandleConnectionThread bthHandleConnectionThread;

    BthModel(Looper mainLooper) throws Exception {
        //Object[] connectedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices().toArray();
        /*if (connectedDevices.size() != 1) {
            throw new Exception();
        }*/

        //this.device = (BluetoothDevice) connectedDevices[1];
        this.device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:21:06:BE:5A:36");
        this.bthConnectionThread = new BthAcceptConnectionThread(device, this);
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void tryConnection() {
        this.bthConnectionThread.start();
        //BluetoothSocket socket = this.bthConnectionThread.getBluetoothSocket();

        //this.bthHandleConnectionThread = new BthHandleConnectionThread(socket);
        //this.bthHandleConnectionThread.start();
    }

    public void closeConnection() {
        this.bthHandleConnectionThread.cancel();
    }


    final private static List<String> commands = Collections.unmodifiableList(
            Arrays.asList("encender", "apagar")
            );

    //poner toda la logica asociada al bluetooth
    public void getPulseSensorValue() {
        //definir thread/asyncTask/Service para no bloquear activity principal
        this.presenter.notifyValues("80");
    }

    public void sendCommandToDevice(String command) {
        //definir thread/asyncTask/Service para no bloquear activity principal
        this.bthHandleConnectionThread.write(intToByteArray(commands.indexOf(command)));
        System.out.println("enviando comando " + commands.indexOf(command) + " a dispostivo...");
    }

    public void inititateCallback(BluetoothSocket bluetoothSocket){
        this.bthHandleConnectionThread = new BthHandleConnectionThread(bluetoothSocket,this);
        this.bthHandleConnectionThread.start();
    }

    public void readCallback(byte[] readData){
        this.getPulseSensorValue();
    }

    public static byte[] intToByteArray(int a)
    {
        byte[] ret = new byte[4];
        ret[3] = (byte) (a & 0xFF);
        ret[2] = (byte) ((a >> 8) & 0xFF);
        ret[1] = (byte) ((a >> 16) & 0xFF);
        ret[0] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }
}

interface ICallback{
    public void inititateCallback(BluetoothSocket bluetoothSocket);
    public void readCallback(byte[] readData);
}