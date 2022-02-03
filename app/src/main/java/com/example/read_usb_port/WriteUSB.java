package com.example.read_usb_port;

import android.util.Log;

import com.pos.device.uart.SerialPort;

import java.io.IOException;
import java.io.OutputStream;

public class WriteUSB implements Runnable {

    Thread hilo;

    private boolean suspender; //Suspende un hilo cuando es true
    private boolean pausar;    //Detiene un hilo cuando es true
    private static SerialPort mSerialPort;
    private byte[] tramaEnviar;
    private String stringbytes;

    public WriteUSB(String nombre, SerialPort serialPort, byte[] trama) {
        this.tramaEnviar = trama;
        this.mSerialPort = serialPort;
        this.stringbytes = new String((trama));
        this.hilo = new Thread(this, nombre);
        this.suspender = false;
        this.pausar = false;
    }

    public static WriteUSB crearEIniciar(String nombre, byte[] trama, SerialPort serialPort) {
        WriteUSB escritura = new WriteUSB(nombre, serialPort, trama);
        escritura.hilo.start(); //Iniciar el hilo
        return escritura;
    }

    @Override
    public void run() {
        Log.d("",hilo.getName() + " iniciando.");
        try {
            OutputStream os = mSerialPort.getOutputStream();
            os.write(tramaEnviar);
            synchronized (this) {
                while (suspender) {
                    wait();
                }
                if (pausar) return;
            }

        } catch (InterruptedException exc) {
            Log.d("",hilo.getName() + "interrumpido.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("",hilo.getName() + " finalizado.");
    }

    //Pausar el hilo
    synchronized void pausarhilo() {
        pausar = true;
        //lo siguiente garantiza que un hilo suspendido puede detenerse.
        suspender = false;
        notify();
    }

    //Suspender un hilo
    synchronized void suspenderhilo() {
        suspender = true;
    }

    //Renaudar un hilo
    synchronized void renaudarhilo() {
        suspender = false;
        notify();
    }

}
