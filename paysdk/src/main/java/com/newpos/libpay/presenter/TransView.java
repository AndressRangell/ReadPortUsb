package com.newpos.libpay.presenter;

import com.newpos.libpay.device.user.OnUserResultListener;

/**
 * Created by zhouqiang on 2017/4/25.
 * User UI
 * @author zhouqiang
 */

public interface TransView {
    /**
     * show search card UI
     * @param timeout : s
     */
    void showCardView(int timeout);

    /**
     * show card number UI
     * @param timeout : s
     * @param pan : card number
     * @param l User confirm or cancel callback function, refer to:@{@link OnUserResultListener}
     */
    void showCardNo(int timeout, String pan, OnUserResultListener l);

    /**
     * show and select card applications
     * @param timeout timeout
     * @param apps application list
     * @param l User confirm or cancel callback function @{@link OnUserResultListener}
     * @return select index
     */
    int showCardAppListView(int timeout, String[] apps, OnUserResultListener l);

    /**
     * show transaction information
     * @param timeout timeout
     * @param status the detail information of transaction result
     */
    void showMsgInfo(int timeout, String status);
}
