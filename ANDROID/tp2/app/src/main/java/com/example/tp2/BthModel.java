package com.example.tp2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BthModel {
    final private static List<String> commands = Collections.unmodifiableList(
            Arrays.asList("encender", "apagar")
            );

    //poner toda la logica asociada al bluetooth
    public void getPulseSensorValue() {
        //definir thread/asyncTask/Service para no bloquear activity principal
    }

    public void sendCommandToDevice(String command) {
        //definir thread/asyncTask/Service para no bloquear activity principal
        System.out.println("enviando comando " + commands.indexOf(command) + " a dispostivo...");
    }
}
