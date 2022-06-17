package com.example.tp2;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
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

        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        
        this.bluetoothDevicesToString = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);

        for (BluetoothDevice device: bondedDevices) {
            bluetoothDevicesToString.add(device.getName() + '\n' + device.getAddress());
        }

        this.bluetoothDevicesListView.setOnItemClickListener(clickListener);
        this.bluetoothDevicesListView.setAdapter(bluetoothDevicesToString);
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
}