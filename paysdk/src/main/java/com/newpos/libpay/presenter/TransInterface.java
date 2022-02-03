package com.newpos.libpay.presenter;

/**
 * Created by zhouqiang on 2017/3/15.
 * @author zhouqiang
 * define MODEL interface
 */

public interface TransInterface {

    /**
     * notice user confirm card number
     * @param cn card number
     * @return 0:user confirm  others:user cancel
     */
    int confirmCardNO(String cn);

    /**
     * show and select card app
     * @param list card app list
     * @return card app index
     * @attention index start with 0
     */
    int choseAppList(String[] list);

    /**
     * show transaction status
     */
    void handling();

    void showMessage();
}
