package com.example.read_usb_port;

import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;

public class readUSB extends Thread {

    private ICajas cajas;
    private boolean keepRunning;

    public readUSB(ICajas cajas) {
        this.cajas = cajas;
        this.keepRunning = true;
    }

    public void detenerHilo() {
        keepRunning = false;
    }

    @Override
    public void run() {
        Log.d("","Hilo lectura corriendo...");
        try {
            Integer numRead;
            String hexa;

            InputStream inputStream;
            ArrayList<String> trama = new ArrayList<>();
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(10);
                inputStream = cajas.getMSerialPort().getInputStream();
                numRead = inputStream.read();
                if (numRead != -1) {
                    Log.d("","readUSB/37 --- > Trama recibida: numREad--" + numRead);
                    hexa = Integer.toHexString(numRead);
                    if (numRead < 10) {
                        hexa = "0" + numRead;
                    }
                    if (BuildFrame.validarHexaLetra(hexa)) {
                        hexa = "0" + hexa;
                    }
                    Log.d("","Hexa recibido: numREad--" + hexa);
                    trama.add(hexa);
                }
                if (!keepRunning) break;
                if (numRead == -1 && trama.size() > 0) {
                    //Se procede a analizar el mensaje
                    cajas.recibirMensaje(trama);
                    trama = new ArrayList<>();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


