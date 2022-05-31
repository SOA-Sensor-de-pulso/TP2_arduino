package com.example.tp2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

public class FlashLightManager implements SensorEventListener {

    private FlashLightPresenter presenter;
    private SensorManager sensorManager;
    private CameraManager cameraManager;
    private String cameraId;

    private final static float NO_LIGHT = 0f;

    FlashLightManager(FlashLightPresenter presenter,
                      SensorManager sensorManager,
                      CameraManager cameraManager,
                      String cameraId) {
        this.presenter = presenter;
        this.sensorManager = sensorManager;
        this.cameraManager = cameraManager;
        this.cameraId = cameraId;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized(this) {
            if(event.sensor.getType() == Sensor.TYPE_LIGHT){

                System.out.println("valor luz: " + event.values[0]);

                if((event.values[0]) == FlashLightManager.NO_LIGHT) {
                    this.switchFlashLight(true);
                    this.presenter.notifyActivity("Linterna encendida!");
                }
                else {
                    this.switchFlashLight(false);
                    this.presenter.notifyActivity("Linterna apagada!");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void startLuminositySensor(){
        this.sensorManager.registerListener(
                this,
                this.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopLuminositySensor() {
        this.sensorManager.unregisterListener(
                this,
                this.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
    }

    private void switchFlashLight(boolean state) {
        try {
            this.cameraManager.setTorchMode(this.cameraId,state);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
