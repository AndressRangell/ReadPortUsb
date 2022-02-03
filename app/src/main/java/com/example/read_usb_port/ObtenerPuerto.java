package com.example.read_usb_port;

import android.util.Log;

import com.pos.device.uart.SerialPort;

public class ObtenerPuerto {

    protected SerialPort mSerialPort;

    /**
     * Inicializacion del puerto serial
     */
    public void obtenerPuertoSerial() {
        mSerialPort = SerialPort.getInstance(SerialPort.DEFAULT_CFG);

        if (mSerialPort == null) {
            mSerialPort = SerialPort.getInstance(SerialPort.DEFAULT_CFG, 1);
            if (mSerialPort == null) {
                mSerialPort = SerialPort.getInstance(SerialPort.DEFAULT_CFG, SerialPort.TTY_GS0);
                if (mSerialPort == null) {
                    mSerialPort = SerialPort.getInstance(SerialPort.DEFAULT_CFG, SerialPort.TTY_USB0);
                    if (mSerialPort == null) {
                        mSerialPort = SerialPort.getInstance(SerialPort.DEFAULT_CFG, SerialPort.TTY_ACM0);
                    }if (mSerialPort == null) {
                        mSerialPort = SerialPort.getInstance(SerialPort.DEFAULT_CFG, 5);
                    }
                }
            }
        }

        /**
         * Valiando el puerto serial
         * Se inicializa la lectura de USB
         */
        /*if (mSerialPort != null) {
            Log.e("PORT", "get serial port");
            readUSB = new readUSB(this);
            readUSB.start();
        }
    }

    public void openSocket() {
        writeSocket = new ThreadOpenSocket(this);
        writeSocket.start();
    }*/
}}
