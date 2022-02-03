package com.newpos.libpay.device.ped;

import android.app.Activity;
import android.widget.Toast;

import com.newpos.libpay.device.logs.Logger;
import com.newpos.libpay.device.utils.ISOUtil;
import com.pos.device.SDKException;
import com.pos.device.ped.KeySystem;
import com.pos.device.ped.KeyType;
import com.pos.device.ped.Ped;

public class InjectKeys {

    public static final int MASTERKEYIDX = 0;
    public static final int WORKINGKEYIDX = 0;

    public InjectKeys(){}

    public static int injectMk(byte[] masterkey){
        return Ped.getInstance().injectKey(KeySystem.MS_DES, KeyType.KEY_TYPE_MASTK, MASTERKEYIDX, masterkey);
    }

    /**
     *
     * @param masterKey
     * @return
     */
    public static int injectMk(String masterKey) {
        byte[] masterKeyData = ISOUtil.str2bcd(masterKey, false);
        return Ped.getInstance().injectKey(KeySystem.MS_DES, KeyType.KEY_TYPE_MASTK, MASTERKEYIDX, masterKeyData);
    }

    /**
     *
     * @param workingKey
     * @return
     */
    public static int injectWorkingKey(String workingKey) {
        byte[] workingKeyData = ISOUtil.str2bcd(workingKey, false);
        return Ped.getInstance().writeKey(KeySystem.MS_DES, KeyType.KEY_TYPE_PINK, MASTERKEYIDX, WORKINGKEYIDX, Ped.KEY_VERIFY_NONE, workingKeyData);
    }

    public static byte[] threreIsKey(int indexKey){
        return Ped.getInstance().getWorkKeyKCV(KeySystem.MS_DES, KeyType.KEY_TYPE_MASTK, indexKey);
    }

    public static void deleteMasterKey(String msg, Activity activity){
        try {
            Ped.getInstance().deleteKey(KeySystem.MS_DES, KeyType.KEY_TYPE_MASTK, MASTERKEYIDX);
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
        } catch (SDKException e) {
            Logger.error(e + "");
        }
    }
}
