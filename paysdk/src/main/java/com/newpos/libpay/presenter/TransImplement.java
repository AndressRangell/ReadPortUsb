package com.newpos.libpay.presenter;

import com.android.desert.keyboard.InputManager;
import com.newpos.libpay.device.logs.Logger;
import com.newpos.libpay.device.user.OnUserResultListener;

/**
 * Created by zhouqiang on 2017/4/25.
 * @author zhouqiang
 * MVP --> P
 * Data collection and distribution, logic processing
 */

public class TransImplement implements TransInterface {

    private TransView transView = null;

    private int mRet = 0 ;
    private int timeout ;

    public TransImplement(TransView tv){
        this.transView = tv ;
        this.timeout = 60000;
    }

    /**
     * object lock
     */
    private Object o = new byte[0] ;

    /**
     * Notify
     */
    private void listenNotify(){
        synchronized (o){
            o.notify();
        }
    }

    /**
     * block
     */
    private void funWait(){
        synchronized (o){
            try {
                o.wait();
            } catch (InterruptedException e) {
                Logger.error(e + "");
            }
        }
    }

    /**
     * user confirm or cancel listener
     */
    final OnUserResultListener listener = new OnUserResultListener() {
        @Override
        public void confirm(InputManager.Style style) {
            mRet = 0 ;
            listenNotify();
        }

    };

    @Override
    public int confirmCardNO(String cn) {
        transView.showCardNo(timeout, cn, listener);
        funWait();
        transView.showMsgInfo(timeout , "PROCESANDO");
        return mRet;
    }

    @Override
    public int choseAppList(String[] list) {
        int ret = transView.showCardAppListView(timeout, list, listener);
        funWait();
        transView.showMsgInfo(timeout , "PROCESANDO");
        return ret;
    }

    @Override
    public void handling() {
        transView.showMsgInfo(timeout , "PROCESANDO");
    }

    @Override
    public void showMessage() {
        transView.showMsgInfo(timeout , "ERROR AL LEER TARJETA");
    }

}
