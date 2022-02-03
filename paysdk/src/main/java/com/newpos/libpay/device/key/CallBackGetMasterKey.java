package com.newpos.libpay.device.key;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import com.newpos.libpay.device.card.CardManager;
import com.newpos.libpay.device.utils.ISOUtil;
import com.pos.device.apdu.CommandApdu;
import com.pos.device.apdu.ResponseApdu;
import com.pos.device.icc.ContactCard;

/**
 * Created by Acer on 5/02/2018.
 */

public class CallBackGetMasterKey extends AsyncTask<String, Integer, String> {

    private FileCallback callback;
    public static final String ERR = "1";
    public static final String CANCEL = "2";
    public static final String OK = "3";
    public static final String TIMEOUT = "4";

    private String accessCard;
    CardManager iccReader0 = CardManager.getInstance();

    public CallBackGetMasterKey(String accessCard, FileCallback callback) {
        this.callback = callback;
        this.accessCard = accessCard;
    }

    @Override
    protected String doInBackground(String... params) {

        boolean cardPresent;
        String hex1 = null;
        String hex2;
        String rta = ERR;
        long start = SystemClock.uptimeMillis();

        try {
            while (true) {

                if(timeout(start).equals(TIMEOUT)) {
                    rta = TIMEOUT;
                    break;
                }

                if (isCancelled()) {
                    rta = CANCEL;
                    break;
                }

                iccReader0.init();

                ContactCard contactCardSam = iccReader0.getCContactCard();

                if (contactCardSam != null) {

                    callback.statusCard(true);

                    cardPresent = iccReader0.isCardPresent();

                    if (cardPresent) {

                        byte[] apdu;
                        CommandApdu cmdApdu;

                        apdu = cardCmd();
                        if (apdu == null) {
                            break;
                        }
                        cmdApdu = new CommandApdu(apdu);

                        //SELECT FILE
                        if(!validateRspApdu(contactCardSam, cmdApdu,"6114").equals(OK)){
                            break;
                        }

                        apdu = cardNewCmdPin();
                        if (apdu == null) {
                            break;
                        }
                        cmdApdu = new CommandApdu(apdu);

                        //Verificacion pin
                        if(!validateRspApdu(contactCardSam, cmdApdu,"9000").equals(OK)){
                            break;
                        }

                        //get master key part 1
                        iccReader0.transmit(contactCardSam, convertCmd("00B0850010"));
                        byte[] rspData = iccReader0.getTransmit();
                        if (rspData != null) {
                            hex1 = ISOUtil.bcd2str(rspData, 0, rspData.length * 2, false);
                        }

                        //get master key part 2
                        iccReader0.transmit(contactCardSam, convertCmd("00B0860010"));
                        byte[] rspData3 = iccReader0.getTransmit();

                        if (rspData3 != null) {

                            hex2 = ISOUtil.bcd2str(rspData3, 0, rspData3.length * 2, false);

                            String auxMk1 = ISOUtil.convertHexToString(hex1);
                            String auxMk2 = ISOUtil.convertHexToString(hex2);

                            rta = auxMk1.substring(0, auxMk1.length() - 2) + auxMk2.substring(0, auxMk2.length() - 2);
                            break;
                        }
                        rta = OK;
                        break;
                    }

                } else {
                    callback.statusCard(false);
                }
            }//end while

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }

        return rta;

    }

    private byte[] convertCmd(String cmd){
        return ISOUtil.str2bcd(cmd, false);
    }

    private String timeout(long start){
        if ((SystemClock.uptimeMillis() - start) > (60 * 1000)) {
            return TIMEOUT;
        }else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return OK;
    }

    private byte[] cardNewCmdPin(){

        String cmdApduPin = "00200000" + "0" + (accessCard.length()/2) + accessCard;
        return ISOUtil.str2bcd(cmdApduPin, false);
    }

    private byte[] cardCmd(){

        String cmdApduPin = "00A40000021001";
        return ISOUtil.str2bcd(cmdApduPin, false);

    }

    private String validateRspApdu(ContactCard contactCard, CommandApdu command, String cmd) {
        String rta;
        try {
            ResponseApdu rspApdu = iccReader0.transmit(contactCard, command);
            String respuestaAPDU = null;
            if (rspApdu != null) {
                respuestaAPDU = ISOUtil.bcd2str(rspApdu.getBytes(), 0, rspApdu.getBytes().length * 2, false);
            }

            if (respuestaAPDU != null && !respuestaAPDU.equals(cmd)) {
                rta = ERR;
            } else
                rta = OK;
        }catch (Exception e){
            rta = ERR;
        }
        return rta;
    }

    @Override
    protected void onPostExecute(String text) {
        super.onPostExecute(text);
        callback.rspUnpack(text);
    }

    public interface FileCallback {
        void rspUnpack(String okUnpack);
        void statusCard(boolean isCard);
    }
}
