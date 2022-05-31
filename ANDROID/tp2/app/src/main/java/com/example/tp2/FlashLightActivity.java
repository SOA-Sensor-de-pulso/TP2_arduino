package com.example.tp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.widget.TextView;

public class FlashLightActivity extends AppCompatActivity{

    private TextView text;
    private FlashLightPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_light);

        this.text = findViewById(R.id.textView);
        this.text.setText("Linterna apagada!");

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        CameraManager mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String mCameraId = null;
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        this.presenter = new FlashLightPresenter(this, sensorManager, mCameraManager, mCameraId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.presenter.startSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.presenter.stopSensors();
    }

    public void setText(String text) {
        this.text.setText(text);
    }
}