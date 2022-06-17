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
import android.util.Log;

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
    final private static List<String> commands = Collections.unmodifiableList(
            Arrays.asList("encender", "apagar")
    );


    BthModel(String macAddress) {
        this.device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress);
        this.bthConnectionThread = new BthAcceptConnectionThread(device, this);
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void tryConnection() {
        this.bthConnectionThread.start();
    }

    public void closeConnection() {
        this.bthHandleConnectionThread.cancel();
    }

    public void getPulseSensorValue(String data) {
        this.presenter.notifyValues(data);
    }

    public void sendCommandToDevice(String command) {
        char commandNumber = Character.forDigit(commands.indexOf(command),10);

        byte[] info = {(byte)commandNumber,'\0'};
        this.bthHandleConnectionThread.write(info);
        System.out.println("enviando comando " + commands.indexOf(command) + " a dispostivo...");
    }

    public void inititateCallback(BluetoothSocket bluetoothSocket){
        this.presenter.notifyConnectionEstablished();
        this.bthHandleConnectionThread = new BthHandleConnectionThread(bluetoothSocket,this);
        this.bthHandleConnectionThread.start();
    }

    public void readCallback(byte[] readData){
        Log.i("app_arduino", "iniciando lectura");
        for (byte value: readData) {
            Log.i("app_arduino", String.valueOf(value -'0'));
        }
        this.getPulseSensorValue(new String(readData));
    }
}

interface ICallback{
    public void inititateCallback(BluetoothSocket bluetoothSocket);
    public void readCallback(byte[] readData);
}