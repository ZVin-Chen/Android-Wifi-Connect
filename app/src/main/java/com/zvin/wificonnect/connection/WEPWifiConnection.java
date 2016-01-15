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
public class WEPWifiConnection extends WifiConnnection {
    public void setPasswd(String mPasswd) {
        this.mPasswd = mPasswd;
    }

    private String mPasswd;

    public WEPWifiConnection(Context context, ScanResult scanResult, String passwd) {
        super(context, scanResult);
        this.mPasswd = passwd;
    }

    @Override
    public int prepare() {

        if(mWifiConfig == null){
            LogUtil.i(LogUtil.DEBUG_TAG, "WEPWifiConnection new WifiConfiguration");

            if(TextUtils.isEmpty(mPasswd)){
                LogUtil.i(LogUtil.DEBUG_TAG, "WEPWifiConnection mPasswd is empty");
                return 2;
            }

            mWifiConfig = new WifiConfiguration();

            String tempSSID = getScanResult().SSID;
            LogUtil.i(LogUtil.DEBUG_TAG, "WEPWifiConnection prepare tempSSID=" + tempSSID);
            mWifiConfig.SSID = "\"" + tempSSID + "\"";
            LogUtil.i(LogUtil.DEBUG_TAG, "WEPWifiConnection prepare mWifiConfig.SSID=" + mWifiConfig.SSID);

            if(WLANUtil.isHexWepKey(mPasswd)){
                mWifiConfig.wepKeys[0] = mPasswd;
            }else{
                mWifiConfig.wepKeys[0] = "\"" + mPasswd + "\"";
            }

            mWifiConfig.wepTxKeyIndex = 0;
            mWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            mWifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);

            mNetworkId = mWifiManager.addNetwork(mWifiConfig);
        }else{
            mNetworkId = mWifiConfig.networkId;
        }

        LogUtil.i(LogUtil.DEBUG_TAG, "WEPWifiConnection prepare networkid=" + mNetworkId);

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
        return true;
    }
}
