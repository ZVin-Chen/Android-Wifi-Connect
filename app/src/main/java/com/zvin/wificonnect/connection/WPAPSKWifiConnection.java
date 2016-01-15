package com.zvin.wificonnect.connection;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.text.TextUtils;

import com.zvin.wificonnect.util.LogUtil;
import com.zvin.wificonnect.util.WLANUtil;

/**
 * Created by chenzhengwen on 2015/12/17.
 */
public class WPAPSKWifiConnection extends WifiConnnection {
    public void setPasswd(String mPasswd) {
        this.mPasswd = mPasswd;
    }

    private String mPasswd;

    public WPAPSKWifiConnection(Context context, ScanResult scanResult, String passwd) {
        super(context, scanResult);
        this.mPasswd = passwd;
    }

    @Override
    public int prepare() {

        if(mWifiConfig == null){
            LogUtil.i(LogUtil.DEBUG_TAG, "WPAPSKWifiConnection new WifiConfiguration");
            if(TextUtils.isEmpty(mPasswd)){
                LogUtil.i(LogUtil.DEBUG_TAG, "WPAPSKWifiConnection prepare mPasswd is empty");
                return 2;
            }

            mWifiConfig = new WifiConfiguration();

            mWifiConfig.SSID = "\"" + getScanResult().SSID + "\"";

            if(mPasswd.length() == 64 && WLANUtil.isHex(mPasswd)){
                mWifiConfig.preSharedKey = mPasswd;
            }else{
                mWifiConfig.preSharedKey = "\"" + mPasswd + "\"";
            }

            mNetworkId = mWifiManager.addNetwork(mWifiConfig);
        }else{
            mNetworkId = mWifiConfig.networkId;
        }

        LogUtil.i(LogUtil.DEBUG_TAG, "WPAPSKWifiConnection prepare networkid=" + mNetworkId);

        if(mNetworkId == -1){
            return 4;
        }

        return 1;
    }

    @Override
    public int getRetryTime() {
        return 3;
    }

    @Override
    public boolean needAuth() {
        return true;
    }
}
