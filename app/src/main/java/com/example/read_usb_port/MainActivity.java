package com.example.read_usb_port;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.device.uart.SerialPort;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Timer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.example.read_usb_port.BuildFrame.ACK_STRING;
import static com.example.read_usb_port.BuildFrame.NACK_STRING;
import static com.example.read_usb_port.BuildFrame.calcularLRC;
import static com.example.utils.ISOUtil.hex2byte;

public class MainActivity extends AppCompatActivity {
    Button writeMessage;
    TextView tramaTV;
    EditText txtMessage;
    protected SerialPort mSerialPort;
    protected readUSB readUSB;
    protected int contadorErrores;
    private String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        writeMessage = findViewById(R.id.connectBT);
        tramaTV = findViewById(R.id.tramaTV);
        txtMessage = findViewById(R.id.txtMessage);
        tramaTV.setText("NO CONNECTED");

        writeMessage.setOnClickListener(view -> {
            String text = txtMessage.getText().toString().trim();
            writeMessage(text);
        });

        contadorErrores = 0;

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("-------SerialPort", mSerialPort.toString());
        if (mSerialPort != null) {
            Log.e("PORT", "get serial port");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Integer numRead;
                        String hexa;
                        InputStream inputStream;
                        ArrayList<String> trama = new ArrayList<>();
                        while (!Thread.currentThread().isInterrupted()) {
                            inputStream = mSerialPort.getInputStream();
                            numRead = inputStream.read();
                            if (numRead != -1) {
                                hexa = Integer.toHexString(numRead);
                                if (numRead < 10) {
                                    hexa = "0" + numRead;
                                }
                                if (BuildFrame.validarHexaLetra(hexa)) {
                                    hexa = "0" + hexa;
                                }
                                trama.add(hexa);
                            }
                            if (numRead == -1 && trama.size() > 0) {
                                ArrayList<String> finalTrama = trama;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tramaTV.setText(finalTrama.toString());
                                    }
                                });
                                trama = new ArrayList<>();
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void showAlertDialog(XmlPullParser parser) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(parser, null);
        builder.setView(view);
        AlertDialog alertDialog = builder.setCancelable(false).create();
        alertDialog.show();

        view.findViewById(R.id.btn_accept).setOnClickListener(v -> {
            alertDialog.dismiss();
        });
    }

    public void writeMessage(String message){
        OutputStream outputStream;
        Log.d("mserialport: ",mSerialPort.toString());
        outputStream = mSerialPort.getOutputStream();
        Log.d("outputStream: ",mSerialPort.getOutputStream().toString());
        byte[] bytes = message.getBytes();
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recibirMensaje(ArrayList<String> mensajeRecibido) {
        Log.d("Mensaje recibido ", mensajeRecibido.toString());
        //Se analiza el mensaje, para ver que tipo es, tomado del metodo de Silvia
        //primero se mira si es de un solo tamaño
        if (mensajeRecibido.size() == 1) {
            Log.d("Comunicacion USB", "Trama de un solo campo, valor " + mensajeRecibido);
            //validarNACK_ACK(mensajeRecibido.get(0));
        } else {
            if (mensajeRecibido.get(0).equals("06")) {
                Log.d("Comunicacion USB", "Trama con un ACK inicial");
                //pilaTransRecibidas.add("ACK");
                mensajeRecibido.remove(0);
            }
            //por ultimo se toma el resto del mensaje, que corresponderia a un mensaje normal
            //Silvia le calcula el LRC de una vez
            Byte LRC_calculado = calcularLRC(mensajeRecibido);
            //se toma el LRC del mensaje recibido
            int LRC_recibido = Integer.parseInt(mensajeRecibido.get(mensajeRecibido.size() - 1), 16);
            Log.d("COM15", "read-->LRC_recibido: " + LRC_recibido + ", LRC_calculado: " + LRC_calculado + ", tam trama: " + mensajeRecibido.size());
            //se hace la comparacion
            int LRC_calculado_int = LRC_calculado.intValue();
            if(LRC_calculado_int < 0){
                LRC_calculado_int += 256;
            }
            if (LRC_calculado_int == LRC_recibido) {
                Log.d("COM15", "read-->LRC son iguales");
                //enviarACK(); no esta funcionando enviar el ACK aqui

            } else {
                Log.d("COM15", "read-->Contador LRC diferentes: ");
                //readUSB.interrupt();

                //enviarNACK();
            }

        }
    }

    public String obtenerCadena(ArrayList<String> mensajeRecibido){
        String trama = "";
        if (mensajeRecibido != null && mensajeRecibido.size() > 0) {
            for (int i = 0; i < mensajeRecibido.size(); i++) {
                trama += mensajeRecibido.get(i);
            }
        }else{
            return "";
        }
        return trama;
    }

    private String obtenerAscii(ArrayList<String> mensajeRecibido) {
        String trama = "";
        if (mensajeRecibido != null && mensajeRecibido.size() > 0) {
            for (int i = 0; i < mensajeRecibido.size(); i++) {
                trama += mensajeRecibido.get(i);
            }
        }else{
            return "";
        }
        StringBuilder salida = new StringBuilder("");
        for (int i = 0; i < trama.length(); i += 2) {
            String str = trama.substring(i, i + 2);
            salida.append((char) Integer.parseInt(str, 16));
        }
        return salida.toString();
    }

    public String NACK_ACK_String(String hexa) {
        String retorno = "";
        if (hexa.equalsIgnoreCase(NACK_STRING)) {
            retorno = "NACK";
        } else if (hexa.equalsIgnoreCase(ACK_STRING)) {
            retorno = "ACK";
        }
        return retorno;
    }

}

