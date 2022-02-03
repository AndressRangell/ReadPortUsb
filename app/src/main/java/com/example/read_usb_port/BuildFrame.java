package com.example.read_usb_port;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.example.utils.ISOUtil;

public class BuildFrame {

    //Variables de tramas
    public final static String ACK_STRING = "06";
    public final static String NACK_STRING = "15";
    public final static String SEPARADOR_STRING = "1C";

    public final static byte STX = 0x02;
    public final static byte ETX = 0x03;
    public final static byte SEPARADOR = 0x1C;

    public final static String USB_STRING = "USB";
    public final static String TCPIP_STRING = "TCPIP";
    public final static String BANDA = "BANDA";
    public final static String CHIP = "CHIP";
    public final static String CTL = "CTL";


    //Transacciones que salen del POS al MPK
    //################### POS -> MPK #############################
    //Ventas
    public final static String ULTIMA_TRANS = "1000  1";
    public final static String NUEVA_PANTALLA = "1004  0";
    public final static String NUEVA_PANTALLA_PIN = "1004  0";
    public final static String SOLICITUD_DATOS = "1000  2";
    public final static String RESP_HOST = "1000  3";
    public final static String RESP_HOST_CONTACTLESS = "1006  0";
    //cierre
    public final static String CIERRE_CANTIDAD = "1001  0";//-->cantidad de transacciones inicial
    public final static String DATOS_CIERRE = "1001  1"; //la transaccion
    public final static String RESP_CIERRE = "1001  2";//-->respuesta final
    //inicializacion
    public final static String RESP_INIT = "1002  0";
    //anulacion
    public final static String SOLICITUD_ANULACION_REFERENCIA = "1005  0";
    public final static String RESULTADO_BUSQUEDA_REFERENCIA = "1005 1";
    public final static String RESPUESA_HOST_ANULACION = "1005  2";

    //Tramas que llegan al POS
    //################## MPK -> POS ###############################
    //Ventas
    public final static String SOLICITUD_CONEXION = "1000000";
    public final static String SOLICITUD_CONEXION_CONTACTLESS = "1006000";
    public final static String TRANS_REV_No = "1000001";
    public final static String TRANS_REV_Si = "1000001";
    public final static String TRANSACCION_ENVIO_DATOS = "1000002";
    public final static String TARJETA_CONTACTLESS = "1006001";
    //Cierre
    public final static String SOLICITUD_CIERRE = "1001000";
    //Inicializacion
    public final static String SOLICITUD_INIT = "1002000";
    //Anulacion
    public final static String SOLICITUD_ANULACION = "1005000";
    public final static String REFERENCIA_TRANSACCION_ANULACION = "1005001";
    public final static String CONFIRMACION_ANULACION = "1005002";

    public final static String ESTADO_POS = "1003000";

    public final static String PROC_CANCEL = "1004  0";


    //Presentation Header para las tramas enviadas al MPK en formato Bytes
    //venta
    public final static byte[] ULTIMA_TRANS_BYTE ={0x31, 0x30, 0x30, 0x30, 0x20, 0x20, 0x31 };
    public final static byte[] NUEVA_PANTALLA_BYTE ={ 0x31, 0x30, 0x30, 0x34, 0x20, 0x20, 0x30 };
    public final static byte[] SOLCITUD_DATOS_BYTE ={ 0x31, 0x30, 0x30, 0x30, 0x20, 0x20, 0x32 };
    public final static byte[] RESP_HOST_BYTES ={ 0x31, 0x30, 0x30, 0x30, 0x20, 0x20, 0x33 };
    public final static byte[] RESP_HOST_CONTACTLESS_BYTES ={ 0x31, 0x30, 0x30, 0x36, 0x20, 0x20, 0x30 };
    //cierre
    public final static byte[] CIERRE_CANTIDAD_BYTES ={ 0x31, 0x30, 0x30, 0x31, 0x20, 0x20, 0x30 };
    public final static byte[] DATOS_CIERRE_BYTES ={ 0x31, 0x30, 0x30, 0x31, 0x20, 0x20, 0x31 };
    public final static byte[] RESP_CIERRE_BYTES ={ 0x31, 0x30, 0x30, 0x31, 0x20, 0x20, 0x32 };
    //inicializacion
    public final static byte[] RESP_INIT_BYTES ={0x31, 0x30, 0x30, 0x32, 0x20, 0x20, 0x30};
    //anulacion
    public final static byte[] SOLICITUD_ANULACION_REFERENCIA_BYTES ={ 0x31, 0x30, 0x30, 0x35, 0x20, 0x20, 0x30 };
    public final static byte[] RESULTADO_BUSQUEDA_REFERENCIA_BYTES ={ 0x31, 0x30, 0x30, 0x35, 0x20, 0x20, 0x31 };
    public final static byte[] RESPUESTA_HOST_ANULACION_BYTES ={ 0x31, 0x30, 0x30, 0x35, 0x20, 0x20, 0x32 };

    //Transport Header
    public final static  byte []transportHeader={0x36, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30 };//(10Bytes)



    public final static String MENSAJE_COMUNICACION_USB = "Comunicación USB...";
    public final static String MENSAJE_ERROR_COMUNICACION_USB = "Error de comunicación USB";
    public final static String MENSAJE_COMUNICACION_TCP_IP = "Comunicación TCP/IP...";
    public final static String MENSAJE_ERROR_COMUNICACION_TCP_IP = "Error de comunicación TCP/IP";
    public final static String MENSAJE_ESPERANDO_RESPUESTA = "Esperando Respuesta";
    public final static String MENSAJE_PROCESANDO_DEPOSITO = "Procesando Depósito...";
    public final static String MENSAJE_ERROR_COMUNICACION_CAJAS = "Error de Comunicación";
    public final static String MENSAJE_TRANSACCION_CANCELADA = "Transacción Cancelada";
    public final static String MENSAJE_USUARIO_CANCELO = "Usuario canceló";
    public final static String MENSAJE_ERROR_COMUNICACION_PROCESAR = "Error de comunicación, error al procesar el mensaje";
    public final static String MENSAJE_ERROR_MENSAJE_INESPERADO = "Error de comunicación, mensaje inesperado";


/*

    */
/**
     * Metodo utilizado para obtener el numero de la ultima transacción realizada
     *
     * @return arreglo de bytes en formato hexa
     *//*

    public static byte[] obtenerNumUltTrans(TransLog transLog) {
        byte[] retorno = new byte[6];
        if (transLog.getSize() > 0) {
            String ultTrans = transLog.get(transLog.getSize() - 1).getTraceNo();
            Log.d("","ult trans---" + ultTrans);
            retorno = ultTrans.getBytes(StandardCharsets.US_ASCII);//convertir cadena en []ascii
        }
        return retorno;
    }
*/

    /**
     * Metodo que permite convertir un String a un arreglo de bytes en hexadecimal
     *
     * @param size
     * @param value
     * @return
     */
    public static byte[] converT2byteHexa(Integer size, String value) {
        //Logger.debug("Convirtiendo a hexa el valor " + value);
        byte[] retorno = new byte[size];
        if(value != null)retorno = value.getBytes(StandardCharsets.US_ASCII);//convertir cadena en []ascii
        return retorno;
    }

    /**
     * Metodo utilizado para calcular el LRC de la trama
     * LRC = XOR de todos los bytes después del STX [0] (sin incluir) hasta el ETX [leng-2](incluyéndolo)
     *
     * @param arreglo
     * @return
     */
    public static byte calcularLRC(byte[] arreglo) {
        byte LRC = 0x00;
        //definir nuevo arreglo sin incluir la primera y la ultima posicion del arreglo
        byte[] trama = new byte[arreglo.length - 2];

        //llenar el [] trama quitando el STX (primer elemento) y el ultimo elemento de la trama (LRC)
        for (int i = 1; i < arreglo.length - 1; i++) {
            trama[i - 1] = arreglo[i];
        }
        //calculo del LRC
        for (int i = 0; i < trama.length; i++) {
            LRC ^= trama[i];//XOR
            Log.d("","MET for LRC--" + LRC);
        }
        Log.d("","MET LRC---" + LRC);
        return LRC;
    }

    /**
     * Metodo utilizado para calcular el LRC de la trama que se almaceno
     * @param frame
     * @return
     */
    public static byte calcularLRC(ArrayList<String> frame) {//numeros hexa
        byte LRC = 0x00;
        String trama = "";

        if (frame != null && frame.size() > 0) {

            for (int i = 1; i < frame.size() - 1; i++) {
                trama += frame.get(i);
            }
            Log.d("","I/BuildFrame: 90 --> trama  completa: " + trama);

            byte[] arreglo = ISOUtil.hex2byte(trama);
            for (int i = 0; i < arreglo.length; i++) {
                LRC ^= arreglo[i];//XOR
            }
        }
        return LRC;
    }


    /**
     * Metodo utilizado para armar la trama de la ultima transacción
     * Envia numero de recibo y codigo de repuesta
     *
     * @return la parte variable de la ultima trasacción
     *//*
    public static byte[] armarUltTrans(TransLog transLog) {
        byte[] retorno = new byte[18]; //
        //----1er campo---- Nombre: Numero de recibo (cod 43), Long: 6 byte, formto: ASCII
        //separador
        retorno[0] = SEPARADOR;
        //Nombre del campo (codigo: 43 en ascii ; 34 33 en hexa )
        retorno[1] = 0x34;
        retorno[2] = 0x33;
        //longitud del campo
        retorno[3] = 0x00;
        retorno[4] = 0x06;
        int con = 5;
        //campo (numero de recibo)
        byte[] campoNum = obtenerNumUltTrans(transLog);
        for (int i = 0; i < campoNum.length; i++) {
            retorno[i + 5] = campoNum[i];
            con++;
        }
        //----2do campo---- Nombre: Codigo de respuesta -Cod:48- Lognitud: 2 ASCII Numerico
        //separador
        byte[] codRespuesta = hacerCodigoRespuesta(true);
        for (int i = 0; i < codRespuesta.length; i++) {
            retorno[con] = codRespuesta[i];
            con++;
        }

        return retorno;
    }*/


    /**
     * Metodo utilizado para armar el mensaje de nueva pantalla que es enviado al MPK
     * @return el mensaje en bytes
     */
    public static byte[] armarNuevaPantalla() {
        byte[] retorno = new byte[14]; //
        //----1er campo---- Nombre: Solicitud Nueva Pantalla (cod 87), Long: 2 byte, formto: HEXA
        //Nombre del campo (codigo: 48 en ascii ; 34 38 en hexa )

        byte[] codigoResp = hacerCodigoRespuesta(true);
        System.arraycopy(codigoResp,0,retorno,0, codigoResp.length);

        retorno[7] = SEPARADOR;
        //se agrega el 2do campo, el campo 87
        //codigo del campo
        retorno[8] = 0x38;
        retorno[9] = 0x37;
        //longitud del campo
        retorno[10] = 0x00;
        retorno[11] = 0x02;
        //pantalla de insertar la tarjeta
        retorno[12] = 0x12;
        retorno[13] = 0x01;

        return retorno;
    }

    /**
     * Metodo utilizado para armar el mensaje de nueva pantalla PIN que es enviado al MPK
     * @return
     */
    public static byte[] armarNuevaPantallaPIN() {
        byte[] retorno = new byte[14]; //
        //----1er campo---- Nombre: Solicitud Nueva Pantalla (cod 87), Long: 2 byte, formto: HEXA
        byte[] codigoResp = hacerCodigoRespuesta(true);
        System.arraycopy(codigoResp,0,retorno,0, codigoResp.length);

        retorno[7] = SEPARADOR;
        //se agrega el 2do campo, el campo 87, longitud 2
        //codigo del campo
        retorno[8] = 0x38;
        retorno[9] = 0x37;
        //longitud del campo
        retorno[10] = 0x00;
        retorno[11] = 0x02;
        //pantalla de pin
        retorno[12] = 0x12;
        retorno[13] = 0x02;
        return retorno;
    }

    /**
     * Metodo usado para armar el mensaje de solicitud de datos que es enviado al MPK
     * @return
     */
    public static byte[] armarSolicitudDatos() {
            return hacerCodigoRespuesta(true);
    }


    /**
     * Metodo que permite hacer un campo de codigo de respuesta, correcto o incorrecto dependiendo
     * del boolean enviando
     * @param tipo el tipo de codigo de respuesta, true si es correcto, de lo contrario false
     * @return el campo de codigo de respuesta
     */
    public static byte[] hacerCodigoRespuesta(boolean tipo){
        byte retorno[] = new byte[7];
        retorno[0] = SEPARADOR;
        //Nombre del campo
        retorno[1] = 0x34;
        retorno[2] = 0x38;

        byte[] retorn;
        retorn= ISOUtil.str2bcd("4",true);
        //byte[] field62 = ISOUtil.str2bcd(str62, false);

        System.out.println("retorno :"+ ISOUtil.byte2hex(retorn));


        //Longitud
        retorno[3] = 0x00;
        retorno[4] = 0x02;
        //campo
        if (tipo){
            retorno[5] = 0x20;
            retorno[6] = 0x20;
        }else{
            retorno[5] = 0x58;
            retorno[6] = 0x58;
        }


        String retCompleto=ISOUtil.byte2hex(retorno);

        System.out.println("Retorno  Completo : "+retCompleto);
        return retorno;
    }


    /**
     * Metodo que permite hacer un campo de codigo respuesta con el valor de retVal, se
     * usa para las respuestas finales de la diferentes transaccion de cajas
     * @param valorRetVal el valor del retval en int
     * @return el campo de codigo de respuesta
     */
    public static byte[] hacerCodigoRespuestaFinal(int valorRetVal){
        byte retorno[] = new byte[7];

        retorno[0] = SEPARADOR;
        //Nombre del campo
        retorno[1] = 0x34;
        retorno[2] = 0x38;
        //Longitud
        retorno[3] = 0x00;
        retorno[4] = 0x02;
        //campo, cuando el valor retval es 0, quiere decir que es correcto
        switch (valorRetVal){
            case 0://caso venta exitosa
                retorno[5] = 0x30;
                retorno[6] = 0x30;
                break;
            case 101://caso no hay conexion
                retorno[5] = 0x39;
                retorno[6] = 0x39;
                break;
            default:
                retorno[5] = 0x31;
                retorno[6] = 0x31;
                break;
        }
        return retorno;
    }

    /**
     * Metodo que analiza la parte de datos una trama de codigo de respuesta y regresa si es un codigo de respuesta
     * correcto o incorrecto
     * @param trama
     * @return
     */
    public static boolean analizarCodigoRespuesta(List<String> trama){
        if(trama.size()==2){
            if (trama.get(0).equalsIgnoreCase("20")) {
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    /**
     * Metodo que analiza la parte de datos una trama de codigo de respuesta normal y regresa si es un codigo de respuesta
     * correcto o incorrecto
     * @param trama
     * @return
     */
    public static boolean analizarCodigoRespuestaNormal(List<String> trama){
        if(trama.size()==2){
            if (trama.get(0).equalsIgnoreCase("30")) {
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    /**
     * metodo utilizado para calcular la longitud de la trama a enviar
     * corresponde: primer byte transportHeader hasta el byte anterior al ETX
     * @param size tamaño de la trama (TransportHeader+PresentationHeader+Campos)
     * @return
     * Creado por Silvia Hernandez
     */
    public static byte[] calcularLongitudMensaje(int size) {
        return ISOUtil.int2bcd(size, 2);
    }


    /**
     * Metodo utilizado para descomponer la parte fija del mensaje recibido
     * El presentation header
     * @param trama
     * Creado por Silvia Hernandez
     */
    public static String obtenerPresentationHeader(ArrayList<String> trama) {
        String presentationHeader = "";
        if (trama.size() > 19) {
            for (int i = 13; i < 20; i++) {
                //es el que identifica la trasacción; ubicado desde la posición 13 a 19 de la trama
                presentationHeader = presentationHeader + trama.get(i);
            }
        }
        return presentationHeader;
    }


    /**
     * Metodo utilizado para convetir una cadena hexadecimal a ASCII
     *
     * @param hexStr-->Cadena con la trama  en hexadecimal
     * @return Cadena convertida a ASCII
     * Creado por: Silvia Hernandez
     */
    public static String hexToAscii(String hexStr) {
        StringBuilder retorno = new StringBuilder();

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            retorno.append((char) Integer.parseInt(str, 16));
        }

        return retorno.toString();
    }

    /**
     * Converts a hex string into a byte array
     *
     * @param s source string (with Hex representation)
     * @return byte array
     */
    public static byte[] hex2byte(String s) {
        if (s.length() % 2 == 0) {
            return ISOUtil.hex2byte(s.getBytes(), 0, s.length() >> 1);
        } else {
            // Padding left zero to make it even size #Bug raised by tommy
            return hex2byte("0" + s);
        }
    }

    /**
     * Metodo utilizado para validar si el String es hexa: A,B,C,D,E o F (decimal 10 al 15)
     *
     * @param hexa --> valor hexa
     * @return true--> si es un decimal del 10 al 15, false
     * Creado por: Silvia Hernandez
     */
    public static boolean validarHexaLetra(String hexa) {
        if (hexa.equalsIgnoreCase("a") || hexa.equalsIgnoreCase("b")
                || hexa.equalsIgnoreCase("c") || hexa.equalsIgnoreCase("d")
                || hexa.equalsIgnoreCase("e") || hexa.equalsIgnoreCase("f")) {
            return true;
        }else{
            return false;
        }
    }

    /**
     * Metodo que permite buscar un campo en la trama,
     * @param mensaje el arreglo del mensaje
     * @param campo el campo a buscar ej: 43, 48
     * @return Una lista con los datos del campo buscado, null si no encuentra el campo
     */
    public static List<String> buscarDato(ArrayList<String> mensaje, Integer campo) {
        try {
            Integer contador = 20;
            if(mensaje!=null) {
                while (contador < mensaje.size() - 2) {
                    if (isSeparador(mensaje.get(contador))) {
                        String tipo = conversorAString(mensaje.subList(contador + 1, contador + 3));
                        Integer tipoInteger = Integer.valueOf(tipo);
                        if (campo == tipoInteger) {
                            StringBuilder juntos = new StringBuilder();
                            juntos.append(mensaje.get(contador + 3));
                            juntos.append(mensaje.get(contador + 4));
                            int longitudInteger = hex2Int(juntos.toString());
                            //Logger.debug("Imprimiendo longitud " + longitudInteger);
                            return mensaje.subList(contador + 5, contador + 5 + longitudInteger);
                        }
                    }
                    contador++;
                }
                return null;
            }else{
                return null;
            }
        }catch (Exception e){
            //Logger.error("Hubo un error recorriendo la trama para buscar un dato");
            return null;
        }
    }

    /**
     * Metodo que convierte una lista en un string continuo con los datos
     * @param porcion lista a convertir
     * @return el string equivalente a la lista
     */
    public static String conversorAString(List<String> porcion) {
        StringBuilder sb = new StringBuilder();
        for (String s : porcion) {
            sb.append(s);
        }
        return hexToAscii(sb.toString());
    }


    /**
     * Metodo que convierte un String de hex (0A0C) a integer, se usa para tomar la longitud de un campo
     * de una trama 0x00 0x12 = 12
     * @param hex el String de hex
     * @return el resultado
     */
    public static int hex2Int(String hex) {
        int resultado = 0;
        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {
            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            resultado += decimal;
        }
        return resultado;
    }

    /**
     * Metodo usado para saber si un valor hexa en string es separador = 1C
     * @param entrada
     * @return
     */
    public static boolean isSeparador(String entrada){
        return entrada.equalsIgnoreCase(SEPARADOR_STRING);
    }
}
