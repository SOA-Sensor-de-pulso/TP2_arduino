package com.example.tp2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BluetoothDevicesActivity extends AppCompatActivity {

    private ListView bluetoothDevicesListView;
    private ArrayAdapter bluetoothDevicesToString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_devices);
        this.bluetoothDevicesListView = findViewById(R.id.list);

        this.bluetoothDevicesToString = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);

        this.bluetoothDevicesListView.setOnItemClickListener(clickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.createListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.checkBluetooth();
        registerReceiver(bthReceiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(bthReceiver);
    }

    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String[] info = ((TextView) view).getText().toString().split("\n");
            String address = info[1];

            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.putExtra("direccionSeleccionada", address);
            startActivity(intent);
        }
    };

    public void checkBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            this.createListView();
        }
        else{
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            someActivityResultLauncher.launch(enableBtIntent);
        }
    }

    private void createListView(){
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if(!this.bluetoothDevicesToString.isEmpty()) {
            this.bluetoothDevicesToString.clear();
        }
        for (BluetoothDevice device: bondedDevices) {
            bluetoothDevicesToString.add(device.getName() + '\n' + device.getAddress());
        }
        this.bluetoothDevicesListView.setAdapter(this.bluetoothDevicesToString);
    }

    private ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {});

    private final BroadcastReceiver bthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction()) &&
                    intent.getExtras().getInt("EXTRA_STATE") == BluetoothAdapter.STATE_DISCONNECTED){
                checkBluetooth();
            }
        }
    };
}