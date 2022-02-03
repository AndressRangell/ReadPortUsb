package com.newpos.libpay.device.card;

/**
 * Created by zhouqiang on 2017/3/26.
 * @author zhouqiang
 * Transaction return code
 */

public class Tcode {

    public static final int START = 300 ;

    private Tcode(){

    }

    /**
     * search card failed
     */
    public static final int SEARCH_CARD_FAIL = START + 5;

}
