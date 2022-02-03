package com.example.read_usb_port;

import android.util.Log;

import java.net.Socket;
import java.util.ArrayList;

public class ThreadOpenSocket extends Thread {

    private ICajas cajas;
    private static boolean keepRunning;

    public ThreadOpenSocket(ICajas cajas){
        this.cajas = cajas;
        keepRunning = true;
        //resetCycle();
    }

    /**
     * Inicia el hilo para crear el socket
     */
    public void stopCommunication() {
        SocketTCP.disconnectSocket();
        keepRunning = false;
    }



    @Override
    public void run() {
        Socket sk = SocketTCP.openSocket(TCP.address, TCP.port);
        if (sk != null && sk.isConnected()) {
            while (keepRunning) {
                byte[] input = SocketTCP.waitFrame();
                Log.d("","Estoy Esperando");
                if (input != null) {
                    ArrayList<String> trama = SocketTCP.toListStringFrame(input);
                    cajas.recibirMensaje(trama);
                }else {
                    break;
                }
            }
        } else {
            Log.d("","No se pudo conectar el Socket");
        }
        //SocketTCP.disconnectSocket();
    }
}
