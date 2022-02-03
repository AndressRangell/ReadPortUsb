package com.newpos.libpay.presenter;


/**
 * Created by zhouqiang on 2017/3/15.
 * @author zhouqiang
 * 交易属性接口类
 */

public interface TransPresenter {
    /**
     * 开始交易流程MODEL接口
     * 用户可以通过此接口进行某一交易流程的入口开始
     */
    void start();

}
