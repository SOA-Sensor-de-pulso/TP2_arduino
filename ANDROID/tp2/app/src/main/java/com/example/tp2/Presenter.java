package com.example.tp2;

import android.widget.Toast;

public class Presenter {

    private MainActivity activity;
    private BthModel model;

    public Presenter(MainActivity activity,BthModel model) {
        this.activity = activity;
        this.model = model;
        model.setPresenter(this);
        //this.model.getPulseSensorValue();
    }

    public void onButtonClick(String command) {
        model.sendCommandToDevice(command);
    }

    public void notifyValues(String value) {
        this.activity.runOnUiThread(() -> activity.setReadValues(value));
    }

    public void notifyConnectionEstablished() {
        this.activity.runOnUiThread(() -> activity.showConnectionSuccess());
    }

    public void tryConnection() {
        this.model.tryConnection();
    }

    public void closeConnection() {
        this.model.closeConnection();
    }
}
