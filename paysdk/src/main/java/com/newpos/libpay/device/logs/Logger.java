package com.newpos.libpay.device.logs;

import android.util.Log;

/**
 * Created by zhouqiang on 2017/3/8.
 * @author zhouqiang
 * sdk全局日主输出
 */

public class Logger {
    
    private Logger() {
    }

    private static final String TAG = "LOGGER";

    public static void debug(String msg){
        Log.i(TAG, msg);
    }

    public static void error(String msg){
        Log.e(TAG , msg);
    }

    public static void info(String msg){
        Log.i(TAG , msg);
    }
}
