package com.newpos.libpay.device.card;

import com.pos.device.SDKException;
import com.pos.device.apdu.CommandApdu;
import com.pos.device.apdu.ResponseApdu;
import com.pos.device.icc.ContactCard;
import com.pos.device.icc.IccReader;
import com.pos.device.icc.OperatorMode;
import com.pos.device.icc.SlotType;
import com.pos.device.icc.VCC;

/**
 * Created by zhouqiang on 2017/3/14.
 * @author zhouqiang
 * card manage
 */

public class CardManager {

    private static CardManager instance ;
    ContactCard contactCard;
    private byte[] transmit;
    private CardManager(){}

    public static CardManager getInstance(){
        if(null == instance){
            instance = new CardManager();
        }
        return instance ;
    }


    private IccReader iccReader ;

    public IccReader getIccReader() {
        return iccReader;
    }

    public ContactCard getCContactCard(){
        ContactCard contactCardSam = null;
        try {
            contactCardSam = iccReader.connectCard(VCC.VOLT_5, OperatorMode.EMV_MODE);
        } catch (SDKException e) {
            return null;
        }
        return contactCardSam;
    }

    public boolean isCardPresent(){
        return iccReader.isCardPresent();
    }

    public ResponseApdu transmit(ContactCard contactCard, CommandApdu cmdApdu){
        try {
            return iccReader.transmit(contactCard, cmdApdu);
        } catch (SDKException e) {
            return null;
        }
    }

    public void transmit(ContactCard contactCard, byte[] command){
        try {
            transmit = iccReader.transmit(contactCard, command);
        } catch (SDKException e) {
            transmit = null;
        }
    }

    public void init(){
        iccReader = IccReader.getInstance(SlotType.USER_CARD);
    }

    public byte[] getTransmit() {
        return transmit;
    }
}
