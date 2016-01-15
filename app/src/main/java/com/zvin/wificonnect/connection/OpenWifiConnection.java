package com.zvin.wificonnect.connection;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

import com.zvin.wificonnect.util.LogUtil;

/**
 * Created by chenzhengwen on 2015/12/16.
 */
public class OpenWifiConnection extends WifiConnnection {

    public OpenWifiConnection(Context context, ScanResult scanResult) {
        super(context, scanResult);
    }

    @Override
    public int prepare() {

        if(mWifiConfig == null){
            LogUtil.i(LogUtil.DEBUG_TAG, "OpenWifiConnection new WifiConfiguration");
            mWifiConfig = new WifiConfiguration();

            String tempSSID = getScanResult().SSID;
            LogUtil.i(LogUtil.DEBUG_TAG, "OpenWifiConnection prepare tempSSID=" + tempSSID);
            mWifiConfig.SSID = "\"" + tempSSID + "\"";
            LogUtil.i(LogUtil.DEBUG_TAG, "OpenWifiConnection prepare mWifiConfig.SSID=" + mWifiConfig.SSID);
            mWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            mNetworkId = mWifiManager.addNetwork(mWifiConfig);
        }else{
            mNetworkId = mWifiConfig.networkId;
        }

        LogUtil.i(LogUtil.DEBUG_TAG, "OpenWifiConnection prepare networkid=" + mNetworkId);

        if(mNetworkId == -1){
            return 4;
        }

        return 1;
    }

    @Override
    public int getRetryTime() {
        return 2;
    }

    @Override
    public boolean needAuth() {
        return false;
    }

}
