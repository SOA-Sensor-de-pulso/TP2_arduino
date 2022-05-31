package com.example.tp2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;

public class FlashLightPresenter {

    private FlashLightActivity activity;
    private FlashLightManager manager;

    FlashLightPresenter(FlashLightActivity activity,
                        SensorManager sensorManager,
                        CameraManager cameraManager,
                        String cameraId){
        this.activity = activity;
        this.manager = new FlashLightManager(this, sensorManager, cameraManager, cameraId);
    }

    public void startSensors() {
        this.manager.startLuminositySensor();
    }

    public void stopSensors() {
        this.manager.stopLuminositySensor();
    }

    public void notifyActivity(String text) {
        this.activity.setText(text);
    }
}
