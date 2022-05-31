package com.example.tp2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class FlashLightManager implements SensorEventListener {

    private FlashLightPresenter presenter;
    private SensorManager sensorManager;
    private final static int NO_LIGHT = 0;

    FlashLightManager(FlashLightPresenter presenter, SensorManager sensorManager) {
        this.presenter = presenter;
        this.sensorManager = sensorManager;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized(this) {
            if(event.sensor.getType() == Sensor.TYPE_LIGHT){
                System.out.println("valor luz: " + event.values[0]);
                if(((int)event.values[0]) == FlashLightManager.NO_LIGHT) {
                    this.enableFlashLight();
                    this.presenter.notifyActivity("Linterna encendida!");
                }
                else {
                    this.disableFlashLight();
                    this.presenter.notifyActivity("Linterna apagada!");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

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

    private void enableFlashLight() {

        System.out.println("Encender linterna...");
    }

    private void disableFlashLight() {
        System.out.println("Apagar linterna...");
    }
}
