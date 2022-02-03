package com.example.read_usb_port;

import android.content.Context;
import android.util.Log;

import com.pos.device.SDKManager;
import com.pos.device.SDKManagerCallback;
import com.pos.device.uart.SerialPort;

import java.io.InputStream;
import java.util.ArrayList;

public class USB {

    MediadorTodo mediador;
    private readUSB readUSB;
    public static SerialPort mSerialPort;
    private Context ctx;
    private static boolean keepRunning = true;

    public USB(MediadorTodo m, Context contexto) {
        this.ctx = contexto;
        SDKManager.init(ctx, new SDKManagerCallback() {
            @Override
            public void onFinish() {
            }
        });
        mSerialPort = SerialPort.getInstance(SerialPort.DEFAULT_CFG);

        if (mSerialPort == null) {
            mSerialPort = SerialPort.getInstance(SerialPort.DEFAULT_CFG, 1);
            if (mSerialPort == null) {
                mSerialPort = SerialPort.getInstance(SerialPort.DEFAULT_CFG, SerialPort.TTY_GS0);
                if (mSerialPort == null) {
                    mSerialPort = SerialPort.getInstance(SerialPort.DEFAULT_CFG, SerialPort.TTY_USB0);
                    if (mSerialPort == null) {
                        mSerialPort = SerialPort.getInstance(SerialPort.DEFAULT_CFG, SerialPort.TTY_ACM0);
                    }
                }
            }
        }
        if (mSerialPort != null) {
            Log.e("PORT", "get serial port");
            this.inicilizar();
            readUSB = new readUSB();
            readUSB.start();
        }
        this.mediador = m;
    }

    public static SerialPort getmSerialPort() {
        return mSerialPort;
    }

    /**
     * Inicializa el receptor USB
     *
     * @return
     */

    public int inicilizar() {
        readUSB = null;
        keepRunning = true;
        return 0;
    }

    public static void detenerHilo() {
        keepRunning = false;
    }

    class readUSB extends Thread {

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
                    inputStream = mSerialPort.getInputStream();
                    numRead = inputStream.read();
                    if (numRead != -1) {
                        //Lo que se realiza al recibir una trama
                        Log.d("","Trama recibida: numREad--" + numRead);
                        hexa = Integer.toHexString(numRead);
                        if (numRead < 10) {
                            hexa = "0" + numRead;
                        }
                        if (BuildFrame.validarHexaLetra(hexa)) {
                            hexa = "0" + hexa;
                        }
                        //TXCajas.getTrama().add(hexa);
                        Log.d("","Hexa recibida: numREad--" + hexa);
                        trama.add(hexa);
                        //Log.d("COM15", "read-->" + TXCajas.getTrama().toString());
                    }
                    if (!keepRunning) break;
                    if (numRead == -1 && trama.size() > 0) {
                        mediador.recibirMensaje(trama);
                        trama = new ArrayList<>();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
