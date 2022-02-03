package com.example.read_usb_port;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;


import java.net.Socket;
import java.util.ArrayList;

import static android.content.Context.POWER_SERVICE;
import static com.example.read_usb_port.BuildFrame.ACK_STRING;
import static com.example.read_usb_port.BuildFrame.NACK_STRING;
import static com.example.read_usb_port.BuildFrame.calcularLRC;
import static com.example.utils.ISOUtil.hex2byte;

/**
 * Clase que se interpone entre el dispositivo de salida y la aplicacion
 * permite enviar y recibir los mensajes dependiendo del dispositivo de medio
 * con el que se haya inicializado
 */
public class MediadorTodo {


    private String tipoConexion;
    private Context contexto;
    private int contadorErrores;
    private USB usb;
    private Socket socket;
    public static boolean comercioCajas;


    /**
     * Constructor del mediador
     *
     * @param contexto     el contexto de la aplicacion
     * @param tipoConexion el tipo de conexion a realizar, sea USB o TCP/IP
     */
    public MediadorTodo(Context contexto, String tipoConexion) {
        //Logger.debug("creando mediador todo");
        this.inicializaciones(tipoConexion, contexto);
        comercioCajas = false;

        if (tipoConexion.equals("USB")) {
            usb = new USB(this, contexto);
        } else {
        }
    }

    /**
     * Inicializacion de variables necesarias para hacer la conexiones
     */
    private void inicializaciones(String tipoConexion, Context contexto) {
        this.tipoConexion = tipoConexion;
        this.contexto = contexto;
        this.contadorErrores = 0;
    }


    /**
     * Metodo que es llamado cuando se recibe un mensaje, hay que analizarlo
     *
     * @param mensajeRecibido es un arraylist, donde esta contenida la trama recibida
     *                        <p>
     *                        Creado por Silvia Hernandez
     */
    public void recibirMensaje(ArrayList<String> mensajeRecibido) {
        //Logger.debug("Mensaje recibido " + mensajeRecibido.toString());
        //Se analiza el mensaje, para ver que tipo es, tomado del metodo de Silvia
        //primero se mira si es de un solo tamaño
        if (mensajeRecibido.size() == 1) {
            String retorno = NACK_ACK_String(mensajeRecibido.get(0));
            if (retorno.equals("ACK")) {
                enviarNACK();
            } else if (retorno.equals("NACK")) {
                enviarNACK();
            }
        } else {
            //por ultimo se toma el resto del mensaje, que corresponderia a un mensaje normal
            //Silvia le calcula el LRC de una vez
            Byte LRC_calculado = calcularLRC(mensajeRecibido);
            //Logger.debug("LRC_calculado: " + LRC_calculado);
            //se toma el LRC del mensaje recibido
            int LRC_recibido = Integer.parseInt(mensajeRecibido.get(mensajeRecibido.size() - 1), 16);
            Log.d("COMX", "read-->LRC_recibido: " + LRC_recibido + ", LRC_calculado: " + LRC_calculado + ", tam trama: " + mensajeRecibido.size());
            //se hace la comparacion
            int LRC_calculado_int = LRC_calculado.intValue();
            if (LRC_calculado_int < 0) {
                LRC_calculado_int += 256;
            }
            if (LRC_calculado_int == LRC_recibido) {
                //enviar ACK
                Log.d("COMX", "read--> LRC son iguales");
                //flujoTransaccion(mensajeRecibido);
            } else {
                Log.d("COMX", "read--> Contador LRC diferentes: ");
                //readUSB.interrupt();
                enviarNACK();
            }

        }
    }

    /**
     * Metodo que envia un NACK
     */
    private void enviarNACK() {
        if (contadorErrores < 3) {
            if (tipoConexion.equals("USB")) {
                Log.d("COMX", "read-->Contador enviarNACK: " + hex2byte(NACK_STRING).toString());
                //enviarMensaje(null);
                WriteUSB.crearEIniciar("Escritura", hex2byte(NACK_STRING), usb.getmSerialPort());
            } else {
                Log.d("No  está leyendo ", "El puerto ");
                /*//Logger.debug("I/Mediador: "+"Comunicacion TCP enviar NACK");
                socket = SocketTCP.getInstance();
                if (socket == null || socket.isClosed()) {
                    //Logger.debug("Fallo al enviar la trama");
                }else {
                    //SocketTCP.sendFrame(hex2byte(NACK_STRING));*/
            }
        }
    }


    /**
     * Metodo que envia un ACK
     */
    private void enviarACK() {
        if (contadorErrores < 3) {
            if (tipoConexion.equals("USB")) {
                Log.d("COMX", "read-->Contador enviarNACK: " + hex2byte(ACK_STRING).toString());
                //enviarMensaje(null);
                WriteUSB.crearEIniciar("Escritura", hex2byte(ACK_STRING), usb.getmSerialPort());
                //cantidadEnviados++;
            } else {
                /*//Logger.debug("I/Mediador: "+"Comunicacion TCP enviar ACK");
                socket = SocketTCP.getInstance();
                if (socket == null || socket.isClosed()) {
                    //Logger.debug("Fallo al enviar la trama");
                } else {
                    SocketTCP.sendFrame(hex2byte(ACK_STRING));
                }*/
            }
        }
    }

    /**
     * Metodo utilizado para validar si la trama recibida corresponde a un ACK o NACK
     *
     * @param hexa-->trama
     * @return NACK->hexa=15   ACK->hexa=06
     * Creado por Silvia Hernandez
     */
    public String NACK_ACK_String(String hexa) {
        String retorno = "";
        if (hexa.equalsIgnoreCase(NACK_STRING)) {
            retorno = "NACK";
        } else if (hexa.equalsIgnoreCase(ACK_STRING)) {
            retorno = "ACK";
        }
        return retorno;
    }

    /**
     * metodo utilizado para continuar con el flujo, valida la transacción recibida
     * en este caso son solo transacciones iniciales, de lo contrario responde con NACK
     *
     * @param trama
     *//*
    public void flujoTransaccion(ArrayList<String> trama) {
        Log.d("PORT", "read-->trama " + trama.toString());
        String idTrans = obtenerPresentationHeader(trama);
        Log.d("PORT", "read-->ID TRANS hexa: " + idTrans);
        String ascii = hex2AsciiStr(idTrans);
        Log.d("PORT", "read-->ID TRANS asci: " + ascii);
        encenderPantallaPos();

        if ((ascii.equals(SOLICITUD_CONEXION) || ascii.equals(SOLICITUD_CONEXION_CONTACTLESS) || ascii.equals(SOLICITUD_CIERRE) || ascii.equals(SOLICITUD_ANULACION)) && trama.size() == 29) {
            String idcomercio = conversorAString(buscarDato(trama, 79));
            if (idcomercio != null) {
                StartAppATC.idAcquirer = idcomercio;
                comercioCajas = true;
            } else {
                comercioCajas = false;
            }
            Log.d("PORT", "read-->ID Comercio: " + StartAppATC.idAcquirer);

        }
        switch (ascii) {
            case SOLICITUD_CONEXION:
                enviarACK();
                Logger.debug("Transaccion de Solicitud de Conexion, respondiendo....");
                iniciarVentaCajas(tipoConexion);
                detenerHiloLectura();
                break;
            case SOLICITUD_CONEXION_CONTACTLESS:
                enviarACK();
                Logger.debug("Transaccion solicitud de conexion por Contactless, respondiendo");
                iniciarVentaCajasContactless(tipoConexion);
                detenerHiloLectura();
                break;
            case SOLICITUD_CIERRE:
                enviarACK();
                Logger.debug("Transaccion de solicitud de cierre, respondiendo...");
                iniciarSolicitudCierre(tipoConexion);
                detenerHiloLectura();
                break;
            case SOLICITUD_ANULACION:
                enviarACK();
                Logger.debug("Transaccion de solicitud de anulacion, respondiendo...");
                iniciarSolicitudAnulacion(tipoConexion);
                detenerHiloLectura();
                break;
            case SOLICITUD_INIT:
                enviarACK();
                Logger.debug("realizando inicializacion STIS");
                iniciarInicializacion();
                break;
            default:
                enviarNACK();
        }
    }*/

    /**
     * Inicia la inicializacion del STIS, muestra toast si no ha cerrado lote
     *//*
    public void iniciarInicializacion() {
        if (!ToolsBatch.statusTrans(contexto)) {
            detenerHiloLectura();
            Intent intent = new Intent(contexto.getApplicationContext(), InitCajas.class);
            intent.putExtra("tipo", tipoConexion);
            contexto.startActivity(intent);
        } else {
            Logger.debug("Mostrando mensaje de cerrar lote");
            enviarNACK();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    UIUtils.toast((Activity) contexto, R.drawable.ic_atc, "Cierre lote para continuar", Toast.LENGTH_LONG);
                }
            });
        }
    }
*/
    /**
     * Inicia la venta de cajas normal (Chip - Magnetico)
     *
     * @param tipoConexion el tipo de la conexion de cajas
     *//*
    public void iniciarVentaCajas(String tipoConexion) {
        Logger.debug("startTrans: " + PrintRes.TRANSEN[44]);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(contexto, MasterControl.class);
        intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[44]);
        intent.putExtra("tipo", tipoConexion);
        contexto.startActivity(intent);
    }
*/
    /**
     * Iniciado la venta de cajas Contactless
     *
     * @param tipoConexion el tipo de la conexion de cajas
     *//*
    public void iniciarVentaCajasContactless(String tipoConexion) {
        Logger.debug("startTrans: " + PrintRes.TRANSEN[47]);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(contexto, MasterControl.class);
        intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[47]);
        intent.putExtra("tipo", tipoConexion);
        contexto.startActivity(intent);
    }*/

    /**
     * Inicia cierre por cajas
     *
     * @param tipoConexion el tipo de la conexion de cajas
     *//*
    public void iniciarSolicitudCierre(String tipoConexion) {
        Logger.debug("startTrans: " + PrintRes.TRANSEN[46]);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(contexto, MasterControl.class);
        intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[46]);
        intent.putExtra("tipo", tipoConexion);
        contexto.startActivity(intent);
    }*/

    /**
     * Inicia la solicitud de anulacion
     *
     * @param tipoConexion el tipo de la conexion de cajas
     *//*
    public void iniciarSolicitudAnulacion(String tipoConexion) {
        Logger.debug("startTrans: " + PrintRes.TRANSEN[48]);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(contexto, MasterControl.class);
        intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[48]);
        intent.putExtra("tipo", tipoConexion);
        contexto.startActivity(intent);
    }*/

    /**
     * Metodo que enciende la pantalla del POS
     * by Hender y Alexis
     */
    public void encenderPantallaPos() {
        PowerManager.WakeLock wl;
        wl = ((PowerManager) contexto.getSystemService(POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag:");
        wl.acquire();
        wl.release();
    }

    /**
     * Permite detener los hilos de lectura tanto usb como TCP/IP
     *//*
    public void detenerHiloLectura() {
        if (usb != null) {
            Logger.debug("deteniendo hilo de lectura usb");
            usb.detenerHilo();
        } else if (tcp != null) {
            tcp.stopCommunication();
        }
    }

    public void detenerHiloLecturaSocket() {
        if (usb != null) {
            Logger.debug("deteniendo hilo de lectura usb");
            usb.detenerHilo();
        } else if (tcp != null) {
            tcp.stopCommunicationSocket();
        }
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }*/
}


