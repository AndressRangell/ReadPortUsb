package com.example.read_usb_port;

import android.content.Context;
import android.util.Log;


import java.net.Socket;


public class TCP {
    private Context ctx;
    private MediadorTodo mediador;

    public static int port;
    public static String address;

    public ThreadOpenSocket open;
    public static boolean keepRunning = false;

    public TCP(Context ctx, MediadorTodo mediador) {
        //obtenerIp();
        this.ctx = ctx;
        this.mediador = mediador;
        startCommunication();
    }

    /*public void obtenerIp() {
        TMConfig tm = TMConfig.getInstance();
        try {
            address = tm.getIP3();
            port = Integer.parseInt(tm.getPort3());

        } catch (Exception e) {
            address = "1.1.1.1";
            port = 0000;
        }

    }*/

    /**
     * Inicia el hilo para crear el socket
     */
    public void startCommunication() {
        resetCycle();
        open = new ThreadOpenSocket();
        open.start();
    }

    /**
     * Inicia el hilo para crear el socket
     */
    public void stopCommunication() {
        keepRunning = false;
        //SocketTCP.disconnectSocket();
    }

    /**
     * Inicia el hilo para crear el socket
     */
    public static void stopCommunicationSocket() {
        keepRunning = false;
        SocketTCP.disconnectSocket();
    }

    public static void resetCycle() {
        keepRunning = true;
    }

    public class ThreadOpenSocket extends Thread {
        @Override
        public void run() {
            while (TCP.keepRunning){
                SocketTCP.setCtx(ctx);
                Socket sk = SocketTCP.openSocket(TCP.address, TCP.port);
                if (sk != null && sk.isConnected()) {
                    while (TCP.keepRunning) {
                        byte[] input = SocketTCP.waitFrame();

                        if (input != null) {
                            mediador.recibirMensaje(SocketTCP.toListStringFrame(input));
                        } else {
                            SocketTCP.disconnectSocket();
                            break;
                        }
                    }
                } else {
                    Log.d("","No se pudo conectar el Socket");
                }
            }
            //SocketTCP.disconnectSocket();
        }
    }
}
