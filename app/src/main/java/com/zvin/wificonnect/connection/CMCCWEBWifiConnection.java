package com.zvin.wificonnect.connection;

import android.content.Context;
import android.net.wifi.ScanResult;

import com.zvin.wificonnect.prelogin.CMCCWEBPreloginProcess;
import com.zvin.wificonnect.prelogin.PreloginProcess;

/**
 * Created by chenzhengwen on 2015/12/24.
 */
public class CMCCWEBWifiConnection extends OpenWifiConnection {
    private PreloginProcess mPreloginProcess;

    public CMCCWEBWifiConnection(Context context, ScanResult scanResult) {
        super(context, scanResult);
        mPreloginProcess = new CMCCWEBPreloginProcess();
    }

    @Override
    public PreloginProcess getPreloginProcess() {
        return mPreloginProcess;
    }
}
