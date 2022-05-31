package com.example.tp2;

import android.app.Activity;

public class Presenter {

    private Activity activity;
    private BthModel model;

    public Presenter(Activity activity,BthModel model) {
        this.activity = activity;
        this.model = model;
    }

    public void onButtonClick(String command) {
        model.sendCommandToDevice(command);
    }
}
