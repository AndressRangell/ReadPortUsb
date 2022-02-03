package com.newpos.libpay.device;

import android.content.Context;
import com.newpos.libpay.PaySdkListener;
import com.newpos.libpay.device.logs.Logger;
import com.pos.device.SDKManager;
import com.pos.device.SDKManagerCallback;

public class PaySdk {

    private static PaySdk mInstance = null ;


    private Context mContext = null;

    private PaySdkListener mListener = null ;

    private PaySdk(){}

    public static PaySdk getInstance(){
        if(mInstance == null){
            mInstance = new PaySdk();
        }
        return mInstance ;
    }

    public void init(Context context , PaySdkListener listener) {
        this.mContext = context ;
        this.mListener = listener ;
        this.init();
    }

    public void init() {

        SDKManager.init(mContext, new SDKManagerCallback() {
            @Override
            public void onFinish() {
                Logger.info("init->success");
                if(mListener!=null){
                    mListener.success();
                }

            }
        });
    }
}
