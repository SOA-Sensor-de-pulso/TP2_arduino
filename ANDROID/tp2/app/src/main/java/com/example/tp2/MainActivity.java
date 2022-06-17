package com.example.tp2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText bthCommand;
    private TextView title;
    private TextView readValues;
    private Button button;
    private Button nextActivity;
    private Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.readValues = findViewById(R.id.textView3);
        Intent intent = this.getIntent();

        this.checkBluetooth();

        try {
            this.presenter = new Presenter(this, new BthModel(intent.getStringExtra("direccionSeleccionada")));
        } catch (Exception e) {
            Log.e("app_arduino","Debe haber un unico dispositivo conectado");
            e.printStackTrace();
        }

        this.bthCommand = findViewById(R.id.bthCommand);
        this.button = findViewById(R.id.button);
        this.title = findViewById(R.id.textView2);


        this.button.setOnClickListener(v -> {
            presenter.onButtonClick(bthCommand.getText().toString());
            Toast.makeText(getApplicationContext(),bthCommand.getText().toString(),Toast.LENGTH_LONG).show();
        });

        this.nextActivity = findViewById(R.id.button2);
        this.nextActivity.setOnClickListener(v -> startActivity(new Intent(v.getContext(), FlashLightActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.presenter.tryConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bthReceiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(bthReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.presenter.closeConnection();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void checkBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            someActivityResultLauncher.launch(enableBtIntent);
        }
    }

    private ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    checkBluetooth();
                }
            });

    private final BroadcastReceiver bthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction()) &&
                intent.getExtras().getInt("EXTRA_STATE") == BluetoothAdapter.STATE_DISCONNECTED){
                checkBluetooth();
            }
        }
    };

    public void setReadValues(String value) {
        this.readValues.setText(value);
    }

    public void showConnectionSuccess() {
        Toast.makeText(this.getApplicationContext(),"Conexion establecida!",Toast.LENGTH_LONG).show();
    }
}