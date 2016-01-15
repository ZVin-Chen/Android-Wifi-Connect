package com.zvin.wificonnect.connection;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.text.TextUtils;

import com.zvin.wificonnect.util.LogUtil;
import com.zvin.wificonnect.util.WLANUtil;

/**
 * Created by chenzhengwen on 2015/12/18.
 */
public class WPAEAPWifiConnection extends WifiConnnection {
    public void setPasswd(String mPasswd) {
        this.mPasswd = mPasswd;
    }

    public void setIdentity(String mIdentity) {
        this.mIdentity = mIdentity;
    }

    private String mPasswd;
    private String mIdentity;

    public WPAEAPWifiConnection(Context context, ScanResult scanResult, String identity, String passwd) {
        super(context, scanResult);
        this.mPasswd = passwd;
        this.mIdentity = identity;
    }

    @Override
    public int prepare() {

        if(mWifiConfig == null){
            LogUtil.i(LogUtil.DEBUG_TAG, "WPAEAPWifiConnection new WifiConfiguration");
            if(TextUtils.isEmpty(mIdentity) || TextUtils.isEmpty(mPasswd)){
                LogUtil.i(LogUtil.DEBUG_TAG, "WPAEAPWifiConnection mIdentity or mPasswd is empty");
                return 2;
            }

            mWifiConfig = new WifiConfiguration();

            mWifiConfig.SSID = "\"" + getScanResult().SSID + "\"";

            mWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            mWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);

            if (Build.VERSION.SDK_INT < 8) {
                //template code
			/*config.eap.setValue("PEAP");
			if (!TextUtils.isEmpty(id)) {
				config.identity.setValue(convertToQuotedString(id));
	        }
			if (!TextUtils.isEmpty(pwd)) {
				config.password.setValue(convertToQuotedString(pwd));
	        }
			config.phase2.setValue(convertToQuotedString("auth=MSCHAPV2"));*/

                WLANUtil.setEnterpriseFieldValue(mWifiConfig, "eap", "PEAP");
                WLANUtil.setEnterpriseFieldValue(mWifiConfig, "identity", "\"" + mIdentity + "\"");
                WLANUtil.setEnterpriseFieldValue(mWifiConfig, "password", "\"" + mPasswd + "\"");
                WLANUtil.setEnterpriseFieldValue(mWifiConfig, "phase2", "\"auth=MSCHAPV2\"");
            }else if (Build.VERSION.SDK_INT >= 18) {
                mWifiConfig.enterpriseConfig.setEapMethod(0);
                mWifiConfig.enterpriseConfig.setIdentity(mIdentity);
                mWifiConfig.enterpriseConfig.setPassword(mPasswd);
                mWifiConfig.enterpriseConfig.setPhase2Method(3);

            } else {
                //template code
			/*config.eap.setValue("PEAP");
			if (!TextUtils.isEmpty(id)) {
	            config.identity.setValue(id);
	        }
			if (!TextUtils.isEmpty(pwd)) {
	            config.password.setValue(pwd);
	        }*/

                WLANUtil.setEnterpriseFieldValue(mWifiConfig, "eap", "PEAP");
                WLANUtil.setEnterpriseFieldValue(mWifiConfig, "identity", mIdentity);
                WLANUtil.setEnterpriseFieldValue(mWifiConfig, "password", mPasswd);
            }

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
        return 3;
    }

    @Override
    public boolean needAuth() {
        return true;
    }
}
