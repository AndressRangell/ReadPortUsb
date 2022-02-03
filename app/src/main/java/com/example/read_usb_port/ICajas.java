package com.example.read_usb_port;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;

import com.pos.device.uart.SerialPort;

import java.util.ArrayList;
import java.util.Arrays;

import static com.example.read_usb_port.BuildFrame.ACK_STRING;
import static com.example.read_usb_port.BuildFrame.NACK_STRING;
import static com.example.read_usb_port.BuildFrame.SEPARADOR;
import static com.example.read_usb_port.BuildFrame.converT2byteHexa;

public abstract class ICajas{

    protected ThreadOpenSocket writeSocket;

    protected Context ctx;
    protected SerialPort mSerialPort;
    protected String tipoConexion;
    protected readUSB readUSB;

    protected ArrayList<String> pilaTransRecibidas;
    protected ArrayList<String> pilaTransEnviadas;
    protected int contadorErrores;
    protected WriteUSB usb;
    protected boolean keepRunning;
    protected String LocalPan;

    //protected ThreadOpenSocket writeSocket;

    //usado para almanecar informacion de la tarjeta para la venta en magnetico
    //protected CardInfo cardInfoGlobal;

    CountDownTimer contador;

    String tipoEntrada;


    public static boolean CTL_SIGN;
    protected boolean CTL_PIN_GLOBAL;
    protected long auxCVMLocal_Global;
    //protected EXTRAPARAM_ROW extraparam_row_global;

    public ICajas(Context ctx) {
        this.ctx = ctx;
    }

    public ICajas(Context ctx, String transEname, TransInputPara p) {
        super();
        //isCajas=true;
        //Logger.debug("creando interfaz i cajas");
        this.pilaTransRecibidas = new ArrayList<>();
        this.pilaTransEnviadas = new ArrayList<>();
        this.contadorErrores = 0;
        this.keepRunning = true;
        CTL_SIGN = false;
        //Contador usado para detener la transaccion si han pasado 30 segundos
        contador = new CountDownTimer(70000,5000){
            @Override
            public void onTick(long l) {
            }
            @Override
            public void onFinish() {
                //Logger.error("Tiempo de espera agotado.....finalizando");
                enviarMensaje(NACK_STRING);
                resetearTodo();
                //transUI.showError(timeout, "Tiempo de espera agotado");
            }
        }.start();
    }

    /**
     * Metodo usado para reiniciar el contador de 70 segundos
     */
    public void reiniciarTiempoEspera(){
        contador.cancel();
        contador.start();
    }

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
                    }
                }
            }
        }

        /**
         * Valiando el puerto serial
         * Se inicializa la lectura de USB
         */
        if (mSerialPort != null) {
            Log.e("PORT", "get serial port");
            readUSB = new readUSB(this);
            readUSB.start();
        }
    }

    /*public void openSocket() {
        writeSocket = new ThreadOpenSocket(this);
        writeSocket.start();
    }*/

    /**
     * Metodo que permite crear un campo especifico de un dato
     * @param campo el valor del dato Ej: 5000
     * @param idCampo el id del campo
     * @param longitud la longitud del campo
     * @return un arreglo de bytes con todos los datos del campo: separador - id - longitud - valor
     */
    public byte[] crearCampo(String campo, Integer idCampo, Integer longitud) {
        byte[] totalByte = new byte[longitud + 5];
        byte[] idCampoByte = new byte[2];
        byte[] longitudCampoByte = new byte[2];
        byte[] campoByte = converT2byteHexa(longitud, campo);
        totalByte[0] = SEPARADOR;
        switch (idCampo) {
            case 1: //Codigo de Autorizacion
                idCampoByte[0] = 0x30;
                idCampoByte[1] = 0x31;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x06;
                break;
            case 30: //Envio Bin de tarjeta
                idCampoByte[0] = 0x33;
                idCampoByte[1] = 0x30;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x0C;
                break;
            case 40: //Monto de transaccion
                idCampoByte[0] = 0x34;
                idCampoByte[1] = 0x30;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x0C;
                break;
            case 43: //Numero del recibo
                idCampoByte[0] = 0x34;
                idCampoByte[1] = 0x33;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x06;
                break;
            case 44: //RNN
                idCampoByte[0] = 0x34;
                idCampoByte[1] = 0x34;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x0C;
                break;
            case 45: //Terminal ID
                idCampoByte[0] = 0x34;
                idCampoByte[1] = 0x35;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x08;
                break;
            case 46: //Fecha
                idCampoByte[0] = 0x34;
                idCampoByte[1] = 0x36;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x04;
                break;
            case 47: //Hora
                idCampoByte[0] = 0x34;
                idCampoByte[1] = 0x37;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x04;
                break;
            case 48://cod de respuesta
                idCampoByte[0] = 0x34;
                idCampoByte[1] = 0x38;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x02;
                break;
            case 54: //Ultimos 4 digitos
                idCampoByte[0] = 0x35;
                idCampoByte[1] = 0x34;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x04;
                break;
            case 75: //BIN
                idCampoByte[0] = 0x37;
                idCampoByte[1] = 0x35;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x06;
                break;
            case 77: //idcomercio
                idCampoByte[0] = 0x37;
                idCampoByte[1] = 0x37;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x17;
                break;
            case 50:
                idCampoByte[0] = 0x35;
                idCampoByte[1] = 0x30;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x02;
                break;
            case 51:
                idCampoByte[0] = 0x35;
                idCampoByte[1] = 0x31;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x02;
                break;
            case 61:
                Arrays.fill(totalByte, (byte) 0x20);
                idCampoByte[0] = 0x36;
                idCampoByte[1] = 0x31;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x45;
                totalByte[0] = SEPARADOR;
                break;
            case 82:
                idCampoByte[0] = 0x38;
                idCampoByte[1] = 0x32;
                longitudCampoByte[0] = 0x00;
                longitudCampoByte[1] = 0x0C;
                break;

        }
        System.arraycopy(idCampoByte, 0, totalByte, 1, idCampoByte.length);
        System.arraycopy(longitudCampoByte, 0, totalByte, 3, longitudCampoByte.length);
        if(longitud!=0){
            System.arraycopy(campoByte, 0, totalByte, 5, campoByte.length);
        }
        return totalByte;
    }


    /**
     * Metodo utilizado para armar la parte variable para armar la respuesta al Host
     *
     * @return
     */
   /* protected byte[] armarRespuestaHost() {
        //--------------- ORDEN DE LA TRAMA -----------------//
        //Campo 01 Codigo de Autorizacion, 6
        //Campo 40 Monto Compra, 10
        //Campo 43 Numero de Recibo, 6
        //Campo 44 RRN, 6
        //Campo 45 Terminal ID, 8
        //Campo 46 Fecha Transaccion, (6 en documento) 2 en la trama
        //Campo 47 Hora Transaccion, (4 en documento) 2 en la trama
        //Campo 48 Codigo de Respuesta, (2 en documento) 2 en la trama // 0x30 0x30 si correcto, 0x39 0x39 si no
        //Falta campo 49 Franquicia
        //Campo 50 Tipo de Cuenta,
        //Campo 51 Numero de Cuotas
        //Campo 54 Ultimos 4 Digitos, 4
        //Campo 61 Mensaje de Error, 45
        //Falta campo 62 Holder Name
        //Falta campo 63 Criptograma
        //Falta campo 64 TVR
        //Falta campo 66 TSI
        //Falta campo 67 AID
        //Falta campo 68 Label
        //Campo 75 BIN tarjeta, 6
        //Falta campo 76 Fecha de Vencimiento de la tarjeta
        //Falta campo 77 Codigo comercio
        //Falta campo 78 Direccion Establecimiento


        //Campos en el spectra 13, Campos en el documento 23

        //Inicializamos el arreglo de Bytes
        byte[] retorno;
        if(AmountCashBack > 0){
            retorno = new byte[219];
        }else{
            retorno = new byte[202];
        }

        //una bandera para saber en donde vamos
        int bandera = 0;

        //arreglo fuente | inicio arreglo fuente | arreglo destino | inicio de copiado en arreglo destino | longitud a copiar


        //Campo 01 Codigo de Autorizacion, 6
        byte[] otroAuthCode = crearCampo(AuthCode, 1, 6);
        System.arraycopy(otroAuthCode, 0, retorno, bandera, otroAuthCode.length);
        bandera += otroAuthCode.length;

        //campo 30 Envio de Bin
        //byte[] binTarjeta = crearCampo("48999768",30,12);

        //Campo 40, Monto de la transaccion, 12
        //Amount;
        Long montoTotal;
        if(AmountCashBack > 0){
            montoTotal = Amount + AmountCashBack;
        }else{
            montoTotal = Amount;
        }
        String AmountComplete = ISOUtil.padleft(montoTotal + "" , 12, '0');
        byte[] montoBytes = crearCampo(AmountComplete, 40, 12);
        System.arraycopy(montoBytes, 0, retorno, bandera, montoBytes.length);
        bandera += montoBytes.length;


        //Campo 43, Numero del recibo , 6
        //En el spectra llegan todos vacios
        byte[] numeroReciboByte = crearCampo(TraceNo, 43, 6);
        System.arraycopy(numeroReciboByte, 0, retorno, bandera, numeroReciboByte.length);
        bandera += numeroReciboByte.length;

        //Campo 44, RNN, 6
        Logger.debug("El RRN es: " + RRN);
        byte[] rrnByte = crearCampo(RRN, 44, 12);
        System.arraycopy(rrnByte, 0, retorno, bandera, rrnByte.length);
        bandera += rrnByte.length;

        //Campo 45, Terminal ID, 8
        byte[] terminalIdByte = crearCampo(TermID, 45, 8);
        System.arraycopy(terminalIdByte, 0, retorno, bandera, terminalIdByte.length);
        bandera += terminalIdByte.length;

        //Campo 46, Fecha de transaccion, 2
        //LocalDate;
        Logger.debug("Fecha de la transaccion es " + LocalDate);
        byte[] localDateByte = crearCampo(LocalDate, 46, 4);
        System.arraycopy(localDateByte, 0, retorno, bandera, localDateByte.length);
        bandera += localDateByte.length;

        //Campo 47, Hora de transaccion, 2
        //LocalTime;
        //byte[] hora = ISOUtil.int2bcd (Integer.valueOf(LocalTime.substring(0,2)),1);
        Logger.debug("Hora de la transaccion " + LocalTime);
        byte[] localTimeByte = crearCampo(LocalTime.substring(0,4),47,4);
        System.arraycopy(localTimeByte, 0, retorno, bandera, localTimeByte.length);
        bandera += localTimeByte.length;

        //Campo 48, Codigo de respuesta, 2
        Logger.debug("Mostrando codigo de respuesta " + RspCode);
        byte[] codigoRespuestaByte;
        if(RspCode==null){
            codigoRespuestaByte = crearCampo("99", 48, 2);
        }else{
            codigoRespuestaByte = crearCampo(RspCode, 48, 2);
        }
        System.arraycopy(codigoRespuestaByte, 0, retorno, bandera, codigoRespuestaByte.length);
        bandera += codigoRespuestaByte.length;

        //Campo 50, tipo de cuenta
        //isDebit;
        Integer tipoInt = 3;
        if(isDebit){
            tipoInt = 1;
        }
        String tipoIntString = ISOUtil.padleft(tipoInt + "", 2, '0');
        byte[] tipoCuentaByte = crearCampo(tipoIntString, 50, 2);
        System.arraycopy(tipoCuentaByte, 0, retorno, bandera, tipoCuentaByte.length);
        bandera += tipoCuentaByte.length;

        //Campo 51, Numero de cuotas
        //numCuotas;
        String numCuotasString = ISOUtil.padleft(numCuotas + "", 2, '0');
        byte[] numCuotasByte = crearCampo(numCuotasString, 51, 2);
        System.arraycopy(numCuotasByte, 0, retorno, bandera, numCuotasByte.length);
        bandera += numCuotasByte.length;

        //Campo 54, Ultimos 4 digitos
        //Pan.substring();
        Logger.debug("El Local Pan es " + LocalPan);
        byte[] ultimosBytes = crearCampo(LocalPan.substring(LocalPan.length() - 4), 54, 4);
        System.arraycopy(ultimosBytes, 0, retorno, bandera, ultimosBytes.length);
        bandera += ultimosBytes.length;

        //Campo 61, Mensaje de error. 45
        String mensajeerror;
        if (RspCode!= null && RspCode.equals("00")){
            mensajeerror= "0000000";
        }else {
            int respuesta=formatRsp(RspCode);
            mensajeerror= TransUIImpl.getErrInfo(String.valueOf(respuesta));
        }
        byte[] mensajeErrorBytes = crearCampo(mensajeerror, 61, 69);
        System.arraycopy(mensajeErrorBytes, 0, retorno, bandera, mensajeErrorBytes.length);
        bandera += mensajeErrorBytes.length;

        //Campo 75, BIN tarjeta (6 primeros numeros), 6
        //Pan.substring(0,5);
        byte[] binByte = crearCampo(LocalPan.substring(0, 6), 75, 6);
        System.arraycopy(binByte, 0, retorno, bandera, binByte.length);

        //campo 82, Monto de Cashback
        if(AmountCashBack > 0){
            bandera += binByte.length;
            String AmountCashback = ISOUtil.padleft(AmountCashBack + "", 12, '0');
            byte[] montoCashbackBytes = crearCampo(AmountCashback, 82, 12);
            System.arraycopy(montoCashbackBytes, 0, retorno, bandera, montoCashbackBytes.length);

        }
        return retorno;
    }*/

    /**
     * Metodo que permite reiniciar las variables usadas para la venta de cajas, incluso el manejo de los
     * hilos de lectura USB y TCP/IP
     */
    public void resetearTodo() {
        //Logger.debug("Reseteando todo....");
        pilaTransEnviadas = new ArrayList<>();
        pilaTransRecibidas = new ArrayList<>();
        contadorErrores = 0;
        LocalPan = "";
        contador.cancel();
        if(readUSB != null) {
            readUSB.detenerHilo();
        }
        if (writeSocket != null){
            //Logger.debug("Cerrar la comunicacion");
            writeSocket.stopCommunication();
        }
        USB.detenerHilo();
        TCP.resetCycle();
    }

    public SerialPort getMSerialPort() {
        return mSerialPort;
    }


/*
    public void desempaquetarEnvioDatos(ArrayList<String> mensaje) {
        //0 STX
        //1 - 2 Longitud
        //3 - 19 Transport header y presentation header

        //String montoString = conversorAString(40, Arrays.copyOfRange(mensaje,20,36));

        //Primero revisar el valor del codigo de respuesta, para saber si hay que continuar con la transaccion
        try {
            if (mensaje.get(58).equalsIgnoreCase("58")) {
                resetearTodo();
                transUI.showError(timeout, MENSAJE_TRANSACCION_CANCELADA);
                return;
            }

            //25 - 37 Campo 40 Monto - Longitud 12
            String montoString = conversorAString(mensaje.subList(25, 37));
            Logger.debug("El monto recibido de la transaccion es " + montoString);
            //Se inicializa el valor de la cantidad, hay que evitar que sea reemplazado
            Amount = Long.parseLong(montoString);
            para.setAmount(Amount);

            //42 - 52 Campo 42 Numero de Caja - Longitud 10
            String numeroCaja = conversorAString(mensaje.subList(42, 52));
            Logger.debug("El numero de caja es " + numeroCaja);

            //57 - 59 Campo 48 Codigo de Respuesta - Longitud 2
            String codigoRespuesta = conversorAString(mensaje.subList(57, 59));
            Logger.debug("El codigo de respuesta de la transaccion es " + codigoRespuesta);

            //64 - 74 Campo 53 Numero de Transaccion - Longitud 10
            String numeroTransaccion = conversorAString(mensaje.subList(64, 74));
            Logger.debug("El numero de la transaccion es " + numeroTransaccion);


            //Campo 89 Tipo de Cuenta - Longitud 1, 1 Debido, 3 Credito
            String tipoCuenta = hexToAscii(mensaje.get(79));
            Logger.debug("El tipo de cuenta de la transaccion es " + tipoCuenta);
            if (tipoCuenta.equalsIgnoreCase("1")) isDebit = true;



            // Campo 51, Numero de Cuotas


            //80 ETX
            //81 LRC

        } catch (Exception e) {
            resetearTodo();
            transUI.showError(timeout, MENSAJE_ERROR_COMUNICACION_PROCESAR);
        }

    }*/

    /**
     * Metodo que desempaqueta los campos que llegan al MPK enviarle los datos al POS,
     * Tiene una longitud de 82
     * P40 - Monto Con ceros a la izquierda
     * P42 - Serial del MPK
     * P48 - Inidicacion si continua(0x58 0x58) o se cancela(0x20 0x20) la transaccion
     * P53 - Numero de la transaccion
     * P88 - Tipo de cuenta 1 (0x31) ahorros o 3 (0x33) credito
     *..,.,.CNG.N
     * @param mensaje El mensaje o la trama completa recibida
     */


    /*public int desempaquetarEnvioDatos2(ArrayList<String> mensaje) {
        //0 STX
        //1 - 2 Longitud
        //3 - 19 Transport header y presentation header

        //String montoString = conversorAString(40, Arrays.copyOfRange(mensaje,20,36));

        //Primero revisar el valor del codigo de respuesta, para saber si hay que continuar con la transaccion
        try {

            //25 - 37 Campo 40 Monto - Longitud 12
            String montoString = conversorAString(buscarDato(mensaje,40));
            Logger.debug("El monto recibido de la transaccion es " + montoString);
            //Se inicializa el valor de la cantidad, hay que evitar que sea reemplazado
            Amount = Long.parseLong(montoString);
            if(Amount <= 0){
                return -2;
            }else{
                para.setAmount(Amount);
            }

            //42 - 52 Campo 42 Numero de Caja - Longitud 10
            String numeroCaja = conversorAString(buscarDato(mensaje,42));
            Logger.debug("El numero de caja es " + numeroCaja);

            //57 - 59 Campo 48 Codigo de Respuesta - Longitud 2
            List<String> codRespBytes = buscarDato(mensaje, 48);
            if(!analizarCodigoRespuesta(codRespBytes)){
                Logger.debug("Transaccion cancelada");
                return 1;
            }
            String codigoRespuesta = conversorAString(codRespBytes);
            Logger.debug("El codigo de respuesta de la transaccion es " + codigoRespuesta);

            //64 - 74 Campo 53 Numero de Transaccion - Longitud 10
            String numeroTransaccion = conversorAString(buscarDato(mensaje,53));
            Logger.debug("El numero de la transaccion es " + numeroTransaccion);
            //TraceNo = numeroTransaccion;

            //79 Campo 88 Tipo de Cuenta - Longitud 1, 1 Debido, 3 Credito
            List<String> listTipoCuenta = buscarDato(mensaje,88);
            if(listTipoCuenta != null){
                String tipoCuenta = conversorAString(listTipoCuenta);
                Logger.debug("El tipo de cuenta de la transaccion es " + tipoCuenta);
                if (tipoCuenta.equalsIgnoreCase("1")) isDebit = true;
            }


            // Campo 51, Numero de Cuotas
            List<String> list = buscarDato(mensaje,51);
            if(list != null){
                String numeroCuotas = conversorAString(list);
                numCuotas = Integer.valueOf(numeroCuotas);
                Logger.debug("El numero de cuotas es " + numeroCuotas);
            }

            //campo 82 Monto Cashback - Longitud 12
            List<String> montoCashList = buscarDato(mensaje,82);
            if(montoCashList != null){
                String montoCashbackString = conversorAString(montoCashList);
                Logger.debug("El monto de cashback recibido de la transaccion es " + montoCashbackString);
                //Se inicializa el valor de la cantidad, hay que evitar que sea reemplazado
                AmountCashBack = Long.parseLong(montoCashbackString);
                if(AmountCashBack > 0)
                    para.setAmountCashBack(AmountCashBack);
            }

            //80 ETX
            //81 LRC

        } catch (Exception e) {
            Logger.debug("Error al desempaquetar los datos ");
            return 2;
        }
        return 0;
    }


    private byte[] ultimaTrans(){

        ACQUIRER_ROW acquirer_row = ACQUIRER_ROW.getSingletonInstance();
        acquirer_row.selectACQUIRER_ROW(StartAppATC.idAcquirer, context);

        byte[] ultimatx;
        byte[] terminalId;
        byte[] comercioId;
        String termID = ISOUtil.hex2AsciiStr(acquirerRow.getSb_term_id());
        String idAcquirer = ISOUtil.padright(ISOUtil.strpad(ISOUtil.hex2AsciiStr(acquirerRow.getSb_acceptor_id()), 15),23, ' ');
        Logger.debug("TerminalId: -----  "+termID);
        Logger.debug("idAcquirer: -----  "+idAcquirer);

        ultimatx=armarUltTrans(transLog);
        terminalId=crearCampo(termID, 45, 8);
        comercioId=crearCampo(idAcquirer, 77, 23);

        byte[] respuestaFinal = new byte[ultimatx.length + terminalId.length + comercioId.length];

        System.arraycopy(ultimatx,0,respuestaFinal, 0, ultimatx.length);
        System.arraycopy(terminalId, 0, respuestaFinal, ultimatx.length, terminalId.length);
        System.arraycopy(comercioId, 0, respuestaFinal, ultimatx.length+terminalId.length, comercioId.length);

        return respuestaFinal;
    }

    *//**
     * Metodo utilizado para armar la parte variable de la trama
     * Valida que tipo de transacción es y procede a armar la trama
     *
     * @param presentationHeader
     * @return
     *//*
    public byte[] armarParteVariable(String presentationHeader) {
        byte[] retorno = null;
        switch (presentationHeader) {
            case ULTIMA_TRANS:
                if (MediadorTodo.comercioCajas)
                    retorno = ultimaTrans();
                else
                    retorno = armarUltTrans(transLog);
                break;
            case NUEVA_PANTALLA:
                //se verifica si es chip/mag para enviar nueva pantalla
                if(tipoEntrada.equals(CHIP)) {
                    if (pilaTransEnviadas.get(pilaTransEnviadas.size() - 1).equalsIgnoreCase(SOLICITUD_DATOS)) {
                        retorno = armarNuevaPantallaPIN();
                    } else {
                        retorno = armarNuevaPantalla();
                    }
                }else{
                    if (pilaTransEnviadas.get(pilaTransEnviadas.size() - 1).equalsIgnoreCase(SOLICITUD_DATOS)) {
                        retorno = armarNuevaPantalla();
                    } else {
                        retorno = armarNuevaPantallaPIN();
                    }
                }
                break;
            case SOLICITUD_DATOS:
                if (tipoEntrada.equals(CHIP) && TMConfig.getInstance().isPreauthVoidPassSwitch())
                    retorno = solicituddeDatosBin();
                else
                    retorno=armarSolicitudDatos();
                break;
            case RESP_HOST:
            case RESP_HOST_CONTACTLESS:
                retorno = armarRespuestaHost();
                break;
        }
        return retorno;
    }

    private byte[] solicituddeDatosBin(){
        byte[] codigoRespuesta;
        byte[] bin;
        String panTemp=ISOUtil.padright(LocalPan.substring(0,8),12, ' ');


        codigoRespuesta=armarSolicitudDatos();
        bin=crearCampo(panTemp, 30, 12);

        byte respuestaFinal[] = new byte[codigoRespuesta.length+bin.length];

        System.arraycopy(codigoRespuesta,0,respuestaFinal, 0, codigoRespuesta.length);
        System.arraycopy(bin, 0, respuestaFinal, codigoRespuesta.length, bin.length);

        return respuestaFinal;
    }*/

    /**
     * Metodo utilizado para validar si la trama recibida corresponde a un ACK o NACK     *
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
     * Metodo utilizado cuando se recibe un NACK
     * valida que no haya recibido más de 3 NACK y vuelve a enviar la ultima transacción transacción
     * Si ha enviado más de 3 envía un NACK
     */
    /*protected void recibioNACK() {
        contadorErrores++;
        if (contadorErrores < 3 && !pilaTransEnviadas.isEmpty()) {
            //tiene que volver a enviar la transaccion
            Logger.debug("Ha ocurrido un error en la transaccion, reenviando ultima transaccion...");
            enviarMensaje(pilaTransEnviadas.get(pilaTransEnviadas.size() - 1));
        } else {
            resetearTodo();
            if(tipoConexion.equals("USB")){
                transUI.showError(timeout, MENSAJE_ERROR_COMUNICACION_USB);
            }else{
                transUI.showError(timeout, MENSAJE_ERROR_COMUNICACION_TCP_IP);
            }
        }
    }

    *//**
     * 准备联机
     *//*
    protected void prepareOnline() {
        transUI.handling(timeout, Tcode.Status.connecting_center);
        setDatas(inputMode);
        if (inputMode == ENTRY_MODE_ICC || inputMode == ENTRY_MODE_NFC) {
            retVal = OnlineTrans(emv);
        } else {
            retVal = OnlineTrans(null);
        }
        Logger.debug("VentaCajas>>OnlineTrans=" + retVal);
        clearPan();
        transUI.showMessage(MENSAJE_ESPERANDO_RESPUESTA);
        enviarMensaje(RESP_HOST);
    }*/

    /**
     * Metodo utilizado para armar la trama de acuerdo que será enviada a la caja
     *
     * @param tamTrama           tamaño que va a tener la trama completa
     * @param tipoTrans          tipo de transacción realizada ASCII
     * @param presentationHeader arreglo de byte del tipo de transacción
     * @return arreglo de bytes de la trama que será enviada
     */
    /*public byte[] armarTrama(int tamTrama, String tipoTrans, byte[] presentationHeader) {
        byte[] retorno = new byte[tamTrama];

        retorno[0] = STX;
        //en la pos [1]y[2] va la longitud de la trama
        //se lleva un registro de en que parte debemos escribir
        int con = 3;
        //agregar TransportHeader--desde pos [3] hasta pos[12]
        for (int i = 0; i < transportHeader.length; i++) {
            retorno[i + 3] = transportHeader[i];
            con++;
        }
        //agregar presentationHeader--desde posi [13]  hasta [19]
        for (int i = 0; i < presentationHeader.length; i++) {
            retorno[i + 13] = presentationHeader[i];
            con++;
        }
        //La longitud de este campo es variable dependiendo de lo que se vaya a enviar
        //campos --desde[20] hasta [37] en ultima transaccion
        //desde --[20] hasta 34 en nueva pantalla ingreso de tarjeta y nueva pantalla ingreso de PIN
        //desde --[20] hasta el 27 en solicitud de datos
        //desde --[20] hasta el [207] en respuesta host
        byte[] parteVariable = armarParteVariable(tipoTrans);
        for (int i = 0; i < parteVariable.length; i++) {
            retorno[i + 20] = parteVariable[i];
            con++;
        }
        int longMensaje = transportHeader.length + ULTIMA_TRANS_BYTE.length + parteVariable.length;
        byte[] longMsj = calcularLongitudMensaje(longMensaje);
        retorno[1] = longMsj[0];
        retorno[2] = longMsj[1];

        retorno[con] = ETX;
        con++;
        Logger.debug("contador ---" + con);
        retorno[con] = calcularLRC(retorno);
        Logger.debug("retorn [con] ---" + retorno[con]);

        return retorno;
    }

    protected boolean fidelizacion() {
        ProcessFileFidelizacion processFileFidelizacion = new ProcessFileFidelizacion(Pan, context);
        if ((retVal = processFileFidelizacion.checkBinInFile()) == 0) {
            nameClubFidelizacion = processFileFidelizacion.getNombreClub();
            return true;
        } else {
            return false;
        }
    }*/

    /**
     * Metodo utilizado para enviar la trama en su forma mas basica, un arreglo de bytes
     *
     * @param info --> trama que se va a enviar es un arreglo de bytes
     * @return
     */
    public abstract byte[] enviarTrama(byte info[]);

    /**
     * Metodo que permite enviar una trama dependiendo del tipo
     *
     * @param tipoMensaje el tipo de mensaje que se quiere enviar
     */
    public abstract void enviarMensaje(String tipoMensaje);

    /**
     * Metodo utilizado para validar si recibió un ACK o NACK     *
     *
     * @param hexa String en hexa que se va a valdiar si es NACK o ACK
     * @return
     */
    protected abstract String validarNACK_ACK(String hexa);

    /**
     * Metodo utilizado cuando se recibe solo un ACK, este valida la ultima transacción recibida
     * y continua con la transacción
     *
     * @param elemento ultimo elemento guardado en la pila
     *                 Creado por Silvia Hernandez
     */
    public abstract void recibioACK(String elemento);

    /**
     * Cuando no es un ACK o NACK entonces hay que ver que trama es para continuar su flujo correcto
     * metodo utilizado para continuar con el flujo, valida la transacción recibida
     *
     * @param trama
     */
    protected abstract void flujoTransaccion(ArrayList<String> trama);

    /**
     * Metodo que es llamado cuando se recibe un mensaje, se analiza con el fin de saber si es un ACK/NACK o un mensaje de otro tipo
     *
     * @param mensajeRecibido es un arraylist, donde está contenida la trama recibida
     *                        <p>
     *                        Creado por Silvia Hernandez
     */
    public abstract void recibirMensaje(ArrayList<String> mensajeRecibido);

}

