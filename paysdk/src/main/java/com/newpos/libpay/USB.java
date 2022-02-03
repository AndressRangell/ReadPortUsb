package com.newpos.libpay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import com.newpos.libpay.device.ped.InjectKeys;

public class USB extends BroadcastReceiver {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    UsbManager usbManager;
    UsbDeviceConnection connection;
    UsbInterface intf;
    UsbEndpoint endpoint;
    UsbDevice device;


    public void dataSend() {
        intf = device.getInterface(0);
        endpoint = intf.getEndpoint(0);
        connection = usbManager.openDevice(device);
        connection.claimInterface(intf, true);
        byte[] mk = InjectKeys.threreIsKey(0);
        int ret = connection.controlTransfer((int) 0x80, (int) 0x06, (int) 0x200, (int) 0x00, mk, mk.length, 3000); //do in another thread
        Log.e("TAG", "dataSend: Ret "+ret );
        if (ret < 0) {
            Log.d("TAG", "Error happened!");
        } else if (ret == 0) {
            Log.d("TAG", "No data transferred!");
        } else {
            Log.d("TAG", "success!");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if(device != null){
                        //call method to set up device communication
                        dataSend();
                    }
                }
                else {
                    Log.d("TAG", "permission denied for device " + device);
                }
            }
        }
    }
}
