package com.example.read_usb_port;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;



public class SocketTCP {

    private static Socket socket = null;

    private static Context ctx;
    public static boolean mensaje;

    public static void setCtx(Context context){
        ctx = context;
    }

    /**
     * Metodo para conectar un socket
     *
     * @param address
     * @param port
     * @return socket conectado o null si no se pudo conectar
     */
    public static Socket openSocket(String address, int port) {
        getInstance();
        if(socket.isConnected())
            return socket;
        Log.d("","I/SocketTCP " + "Connecting...");
        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
            socket.connect(inetSocketAddress, 500);
            Log.d("","I/SocketTCP " + "Connected");
            if(ctx != null){
                if (!mensaje) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                           // UIUtils.toast((Activity) ctx, R.drawable.ic_atc, "Conexión TCP/IP establecida.", Toast.LENGTH_LONG);
                        }
                    });
                    mensaje = true;
                }
            }
        } catch (IOException | NullPointerException e) {
            Log.d("","E/SocketTCP" + "Connecting fail " + e.getMessage());
            if(ctx != null){
                if (mensaje) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //UIUtils.toast((Activity) ctx, R.drawable.ic_atc, "No se pudo conectar al servidor Cajas TCP/IP", Toast.LENGTH_LONG);
                        }
                    });
                    mensaje = false;
                }
            }
            socket = null;
        }
        return socket;
    }

    /**
     * Metodo para cerrar el socket
     *
     * @return true si el socket se cerro correctamente
     */
    public static boolean disconnectSocket() {
        boolean result = false;
        try {
            if (socket != null && !socket.isClosed()){
                socket.close();
                socket = null;
            }
            Log.d("","I/SocketTCP " + "Closing OK");
            result = true;
        } catch (IOException e) {
            Log.d("","E/SocketTCP " + "Closing fail");
            result = false;
        }

        return result;
    }

    /**
     * Metodo para enviar una trama del POS hacia MPK
     *
     * @param frame
     * @return true si la trama se envio correctamente
     */
    public static boolean sendFrame( byte[] frame) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean result = false;
        int len = frame.length;
        Log.d("","I/SocketTCP " + "Sending frame...");
        try {
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            //for (byte b : frame) {
            //    output.writeByte(b);
            //}
            output.write(frame);
            Log.d("","I/SocketTCP " + "Sending frame OK");
            result = true;
        } catch (IOException e) {
            Log.d("","E/SocketTCP " + "Error sending frame " + e.getMessage());
            result = false;
        }
        return result;
    }

    /**
     * Metodo que espera una trama que proviene del MPK hacia el POS
     *
     * @return arreglo de bytes recibidos del MPK
     */
    public static byte[] waitFrame() {
        byte[] temporary = new byte[5000];
        byte[] frame = null;
        try {
            Log.d("","I/SocketTCP " + "waiting frame to MPK...");
            InputStream input = socket.getInputStream();
            int len = input.read(temporary);
            Log.d("",""+len);
            if (len > 1) {
                frame = dicardEmpty(temporary);
            } else if (len == 1) {
                frame = new byte[1];
                frame[0] = temporary[0];
            }
        } catch (IOException e) {
            Log.d("","E/SocketTCP " + "Error waiting for MPK frame " + e.getMessage());
        }
        return frame;
    }

    /**
     * Metodo para descartar los espacios sobrantes del arreglo temporal donde se recibe la trama
     * (SOLO MODO CAJA)
     *
     * @param data
     * @return arreglo de bytes con los datos exactos de la trama
     */
    private static byte[] dicardEmpty(byte[] data) {
        byte etx = 0x03;
        byte[] frame = null;
        int len = data.length;
        byte[] temporary = new byte[len];
        int i = 0;
        if (!manyAckAndNack(data)) {
            for (int j = 0; i < len; i++, j++) {
                byte b = data[i];
                Log.d("",""+b);
                if (Byte.compare(b, etx) == 0) {
                    temporary[j] = data[i];
                    temporary[++j] = data[++i];
                    break;
                }
                temporary[j] = data[i];
            }
            if (i == 5000)
                frame = new byte[i];
            else
                frame = new byte[++i];
            for (i = 0; i < frame.length; i++) {
                frame[i] = temporary[i];
            }
        } else {
            frame = new byte[1];
            frame[0] = data[0];
        }

        return frame;
    }

    private static boolean manyAckAndNack(byte[] data) {
        byte ack = 0x06;
        byte nack = 0x15;
        int len = data.length;

        byte b = data[0];
        byte c = data[1];
        if ((Byte.compare(b, ack) == 0 && Byte.compare(c, ack) == 0) || (Byte.compare(b, nack) == 0 && Byte.compare(c, nack) == 0)
                || (Byte.compare(b, ack) == 0 && Byte.compare(c, nack) == 0) || (Byte.compare(b, nack) == 0 && Byte.compare(c, ack) == 0) ) {
            return true;
        }

        return false;
    }

    /**
     * Metodo que convierte en un arreglo de String con formato hexadeimal una trama
     *
     * @param frame
     * @return trama covertida a String
     */
    public static ArrayList<String> toListStringFrame(byte[] frame) {
        ArrayList<String> result = new ArrayList<>();
        int numRead = frame.length;
        String hexa;

        if (numRead != -1) {
            for (byte b : frame) {
                Log.d("","Trama recibida: numREad--" + b);
                hexa = Integer.toHexString(b);
                if(b < 0){
                    int c = b + 256;
                    hexa = Integer.toHexString(c);
                }
                if (b < 10) {
                    if(b >= 0)
                        hexa = "0" + b;
                }
                if (BuildFrame.validarHexaLetra(hexa)) {
                    hexa = "0" + hexa;
                }
                Log.d("","Hexa recibida: numREad--" + hexa);
                result.add(hexa);
            }
        }
        return result;
    }


    public static Socket getInstance() {
        if (socket == null || socket.isClosed()) {
            socket = new Socket();
        }
        return socket;
    }

    private SocketTCP() {
    }
}